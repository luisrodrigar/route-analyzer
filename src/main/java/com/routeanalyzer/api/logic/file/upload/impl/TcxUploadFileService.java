package com.routeanalyzer.api.logic.file.upload.impl;

import com.routeanalyzer.api.common.DateUtils;
import com.routeanalyzer.api.common.MathUtils;
import com.routeanalyzer.api.logic.ActivityOperations;
import com.routeanalyzer.api.logic.LapsOperations;
import com.routeanalyzer.api.logic.TrackPointOperations;
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

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.IntStream;

import static com.routeanalyzer.api.common.CommonUtils.not;
import static com.routeanalyzer.api.common.Constants.SOURCE_TCX_XML;
import static java.util.Optional.of;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toList;

@Service
public class TcxUploadFileService extends UploadFileService<TrainingCenterDatabaseT> {

    @Autowired
    public TcxUploadFileService(final TCXService tcxService, final ActivityOperations activityOperationsImpl,
                                final LapsOperations lapsOperationsImpl, final TrackPointOperations trackPointOperations) {
        super(tcxService, activityOperationsImpl, lapsOperationsImpl, trackPointOperations);
    }

    private Function<PositionT, Predicate<Position>> getIsThisPosition = positionT -> positionToCompare ->
            positionT.getLatitudeDegrees() == positionToCompare.getLatitudeDegrees().doubleValue()
                    && positionT.getLongitudeDegrees() == positionToCompare.getLongitudeDegrees().doubleValue();

    /**
     * Get the list of activities
     * @param tcxType: class which represents the info inside of a xml document (tcx)
     * @return list of activities which contains the xml document.
     */
    @Override
    public List<Activity> toListActivities(final TrainingCenterDatabaseT tcxType) {
        return ofNullable(tcxType)
                .map(TrainingCenterDatabaseT::getActivities)
                .map(this::toActivities)
                .orElseGet(() -> ofNullable(tcxType)
                        .map(TrainingCenterDatabaseT::getCourses)
                        .map(this::toActivities)
                        .orElseGet(Collections::emptyList));

    }

    private List<Activity> toActivities(final ActivityListT activityListT) {
        return activityListT.getActivity()
                .stream()
                .map(this::toActivity)
                .map(activity -> {
                    activityOperationsService.calculateDistanceSpeedValues(activity);
                    return activity;
                })
                .collect(toList());
    }

    private Activity toActivity(final ActivityT activityT) {
        AtomicInteger indexLap = new AtomicInteger(1);
        AtomicInteger indexTrackPoint = new AtomicInteger(1);
        return Activity.builder()
                .sourceXmlType(SOURCE_TCX_XML)
                .device(of(activityT)
                        .map(ActivityT::getCreator)
                        .map(AbstractSourceT::getName)
                        .orElse(null))
                .date(of(activityT)
                        .map(ActivityT::getId)
                        .flatMap(DateUtils::toZonedDateTime)
                        .orElse(null))
                .sport(of(activityT)
                        .map(ActivityT::getSport)
                        .map(SportT::value)
                        .orElse(null))
                .laps(toLapFromActivityLapTList(activityT.getLap(), indexLap, indexTrackPoint))
                .build();
    }

    private List<Lap> toLapFromActivityLapTList(final List<ActivityLapT> activityLapTS, final AtomicInteger indexLaps,
                                final AtomicInteger indexTrackPoints) {
        return activityLapTS.stream()
                .map(activityLapT -> toLap(activityLapT, indexLaps, indexTrackPoints))
                .map(lap -> {
                    // Calculate values not informed of a lap.
                    lapsOperationsService.calculateLapValues(lap);
                    return lap;
                })
                .collect(toList());
    }

