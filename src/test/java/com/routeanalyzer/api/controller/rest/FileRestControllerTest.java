package com.routeanalyzer.api.controller.rest;

import com.amazonaws.AmazonClientException;
import com.amazonaws.services.s3.model.S3ObjectInputStream;
import com.lordofthejars.nosqlunit.annotation.ShouldMatchDataSet;
import com.lordofthejars.nosqlunit.annotation.UsingDataSet;
import com.lordofthejars.nosqlunit.core.LoadStrategyEnum;
import com.routeanalyzer.api.database.ActivityMongoRepository;
import com.routeanalyzer.api.logic.file.upload.impl.GpxUploadFileService;
import com.routeanalyzer.api.logic.file.upload.impl.TcxUploadFileService;
import com.routeanalyzer.api.model.Activity;
import com.routeanalyzer.api.services.OriginalActivityRepository;
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
import org.springframework.data.domain.Example;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.xml.sax.SAXParseException;
import utils.TestUtils;

import javax.xml.bind.JAXBException;
import java.io.IOException;
import java.util.Arrays;
import java.util.Optional;

import static com.routeanalyzer.api.common.Constants.AMAZON_CLIENT_EXCEPTION_MESSAGE;
import static com.routeanalyzer.api.common.Constants.BAD_REQUEST_MESSAGE;
import static com.routeanalyzer.api.common.Constants.GET_FILE_PATH;
import static com.routeanalyzer.api.common.Constants.JAXB_EXCEPTION_MESSAGE;
import static com.routeanalyzer.api.common.Constants.SAX_PARSE_EXCEPTION_MESSAGE;
import static com.routeanalyzer.api.common.Constants.SOURCE_GPX_XML;
import static com.routeanalyzer.api.common.Constants.SOURCE_TCX_XML;
import static com.routeanalyzer.api.common.Constants.UPLOAD_FILE_PATH;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.contains;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_XML;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static utils.TestUtils.getFileBytes;
import static utils.TestUtils.toActivity;
import static utils.TestUtils.toRuntimeException;
import static utils.TestUtils.toS3ObjectInputStream;

@RunWith(SpringJUnit4ClassRunner.class)
@ActiveProfiles("test-mongodb")
@EnableAutoConfiguration(exclude = { MongoAutoConfiguration.class, MongoDataAutoConfiguration.class,
		MongoRepositoriesAutoConfiguration.class })
public class FileRestControllerTest extends MockMvcTestController {

	@Autowired
	protected ApplicationContext applicationContext;
	@Autowired
	private OriginalActivityRepository aS3Service;
	@Autowired
	private TcxUploadFileService tcxUploadFileService;
	@Autowired
	private GpxUploadFileService gpxUploadFileService;
	@Autowired
	private ActivityMongoRepository activityMongoRepository;

	private MockMultipartFile xmlFile;
	private MockMultipartFile xmlOtherFile;
	private MockMultipartFile exceptionJAXBFile;
	private MockMultipartFile exceptionSAXFile;

	private Activity gpxActivity;
	private Activity tcxActivity;
	private Activity unknownXml;

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


	@Value("classpath:utils/json-activity-tcx.json")
	private Resource tcxJsonResource;
	@Value("classpath:utils/json-activity-gpx.json")
	private Resource gpxJsonResource;

	private static final String APPLICATION_XML_STR = APPLICATION_XML.toString();

	@Before
	public void setUp() throws AmazonClientException, SAXParseException, IOException, JAXBException {
		gpxActivity = toActivity(gpxJsonResource).get();
		tcxActivity = toActivity(tcxJsonResource).get();
		unknownXml = TestUtils.createUnknownActivity.get();
		loadMultiPartFiles();
	}

	private void loadMultiPartFiles() {
		String fileName = "file";
		xmlFile = new MockMultipartFile(fileName, "", APPLICATION_XML_STR, getFileBytes(fake1));
		xmlOtherFile = new MockMultipartFile(fileName, "", APPLICATION_XML_STR, getFileBytes(fake2));
		exceptionJAXBFile = new MockMultipartFile(fileName, "", APPLICATION_XML_STR,
				getFileBytes(fakeJAXBException));
		exceptionSAXFile = new MockMultipartFile(fileName, "", APPLICATION_XML_STR,
				getFileBytes(fakeSAXParseException));
	}

	@Test
	@UsingDataSet(locations = "/controller/db-empty-data.json", loadStrategy = LoadStrategyEnum.CLEAN_INSERT)
	@ShouldMatchDataSet(location = "/controller/db-activity-gpx.json")
	public void uploadGPXFileTest() throws Exception {
		// Given
		setPostFileBuilder(UPLOAD_FILE_PATH);
		// When
		doReturn(Arrays.asList(gpxActivity)).when(gpxUploadFileService).upload(eq(xmlFile));
		// Then
		mockMvc.perform(builder.file(xmlFile).param("type", SOURCE_GPX_XML)).andExpect(status().isOk());
		assertThat(activityMongoRepository.exists(Example.of(gpxActivity))).isTrue();
	}

