package com.routeanalyzer.api.logic;

import com.routeanalyzer.api.model.Position;
import com.routeanalyzer.api.model.TrackPoint;
import com.routeanalyzer.api.xml.gpx11.WptType;
import com.routeanalyzer.api.xml.tcx.TrackpointT;

import javax.xml.datatype.XMLGregorianCalendar;
import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.Optional;

public interface TrackPointOperations {

	/**
	 * Check if the track point corresponds with the values of the params
	 * 
	 * @param track
	 *            point
	 * @param latitude:
	 *            latitude degrees
	 * @param longitude:
	 *            longitude degrees
	 * @param timeInMillis:
	 *            time in milliseconds
	 * @param index
	 * @return true or false
	 */
	boolean isThisTrack(final TrackPoint track, final String latitude, final String longitude, final Long timeInMillis,
						final Integer index);

	/**
	 * Distance between two track points.
	 *
	 * @param origin
	 *            position
	 * @param end
	 *            position
	 * @return distance in meters
	 */
	Double calculateDistance(final TrackPoint origin, final TrackPoint end);

	/**
	 * Speed calculated with the distance information of both track points.
	 * It is a mandatory that every track point has distance attribute informed.
	 * 
	 * @param origin
	 * @param end
	 * @return speed meters per second
	 */
	Double calculateSpeed(final TrackPoint origin, final TrackPoint end);

	/**
	 * Created track point based on the zoned date time, index, position and altitude
	 * @param zonedDateTime: date time with zone time
	 * @param index: index of the track point in the activity
	 * @param position: geographic position
	 * @param altitude
	 * @return track point model
	 */
	TrackPoint toTrackPoint(final ZonedDateTime zonedDateTime, final int index, final Position position,
							final BigDecimal altitude);

	/**
	 * Created track point based on the zoned date time, index, position and altitude
	 * @param zonedDateTime: date time with zone time
	 * @param index: index of the track point in the activity
	 * @param position: geographic position
	 * @param altitude string
	 * @param distance string
	 * @param heartRate string
	 * @return track point model
	 */
	TrackPoint toTrackPoint(final ZonedDateTime zonedDateTime, final int index, final Position position,
							final String altitude, final String distance, final String speed, final Integer heartRate);


	/**
	 * Created track point based on the zoned date time, index, position and altitude
	 * @param zonedDateTime: date time with zone time
	 * @param index: index of the track point in the activity
	 * @param position: geographic position
	 * @param altitude double
	 * @param distance double
	 * @param speed double
	 * @param heartRate integer
	 * @return track point model
	 */
	TrackPoint toTrackPoint(final ZonedDateTime zonedDateTime, final int index, final Position position,
							final Double altitude, final Double distance, final Double speed,
							final Integer heartRate);

	/**
	 * Created track point based on the zoned date time, index, position and altitude
	 * @param xmlGregorianCalendar: xml gregorian calendar date
	 * @param index: index of the track point in the activity
	 * @param position: geographic position
	 * @param altitude string
	 * @param distance string
	 * @param speed string
	 * @param heartRate integer
	 * @return track point model
	 */
	TrackPoint toTrackPoint(final XMLGregorianCalendar xmlGregorianCalendar, final int index,
							final Position position, final String altitude, final String distance,
							final String speed, final Integer heartRate);
	/**
	 * Created track point based on the zoned date time, index, position and altitude
	 * @param timeMillis: date time in milli seconds
	 * @param index: index of the track point in the activity
	 * @param position: geographic position
	 * @param altitude string
	 * @param distance string
	 * @param speed string
	 * @param heartRate integer
	 * @return track point model
	 */
	TrackPoint toTrackPoint(final long timeMillis, final int index, final Position position,
							final String altitude, final String distance, final String speed,
							final Integer heartRate);

	/**
	 * Created track point based on the zoned date time, index, position and altitude
	 * @param zonedDateTime: date time with time zone
	 * @param index: index of the track point in the activity
	 * @param latitude: latitude geographical position
	 * @param longitude: latitude geographical position
	 * @param altitude string
	 * @param distance string
	 * @param speed string
	 * @param heartRate integer
	 * @return track point model
	 */
	TrackPoint toTrackPoint(final ZonedDateTime zonedDateTime, final int index, final String latitude,
							final String longitude, final String altitude, final String distance,
							final String speed, final Integer heartRate);

	/**
	 * Created track point based on:
	 * @param timeMillis: date time in milli seconds
	 * @param index: index of the track point in the activity
	 * @param latitude: latitude geographical position
	 * @param longitude: latitude geographical position
	 * @param altitude string
	 * @param distance string
	 * @param speed string
	 * @param heartRate integer
	 * @return track point model
	 */
	TrackPoint toTrackPoint(final long timeMillis, final int index, final String latitude,
							final String longitude, final String altitude, final String distance,
							final String speed, final Integer heartRate);

	/**
	 * Created track point based on track point xml model and index
	 * @param trackpointT: xml data model
	 * @param indexTrackPoint: index
	 * @return track point
	 */
	TrackPoint toTrackPoint(final TrackpointT trackpointT, final int indexTrackPoint);

	/**
	 * Create track point from way point and index
	 * @param wptType way point comes from gpx xml file
	 * @param indexTrackPoints index of the track point
	 * @return optional track point
	 */
	Optional<TrackPoint> toTrackPoint(final WptType wptType, final int indexTrackPoints);

}
