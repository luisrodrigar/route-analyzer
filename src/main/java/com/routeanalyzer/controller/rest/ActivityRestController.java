package com.routeanalyzer.controller.rest;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import javax.xml.bind.JAXBException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.routeanalyzer.database.ActivityMongoRepository;
import com.routeanalyzer.logic.ActivityUtils;
import com.routeanalyzer.model.Activity;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("activity")
public class ActivityRestController {
	
	@Autowired
	private ActivityMongoRepository mongoRepository;

	@RequestMapping(value = "/{id}", method = RequestMethod.GET, produces = "application/json;")
	public @ResponseBody ResponseEntity<Activity> getActivityById(@PathVariable String id) {
		Activity act = mongoRepository.findById(id).orElse(null);
		return act != null ? new ResponseEntity<Activity>(act, HttpStatus.ACCEPTED)
				: new ResponseEntity<Activity>(act, HttpStatus.INTERNAL_SERVER_ERROR);
	}

	@RequestMapping(value = "/{id}/export/{type}", method = RequestMethod.GET)
	public ResponseEntity<String> exportAs(@PathVariable final String id, @PathVariable final String type) {
		switch (type.toLowerCase()) {
		case "tcx":
			try {
				HttpHeaders responseHeaders = new HttpHeaders();
				responseHeaders.add("Content-Type", "application/octet-stream");
				responseHeaders.add("Content-Disposition", "attachment;filename=" + id + "_tcx.xml");
				return new ResponseEntity<String>(ActivityUtils.exportAsTCX(id, mongoRepository), responseHeaders, HttpStatus.ACCEPTED);
			} catch (JAXBException e1) {
				HttpHeaders responseHeaders = new HttpHeaders();
				responseHeaders.add("Content-Type", "application/json; charset=utf-8");
				String errorValue = "{" + "\"error\":true,"
						+ "\"description\":\"Problem with the file format uploaded.\"," + "\"exception\":\""
						+ e1.getMessage() + "\"" + "}";
				return new ResponseEntity<String>(errorValue, responseHeaders, HttpStatus.INTERNAL_SERVER_ERROR);
			}
		case "gpx":
			try {
				HttpHeaders responseHeaders = new HttpHeaders();
				responseHeaders.add("Content-Type", "application/octet-stream");
				responseHeaders.add("Content-Disposition", "attachment;filename=" + id + "_gpx.xml");
				return new ResponseEntity<String>(ActivityUtils.exportAsGPX(id, mongoRepository), responseHeaders, HttpStatus.ACCEPTED);
			} catch (JAXBException e) {
				HttpHeaders responseHeaders = new HttpHeaders();
				responseHeaders.add("Content-Type", "application/json; charset=utf-8");
				String errorValue = "{" + "\"error\":true,"
						+ "\"description\":\"Problem with the file format uploaded.\"," + "\"exception\":\""
						+ e.getMessage() + "\"" + "}";
				return new ResponseEntity<String>(errorValue, responseHeaders, HttpStatus.INTERNAL_SERVER_ERROR);
			}
		default:
			HttpHeaders responseHeaders = new HttpHeaders();
			responseHeaders.add("Content-Type", "application/json; charset=utf-8");
			String errorValue = "{" + "\"error\":true," + "\"description\":\"Select a correct type for export it.\""
					+ "}";
			return new ResponseEntity<String>(errorValue, responseHeaders, HttpStatus.BAD_REQUEST);
		}
	}

	@RequestMapping(value = "/{id}/remove/point", method = RequestMethod.PUT, produces = "application/json;")
	public @ResponseBody ResponseEntity<Object> removePoint(@PathVariable String id, @RequestParam String lat,
			@RequestParam String lng, @RequestParam String timeInMillis, @RequestParam String index) {
		Activity act = ActivityUtils.removePoint(id, lat, lng, timeInMillis, index, mongoRepository);
		return act != null ? new ResponseEntity<Object>(act, HttpStatus.ACCEPTED)
				: new ResponseEntity<Object>(
						"{" + "\"error\":true," + "\"description\":\"Error trying to remove point.\"" + "}",
						HttpStatus.INTERNAL_SERVER_ERROR);
	}

