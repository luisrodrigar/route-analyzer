package com.routeanalyzer.api.logic.file.upload.impl;

import com.google.common.collect.Lists;
import com.routeanalyzer.api.common.CommonUtils;
import com.routeanalyzer.api.common.DateUtils;
import com.routeanalyzer.api.common.MathUtils;
import com.routeanalyzer.api.logic.ActivityOperations;
import com.routeanalyzer.api.logic.LapsOperations;
import com.routeanalyzer.api.logic.file.upload.UploadFileService;
import com.routeanalyzer.api.model.Activity;
import com.routeanalyzer.api.model.Lap;
import com.routeanalyzer.api.model.Position;
import com.routeanalyzer.api.model.TrackPoint;
import com.routeanalyzer.api.services.reader.TCXService;
import com.routeanalyzer.api.xml.tcx.AbstractSourceT;
import com.routeanalyzer.api.xml.tcx.ActivityLapT;
import com.routeanalyzer.api.xml.tcx.ActivityListT;
import com.routeanalyzer.api.xml.tcx.ActivityT;
import com.routeanalyzer.api.xml.tcx.CourseLapT;
import com.routeanalyzer.api.xml.tcx.CourseListT;
import com.routeanalyzer.api.xml.tcx.CourseT;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.xml.bind.JAXBElement;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.IntStream;

import static com.routeanalyzer.api.common.CommonUtils.toPosition;
import static com.routeanalyzer.api.common.CommonUtils.toTrackPoint;
import static com.routeanalyzer.api.common.Constants.SOURCE_TCX_XML;
import static java.util.Optional.of;
import static java.util.Optional.ofNullable;

@Service
public class TcxUploadFileService extends UploadFileService<TrainingCenterDatabaseT> {

    private ActivityOperations activityOperations;
    private LapsOperations lapsOperations;

    @Autowired
    public TcxUploadFileService(TCXService tcxService, ActivityOperations activityOperations,
                                LapsOperations lapsOperations) {
        super(tcxService);
        this.activityOperations = activityOperations;
        this.lapsOperations = lapsOperations;
    }

    private Function<PositionT, Predicate<Position>> getIsThisPosition = positionT -> positionToCompare ->
            positionT.getLatitudeDegrees() == positionToCompare.getLatitudeDegrees().doubleValue()
                    && positionT.getLongitudeDegrees() == positionToCompare.getLongitudeDegrees().doubleValue();

    /**
     *
     * @param optXmlType
     * @return
     */
    @Override
    protected List<Activity> toListActivities(Optional<TrainingCenterDatabaseT> optXmlType) {
        return optXmlType
                .map(TrainingCenterDatabaseT::getActivities)
                .map(this::toActivities)
                .orElseGet(() -> optXmlType
                        .map(TrainingCenterDatabaseT::getCourses)
                        .map(this::toActivities)
                        .orElseGet(Collections::emptyList));

    }

