package com.routeanalyzer.controller.rest;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import javax.xml.bind.JAXBException;

import com.routeanalyzer.logic.file.upload.UploadFileService;
import com.routeanalyzer.logic.file.upload.impl.GpxUploadFileService;
import com.routeanalyzer.logic.file.upload.impl.TcxUploadFileService;
import io.vavr.control.Try;
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
import com.routeanalyzer.model.Activity;
import com.routeanalyzer.services.OriginalRouteAS3Service;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController()
@RequestMapping("/file")
public class FileRestController {

	private final Logger log = LoggerFactory.getLogger(FileRestController.class);
	
	@Autowired
	private ActivityMongoRepository mongoRepository;
	@Autowired
	private OriginalRouteAS3Service aS3Service;
	@Autowired
	private TcxUploadFileService tcxService;
	@Autowired
	private GpxUploadFileService gpxService;

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
					return generateActivityCreatedResponse(multiPart, tcxService);
				case "gpx":
					return generateActivityCreatedResponse(multiPart, gpxService);
			}
		} catch (IOException | AmazonClientException e) {
			log.error(e.getClass().getSimpleName() + " error: " + e.getMessage());
			String errorValue = "{" + "\"error\":true,"
					+ "\"description\":\"Problem with the type of the file which you want to upload\","
					+ "\"exception\":\"" + e.getMessage() + "\"" + "}";
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorValue);
		}
		String errorValue = "{" + "\"error\":true,"
				+ "\"description\":\"Problem with the type of the file which you want to upload\"" + "}";
		return ResponseEntity.badRequest().body(errorValue);
	}

	private ResponseEntity<String> generateActivityCreatedResponse(MultipartFile multiPart, UploadFileService service)
			throws AmazonClientException, IOException {
		GsonBuilder gsonBuilder = new GsonBuilder();
		Gson gson = gsonBuilder.create();
		return Try.of(() -> service.upload(multiPart))
				.flatMap(activities ->
					Try.of(() -> saveActivity(activities, multiPart.getBytes()))
							.map((ids) -> ResponseEntity.ok().body(gson.toJson(ids))))
				.recover(SAXParseException.class, (error) -> {
					log.error(error.getClass().getSimpleName() + " error: " + error.getMessage());
					String errorValue = "{" + "\"error\":true,"
							+ "\"description\":\"Problem trying to parser xml file. Check if its correct.\","
							+ "\"exception\":\"" + error.getMessage() + "\"" + "}";
					return ResponseEntity.badRequest().body(errorValue);
				}).recover(JAXBException.class, (error) -> {
			log.error(error.getClass().getSimpleName() + " error: " + error.getMessage());
			String errorValue = "{" + "\"error\":true,"
					+ "\"description\":\"Problem with the file format uploaded.\"," + "\"exception\":\""
					+ error.getMessage() + "\"" + "}";
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorValue);
		}).get();
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
				responseHeaders.add("Content-Type", MediaType.APPLICATION_OCTET_STREAM.toString());
				responseHeaders.add("Content-Disposition", "attachment;filename=" + id + "." + type);
				return ResponseEntity.ok().headers(responseHeaders).body(xml);

			} catch (AmazonClientException amazonException) {
				log.error("AmazonClientException error", amazonException);
				responseHeaders.add("Content-Type", MediaType.APPLICATION_JSON_UTF8.toString());
				String json = "{" + "error:true," + "description: 'Problem trying to get the file :: Amazon S3 Problem'"
						+ "exception: " + amazonException.getMessage() + " }";
				return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).headers(responseHeaders).body(json);
			} catch (IOException iOException) {
				log.error("IOException error", iOException);
				responseHeaders.add("Content-Type", MediaType.APPLICATION_JSON_UTF8.toString());
				String json = "{" + "error:true,"
						+ "description: 'Problem trying to get the file :: Input/Output Problem'" + "exception: "
						+ iOException.getMessage() + " }";
				return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).headers(responseHeaders).body(json);
			}
		} else {
			responseHeaders.add("Content-Type", MediaType.APPLICATION_JSON_UTF8.toString());
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
	private List<String> saveActivity(List<Activity> activities, byte[] arrayBytes) throws AmazonClientException {
		List<String> ids = new ArrayList<String>();
		activities.forEach(activity -> {
			mongoRepository.save(activity);
			ids.add(activity.getId());
			try {
				aS3Service.uploadFile(arrayBytes, activity.getId() + "." + activity.getSourceXmlType());
			} catch (AmazonClientException aS3Exception) {
				log.error("Delete activity with id: " + activity.getId()
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
	private String getActivityAS3(String nameFile) throws AmazonServiceException, AmazonClientException, IOException {
		BufferedReader bufReader = aS3Service.getFile(nameFile);
		return bufReader.lines().collect(Collectors.joining("\n"));
	}

}
