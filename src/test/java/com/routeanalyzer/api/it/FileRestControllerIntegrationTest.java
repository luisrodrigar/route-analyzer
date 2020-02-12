package com.routeanalyzer.api.it;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.internal.SkipMd5CheckStrategy;
import com.amazonaws.services.s3.model.S3ObjectInputStream;
import com.routeanalyzer.api.database.ActivityMongoRepository;
import com.routeanalyzer.api.model.Activity;
import com.routeanalyzer.api.services.OriginalActivityRepository;
import io.vavr.control.Try;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MvcResult;
import org.testcontainers.containers.DockerComposeContainer;
import org.testcontainers.containers.localstack.LocalStackContainer;
import org.testcontainers.shaded.org.apache.commons.io.IOUtils;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.routeanalyzer.api.common.Constants.BAD_REQUEST_MESSAGE;
import static com.routeanalyzer.api.common.Constants.GET_FILE_PATH;
import static com.routeanalyzer.api.common.Constants.SOURCE_GPX_XML;
import static com.routeanalyzer.api.common.Constants.SOURCE_TCX_XML;
import static com.routeanalyzer.api.common.Constants.UPLOAD_FILE_PATH;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.MediaType.APPLICATION_XML_VALUE;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.testcontainers.containers.localstack.LocalStackContainer.Service.S3;
import static utils.TestUtils.getFileBytes;
import static utils.TestUtils.GPX_ID_XML;
import static utils.TestUtils.TCX_ID_XML;

@RunWith(SpringRunner.class)
@TestPropertySource("classpath:test.properties")
public class FileRestControllerIntegrationTest extends IntegrationTest {

    private static final String CORUNA_XML_FILE = "coruna.gpx.xml";
    private static final String OVIEDO_XML_FILE = "oviedo.tcx.xml";

    @ClassRule
    public static LocalStackContainer localStackS3 = new LocalStackContainer().withServices(S3);

    @Rule
    public DockerComposeContainer mongoDbContainer =
            new DockerComposeContainer(new File(DOCKER_COMPOSE_MONGO_DB))
                    .withExposedService(MONGO_CONTAINER_NAME, MONGO_PORT);

    @Value("${aws.s3-bucket}")
    private String bucketName;

    @Value("classpath:controller/xml-input-fake-1.json")
    private Resource unknownResource;
    @Value("classpath:controller/coruna.gpx.xml")
    private Resource gpxXmlResource;
    @Value("classpath:controller/oviedo.tcx.xml")
    private Resource tcxXmlResource;

    private MockMultipartFile gpxXmlFile;
    private MockMultipartFile tcxXmlFile;
    private MockMultipartFile unknownXmlFile;

    @Autowired
    private AmazonS3 amazonS3;
    @Autowired
    private OriginalActivityRepository originalActivityRepository;
    @Autowired
    private ActivityMongoRepository activityMongoRepository;

    @Before
    public void setUp() {
        amazonS3.createBucket(bucketName);
        loadMultiPartFiles();
    }

    @TestConfiguration
    public static class AWSTestConfiguration {

        @Bean
        @Primary
        public AmazonS3 s3client() {
            return AmazonS3ClientBuilder
                    .standard()
                    .withEndpointConfiguration(localStackS3.getEndpointConfiguration(S3))
                    .withCredentials(localStackS3.getDefaultCredentialsProvider())
                    .withPathStyleAccessEnabled(true)
                    .disableChunkedEncoding()
                    .enablePathStyleAccess()
                    .build();
        }
    }

    private void loadMultiPartFiles() {
        String fileName = "file";
        gpxXmlFile = new MockMultipartFile(fileName, CORUNA_XML_FILE, APPLICATION_XML_VALUE,
                getFileBytes(gpxXmlResource));
        tcxXmlFile = new MockMultipartFile(fileName, OVIEDO_XML_FILE, APPLICATION_XML_VALUE,
                getFileBytes(tcxXmlResource));
        unknownXmlFile = new MockMultipartFile(fileName, "", APPLICATION_XML_VALUE,
                getFileBytes(unknownResource));
    }

