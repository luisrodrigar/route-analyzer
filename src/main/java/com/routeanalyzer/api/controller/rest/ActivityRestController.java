package com.routeanalyzer.api.controller.rest;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import javax.xml.bind.JAXBException;

import com.routeanalyzer.api.logic.file.export.impl.GpxExportFileService;
import com.routeanalyzer.api.logic.file.export.impl.TcxExportFileService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
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
	public @ResponseBody ResponseEntity<Object> getActivityById(@PathVariable String id) {
		Activity act = mongoRepository.findById(id).orElse(null);
		if (Objects.isNull(act)) {
			log.debug("Given activity id not found in database.");
			String error = "{\"error\":true, \"description\":\"Given activity id not found in database.\"}";
			return ResponseEntity.badRequest().contentType(MediaType.APPLICATION_JSON_UTF8).body(error);
		} else
			return ResponseEntity.ok().body(act);
	}

	@GetMapping(value = "/{id}/export/{type}")
	public ResponseEntity<String> exportAs(@PathVariable final String id, @PathVariable final String type) {
		Activity activity = mongoRepository.findById(id).orElse(null);
		switch (type.toLowerCase()) {
		case "tcx":
			try {
				HttpHeaders responseHeaders = new HttpHeaders();
				responseHeaders.add("Content-Type", MediaType.APPLICATION_OCTET_STREAM.toString());
				responseHeaders.add("Content-Disposition", "attachment;filename=" + id + "_tcx.xml");
				return ResponseEntity.ok().headers(responseHeaders).body(tcxExportService.export(activity));
			} catch (JAXBException e1) {
				HttpHeaders responseHeaders = new HttpHeaders();
				responseHeaders.add("Content-Type", MediaType.APPLICATION_JSON_UTF8.toString());
				String errorValue = "{\"error\":true,\"description\":\"Problem with the file format uploaded.\"," 
						+ "\"exception\":\"" + e1.getMessage() + "\"}";
				return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).headers(responseHeaders)
						.body(errorValue);
			}
		case "gpx":
			try {
				HttpHeaders responseHeaders = new HttpHeaders();
				responseHeaders.add("Content-Type", MediaType.APPLICATION_OCTET_STREAM.toString());
				responseHeaders.add("Content-Disposition", "attachment;filename=" + id + "_gpx.xml");
				return ResponseEntity.ok().headers(responseHeaders).body(gpxExportService.export(activity));
			} catch (JAXBException e) {
				HttpHeaders responseHeaders = new HttpHeaders();
				responseHeaders.add("Content-Type", MediaType.APPLICATION_JSON_UTF8.toString());
				String errorValue = "{" + "\"error\":true,"
						+ "\"description\":\"Problem with the file format uploaded.\"," + "\"exception\":\""
						+ e.getMessage() + "\"" + "}";
				return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).headers(responseHeaders)
						.body(errorValue);
			}
		default:
			HttpHeaders responseHeaders = new HttpHeaders();
			responseHeaders.add("Content-Type", MediaType.APPLICATION_JSON_UTF8.toString());
			String errorValue = "{" + "\"error\":true," + "\"description\":\"Select a correct type for export it.\""
					+ "}";
			return ResponseEntity.badRequest().headers(responseHeaders).body(errorValue);
		}
	}

	@PutMapping(value = "/{id}/remove/point", produces = "application/json;")
	public @ResponseBody ResponseEntity<Object> removePoint(@PathVariable String id, @RequestParam String lat,
			@RequestParam String lng, @RequestParam String timeInMillis, @RequestParam String index) {
		Activity activity = mongoRepository.findById(id).orElse(null);
		if (Objects.isNull(activity)) {
			String error = "{" + "\"error\":true," + "\"description\":\"Given activity not found in database.\"" + "}";
			return ResponseEntity.badRequest().body(error);
		} else {
			try{
				activityOperationsService.removePoint(activity, lat, lng, timeInMillis, index);
				mongoRepository.save(activity);
				return ResponseEntity.ok().body(activity);
			} catch(IndexOutOfBoundsException e){
				String error = "{" + "\"error\":true," + "\"description\":\"Check if the index is correct.\"" + "}";
				return ResponseEntity.badRequest().body(error);
			}
		}
	}

	@PutMapping(value = "/{id}/join/laps", produces = "application/json;")
	public @ResponseBody ResponseEntity<Object> joinLaps(@PathVariable String id,
			@RequestParam(name = "index1") String indexLap1, @RequestParam(name = "index2") String indexLap2) {
		Activity act = mongoRepository.findById(id).orElse(null);
		if (!Objects.isNull(indexLap1) && !Objects.isNull(indexLap2) && !indexLap1.isEmpty() && !indexLap2.isEmpty()) {
			act = activityOperationsService.joinLaps(act, Integer.parseInt(indexLap1), Integer.parseInt(indexLap2));
			if (Objects.isNull(act)) {
				String error = "{" + "\"error\":true," + "\"description\":\"Given activity id not found in database.\""
						+ "}";
				return ResponseEntity.badRequest().body(error);
			} else {
				mongoRepository.save(act);
				return ResponseEntity.ok().body(act);
			}
		} else {
			String error = "{" + "\"error\":true," + "\"description\":\"Please check the laps indexes params.\"" + "}";
			return ResponseEntity.badRequest().body(error);
		}

	}

	@PutMapping(value = "/{id}/split/lap", produces = "application/json;")
	public @ResponseBody ResponseEntity<Object> splitLap(@PathVariable String id, @RequestParam String lat,
			@RequestParam String lng, @RequestParam String timeInMillis, @RequestParam String index) {
		Activity act = mongoRepository.findById(id).orElse(null);
		if (Objects.isNull(act))
			return ResponseEntity.badRequest().body("{" + "\"error\":true,"
					+ "\"description\":\"Given activity id not found in database.\"" + "}");
		else {
			act = activityOperationsService.splitLap(act, lat, lng, timeInMillis, index);
			if(Objects.isNull(act)){
				return ResponseEntity.badRequest().body("{" + "\"error\":true,"
						+ "\"description\":\"Error trying split the lap.\"" + "}");
			}
			mongoRepository.save(act);
			return ResponseEntity.ok().body(act);
		}
	}

	@PutMapping(value = "/{id}/remove/laps", produces = "application/json;")
	public @ResponseBody ResponseEntity<Object> removeLaps(@PathVariable String id,
			@RequestParam(name = "date") String startTimeLaps, @RequestParam(name = "index") String indexLaps) {
		Activity act = mongoRepository.findById(id).orElse(null);

		if (!Objects.isNull(act)) {
			List<String> dates = Arrays.asList(startTimeLaps.split(",")).stream()
					.filter(element -> element != null && !element.isEmpty()).collect(Collectors.toList());
			List<String> index = Arrays.asList(indexLaps.split(",")).stream()
					.filter(element -> element != null && !element.isEmpty()).collect(Collectors.toList());

			if (index != null && !index.isEmpty()) {
				boolean isDates = dates != null && !dates.isEmpty();
				IntStream.range(0, index.size()).forEach(indexArrays -> {
					Integer indexLap = Integer.parseInt(index.get(indexArrays));
					Long date = isDates ? Long.parseLong(dates.get(indexArrays)) : null;
					activityOperationsService.removeLap(act, date, indexLap);
				});
			}
			mongoRepository.save(act);
			return ResponseEntity.ok().body(act);
		} else {
			String error = "{" + "\"error\":true," + "\"description\":\"Given activity id not found in database.\""
					+ "}";
			return ResponseEntity.badRequest().body(error);
		}
	}

	@PutMapping(value = "/{id}/color/laps")
	public @ResponseBody ResponseEntity<String> setColorLaps(@PathVariable String id, @RequestParam String data) {
		Activity act = mongoRepository.findById(id).orElse(null);
		// Lap splitter: @, color splitter: -, first char in hexadecimal number:
		// #
		String lapSplitter = "@", colorSplitter = "-", startedCharHex = "#";
		// [color(hex)-lightColor(hex)]@[...]... number without #
		if (!Objects.isNull(data) && !data.isEmpty() && !Objects.isNull(act)) {
			List<String> laps = null;
			if (data.contains(lapSplitter))
				laps = Arrays.asList(data.split(lapSplitter));
			else
				laps = Arrays.asList(data);
			AtomicInteger index = new AtomicInteger();
			laps.forEach(lap -> {
				int indexLap = index.getAndIncrement();
				String color = lap.split(colorSplitter)[0], lightColor = lap.split(colorSplitter)[1];
				if (!Objects.isNull(color) && !Objects.isNull(lightColor) && !color.isEmpty()
						&& !lightColor.isEmpty()) {
					String hexColor = startedCharHex + color, hexLightColor = startedCharHex + lightColor;
					act.getLaps().get(indexLap).setColor(hexColor);
					act.getLaps().get(indexLap).setLightColor(hexLightColor);
				}
			});
			mongoRepository.save(act);
			String info = "{" + "\"error\":false," + "\"description\":\"Lap's colors are updated.\"" + "}";
			return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON_UTF8).body(info);
		} else {
			String error = "{" + "\"error\":true," + "\"description\":\"Not be posible to update lap's colors.\"" + "}";
			return ResponseEntity.badRequest().contentType(MediaType.APPLICATION_JSON_UTF8).body(error);
		}
	}
}
