package com.routeanalyzer.logic;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.DoubleAdder;
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
			if (index > 0) {
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
				maxSpeed.add((maxSpeed.doubleValue() < currentSpeed) ? currentSpeed - maxSpeed.doubleValue()
						: maxSpeed.doubleValue());
				maxBpm.set(track.getHeartRateBpm() != null
						? (maxBpm.get() < track.getHeartRateBpm()) ? track.getHeartRateBpm() : maxBpm.get()
						: maxBpm.get());
				totalBpm.addAndGet(track.getHeartRateBpm() != null ? track.getHeartRateBpm() : 0);
				// Distance in meters
				if (track.getDistanceMeters() == null && currentDistance != 0.0)
					track.setDistanceMeters(new BigDecimal(currentDistance));
				// Speed in meters per second
				if (track.getSpeed() == null && currentSpeed != 0.0)
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
		if(positions!=null && (positions.split("|").length>0 || !positions.isEmpty())){
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
}
