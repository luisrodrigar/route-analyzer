package com.routeanalyzer.api.logic.file.upload.impl;

import com.routeanalyzer.api.common.CommonUtils;
import com.routeanalyzer.api.common.DateUtils;
import com.routeanalyzer.api.logic.ActivityOperations;
import com.routeanalyzer.api.logic.LapsOperations;
import com.routeanalyzer.api.logic.file.upload.UploadFileService;
import com.routeanalyzer.api.model.Activity;
import com.routeanalyzer.api.model.Lap;
import com.routeanalyzer.api.services.reader.GPXService;
import com.routeanalyzer.api.xml.gpx11.ExtensionsType;
import com.routeanalyzer.api.xml.gpx11.GpxType;
import com.routeanalyzer.api.xml.gpx11.TrkType;
import com.routeanalyzer.api.xml.gpx11.TrksegType;
import com.routeanalyzer.api.xml.gpx11.WptType;
import com.routeanalyzer.api.xml.gpx11.trackpointextension.garmin.TrackPointExtensionT;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.xml.bind.JAXBElement;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static com.routeanalyzer.api.common.CommonUtils.toTrackPoint;
import static com.routeanalyzer.api.common.Constants.SOURCE_GPX_XML;
import static java.util.Objects.nonNull;
import static java.util.Optional.of;
import static java.util.Optional.ofNullable;

@Service
public class GpxUploadFileService extends UploadFileService<GpxType> {

    @Autowired
    public GpxUploadFileService(GPXService gpxService, ActivityOperations activityOperationsImpl,
                                LapsOperations lapsOperationsImpl) {
        super(gpxService, activityOperationsImpl, lapsOperationsImpl);
    }

    /**
     * Get these Activity model list with the information contained in the xml file
     * @param gpx contains if it exists the info about the gpx route
     * @return a list with the activities of the xml document (gpx).
     */
    @Override
    public List<Activity> toListActivities(GpxType gpx) {
        return ofNullable(gpx)
                .filter(__ -> nonNull(gpx.getTrk()))
                .map(this::toDataList)
                .orElseGet(Collections::emptyList);
    }

    private List<Activity> toDataList(GpxType gpxType) {
        AtomicInteger indexLap = new AtomicInteger();
        AtomicInteger indexTrackPoint = new AtomicInteger();
        return gpxType.getTrk().stream().map(track -> {
            Optional<TrkType> optTrkType = ofNullable(track);
            Activity activity = new Activity();
            // Source xml type, in this case gpx.
            activity.setSourceXmlType(SOURCE_GPX_XML);
            // Set the the date
            ofNullable(gpxType.getMetadata())
                    .ifPresent(metadataType -> ofNullable(metadataType.getTime())
                            .map(xmlGregorianCalendar -> of(indexLap)
                                    .map(AtomicInteger::get)
                                    .map(indexLapParam -> xmlGregorianCalendar)
                                    .orElseGet(() -> optTrkType.map(TrkType::getTrkseg)
                                            .map(CommonUtils::getFirstElement)
                                            .map(TrksegType::getTrkpt)
                                            .map(CommonUtils::getFirstElement)
                                            .map(WptType::getTime)
                                            .orElse(null)))
                            .flatMap(DateUtils::toZonedDateTime)
                            .ifPresent(activity::setDate));
            // Device
            ofNullable(gpxType.getCreator()).ifPresent(activity::setDevice);
            // Name
            optTrkType.map(TrkType::getName).map(String::trim).ifPresent(activity::setName);
            optTrkType.map(TrkType::getTrkseg)
                    .ifPresent(trkSegTypes ->
                            trkSegTypes.forEach(trkSegType -> {
                                Lap lap = Lap.builder().build();
                                // Lap start time
                                ofNullable(trkSegType).map(TrksegType::getTrkpt)
                                        .map(CommonUtils::getFirstElement)
                                        .map(WptType::getTime)
                                        .flatMap(DateUtils::toZonedDateTime)
                                        .ifPresent(lap::setStartTime);
                                // Index
                                of(indexLap).map(AtomicInteger::incrementAndGet)
                                        .ifPresent(lap::setIndex);
                                // Set all the track point in lap object
                                setTrackPoints(lap, trkSegType, indexTrackPoint);
                                // calculating lap values
                                lapsOperationsService.calculateLapValues(lap);
                                // adding lap
                                activity.addLap(lap);
                            }));
            // calculating distance speed values
            activityOperationsService.calculateDistanceSpeedValues(activity);
            // adding activity
            return activity;
        }).collect(Collectors.toList());
    }

    private void setTrackPoints(Lap lap, TrksegType trkSetType, AtomicInteger indexTrackPoint) {
        ofNullable(trkSetType)
                .map(TrksegType::getTrkpt)
                .ifPresent(trkPtList -> trkPtList.forEach(eachTrackPoint -> ofNullable(eachTrackPoint)
                        .ifPresent(wptType -> CommonUtils.toPosition(wptType.getLat(), wptType.getLon())
                            .flatMap(position -> ofNullable(eachTrackPoint.getTime())
                                    .flatMap(DateUtils::toZonedDateTime)
                                    .flatMap(time -> ofNullable(eachTrackPoint.getEle())
                                            .map(String::valueOf)
                                            .flatMap(elevation -> ofNullable(indexTrackPoint)
                                                    .map(AtomicInteger::incrementAndGet)
                                                    .map(indexTrackPointParam ->
                                                            toTrackPoint(time, indexTrackPointParam, position,
                                                                    elevation, null, null, null))
                                                    .flatMap(trackPoint -> ofNullable(eachTrackPoint.getExtensions())
                                                            .map(ExtensionsType::getAny)
                                                            .flatMap(anyList -> anyList.stream()
                                                                    .filter(JAXBElement.class::isInstance)
                                                                    .map(JAXBElement.class::cast)
                                                                    .map(JAXBElement::getValue)
                                                                    .filter(TrackPointExtensionT.class::isInstance)
                                                                    .map(TrackPointExtensionT.class::cast)
                                                                    .map(TrackPointExtensionT::getHr)
                                                                    .map(Short::intValue).map(hr -> {
                                                                        trackPoint.setHeartRateBpm(hr);
                                                                        return trackPoint;
                                                                    })
                                                                    .findFirst())))))
                                .ifPresent(lap::addTrack))));
    }
}
