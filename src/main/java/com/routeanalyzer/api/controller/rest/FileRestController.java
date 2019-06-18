package com.routeanalyzer.api.controller.rest;

import com.routeanalyzer.api.common.CommonUtils;
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
import java.io.InputStreamReader;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

import static com.routeanalyzer.api.common.CommonUtils.emptyResponse;
import static com.routeanalyzer.api.common.CommonUtils.getFileExportResponse;
import static com.routeanalyzer.api.common.Constants.BAD_TYPE_MESSAGE;
import static com.routeanalyzer.api.common.Constants.GET_FILE_PATH;
import static com.routeanalyzer.api.common.Constants.SOURCE_GPX_XML;
import static com.routeanalyzer.api.common.Constants.SOURCE_TCX_XML;
import static com.routeanalyzer.api.common.Constants.UPLOAD_FILE_PATH;
import static com.routeanalyzer.api.common.ThrowingFunction.unchecked;
import static java.util.Optional.of;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;
import static org.springframework.http.ResponseEntity.ok;

@RestController
@CrossOrigin(origins = "*", maxAge = 3600)
public class FileRestController extends RestControllerBase {

	private ActivityMongoRepository mongoRepository;
	private OriginalRouteAS3Service aS3Service;
	private TcxUploadFileService tcxService;
	private GpxUploadFileService gpxService;

	@Autowired
	public FileRestController(ActivityMongoRepository mongoRepository, OriginalRouteAS3Service aS3Service,
							  TcxUploadFileService tcxService, GpxUploadFileService gpxService) {
		super(LoggerFactory.getLogger(FileRestController.class));
		this.mongoRepository = mongoRepository;
		this.aS3Service = aS3Service;
		this.tcxService = tcxService;
		this.gpxService = gpxService;
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
				.getOrElse(CommonUtils::toBadRequestParams);
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
				.orElseGet(CommonUtils::toBadRequestParams);
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

	private Function<MultipartFile, Optional<String>> uploadTcxFile = multipartFile ->
			upload(multipartFile, tcxService);
	private Function<MultipartFile, Optional<String>> uploadGpxFile = multipartFile ->
			upload(multipartFile, gpxService);

	private Optional<String> upload(MultipartFile multiPart, UploadFileService service) {
		return ofNullable(multiPart)
				.map(unchecked(MultipartFile::getBytes))
				.flatMap(bytes -> of(multiPart)
						.map(service::upload)
						.map(activities -> saveActivity(activities, bytes))
						.map(JsonUtils::toJson));
	}

	/**
	 * Save activity in Amazon Web Services AS3
	 * @param activities: list of activities
	 * @param arrayBytes: array of bytes
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
				.collect(toList());
	}

	/**
	 * Get activity stored in Amazon Web Services S3.
	 * @param nameFile: name of the file in AWS S3
	 * @return serialized activity
	 */
	private String getActivityAS3(String nameFile) {
		return ofNullable(nameFile)
				.flatMap(aS3Service::getFile)
				.map(InputStreamReader::new)
				.map(BufferedReader::new)
				.map(BufferedReader::lines)
				.map(streamLines -> streamLines.collect(joining("\n")))
				.orElse(null);
	}

}