    private List<Activity> toActivities(ActivityListT activityListT) {
        List<Activity> activities = Lists.newArrayList();
        activityListT.getActivity().forEach(eachActivity -> {
            AtomicInteger indexLap = new AtomicInteger();
            AtomicInteger indexTrackPoint = new AtomicInteger();
            Activity activity = new Activity();
            activity.setSourceXmlType(SOURCE_TCX_XML);
            of(eachActivity)
                    .map(ActivityT::getCreator)
                    .map(AbstractSourceT::getName)
                    .ifPresent(activity::setDevice);
            of(eachActivity)
                    .map(ActivityT::getId)
                    .flatMap(DateUtils::toLocalDateTime)
                    .ifPresent(activity::setDate);
            of(eachActivity)
                    .map(ActivityT::getSport)
                    .map(SportT::value)
                    .ifPresent(activity::setSport);
            eachActivity.getLap().forEach(eachLap -> {
                Lap lap = new Lap();
                of(eachLap)
                        .map(ActivityLapT::getAverageHeartRateBpm)
                        .map(HeartRateInBeatsPerMinuteT::getValue)
                        .map(Double::new)
                        .filter(MathUtils::isPositiveNonZero)
                        .ifPresent(lap::setAverageHearRate);
                of(eachLap)
                        .map(ActivityLapT::getCalories)
                        .filter(MathUtils::isPositiveNonZero)
                        .ifPresent(lap::setCalories);
                of(eachLap)
                        .map(ActivityLapT::getDistanceMeters)
                        .filter(MathUtils::isPositiveNonZero)
                        .ifPresent(lap::setDistanceMeters);
                of(eachLap)
                        .map(ActivityLapT::getMaximumSpeed)
                        .filter(MathUtils::isPositiveNonZero)
                        .ifPresent(lap::setMaximumSpeed);
                of(eachLap)
                        .map(ActivityLapT::getMaximumHeartRateBpm)
                        .map(HeartRateInBeatsPerMinuteT::getValue)
                        .map(Integer::new)
                        .filter(MathUtils::isPositiveNonZero)
                        .ifPresent(lap::setMaximumHeartRate);
                of(eachLap)
                        .map(ActivityLapT::getStartTime)
                        .flatMap(DateUtils::toLocalDateTime)
                        .ifPresent(lap::setStartTime);
                of(indexLap)
                        .map(AtomicInteger::incrementAndGet)
                        .ifPresent(lap::setIndex);
                of(eachLap)
                        .map(ActivityLapT::getTotalTimeSeconds)
                        .filter(MathUtils::isPositiveNonZero)
                        .ifPresent(lap::setTotalTimeSeconds);
                of(eachLap)
                        .map(ActivityLapT::getIntensity)
                        .map(IntensityT::value)
                        .ifPresent(lap::setIntensity);
                of(eachLap)
                        .map(ActivityLapT::getExtensions)
                        .ifPresent(extensionsT -> setExtensions(extensionsT, lap));
                of(eachLap)
                        .map(ActivityLapT::getTrack)
                        .ifPresent(trackTS -> trackTS.stream()
                                .forEach(trackT -> trackT.getTrackpoint().stream().forEach(trackPointT ->
                                        of(trackPointT)
                                                .map(TrackpointT::getPosition)
                                                .flatMap(positionT -> of(indexTrackPoint)
                                                        .map(AtomicInteger::incrementAndGet)
                                                        .map(indexTrackPointParam ->
                                                                toTrackPoint(trackPointT, indexTrackPointParam))
                                                        .map(trackPoint -> {
                                                            setExtensions(trackPointT, trackPoint);
                                                            return trackPoint;
                                                        }))
                                                .ifPresent(lap::addTrack))));
                // Calculate values not informed of a lap.
                lapsOperations.calculateLapValues(lap);
                activity.addLap(lap);
            });
            activityOperations.calculateDistanceSpeedValues(activity);
            activities.add(activity);
        });
        return activities;
    }

