package com.routeanalyzer.api.services.reader;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;

import org.springframework.stereotype.Service;

import com.routeanalyzer.api.xml.tcx.TrainingCenterDatabaseT;
import com.routeanalyzer.api.xml.tcx.activityextension.ActivityLapExtensionT;
import com.routeanalyzer.api.xml.tcx.activityextension.ActivityTrackpointExtensionT;

@Service
public class TCXService extends AbstractXMLService<TrainingCenterDatabaseT> {

	public TCXService() {
		super(TrainingCenterDatabaseT.class);
	}

	@Override
	protected JAXBContext getJAXBContext() throws JAXBException {
		return JAXBContext.newInstance(TrainingCenterDatabaseT.class, ActivityLapExtensionT.class,
				ActivityTrackpointExtensionT.class);
	}

}
