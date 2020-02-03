package com.routeanalyzer.api.controller;

import com.routeanalyzer.api.database.ActivityMongoRepository;
import com.routeanalyzer.api.logic.ActivityOperations;
import com.routeanalyzer.api.logic.file.export.impl.GpxExportFileService;
import com.routeanalyzer.api.logic.file.export.impl.TcxExportFileService;
import com.routeanalyzer.api.model.Activity;
import com.routeanalyzer.api.model.exception.ActivityNotFoundException;
import com.routeanalyzer.api.model.exception.ColorsNotAssignedException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import java.util.List;

import static com.google.common.base.Predicates.not;
import static com.routeanalyzer.api.common.CommonUtils.setExportHeaders;
import static com.routeanalyzer.api.common.CommonUtils.toListOfType;
import static com.routeanalyzer.api.common.Constants.SOURCE_GPX_XML;
import static com.routeanalyzer.api.common.Constants.SOURCE_TCX_XML;
import static io.vavr.API.$;
import static io.vavr.API.Case;
import static io.vavr.API.Match;
import static io.vavr.Predicates.is;
import static java.util.Optional.of;

@Slf4j
@Validated
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
	public Activity getActivityById(@PathVariable @NotBlank String id) {
		return getActivity(id);
	}

	@GetMapping(value = "/{id}/export/{type}")
	public String exportAs(@PathVariable @Pattern(regexp = "^[a-f\\d]{24}$") final String id,
						   @PathVariable @Pattern(regexp = "gpx|tcx", message = "Message type should be gpx or tcx.")
						   final String type, final HttpServletResponse response) {
		return of(getActivity(id))
				.map(activity -> {
					String exportFile = exportByType(type, activity);
					setExportHeaders(response, id, type);
					return exportFile;
				})
				.orElse(null);
	}

	@PutMapping(value = "/{id}/remove/point")
	public Activity removePoint(@PathVariable @Pattern(regexp = "^[a-f\\d]{24}$") final String id,
								@RequestParam @Pattern(regexp = "^([0-9.-]+).+?([0-9.-]+)$",
										message = "Latitude parameter not valid") final String lat,
								@RequestParam @Pattern(regexp = "^([0-9.-]+).+?([0-9.-]+)$",
										message = "Longitude parameter not valid") final String lng,
								@RequestParam @Min(0L) final Long timeInMillis,
								@RequestParam @Min(0) final Integer index) {
		return of(getActivity(id))
				.map(activity -> activityOperationsService.removePoint(activity, lat, lng, timeInMillis, index))
				.map(mongoRepository::save)
				.orElse(null);
	}

	@PutMapping(value = "/{id}/join/laps")
	public Activity joinLaps(@PathVariable @Pattern(regexp = "^[a-f\\d]{24}$") final String id,
							 @RequestParam(name = "index1") @NotNull @Min(0) final Integer indexLeft,
							 @RequestParam(name = "index2") @NotNull @Min(0) final Integer indexRight) {
		return of(getActivity(id))
				.map(activity -> activityOperationsService.joinLaps(activity, indexLeft, indexRight))
				.map(mongoRepository::save)
				.orElse(null);
	}

	@PutMapping(value = "/{id}/split/lap")
	public Activity splitLap(@PathVariable @Pattern(regexp = "^[a-f\\d]{24}$") String id,
							 @RequestParam @Pattern(regexp = "^([0-9.-]+).+?([0-9.-]+)$",
									 message = "Latitude parameter not valid") String lat,
							 @RequestParam @Pattern(regexp = "^([0-9.-]+).+?([0-9.-]+)$",
									 message = "Longitude parameter not valid") String lng,
							 @RequestParam @Min(0L) Long timeInMillis,
							 @RequestParam @Min(0) Integer index) {
		return of(getActivity(id))
				.map(activity -> activityOperationsService.splitLap(activity, lat, lng, timeInMillis, index))
				.map(mongoRepository::save)
				.orElse(null);
	}

	@PutMapping(value = "/{id}/remove/laps")
	public Activity removeLaps(@PathVariable  @Pattern(regexp = "^[a-f\\d]{24}$") String id,
							   @RequestParam(name = "date") @Size(min = 1) List<String> startTimeLaps,
							   @RequestParam(name = "index") @Size(min = 1) List<String> indexLaps) {
		List<Integer> indexes = toListOfType(indexLaps, Integer::parseInt);
	    return of(getActivity(id))
				.map(activity -> of(toListOfType(startTimeLaps, Long::valueOf))
						.filter(not(List::isEmpty))
						.map(dates -> activityOperationsService.removeLaps(activity, dates, indexes))
						.orElseGet(() -> activityOperationsService.removeLaps(activity, null, indexes)))
				.map(mongoRepository::save)
				.orElse(null);
	}

	@PutMapping(value = "/{id}/color/laps")
	public Activity setColorLap(@PathVariable @Pattern(regexp = "^[a-f\\d]{24}$") final String id,
								@RequestParam @Pattern(regexp = "^([a-f\\d]{6}-[a-f\\d]{6})((@([a-f\\d]{6}-[a-f\\d]{6}))*)")
								final String data) {
		return of(getActivity(id))
				.map(activity -> activityOperationsService.setColorsGetActivity(activity, data))
				.map(mongoRepository::save)
				.orElseThrow(() -> new ColorsNotAssignedException(id));
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
