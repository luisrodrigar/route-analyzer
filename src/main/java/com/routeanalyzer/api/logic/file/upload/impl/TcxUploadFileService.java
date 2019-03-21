package com.routeanalyzer.api.logic.file.upload.impl;

import com.amazonaws.AmazonClientException;
import com.google.common.collect.Lists;
import com.routeanalyzer.api.common.CommonUtils;
import com.routeanalyzer.api.logic.ActivityUtils;
import com.routeanalyzer.api.logic.LapsUtils;
import com.routeanalyzer.api.logic.file.upload.UploadFileService;
import com.routeanalyzer.api.model.Activity;
import com.routeanalyzer.api.model.Lap;
import com.routeanalyzer.api.model.Position;
import com.routeanalyzer.api.model.TrackPoint;
import com.routeanalyzer.api.services.reader.TCXService;
import com.routeanalyzer.api.xml.tcx.ExtensionsT;
import com.routeanalyzer.api.xml.tcx.TrackpointT;
import com.routeanalyzer.api.xml.tcx.TrainingCenterDatabaseT;
import com.routeanalyzer.api.xml.tcx.activityextension.ActivityLapExtensionT;
import com.routeanalyzer.api.xml.tcx.activityextension.ActivityTrackpointExtensionT;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.xml.sax.SAXParseException;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;

import static com.routeanalyzer.api.common.CommonUtils.toBigDecimal;
import static com.routeanalyzer.api.common.CommonUtils.toLocalDateTime;
import static com.routeanalyzer.api.common.CommonUtils.toPosition;
import static com.routeanalyzer.api.common.CommonUtils.toTrackPoint;
import static java.util.Optional.ofNullable;

@Service
public class TcxUploadFileService implements UploadFileService {

    private TCXService tcxService;
    private ActivityUtils activityUtils;
    private LapsUtils lapsUtils;

    public TcxUploadFileService(TCXService tcxService, ActivityUtils activityUtils, LapsUtils lapsUtils) {
        this.tcxService = tcxService;
        this.activityUtils = activityUtils;
        this.lapsUtils = lapsUtils;
    }

    @Override
    public List<Activity> upload(MultipartFile multiPart)
            throws IOException, AmazonClientException, JAXBException, SAXParseException {
        InputStream inputFileTCX = multiPart.getInputStream();
        // Get the object from xml file (type TCX)
        TrainingCenterDatabaseT tcx = tcxService.readXML(inputFileTCX);
        // Create each activity of the file
        return getListActivitiesFromTCX(tcx);
    }

