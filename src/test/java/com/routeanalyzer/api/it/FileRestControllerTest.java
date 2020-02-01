package com.routeanalyzer.api.it;

import com.amazonaws.AmazonClientException;
import com.amazonaws.services.s3.model.S3ObjectInputStream;
import com.routeanalyzer.api.database.ActivityMongoRepository;
import com.routeanalyzer.api.it.config.S3AWSTestConfig;
import com.routeanalyzer.api.model.Activity;
import com.routeanalyzer.api.services.impl.OriginalRouteAS3ServiceImpl;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.testcontainers.containers.localstack.LocalStackContainer;
import org.xml.sax.SAXParseException;
import utils.TestUtils;

import javax.xml.bind.JAXBException;
import java.io.IOException;
import java.util.Optional;

import static com.routeanalyzer.api.common.Constants.BAD_REQUEST_MESSAGE;
import static com.routeanalyzer.api.common.Constants.GET_FILE_PATH;
import static com.routeanalyzer.api.common.Constants.SOURCE_GPX_XML;
import static com.routeanalyzer.api.common.Constants.SOURCE_TCX_XML;
import static com.routeanalyzer.api.common.Constants.UPLOAD_FILE_PATH;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_XML;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.testcontainers.containers.localstack.LocalStackContainer.Service.S3;
import static utils.TestUtils.ACTIVITY_GPX_ID;
import static utils.TestUtils.ACTIVITY_TCX_ID;
import static utils.TestUtils.getFileBytes;
import static utils.TestUtils.toActivity;
import static utils.TestUtils.toS3ObjectInputStream;

@ActiveProfiles("test-as3")
@RunWith(SpringRunner.class)
@TestPropertySource("/test.properties")
@ContextConfiguration(classes = {OriginalRouteAS3ServiceImpl.class, S3AWSTestConfig.class})
public class FileRestControllerTest extends IntegrationTest {

    private static final String APPLICATION_XML_STR = APPLICATION_XML.toString();

    @Autowired
    private ActivityMongoRepository activityMongoRepository;

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

    private MockMultipartFile xmlFile;
    private MockMultipartFile xmlOtherFile;
    private MockMultipartFile exceptionJAXBFile;
    private MockMultipartFile exceptionSAXFile;

    private Activity gpxActivity;
    private Activity tcxActivity;
    private Activity unknownXml;

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
    public void uploadGPXFileTest() throws Exception {
        // Given
        setPostFileBuilder(UPLOAD_FILE_PATH);

        Optional<Activity> beforeTestCase = activityMongoRepository.findById(ACTIVITY_GPX_ID);

        // When
        isReturningActivityHTTP(builder.file(xmlFile).param("type", SOURCE_GPX_XML), gpxActivity);

        // Then
        Optional<Activity> afterTestCase = activityMongoRepository.findById(ACTIVITY_GPX_ID);

        assertThat(beforeTestCase).isEmpty();
        assertThat(afterTestCase).isNotEmpty();
    }

    @Test
    public void uploadTCXFileTest() throws Exception {
        // Given
        setPostFileBuilder(UPLOAD_FILE_PATH);

        Optional<Activity> beforeTestCase = activityMongoRepository.findById(ACTIVITY_TCX_ID);

        // When
        isReturningActivityHTTP(builder.file(xmlFile).param("type", SOURCE_TCX_XML), tcxActivity);

        // Then
        Optional<Activity> afterTestCase = activityMongoRepository.findById(ACTIVITY_TCX_ID);

        assertThat(beforeTestCase).isEmpty();
        assertThat(afterTestCase).isNotEmpty();
    }

    @Test
    public void uploadUnknownFileTest() throws Exception {
        // Given
        setPostFileBuilder(UPLOAD_FILE_PATH);
        // When
        // Then
        isGenerateErrorByMockMultipartHTTPPost(xmlFile, status().isBadRequest(), "kml",
                BAD_REQUEST_MESSAGE);
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
        mockMvc.perform(get(GET_FILE_PATH, SOURCE_GPX_XML, "some_id")).andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_OCTET_STREAM.toString()))
                .andExpect(content().xml(new String(getFileBytes(gpxXmlResource), UTF_8)));
    }

    @Test
    public void getTcxFileTest() throws Exception {
        Optional<S3ObjectInputStream> optTcxS3ObjectInput = Optional.ofNullable(tcxXmlResource.getFile().toPath())
                .map(toS3ObjectInputStream::apply);
        mockMvc.perform(get(GET_FILE_PATH, SOURCE_TCX_XML, "some_id")).andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_OCTET_STREAM.toString()))
                .andExpect(content().xml(new String(getFileBytes(tcxXmlResource), UTF_8)));
    }

}
