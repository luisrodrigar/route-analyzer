package com.routeanalyzer.logic.file.export.impl;

import com.routeanalyzer.common.CommonUtils;
import com.routeanalyzer.logic.file.export.ExportService;
import com.routeanalyzer.model.Activity;
import com.routeanalyzer.model.Lap;
import com.routeanalyzer.model.Position;
import com.routeanalyzer.model.TrackPoint;
import com.routeanalyzer.services.reader.TCXService;
import com.routeanalyzer.xml.tcx.ActivityLapT;
import com.routeanalyzer.xml.tcx.ActivityListT;
import com.routeanalyzer.xml.tcx.ActivityT;
import com.routeanalyzer.xml.tcx.ExtensionsT;
import com.routeanalyzer.xml.tcx.HeartRateInBeatsPerMinuteT;
import com.routeanalyzer.xml.tcx.IntensityT;
import com.routeanalyzer.xml.tcx.PositionT;
import com.routeanalyzer.xml.tcx.SportT;
import com.routeanalyzer.xml.tcx.TrackT;
import com.routeanalyzer.xml.tcx.TrackpointT;
import com.routeanalyzer.xml.tcx.TrainingCenterDatabaseT;
import com.routeanalyzer.xml.tcx.activityextension.ActivityLapExtensionT;
import com.routeanalyzer.xml.tcx.activityextension.ActivityTrackpointExtensionT;
import org.springframework.stereotype.Service;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;

import static java.util.Optional.ofNullable;

@Service
public class TcxExportServiceImpl implements ExportService {

    private TCXService tcxService;

    public TcxExportServiceImpl(TCXService tcxService) {
        this.tcxService = tcxService;
    }

    @Override
    public String export(Activity act) throws JAXBException {
        com.routeanalyzer.xml.tcx.ObjectFactory oFactory =
                new com.routeanalyzer.xml.tcx.ObjectFactory();

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
                .flatMap(CommonUtils::toDate)
                .map(CommonUtils::createGregorianCalendar)
                .map(CommonUtils::createXmlGregorianCalendar)
                .ifPresent(activityT::setId);
        // Laps
        tcxLapsMapping(activityT, optAct.map(Activity::getLaps));

        actListT.addActivity(activityT);
        trainingCenterDatabaseT.setActivities(actListT);

        return tcxService.createXML(oFactory.createTrainingCenterDatabase(trainingCenterDatabaseT));
    }

    private void tcxLapsMapping(ActivityT activityT, Optional<List<Lap>> optLapList) {
        com.routeanalyzer.xml.tcx.activityextension.ObjectFactory extensionFactory =
                new com.routeanalyzer.xml.tcx.activityextension.ObjectFactory();
        optLapList.map(List::stream)
                .ifPresent(laps -> {
                    laps.forEach(lap -> {
                        ActivityLapT lapT = new ActivityLapT();
                        Predicate<Double> isPositive = num -> num > 0;
                        Optional<Lap> optLap = ofNullable(lap);
                        // Start time in xml gregorian calendar
                        optLap.map(Lap::getStartTime)
                                .flatMap(CommonUtils::toDate)
                                .map(CommonUtils::createGregorianCalendar)
                                .map(CommonUtils::createXmlGregorianCalendar)
                                .ifPresent(lapT::setStartTime);
                        // Heart Rate lambda functions
                        Predicate<HeartRateInBeatsPerMinuteT> isPositiveHeartRateVal = heartRateObj ->
                                heartRateObj.getValue() > 0;
                        // heart rate average in beats per minute
                        optLap.map(Lap::getAverageHearRate)
                                .map(Double::shortValue)
                                .map(setTcxValueHeartRate)
                                .filter(isPositiveHeartRateVal)
                                .ifPresent(lapT::setAverageHeartRateBpm);
                        // Max heart rate in beats per minute
                        optLap.map(Lap::getMaximumHeartRate)
                                .map(Integer::shortValue)
                                .map(setTcxValueHeartRate)
                                .filter(isPositiveHeartRateVal)
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
                                .filter(isPositive)
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
        com.routeanalyzer.xml.tcx.activityextension.ObjectFactory extensionFactory =
                new com.routeanalyzer.xml.tcx.activityextension.ObjectFactory();
        TrackT trackT = new TrackT();
        optTrackPointList
                .ifPresent(tracks -> tracks.forEach(trackPoint -> {
                    TrackpointT trackPointT = new TrackpointT();
                    Optional<TrackPoint> optTrack = ofNullable(trackPoint);
                    // Start time
                    optTrack.map(TrackPoint::getDate)
                            .flatMap(CommonUtils::toDate)
                            .map(CommonUtils::createGregorianCalendar)
                            .map(CommonUtils::createXmlGregorianCalendar)
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
