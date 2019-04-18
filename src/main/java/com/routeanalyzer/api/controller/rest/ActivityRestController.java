package com.routeanalyzer.api.controller.rest;

import com.routeanalyzer.api.common.CommonUtils;
import com.routeanalyzer.api.common.Response;
import com.routeanalyzer.api.database.ActivityMongoRepository;
import com.routeanalyzer.api.logic.ActivityOperations;
import com.routeanalyzer.api.logic.file.export.impl.GpxExportFileService;
import com.routeanalyzer.api.logic.file.export.impl.TcxExportFileService;
import com.routeanalyzer.api.model.Activity;
import com.routeanalyzer.api.model.Lap;
import io.vavr.control.Try;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static com.routeanalyzer.api.common.CommonUtils.getFileExportResponse;
import static com.routeanalyzer.api.logic.impl.LapsOperationsImpl.COLOR_DELIMITER;
import static com.routeanalyzer.api.logic.impl.LapsOperationsImpl.COMMA_DELIMITER;
import static com.routeanalyzer.api.logic.impl.LapsOperationsImpl.LAP_DELIMITER;
import static com.routeanalyzer.api.logic.impl.LapsOperationsImpl.STARTED_HEX_CHAR;
import static java.util.Optional.ofNullable;
import static org.springframework.http.ResponseEntity.notFound;

@RestController
@RequestMapping("activity")
@CrossOrigin(origins = "*", maxAge = 3600)
public class ActivityRestController extends RestControllerBase {

	@Autowired
	private ActivityMongoRepository mongoRepository;
	@Autowired
	private ActivityOperations activityOperationsService;
	@Autowired
	private TcxExportFileService tcxExportService;
	@Autowired
	private GpxExportFileService gpxExportService;

	public ActivityRestController() {
		super(LoggerFactory.getLogger(ActivityRestController.class));
	}

	@GetMapping(value = "/{id}", produces = "application/json;")
	public @ResponseBody ResponseEntity<String> getActivityById(@PathVariable String id) {
		return getOptionalActivityById(id)
				.map(CommonUtils::toOKMessageResponse)
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
								.map(CommonUtils::toOKMessageResponse)))
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
				.map(CommonUtils::toOKMessageResponse)
				.orElseGet(CommonUtils::toBadRequestParams);
	}

	@PutMapping(value = "/{id}/split/lap", produces = "application/json;")
	public @ResponseBody ResponseEntity<String> splitLap(@PathVariable String id, @RequestParam String lat,
			@RequestParam String lng, @RequestParam String timeInMillis, @RequestParam String index) {
		return getOptionalActivityById(id)
				.map(activity -> activityOperationsService.splitLap(activity, lat, lng, timeInMillis, index))
				.map(mongoRepository::save)
				.map(CommonUtils::toOKMessageResponse)
				.orElseGet(CommonUtils::toBadRequestParams);
	}

	@PutMapping(value = "/{id}/remove/laps", produces = "application/json;")
	public @ResponseBody ResponseEntity<String> removeLaps(@PathVariable String id,
			@RequestParam(name = "date") String startTimeLaps, @RequestParam(name = "index") String indexLaps) {
	    Function<String, List<String>> splitStringByComma = string -> ofNullable(string).map(stringParam ->
                CommonUtils.splitStringByDelimiter(stringParam, COMMA_DELIMITER)).orElseGet(Collections::emptyList);
	    return getOptionalActivityById(id)
				.flatMap(activity -> ofNullable(indexLaps)
						.map(splitStringByComma)
						.map(indexStrings -> this.toListType(indexStrings, Integer::parseInt))
						.map(index -> ofNullable(startTimeLaps)
								.map(splitStringByComma)
								.map(indexStrings -> this.toListType(indexStrings, Long::new))
								.map(datesList -> activityOperationsService
										.removeLaps(activity, datesList, index))
								.orElseGet(() -> activityOperationsService
										.removeLaps(activity, null, index)))
						.map(mongoRepository::save))
				.map(CommonUtils::toOKMessageResponse)
				.orElseGet(() -> CommonUtils.toBadRequestParams());
	}

	@PutMapping(value = "/{id}/color/laps")
	public @ResponseBody ResponseEntity<String> setColorLap(@PathVariable String id, @RequestParam String data) {
        Supplier<AtomicInteger> atomicIntegerSupplier = () -> new AtomicInteger();
        Supplier<Response> okSupplier = () -> new Response(false,
                "Lap's colors are updated.", null, null);
        Supplier<Response> badRequestSupplier = () -> new Response(true,
                "Not being possible to update lap's colors.", null, null);
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
                .map(activity -> okSupplier.get())
                .map(CommonUtils::toOKMessageResponse)
                .orElseGet(() -> CommonUtils.toBadRequestResponse(badRequestSupplier.get()));
	}

	private <T> List<T> toListType(List<String> listStrings, Function<String, T> convertTo) {
		return ofNullable(listStrings)
				.map(List::size)
				.map(maxSizeDates -> IntStream.range(0, maxSizeDates))
				.map(intStream -> intStream.mapToObj(Integer::new))
				.map(indexes -> indexes.map(listStrings::get)
						.map(convertTo).collect(Collectors.toList()))
				.orElseGet(Collections::emptyList);
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
						.map(file -> getFileExportResponse(file, activity.getId(), sourceTcx))
						.recover(RuntimeException.class, handleControllerExceptions)
						.getOrElse(notFound().build());
			case sourceGpx:
				return Try.of(() -> gpxExportService.export(activity))
						.map(file -> getFileExportResponse(file, activity.getId(), sourceGpx))
						.recover(RuntimeException.class, handleControllerExceptions)
						.getOrElse(notFound().build());
			default:
				return CommonUtils.toBadRequestParams();
		}
	}

}
