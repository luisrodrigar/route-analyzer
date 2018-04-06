package com.routeanalyzer.controller.rest;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;
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
import com.amazonaws.AmazonServiceException;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.routeanalyzer.database.ActivityMongoRepository;
import com.routeanalyzer.logic.ActivityUtils;
import com.routeanalyzer.model.Activity;
import com.routeanalyzer.services.OriginalRouteAS3Service;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController()
@RequestMapping("/file")
public class FileRestController {

	@Autowired
	private ActivityMongoRepository mongoRepository;
	
	@Autowired
	private OriginalRouteAS3Service aS3Service;
	
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
		GsonBuilder gsonBuilder = new GsonBuilder();
		Gson gson = gsonBuilder.create();
		List<Activity> activities = null;
		try {
			switch (type) {
			case "tcx":
				try {
					activities = ActivityUtils.uploadTCXFile(multiPart);
					// Se guarda en la base de datos
					List<String> ids = saveActivity(activities, multiPart.getBytes());
					return ResponseEntity.ok().body(gson.toJson(ids));
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
					activities = ActivityUtils.uploadGPXFile(multiPart);
					// Se guarda en la base de datos
					List<String> ids = saveActivity(activities, multiPart.getBytes());
					return ResponseEntity.ok().body(gson.toJson(ids));
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
				String xml = getActivityAS3(id + "." + type);
				responseHeaders.add("Content-Type", "application/octet-stream");
				responseHeaders.add("Content-Disposition", "attachment;filename=" + id + "." + type);
				return ResponseEntity.ok().body(xml);

			} catch (AmazonClientException amazonException) {
				responseHeaders.add("Content-Type", "application/json; charset=utf-8");
				String json = "{" + "error:true," + "description: 'Problem trying to get the file :: Amazon S3 Problem'"
						+ "exception: " + amazonException.getMessage() + " }";
				return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).headers(responseHeaders).body(json);
			} catch (IOException iOException) {
				responseHeaders.add("Content-Type", "application/json; charset=utf-8");
				String json = "{" + "error:true,"
						+ "description: 'Problem trying to get the file :: Input/Output Problem'" + "exception: "
						+ iOException.getMessage() + " }";
				return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(json);
			}
		} else {
			responseHeaders.add("Content-Type", "application/json; charset=utf-8");
			String json = "{" + "error:true," + "description:'Problem with the type of the file which you want to get'"
					+ "}";
			return ResponseEntity.badRequest().headers(responseHeaders).body(json);
		}
	}
	
	/**
	 * 
	 * @param activities
	 * @param arrayBytes
	 * @return
	 * @throws AmazonClientException
	 */
	private List<String> saveActivity(List<Activity> activities, byte[] arrayBytes)
			throws AmazonClientException {
		List<String> ids = new ArrayList<String>();
		activities.forEach(activity -> {
			mongoRepository.save(activity);
			ids.add(activity.getId());
			try {
				aS3Service.uploadFile(arrayBytes, activity.getId() + "." + activity.getSourceXmlType());
			} catch (AmazonClientException aS3Exception) {
				System.err.println("Delete activity with id: " + activity.getId()
						+ " due to problems trying to upload file to AS3.");
				mongoRepository.delete(activity);
				throw aS3Exception;
			}
		});
		return ids;
	}
	
	/**
	 * 
	 * @param nameFile
	 * @return
	 * @throws AmazonServiceException
	 * @throws AmazonClientException
	 * @throws IOException
	 */
	private String getActivityAS3(String nameFile)
			throws AmazonServiceException, AmazonClientException, IOException {
		BufferedReader bufReader = aS3Service.getFile(nameFile);
		return bufReader.lines().collect(Collectors.joining("\n"));
	}

}
