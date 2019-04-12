package com.routeanalyzer.api.controller.rest;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.routeanalyzer.api.common.JsonUtils;
import com.routeanalyzer.api.common.ThrowingConsumer;
import com.routeanalyzer.api.common.ThrowingFunction;
import com.routeanalyzer.api.database.ActivityMongoRepository;
import com.routeanalyzer.api.logic.file.upload.UploadFileService;
import com.routeanalyzer.api.logic.file.upload.impl.GpxUploadFileService;
import com.routeanalyzer.api.logic.file.upload.impl.TcxUploadFileService;
import com.routeanalyzer.api.model.Activity;
import com.routeanalyzer.api.common.Response;
import com.routeanalyzer.api.services.OriginalRouteAS3Service;
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

import javax.xml.bind.JAXBException;
import java.io.BufferedReader;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import static java.util.Optional.ofNullable;
import static org.springframework.http.ResponseEntity.BodyBuilder;
import static org.springframework.http.ResponseEntity.badRequest;
import static org.springframework.http.ResponseEntity.ok;
import static org.springframework.http.ResponseEntity.status;
import static com.routeanalyzer.api.common.JsonUtils.toJson;

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
		ResponseEntity<String> emptyResponse = status(HttpStatus.NOT_FOUND).body(toJson(Collections.emptyList()));
		Function<String, ResponseEntity<String>> createResponse = ids -> ok().body(ids);
		return Try.of( () -> {
			Optional<String> response = Optional.empty();
			switch (type) {
				case TcxUploadFileService.SOURCE_XML_TYPE:
					response = generateActivityCreatedResponse(multiPart, tcxService);
					break;
				case GpxUploadFileService.SOURCE_XML_TYPE:
					response = generateActivityCreatedResponse(multiPart, gpxService);
					break;
			}
			return response.map(createResponse).orElse(emptyResponse);
		}).recover(AmazonClientException.class, (error) -> {
			AmazonClientException amazonClientException = AmazonClientException.class.cast(error);
			log.error(amazonClientException.getClass().getSimpleName() + " error: " + amazonClientException.getMessage());
			Response errorException = new Response(true,
					"Problem with the type of the file which you want to upload",
					amazonClientException.getMessage());
			return status(HttpStatus.INTERNAL_SERVER_ERROR).body(toJson(errorException));
		}).recover(RuntimeException.class, handleExceptions)
		.get();
	}

	private Optional<String> generateActivityCreatedResponse(MultipartFile multiPart, UploadFileService service)
			throws AmazonClientException {
		Function<List<String>, Optional<String>> createJsonIds = ids -> ofNullable(ids).map(JsonUtils::toJson);
		return ofNullable(multiPart)
				.map(ThrowingFunction.unchecked(MultipartFile::getBytes))
				.flatMap(bytes -> ofNullable(multiPart).map(service::upload)
						.map(activities -> saveActivity(activities, bytes))
						.flatMap(createJsonIds));
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
				return ok().headers(responseHeaders).body(xml);

			} catch (AmazonClientException amazonException) {
				log.error("AmazonClientException error", amazonException);
				responseHeaders.add("Content-Type", MediaType.APPLICATION_JSON_UTF8.toString());
				String json = "{" + "error:true," + "description: 'Problem trying to get the file :: Amazon S3 Problem'"
						+ "exception: " + amazonException.getMessage() + " }";
				return status(HttpStatus.INTERNAL_SERVER_ERROR).headers(responseHeaders).body(json);
			} catch (IOException iOException) {
				log.error("IOException error", iOException);
				responseHeaders.add("Content-Type", MediaType.APPLICATION_JSON_UTF8.toString());
				String json = "{" + "error:true,"
						+ "description: 'Problem trying to get the file :: Input/Output Problem'" + "exception: "
						+ iOException.getMessage() + " }";
				return status(HttpStatus.INTERNAL_SERVER_ERROR).headers(responseHeaders).body(json);
			}
		} else {
			responseHeaders.add("Content-Type", MediaType.APPLICATION_JSON_UTF8.toString());
			String json = "{" + "error:true," + "description:'Problem with the type of the file which you want to get'"
					+ "}";
			return badRequest().headers(responseHeaders).body(json);
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
		return activities.stream()
				.map(mongoRepository::save)
				.map(activity -> { ThrowingConsumer.unchecked( (act) -> aS3Service.uploadFile(arrayBytes,
						activity.getId() + "." +  activity.getSourceXmlType()));
					return activity;
				})
				.map(Activity::getId)
				.collect(Collectors.toList());
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

    private Function<RuntimeException, ResponseEntity<String>> handleExceptions = error -> {
        String logMessage = null;
        String description = null;
        String errorMessage = null;
        BodyBuilder bodyBuilder = null;
        HttpHeaders responseHeaders = new HttpHeaders();
        if (SAXParseException.class.isInstance(error)) {
            SAXParseException exception = SAXParseException.class.cast(error);
            logMessage = "SAXParseException error: " + error.getMessage();
            description = "Problem trying to parser xml file. Check if its correct.";
            errorMessage = exception.getMessage();
            bodyBuilder = badRequest();
        } else if (JAXBException.class.isInstance(error)) {
            JAXBException exception = JAXBException.class.cast(error);
            logMessage = "JAXBException error: " + error.getMessage();
            description = "Problem with the file format uploaded.";
            errorMessage = exception.getMessage();
            bodyBuilder = status(HttpStatus.INTERNAL_SERVER_ERROR);
        } else if(AmazonClientException.class.isInstance(error)){
            AmazonClientException exception = AmazonClientException.class.cast(error);
            logMessage = "AmazonClientException error: " + error.getMessage();
            description = "Problem trying to delete/get the activity/file :: Amazon S3 Problem";
            errorMessage = exception.getMessage();
            responseHeaders.add("Content-Type", MediaType.APPLICATION_JSON_UTF8.toString());
            bodyBuilder = status(HttpStatus.INTERNAL_SERVER_ERROR);
        } else if(IOException.class.isInstance(error)) {
            IOException exception = IOException.class.cast(error);
            logMessage = "IOException error: " + error.getMessage();
            description = "Problem trying to get the file :: Input/Output Problem";
            errorMessage = exception.getMessage();
            responseHeaders.add("Content-Type", MediaType.APPLICATION_JSON_UTF8.toString());
            bodyBuilder = status(HttpStatus.INTERNAL_SERVER_ERROR).headers(responseHeaders);
        }
        log.error(logMessage);
        Response errorBody = new Response(true, description, errorMessage);
        return bodyBuilder.body(toJson(errorBody));
	};
}
