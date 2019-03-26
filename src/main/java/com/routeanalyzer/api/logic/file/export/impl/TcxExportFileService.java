package com.routeanalyzer.api.logic.file.export.impl;

import com.routeanalyzer.api.common.DateUtils;
import com.routeanalyzer.api.common.MathUtils;
import com.routeanalyzer.api.logic.file.export.ExportFileService;
import com.routeanalyzer.api.model.Activity;
import com.routeanalyzer.api.model.Lap;
import com.routeanalyzer.api.model.Position;
import com.routeanalyzer.api.model.TrackPoint;
import com.routeanalyzer.api.services.reader.TCXService;
import com.routeanalyzer.api.xml.tcx.activityextension.ObjectFactory;
import com.routeanalyzer.api.xml.tcx.ActivityLapT;
import com.routeanalyzer.api.xml.tcx.ActivityListT;
import com.routeanalyzer.api.xml.tcx.ActivityT;
import com.routeanalyzer.api.xml.tcx.ExtensionsT;
import com.routeanalyzer.api.xml.tcx.HeartRateInBeatsPerMinuteT;
import com.routeanalyzer.api.xml.tcx.IntensityT;
import com.routeanalyzer.api.xml.tcx.PositionT;
import com.routeanalyzer.api.xml.tcx.SportT;
import com.routeanalyzer.api.xml.tcx.TrackT;
import com.routeanalyzer.api.xml.tcx.TrackpointT;
import com.routeanalyzer.api.xml.tcx.TrainingCenterDatabaseT;
import com.routeanalyzer.api.xml.tcx.activityextension.ActivityLapExtensionT;
import com.routeanalyzer.api.xml.tcx.activityextension.ActivityTrackpointExtensionT;
import org.springframework.stereotype.Service;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

import static java.util.Optional.ofNullable;

@Service
public class TcxExportFileService implements ExportFileService {

    private TCXService tcxService;

    public TcxExportFileService(TCXService tcxService) {
        this.tcxService = tcxService;
    }

    @Override
    public String export(Activity act) throws JAXBException {
        com.routeanalyzer.api.xml.tcx.ObjectFactory oFactory =
                new com.routeanalyzer.api.xml.tcx.ObjectFactory();

        TrainingCenterDatabaseT trainingCenterDatabaseT = new TrainingCenterDatabaseT();
        ActivityListT actListT = new ActivityListT();
        ActivityT activityT = new ActivityT();

        // Sport is an enum
        Optional<Activity> optAct = ofNullable(act);
        optAct.map(Activity::getSport)
                .map(SportT::valueOf)
                .ifPresent(activityT::setSport);

        // Set xml gregorian calendar date
        optAct.map(Activity::getDate)
                .flatMap(DateUtils::toDate)
                .map(DateUtils::createGregorianCalendar)
                .map(DateUtils::createXmlGregorianCalendar)
                .ifPresent(activityT::setId);
        // Laps
        tcxLapsMapping(activityT, optAct.map(Activity::getLaps));

        actListT.addActivity(activityT);
        trainingCenterDatabaseT.setActivities(actListT);

        return tcxService.createXML(oFactory.createTrainingCenterDatabase(trainingCenterDatabaseT));
    }

