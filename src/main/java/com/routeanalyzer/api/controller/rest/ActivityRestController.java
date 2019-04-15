package com.routeanalyzer.api.controller.rest;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import javax.xml.bind.JAXBException;

import com.amazonaws.AmazonClientException;
import com.routeanalyzer.api.common.CommonUtils;
import com.routeanalyzer.api.common.JsonUtils;
import com.routeanalyzer.api.logic.file.export.impl.GpxExportFileService;
import com.routeanalyzer.api.logic.file.export.impl.TcxExportFileService;
import com.routeanalyzer.api.common.Response;
import com.routeanalyzer.api.model.Lap;
import io.vavr.control.Try;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.routeanalyzer.api.database.ActivityMongoRepository;
import com.routeanalyzer.api.logic.ActivityOperations;
import com.routeanalyzer.api.model.Activity;
import org.xml.sax.SAXParseException;

import static com.routeanalyzer.api.common.CommonUtils.toJsonHeaders;
import static com.routeanalyzer.api.logic.impl.LapsOperationsImpl.COMMA_DELIMITER;
import static org.springframework.http.ResponseEntity.ok;
import static org.springframework.http.ResponseEntity.badRequest;
import static org.springframework.http.ResponseEntity.notFound;
import static org.springframework.http.ResponseEntity.status;
import static com.routeanalyzer.api.common.JsonUtils.toJson;
import static java.util.Optional.ofNullable;
import static com.routeanalyzer.api.logic.impl.LapsOperationsImpl.COLOR_DELIMITER;
import static com.routeanalyzer.api.logic.impl.LapsOperationsImpl.LAP_DELIMITER;
import static com.routeanalyzer.api.logic.impl.LapsOperationsImpl.STARTED_HEX_CHAR;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("activity")
public class ActivityRestController {
	
	private final Logger log = LoggerFactory.getLogger(ActivityRestController.class);

	@Autowired
	private ActivityMongoRepository mongoRepository;
	@Autowired
	private ActivityOperations activityOperationsService;
	@Autowired
	private TcxExportFileService tcxExportService;
	@Autowired
	private GpxExportFileService gpxExportService;

	@GetMapping(value = "/{id}", produces = "application/json;")
	public @ResponseBody ResponseEntity<String> getActivityById(@PathVariable String id) {
		return getOptionalActivityById(id)
				.map(JsonUtils::toJson)
				.map(ok().headers(CommonUtils.toJsonHeaders())::body)
				.orElseGet(CommonUtils::toBadRequestParams);
	}

	@GetMapping(value = "/{id}/export/{type}")
	public ResponseEntity<String> exportAs(@PathVariable final String id, @PathVariable final String type) {
		return getOptionalActivityById(id)
				.flatMap(activity -> ofNullable(type)
						.filter(StringUtils::isNotEmpty)
						.map(String::toLowerCase)
						.map(typeLowerCase -> getResponseByType(typeLowerCase, activity)))
				.orElseGet(CommonUtils::toBadRequestParams);

	}

	@PutMapping(value = "/{id}/remove/point")
	public @ResponseBody ResponseEntity<String> removePoint(@PathVariable String id, @RequestParam String lat,
			@RequestParam String lng, @RequestParam String timeInMillis, @RequestParam String index) {
		return getOptionalActivityById(id)
				.flatMap(activity -> ofNullable(lat)
						.filter(StringUtils::isNotEmpty)
						.flatMap(latitude -> ofNullable(lng)
								.filter(StringUtils::isNotEmpty)
								.map(longitude -> activityOperationsService
										.removePoint(activity, latitude, longitude, timeInMillis, index))
								.map(mongoRepository::save)
								.map(JsonUtils::toJson)
								.map(ok().headers(CommonUtils.toJsonHeaders())::body)))
				.orElseGet(CommonUtils::toBadRequestParams);
	}

	@PutMapping(value = "/{id}/join/laps")
	public @ResponseBody ResponseEntity<String> joinLaps(@PathVariable String id,
			@RequestParam(name = "index1") String indexLeft, @RequestParam(name = "index2") String indexRight) {
		return ofNullable(id)
				.filter(StringUtils::isNotEmpty)
				.flatMap(mongoRepository::findById)
				.map(activity -> activityOperationsService.joinLaps(activity, indexLeft, indexRight))
				.map(mongoRepository::save)
				.map(JsonUtils::toJson)
				.map(badRequest().headers(CommonUtils.toJsonHeaders())::body)
				.orElseGet(CommonUtils::toBadRequestParams);
	}

