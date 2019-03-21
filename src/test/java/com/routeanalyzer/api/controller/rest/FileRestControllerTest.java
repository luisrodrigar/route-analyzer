package com.routeanalyzer.controller.rest;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Optional;

import javax.xml.bind.JAXBException;

import com.routeanalyzer.logic.file.upload.impl.GpxUploadFileService;
import com.routeanalyzer.logic.file.upload.impl.TcxUploadFileService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.data.mongo.MongoDataAutoConfiguration;
import org.springframework.boot.autoconfigure.data.mongo.MongoRepositoriesAutoConfiguration;
import org.springframework.boot.autoconfigure.mongo.MongoAutoConfiguration;
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.xml.sax.SAXParseException;

import com.amazonaws.AmazonClientException;
import com.lordofthejars.nosqlunit.annotation.ShouldMatchDataSet;
import com.lordofthejars.nosqlunit.annotation.UsingDataSet;
import com.lordofthejars.nosqlunit.core.LoadStrategyEnum;
import com.routeanalyzer.logic.ActivityUtils;
import com.routeanalyzer.model.Activity;
import com.routeanalyzer.services.OriginalRouteAS3Service;
import com.routeanalyzer.common.TestUtils;

import static org.mockito.Mockito.when;
import static org.mockito.Mockito.contains;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.doThrow;

@RunWith(SpringJUnit4ClassRunner.class)
@ActiveProfiles("test-mongodb")
@EnableAutoConfiguration(exclude = { MongoAutoConfiguration.class, MongoDataAutoConfiguration.class,
		MongoRepositoriesAutoConfiguration.class })
public class FileRestControllerTest extends MockMvcTestController {

	@Autowired
	protected ApplicationContext applicationContext;
	@Autowired
	private ActivityUtils activityUtilsService;
	@Autowired
	private OriginalRouteAS3Service aS3Service;
	@Autowired
	private TcxUploadFileService tcxService;
	@Autowired
	private GpxUploadFileService gpxService;

	private MockMultipartFile xmlFile, xmlOtherFile, exceptionJAXBFile, exceptionSAXFile;
	private Activity gpxActivity, tcxActivity, unknownXml;

	@Value("classpath:controller/xml-input-fake-1.json")
	private Resource fake1;
	@Value("classpath:controller/xml-input-fake-2.json")
	private Resource fake2;
	@Value("classpath:controller/xml-input-fake-jaxb-exception.json")
	private Resource fakeJAXBException;
	@Value("classpath:controller/xml-input-fake-sax-parse-exception.json")
	private Resource fakeSAXParseException;

	@Value("classpath:controller/coruna.gpx.xml")
	private Resource gpxXmlResource;
	@Value("classpath:controller/oviedo.tcx.xml")
	private Resource tcxXmlResource;

	@Before
	public void setUp() throws AmazonClientException, SAXParseException, IOException, JAXBException {
		gpxActivity = TestUtils.createGPXActivity.get();
		tcxActivity = TestUtils.createTCXActivity.get();
		unknownXml = TestUtils.createUnknownActivity.get();

		loadMultiPartFiles();
		setMockBehaviour();
	}

	private void setMockBehaviour() throws AmazonClientException, SAXParseException, IOException, JAXBException {
		String exceptionDescription = "Syntax error while trying to parse the file.";
		doReturn(Arrays.asList(gpxActivity)).when(gpxService).upload(xmlFile);
		doReturn(Arrays.asList(tcxActivity)).when(tcxService).upload(xmlFile);
		doThrow(new JAXBException(exceptionDescription))
				.when(gpxService).upload(exceptionJAXBFile);
		doThrow(new SAXParseException(exceptionDescription, null))
				.when(tcxService).upload(exceptionSAXFile);
		doReturn(Arrays.asList(unknownXml))
				.when(tcxService).upload(xmlOtherFile);
	}

