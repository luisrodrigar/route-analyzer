package com.routeanalyzer.api.logic.file.upload.impl;

import com.routeanalyzer.api.logic.ActivityOperations;
import com.routeanalyzer.api.logic.LapsOperations;
import com.routeanalyzer.api.model.Activity;
import com.routeanalyzer.api.services.reader.GPXService;
import com.routeanalyzer.api.xml.gpx11.*;
import com.routeanalyzer.api.xml.gpx11.trackpointextension.garmin.TrackPointExtensionT;
import io.vavr.control.Try;
import lombok.extern.slf4j.Slf4j;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.web.multipart.MultipartFile;
import org.xml.sax.SAXParseException;
import utils.TestUtils;

import javax.xml.bind.JAXBException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.GregorianCalendar;
import java.util.List;

import static com.routeanalyzer.api.common.JsonUtils.fromJson;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static utils.TestUtils.getFileBytes;

@Slf4j
@RunWith(SpringJUnit4ClassRunner.class)
public class GpxUploadServiceImplTest {

    @Mock
    private GPXService gpxService;

    @Mock
    private LapsOperations lapsOperations;

    @Mock
    private ActivityOperations activityOperations;

    @InjectMocks
    private GpxUploadFileService gpxUploadService;

    @Value("classpath:utils/gpx-test.xml")
    private Resource gpxXmlResource;
    @Value("classpath:utils/upload-file-gpx-test.json")
    private Resource activityGpxResource;


    private GpxType gpxObject;
    private Activity activityGpxTest;

    @Before
    public void setUp() throws Exception {
        gpxObject = createValidGpxType();
        String jsonActivityGpxStr = new String(getFileBytes(activityGpxResource), UTF_8);
        activityGpxTest = fromJson(jsonActivityGpxStr, Activity.class).getOrNull();
    }

    private GpxType createValidGpxType() {
        GpxType gpxType = new GpxType();
        gpxType.setMetadata(createMetadata());
        gpxType.setCreator("Garmin Connect");
        gpxType.addTrk(createTrkType());
        return gpxType;
    }

    private MetadataType createMetadata() {
        MetadataType metadataType = new MetadataType();
        LocalDateTime localDateTime = LocalDateTime.of(2018, 2, 27, 13, 16, 13);
        metadataType.setTime(getXmlGregorianCalendar(ZonedDateTime.of(localDateTime, ZoneOffset.UTC)));
        return metadataType;
    }

    private TrkType createTrkType() {
        TrkType trkType = new TrkType();
        trkType.addTrkseg(createTrksegType1());
        trkType.addTrkseg(createTrksegType2());
        trkType.addTrkseg(createTrksegType3());
        return trkType;
    }

    private TrksegType createTrksegType1() {
        TrksegType trksegType = new TrksegType();
        LocalDateTime time1 = LocalDateTime.of(2018, 02, 27, 13, 16, 13);
        LocalDateTime time2 = LocalDateTime.of(2018, 02, 27, 13, 16, 18);
        LocalDateTime time3 = LocalDateTime.of(2018, 02, 27, 13, 16, 20);
        trksegType.addTrkpt(createTrkpt(new BigDecimal("42.6131970"), new BigDecimal("-6.5732170"), new BigDecimal(
                "557.3"), new Short("96"), getXmlGregorianCalendar(ZonedDateTime.of(time1, ZoneOffset.UTC))));
        trksegType.addTrkpt(createTrkpt(new BigDecimal("42.6132170"), new BigDecimal("-6.5733730"), new BigDecimal(
                "557.3"), new Short("96"), getXmlGregorianCalendar(ZonedDateTime.of(time2, ZoneOffset.UTC))));
        trksegType.addTrkpt(createTrkpt(new BigDecimal("42.6132120"), new BigDecimal("-6.5734430"), new BigDecimal(
                "557.3"), new Short("96"), getXmlGregorianCalendar(ZonedDateTime.of(time3, ZoneOffset.UTC))));
        return trksegType;
    }

