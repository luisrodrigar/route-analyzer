package com.routeanalyzer.logic;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.ToDoubleFunction;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import com.routeanalyzer.model.Activity;
import com.routeanalyzer.model.Lap;
import com.routeanalyzer.model.Position;
import com.routeanalyzer.model.TrackPoint;
import com.routeanalyzer.services.GoogleMapsService;

public class LapsUtils {

	public static Lap joinLaps(Lap lapLeft, Lap lapRight) {
		// Join the track points of the two laps
		List<TrackPoint> tracks = new ArrayList<TrackPoint>();
		tracks.addAll(lapLeft.getTracks());
		tracks.addAll(lapRight.getTracks());

		Lap newLap = new Lap();
		// Tracks joined
		newLap.setTracks(tracks);
		// Start time is the first left lap's track point time
		newLap.setStartTime(newLap.getTracks().get(0).getDate());
		// Calories are the total sum
		Integer leftCal = null, rightCal = null;
		if (!Objects.isNull(lapLeft.getCalories()))
			leftCal = lapLeft.getCalories();
		if (!Objects.isNull(lapRight.getCalories()))
			rightCal = lapRight.getCalories();
		newLap.setCalories(Objects.isNull(leftCal) && Objects.isNull(rightCal) ? null
				: ((Objects.isNull(leftCal) ? 0 : leftCal) + (Objects.isNull(rightCal) ? 0 : rightCal)));
		// Intensidad
		newLap.setIntensity(
				!Objects.isNull(lapLeft.getIntensity())
						? (!Objects.isNull(lapRight.getIntensity())
								? (lapLeft.getIntensity().equals(lapRight.getIntensity()) ? lapLeft.getIntensity()
										: (lapLeft.getDistanceMeters() > lapRight.getDistanceMeters()
												? lapLeft.getIntensity() : lapRight.getIntensity()))
								: (lapLeft.getIntensity()))
						: (!Objects.isNull(lapRight.getIntensity()) ? (lapRight.getIntensity()) : null));

		// Total time seconds is the left lap total time plus right lap total
		// time
		newLap.setTotalTimeSeconds(lapLeft.getTotalTimeSeconds() + lapRight.getTotalTimeSeconds());
		// Total distance
		newLap.setDistanceMeters(lapLeft.getDistanceMeters() + lapRight.getDistanceMeters());

		calculateAggregateHeartRate(newLap);
		calculateAggregateSpeed(newLap);

		// Index of the left lap
		newLap.setIndex(lapLeft.getIndex());

		return newLap;
	}

	public static void calculateLapValues(Lap lap) {
		// Fill altitude fields
		calculateAltitude(lap);
		// Fill aggregate heart rate fields
		calculateAggregateHeartRate(lap);
		boolean hasSpeedValues = hasLapTrackpointValue(lap, track->!Objects.isNull(track.getSpeed()));
		if(hasSpeedValues)
			calculateAggregateSpeed(lap);
	}

	public static void resetAggregateValues(Lap lap) {
		lap.setAverageHearRate(null);
		lap.setMaximunHeartRate(null);
		lap.setAverageSpeed(null);
		lap.setMaximunSpeed(null);
	}

	public static void setTotalValuesLap(Lap lap) {
		if (Objects.isNull(lap.getTotalTimeSeconds()))
			lap.setTotalTimeSeconds(Double.valueOf((lap.getTracks().get(lap.getTracks().size() - 1).getDate().getTime()
					- lap.getTracks().get(0).getDate().getTime()) / 1000));
		if (Objects.isNull(lap.getDistanceMeters()))
			lap.setDistanceMeters(lap.getTracks().get(lap.getTracks().size() - 1).getDistanceMeters().doubleValue()
					- lap.getTracks().get(0).getDistanceMeters().doubleValue());
	}

	public static void resetTotals(Lap lap) {
		lap.setTotalTimeSeconds(null);
		lap.setDistanceMeters(null);
	}

	/**
	 * Calculate altitude if it does not exist in each lap's track points.
	 * 
	 */
	public static void calculateAltitude(Lap lap) {
		boolean hasAltitudeValues = hasLapTrackpointValue(lap, track -> !Objects.isNull(track.getAltitudeMeters()));
		boolean hasPositionValues = hasLapTrackpointValue(lap, track -> !Objects.isNull(track.getPosition()));
		if (!hasAltitudeValues && hasPositionValues) {
			List<String> listPositionsEachLap = new ArrayList<String>();
			lap.getTracks().stream().filter(track -> {
				return track.getAltitudeMeters() == null && !Objects.isNull(track.getPosition());
			}).forEach(track -> {
				Position position = track.getPosition();
				listPositionsEachLap.add(position.getLatitudeDegrees() + "," + position.getLongitudeDegrees());
			});

			String positions = listPositionsEachLap.stream().collect(Collectors.joining("|"));

			if (!Objects.isNull(positions) && !positions.isEmpty() && !listPositionsEachLap.isEmpty()) {
				Map<String, String> elevations = GoogleMapsService.getAltitude(positions);
				if ("OK".equalsIgnoreCase(elevations.get("status"))) {
					lap.getTracks().forEach(trackPoint -> {
						Position position = trackPoint.getPosition();
						if (trackPoint.getAltitudeMeters() == null)
							trackPoint.setAltitudeMeters(new BigDecimal(elevations
									.get(position.getLatitudeDegrees() + "," + position.getLongitudeDegrees())));
					});
				}
			}
		}
	}

