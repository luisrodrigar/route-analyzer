package com.routeanalyzer.api.logic;

import com.routeanalyzer.api.model.Position;
import com.routeanalyzer.api.xml.gpx11.WptType;
import com.routeanalyzer.api.xml.tcx.TrackpointT;

import java.math.BigDecimal;
import java.util.Optional;

public interface PositionOperations {

    /**
     * Is the position indicated by the latitude and longitude in degrees
     * @param position to compare
     * @param latitudeDegrees string
     * @param longitudeDegrees string
     * @return
     */
    boolean isThisPosition(Position position, String latitudeDegrees, String longitudeDegrees);

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
    Optional<Double> calculateDistance(Position origin, Position end);

    /**
     * Create position based on latitude and longitude string values
     * @param latitude
     * @param longitude
     * @return position created
     */
    Position toPosition(String latitude, String longitude);

    /**
     * Create optional position based on latitude and longitude
     * @param latitude
     * @param longitude
     * @return optional position created
     */
    Optional<Position> toOptPosition(String latitude, String longitude);

    /**
     * Create position based on latitude and longitude numeric values
     * @param latitude: latitude
     * @param longitude: longitude
     * @return position created
     */
    Position toPosition(Double latitude, Double longitude);

    /**
     * Create position based on a way point
     * @param wptType: way point type from gpx type
     * @return optional position created
     */
    Optional<Position> toPosition(WptType wptType);

    /**
     * Create position based on a pointT from tcx xml file
     * @param trackpointT: way point type from tcx type
     * @return optional position created
     */
    Optional<Position> toPosition(TrackpointT trackpointT);

    /**
     * Create position based on latitude and longitude big decimal values
     * @param latitude: latitude param
     * @param longitude: longitude param
     * @return optional position created
     */
    Optional<Position> toPosition(BigDecimal latitude, BigDecimal longitude);

}
