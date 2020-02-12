package com.routeanalyzer.api.controller;

import com.routeanalyzer.api.common.CommonUtils;
import com.routeanalyzer.api.facade.ActivityFacade;
import com.routeanalyzer.api.model.Activity;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import java.util.List;

@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/activity")
@CrossOrigin(origins = "*", maxAge = 3600)
public class ActivityRestController {

	private final ActivityFacade activityFacade;

	@GetMapping(value = "/{id}")
	public Activity getActivityById(@PathVariable @NotBlank String id) {
		return activityFacade.getActivityById(id);
	}

	@GetMapping(value = "/{id}/export/{type}")
	public ResponseEntity<String> exportAs(@PathVariable @Pattern(regexp = "^[a-f\\d]{24}$") final String id,
										  @PathVariable @Pattern(regexp = "gpx|tcx", message = "Message type should be gpx or tcx.")
						   final String type) {
		return activityFacade.exportAs(id, type)
				.map(CommonUtils::createOKApplicationOctetResponse)
				.orElseThrow(() -> new IllegalArgumentException("Not possible to export"));
	}

	@PutMapping(value = "/{id}/remove/point", produces = MediaType.APPLICATION_JSON_VALUE)
	public Activity removePoint(@PathVariable @Pattern(regexp = "^[a-f\\d]{24}$") final String id,
								@RequestParam @Pattern(regexp = "^([0-9.-]+).+?([0-9.-]+)$",
										message = "Latitude parameter not valid") final String lat,
								@RequestParam @Pattern(regexp = "^([0-9.-]+).+?([0-9.-]+)$",
										message = "Longitude parameter not valid") final String lng,
								@RequestParam @Min(0L) final Long timeInMillis,
								@RequestParam @Min(0) final Integer index) {
		return activityFacade.removePoint(id, lat, lng, timeInMillis, index);
	}

	@PutMapping(value = "/{id}/join/laps")
	public Activity joinLaps(@PathVariable @Pattern(regexp = "^[a-f\\d]{24}$") @NotNull final String id,
							 @RequestParam(name = "index1") @NotNull @Min(0) final Integer indexLeft,
							 @RequestParam(name = "index2") @NotNull @Min(0) final Integer indexRight) {
		return activityFacade.joinLaps(id, indexLeft, indexRight);
	}

	@PutMapping(value = "/{id}/split/lap")
	public Activity splitLap(@PathVariable @Pattern(regexp = "^[a-f\\d]{24}$") String id,
							 @RequestParam @Pattern(regexp = "^([0-9.-]+).+?([0-9.-]+)$",
									 message = "Latitude parameter not valid") String lat,
							 @RequestParam @Pattern(regexp = "^([0-9.-]+).+?([0-9.-]+)$",
									 message = "Longitude parameter not valid") String lng,
							 @RequestParam @Min(0L) Long timeInMillis,
							 @RequestParam @Min(0) Integer index) {
		return activityFacade.splitLap(id, lat, lng, timeInMillis, index);
	}

	@PutMapping(value = "/{id}/remove/laps")
	public Activity removeLaps(@PathVariable  @Pattern(regexp = "^[a-f\\d]{24}$") String id,
							   @RequestParam(name = "date") @Size(min = 1) List<String> startTimeLaps,
							   @RequestParam(name = "index") @Size(min = 1) List<String> indexLaps) {
		return activityFacade.removeLaps(id, startTimeLaps, indexLaps);
	}

	@PutMapping(value = "/{id}/color/laps")
	public Activity setColorLap(@PathVariable @Pattern(regexp = "^[a-f\\d]{24}$") final String id,
								@RequestParam @Pattern(regexp = "^([a-fA-F\\d]{6}-[a-fA-F\\d]{6})((@" +
										"([a-fA-F\\d]{6}-[a-fA-F\\d]{6}))*)")
								final String data) {
		return activityFacade.setColorLap(id, data);
	}

}
