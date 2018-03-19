package com.routeanalyzer.logic;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.DoubleAdder;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import com.routeanalyzer.model.Lap;
import com.routeanalyzer.model.TrackPoint;
import com.routeanalyzer.services.GoogleMapsService;

public class LapsUtils {

	/**
	 * Calculate new values of the lap such as: total distance, total time,
	 * average heart rate,...
	 * 
	 * @param lap
	 */
	public static void recalculateLapValues(Lap lap) {
		List<TrackPoint> tracks = lap.getTracks();
		// Lap start time corresponds with the date of the first point
		lap.setStartTime(tracks.get(0).getDate());
		AtomicInteger maxBpm = new AtomicInteger(), totalBpm = new AtomicInteger();
		DoubleAdder totalTime = new DoubleAdder(), totalDistance = new DoubleAdder(), totalSpeed = new DoubleAdder(),
				maxSpeed = new DoubleAdder();

		tracks.forEach(track -> {
			int index = tracks.indexOf(track);
			if (index > 0 && tracks.get(index - 1).getPosition() != null) {
				TrackPoint previousTrack = tracks.get(index - 1);
				double currentDistance = TrackPointsUtils.getDistanceBetweenPoints(previousTrack.getPosition(),
						track.getPosition());
				long timeBetween = 0;
				double currentSpeed = 0.0;
				if (track.getDate() != null) {
					timeBetween = (track.getDate().getTime() - previousTrack.getDate().getTime()) / 1000;
					if (timeBetween != 0)
						currentSpeed = currentDistance / timeBetween;
				}

				totalDistance.add(currentDistance);
				totalTime.add(timeBetween);
				totalSpeed.add(currentSpeed);
				maxSpeed.add((maxSpeed.doubleValue() < currentSpeed) ? (currentSpeed - maxSpeed.doubleValue()) : 0.0);
				maxBpm.set(track.getHeartRateBpm() != null
						? (maxBpm.get() < track.getHeartRateBpm() ? track.getHeartRateBpm() : maxBpm.get())
						: maxBpm.get());
				totalBpm.addAndGet(track.getHeartRateBpm() != null ? track.getHeartRateBpm() : 0);
				// Distance in meters
				if (track.getDistanceMeters() == null)
					track.setDistanceMeters(new BigDecimal(currentDistance));
				// Speed in meters per second
				if (track.getSpeed() == null)
					track.setSpeed(new BigDecimal(currentSpeed));
			}
		});
		if (totalDistance != null && totalDistance.doubleValue() > 0.0)
			lap.setDistanceMeters(totalDistance.doubleValue());
		if (totalTime != null && totalTime.doubleValue() > 0.0)
			lap.setTotalTimeSeconds(totalTime.doubleValue());
		if (totalBpm.get() > 0 && lap.getTracks().size() > 0)
			lap.setAverageHearRate(Double.valueOf(totalBpm.get()) / lap.getTracks().size());
		if (totalSpeed.doubleValue() > 0.0 && lap.getTracks().size() > 0)
			lap.setAverageSpeed(lap.getTracks().size() > 0 ? totalSpeed.doubleValue() / lap.getTracks().size() : 0.0);
		if (maxSpeed.doubleValue() > 0)
			lap.setMaximunSpeed(maxSpeed.doubleValue());
		if (maxBpm.doubleValue() > 0)
			lap.setMaximunHeartRate(maxBpm.get());
	}

	/**
	 * Method which returns an index corresponding to the track point with the
	 * latitude, longitude and ( time or index) contained in the parameters.
	 * 
	 * @param act:
	 *            activity
	 * @param indexLap:
	 *            index of the lap which contains track points
	 * @param lat:
	 *            latitude position
	 * @param lng:
	 *            longitude position
	 * @param timeInMillis:
	 *            time in milliseconds
	 * @param index:
	 *            index of the position in the array
	 * @return index of a track point
	 */
	public static int getIndexOfAPosition(List<Lap> laps, int indexLap, String lat, String lng, String timeInMillis,
			String index) {
		TrackPoint trackPoint = laps.get(indexLap).getTracks().stream().filter(track -> {
			return TrackPointsUtils.isThisTrack(track, lat, lng,
					timeInMillis != null && !timeInMillis.isEmpty() ? Long.parseLong(timeInMillis) : null,
					index != null && !index.isEmpty() ? Integer.parseInt(index) : null);
		}).findFirst().orElse(null);
		return trackPoint != null ? laps.get(indexLap).getTracks().indexOf(trackPoint) : -1;
	}

