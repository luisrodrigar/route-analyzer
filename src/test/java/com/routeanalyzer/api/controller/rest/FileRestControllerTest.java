package com.routeanalyzer.api.controller.rest;

import com.amazonaws.AmazonClientException;
import com.routeanalyzer.api.controller.FileRestController;
import com.routeanalyzer.api.facade.FileFacade;
import com.routeanalyzer.api.model.Activity;
import com.routeanalyzer.api.model.exception.FileOperationNotExecutedException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.core.io.Resource;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMultipartHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import static com.routeanalyzer.api.common.Constants.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.http.MediaType.APPLICATION_OCTET_STREAM_VALUE;
import static org.springframework.http.MediaType.APPLICATION_XML_VALUE;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static utils.TestUtils.*;

@WebMvcTest(FileRestController.class)
@RunWith(SpringJUnit4ClassRunner.class)
public class FileRestControllerTest {

	private static final String TYPE = "type";
	private static final String UNKNOWN_TYPE_ACTIVITY = "unknown";

	@MockBean
	private FileFacade fileFacade;

	@Autowired
	private MockMvc mockMvc;

	private MockMultipartFile corunaGpxXmlFile;
	private MockMultipartFile oviedoTcxXmlFile;;

	private Activity gpxActivity;
	private Activity tcxActivity;

	@Value("classpath:controller/coruna.gpx.xml")
	private Resource gpxXmlResource;
	@Value("classpath:controller/oviedo.tcx.xml")
	private Resource tcxXmlResource;


	@Value("classpath:utils/json-activity-tcx.json")
	private Resource tcxJsonResource;
	@Value("classpath:utils/json-activity-gpx.json")
	private Resource gpxJsonResource;

	@Before
	public void setUp() throws AmazonClientException {
		loadActivityFiles();
		loadMultiPartFiles();
	}

	private void loadActivityFiles() {
		gpxActivity = toActivity(gpxJsonResource);
		tcxActivity = toActivity(tcxJsonResource);
	}

	private void loadMultiPartFiles() {
		String fileName = "file";
		corunaGpxXmlFile = new MockMultipartFile(fileName, "coruna.gpx.tcx", APPLICATION_XML_VALUE,
				getFileBytes(gpxXmlResource));
		oviedoTcxXmlFile = new MockMultipartFile(fileName, "oviedo.tcx.xml", APPLICATION_XML_VALUE,
				getFileBytes(tcxXmlResource));
	}

	/**
	 * HTTP POST with a file in the body uploadFile(...) test methods
	 *
	 */
	protected MockMultipartHttpServletRequestBuilder getMockMultiparfilePostBuilder(String path) {
		MockMultipartHttpServletRequestBuilder builder = MockMvcRequestBuilders.multipart(path);
		builder.with(request -> {
			request.setMethod("POST");
			return request;
		});
		return builder;
	}

	@Test
	public void uploadGPXFileTest() throws Exception {
		// Given
		MockMultipartHttpServletRequestBuilder mockMultipart = getMockMultiparfilePostBuilder(UPLOAD_FILE_PATH);
		doReturn(Arrays.asList(gpxActivity.getId())).when(fileFacade).uploadFile(eq(corunaGpxXmlFile),
				eq(SOURCE_GPX_XML));
		// When
		// Then
		mockMvc.perform(mockMultipart.file(corunaGpxXmlFile).param(TYPE, SOURCE_GPX_XML))
				.andExpect(status().isCreated());
		verify(fileFacade).uploadFile(any(MockMultipartFile.class), eq(SOURCE_GPX_XML));
	}

	@Test
	public void uploadTCXFileTest() throws Exception {
		// Given
		MockMultipartHttpServletRequestBuilder mockMultipart = getMockMultiparfilePostBuilder(UPLOAD_FILE_PATH);
		doReturn(Arrays.asList(tcxActivity.getId())).when(fileFacade).uploadFile(eq(oviedoTcxXmlFile),
				eq(SOURCE_TCX_XML));
		// When
		// Then
		mockMvc.perform(mockMultipart.file(oviedoTcxXmlFile).param(TYPE, SOURCE_TCX_XML))
				.andExpect(status().isCreated());
		verify(fileFacade).uploadFile(any(MockMultipartFile.class), eq(SOURCE_TCX_XML));
	}

	@Test
	public void uploadUnknownFileTest() throws Exception {
		// Given
		MockMultipartHttpServletRequestBuilder mockMultipart = getMockMultiparfilePostBuilder(UPLOAD_FILE_PATH);
		// When
		// Then
		mockMvc.perform(mockMultipart.file(oviedoTcxXmlFile).param(TYPE, UNKNOWN_TYPE_ACTIVITY))
				.andExpect(status().isBadRequest());
		verify(fileFacade, never()).uploadFile(any(MockMultipartFile.class), eq(UNKNOWN_TYPE_ACTIVITY));
	}

	@Test
	public void uploadFileThrowJAXBExceptionTest() throws Exception {
		// Given
		Exception exception = new FileOperationNotExecutedException("uploadFile", SOURCE_GPX_XML);
		MockMultipartHttpServletRequestBuilder mockMultipart = getMockMultiparfilePostBuilder(UPLOAD_FILE_PATH);
		doThrow(exception).when(fileFacade).uploadFile(eq(corunaGpxXmlFile),eq(SOURCE_GPX_XML));
		// When
		// Then
		mockMvc.perform(mockMultipart.file(corunaGpxXmlFile).param(TYPE, SOURCE_GPX_XML))
				.andExpect(status().isInternalServerError());

		verify(fileFacade).uploadFile(any(MockMultipartFile.class), eq(SOURCE_GPX_XML));
	}


	/**
	 *
	 * getFile(...,...) test methods
	 *
	 * @throws Exception
	 *
	 */

	@Test
	public void getGpxFileTest() throws Exception {
		// Given
		String xmlFileStr = new String(getFileBytes(gpxXmlResource), StandardCharsets.UTF_8);
		// When
		// Then
		when(fileFacade.getFile(ACTIVITY_GPX_ID, SOURCE_GPX_XML)).thenReturn(xmlFileStr);
		mockMvc.perform(get(GET_FILE_PATH, SOURCE_GPX_XML, ACTIVITY_GPX_ID)).andExpect(status().isOk())
				.andExpect(content().contentType(APPLICATION_OCTET_STREAM_VALUE))
				.andExpect(content().xml(xmlFileStr));
	}

	@Test
	public void getTcxFileTest() throws Exception {
		// Given
		String xmlFileStr = new String(getFileBytes(tcxXmlResource), StandardCharsets.UTF_8);
		// When
		// Then
		when(fileFacade.getFile(ACTIVITY_TCX_ID, SOURCE_TCX_XML)).thenReturn(xmlFileStr);
		mockMvc.perform(get(GET_FILE_PATH, SOURCE_TCX_XML, ACTIVITY_TCX_ID))
				.andExpect(status().isOk())
				.andExpect(content().contentType(APPLICATION_OCTET_STREAM_VALUE))
				.andExpect(content().xml(xmlFileStr));
	}

}