    @Test
    public void uploadGPXFileTest() throws Exception {
        // Given
        setPostFileBuilder(UPLOAD_FILE_PATH);

        // When
        MvcResult result = mockMvc.perform(builder.file(gpxXmlFile).param("type", SOURCE_GPX_XML))
                .andExpect(status().isOk())
                .andReturn();
        List<String> ids = getIds(result);

        // Then
        Optional<Activity> afterTestCase = activityMongoRepository.findById(ids.get(0));
        Optional<S3ObjectInputStream> fileStored = originalActivityRepository.getFile(String.format("%s.%s",
                ids.get(0), SOURCE_GPX_XML));

        assertThat(afterTestCase).isNotEmpty();
        assertThat(fileStored).isNotEmpty();
    }

    @Test
    public void uploadTCXFileTest() throws Exception {
        // Given
        setPostFileBuilder(UPLOAD_FILE_PATH);

        // When
        MvcResult result = mockMvc.perform(builder.file(tcxXmlFile).param("type", SOURCE_TCX_XML))
                .andExpect(status().isOk())
                .andReturn();
        List<String> ids = getIds(result);

        // Then
        Optional<Activity> afterTestCase = activityMongoRepository.findById(ids.get(0));
        Optional<S3ObjectInputStream> fileStored = originalActivityRepository.getFile(String.format("%s.%s",
                ids.get(0), SOURCE_TCX_XML));

        assertThat(afterTestCase).isNotEmpty();
        assertThat(fileStored).isNotEmpty();
    }

    private List<String> getIds(MvcResult result) {
        return Try.of(() -> result.getResponse().getContentAsString())
                .map(ids -> ids.replace("[", "")
                        .replace("]","")
                        .replace("\"", ""))
                .toJavaStream()
                .flatMap(ids -> Arrays.asList(ids.split(",")).stream())
                .map(String::trim)
                .collect(Collectors.toList());
    }

    @Test
    public void uploadUnknownFileTest() throws Exception {
        // Given
        setPostFileBuilder(UPLOAD_FILE_PATH);
        // When
        // Then
        isGenerateErrorByMockMultipartHTTPPost(unknownXmlFile, status().isBadRequest(), "kml",
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
        // Given
        System.setProperty(SkipMd5CheckStrategy.DISABLE_GET_OBJECT_MD5_VALIDATION_PROPERTY,"true");
        Try.of(() -> IOUtils.toString(gpxXmlResource.getInputStream(), UTF_8).getBytes())
                .forEach(arrayBytes -> originalActivityRepository.uploadFile(arrayBytes, String.format("%s.%s",
                        GPX_ID_XML, SOURCE_GPX_XML)));
        // When
        // Then
        mockMvc.perform(get(GET_FILE_PATH, SOURCE_GPX_XML, GPX_ID_XML)).andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_OCTET_STREAM.toString()))
                .andExpect(content().xml(new String(getFileBytes(gpxXmlResource), UTF_8)));
    }

    @Test
    public void getTcxFileTest() throws Exception {
        // Given
        System.setProperty(SkipMd5CheckStrategy.DISABLE_GET_OBJECT_MD5_VALIDATION_PROPERTY,"true");
        Try.of(() -> IOUtils.toString(tcxXmlResource.getInputStream(), UTF_8).getBytes())
                .forEach(arrayBytes -> originalActivityRepository.uploadFile(arrayBytes, String.format("%s.%s",
                        TCX_ID_XML, SOURCE_TCX_XML)));
        // When
        // Then
        mockMvc.perform(get(GET_FILE_PATH, SOURCE_TCX_XML, TCX_ID_XML))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_OCTET_STREAM.toString()))
                .andExpect(content().xml(new String(getFileBytes(tcxXmlResource), UTF_8)));
    }

}
