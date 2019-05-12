package com.routeanalyzer.api.logic.file.export.impl;

import com.routeanalyzer.api.common.DateUtils;
import com.routeanalyzer.api.logic.file.export.ExportFileService;
import com.routeanalyzer.api.model.Activity;
import com.routeanalyzer.api.model.Lap;
import com.routeanalyzer.api.model.Position;
import com.routeanalyzer.api.model.TrackPoint;
import com.routeanalyzer.api.services.reader.GPXService;
import com.routeanalyzer.api.xml.gpx11.ExtensionsType;
import com.routeanalyzer.api.xml.gpx11.GpxType;
import com.routeanalyzer.api.xml.gpx11.MetadataType;
import com.routeanalyzer.api.xml.gpx11.TrkType;
import com.routeanalyzer.api.xml.gpx11.TrksegType;
import com.routeanalyzer.api.xml.gpx11.WptType;
import com.routeanalyzer.api.xml.gpx11.trackpointextension.garmin.ObjectFactory;
import com.routeanalyzer.api.xml.gpx11.trackpointextension.garmin.TrackPointExtensionT;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.xml.bind.JAXBElement;
import javax.xml.datatype.XMLGregorianCalendar;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static java.util.Optional.of;
import static java.util.Optional.ofNullable;

@Service
public class GpxExportFileService extends ExportFileService<GpxType> {

    @Autowired
    public GpxExportFileService(GPXService gpxService) {
        super(gpxService);
    }

    @Override
    public Optional<JAXBElement<GpxType>> convertToXmlObjects(Activity activity) {
        return ofNullable(activity)
                .map(Activity::getLaps)
                .flatMap(this::toOptionalTrkType)
                .map(addTrkType)
                .map(addGlobalData)
                .map(adderGlobalData -> adderGlobalData.apply(activity))
                .map(objectFactorySupplier.get()::createGpx);
    }

    /**
     * Suppliers
     */
    private Supplier<com.routeanalyzer.api.xml.gpx11.ObjectFactory> objectFactorySupplier =
            () -> new com.routeanalyzer.api.xml.gpx11.ObjectFactory();
    private Supplier<GpxType> gpxTypeSupplier = () -> new GpxType();
    private Supplier<TrkType> createTrkType = () -> new TrkType();
    private Supplier<ObjectFactory> creationExtensionFactory = () -> new ObjectFactory();
    private Supplier<TrksegType> createTrkSegType = () -> new TrksegType();
    private Supplier<WptType> createWptType = () -> new WptType();
    private Supplier<TrackPointExtensionT> createTrackPointExtensionT = () -> new TrackPointExtensionT();
    private Supplier<ExtensionsType> createExtensionsType = () -> new ExtensionsType();
    private Supplier<MetadataType> createMetadataType = () -> new MetadataType();

    private Function<XMLGregorianCalendar, MetadataType> generateMetadata = xmlGregorianCalendar -> {
        MetadataType metadata = createMetadataType.get();
        metadata.setTime(xmlGregorianCalendar);
        return metadata;
    };

    /**
     *
     * Functions
     *
     */
    private Function<GpxType, Function<Activity, GpxType>> addGlobalData = gpxType -> act -> {
        of(act)
                .map(Activity::getDate)
                .flatMap(DateUtils::toDate)
                .map(DateUtils::createGregorianCalendar)
                .map(DateUtils::createXmlGregorianCalendar)
                .map(generateMetadata)
                .ifPresent(gpxType::setMetadata);
        of(act)
                .map(Activity::getDevice)
                .ifPresent(gpxType::setCreator);
        return gpxType;
    };

    private Function<List<TrksegType>, TrkType> addTrkSegType = trkSegTypes -> {
        TrkType trkType = createTrkType.get();
        trkType.getTrkseg().addAll(trkSegTypes);
        return trkType;
    };

    private Function<List<WptType>, TrksegType> addWptType = wptType -> {
        TrksegType trkSeg = createTrkSegType.get();
        trkSeg.getTrkpt().addAll(wptType);
        return trkSeg;
    };
    private Function<Short, TrackPointExtensionT> setActivityLapExtension = heartRate -> {
        TrackPointExtensionT actExtensionT = createTrackPointExtensionT.get();
        actExtensionT.setHr(heartRate);
        return actExtensionT;
    };
    private Function<JAXBElement, ExtensionsType> setExtension = jaxbElement -> {
        ExtensionsType extT = createExtensionsType.get();
        extT.addAny(jaxbElement);
        return extT;
    };
    private Function<TrkType, GpxType> addTrkType = trkType -> {
        GpxType gpx = gpxTypeSupplier.get();
        gpx.addTrk(trkType);
        return gpx;
    };
    private Function<ExtensionsType, Function<WptType, WptType>> getAdderExtensions = extensionsType -> wpt -> {
        wpt.setExtensions(extensionsType);
        return wpt;
    };

    private Optional<TrkType> toOptionalTrkType(List<Lap> laps) {
        return of(laps.stream()
                .map(this::toTrkSegTypes)
                .filter(Objects::nonNull)
                .collect(Collectors.toList()))
                .map(addTrkSegType);
    }

    private TrksegType toTrkSegTypes(Lap lap) {
        return ofNullable(lap)
                .map(Lap::getTracks)
                .map(trackPoints ->
                    trackPoints.stream().map(trackPoint -> {
                        WptType wpt = createWptType.get();
                        Optional<TrackPoint> optTrackPoint = ofNullable(trackPoint);
                        // Date time
                        optTrackPoint.map(TrackPoint::getDate)
                                .flatMap(DateUtils::toDate)
                                .map(DateUtils::createGregorianCalendar)
                                .map(DateUtils::createXmlGregorianCalendar)
                                .ifPresent(wpt::setTime);
                        // Latitude
                        optTrackPoint.map(TrackPoint::getPosition)
                                .map(Position::getLatitudeDegrees)
                                .ifPresent(wpt::setLat);
                        // Longitude
                        optTrackPoint.map(TrackPoint::getPosition)
                                .map(Position::getLongitudeDegrees)
                                .ifPresent(wpt::setLon);
                        // Elevation
                        optTrackPoint.map(TrackPoint::getAltitudeMeters)
                                .ifPresent(wpt::setEle);

                        return optTrackPoint.map(TrackPoint::getHeartRateBpm)
                                .map(Integer::shortValue)
                                .map(setActivityLapExtension)
                                .map(creationExtensionFactory.get()::createTrackPointExtension)
                                .map(setExtension)
                                .map(getAdderExtensions::apply)
                                .map(setAddExtension -> setAddExtension.apply(wpt))
                                .orElse(null);
                    })
                )
                .filter(Objects::nonNull)
                .map(stream -> stream.collect(Collectors.toList()))
                .map(addWptType)
                .orElse(null);
    }

}