    private Lap toLap(final ActivityLapT activityLapT, final AtomicInteger indexLaps,
                      final AtomicInteger indexTrackPoints) {
        return Lap.builder()
                .averageHearRate(of(activityLapT)
                        .map(ActivityLapT::getAverageHeartRateBpm)
                        .map(HeartRateInBeatsPerMinuteT::getValue)
                        .map(Double::valueOf)
                        .filter(MathUtils::isPositiveNonZero)
                        .orElse(null))
                .calories(of(activityLapT)
                        .map(ActivityLapT::getCalories)
                        .filter(MathUtils::isPositiveNonZero)
                        .orElse(null))
                .distanceMeters(of(activityLapT)
                        .map(ActivityLapT::getDistanceMeters)
                        .filter(MathUtils::isPositiveNonZero)
                        .orElse(null))
                .maximumSpeed(of(activityLapT)
                        .map(ActivityLapT::getMaximumSpeed)
                        .filter(MathUtils::isPositiveNonZero)
                        .orElse(null))
                .maximumHeartRate(of(activityLapT)
                        .map(ActivityLapT::getMaximumHeartRateBpm)
                        .map(HeartRateInBeatsPerMinuteT::getValue)
                        .map(Integer::valueOf)
                        .filter(MathUtils::isPositiveNonZero)
                        .orElse(null))
                .startTime(of(activityLapT)
                        .map(ActivityLapT::getStartTime)
                        .flatMap(DateUtils::toZonedDateTime)
                        .orElse(null))
                .index(indexLaps.getAndIncrement())
                .totalTimeSeconds(of(activityLapT)
                        .map(ActivityLapT::getTotalTimeSeconds)
                        .filter(MathUtils::isPositiveNonZero)
                        .orElse(null))
                .intensity(of(activityLapT)
                        .map(ActivityLapT::getIntensity)
                        .map(IntensityT::value)
                        .orElse(null))
                .tracks(getTrackPointsOfLap(activityLapT.getTrack(), indexTrackPoints))
                .averageSpeed(getAverageSpeedExtensionValue(activityLapT.getExtensions())
                        .orElse(null))
                .build();
    }

    private List<TrackPoint> getTrackPointsOfLap(final List<TrackT> trackTList, final AtomicInteger indexTrackPoints) {
        return trackTList.stream()
                .flatMap(trackT -> trackT.getTrackpoint()
                        .stream()
                        .map(trackPointT -> toTrackPointModel(trackPointT, indexTrackPoints)))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(toList());
    }

    private Optional<TrackPoint> toTrackPointModel(final TrackpointT trackpointT, final AtomicInteger indexTrackPoints) {
        return of(trackpointT)
                .map(TrackpointT::getPosition)
                .map(positionT -> trackPointOperations.toTrackPoint(trackpointT, indexTrackPoints.getAndIncrement()))
                .map(trackPoint -> setTrackPointExtensions(trackpointT, trackPoint));
    }

    private List<Activity> toActivities(final CourseListT courseListT) {
        return courseListT.getCourse()
                .stream()
                .map(this::toActivity)
                .map(activity -> {
                    // Calculate values not informed of a lap.
                    activityOperationsService.calculateDistanceSpeedValues(activity);
                    return activity;
                })
                .collect(toList());
    }

    private Activity toActivity(final CourseT courseT) {
        AtomicInteger indexLap = new AtomicInteger(0);
        AtomicInteger indexTrackPoint = new AtomicInteger(0);
        return Activity.builder()
                .sourceXmlType(SOURCE_TCX_XML)
                .name(courseT.getName())
                .laps(toLapList(courseT.getLap(), courseT.getTrack(), indexLap, indexTrackPoint))
                .build();
    }

    private List<Lap> toLapList(final List<CourseLapT> courseLapTList, final List<TrackT> trackTList,
                                final AtomicInteger indexLap, final AtomicInteger indexTrackPoints) {
        return courseLapTList
                .stream()
                .map(courseLapT -> toLap(courseLapT, trackTList, indexLap, indexTrackPoints))
                .map(lap -> {
                    lapsOperationsService.calculateLapValues(lap);
                    return lap;
                })
                .collect(toList());
    }

    private Lap toLap(final CourseLapT courseLapT, final List<TrackT> trackTList, final AtomicInteger indexLap,
                      final AtomicInteger indexTrackPoints) {
        return Lap.builder()
                .averageHearRate(of(courseLapT)
                        .map(CourseLapT::getAverageHeartRateBpm)
                        .map(HeartRateInBeatsPerMinuteT::getValue)
                        .map(Double::valueOf)
                        .filter(MathUtils::isPositiveNonZero)
                        .orElse(null))
                .totalTimeSeconds(courseLapT.getTotalTimeSeconds())
                .distanceMeters(courseLapT.getDistanceMeters())
                .index(indexLap.incrementAndGet())
                .averageSpeed(getAverageSpeedExtensionValue(courseLapT.getExtensions()).orElse(null))
                .tracks(getTrackPointsOfLap(courseLapT, trackTList, indexTrackPoints))
                .build();
    }

