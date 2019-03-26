package com.routeanalyzer.api.logic;

import com.routeanalyzer.api.model.Position;

import java.math.BigDecimal;

public interface PositionOperations {

    /**
     *
     * @param position
     * @param latitudeDegrees
     * @param longitudeDegrees
     * @return
     */
    boolean isThisPosition(Position position, BigDecimal latitudeDegrees, BigDecimal longitudeDegrees);

    /**
     * Distance between two positions. Degrees to radians and then, radians to
     * meters.
     *
     * @param origin
     *            position
     * @param end
     *            position
     * @return distance in meters
     */
    Double calculateDistance(Position origin, Position end);

}