	@RequestMapping(value = "/{id}/join/laps", method = RequestMethod.PUT, produces = "application/json;")
	public @ResponseBody ResponseEntity<Activity> joinLaps(@PathVariable String id,
			@RequestParam(name = "index1") String indexLap1, @RequestParam(name = "index2") String indexLap2) {
		Activity act = null;
		if (indexLap1 != null && !indexLap1.isEmpty() && indexLap2 != null && !indexLap2.isEmpty()) {
			act = ActivityUtils.joinLap(id, Integer.parseInt(indexLap1), Integer.parseInt(indexLap2), mongoRepository);
			
			return act != null ? new ResponseEntity<Activity>(act, HttpStatus.ACCEPTED)
					: new ResponseEntity<Activity>(act, HttpStatus.INTERNAL_SERVER_ERROR);
		} else
			return new ResponseEntity<Activity>(act, HttpStatus.INTERNAL_SERVER_ERROR);

	}

	@RequestMapping(value = "/{id}/split/lap", method = RequestMethod.PUT, produces = "application/json;")
	public @ResponseBody ResponseEntity<Object> splitLap(@PathVariable String id, @RequestParam String lat,
			@RequestParam String lng, @RequestParam String timeInMillis, @RequestParam String index) {
		Activity act = ActivityUtils.splitLap(id, lat, lng, timeInMillis, index, mongoRepository);
		return act != null ? new ResponseEntity<Object>(act, HttpStatus.ACCEPTED)
				: new ResponseEntity<Object>(
						"{" + "\"error\":true," + "\"description\":\"Error trying split the lap.\"" + "}",
						HttpStatus.INTERNAL_SERVER_ERROR);
	}

	@RequestMapping(value = "/{id}/remove/laps", method = RequestMethod.PUT, produces = "application/json;")
	public @ResponseBody ResponseEntity<Activity> removeLaps(@PathVariable String id,
			@RequestParam(name = "date") String startTimeLaps, @RequestParam(name = "index") String indexLaps) {
		Activity act = mongoRepository.findById(id).orElse(null);

		List<String> dates = Arrays.asList(startTimeLaps.split(",")).stream()
				.filter(element -> element != null && !element.isEmpty()).collect(Collectors.toList());
		List<String> index = Arrays.asList(indexLaps.split(",")).stream()
				.filter(element -> element != null && !element.isEmpty()).collect(Collectors.toList());

		if (index != null && !index.isEmpty()) {
			boolean isDates = dates != null && !dates.isEmpty();
			IntStream.range(0, index.size()).forEach(indexArrays -> {
				Integer indexLap = Integer.parseInt(index.get(indexArrays));
				Long date = isDates ? Long.parseLong(dates.get(indexArrays)) : null;
				ActivityUtils.removeLap(act, date, indexLap);
			});
		}

		mongoRepository.save(act);

		return act != null ? new ResponseEntity<Activity>(act, HttpStatus.ACCEPTED)
				: new ResponseEntity<Activity>(act, HttpStatus.INTERNAL_SERVER_ERROR);
	}

	@RequestMapping(value = "/{id}/color/laps", method = RequestMethod.PUT)
	public @ResponseBody ResponseEntity<String> setColorLaps(@PathVariable String id, String data) {
		Activity act = mongoRepository.findById(id).orElse(null);
		// Lap splitter: @, color splitter: -, first char in hexadecimal number: #
		String lapSplitter = "@", colorSplitter = "-", startedCharHex = "#";
		// [color(hex)-lightColor(hex)]@[...]... number without #
		if (!Objects.isNull(data) && !data.isEmpty()) {
			List<String> laps = null;
			if(data.contains(lapSplitter))
				laps = Arrays.asList(data.split(lapSplitter));
			else
				laps = Arrays.asList(data);
			AtomicInteger index = new AtomicInteger();
			laps.forEach(lap -> {
				int indexLap = index.getAndIncrement();
				String color = lap.split(colorSplitter)[0], lightColor = lap.split(colorSplitter)[1];
				if (!Objects.isNull(color) && !Objects.isNull(lightColor)
						&& !color.isEmpty() && !lightColor.isEmpty()) {
					String hexColor = startedCharHex + color, hexLightColor = startedCharHex + lightColor;
					act.getLaps().get(indexLap).setColor(hexColor);
					act.getLaps().get(indexLap).setLightColor(hexLightColor);
				}
			});
			mongoRepository.save(act);
			return new ResponseEntity<String>(HttpStatus.ACCEPTED);
		}else
			return new ResponseEntity<String>(HttpStatus.BAD_REQUEST);
	}
}
