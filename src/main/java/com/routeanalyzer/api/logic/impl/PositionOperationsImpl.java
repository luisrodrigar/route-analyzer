package com.routeanalyzer.api.logic.impl;

import com.routeanalyzer.api.common.MathUtils;
import com.routeanalyzer.api.logic.PositionOperations;
import com.routeanalyzer.api.model.Position;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Predicate;

import static java.util.Objects.nonNull;
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
                .filter(isCoordinatesNotNull)
                .flatMap(__ -> ofNullable(end)
                        .filter(isCoordinatesNotNull)
                        .map(___ -> metersBetweenPositions(origin, end)))
                .orElse(null);
    }

    private double metersBetweenPositions(Position origin, Position end) {
        return MathUtils.metersBetweenCoordinates(origin.getLatitudeDegrees(), origin.getLongitudeDegrees(),
                end.getLatitudeDegrees(), end.getLongitudeDegrees());
    }

    private Predicate<Position> isCoordinatesNotNull = position -> nonNull(position.getLatitudeDegrees())
            && nonNull(position.getLongitudeDegrees());

    private Predicate<Position> isSameCoordinate(BigDecimal coordinateDegrees,
                                                 Function<Position, BigDecimal> coordinateGetter) {
        return positionParam -> ofNullable(coordinateDegrees)
                .flatMap(coordinateDegreesExpected -> ofNullable(positionParam)
                        .map(coordinateGetter)
                        .map(currentCoordinatePosition -> currentCoordinatePosition.equals(coordinateDegreesExpected)))
                .orElse(false);
    }

}