    private List<Activity> toActivities(CourseListT courseListT) {
        List<Activity> activities = Lists.newArrayList();
        courseListT.getCourse().forEach(course -> {
            Activity activity = new Activity();
            AtomicInteger indexLap = new AtomicInteger();
            AtomicInteger indexTrackPoint = new AtomicInteger();
            activity.setSourceXmlType(SOURCE_TCX_XML);
            of(course)
                    .map(CourseT::getName)
                    .ifPresent(activity::setName);
            course.getLap().forEach(eachLap -> {
                Lap lap = new Lap();
                of(eachLap)
                        .map(CourseLapT::getAverageHeartRateBpm)
                        .map(HeartRateInBeatsPerMinuteT::getValue)
                        .map(Double::new)
                        .filter(MathUtils::isPositiveNonZero)
                        .ifPresent(lap::setAverageHearRate);
                of(eachLap)
                        .map(CourseLapT::getTotalTimeSeconds)
                        .ifPresent(lap::setTotalTimeSeconds);
                of(eachLap)
                        .map(CourseLapT::getDistanceMeters)
                        .ifPresent(lap::setDistanceMeters);
                of(indexLap)
                        .map(AtomicInteger::getAndIncrement)
                        .ifPresent(lap::setIndex);
                of(eachLap)
                        .map(CourseLapT::getExtensions)
                        .ifPresent(extensionsT -> setExtensions(extensionsT, lap));
                Position initial = toPosition(eachLap.getBeginPosition().getLatitudeDegrees(),
                        eachLap.getBeginPosition().getLongitudeDegrees());
                Position end = toPosition(eachLap.getEndPosition().getLatitudeDegrees(),
                        eachLap.getEndPosition().getLongitudeDegrees());
                AtomicInteger eachIndex = new AtomicInteger();
                AtomicInteger indexStart = new AtomicInteger();
                AtomicInteger indexEnd = new AtomicInteger();

                course.getTrack().forEach(track ->
                    track.getTrackpoint().forEach(trackPoint -> {
                        of(trackPoint)
                                .map(TrackpointT::getPosition)
                                .filter(positionT -> getIsThisPosition.apply(positionT).test(initial))
                                .ifPresent(positionT -> of(eachIndex).map(AtomicInteger::get).ifPresent(indexStart::set));
                        of(trackPoint)
                                .map(TrackpointT::getPosition)
                                .filter(positionT -> getIsThisPosition.apply(positionT).test(end))
                                .ifPresent(positionT -> of(eachIndex).map(AtomicInteger::get).ifPresent(indexEnd::set));
                        eachIndex.incrementAndGet();

                    }));
                IntStream.range(indexStart.get(), MathUtils.increaseUnit(indexEnd.get()))
                        .forEach(index -> of(course)
                            .map(CourseT::getTrack)
                            .map(CommonUtils::getFirstElement)
                            .map(TrackT::getTrackpoint)
                            .map(trackPointTs -> trackPointTs.get(index))
                            .flatMap(trT -> of(indexTrackPoint)
                                    .map(AtomicInteger::getAndIncrement)
                                    .map(indexTrackPointParam -> toTrackPoint(trT, indexTrackPointParam))
                                    .map(trackPoint -> {
                                        setExtensions(trT, trackPoint);
                                        return trackPoint;
                                    }))
                            .ifPresent(lap::addTrack));
                // Calculate values not informed of a lap.
                lapsOperations.calculateLapValues(lap);
                activity.addLap(lap);
            });
            activityOperations.calculateDistanceSpeedValues(activity);
            activities.add(activity);
        });
        return activities;
    }

    private void setExtensions(ExtensionsT xmlExtensionsLap, Lap modelLap) {
        of(xmlExtensionsLap)
                .map(ExtensionsT::getAny)
                .ifPresent(extensions -> extensions.stream()
                        .filter(JAXBElement.class::isInstance)
                        .map(JAXBElement.class::cast)
                        .map(JAXBElement::getValue)
                        .filter(ActivityLapExtensionT.class::isInstance)
                        .map(ActivityLapExtensionT.class::cast)
                        .map(ActivityLapExtensionT::getAvgSpeed)
                        .filter(Objects::nonNull)
                        .findFirst()
                        .ifPresent(modelLap::setAverageSpeed));
    }

    private void setExtensions(TrackpointT xmlTrackPoint, TrackPoint modelTrackPoint) {
        ofNullable(xmlTrackPoint)
                .map(TrackpointT::getExtensions)
                .map(ExtensionsT::getAny)
                .ifPresent(extension -> extension.stream()
                        .filter(Objects::nonNull)
                        .filter(JAXBElement.class::isInstance)
                        .map(JAXBElement.class::cast)
                        .map(JAXBElement::getValue)
                        .filter(ActivityTrackpointExtensionT.class::isInstance)
                        .map(ActivityTrackpointExtensionT.class::cast)
                        .map(ActivityTrackpointExtensionT::getSpeed)
                        .filter(Objects::nonNull)
                        .map(MathUtils::toBigDecimal)
                        .findFirst()
                        .ifPresent(speed -> modelTrackPoint.setSpeed(speed))
                );
    }
}