	public static void calculateDistanceLap(Lap lap, TrackPoint previousLapLastTrackpoint) {
		lap.getTracks().forEach(track -> {
			int indexTrack = lap.getTracks().indexOf(track);
			double distance = 0.0;
			switch (indexTrack) {
			case 0:
				if (!Objects.isNull(previousLapLastTrackpoint)) {
					distance = TrackPointsUtils.getDistanceBetweenPoints(track.getPosition(),
							previousLapLastTrackpoint.getPosition());
					track.setDistanceMeters(
							new BigDecimal(previousLapLastTrackpoint.getDistanceMeters().doubleValue() + distance));
				} else
					track.setDistanceMeters(new BigDecimal(0.0));
				break;
			default:
				TrackPoint previousTrackpoint = lap.getTracks().get(indexTrack - 1);
				distance = TrackPointsUtils.getDistanceBetweenPoints(previousTrackpoint.getPosition(),
						track.getPosition());
				track.setDistanceMeters(
						new BigDecimal(previousTrackpoint.getDistanceMeters().doubleValue() + distance));
				break;
			}
		});
	}

	public static void calculateSpeedLap(Lap lap, TrackPoint previousLapLastTrackpoint) {
		lap.getTracks().forEach(track -> {
			int indexTrack = lap.getTracks().indexOf(track);
			double speed = 0.0;
			if (Objects.isNull(track.getSpeed())) {
				switch (indexTrack) {
				case 0:
					speed = TrackPointsUtils.getSpeedBetweenPoints(previousLapLastTrackpoint, track);
					track.setSpeed(new BigDecimal(speed));
					break;
				default:
					TrackPoint previousTrackpoint = lap.getTracks().get(indexTrack - 1);
					speed = TrackPointsUtils.getSpeedBetweenPoints(previousTrackpoint, track);
					track.setSpeed(new BigDecimal(speed));
					break;
				}
			}
		});
		setTotalValuesLap(lap);
		calculateAggregateSpeed(lap);
	}

