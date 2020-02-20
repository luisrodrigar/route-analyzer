package com.routeanalyzer.api.logic.file.upload.impl;

import com.routeanalyzer.api.common.CommonUtils;
import com.routeanalyzer.api.common.DateUtils;
import com.routeanalyzer.api.logic.ActivityOperations;
import com.routeanalyzer.api.logic.LapsOperations;
import com.routeanalyzer.api.logic.file.upload.UploadFileService;
import com.routeanalyzer.api.model.Activity;
import com.routeanalyzer.api.model.Lap;
import com.routeanalyzer.api.model.TrackPoint;
import com.routeanalyzer.api.services.reader.GPXService;
import com.routeanalyzer.api.xml.gpx11.*;
import com.routeanalyzer.api.xml.gpx11.trackpointextension.garmin.TrackPointExtensionT;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

import static com.routeanalyzer.api.common.CommonUtils.toPosition;
import static com.routeanalyzer.api.common.CommonUtils.toTrackPoint;
import static com.routeanalyzer.api.common.Constants.SOURCE_GPX_XML;
import static com.routeanalyzer.api.common.DateUtils.toZonedDateTime;
import static java.util.Objects.nonNull;
import static java.util.Optional.of;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toList;

@Service
public class GpxUploadFileService extends UploadFileService<GpxType> {

    @Autowired
    public GpxUploadFileService(GPXService gpxService, ActivityOperations activityOperationsImpl,
                                LapsOperations lapsOperationsImpl) {
        super(gpxService, activityOperationsImpl, lapsOperationsImpl);
    }

    /**
     * Get these Activity model list with the information contained in the xml file
     * @param gpxType contains if it exists the info about the gpx route
     * @return a list with the activities of the xml document (gpx).
     */
    @Override
    public List<Activity> toListActivities(GpxType gpxType) {
        return ofNullable(gpxType)
                .map(GpxType::getTrk)
                .map(trackLapsList -> trackLapsList.stream()
                        .map(trackLap -> toActivity(gpxType.getMetadata(), gpxType.getCreator(), trackLap))
                        .collect(toList()))
                .orElseGet(Collections::emptyList);
    }

    private Activity toActivity(final MetadataType metadataType, final String creator, final TrkType trkType) {
        AtomicInteger indexLap = new AtomicInteger(0);
        AtomicInteger indexTrackPoint = new AtomicInteger(0);
        Activity activity = new Activity();
        // Source xml type, in this case gpx.
        activity.setSourceXmlType(SOURCE_GPX_XML);
        // Set the the date
        activity.setDate(getMetadataDate(metadataType, trkType));
        // Device
        ofNullable(creator).ifPresent(activity::setDevice);
        // Name
        ofNullable(trkType)
                .map(TrkType::getName)
                .map(String::trim)
                .ifPresent(activity::setName);
        // Laps
        getLaps(trkType.getTrkseg(), indexLap, indexTrackPoint)
                .forEach(activity::addLap);
        // calculating distance speed values
        activityOperationsService.calculateDistanceSpeedValues(activity);
        // adding activity
        return activity;
    }

    private ZonedDateTime getMetadataDate(final MetadataType metadataType, final TrkType trkType) {
        return ofNullable(metadataType)
                .map(MetadataType::getTime)
                .flatMap(DateUtils::toZonedDateTime)
                .orElseGet(getFirstDateTimeTrackPoint(trkType));
    }

    private Supplier<ZonedDateTime> getFirstDateTimeTrackPoint(final TrkType trkType) {
        return () -> ofNullable(trkType)
                .map(TrkType::getTrkseg)
                .map(CommonUtils::getFirstElement)
                .map(TrksegType::getTrkpt)
                .map(CommonUtils::getFirstElement)
                .map(WptType::getTime)
                .flatMap(DateUtils::toZonedDateTime)
                .orElse(null);
    }

    private List<Lap> getLaps(final List<TrksegType> trksegTypeList, final AtomicInteger indexLaps,
                              final AtomicInteger indexTrackPoints) {
        return trksegTypeList
                .stream()
                .map(trksegType -> toLap(trksegType, indexLaps, indexTrackPoints))
                .collect(toList());
    }

    private Lap toLap(final TrksegType trksegType, final AtomicInteger indexLaps,
                                final AtomicInteger indexTrackPoints) {
        Lap lap = Lap.builder()
                .startTime(toZonedDateTime(CommonUtils.getFirstElement(trksegType.getTrkpt()).getTime())
                        .orElse(null))
                .index(indexLaps.incrementAndGet())
                .tracks(getTrackPoints(trksegType, indexTrackPoints))
                .build();
        // calculating lap values
        lapsOperationsService.calculateLapValues(lap);
        return lap;
    }

    private List<TrackPoint> getTrackPoints(final TrksegType trkSetType, final AtomicInteger indexTrackPoints) {
        return trkSetType.getTrkpt()
                .stream()
                .map(eachTrackPoint -> ofNullable(eachTrackPoint)
                        .flatMap(wptType -> createTrackPoint(wptType, indexTrackPoints)))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(toList());
    }

    private Optional<TrackPoint> createTrackPoint(final WptType wptType, final AtomicInteger indexTrackPoints) {
        return toPosition(wptType)
                .filter(__ -> nonNull(wptType.getTime()))
                .flatMap(position -> of(wptType)
                        .map(WptType::getTime)
                        .flatMap(DateUtils::toZonedDateTime)
                        .map(dateTime ->
                                toTrackPoint(dateTime, indexTrackPoints.incrementAndGet(), position, wptType.getEle())))
                .map(trackPoint -> addExtensions(wptType, trackPoint));
    }

    private TrackPoint addExtensions(final WptType wptType, final TrackPoint trackPoint) {
        return ofNullable(wptType.getExtensions())
                .map(ExtensionsType::getAny)
                .map(extensions -> addHeartRate(extensions, trackPoint))
                .orElse(trackPoint);
    }

    private TrackPoint addHeartRate(final List<Object> extensions, final TrackPoint trackPoint) {
        getHeartRateExtensionValue(extensions)
                .ifPresent(trackPoint::setHeartRateBpm);
        return trackPoint;
    }

    private Optional<Integer> getHeartRateExtensionValue(final List<Object> extensions) {
        return extensions.stream()
                .filter(TrackPointExtensionT.class::isInstance)
                .map(TrackPointExtensionT.class::cast)
                .map(TrackPointExtensionT::getHr)
                .map(Short::intValue)
                .findFirst();
    }
}