	@Test
	@UsingDataSet(locations = "/controller/db-empty-data.json", loadStrategy = LoadStrategyEnum.CLEAN_INSERT)
	@ShouldMatchDataSet(location = "/controller/db-activity-tcx.json")
	public void uploadTCXFileTest() throws Exception {
		// Given
		setPostFileBuilder(UPLOAD_FILE_PATH);
		// When
		doReturn(Arrays.asList(tcxActivity)).when(tcxUploadFileService).upload(eq(xmlFile));
		// Then
		mockMvc.perform(builder.file(xmlFile).param("type", SOURCE_TCX_XML))
				.andExpect(status().isOk());
		assertThat(activityMongoRepository.exists(Example.of(tcxActivity))).isTrue();
	}

	@Test
	public void uploadUnknownFileTest() throws Exception {
		// Given
		setPostFileBuilder(UPLOAD_FILE_PATH);
		// When
		doReturn(Arrays.asList(unknownXml)).when(tcxUploadFileService).upload(eq(xmlOtherFile));
		// Then
		isGenerateErrorByMockMultipartHTTPPost(xmlFile, status().isBadRequest(), "kml",
				BAD_REQUEST_MESSAGE);
	}

	@Test
	public void uploadFileThrowJAXBExceptionTest() throws Exception {
		// Given
		String exceptionDescription = "Syntax error while trying to parse the file.";
		Exception exception = toRuntimeException(new JAXBException(exceptionDescription));
		setPostFileBuilder(UPLOAD_FILE_PATH);
		// When
		doThrow(exception).when(gpxUploadFileService).upload(eq(exceptionJAXBFile));
		// Then
		isThrowingExceptionByMockMultipartHTTPPost(exceptionJAXBFile, status().isInternalServerError(), SOURCE_GPX_XML,
				JAXB_EXCEPTION_MESSAGE, exception.getCause());
	}

	@Test
	public void uploadFileThrowSAXExceptionTest() throws Exception {
		// Given
		String exceptionDescription = "Syntax error while trying to parse the file.";
		Exception exception = toRuntimeException(new SAXParseException(exceptionDescription, null));
		setPostFileBuilder(UPLOAD_FILE_PATH);
		// When
		doThrow(exception).when(tcxUploadFileService).upload(eq(exceptionSAXFile));
		// Then
		isThrowingExceptionByMockMultipartHTTPPost(exceptionSAXFile, status().isBadRequest(), SOURCE_TCX_XML,
				SAX_PARSE_EXCEPTION_MESSAGE, exception.getCause());
	}

	@Test
	public void uploadThrowAmazonExceptionException() throws Exception {
		// Given
		Exception exception = new AmazonClientException("Problems with AWS S3");
		setPostFileBuilder(UPLOAD_FILE_PATH);
		// When
		doReturn(Arrays.asList(unknownXml)).when(tcxUploadFileService).upload(eq(xmlOtherFile));
		// Amazon exception is generated after getting al the uploaded files.
		doThrow(exception).when(aS3Service).uploadFile(any(), anyString());
		// Then
		isThrowingExceptionByMockMultipartHTTPPost(xmlOtherFile, status().isInternalServerError(), SOURCE_TCX_XML,
				AMAZON_CLIENT_EXCEPTION_MESSAGE, exception);
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
		Optional<S3ObjectInputStream> optGpxS3ObjectInput = Optional.ofNullable(gpxXmlResource.getFile().toPath())
				.map(toS3ObjectInputStream::apply);
		when(aS3Service.getFile(contains(SOURCE_GPX_XML))).thenReturn(optGpxS3ObjectInput);
		mockMvc.perform(get(GET_FILE_PATH, SOURCE_GPX_XML, "some_id")).andExpect(status().isOk())
				.andExpect(content().contentType(MediaType.APPLICATION_OCTET_STREAM.toString()))
				.andExpect(content().xml(new String(getFileBytes(gpxXmlResource), UTF_8)));
	}

	@Test
	public void getTcxFileTest() throws Exception {
		Optional<S3ObjectInputStream> optTcxS3ObjectInput = Optional.ofNullable(tcxXmlResource.getFile().toPath())
				.map(toS3ObjectInputStream::apply);
		when(aS3Service.getFile(contains(SOURCE_TCX_XML))).thenReturn(optTcxS3ObjectInput);
		mockMvc.perform(get(GET_FILE_PATH, SOURCE_TCX_XML, "some_id")).andExpect(status().isOk())
				.andExpect(content().contentType(MediaType.APPLICATION_OCTET_STREAM.toString()))
				.andExpect(content().xml(new String(getFileBytes(tcxXmlResource), UTF_8)));
	}

}
