package com.routeanalyzer.api.controller.rest;

import com.routeanalyzer.api.common.CommonUtils;
import com.routeanalyzer.api.common.JsonUtils;
import com.routeanalyzer.api.controller.Response;
import com.routeanalyzer.api.database.ActivityMongoRepository;
import com.routeanalyzer.api.logic.ActivityOperations;
import com.routeanalyzer.api.logic.file.export.ExportFileService;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.IntStream;

import static com.routeanalyzer.api.common.CommonUtils.getFileExportResponse;
import static com.routeanalyzer.api.common.CommonUtils.splitStringByDelimiter;
import static com.routeanalyzer.api.common.CommonUtils.toJsonHeaders;
import static com.routeanalyzer.api.common.Constants.COLORS_LAP_PATH;
import static com.routeanalyzer.api.common.Constants.COLOR_DELIMITER;
import static com.routeanalyzer.api.common.Constants.COMMA_DELIMITER;
import static com.routeanalyzer.api.common.Constants.EXPORT_AS_PATH;
import static com.routeanalyzer.api.common.Constants.GET_ACTIVITY_PATH;
import static com.routeanalyzer.api.common.Constants.JOIN_LAPS_PATH;
import static com.routeanalyzer.api.common.Constants.LAP_DELIMITER;
import static com.routeanalyzer.api.common.Constants.REMOVE_LAP_PATH;
import static com.routeanalyzer.api.common.Constants.REMOVE_POINT_PATH;
import static com.routeanalyzer.api.common.Constants.SOURCE_GPX_XML;
import static com.routeanalyzer.api.common.Constants.SOURCE_TCX_XML;
import static com.routeanalyzer.api.common.Constants.SPLIT_LAP_PATH;
import static com.routeanalyzer.api.common.Constants.STARTED_HEX_CHAR;
import static java.util.Optional.of;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toList;
import static org.springframework.http.ResponseEntity.badRequest;
import static org.springframework.http.ResponseEntity.notFound;

@RestController
@CrossOrigin(origins = "*", maxAge = 3600)
public class ActivityRestController extends RestControllerBase {

	private ActivityMongoRepository mongoRepository;
	private ActivityOperations activityOperationsService;
	private TcxExportFileService tcxExportService;
	private GpxExportFileService gpxExportService;

	@Autowired
	public ActivityRestController(ActivityMongoRepository mongoRepository, ActivityOperations activityOperationsService,
								  TcxExportFileService tcxExportService, GpxExportFileService gpxExportService) {
		super(LoggerFactory.getLogger(ActivityRestController.class));
		this.mongoRepository = mongoRepository;
		this.activityOperationsService = activityOperationsService;
		this.tcxExportService = tcxExportService;
		this.gpxExportService = gpxExportService;
	}

	@GetMapping(value = GET_ACTIVITY_PATH, produces = "application/json;")
	public @ResponseBody ResponseEntity<String> getActivityById(@PathVariable String id) {
		return getOptionalActivityById(id)
				.map(CommonUtils::toOKMessageResponse)
				.orElseGet(CommonUtils::toBadRequestParams);
	}

	@GetMapping(value = EXPORT_AS_PATH)
	public ResponseEntity<String> exportAs(@PathVariable final String id, @PathVariable final String type) {
		return getOptionalActivityById(id)
				.flatMap(activity -> ofNullable(type)
						.filter(StringUtils::isNotEmpty)
						.map(String::toLowerCase)
						.map(typeLowerCase -> exportByType(typeLowerCase, activity)))
				.orElseGet(CommonUtils::toBadRequestParams);

	}

	@PutMapping(value = REMOVE_POINT_PATH)
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

	@PutMapping(value = JOIN_LAPS_PATH)
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

	@PutMapping(value = SPLIT_LAP_PATH)
	public @ResponseBody ResponseEntity<String> splitLap(@PathVariable String id, @RequestParam String lat,
			@RequestParam String lng, @RequestParam String timeInMillis, @RequestParam String index) {
		return getOptionalActivityById(id)
				.map(activity -> activityOperationsService.splitLap(activity, lat, lng, timeInMillis, index))
				.map(mongoRepository::save)
				.map(CommonUtils::toOKMessageResponse)
				.orElseGet(CommonUtils::toBadRequestParams);
	}

	@PutMapping(value = REMOVE_LAP_PATH)
	public @ResponseBody ResponseEntity<String> removeLaps(@PathVariable String id,
			@RequestParam(name = "date") String startTimeLaps, @RequestParam(name = "index") String indexLaps) {
	    Function<String, List<String>> splitStringByComma = string -> splitStringByDelimiter(string, COMMA_DELIMITER);
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
				.orElseGet(CommonUtils::toBadRequestParams);
	}

	@PutMapping(value = COLORS_LAP_PATH)
	public @ResponseBody ResponseEntity<String> setColorLap(@PathVariable String id, @RequestParam String data) {
        Supplier<AtomicInteger> atomicIntegerSupplier = AtomicInteger::new;
        Supplier<Response> okSupplier = () -> Response.builder()
				.error(false)
				.description("Lap's colors are updated.")
				.errorMessage(null)
				.exception(null)
				.build();
        Supplier<Response> badRequestSupplier = () -> Response.builder()
				.error(true)
				.description("Not being possible to update lap's colors.")
				.errorMessage(null)
				.exception(null)
				.build();
		return getOptionalActivityById(id)
				.flatMap(activity -> ofNullable(data)
						.map(this::toLapList)
						.flatMap(laps -> of(atomicIntegerSupplier)
								.map(Supplier::get)
								.map(indexLap -> laps
                                        .stream()
										.map(lapColor -> this.setColorLap(activity, lapColor, indexLap)))
                                .map(lapStream -> lapStream.collect(toList())))
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
				.map(IntStream::boxed)
				.flatMap(indexes -> of(indexes.map(listStrings::get))
						.map(stringStream -> stringStream
								.map(convertTo)
								.collect(toList())))
				.orElseGet(Collections::emptyList);
	}

    private List<String> toLapList(String lapsStr) {
        return ofNullable(LAP_DELIMITER)
                .filter(lapsStr::contains)
                .map(lapsStr::split)
                .map(Arrays::asList)
                .orElseGet(() -> of(lapsStr)
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

	private ResponseEntity<String> exportByType(String type, Activity activity) {
		switch (type.toLowerCase()) {
			case SOURCE_TCX_XML:
				return getExportedFileResponse(tcxExportService, activity, SOURCE_TCX_XML);
			case SOURCE_GPX_XML:
				return getExportedFileResponse(gpxExportService, activity, SOURCE_GPX_XML);
			default:
				return CommonUtils.toBadRequestParams();
		}
	}

	private ResponseEntity<String> getExportedFileResponse(ExportFileService exportService, Activity activity,
														   String sourceXml) {
		return Try.of(() -> exportService.export(activity))
				.map(file -> getFileExport(file, activity.getId(), sourceXml))
				.recover(RuntimeException.class, handleControllerExceptions)
				.getOrElse(notFound().build());
	}

	private ResponseEntity<String> getFileExport(String exportedFile, String id, String sourceXml) {
		return ofNullable(exportedFile)
				.map(file -> getFileExportResponse(file, id, sourceXml))
				.orElseGet(() -> badRequest().headers(toJsonHeaders())
						.body(JsonUtils.toJson(Response.builder().error(true).build())));
	}

}
