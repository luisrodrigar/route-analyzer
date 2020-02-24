package com.routeanalyzer.api.controller;

import com.routeanalyzer.api.facade.FileFacade;
import com.routeanalyzer.api.model.exception.FileNotFoundException;
import com.routeanalyzer.api.model.exception.FileOperationNotExecutedException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.constraints.Pattern;
import java.util.List;

import static com.routeanalyzer.api.common.CommonUtils.createOKApplicationOctetResponse;

@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/file")
@CrossOrigin(origins = "*", maxAge = 3600)
public class FileRestController {

	private final FileFacade fileFacade;

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
	@ResponseStatus(HttpStatus.CREATED)
	public List<String> uploadFile(@RequestParam("file") MultipartFile multiPart,
								   @RequestParam("type")
								   @Pattern(regexp = "gpx|tcx",
										   message = "Message type should be gpx or tcx.") String type)
			throws FileOperationNotExecutedException {
		return fileFacade.uploadFile(multiPart, type);
	}

	/**
	 * Get the XML file of original file stored in Amazon S3
	 * 
	 * @param id of activity
	 * @param type of the xml: tcx or gpx
	 * @return response: xml if all has gone well or a json file with the errors
	 */
	@GetMapping("/get/{type}/{id}")
	public ResponseEntity<String> getFile(@PathVariable  @Pattern(regexp = "^[a-f\\d]{24}$") final String id,
										  @PathVariable @Pattern(regexp = "gpx|tcx",
												  message = "Message type should be gpx or tcx.") final String type)
			throws FileNotFoundException {
		return createOKApplicationOctetResponse(fileFacade.getFile(id, type));
	}

}