	/**
	 * Method which returns an index corresponding to the lap which contains
	 * track point with the latitude, longitude and ( time or index) contained
	 * in the parameters.
	 * 
	 * @param act:
	 *            activity
	 * @param lat:
	 *            latitude position
	 * @param lng:
	 *            longitude position
	 * @param timeInMillis:
	 *            time in milliseconds
	 * @param index:
	 *            index of the lap in the array
	 * @return index of the lap
	 */
	public static int getIndexOfALap(List<Lap> laps, String lat, String lng, String timeInMillis, String index) {
		Lap lap = laps.stream().filter(eachLap -> {
			return eachLap.getTracks().stream().anyMatch(track -> {
				return TrackPointsUtils.isThisTrack(track, lat, lng,
						timeInMillis != null && !timeInMillis.isEmpty() ? Long.parseLong(timeInMillis) : null,
						index != null && !index.isEmpty() ? Integer.parseInt(index) : null);
			});
		}).findFirst().orElse(null);

		return lap != null ? laps.indexOf(lap) : -1;
	}

	/**
	 * Calculate altitude if it does not exist in each trackpoint.
	 */
	public static void calculateAltitude(List<Lap> laps) {
		List<List<String>> listLapGetAltitude = new ArrayList<List<String>>();
		laps.forEach(lap -> {
			List<String> listPositionsEachLap = new ArrayList<String>();
			lap.getTracks().stream().filter(track -> {
				return track.getAltitudeMeters() == null && track.getPosition() != null;
			}).forEach(track -> {
				listPositionsEachLap.add(
						track.getPosition().getLatitudeDegrees() + "," + track.getPosition().getLongitudeDegrees());
			});
			listLapGetAltitude.add(listPositionsEachLap);
		});

		String positions = listLapGetAltitude.stream().map(element -> {
			return element.stream().collect(Collectors.joining("|"));
		}).collect(Collectors.joining("|"));
		// Solo si al menos contiene una
		if (positions != null && !positions.isEmpty() && !positions.replaceAll(Pattern.quote("|"), "").isEmpty()) {
			Map<String, String> elevations = GoogleMapsService.getAltitude(positions);
			if ("OK".equalsIgnoreCase(elevations.get("status"))) {
				laps.forEach(lap -> {
					lap.getTracks().forEach(trackPoint -> {
						if (trackPoint.getAltitudeMeters() == null)
							trackPoint.setAltitudeMeters(
									new BigDecimal(elevations.get(trackPoint.getPosition().getLatitudeDegrees() + ","
											+ trackPoint.getPosition().getLongitudeDegrees())));
					});
				});
			}
		}
	}

	/**
	 * Calculate speed of a laps
	 * 
	 * @param laps
	 */
	public static void calculateSpeed(List<Lap> laps) {
		laps.forEach(lap -> {
			int indexCurrentLap = laps.indexOf(lap);
			DoubleAdder totalDistance = new DoubleAdder(), maxSpeed = new DoubleAdder();
			lap.getTracks().stream().forEach(track -> {
				if (track.getSpeed() == null) {
					int indexCurrentTrack = lap.getTracks().indexOf(track);
					// Start point's speed value is zero unless it is informed
					if (indexCurrentTrack == 0 && indexCurrentLap == 0) {
						if (track.getDistanceMeters() == null)
							track.setDistanceMeters(new BigDecimal(0.0));
						track.setSpeed(new BigDecimal(0.0));
					}
					// Start point of a lap, take the last point of the previous
					// lap
					else if (indexCurrentTrack == 0) {
						int sizeTracksPreviousLap = laps.get(indexCurrentLap - 1).getTracks().size();
						TrackPoint previousTrackPreviousLap = laps.get(indexCurrentLap - 1).getTracks()
								.get(sizeTracksPreviousLap - 1);
						TrackPointsUtils.calculateSpeedBetweenPoints(previousTrackPreviousLap, track);
					}
					// A point between points in a same lap
					else {
						int indexPreviousTrack = indexCurrentTrack - 1;
						TrackPoint previousTrackPoint = lap.getTracks().get(indexPreviousTrack);
						TrackPointsUtils.calculateSpeedBetweenPoints(previousTrackPoint, track);
					}
					totalDistance.add(track.getDistanceMeters().doubleValue());
					if (track.getSpeed().doubleValue() > maxSpeed.doubleValue())
						maxSpeed.add(track.getSpeed().doubleValue() - maxSpeed.doubleValue());
				}
			});
			// lap's total distance in meters
			lap.setDistanceMeters(totalDistance.doubleValue());
			// lap's max speed
			if(lap.getMaximunSpeed()==null)
				lap.setMaximunSpeed(maxSpeed.doubleValue());
			// lap's total time
			double totalTime = (lap.getTracks().get(lap.getTracks().size() - 1).getDate().getTime()
					- lap.getTracks().get(0).getDate().getTime())/1000;
			lap.setTotalTimeSeconds(totalTime);
			// lap's average speed
			if(lap.getAverageSpeed()==null && totalTime > 0)
				lap.setAverageSpeed(lap.getDistanceMeters()/lap.getTotalTimeSeconds());
			
		});
	}
}