    private TrksegType createTrksegType2() {
        TrksegType trksegType = new TrksegType();
        LocalDateTime time1 = LocalDateTime.of(2018, 02, 27, 13, 16, 30);
        LocalDateTime time2 = LocalDateTime.of(2018, 02, 27, 13, 16, 33);
        trksegType.addTrkpt(createTrkpt(new BigDecimal("42.6132120"), new BigDecimal("-6.5738250"), new BigDecimal(
                "557.3"), new Short("106"), getXmlGregorianCalendar(ZonedDateTime.of(time1, ZoneOffset.UTC))));
        trksegType.addTrkpt(createTrkpt(new BigDecimal("42.6132120"), new BigDecimal("-6.5739120"), new BigDecimal(
                "557.3"), new Short("109"), getXmlGregorianCalendar(ZonedDateTime.of(time2, ZoneOffset.UTC))));
        return trksegType;
    }

    private TrksegType createTrksegType3() {
        TrksegType trksegType = new TrksegType();
        LocalDateTime time1 = LocalDateTime.of(2018, 02, 27, 13, 17, 30);
        trksegType.addTrkpt(createTrkpt(new BigDecimal("42.6132120"), new BigDecimal("-6.5738250"), new BigDecimal(
                "557.3"), new Short("120"), getXmlGregorianCalendar(ZonedDateTime.of(time1, ZoneOffset.UTC))));
        return trksegType;
    }

    private WptType createTrkpt(final BigDecimal lat, final BigDecimal lon, final BigDecimal elevation,
                                final Short heartRate, final XMLGregorianCalendar time) {
        WptType trkpt = new WptType();
        trkpt.setTime(time);
        trkpt.setLat(lat);
        trkpt.setLon(lon);
        trkpt.setEle(elevation);
        trkpt.setExtensions(createExtensionType(heartRate));
        return trkpt;
    }

    private ExtensionsType createExtensionType(final Short heartRate) {
        ExtensionsType extensionsType = new ExtensionsType();
        extensionsType.addAny(createTrackPointExtensionT(heartRate));
        return extensionsType;
    }

    private TrackPointExtensionT createTrackPointExtensionT(final Short heartRate) {
        TrackPointExtensionT trackPointExtensionT = new TrackPointExtensionT();
        trackPointExtensionT.setHr(heartRate);
        return trackPointExtensionT;
    }

    private XMLGregorianCalendar getXmlGregorianCalendar(final ZonedDateTime zonedDateTime) {
        GregorianCalendar gregorianCalendar = GregorianCalendar.from(zonedDateTime);
        return Try.of(() -> DatatypeFactory.newInstance().newXMLGregorianCalendar(gregorianCalendar))
                        .onFailure(err -> log.error("Error trying to create xml gregorian calendar"))
                        .getOrNull();
    }

    @Test
    public void uploadGpxTest() throws IOException {
        // Given
        MultipartFile multipart = new MockMultipartFile("file", gpxXmlResource.getInputStream());
        doReturn(Try.success(TestUtils.toGpxRootModel(gpxXmlResource)))
                .when(gpxService).readXML(eq(gpxXmlResource.getInputStream()));
        doReturn(Try.success(gpxObject)).when(gpxService).readXML(Mockito.any());
        // When
        GpxType result = gpxUploadService.upload(multipart).get();
        // Then
        assertThat(result).isEqualTo(gpxObject);
    }

    @Test(expected = JAXBException.class)
    public void uploadThrowExceptionTest() throws IOException, SAXParseException, JAXBException {
        // Given
        MultipartFile multipart = new MockMultipartFile("file", gpxXmlResource.getInputStream());
        Exception jaxbException = new JAXBException("Problems with xml.");
        // When
        // Then
        doReturn(Try.failure(jaxbException)).when(gpxService).readXML(Mockito.any());
        gpxUploadService.upload(multipart).get();
    }

    @Test
    public void xmlConvertToModelTest() {
        // Given

        // When
        List<Activity> activities = gpxUploadService.toListActivities(gpxObject);

        // Then
        assertThat(activities).isEqualTo(asList(activityGpxTest));
    }

}
