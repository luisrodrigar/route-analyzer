package com.routeanalyzer.controller.rest;

import java.io.IOException;
import javax.xml.bind.JAXBException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.xml.sax.SAXParseException;

import com.amazonaws.AmazonClientException;
import com.routeanalyzer.database.ActivityMongoRepository;
import com.routeanalyzer.logic.ActivityUtils;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController()
@RequestMapping("/file")
public class FileRestController {

	@Autowired
	private ActivityMongoRepository mongoRepository;
	
	/**
	 * Upload xml file: tcx or gpx to activity object. It allows to process the
	 * data contained in the file
	 * 
	 * @param multiPart:
	 *            xml file
	 * @param type:
	 *            tcx or gpx
	 * @return id of the activity created or a json error with a description of
	 *         why it failed.
	 */
	@PostMapping(value = "/upload", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<String> uploadFile(@RequestParam("file") MultipartFile multiPart,
			@RequestParam("type") String type) {
		try {
			switch (type) {
			case "tcx":
				try {
					return ResponseEntity.ok().body(ActivityUtils.uploadTCXFile(multiPart,mongoRepository));
				} catch (SAXParseException e) {
					String errorValue = "{" + "\"error\":true,"
							+ "\"description\":\"Problem trying to parser xml file. Check if its correct.\","
							+ "\"exception\":\"" + e.getMessage() + "\"" + "}";
					return ResponseEntity.badRequest().body(errorValue);
				} catch (JAXBException e) {
					String errorValue = "{" + "\"error\":true,"
							+ "\"description\":\"Problem with the file format uploaded.\"," + "\"exception\":\""
							+ e.getMessage() + "\"" + "}";
					return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorValue);
				}
			case "gpx":
				try {
					return ResponseEntity.ok().body(ActivityUtils.uploadGPXFile(multiPart, mongoRepository));
				} catch (SAXParseException e) {
					String errorValue = "{" + "\"error\":true,"
							+ "\"description\":\"Problem trying to parser xml file. Check if its correct.\","
							+ "\"exception\":\"" + e.getMessage() + "\"" + "}";
					return ResponseEntity.badRequest().body(errorValue);
				} catch (JAXBException e) {
					String errorValue = "{" + "\"error\":true,"
							+ "\"description\":\"Problem with the file format uploaded.\"," + "\"exception\":\""
							+ e.getMessage() + "\"" + "}";
					return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorValue);
				}
			}
		} catch (IOException | AmazonClientException e) {
			String errorValue = "{" + "\"error\":true,"
					+ "\"description\":\"Problem with the type of the file which you want to upload\","
					+ "\"exception\":\"" + e.getMessage() + "\"" + "}";
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorValue);
		}
		String errorValue = "{" + "\"error\":true,"
				+ "\"description\":\"Problem with the type of the file which you want to upload\"" + "}";
		return ResponseEntity.badRequest().body(errorValue);
	}

	/**
	 * Get the XML file of original file stored in Amazon S3
	 * 
	 * @param id
	 *            of activity
	 * @param type
	 *            of the xml: tcx or gpx
	 * @return response: xml if all has gone well or a json file with the errors
	 */
	@GetMapping(value = "/get/{type}/{id}")
	public ResponseEntity<String> getFile(@PathVariable final String id, @PathVariable final String type) {
		HttpHeaders responseHeaders = new HttpHeaders();
		if (type != null && !type.isEmpty()) {
			try {
				String xml = ActivityUtils.getActivityAS3(id + "." + type);
				responseHeaders.add("Content-Type", "application/octet-stream");
				responseHeaders.add("Content-Disposition", "attachment;filename=" + id + "." + type);
				return ResponseEntity.ok().body(xml);

			} catch (AmazonClientException amazonException) {
				responseHeaders.add("Content-Type", "application/json; charset=utf-8");
				String json = "{" + "error:true," + "description: 'Problem trying to get the file :: Amazon S3 Problem'"
						+ "exception: " + amazonException.getMessage() + " }";
				return new ResponseEntity<String>(json, responseHeaders, HttpStatus.INTERNAL_SERVER_ERROR);
			} catch (IOException iOException) {
				responseHeaders.add("Content-Type", "application/json; charset=utf-8");
				String json = "{" + "error:true,"
						+ "description: 'Problem trying to get the file :: Input/Output Problem'" + "exception: "
						+ iOException.getMessage() + " }";
				return new ResponseEntity<String>(json, responseHeaders, HttpStatus.INTERNAL_SERVER_ERROR);
			}
		} else {
			responseHeaders.add("Content-Type", "application/json; charset=utf-8");
			String json = "{" + "error:true," + "description:'Problem with the type of the file which you want to get'"
					+ "}";
			return new ResponseEntity<String>(json, responseHeaders, HttpStatus.BAD_REQUEST);
		}
	}

}
