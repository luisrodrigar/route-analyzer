package com.routeanalyzer.api.controller;

import com.routeanalyzer.api.logic.ActivityOperations;
import com.routeanalyzer.api.logic.file.upload.impl.GpxUploadFileService;
import com.routeanalyzer.api.logic.file.upload.impl.TcxUploadFileService;
import com.routeanalyzer.api.services.OriginalActivityRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Optional;

import static com.routeanalyzer.api.common.CommonUtils.setExportHeaders;
import static com.routeanalyzer.api.common.Constants.BAD_TYPE_MESSAGE;
import static com.routeanalyzer.api.common.Constants.SOURCE_GPX_XML;
import static com.routeanalyzer.api.common.Constants.SOURCE_TCX_XML;
import static io.vavr.API.$;
import static io.vavr.API.Case;
import static io.vavr.API.Match;
import static io.vavr.Predicates.is;
import static java.lang.String.format;
import static java.util.stream.Collectors.joining;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/file")
@CrossOrigin(origins = "*", maxAge = 3600)
public class FileRestController {

	private final OriginalActivityRepository aS3Service;
	private final TcxUploadFileService tcxService;
	private final GpxUploadFileService gpxService;
	private final ActivityOperations activityOperations;

	/**
	 * Upload xml file: tcx or gpx to activity object. It allows to process the
	 * data contained in the file
	 * 
	 * @param multiPart: xml file
	 * @param type: tcx or gpx
	 * @return id of the activity created or a json error with a description of
	 *         why it failed.
	 */
	@PostMapping("/upload")
	public List<String> uploadFile(@RequestParam("file") MultipartFile multiPart,
								   @RequestParam("type") String type) {
		return uploadTypeFile(type, multiPart)
				.orElseThrow(() -> new IllegalArgumentException(BAD_TYPE_MESSAGE));
	}

	/**
	 * Get the XML file of original file stored in Amazon S3
	 * 
	 * @param id of activity
	 * @param type of the xml: tcx or gpx
	 * @return response: xml if all has gone well or a json file with the errors
	 */
	@GetMapping("/get/{type}/{id}")
	public String getFile(@PathVariable final String id, @PathVariable final String type,
						  HttpServletResponse response) {
		setExportHeaders(response, id, type);
		return getActivityAS3(getFileName(id, type));
	}

	private Optional<List<String>> uploadTypeFile(String type, MultipartFile multipartFile) {
		return Match(type).option(
				Case($(is(SOURCE_TCX_XML)), tcxType -> activityOperations.uploadAndSave(multipartFile, tcxService)),
				Case($(is(SOURCE_GPX_XML)), gpxType -> activityOperations.uploadAndSave(multipartFile, gpxService)))
		.toJavaOptional();
	}

	/**
	 * Get activity stored in Amazon Web Services S3.
	 * @param fileName: name of the file in AWS S3
	 * @return serialized activity
	 */
	private String getActivityAS3(String fileName) {
		return aS3Service.getFile(fileName)
				.map(InputStreamReader::new)
				.map(BufferedReader::new)
				.map(BufferedReader::lines)
				.map(streamLines -> streamLines.collect(joining("\n")))
				.orElse(null);
	}

	private String getFileName(String id, String type) {
		return format("%s.%s", id, type);
	}

}