	@PutMapping(value = "/{id}/split/lap", produces = "application/json;")
	public @ResponseBody ResponseEntity<String> splitLap(@PathVariable String id, @RequestParam String lat,
			@RequestParam String lng, @RequestParam String timeInMillis, @RequestParam String index) {
		return getOptionalActivityById(id)
				.map(activity -> activityOperationsService.splitLap(activity, lat, lng, timeInMillis, index))
				.map(mongoRepository::save)
				.map(JsonUtils::toJson)
				.map(ok().headers(toJsonHeaders())::body)
				.orElseGet(CommonUtils::toBadRequestParams);
	}

	@PutMapping(value = "/{id}/remove/laps", produces = "application/json;")
	public @ResponseBody ResponseEntity<String> removeLaps(@PathVariable String id,
			@RequestParam(name = "date") String startTimeLaps, @RequestParam(name = "index") String indexLaps) {
	    Function<String, List<String>> splitStringByComma = string -> ofNullable(string).map(stringParam ->
                CommonUtils.splitStringByDelimiter(stringParam, COMMA_DELIMITER)).orElseGet(Collections::emptyList);
	    Supplier<Response> errorResponse = () -> new Response(true,
                "Given activity id not found in database.", null);
	    return getOptionalActivityById(id)
				.flatMap(activity -> ofNullable(indexLaps)
						.map(splitStringByComma)
						.flatMap(index -> ofNullable(startTimeLaps)
								.map(splitStringByComma)
								.map(this::toListDates)
								.flatMap(datesList -> ofNullable(index)
										.map(List::size)
										.map(maxSizeIndex -> IntStream.range(0, maxSizeIndex))
										.map(intStream -> intStream.mapToObj(Integer::new))
										.map(indexes -> indexes.map(indexParam ->
												ofNullable(indexParam)
														.map(index::get)
														.map(Integer::parseInt)
														.map(indexLap -> ofNullable(datesList)
																.filter(List::isEmpty)
																.map(datesListParam -> activityOperationsService
																		.removeLap(activity, null, indexLap))
																.orElseGet(() -> activityOperationsService
																		.removeLap(activity, datesList.get(indexParam), indexLap)))
														.orElseGet(null))
												.collect(Collectors.toList())))
										.map(mongoRepository::saveAll)))
				.map(JsonUtils::toJson)
				.map(CommonUtils::toOKMessageResponse)
				.orElseGet(() -> CommonUtils.toBadRequestResponse(errorResponse.get()));
	}

	private List<Long> toListDates(List<String> dates) {
	    return ofNullable(dates)
                .map(List::size)
                .map(maxSizeDates -> IntStream.range(0, maxSizeDates))
                .map(intStream -> intStream.mapToObj(Integer::new))
                .map(indexes -> indexes.map(dates::get)
                        .map(Long::parseLong).collect(Collectors.toList()))
                .orElseGet(Collections::emptyList);
    }

	@PutMapping(value = "/{id}/color/laps")
	public @ResponseBody ResponseEntity<String> setColorLap(@PathVariable String id, @RequestParam String data) {
        Supplier<AtomicInteger> atomicIntegerSupplier = () -> new AtomicInteger();
        Supplier<Response> okSupplier = () -> new Response(false,
                "Lap's colors are updated.", null);
        Supplier<Response> badRequestSupplier = () -> new Response(true,
                "Not be posible to update lap's colors.", null);
		return getOptionalActivityById(id)
				.flatMap(activity -> ofNullable(data)
						.map(this::toLapList)
						.flatMap(laps -> ofNullable(atomicIntegerSupplier)
								.map(Supplier::get)
								.map(indexLap -> laps
                                        .stream()
										.map(lapColor -> this.setColorLap(activity, lapColor, indexLap)))
                                .map(lapStream -> lapStream.collect(Collectors.toList())))
                        .map(laps -> activity))
                .map(mongoRepository::save)
                .map(activity -> okSupplier)
                .map(Supplier::get)
                .map(CommonUtils::toOKMessageResponse)
                .orElseGet(() -> CommonUtils.toBadRequestResponse(badRequestSupplier.get()));
	}

