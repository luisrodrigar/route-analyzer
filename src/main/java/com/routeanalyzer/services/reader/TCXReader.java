package com.routeanalyzer.services.reader;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;

import com.routeanalyzer.xml.tcx.TrainingCenterDatabaseT;
import com.routeanalyzer.xml.tcx.activityextension.ActivityLapExtensionT;
import com.routeanalyzer.xml.tcx.activityextension.ActivityTrackpointExtensionT;

public class TCXReader extends XMLReaderService<TrainingCenterDatabaseT> {

	public TCXReader() {
		super(TrainingCenterDatabaseT.class);
	}

	@Override
	protected JAXBContext getJAXBContext() throws JAXBException {
		return JAXBContext.newInstance(TrainingCenterDatabaseT.class, ActivityLapExtensionT.class,
				ActivityTrackpointExtensionT.class);
	}

}
