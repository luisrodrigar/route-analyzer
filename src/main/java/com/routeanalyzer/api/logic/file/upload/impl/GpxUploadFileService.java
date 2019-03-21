package com.routeanalyzer.logic.file.upload.impl;

import com.amazonaws.AmazonClientException;
import com.google.common.collect.Lists;
import com.routeanalyzer.logic.ActivityUtils;
import com.routeanalyzer.logic.LapsUtils;
import com.routeanalyzer.logic.file.upload.UploadFileService;
import com.routeanalyzer.model.Activity;
import com.routeanalyzer.model.Lap;
import com.routeanalyzer.model.TrackPoint;
import com.routeanalyzer.services.reader.GPXService;
import com.routeanalyzer.xml.gpx11.GpxType;
import com.routeanalyzer.xml.gpx11.trackpointextension.garmin.TrackPointExtensionT;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.xml.sax.SAXParseException;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

import static com.routeanalyzer.common.CommonUtils.toLocalDateTime;
import static com.routeanalyzer.common.CommonUtils.toPosition;
import static com.routeanalyzer.common.CommonUtils.toTrackPoint;

@Service
public class GpxUploadFileService implements UploadFileService {

    private GPXService gpxService;
    private ActivityUtils activityUtils;
    private LapsUtils lapsUtils;

    public GpxUploadFileService(GPXService gpxService, ActivityUtils activityUtils, LapsUtils lapsUtils) {
        this.gpxService = gpxService;
        this.activityUtils = activityUtils;
        this.lapsUtils = lapsUtils;
    }

    @Override
    public List<Activity> upload(MultipartFile multiPart)
            throws IOException, AmazonClientException, JAXBException, SAXParseException {
        InputStream inputFileGPX = multiPart.getInputStream();
        // Get the object from xml file (type GPX)
        GpxType gpx = gpxService.readXML(inputFileGPX);
        // Create each activity of the file
        return getListActivitiesFromGPX(gpx);
    }

    /**
     * Get these Activity model list with the information contained in the xml file
     * @param gpx
     * @return
     */
    private List<Activity> getListActivitiesFromGPX(GpxType gpx) {
        List<Activity> activities = Lists.newArrayList();
        AtomicInteger indexLap = new AtomicInteger(), indexTrackPoint = new AtomicInteger();
        gpx.getTrk().forEach(track -> {
            Activity activity = new Activity();
            activity.setSourceXmlType("gpx");
            activity.setDate(!Objects.isNull(gpx.getMetadata()) && !Objects.isNull(gpx.getMetadata().getTime())
                    && !Objects.isNull(indexLap.get())
                    ? LocalDateTime
                    .from(gpx.getMetadata().getTime().toGregorianCalendar().getTime().toInstant())
                    : (!Objects.isNull(track.getTrkseg()) && !Objects.isNull(track.getTrkseg().get(0))
                    && !Objects.isNull(track.getTrkseg().get(0).getTrkpt())
                    && !Objects.isNull(track.getTrkseg().get(0).getTrkpt().get(0))
                    && !Objects.isNull(track.getTrkseg().get(0).getTrkpt().get(0).getTime())
                    ?  toLocalDateTime(track.getTrkseg().get(0).getTrkpt().get(0).getTime()).orElse(null)
                    : null));
            activity.setDevice(gpx.getCreator());
            activity.setName(!Objects.isNull(track.getName()) ? track.getName().trim() : null);
            track.getTrkseg().forEach(eachLap -> {
                Lap lap = new Lap();
                if (!Objects.isNull(eachLap.getTrkpt()) && !eachLap.getTrkpt().isEmpty()
                        && !Objects.isNull(eachLap.getTrkpt().get(0).getTime()))
                    lap.setStartTime(toLocalDateTime(eachLap.getTrkpt().get(0).getTime()).orElse(null));
                lap.setIndex(indexLap.incrementAndGet());
                eachLap.getTrkpt().forEach(eachTrackPoint -> {
                    // Adding track point only if position is informed
                    if (!Objects.isNull(eachTrackPoint.getLat()) && !Objects.isNull(eachTrackPoint.getLon())) {
                        TrackPoint tkp = toTrackPoint(eachTrackPoint.getTime(), indexTrackPoint.incrementAndGet(),
                                toPosition(eachTrackPoint.getLat(), eachTrackPoint.getLon()), String.valueOf(eachTrackPoint.getEle()),
                                null, null, null);
                        if (!Objects.isNull(eachTrackPoint.getExtensions())) {
                            eachTrackPoint.getExtensions().getAny()
                                    .stream().filter(
                                    item -> (!Objects.isNull(JAXBElement.class.cast(item))
                                            && !Objects.isNull(item)
                                            && !Objects.isNull(JAXBElement.class.cast(item).getValue())
                                            && !Objects.isNull(TrackPointExtensionT.class
                                            .cast(JAXBElement.class.cast(item).getValue()))))
                                    .forEach(item -> {
                                        TrackPointExtensionT trpExt = TrackPointExtensionT.class
                                                .cast((JAXBElement.class.cast(item)).getValue());
                                        if (!Objects.isNull(trpExt.getHr()))
                                            tkp.setHeartRateBpm(trpExt.getHr().intValue());
                                    });
                        }
                        lap.addTrack(tkp);
                    }
                });
                lapsUtils.calculateLapValues(lap);
                activity.addLap(lap);
            });
            // Check if no speed nor distance values
            activityUtils.calculateDistanceSpeedValues(activity);

            activities.add(activity);
        });
        return activities;
    }
}
