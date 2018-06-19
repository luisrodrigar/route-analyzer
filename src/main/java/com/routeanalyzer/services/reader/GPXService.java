package com.routeanalyzer.services.reader;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;

import org.springframework.stereotype.Service;

import com.routeanalyzer.xml.gpx11.GpxType;
import com.routeanalyzer.xml.gpx11.trackpointextension.garmin.TrackPointExtensionT;

@Service
public class GPXService extends AbstractXMLService<GpxType> {

	public GPXService() {
		super(GpxType.class);
	}

	@Override
	protected JAXBContext getJAXBContext() throws JAXBException {
		return JAXBContext.newInstance(GpxType.class, TrackPointExtensionT.class);
	}

}
