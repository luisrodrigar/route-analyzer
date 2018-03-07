package com.routeanalyzer.controller.rest;

import java.io.IOException;
import javax.xml.bind.JAXBException;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.xml.sax.SAXParseException;

import com.amazonaws.AmazonClientException;
import com.routeanalyzer.logic.ActivityUtils;

@CrossOrigin(origins = "http://localhost:3000", maxAge = 3600)
@RestController()
@RequestMapping("/file")
public class FileRestController {
	
	/**
	 * Upload xml file: tcx or gpx to activity object.
	 * It allows to process the data contained in the file
	 * @param multiPart: xml file
	 * @param type: tcx or gpx
	 * @return id of the activity created or a json error with a description of why it failed.
	 */
	@RequestMapping(value = "/upload", method = RequestMethod.POST, headers = "content-type=multipart/form-data", produces = "application/json; charset=UTF-8")
	public ResponseEntity<String> uploadFile(@RequestParam("file") final MultipartFile multiPart,
			@RequestParam("type") final String type) {
		try {
			switch (type) {
			case "tcx":
				try {
					return new ResponseEntity<String>(ActivityUtils.uploadTCXFile(multiPart), HttpStatus.ACCEPTED);
				} catch (SAXParseException e) {
					String errorValue = "{" + "\"error\":true,"
							+ "\"description\":\"Problem trying to parser xml file. Check if its correct.\","
							+ "\"exception\":\"" + e.getMessage() + "\"" + "}";
					return new ResponseEntity<String>(errorValue, HttpStatus.BAD_REQUEST);
				} catch (JAXBException e) {
					String errorValue = "{" + "\"error\":true,"
							+ "\"description\":\"Problem with the file format uploaded.\"," + "\"exception\":\""
							+ e.getMessage() + "\"" + "}";
					return new ResponseEntity<String>(errorValue, HttpStatus.INTERNAL_SERVER_ERROR);
				}
			case "gpx":
				try {
					return new ResponseEntity<String>(ActivityUtils.uploadGPXFile(multiPart), HttpStatus.ACCEPTED);
				} catch (SAXParseException e) {
					String errorValue = "{" + "\"error\":true,"
							+ "\"description\":\"Problem trying to parser xml file. Check if its correct.\","
							+ "\"exception\":\"" + e.getMessage() + "\"" + "}";
					return new ResponseEntity<String>(errorValue, HttpStatus.BAD_REQUEST);
				} catch (JAXBException e) {
					String errorValue = "{" + "\"error\":true,"
							+ "\"description\":\"Problem with the file format uploaded.\"," + "\"exception\":\""
							+ e.getMessage() + "\"" + "}";
					return new ResponseEntity<String>(errorValue, HttpStatus.INTERNAL_SERVER_ERROR);
				}
			}
		} catch (IOException | AmazonClientException e) {
			String errorValue = "{" + "\"error\":true,"
					+ "\"description\":\"Problem with the type of the file which you want to upload\","
					+ "\"exception\":\"" + e.getMessage() + "\"" + "}";
			return new ResponseEntity<String>(errorValue, HttpStatus.INTERNAL_SERVER_ERROR);
		}
		String errorValue = "{" + "\"error\":true,"
				+ "\"description\":\"Problem with the type of the file which you want to upload\"" + "}";
		return new ResponseEntity<String>(errorValue, HttpStatus.BAD_REQUEST);
	}

	/**
	 * Get the XML file of original file stored in Amazon S3
	 * @param id of activity
	 * @param type of the xml: tcx or gpx
	 * @return response: xml if all has gone well or a json file with the errors
	 */
	@RequestMapping(value = "/get/{type}/{id}", method = RequestMethod.GET)
	public ResponseEntity<String> getFile(@PathVariable final String id, @PathVariable final String type) {
		HttpHeaders responseHeaders = new HttpHeaders();
		if (type != null && !type.isEmpty()) {
			try {
				String xml = ActivityUtils.getActivityAS3(id + "." + type);
				responseHeaders.add("Content-Type", "application/octet-stream");
				responseHeaders.add("Content-Disposition", "attachment;filename=" + id + "." + type);
				return new ResponseEntity<String>(xml, responseHeaders, HttpStatus.ACCEPTED);

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
