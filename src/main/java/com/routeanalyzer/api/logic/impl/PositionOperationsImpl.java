package com.routeanalyzer.api.logic.impl;

import com.routeanalyzer.api.common.MathUtils;
import com.routeanalyzer.api.logic.PositionOperations;
import com.routeanalyzer.api.model.Position;
import com.routeanalyzer.api.xml.gpx11.WptType;
import com.routeanalyzer.api.xml.tcx.TrackpointT;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;

import static com.routeanalyzer.api.common.CommonUtils.toStringValue;
import static com.routeanalyzer.api.common.MathUtils.toBigDecimal;
import static java.util.Objects.nonNull;
import static java.util.Optional.ofNullable;

@Service
public class PositionOperationsImpl implements PositionOperations {

    @Override
    public boolean isThisPosition(final Position position, final String latitudeDegrees, final String longitudeDegrees) {
        Predicate<Position> isSameLatitude = isSameCoordinate(latitudeDegrees, Position::getLatitudeDegrees);
        Predicate<Position> isSameLongitude = isSameCoordinate(longitudeDegrees, Position::getLongitudeDegrees);
        return ofNullable(position)
                .filter(__ -> nonNull(latitudeDegrees))
                .filter(__ -> nonNull(longitudeDegrees))
                .filter(isSameLatitude)
                .filter(isSameLongitude)
                .map(Objects::nonNull)
                .orElse(false);
    }

    @Override
    public Optional<Double> calculateDistance(final Position origin, final Position end) {
        Predicate<Position> isCoordinatesNotNull = position -> nonNull(position.getLatitudeDegrees())
                && nonNull(position.getLongitudeDegrees());
        return ofNullable(origin)
                .filter(isCoordinatesNotNull)
                .flatMap(__ -> ofNullable(end)
                        .filter(isCoordinatesNotNull)
                        .map(___ -> metersBetweenPositions(origin, end)));
    }

    @Override
    public Position toPosition(final String latitude, final String longitude) {
        return toPosition(toBigDecimal(latitude), toBigDecimal(longitude))
                .orElse(null);
    }

    @Override
    public Optional<Position> toOptPosition(final String latitude, final String longitude) {
        return ofNullable(toBigDecimal(latitude))
                .flatMap(latitudeBD -> ofNullable(toBigDecimal(longitude))
                        .map(longitudeBD -> new Position(latitudeBD, longitudeBD)));
    }

    @Override
    public Position toPosition(final Double latitude, final Double longitude) {
        return toPosition(toStringValue(latitude), toStringValue(longitude));
    }

    @Override
    public Optional<Position> toPosition(final WptType wptType) {
        return ofNullable(wptType)
                .flatMap(__ -> toPosition(wptType.getLat(), wptType.getLon()));
    }

    @Override
    public Optional<Position> toPosition(final TrackpointT trackpointT) {
        return ofNullable(trackpointT)
                .map(TrackpointT::getPosition)
                .map(positionT -> toPosition(positionT.getLatitudeDegrees(), positionT.getLongitudeDegrees()));
    }

    @Override
    public Optional<Position> toPosition(final BigDecimal latParam, final BigDecimal lngParam) {
        return ofNullable(latParam)
                .flatMap(latitude -> ofNullable(lngParam)
                        .map(longitude -> new Position(latitude, longitude)));
    }

    private double metersBetweenPositions(final Position origin, final Position end) {
        return MathUtils.metersBetweenCoordinates(origin.getLatitudeDegrees(), origin.getLongitudeDegrees(),
                end.getLatitudeDegrees(), end.getLongitudeDegrees());
    }

    private Predicate<Position> isSameCoordinate(final String coordinateDegrees,
                                                 final Function<Position, BigDecimal> coordinateGetter) {
        return positionParam -> ofNullable(coordinateDegrees)
                .map(BigDecimal::new)
                .flatMap(coordinateBD -> ofNullable(positionParam)
                        .map(coordinateGetter)
                        .map(BigDecimal::stripTrailingZeros)
                        .map(coordinateBD::equals))
                .orElse(false);
    }

}