    private void tcxLapsMapping(ActivityT activityT, Optional<List<Lap>> optLapList) {
        ObjectFactory extensionFactory =
                new ObjectFactory();
        optLapList.map(List::stream)
                .ifPresent(laps -> {
                    laps.forEach(lap -> {
                        ActivityLapT lapT = new ActivityLapT();
                        Optional<Lap> optLap = ofNullable(lap);
                        // Start time in xml gregorian calendar
                        optLap.map(Lap::getStartTime)
                                .flatMap(DateUtils::toDate)
                                .map(DateUtils::createGregorianCalendar)
                                .map(DateUtils::createXmlGregorianCalendar)
                                .ifPresent(lapT::setStartTime);
                        // heart rate average in beats per minute
                        optLap.map(Lap::getAverageHearRate)
                                .map(Double::shortValue)
                                .map(setTcxValueHeartRate)
                                .filter(MathUtils::isPositiveHeartRate)
                                .ifPresent(lapT::setAverageHeartRateBpm);
                        // Max heart rate in beats per minute
                        optLap.map(Lap::getMaximumHeartRate)
                                .map(Integer::shortValue)
                                .map(setTcxValueHeartRate)
                                .filter(MathUtils::isPositiveHeartRate)
                                .ifPresent(lapT::setMaximumHeartRateBpm);
                        // Calories
                        optLap.map(Lap::getCalories)
                                .ifPresent(lapT::setCalories);
                        // Distance in meters
                        optLap.map(Lap::getDistanceMeters)
                                .ifPresent(lapT::setDistanceMeters);
                        // Max speed in meters per second
                        optLap.map(Lap::getMaximumSpeed)
                                .ifPresent(lapT::setMaximumSpeed);
                        // total time in seconds
                        optLap.map(Lap::getTotalTimeSeconds)
                                .filter(MathUtils::isPositiveNonZero)
                                .ifPresent(lapT::setTotalTimeSeconds);
                        // Intensity
                        optLap.map(Lap::getIntensity)
                                .map(IntensityT::valueOf)
                                .ifPresent(lapT::setIntensity);
                        // Average speed
                        Function<Double, ActivityLapExtensionT> setActivityLapExtension = avgSpeed -> {
                            ActivityLapExtensionT actExtensionT = new ActivityLapExtensionT();
                            actExtensionT.setAvgSpeed(avgSpeed);
                            return actExtensionT;
                        };
                        optLap.map(Lap::getAverageSpeed)
                                .map(setActivityLapExtension)
                                .map(extensionFactory::createLX)
                                .map(setTcxExtension)
                                .ifPresent(lapT::setExtensions);
                        // Tracks
                        TrackT trackT = tcxTrackPointsMapping(optLap.map(Lap::getTracks));
                        lapT.addTrack(trackT);
                        activityT.addLap(lapT);
                    });
                });
    }

    private TrackT tcxTrackPointsMapping(Optional<List<TrackPoint>> optTrackPointList) {
        ObjectFactory extensionFactory =
                new ObjectFactory();
        TrackT trackT = new TrackT();
        optTrackPointList
                .ifPresent(tracks -> tracks.forEach(trackPoint -> {
                    TrackpointT trackPointT = new TrackpointT();
                    Optional<TrackPoint> optTrack = ofNullable(trackPoint);
                    // Start time
                    optTrack.map(TrackPoint::getDate)
                            .flatMap(DateUtils::toDate)
                            .map(DateUtils::createGregorianCalendar)
                            .map(DateUtils::createXmlGregorianCalendar)
                            .ifPresent(trackPointT::setTime);
                    // Position: latitude and longitude
                    Function<Position, PositionT> mapPosition = position -> {
                        PositionT positionT = new PositionT();
                        positionT.setLatitudeDegrees(position.getLatitudeDegrees().doubleValue());
                        positionT.setLongitudeDegrees(position.getLongitudeDegrees().doubleValue());
                        return positionT;
                    };
                    optTrack.map(TrackPoint::getPosition)
                            .map(mapPosition)
                            .ifPresent(trackPointT::setPosition);
                    // Altitude
                    optTrack.map(TrackPoint::getAltitudeMeters)
                            .map(BigDecimal::doubleValue)
                            .ifPresent(trackPointT::setAltitudeMeters);
                    // Distance meters
                    optTrack.map(TrackPoint::getDistanceMeters)
                            .map(BigDecimal::doubleValue)
                            .ifPresent(trackPointT::setDistanceMeters);
                    // Heart rate
                    optTrack.map(TrackPoint::getHeartRateBpm)
                            .map(Integer::shortValue)
                            .map(setTcxValueHeartRate)
                            .ifPresent(trackPointT::setHeartRateBpm);

                    // Speed
                    Function<Double, ActivityTrackpointExtensionT> setActivityTrackExtension = speed -> {
                        ActivityTrackpointExtensionT trackPointExtension =
                                new ActivityTrackpointExtensionT();
                        trackPointExtension.setSpeed(speed);
                        return trackPointExtension;
                    };
                    optTrack.map(TrackPoint::getSpeed)
                            .map(BigDecimal::doubleValue)
                            .map(setActivityTrackExtension)
                            .map(extensionFactory::createTPX)
                            .map(setTcxExtension)
                            .ifPresent(trackPointT::setExtensions);
                    // Add track point to the list
                    trackT.addTrackpoint(trackPointT);

                }));
        return trackT;
    }

    private Function<JAXBElement, ExtensionsT> setTcxExtension = jaxbElement -> {
        ExtensionsT extT = new ExtensionsT();
        extT.addAny(jaxbElement);
        return extT;
    };

    private Function<Short, HeartRateInBeatsPerMinuteT> setTcxValueHeartRate = bpm -> {
        HeartRateInBeatsPerMinuteT heartRate = new HeartRateInBeatsPerMinuteT();
        heartRate.setValue(bpm);
        return heartRate;
    };
}