	/**
	 * 
	 * @param lap
	 * @param position
	 * @param timeInMillis
	 * @param index
	 * @return
	 */
	public static boolean fulfillCriteriaPositionTime(Lap lap, Position position, Long timeInMillis, Integer index) {
		return lap.getTracks().stream().anyMatch(track -> {
			return TrackPointsUtils.isThisTrack(track, position, timeInMillis, index);
		});
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
	public static int indexOfTrackpoint(Activity activity, Integer indexLap, Position position, Long time,
			Integer index) {
		TrackPoint trackPoint = activity.getLaps().get(indexLap).getTracks().stream().filter(track -> {
			return TrackPointsUtils.isThisTrack(track, position, time, index);
		}).findFirst().orElse(null);
		return !Objects.isNull(trackPoint) ? activity.getLaps().get(indexLap).getTracks().indexOf(trackPoint) : -1;
	}

	public static void calculateAggregateValuesLap(Lap lap) {
		calculateAggregateHeartRate(lap);
		calculateAggregateSpeed(lap);
	}

	/**
	 * 
	 * @param initIndex
	 *            track point included
	 * @param endIndex
	 *            track point not included
	 */
	public static void createSplittLap(Lap lap, Lap newLap, int initIndex, int endIndex) {
		int sizeOfTrackPoints = lap.getTracks().size();
		LapsUtils.resetAggregateValues(newLap);
		LapsUtils.resetTotals(newLap);
		// lap left: add left track points
		List<TrackPoint> leftTrackPoints = new ArrayList<TrackPoint>();
		IntStream.range(initIndex, endIndex).forEach(indexEachTrackPoint -> {
			leftTrackPoints.add(lap.getTracks().get(indexEachTrackPoint));
		});
		// Adding track points
		newLap.setTracks(leftTrackPoints);
		// Change color
		newLap.setColor(null);
		newLap.setLightColor(null);
		// Proportional calories
		newLap.setCalories(!Objects.isNull(lap.getCalories()) ? new Long(
				Math.round(Double.valueOf(newLap.getTracks().size()) / sizeOfTrackPoints * lap.getCalories()))
						.intValue()
				: null);
		// Start date
		newLap.setStartTime(newLap.getTracks().get(0).getDate());
		LapsUtils.setTotalValuesLap(newLap);
		LapsUtils.calculateAggregateValuesLap(newLap);
	}

	/**
	 * 
	 * @param lap
	 */
	private static void calculateAggregateSpeed(Lap lap) {
		boolean hasSpeedValues = hasLapTrackpointValue(lap, (track) -> !Objects.isNull(track.getSpeed()));
		boolean hasSpeedAggregateValues = !Objects.isNull(lap.getAverageSpeed())
				&& !Objects.isNull(lap.getMaximunSpeed());
		if (hasSpeedValues && !hasSpeedAggregateValues) {
			// Max Speed
			double maxSpeed = getMaxValueTrackPoint(lap, (track1, track2) -> Double
					.compare(track1.getSpeed().doubleValue(), track2.getSpeed().doubleValue())).getSpeed()
							.doubleValue();
			if (!Objects.isNull(lap.getMaximunSpeed()) && lap.getMaximunSpeed().doubleValue() > 0) {
				TrackPoint trackpoint = existsValue(lap,
						track -> track.getSpeed().doubleValue() == lap.getMaximunSpeed().doubleValue());
				if (Objects.isNull(trackpoint)) {
					lap.setMaximunSpeed(maxSpeed);
				}
			} else {
				lap.setMaximunSpeed(maxSpeed);
			}
			// Average Speed
			double avgSpeed = getAvgValueTrackPoint(lap, trackpoint -> trackpoint.getSpeed().doubleValue());
			if (!Objects.isNull(lap.getAverageSpeed()) && lap.getAverageSpeed().doubleValue() > 0
					&& lap.getAverageSpeed().doubleValue() != avgSpeed) {
				lap.setAverageSpeed(avgSpeed);
			} else
				lap.setAverageSpeed(avgSpeed);
		}
	}

	/**
	 * 
	 * @param lap
	 */
	private static void calculateAggregateHeartRate(Lap lap) {
		boolean hasHeartRateValues = hasLapTrackpointValue(lap, (track) -> !Objects.isNull(track.getHeartRateBpm()));
		boolean hasHeartRateAggregateValues = !Objects.isNull(lap.getAverageHearRate())
				&& !Objects.isNull(lap.getMaximunHeartRate());
		// Heart Rate
		if (hasHeartRateValues && !hasHeartRateAggregateValues) {
			// Max Heart Rate
			int maxBpm = getMaxValueTrackPoint(lap,
					(track1, track2) -> Integer.compare(track1.getHeartRateBpm(), track2.getHeartRateBpm()))
							.getHeartRateBpm();
			if (!Objects.isNull(lap.getMaximunHeartRate()) && lap.getMaximunHeartRate().doubleValue() > 0) {
				TrackPoint trackpoint = existsValue(lap,
						track -> track.getHeartRateBpm().intValue() == lap.getMaximunHeartRate().intValue());
				if (trackpoint == null) {
					lap.setMaximunHeartRate(maxBpm);
				}
			} else {
				lap.setMaximunHeartRate(maxBpm);
			}
			// Average heart rate
			double avgHeartRate = getAvgValueTrackPoint(lap, trackpoint -> trackpoint.getHeartRateBpm().doubleValue());
			if (!Objects.isNull(lap.getAverageHearRate()) && lap.getAverageHearRate().doubleValue() > 0
					&& lap.getAverageHearRate().doubleValue() != avgHeartRate) {
				lap.setAverageHearRate(avgHeartRate);
			} else
				lap.setAverageHearRate(avgHeartRate);
		}
	}

	/**
	 * 
	 * @param lap
	 * @param function
	 * @return
	 */
	private static TrackPoint existsValue(Lap lap, Predicate<TrackPoint> predicate) {
		return lap.getTracks().stream().filter(predicate).findAny().orElse(null);
	}

	/**
	 * 
	 * @param lap
	 * @param function
	 * @return
	 */
	private static boolean hasLapTrackpointValue(Lap lap, Function<TrackPoint, Boolean> function) {
		return lap.getTracks().stream().map(function).reduce(Boolean::logicalAnd).orElse(false);
	}

	/**
	 * 
	 * @param lap
	 * @param comparator
	 * @return
	 */
	private static TrackPoint getMaxValueTrackPoint(Lap lap, Comparator<TrackPoint> comparator) {
		TrackPoint trackpoint = lap.getTracks().stream().max(comparator).orElse(null);
		return trackpoint;
	}

	/**
	 * 
	 */
	private static double getAvgValueTrackPoint(Lap lap, ToDoubleFunction<TrackPoint> function) {
		return lap.getTracks().stream().mapToDouble(function).average().orElse(0.0);
	}
}
