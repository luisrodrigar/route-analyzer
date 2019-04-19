package com.routeanalyzer.api.controller.rest;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.routeanalyzer.api.common.JsonUtils;
import com.routeanalyzer.api.database.ActivityMongoRepository;
import com.routeanalyzer.api.logic.file.upload.UploadFileService;
import com.routeanalyzer.api.logic.file.upload.impl.GpxUploadFileService;
import com.routeanalyzer.api.logic.file.upload.impl.TcxUploadFileService;
import com.routeanalyzer.api.model.Activity;
import com.routeanalyzer.api.services.OriginalRouteAS3Service;
import io.vavr.control.Try;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.routeanalyzer.api.common.CommonUtils.emptyResponse;
import static com.routeanalyzer.api.common.CommonUtils.getFileExportResponse;
import static com.routeanalyzer.api.common.CommonUtils.toBadRequestParams;
import static com.routeanalyzer.api.common.Constants.BAD_TYPE_MESSAGE;
import static com.routeanalyzer.api.common.Constants.GET_FILE_PATH;
import static com.routeanalyzer.api.common.Constants.SOURCE_GPX_XML;
import static com.routeanalyzer.api.common.Constants.SOURCE_TCX_XML;
import static com.routeanalyzer.api.common.Constants.UPLOAD_FILE_PATH;
import static com.routeanalyzer.api.common.ThrowingFunction.unchecked;
import static java.util.Optional.of;
import static java.util.Optional.ofNullable;
import static org.springframework.http.ResponseEntity.ok;

@RestController
@CrossOrigin(origins = "*", maxAge = 3600)
public class FileRestController extends RestControllerBase {
	
	@Autowired
	private ActivityMongoRepository mongoRepository;
	@Autowired
	private OriginalRouteAS3Service aS3Service;
	@Autowired
	private TcxUploadFileService tcxService;
	@Autowired
	private GpxUploadFileService gpxService;

	public FileRestController() {
		super(LoggerFactory.getLogger(FileRestController.class));
	}

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
	@PostMapping(value = UPLOAD_FILE_PATH)
	public ResponseEntity<String> uploadFile(@RequestParam("file") MultipartFile multiPart,
			@RequestParam("type") String type) {
		Function<String, ResponseEntity<String>> createResponse = ids -> ok().body(ids);
		return Try.of(() -> uploadTypeFile(type, multiPart))
				.map(bodyResponse -> bodyResponse.map(createResponse).orElse(emptyResponse()))
				.recover(RuntimeException.class, handleControllerExceptions)
				.getOrElse(() -> toBadRequestParams());
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
	@GetMapping(value = GET_FILE_PATH)
	public ResponseEntity<String> getFile(@PathVariable final String id, @PathVariable final String type) {
		return ofNullable(type)
				.filter(StringUtils::isNotEmpty)
				.map(typeNotEmpty -> ofNullable(id)
						.map(idParam -> idParam + "." + typeNotEmpty)
						.orElse(null))
				.map(fileName -> Try.of(() -> getActivityAS3(fileName)))
				.map(response -> response
						.map(xml -> getFileExportResponse(xml, id, type))
						.recover(RuntimeException.class, handleControllerExceptions)
						.getOrElse(emptyResponse()))
				.orElseGet(() -> toBadRequestParams());
	}

	private Optional<String> uploadTypeFile(String type, MultipartFile multipartFile) {
		return ofNullable(type)
				.map(String::toLowerCase)
				.flatMap(typeLowerCase -> of(typeLowerCase)
						.filter(SOURCE_TCX_XML::equalsIgnoreCase)
						.flatMap(__ -> of(multipartFile)
								.map(uploadTcxFile))
						.orElseGet(() -> of(typeLowerCase)
								.filter(SOURCE_GPX_XML::equalsIgnoreCase)
								.flatMap(__ -> of(multipartFile)
										.map(uploadGpxFile))
								.orElseThrow( () ->
										new RuntimeException(new IllegalArgumentException(BAD_TYPE_MESSAGE)))));
	}

	Function<MultipartFile, Optional<String>> uploadTcxFile = multipartFile ->
			getBodyResponse(multipartFile, tcxService);
	Function<MultipartFile, Optional<String>> uploadGpxFile = multipartFile ->
			getBodyResponse(multipartFile, gpxService);

	private Optional<String> getBodyResponse(MultipartFile multiPart, UploadFileService service) {
		Function<List<String>, Optional<String>> createJsonIds = ids -> ofNullable(ids).map(JsonUtils::toJson);
		return ofNullable(multiPart)
				.map(unchecked(MultipartFile::getBytes))
				.flatMap(bytes -> ofNullable(multiPart)
						.map(service::upload)
						.map(activities -> saveActivity(activities, bytes))
						.flatMap(createJsonIds));
	}

	/**
	 * 
	 * @param activities
	 * @param arrayBytes
	 * @return saved activity list
	 */
	private List<String> saveActivity(List<Activity> activities, byte[] arrayBytes) {
		return activities.stream()
				.map(mongoRepository::save)
				.map(unchecked( activity -> {
						aS3Service.uploadFile(arrayBytes, activity.getId() + "." + activity.getSourceXmlType());
						return activity;
				}))
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
	private String getActivityAS3(String nameFile) throws AmazonClientException {
		BufferedReader bufReader = aS3Service.getFile(nameFile);
		return bufReader.lines().collect(Collectors.joining("\n"));
	}

}