    /**
     *
     * @param tcx
     * @return
     */
    private List<Activity> getListActivitiesFromTCX(TrainingCenterDatabaseT tcx) {

        List<Activity> activities = Lists.newArrayList();

        AtomicInteger indexLap = new AtomicInteger(), indexTrackPoint = new AtomicInteger();
        if (!Objects.isNull(tcx.getActivities())) {
            tcx.getActivities().getActivity().forEach(eachActivity -> {
                Activity activity = new Activity();
                activity.setSourceXmlType("tcx");
                if (!Objects.isNull(eachActivity.getCreator()))
                    activity.setDevice(eachActivity.getCreator().getName());
                if (!Objects.isNull(eachActivity.getId()))
                    activity.setDate(toLocalDateTime(eachActivity.getId()).orElse(null));
                if (!Objects.isNull(eachActivity.getSport()))
                    activity.setSport(eachActivity.getSport().toString());
                eachActivity.getLap().forEach(eachLap -> {
                    Lap lap = new Lap();
                    if (!Objects.isNull(eachLap.getAverageHeartRateBpm())
                            && eachLap.getAverageHeartRateBpm().getValue() > 0)
                        lap.setAverageHearRate(new Double(eachLap.getAverageHeartRateBpm().getValue()));
                    if (eachLap.getCalories() > 0)
                        lap.setCalories(eachLap.getCalories());
                    if (eachLap.getDistanceMeters() > 0)
                        lap.setDistanceMeters(eachLap.getDistanceMeters());
                    if (!Objects.isNull(eachLap.getMaximumSpeed()) && eachLap.getMaximumSpeed() > 0)
                        lap.setMaximumSpeed(eachLap.getMaximumSpeed());
                    if (!Objects.isNull(eachLap.getMaximumHeartRateBpm())
                            && eachLap.getMaximumHeartRateBpm().getValue() > 0)
                        lap.setMaximumHeartRate(new Integer(eachLap.getMaximumHeartRateBpm().getValue()));
                    if (!Objects.isNull(eachLap.getStartTime()))
                        lap.setStartTime(toLocalDateTime(eachLap.getStartTime()).orElse(null));
                    lap.setIndex(indexLap.incrementAndGet());
                    if (eachLap.getTotalTimeSeconds() > 0.0)
                        lap.setTotalTimeSeconds(eachLap.getTotalTimeSeconds());
                    if (!Objects.isNull(eachLap.getIntensity()))
                        lap.setIntensity(eachLap.getIntensity().toString());
                    if (!Objects.isNull(eachLap.getExtensions()) && !Objects.isNull(eachLap.getExtensions().getAny())
                            && !eachLap.getExtensions().getAny().isEmpty()) {
                        eachLap.getExtensions().getAny().stream()
                                .filter(extension -> (!Objects.isNull(extension)
                                        && !Objects.isNull(JAXBElement.class.cast(extension))
                                        && !Objects.isNull(JAXBElement.class.cast(extension).getValue())
                                        && !Objects.isNull(ActivityLapExtensionT.class
                                        .cast(JAXBElement.class.cast(extension).getValue()))))
                                .forEach(extension -> {
                                    ActivityLapExtensionT actTrackpointExtension = ActivityLapExtensionT.class
                                            .cast(JAXBElement.class.cast(extension).getValue());
                                    lap.setAverageSpeed(actTrackpointExtension.getAvgSpeed());
                                });
                    }
                    eachLap.getTrack().forEach(track -> {
                        track.getTrackpoint().forEach(trackPoint -> {
                            // Adding track point only if position is informed
                            if (!Objects.isNull(trackPoint.getPosition())) {
                                TrackPoint trp = toTrackPoint(trackPoint.getTime(),
                                        indexTrackPoint.incrementAndGet(),trackPoint.getPosition(),
                                        trackPoint.getAltitudeMeters(), trackPoint.getDistanceMeters(),
                                        trackPoint.getHeartRateBpm());
                                setExtensions(trackPoint, trp);
                                lap.addTrack(trp);
                            }
                        });
                    });
                    // Calculate values not informed of a lap.
                    lapsUtils.calculateLapValues(lap);
                    activity.addLap(lap);
                });
                activityUtils.calculateDistanceSpeedValues(activity);
                activities.add(activity);
            });
        } else if (!Objects.isNull(tcx.getCourses())) {
            tcx.getCourses().getCourse().forEach(course -> {
                Activity activity = new Activity();
                activity.setSourceXmlType("tcx");
                activity.setName(course.getName());
                course.getLap().forEach(eachLap -> {
                    Lap lap = new Lap();
                    lap.setAverageHearRate(!Objects.isNull(eachLap.getAverageHeartRateBpm())
                            ? Double.valueOf(eachLap.getAverageHeartRateBpm().getValue()) : null);
                    lap.setTotalTimeSeconds(eachLap.getTotalTimeSeconds());
                    lap.setDistanceMeters(eachLap.getDistanceMeters());
                    lap.setIndex(indexLap.getAndIncrement());
                    Position initial = toPosition(eachLap.getBeginPosition().getLatitudeDegrees(),
                            eachLap.getBeginPosition().getLongitudeDegrees());
                    Position end = Position.builder()
                            .latitudeDegrees(toBigDecimal(eachLap.getEndPosition().getLatitudeDegrees()))
                            .longitudeDegrees(toBigDecimal(eachLap.getEndPosition().getLongitudeDegrees()))
                            .build();
                    AtomicInteger eachIndex = new AtomicInteger();
                    AtomicInteger indexStart = new AtomicInteger();
                    AtomicInteger indexEnd = new AtomicInteger();
                    course.getTrack().forEach(track -> {
                        track.getTrackpoint().forEach(trackpoint -> {
                            if (trackpoint.getPosition().getLatitudeDegrees() == initial.getLatitudeDegrees()
                                    .doubleValue()
                                    && trackpoint.getPosition().getLongitudeDegrees() == initial.getLongitudeDegrees()
                                    .doubleValue())
                                indexStart.set(eachIndex.get());
                            else if (trackpoint.getPosition().getLatitudeDegrees() == end.getLatitudeDegrees()
                                    .doubleValue()
                                    && trackpoint.getPosition().getLongitudeDegrees() == end.getLongitudeDegrees()
                                    .doubleValue())
                                indexEnd.set(eachIndex.get());
                            eachIndex.incrementAndGet();

                        });
                    });
                    IntStream.range(indexStart.get(), indexEnd.get() + 1).forEach(index -> {
                        TrackpointT trT = course.getTrack().get(0).getTrackpoint().get(index);
                        TrackPoint trackpoint = toTrackPoint(trT.getTime(), indexTrackPoint.getAndIncrement(),
                                trT.getPosition(), trT.getAltitudeMeters(), trT.getDistanceMeters(), trT.getHeartRateBpm());
                        setExtensions(trT, trackpoint);
                        lap.addTrack(trackpoint);
                    });
                    // Calculate values not informed of a lap.
                    lapsUtils.calculateLapValues(lap);
                    activity.addLap(lap);
                });
                activityUtils.calculateDistanceSpeedValues(activity);
                activities.add(activity);
            });
        }
        return activities;
    }

