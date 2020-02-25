package com.routeanalyzer.api.logic.file.export.impl;

import com.routeanalyzer.api.common.DateUtils;
import com.routeanalyzer.api.common.MathUtils;
import com.routeanalyzer.api.logic.file.export.ExportFileService;
import com.routeanalyzer.api.model.Activity;
import com.routeanalyzer.api.model.Lap;
import com.routeanalyzer.api.model.Position;
import com.routeanalyzer.api.model.TrackPoint;
import com.routeanalyzer.api.services.reader.TCXService;
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
import com.routeanalyzer.api.xml.tcx.activityextension.ObjectFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.xml.bind.JAXBElement;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static java.util.Optional.of;
import static java.util.Optional.ofNullable;

@Service
public class TcxExportFileService extends ExportFileService<TrainingCenterDatabaseT> {

    @Autowired
    public TcxExportFileService(TCXService tcxService) {
        super(tcxService);
    }

    @Override
    public Optional<JAXBElement<TrainingCenterDatabaseT>> convertToXmlObjects(Activity activity) {
        return ofNullable(activity)
                .map(Activity::getLaps)
                .map(this::toTcxLaps)
                .map(addActivityLaps)
                .map(getSetterActivityField::apply)
                .map(setActivityField -> setActivityField.apply(activity))
                .flatMap(createTrainingCenterDatabase)
                .map(objectFactorySupplier.get()::createTrainingCenterDatabase);
    }

    /**
     * Suppliers
     */
    private Supplier<com.routeanalyzer.api.xml.tcx.ObjectFactory> objectFactorySupplier = () ->
            new com.routeanalyzer.api.xml.tcx.ObjectFactory();
    private Supplier<ActivityT> activityTSupplier = () -> new ActivityT();
    private Supplier<ActivityListT> activityListTSupplier = () -> new ActivityListT();
    private Supplier<TrainingCenterDatabaseT> trainingCenterSupplier = () -> new TrainingCenterDatabaseT();
    private Supplier<ActivityLapExtensionT> activityLapExtensionTSupplier = () -> new ActivityLapExtensionT();
    private Supplier<ActivityTrackpointExtensionT> activityTrackPointExtensionTSupplier = () ->
            new ActivityTrackpointExtensionT();
    private Supplier<PositionT> positionTSupplier = () -> new PositionT();

    /**
     *
     * Functions
     *
     */
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

    private Function<ActivityT, Optional<TrainingCenterDatabaseT>> createTrainingCenterDatabase = activityT ->
            of(activityT)
                    .map(activityTParam -> {
                        ActivityListT activityListT = activityListTSupplier.get();
                        activityListT.addActivity(activityTParam);
                        return activityListT;
                    })
                    .map(activityListT -> {
                        TrainingCenterDatabaseT trainingCenterDatabaseT = trainingCenterSupplier.get();
                        trainingCenterDatabaseT.setActivities(activityListT);
                        return trainingCenterDatabaseT;
                    });

    private Function<List<ActivityLapT>, ActivityT> addActivityLaps = activityLapTS -> {
        ActivityT activityT = activityTSupplier.get();
        activityT.getLap().addAll(activityLapTS);
        return activityT;
    };

    private Function<Double, ActivityLapExtensionT> setActivityLapExtension = avgSpeed -> {
        ActivityLapExtensionT actExtensionT = activityLapExtensionTSupplier.get();
        actExtensionT.setAvgSpeed(avgSpeed);
        return actExtensionT;
    };

    private Function<Double, ActivityTrackpointExtensionT> setActivityTrackExtension = speed -> {
        ActivityTrackpointExtensionT trackPointExtension = activityTrackPointExtensionTSupplier.get();
        trackPointExtension.setSpeed(speed);
        return trackPointExtension;
    };
    private Function<Position, PositionT> mapPosition = position -> {
        PositionT positionT = positionTSupplier.get();
        positionT.setLatitudeDegrees(position.getLatitudeDegrees().doubleValue());
        positionT.setLongitudeDegrees(position.getLongitudeDegrees().doubleValue());
        return positionT;
    };

    private Function<ActivityT, Function<Activity, ActivityT>> getSetterActivityField = activityT -> activity -> {
        // Sport is an enum
        of(activity).map(Activity::getSport)
                .map(SportT::fromValue)
                .ifPresent(activityT::setSport);

        // Set xml gregorian calendar date
        of(activity).map(Activity::getDate)
                .flatMap(DateUtils::toDate)
                .map(DateUtils::createGregorianCalendar)
                .map(DateUtils::createXmlGregorianCalendar)
                .ifPresent(activityT::setId);
        return activityT;
    };

    private List<ActivityLapT> toTcxLaps(List<Lap> optLapList) {
        ObjectFactory extensionFactory =
                new ObjectFactory();
        return of(optLapList).map(List::stream)
                .map(laps ->
                    laps.map(lap -> {
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
                                .map(IntensityT::fromValue)
                                .ifPresent(lapT::setIntensity);
                        // Average speed
                        optLap.map(Lap::getAverageSpeed)
                                .map(setActivityLapExtension)
                                .map(extensionFactory::createLX)
                                .map(setTcxExtension)
                                .ifPresent(lapT::setExtensions);
                        // Tracks
                        TrackT trackT = toTrackT(optLap.map(Lap::getTracks));
                        lapT.addTrack(trackT);
                        return lapT;
                    }).collect(Collectors.toList())
                ).orElseGet(Collections::emptyList);
    }

    private TrackT toTrackT(Optional<List<TrackPoint>> optTrackPointList) {
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

}
