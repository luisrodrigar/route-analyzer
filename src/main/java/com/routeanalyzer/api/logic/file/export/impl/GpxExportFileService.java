package com.routeanalyzer.api.logic.file.export.impl;

import com.routeanalyzer.api.common.DateUtils;
import com.routeanalyzer.api.logic.file.export.ExportFileService;
import com.routeanalyzer.api.model.Activity;
import com.routeanalyzer.api.model.Lap;
import com.routeanalyzer.api.model.Position;
import com.routeanalyzer.api.model.TrackPoint;
import com.routeanalyzer.api.services.reader.GPXService;
import com.routeanalyzer.api.xml.gpx11.trackpointextension.garmin.ObjectFactory;
import com.routeanalyzer.api.xml.gpx11.ExtensionsType;
import com.routeanalyzer.api.xml.gpx11.GpxType;
import com.routeanalyzer.api.xml.gpx11.MetadataType;
import com.routeanalyzer.api.xml.gpx11.TrkType;
import com.routeanalyzer.api.xml.gpx11.TrksegType;
import com.routeanalyzer.api.xml.gpx11.WptType;
import com.routeanalyzer.api.xml.gpx11.trackpointextension.garmin.TrackPointExtensionT;
import org.springframework.stereotype.Service;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.datatype.XMLGregorianCalendar;
import java.util.Optional;
import java.util.function.Function;

import static java.util.Optional.ofNullable;

@Service
public class GpxExportFileService implements ExportFileService {

    private GPXService gpxService;

    public GpxExportFileService(GPXService gpxService) {
        this.gpxService = gpxService;
    }

    @Override
    public String export(Activity act) throws JAXBException {
        com.routeanalyzer.api.xml.gpx11.ObjectFactory oFactory =
                new com.routeanalyzer.api.xml.gpx11.ObjectFactory();

        GpxType gpx = new GpxType();

        Optional<Activity> optAct = ofNullable(act);
        // Date time
        Function<XMLGregorianCalendar, MetadataType> generateMetadata = xmlGregorianCalendar -> {
            MetadataType metadata = new MetadataType();
            metadata.setTime(xmlGregorianCalendar);
            return metadata;
        };
        optAct.map(Activity::getDate)
                .flatMap(DateUtils::toDate)
                .map(DateUtils::createGregorianCalendar)
                .map(DateUtils::createXmlGregorianCalendar)
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
        ObjectFactory extensionFactory =
                new ObjectFactory();
        ofNullable(lap)
                .map(Lap::getTracks)
                .ifPresent(trackPoints -> {
                    trackPoints.forEach(trackPoint -> {
                        WptType wpt = new WptType();
                        Optional<TrackPoint> optTrackPoint = ofNullable(trackPoint);
                        // Date time
                        optTrackPoint.map(TrackPoint::getDate)
                                .flatMap(DateUtils::toDate)
                                .map(DateUtils::createGregorianCalendar)
                                .map(DateUtils::createXmlGregorianCalendar)
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