    private void setExtensions(TrackpointT xmlTrackPoint, TrackPoint modeltrackpoint) {
        ofNullable(xmlTrackPoint)
                .map(TrackpointT::getExtensions)
                .map(ExtensionsT::getAny)
                .ifPresent(extension ->
                        extension.stream()
                                .filter(Objects::nonNull)
                                .filter(JAXBElement.class::isInstance)
                                .map(JAXBElement.class::cast)
                                .map(JAXBElement::getValue)
                                .filter(ActivityTrackpointExtensionT.class::isInstance)
                                .map(ActivityTrackpointExtensionT.class::cast)
                                .map(ActivityTrackpointExtensionT::getSpeed)
                                .map(CommonUtils::toBigDecimal)
                                .findFirst()
                                .ifPresent(speed -> modeltrackpoint.setSpeed(speed))
                );
        if (!Objects.isNull(xmlTrackPoint.getExtensions()) && !Objects.isNull(xmlTrackPoint.getExtensions().getAny())) {
            xmlTrackPoint.getExtensions().getAny().stream()
                    .filter(extension -> (!Objects.isNull(JAXBElement.class.cast(extension))
                            && !Objects.isNull(extension)
                            && !Objects.isNull((JAXBElement.class.cast(extension)).getValue())
                            && !Objects.isNull(ActivityTrackpointExtensionT.class
                            .cast((JAXBElement.class.cast(extension)).getValue()))))
                    .forEach(extension -> {
                        ActivityTrackpointExtensionT actTrackpointExtension = ActivityTrackpointExtensionT.class
                                .cast((JAXBElement.class.cast(extension)).getValue());
                        if (!Objects.isNull(actTrackpointExtension.getSpeed())
                                && actTrackpointExtension.getSpeed() >= 0)
                            modeltrackpoint.setSpeed(toBigDecimal(actTrackpointExtension.getSpeed()));
                    });
        }
    }
}
