package com.routeanalyzer.services.reader;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;

import com.routeanalyzer.xml.gpx11.GpxType;
import com.routeanalyzer.xml.gpx11.trackpointextension.garmin.TrackPointExtensionT;

public class GPXReader extends XMLReaderService<GpxType> {

	public GPXReader() {
		super(GpxType.class);
	}

	@Override
	protected JAXBContext getJAXBContext() throws JAXBException {
		return JAXBContext.newInstance(GpxType.class, TrackPointExtensionT.class);
	}

}
