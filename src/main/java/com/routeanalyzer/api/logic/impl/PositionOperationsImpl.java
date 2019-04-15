package com.routeanalyzer.api.logic.impl;

import com.routeanalyzer.api.common.MathUtils;
import com.routeanalyzer.api.logic.PositionOperations;
import com.routeanalyzer.api.model.Position;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Predicate;

import static com.routeanalyzer.api.common.MathUtils.metersBetweenCoordinates;
import static java.util.Optional.ofNullable;

@Service
public class PositionOperationsImpl implements PositionOperations {

    @Override
    public boolean isThisPosition(Position position, BigDecimal latitudeDegrees, BigDecimal longitudeDegrees) {
        Predicate<Position> isSameLatitude = isSameCoordinate(latitudeDegrees, Position::getLatitudeDegrees);
        Predicate<Position> isSameLongitude = isSameCoordinate(longitudeDegrees, Position::getLongitudeDegrees);
        return ofNullable(position)
                .filter(isSameLatitude)
                .filter(isSameLongitude)
                .map(Objects::nonNull)
                .orElse(false);
    }

    @Override
    public Double calculateDistance(Position origin, Position end) {
        return ofNullable(origin)
                .map(Position::getLatitudeDegrees)
                .map(MathUtils::degrees2Radians)
                .flatMap(latitudeOrigin -> ofNullable(origin)
                        .map(Position::getLongitudeDegrees)
                        .map(MathUtils::degrees2Radians)
                        .flatMap(longitudeOrigin -> ofNullable(end)
                                .map(Position::getLatitudeDegrees)
                                .map(MathUtils::degrees2Radians)
                                .flatMap(latitudeEnd -> ofNullable(end)
                                        .map(Position::getLongitudeDegrees)
                                        .map(MathUtils::degrees2Radians)
                                        .map(longitudeEnd ->
                                                metersBetweenCoordinates(latitudeOrigin, longitudeOrigin,
                                                        latitudeEnd, longitudeEnd)))))
                .orElse(null);
    }

    private Predicate<Position> isSameCoordinate(BigDecimal coordinateDegrees,
                                                 Function<Position, BigDecimal> coordinateGetter) {
        return positionParam ->
                ofNullable(coordinateDegrees)
                        .flatMap(coordinateDegreesExpected -> ofNullable(positionParam)
                                .map(coordinateGetter)
                                .map(currentCoordinatePosition ->
                                        currentCoordinatePosition.equals(coordinateDegreesExpected)))
                        .orElse(false);
    }

}
