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
import com.routeanalyzer.api.xml.gpx11.MetadataType;
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
import static java.util.Optional.ofNullable;

@Service
public class GpxUploadFileService extends UploadFileService<GpxType> {

    private ActivityOperations activityOperations;
    private LapsOperations lapsOperations;

    @Autowired
    public GpxUploadFileService(GPXService gpxService, ActivityOperations activityOperations,
                                LapsOperations lapsOperations) {
        super(gpxService);
        this.activityOperations = activityOperations;
        this.lapsOperations = lapsOperations;
    }

    /**
     * Get these Activity model list with the information contained in the xml file
     * @param optGpx contains if it exists the info about the gpx route
     * @return
     */
    @Override
    protected List<Activity> toListActivities(Optional<GpxType> optGpx) {
        AtomicInteger indexLap = new AtomicInteger();
        AtomicInteger indexTrackPoint = new AtomicInteger();
        return optGpx.map(GpxType::getTrk).map(tracks ->
                tracks.stream().map(track -> {
                    Optional<TrkType> optTrkType = ofNullable(track);
                    Activity activity = new Activity();
                    // Source xml type, in this case gpx.
                    activity.setSourceXmlType(SOURCE_GPX_XML);
                    // Set the the date
                    optGpx.ifPresent(gpxParam -> optGpx
                            .map(GpxType::getMetadata)
                            .map(MetadataType::getTime)
                            .map(xmlGregorianCalendar -> ofNullable(indexLap)
                                    .map(AtomicInteger::get)
                                    .map(indexLapParam -> xmlGregorianCalendar)
                                    .orElseGet(() -> optTrkType.map(TrkType::getTrkseg)
                                            .map(CommonUtils::getFirstElement)
                                            .map(TrksegType::getTrkpt)
                                            .map(CommonUtils::getFirstElement)
                                            .map(WptType::getTime).orElse(null)))
                            .flatMap(DateUtils::toLocalDateTime)
                            .ifPresent(activity::setDate));
                    // Device
                    optGpx.map(GpxType::getCreator).ifPresent(activity::setDevice);
                    // Name
                    optTrkType.map(TrkType::getName).map(String::trim).ifPresent(activity::setName);
                    optTrkType.map(TrkType::getTrkseg).ifPresent(trkSegTypes ->
                        trkSegTypes.forEach(trkSegType -> {
                            Lap lap = Lap.builder().build();
                            Optional<TrksegType> optTrkSegType = ofNullable(trkSegType);
                            // Lap start time
                            optTrkSegType.map(TrksegType::getTrkpt)
                                    .map(CommonUtils::getFirstElement)
                                    .map(WptType::getTime)
                                    .flatMap(DateUtils::toLocalDateTime)
                                    .ifPresent(lap::setStartTime);
                            // Index
                            ofNullable(indexLap).map(AtomicInteger::incrementAndGet).ifPresent(lap::setIndex);
                            // Set all the track point in lap object
                            setTrackPoints(lap, optTrkSegType, indexTrackPoint);
                            // calculating lap values
                            lapsOperations.calculateLapValues(lap);
                            // adding lap
                            activity.addLap(lap);
                        }));
                    // calculating distance speed values
                    activityOperations.calculateDistanceSpeedValues(activity);
                    // adding activity
                    return activity;
                }).collect(Collectors.toList())
        ).orElseGet(Collections::emptyList);
    }

    private void setTrackPoints(Lap lap, Optional<TrksegType> optTrkSetType, AtomicInteger indexTrackPoint) {
        optTrkSetType.map(TrksegType::getTrkpt)
                .ifPresent(trkPtList -> trkPtList.forEach(eachTrackPoint -> {
                    Optional<WptType> optWptType = ofNullable(eachTrackPoint);
                    optWptType.map(WptType::getLat)
                            .ifPresent(latitude -> optWptType
                                    .map(WptType::getLon)
                                    .map(longitude -> CommonUtils.toPosition(latitude, longitude))
                                    .flatMap(position -> optWptType.map(WptType::getTime)
                                            .flatMap(DateUtils::toLocalDateTime)
                                            .flatMap(time -> optWptType.map(WptType::getEle)
                                                    .map(String::valueOf)
                                                    .flatMap(elevation -> ofNullable(indexTrackPoint)
                                                            .map(AtomicInteger::incrementAndGet)
                                                            .map(indexTrackPointParam ->
                                                                    toTrackPoint(time, indexTrackPointParam, position,
                                                                            elevation, null, null, null))
                                                            .flatMap(trackPoint -> optWptType
                                                                    .map(WptType::getExtensions)
                                                                    .flatMap(extensionsType -> ofNullable(extensionsType)
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
                                                                                    .findFirst()
                                                                            )
                                                                    )
                                                            )
                                                    )
                                            ))
                                    .ifPresent(lap::addTrack));
                }));
    }
}