    private List<String> toLapList(String lapsStr) {
        return ofNullable(LAP_DELIMITER)
                .filter(lapsStr::contains)
                .map(lapsStr::split)
                .map(Arrays::asList)
                .orElseGet(() -> ofNullable(lapsStr)
                        .map(Arrays::asList)
                        .orElseGet(Collections::emptyList));
    }

    private Lap setColorLap(Activity activity, String lapColor, AtomicInteger indexLap) {
        Function<String, String> addHexPrefix = color -> STARTED_HEX_CHAR + color;
        // [color(hex)-lightColor(hex)]@[...]... number without #
        return ofNullable(COLOR_DELIMITER)
                .map(lapColor::split)
                .flatMap(colorsArray -> ofNullable(colorsArray[0])
                        .map(addHexPrefix)
                        .flatMap(hexColor -> ofNullable(colorsArray[1])
                                .map(addHexPrefix)
                                .flatMap(hexLightColor -> ofNullable(activity)
                                        .map(Activity::getLaps)
                                        .flatMap(lapsParam -> ofNullable(indexLap)
                                                .map(AtomicInteger::getAndIncrement)
                                                .map(lapsParam::get)
                                                .map(lapParamColor -> {
                                                    lapParamColor.setColor(hexColor);
                                                    lapParamColor.setLightColor(hexLightColor);
                                                    return lapParamColor;
                                                })
                                        )
                                )
                        )
                ).orElse(null);
    }

	private Optional<Activity> getOptionalActivityById(String id) {
		return ofNullable(id)
				.filter(StringUtils::isNotEmpty)
				.flatMap(mongoRepository::findById);
	}

	private ResponseEntity<String> getResponseByType(String typeLowercase, Activity activity) {
		final String sourceTcx = "tcx";
		final String sourceGpx = "gpx";
		switch (typeLowercase) {
			case sourceTcx:
				return Try.of(() -> tcxExportService.export(activity))
						.map(file -> CommonUtils.getFileExportResponse(file, activity.getId(), sourceTcx))
						.recover(RuntimeException.class, handleExceptions)
						.getOrElse(notFound().build());
			case sourceGpx:
				return Try.of(() -> gpxExportService.export(activity))
						.map(file -> CommonUtils.getFileExportResponse(file, activity.getId(), sourceGpx))
						.recover(RuntimeException.class, handleExceptions)
						.getOrElse(notFound().build());
			default:
				return CommonUtils.toBadRequestParams();
		}
	}

	private Function<RuntimeException, ResponseEntity<String>> handleExceptions = error -> {
		String logMessage = null;
		String description = null;
		String errorMessage = null;
		ResponseEntity.BodyBuilder bodyBuilder = null;
		if (SAXParseException.class.isInstance(error)) {
			SAXParseException exception = SAXParseException.class.cast(error);
			logMessage = "SAXParseException error: " + error.getMessage();
			description = "Problem trying to parser xml file. Check if its correct.";
			errorMessage = exception.getMessage();
			bodyBuilder = badRequest().headers(CommonUtils.toJsonHeaders());
		} else if (JAXBException.class.isInstance(error)) {
			JAXBException exception = JAXBException.class.cast(error);
			logMessage = "JAXBException error: " + error.getMessage();
			description = "Problem with the file format exported/uploaded.";
			errorMessage = exception.getMessage();
			bodyBuilder = status(HttpStatus.INTERNAL_SERVER_ERROR).headers(CommonUtils.toJsonHeaders());
		} else if(AmazonClientException.class.isInstance(error)){
			AmazonClientException exception = AmazonClientException.class.cast(error);
			logMessage = "AmazonClientException error: " + error.getMessage();
			description = "Problem trying to delete/get the activity/file :: Amazon S3 Problem";
			errorMessage = exception.getMessage();
			bodyBuilder = status(HttpStatus.INTERNAL_SERVER_ERROR).headers(CommonUtils.toJsonHeaders());
		} else if(IOException.class.isInstance(error)) {
			IOException exception = IOException.class.cast(error);
			logMessage = "IOException error: " + error.getMessage();
			description = "Problem trying to get the file :: Input/Output Problem";
			errorMessage = exception.getMessage();
			bodyBuilder = status(HttpStatus.INTERNAL_SERVER_ERROR).headers(CommonUtils.toJsonHeaders());
		}
		log.error(logMessage);
		Response errorBody = new Response(true, description, errorMessage);
		return bodyBuilder.body(toJson(errorBody));
	};
}
