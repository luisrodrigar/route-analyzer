package com.routeanalyzer.controller.rest;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import javax.xml.bind.JAXBException;

import org.springframework.context.ApplicationContext;
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

import com.routeanalyzer.config.ApplicationContextProvider;
import com.routeanalyzer.database.MongoDBJDBC;
import com.routeanalyzer.database.dao.ActivityDAO;
import com.routeanalyzer.logic.ActivityUtils;
import com.routeanalyzer.model.Activity;

@CrossOrigin(origins = "http://localhost:3000", maxAge = 3600)
@RestController
@RequestMapping("activity")
public class ActivityRestController {

	@RequestMapping(value = "/{id}", method = RequestMethod.GET, produces = "application/json;")
	public @ResponseBody ResponseEntity<Activity> getActivityById(@PathVariable String id) {
		ApplicationContext ctxt = (ApplicationContext) ApplicationContextProvider.getApplicationContext();
		MongoDBJDBC mongoDBJDBC = (MongoDBJDBC) ctxt.getBean("mongoDBJDBC");
		ActivityDAO activityDAO = mongoDBJDBC.getActivityDAOImpl();
		Activity act = activityDAO.readById(id);
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
				return new ResponseEntity<String>(ActivityUtils.exportAsTCX(id), responseHeaders, HttpStatus.ACCEPTED);
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
				return new ResponseEntity<String>(ActivityUtils.exportAsGPX(id), responseHeaders, HttpStatus.ACCEPTED);
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
		Activity act = ActivityUtils.removePoint(id, lat, lng, timeInMillis, index);
		return act != null ? new ResponseEntity<Object>(act, HttpStatus.ACCEPTED)
				: new ResponseEntity<Object>(
						"{" + "\"error\":true," + "\"description\":\"Error trying to remove point.\"" + "}",
						HttpStatus.INTERNAL_SERVER_ERROR);
	}
	
	@RequestMapping(value = "/{id}/remove/laps", method = RequestMethod.PUT, produces = "application/json;")
	public @ResponseBody ResponseEntity<Object> removeLap(@PathVariable String id, @RequestParam(name="date") String startTimeLaps,
			@RequestParam(name="index") String indexLaps) {
		List<String> dates = Arrays.asList(startTimeLaps.split(",")).stream()
				.filter(element -> element != null && !element.isEmpty()).collect(Collectors.toList());
		List<String> index = Arrays.asList(indexLaps.split(",")).stream()
				.filter(element -> element != null && !element.isEmpty()).collect(Collectors.toList());
		List<Activity> acts = new ArrayList<>();
		if(index!=null && !index.isEmpty()){
			boolean isDates = dates!=null && !dates.isEmpty();
			IntStream.range(0, index.size()).forEach(indexArrays -> {
				Integer indexLap = Integer.parseInt(index.get(indexArrays));
				Long date = isDates?Long.parseLong(dates.get(indexArrays)):null;
				acts.add(ActivityUtils.removeLap(id, date, indexLap));
			});
		}
		return !acts.isEmpty() ? new ResponseEntity<Object>(acts.get(acts.size()-1), HttpStatus.ACCEPTED)
				: new ResponseEntity<Object>(
						"{" + "\"error\":true," + "\"description\":\"Error trying to remove point.\"" + "}",
						HttpStatus.INTERNAL_SERVER_ERROR);
	}
}
