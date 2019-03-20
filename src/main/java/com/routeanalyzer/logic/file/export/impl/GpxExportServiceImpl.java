package com.routeanalyzer.logic.file.export.impl;

import com.routeanalyzer.common.CommonUtils;
import com.routeanalyzer.logic.file.export.ExportService;
import com.routeanalyzer.model.Activity;
import com.routeanalyzer.model.Lap;
import com.routeanalyzer.model.Position;
import com.routeanalyzer.model.TrackPoint;
import com.routeanalyzer.services.reader.GPXService;
import com.routeanalyzer.xml.gpx11.ExtensionsType;
import com.routeanalyzer.xml.gpx11.GpxType;
import com.routeanalyzer.xml.gpx11.MetadataType;
import com.routeanalyzer.xml.gpx11.TrkType;
import com.routeanalyzer.xml.gpx11.TrksegType;
import com.routeanalyzer.xml.gpx11.WptType;
import com.routeanalyzer.xml.gpx11.trackpointextension.garmin.TrackPointExtensionT;
import org.springframework.stereotype.Service;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.datatype.XMLGregorianCalendar;
import java.util.Optional;
import java.util.function.Function;

import static java.util.Optional.ofNullable;

@Service
public class GpxExportServiceImpl implements ExportService {

    private GPXService gpxService;

    public GpxExportServiceImpl(GPXService gpxService) {
        this.gpxService = gpxService;
    }

    @Override
    public String export(Activity act) throws JAXBException {
        com.routeanalyzer.xml.gpx11.ObjectFactory oFactory =
                new com.routeanalyzer.xml.gpx11.ObjectFactory();

        GpxType gpx = new GpxType();

        Optional<Activity> optAct = ofNullable(act);
        // Date time
        Function<XMLGregorianCalendar, MetadataType> generateMetadata = xmlGregorianCalendar -> {
            MetadataType metadata = new MetadataType();
            metadata.setTime(xmlGregorianCalendar);
            return metadata;
        };
        optAct.map(Activity::getDate)
                .flatMap(CommonUtils::toDate)
                .map(CommonUtils::createGregorianCalendar)
                .map(CommonUtils::createXmlGregorianCalendar)
                .map(generateMetadata)
                .ifPresent(gpx::setMetadata);
        // Device
        optAct.map(Activity::getDevice)
                .ifPresent(gpx::setCreator);
        // Tracks
        TrkType trk = new TrkType();

        optAct.map(Activity::getLaps)
                .ifPresent(laps -> {
                    laps.forEach(lap -> {
                        TrksegType trkSeg = new TrksegType();
                        gpxTrackPointsMapping(lap, trkSeg);
                        trk.addTrkseg(trkSeg);
                    });
                });
        gpx.addTrk(trk);
        return gpxService.createXML(oFactory.createGpx(gpx));
    }

    private void gpxTrackPointsMapping(Lap lap, TrksegType trkSeg) {
        com.routeanalyzer.xml.gpx11.trackpointextension.garmin.ObjectFactory extensionFactory =
                new com.routeanalyzer.xml.gpx11.trackpointextension.garmin.ObjectFactory();
        ofNullable(lap)
                .map(Lap::getTracks)
                .ifPresent(trackPoints -> {
                    trackPoints.forEach(trackPoint -> {
                        WptType wpt = new WptType();
                        Optional<TrackPoint> optTrackPoint = ofNullable(trackPoint);
                        // Date time
                        optTrackPoint.map(TrackPoint::getDate)
                                .flatMap(CommonUtils::toDate)
                                .map(CommonUtils::createGregorianCalendar)
                                .map(CommonUtils::createXmlGregorianCalendar)
                                .ifPresent(wpt::setTime);
                        // Latitude
                        optTrackPoint.map(TrackPoint::getPosition)
                                .map(Position::getLatitudeDegrees)
                                .ifPresent(wpt::setLat);
                        // Longitude
                        optTrackPoint.map(TrackPoint::getPosition)
                                .map(Position::getLongitudeDegrees)
                                .ifPresent(wpt::setLon);
                        // Elevation
                        optTrackPoint.map(TrackPoint::getAltitudeMeters)
                                .ifPresent(wpt::setEle);
                        // Heart rate
                        Function<Short, TrackPointExtensionT> setActivityLapExtension = heartRate -> {
                            TrackPointExtensionT actExtensionT = new TrackPointExtensionT();
                            actExtensionT.setHr(heartRate);
                            return actExtensionT;
                        };
                        Function<JAXBElement, ExtensionsType> setExtension = jaxbElement -> {
                            ExtensionsType extT = new ExtensionsType();
                            extT.addAny(jaxbElement);
                            return extT;
                        };
                        optTrackPoint.map(TrackPoint::getHeartRateBpm)
                                .map(Integer::shortValue)
                                .map(setActivityLapExtension)
                                .map(extensionFactory::createTrackPointExtension)
                                .map(setExtension)
                                .ifPresent(wpt::setExtensions);
                        trkSeg.addTrkpt(wpt);
                    });
                });
    }

}
