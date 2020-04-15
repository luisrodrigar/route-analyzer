package com.routeanalyzer.api.logic.file.upload.impl;

import com.routeanalyzer.api.common.DateUtils;
import com.routeanalyzer.api.logic.ActivityOperations;
import com.routeanalyzer.api.logic.LapsOperations;
import com.routeanalyzer.api.logic.TrackPointOperations;
import com.routeanalyzer.api.logic.file.upload.UploadFileService;
import com.routeanalyzer.api.model.Activity;
import com.routeanalyzer.api.model.Lap;
import com.routeanalyzer.api.model.TrackPoint;
import com.routeanalyzer.api.services.reader.GPXService;
import com.routeanalyzer.api.xml.gpx11.ExtensionsType;
import com.routeanalyzer.api.xml.gpx11.GpxType;
import com.routeanalyzer.api.xml.gpx11.MetadataType;
import com.routeanalyzer.api.xml.gpx11.TrkType;
import com.routeanalyzer.api.xml.gpx11.TrksegType;
import com.routeanalyzer.api.xml.gpx11.WptType;
import com.routeanalyzer.api.xml.gpx11.trackpointextension.garmin.TrackPointExtensionT;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

import static com.routeanalyzer.api.common.CommonUtils.not;
import static com.routeanalyzer.api.common.Constants.SOURCE_GPX_XML;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toList;

@Service
public class GpxUploadFileService extends UploadFileService<GpxType> {

    @Autowired
    public GpxUploadFileService(final GPXService gpxService, final ActivityOperations activityOperationsImpl,
                                final LapsOperations lapsOperationsImpl, final TrackPointOperations trackPointOperations) {
        super(gpxService, activityOperationsImpl, lapsOperationsImpl, trackPointOperations);
    }

    /**
     * Get these Activity model list with the information contained in the xml file
     * @param gpxType contains if it exists the info about the gpx route
     * @return a list with the activities of the xml document (gpx).
     */
    @Override
    public List<Activity> toListActivities(final GpxType gpxType) {
        return ofNullable(gpxType)
                .map(GpxType::getTrk)
                .map(trackLapsList -> trackLapsList.stream()
                        .map(trackLap -> toActivity(gpxType.getMetadata(), gpxType.getCreator(), trackLap))
                        .map(activity -> {
                            // calculating distance speed values
                            activityOperationsService.calculateDistanceSpeedValues(activity);
                            return activity;
                        })
                        .collect(toList()))
                .orElseGet(Collections::emptyList);
    }

    private Activity toActivity(final MetadataType metadataType, final String creator, final TrkType trkType) {
        AtomicInteger indexLap = new AtomicInteger(0);
        AtomicInteger indexTrackPoint = new AtomicInteger(0);
        return Activity.builder()
                // Source xml type, in this case gpx.
                .sourceXmlType(SOURCE_GPX_XML)
                // Set the the date
                .date(getMetadataDate(metadataType, trkType))
                // Device
                .device(creator)
                // Name
                .name(ofNullable(trkType)
                        .map(TrkType::getName)
                        .map(String::trim)
                        .orElse(null))
                // Laps
                .laps(getLaps(trkType.getTrkseg(), indexLap, indexTrackPoint))
                .build();
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
                .map(trkSegTypeList -> trkSegTypeList.get(0))
                .map(TrksegType::getTrkpt)
                .map(wptTypeList -> wptTypeList.get(0))
                .map(WptType::getTime)
                .flatMap(DateUtils::toZonedDateTime)
                .orElse(null);
    }

    private List<Lap> getLaps(final List<TrksegType> trksegTypeList, final AtomicInteger indexLaps,
                              final AtomicInteger indexTrackPoints) {
        return trksegTypeList
                .stream()
                .map(trksegType -> toLap(trksegType, indexLaps, indexTrackPoints))
                .map(lap -> {
                    // calculating lap values
                    lapsOperationsService.calculateLapValues(lap);
                    return lap;
                })
                .collect(toList());
    }

    private Lap toLap(final TrksegType trksegType, final AtomicInteger indexLaps,
                                final AtomicInteger indexTrackPoints) {
        return Lap.builder()
                .startTime(getStartDateTimeLap(trksegType))
                .index(indexLaps.incrementAndGet())
                .tracks(getTrackPoints(trksegType, indexTrackPoints))
                .build();
    }

    private ZonedDateTime getStartDateTimeLap(final TrksegType trksegType) {
        return ofNullable(trksegType)
                .map(TrksegType::getTrkpt)
                .map(wptTypeList -> wptTypeList.get(0))
                .map(WptType::getTime)
                .flatMap(DateUtils::toZonedDateTime)
                .orElse(null);
    }

    private List<TrackPoint> getTrackPoints(final TrksegType trkSetType, final AtomicInteger indexTrackPoints) {
        return trkSetType.getTrkpt()
                .stream()
                .map(wptType -> trackPointOperations.toTrackPoint(wptType, indexTrackPoints.incrementAndGet())
                        .map(trackPoint -> addExtensions(wptType, trackPoint)))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(toList());
    }

    private TrackPoint addExtensions(final WptType wptType, final TrackPoint trackPoint) {
        return ofNullable(wptType)
                .map(WptType::getExtensions)
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
        return ofNullable(extensions)
                .map(this::toJAXBElementExtensionsValue)
                .filter(not(List::isEmpty))
                .map(this::getHeartRateValue)
                .orElseGet(() -> getHeartRateValue(extensions));
    }

    private Optional<Integer> getHeartRateValue(final List<Object> extensions) {
        return extensions.stream()
                .filter(TrackPointExtensionT.class::isInstance)
                .map(TrackPointExtensionT.class::cast)
                .map(TrackPointExtensionT::getHr)
                .map(Short::intValue)
                .findFirst();
    }

}
