package com.routeanalyzer.test.controller;

import static org.hamcrest.CoreMatchers.is;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;

import javax.xml.bind.JAXBException;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMultipartHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import org.xml.sax.SAXParseException;

import com.amazonaws.AmazonClientException;
import com.routeanalyzer.database.ActivityMongoRepository;
import com.routeanalyzer.logic.ActivityUtils;
import com.routeanalyzer.model.Activity;
import com.routeanalyzer.services.OriginalRouteAS3Service;

@ActiveProfiles("test")
@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
public class FileRestTestController {

	@Autowired
	private ActivityUtils activityUtilsService;
	@Autowired
	private OriginalRouteAS3Service aS3Service;
	@Autowired
	private MockMvc mockMvc;

	private MockMultipartHttpServletRequestBuilder builder;
	private MockMultipartFile xmlFile, xmlOtherFile, exceptionJAXBFile, exceptionSAXFile;
	private Activity activity;

	@Before
	public void setUp() {
		activity = new Activity();
		activity.setId("1234567890");

		builder = MockMvcRequestBuilders.multipart("/file/upload");
		builder.with(new RequestPostProcessor() {
			@Override
			public MockHttpServletRequest postProcessRequest(MockHttpServletRequest request) {
				request.setMethod("POST");
				return request;
			}
		});

		xmlFile = new MockMultipartFile("file", "", "application/xml", "{\"xml\": \"This is a fake xml.\"}".getBytes());
		xmlOtherFile = new MockMultipartFile("file", "", "application/xml",
				"{\"xml\": \"This is other fake xml.\"}".getBytes());
		exceptionJAXBFile = new MockMultipartFile("file", "", "application/xml",
				"{\"xml\": \"This xml generates a JAXBException.\"}".getBytes());
		exceptionSAXFile = new MockMultipartFile("file", "", "application/xml",
				"{\"xml\": \"This xml generates a SAXParseException.\"}".getBytes());
		try {
			Mockito.when(activityUtilsService.uploadGPXFile(xmlFile)).thenReturn(Collections.emptyList());
			Mockito.when(activityUtilsService.uploadTCXFile(xmlFile)).thenReturn(Collections.emptyList());
			Mockito.when(activityUtilsService.uploadGPXFile(exceptionJAXBFile))
					.thenThrow(new JAXBException("Syntax error while trying to parse the file."));
			Mockito.when(activityUtilsService.uploadTCXFile(exceptionSAXFile))
					.thenThrow(new SAXParseException("Syntax error while trying to parse the file.", null));
			Mockito.when(activityUtilsService.uploadTCXFile(xmlOtherFile)).thenReturn(Arrays.asList(activity));
		} catch (AmazonClientException | SAXParseException | IOException | JAXBException e) {
			e.printStackTrace();
		}
	}

	@Test
	public void uploadGPXFile() throws Exception {
		mockMvc.perform(builder.file(xmlFile).param("type", "gpx")).andExpect(status().isOk());
	}

	@Test
	public void uploadTCXFile() throws Exception {
		mockMvc.perform(builder.file(xmlFile).param("type", "tcx")).andExpect(status().isOk());
	}

	@Test
	public void uploadUnknownFile() throws Exception {
		mockMvc.perform(builder.file(xmlFile).param("type", "kml")).andExpect(status().isBadRequest())
				.andExpect(content().contentType("application/json;charset=UTF-8"))
				.andExpect(jsonPath("$.error", is(true)))
				.andExpect(jsonPath("$.description", is("Problem with the type of the file which you want to upload")));
	}

	@Test
	public void uploadSyntaxErrorJAXBExceptionFile() throws Exception {
		mockMvc.perform(builder.file(exceptionJAXBFile).param("type", "gpx"))
				.andExpect(status().isInternalServerError())
				.andExpect(content().contentType("application/json;charset=UTF-8"))
				.andExpect(jsonPath("$.error", is(true)))
				.andExpect(jsonPath("$.description", is("Problem with the file format uploaded.")))
				.andExpect(jsonPath("$.exception", is("Syntax error while trying to parse the file.")));
	}

	@Test
	public void uploadSyntaxErrorSAXExceptionFile() throws Exception {
		mockMvc.perform(builder.file(exceptionSAXFile).param("type", "tcx")).andExpect(status().isBadRequest())
				.andExpect(content().contentType("application/json;charset=UTF-8"))
				.andExpect(jsonPath("$.error", is(true)))
				.andExpect(jsonPath("$.description", is("Problem trying to parser xml file. Check if its correct.")))
				.andExpect(jsonPath("$.exception", is("Syntax error while trying to parse the file.")));
	}

	@Test
	public void uploadAWSS3ThrowException() throws Exception {
		// AWS AS3 throws an Exception when
		Mockito.doThrow(new AmazonClientException("Problems with AWS S3")).when(aS3Service).uploadFile(Mockito.any(),
				Mockito.anyString());
		mockMvc.perform(builder.file(xmlOtherFile).param("type", "tcx")).andExpect(status().isInternalServerError())
				.andExpect(content().contentType("application/json;charset=UTF-8"))
				.andExpect(jsonPath("$.error", is(true)))
				.andExpect(jsonPath("$.description", is("Problem with the type of the file which you want to upload")))
				.andExpect(jsonPath("$.exception", is("Problems with AWS S3")));
	}

}
