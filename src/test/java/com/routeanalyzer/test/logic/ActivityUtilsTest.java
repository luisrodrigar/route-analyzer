package com.routeanalyzer.test.logic;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import javax.xml.bind.JAXBException;

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

import com.google.gson.Gson;
import com.routeanalyzer.logic.impl.ActivityUtilsImpl;
import com.routeanalyzer.logic.impl.LapsUtilsImpl;
import com.routeanalyzer.model.Activity;
import com.routeanalyzer.services.reader.GPXService;
import com.routeanalyzer.services.reader.TCXService;
import com.routeanalyzer.test.common.TestUtils;
import com.routeanalyzer.xml.gpx11.GpxType;
import com.routeanalyzer.xml.tcx.TrainingCenterDatabaseT;

import io.vavr.control.Try;

@RunWith(SpringJUnit4ClassRunner.class)
public class ActivityUtilsTest {

	@Value("classpath:utils/gpx-test.xml")
	private Resource gpxXmlResource;
	@Value("classpath:utils/tcx-test.xml")
	private Resource tcxXmlResource;
	@Value("classpath:utils/json-activity.json")
	private Resource activityResource;

	private Activity activityTest;
	private GpxType gpxObject;
	private TrainingCenterDatabaseT tcxObject;
	private String gpxXmlString, tcxXmlString;

	@Mock
	private TCXService tcxService;
	@Mock
	private GPXService gpxService;
	@Mock
	private LapsUtilsImpl lapsUtilsImpl;

	@InjectMocks
	private ActivityUtilsImpl activityUtilsImpl;

	@Before
	public void setUp() throws SAXParseException, JAXBException, IOException {
		gpxXmlString = new String(TestUtils.getFileBytes(gpxXmlResource), StandardCharsets.UTF_8);
		tcxXmlString = new String(TestUtils.getFileBytes(tcxXmlResource), StandardCharsets.UTF_8);
		String jsonActivityString = new String(TestUtils.getFileBytes(activityResource), StandardCharsets.UTF_8);
		activityTest = new Gson().fromJson(jsonActivityString, Activity.class);
		gpxObject = new GPXService().readXML(gpxXmlResource.getInputStream());
		tcxObject = new TCXService().readXML(tcxXmlResource.getInputStream());
	}

	@Test
	public void exportAsTCXTest() {
		try {
			String tcxExportedFile = activityUtilsImpl.exportAsTCX(activityTest);
			assertEquals(tcxXmlString, tcxExportedFile);
		} catch (Exception e) {
			assertFalse(true);
		}
	}

	@Test
	public void exportAsGPXTest() {
		try {
			String gpxExportedFile = activityUtilsImpl.exportAsGPX(activityTest);
			assertEquals(gpxXmlString, gpxExportedFile);
		} catch (Exception e) {
			assertFalse(true);
		}
	}

	@Test
	public void uploadTCXFileTest() throws SAXParseException, JAXBException, IOException {
		when(tcxService.readXML(Mockito.any())).thenReturn(tcxObject);
		MultipartFile multipart = new MockMultipartFile("file", tcxXmlResource.getInputStream());
		assertEquals(Arrays.asList(activityTest), activityUtilsImpl.uploadTCXFile(multipart));
	}

	@Test
	public void uploadGPXFileTest() throws SAXParseException, JAXBException, IOException {
		when(gpxService.readXML(Mockito.any())).thenReturn(gpxObject);
		MultipartFile multipart = new MockMultipartFile("file", gpxXmlResource.getInputStream());

		System.out.println(activityTest);
		assertEquals(Arrays.asList(activityTest), activityUtilsImpl.uploadGPXFile(multipart));
	}

	@Test
	public void uploadTCXFileThrowExceptionTest() throws IOException, SAXParseException, JAXBException {
		doThrow(new SAXParseException("", null)).when(tcxService).readXML(Mockito.any());
		MultipartFile multipart = new MockMultipartFile("file", tcxXmlResource.getInputStream());
		Try.of(() -> activityUtilsImpl.uploadTCXFile(multipart)).onSuccess((success) -> assertTrue(false))
				.onFailure((error) -> assertTrue(error instanceof SAXParseException));
	}

	@Test
	public void uploadGPXFileThrowExceptionTest() throws IOException, SAXParseException, JAXBException {
		doThrow(new JAXBException("")).when(gpxService).readXML(Mockito.any());
		MultipartFile multipart = new MockMultipartFile("file", gpxXmlResource.getInputStream());
		Try.of(() -> activityUtilsImpl.uploadGPXFile(multipart)).onSuccess((success) -> assertTrue(false))
				.onFailure((error) -> assertTrue(error instanceof JAXBException));
	}

}