    private List<TrackPoint> getTrackPointsOfLap(final CourseLapT courseLapT, final List<TrackT> trackTList,
                                                 final AtomicInteger indexTrackPoints) {
        AtomicInteger eachIndex = new AtomicInteger();
        AtomicInteger indexStart = new AtomicInteger();
        AtomicInteger indexEnd = new AtomicInteger();
        Position initial = Position.builder()
                .latitudeDegrees(new BigDecimal(courseLapT.getBeginPosition().getLatitudeDegrees()))
                .longitudeDegrees(new BigDecimal(courseLapT.getBeginPosition().getLongitudeDegrees()))
                .build();
        Position end = Position.builder()
                .latitudeDegrees(new BigDecimal(courseLapT.getEndPosition().getLatitudeDegrees()))
                .longitudeDegrees(new BigDecimal(courseLapT.getEndPosition().getLongitudeDegrees()))
                .build();
        trackTList.forEach(track ->
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
        return IntStream.range(indexStart.get(), MathUtils.increaseUnit(indexEnd.get()))
                .mapToObj(index -> ofNullable(trackTList)
                        .map(trackTs -> trackTs.get(0))
                        .map(TrackT::getTrackpoint)
                        .map(trackPointTs -> trackPointTs.get(index))
                        .flatMap(trT -> of(indexTrackPoints)
                                .map(AtomicInteger::getAndIncrement)
                                .map(indexTrackPointParam -> trackPointOperations.toTrackPoint(trT, indexTrackPointParam))
                                .map(trackPoint -> setSpeedExtensionGetTrackPoint(trT.getExtensions(), trackPoint))))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(toList());
    }

    private TrackPoint setSpeedExtensionGetTrackPoint(final ExtensionsT extensionsT, final TrackPoint trackPoint) {
        getSpeedExtensionValue(extensionsT.getAny())
                .ifPresent(trackPoint::setSpeed);
        return trackPoint;
    }

    private Optional<Double> getAverageSpeedExtensionValue(final ExtensionsT extensionsT) {
        return ofNullable(toJAXBElementExtensionsValue(extensionsT.getAny()))
                .filter(not(List::isEmpty))
                .map(this::getAvgSpeedValue)
                .orElseGet(() -> getAvgSpeedValue(extensionsT.getAny()));
    }

    private Optional<Double> getAvgSpeedValue(final List<Object> extensions) {
        return extensions.stream()
                .filter(ActivityLapExtensionT.class::isInstance)
                .map(ActivityLapExtensionT.class::cast)
                .map(ActivityLapExtensionT::getAvgSpeed)
                .filter(Objects::nonNull)
                .findFirst();
    }

    private TrackPoint setTrackPointExtensions(final TrackpointT xmlTrackPoint, final TrackPoint trackPoint) {
        return ofNullable(xmlTrackPoint)
                .map(TrackpointT::getExtensions)
                .map(ExtensionsT::getAny)
                .map(extensions -> addSpeedToTrackPoint(extensions, trackPoint))
                .orElse(trackPoint);
    }

    private TrackPoint addSpeedToTrackPoint(final List<Object> extensions, final TrackPoint trackPoint) {
        getSpeedExtensionValue(extensions)
                .ifPresent(trackPoint::setSpeed);
        return trackPoint;
    }

    private Optional<BigDecimal> getSpeedExtensionValue(final List<Object> extensions) {
        return ofNullable(toJAXBElementExtensionsValue(extensions))
                .filter(not(List::isEmpty))
                .map(this::getSpeedValue)
                .orElseGet(() -> getSpeedValue(extensions));
    }

    private Optional<BigDecimal> getSpeedValue(final List<Object> extensions) {
        return extensions.stream()
                .filter(ActivityTrackpointExtensionT.class::isInstance)
                .map(ActivityTrackpointExtensionT.class::cast)
                .map(ActivityTrackpointExtensionT::getSpeed)
                .filter(Objects::nonNull)
                .map(MathUtils::toBigDecimal)
                .findFirst();
    }

}