	private void loadMultiPartFiles() {
		String fileName = "file";

		xmlFile = new MockMultipartFile(fileName, "", MediaType.APPLICATION_XML.toString(),
				TestUtils.getFileBytes(fake1));
		xmlOtherFile = new MockMultipartFile(fileName, "", MediaType.APPLICATION_XML.toString(),
				TestUtils.getFileBytes(fake2));
		exceptionJAXBFile = new MockMultipartFile(fileName, "", MediaType.APPLICATION_XML.toString(),
				TestUtils.getFileBytes(fakeJAXBException));
		exceptionSAXFile = new MockMultipartFile(fileName, "", MediaType.APPLICATION_XML.toString(),
				TestUtils.getFileBytes(fakeSAXParseException));
	}

	@Test
	@UsingDataSet(locations = "/controller/db-empty-data.json", loadStrategy = LoadStrategyEnum.CLEAN_INSERT)
	@ShouldMatchDataSet(location = "/controller/db-activity-gpx.json")
	public void uploadGPXFile() throws Exception {
		uploadFileBuilder();
		mockMvc.perform(builder.file(xmlFile).param("type", "gpx")).andExpect(status().isOk());
	}

	@Test
	@UsingDataSet(locations = "/controller/db-empty-data.json", loadStrategy = LoadStrategyEnum.CLEAN_INSERT)
	@ShouldMatchDataSet(location = "/controller/db-activity-tcx.json")
	public void uploadTCXFile() throws Exception {
		uploadFileBuilder();
		mockMvc.perform(builder.file(xmlFile).param("type", "tcx")).andExpect(status().isOk());
	}

	@Test
	public void uploadUnknownFile() throws Exception {
		uploadFileBuilder();
		isGenerateErrorByMockMultipartHTTPPost(xmlFile, status().isBadRequest(), "kml",
				"Problem with the type of the file which you want to upload");
	}

	@Test
	public void uploadSyntaxErrorJAXBExceptionFile() throws Exception {
		uploadFileBuilder();
		isThrowingExceptionByMockMultipartHTTPPost(exceptionJAXBFile, status().isInternalServerError(), "gpx",
				"Problem with the file format uploaded.", "Syntax error while trying to parse the file.");
	}

	@Test
	public void uploadSyntaxErrorSAXExceptionFile() throws Exception {
		uploadFileBuilder();
		isThrowingExceptionByMockMultipartHTTPPost(exceptionSAXFile, status().isBadRequest(), "tcx",
				"Problem trying to parser xml file. Check if its correct.",
				"Syntax error while trying to parse the file.");
	}

	@Test
	public void uploadAWSS3ThrowException() throws Exception {
		// AWS AS3 throws an Exception when
		doThrow(new AmazonClientException("Problems with AWS S3")).when(aS3Service).uploadFile(any(), anyString());
		uploadFileBuilder();
		isThrowingExceptionByMockMultipartHTTPPost(xmlOtherFile, status().isInternalServerError(), "tcx",
				"Problem with the type of the file which you want to upload", "Problems with AWS S3");
	}

	/**
	 *
	 * getFile(...,...) test methods
	 * 
	 * @throws Exception
	 * 
	 */

	@Test
	public void getFileTest() throws Exception {
		BufferedReader gpxBufferedReader = Optional.ofNullable(gpxXmlResource.getFile().toPath())
				.map(TestUtils.toBufferedReader::apply).orElse(null);
		BufferedReader tcxBufferedReader = Optional.ofNullable(tcxXmlResource.getFile().toPath())
				.map(TestUtils.toBufferedReader::apply).orElse(null);
		when(aS3Service.getFile(contains("gpx"))).thenReturn(gpxBufferedReader);
		when(aS3Service.getFile(contains("tcx"))).thenReturn(tcxBufferedReader);
		mockMvc.perform(get("/file/get/{type}/{id}", "gpx", "some_id")).andExpect(status().isOk())
				.andExpect(content().contentType(MediaType.APPLICATION_OCTET_STREAM.toString()))
				.andExpect(content().xml(new String(TestUtils.getFileBytes(gpxXmlResource), StandardCharsets.UTF_8)));
		mockMvc.perform(get("/file/get/{type}/{id}", "tcx", "some_id")).andExpect(status().isOk())
				.andExpect(content().contentType(MediaType.APPLICATION_OCTET_STREAM.toString()))
				.andExpect(content().xml(new String(TestUtils.getFileBytes(tcxXmlResource), StandardCharsets.UTF_8)));
	}

}
