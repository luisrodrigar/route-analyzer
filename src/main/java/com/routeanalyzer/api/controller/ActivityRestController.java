package com.routeanalyzer.api.controller;

import com.routeanalyzer.api.common.CommonUtils;
import com.routeanalyzer.api.database.ActivityMongoRepository;
import com.routeanalyzer.api.logic.ActivityOperations;
import com.routeanalyzer.api.logic.file.export.impl.GpxExportFileService;
import com.routeanalyzer.api.logic.file.export.impl.TcxExportFileService;
import com.routeanalyzer.api.model.Activity;
import com.routeanalyzer.api.model.exception.ActivityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import java.util.stream.IntStream;

import static com.routeanalyzer.api.common.CommonUtils.setExportHeaders;
import static com.routeanalyzer.api.common.CommonUtils.splitStringByDelimiter;
import static com.routeanalyzer.api.common.CommonUtils.toBadRequestResponse;
import static com.routeanalyzer.api.common.Constants.COMMA_DELIMITER;
import static com.routeanalyzer.api.common.Constants.SOURCE_GPX_XML;
import static com.routeanalyzer.api.common.Constants.SOURCE_TCX_XML;
import static io.vavr.API.$;
import static io.vavr.API.Case;
import static io.vavr.API.Match;
import static io.vavr.Predicates.is;
import static java.util.Optional.of;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toList;

@RestController
@RequiredArgsConstructor
@RequestMapping("/activity")
@CrossOrigin(origins = "*", maxAge = 3600)
public class ActivityRestController {

	private final ActivityMongoRepository mongoRepository;
	private final ActivityOperations activityOperationsService;
	private final TcxExportFileService tcxExportService;
	private final GpxExportFileService gpxExportService;

	@GetMapping(value = "/{id}")
	public Activity getActivityById(@PathVariable String id) {
		return getActivity(id);
	}

	@GetMapping(value = "/{id}/export/{type}")
	public String exportAs(@PathVariable final String id, @PathVariable final String type,
						   HttpServletResponse response) {
		setExportHeaders(response, id, type);
		return exportByType(type, getActivity(id));

	}

	@PutMapping(value = "/{id}/remove/point")
	public @ResponseBody ResponseEntity<String> removePoint(@PathVariable String id, @RequestParam String lat,
			@RequestParam String lng, @RequestParam String timeInMillis, @RequestParam String index) {
		return of(getActivity(id))
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

	@PutMapping(value = "/{id}/split/lap")
	public @ResponseBody ResponseEntity<String> splitLap(@PathVariable String id, @RequestParam String lat,
			@RequestParam String lng, @RequestParam String timeInMillis, @RequestParam String index) {
		return of(getActivity(id))
				.map(activity -> activityOperationsService.splitLap(activity, lat, lng, timeInMillis, index))
				.map(mongoRepository::save)
				.map(CommonUtils::toOKMessageResponse)
				.orElseGet(CommonUtils::toBadRequestParams);
	}

	@PutMapping(value = "/{id}/remove/laps")
	public @ResponseBody ResponseEntity<String> removeLaps(@PathVariable String id,
			@RequestParam(name = "date") String startTimeLaps, @RequestParam(name = "index") String indexLaps) {
	    Function<String, List<String>> splitStringByComma = string -> splitStringByDelimiter(string, COMMA_DELIMITER);
	    return of(getActivity(id))
				.flatMap(activity -> ofNullable(indexLaps)
						.map(splitStringByComma)
						.map(indexStrings -> this.toListType(indexStrings, Integer::parseInt))
						.map(index -> ofNullable(startTimeLaps)
								.map(splitStringByComma)
								.map(indexStrings -> this.toListType(indexStrings, Long::valueOf))
								.map(datesList -> activityOperationsService
										.removeLaps(activity, datesList, index))
								.orElseGet(() -> activityOperationsService
										.removeLaps(activity, null, index)))
						.map(mongoRepository::save))
				.map(CommonUtils::toOKMessageResponse)
				.orElseGet(CommonUtils::toBadRequestParams);
	}

	@PutMapping(value = "/{id}/color/laps")
	public @ResponseBody ResponseEntity<String> setColorLap(@PathVariable String id, @RequestParam String data) {
        Response badResponse = Response.builder()
				.error(true)
				.description("Not being possible to update lap's colors.")
				.errorMessage(null)
				.exception(null)
				.build();
		return of(getActivity(id))
				.map(activity -> activityOperationsService.setColorsGetActivity(activity, data))
				.map(mongoRepository::save)
				.map(CommonUtils::toOKMessageResponse)
				.orElseGet(() -> toBadRequestResponse(badResponse));
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

	private Activity getActivity(String id) {
		return mongoRepository.findById(id)
				.orElseThrow(() -> new ActivityNotFoundException(id));
	}

	private String exportByType(String type, Activity activity) {
		return Match(type.toLowerCase()).option(
				Case($(is(SOURCE_TCX_XML)), tcxFile -> tcxExportService.export(activity)),
				Case($(is(SOURCE_GPX_XML)), gpxFile -> gpxExportService.export(activity)))
				.getOrElseThrow(() -> new IllegalArgumentException("Bad type file."));
	}

}
