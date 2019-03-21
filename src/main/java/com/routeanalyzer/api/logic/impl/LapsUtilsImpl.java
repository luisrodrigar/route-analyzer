package com.routeanalyzer.api.logic.impl;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.ToDoubleFunction;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import com.routeanalyzer.api.common.CommonUtils;
import com.routeanalyzer.api.logic.LapsUtils;
import com.routeanalyzer.api.logic.TrackPointUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.routeanalyzer.api.model.Activity;
import com.routeanalyzer.api.model.Lap;
import com.routeanalyzer.api.model.Position;
import com.routeanalyzer.api.model.TrackPoint;
import com.routeanalyzer.api.services.googlemaps.GoogleMapsServiceImpl;

import static com.routeanalyzer.api.common.CommonUtils.toTimeMillis;
import static java.util.Optional.ofNullable;

@Service
public class LapsUtilsImpl implements LapsUtils {

	@Autowired
	private GoogleMapsServiceImpl googleMapsService;
	@Autowired
	private TrackPointUtils trackpointUtilsService;

	@Override
	public Lap joinLaps(Lap lapLeft, Lap lapRight) {
		// Join the track points of the two laps
		List<TrackPoint> tracks = joinTrackPointLaps(lapLeft, lapRight);

		Lap newLap = Lap.builder()
				// Tracks joined
				.tracks(tracks)
				// Start time is the first left lap's track point time
				.startTime(tracks.get(0).getDate())
				// Calories are the total sum
				.calories(sumIntFieldLaps(lapLeft, lapRight, Lap::getCalories).orElse(null))
				// Total time seconds
				.totalTimeSeconds(sumDoubleFieldLaps(lapLeft, lapRight, Lap::getTotalTimeSeconds).orElse(null))
				// Total distance
				.distanceMeters(sumDoubleFieldLaps(lapLeft, lapRight, Lap::getDistanceMeters).orElse(null))
				// Index of the left lap
				.index(lapLeft.getIndex())
				// Intensity
				.intensity(getIntensity(lapLeft, lapRight)).build();

		calculateAggregateHeartRate(newLap);
		calculateAggregateSpeed(newLap);

		return newLap;
	}

	private List<TrackPoint> joinTrackPointLaps(Lap lapLeft, Lap lapRight) {
		return Stream
				.concat(Optional.ofNullable(lapLeft).map(Lap::getTracks).orElseGet(Collections::emptyList).stream(),
						Optional.ofNullable(lapRight).map(Lap::getTracks).orElseGet(Collections::emptyList).stream())
				.collect(Collectors.toList());
	}

	private Optional<Double> sumDoubleFieldLaps(Lap lapLeft, Lap lapRight, Function<Lap, Double> methodGetter) {
		return Stream.of(getValue(lapLeft, methodGetter), getValue(lapRight, methodGetter))
				.reduce(Optional.<Double>empty(), (l1, l2) -> !l1.isPresent() ? l2 : !l2.isPresent() ? l1 : Optional.of(l1.get() + l2.get()));
	}

	private Optional<Integer> sumIntFieldLaps(Lap lapLeft, Lap lapRight, Function<Lap, Integer> methodGetter) {
		return Stream.of(getValue(lapLeft, methodGetter), getValue(lapRight, methodGetter))
				.reduce(Optional.<Integer>empty(), (l1, l2) -> !l1.isPresent() ? l2 : !l2.isPresent() ? l1 : Optional.of(l1.get() + l2.get()));
	}

	private <T> Optional<T> getValue(Lap lap, Function<Lap, T> methodGetter) {
		return Optional.ofNullable(lap).map(methodGetter);
	}

	private String getIntensity(Lap lapLeft, Lap lapRight) {
		return !Objects.isNull(lapLeft.getIntensity())
				? (!Objects.isNull(lapRight.getIntensity())
						? (lapLeft.getIntensity().equals(lapRight.getIntensity()) ? lapLeft.getIntensity()
								: (lapLeft.getDistanceMeters() > lapRight.getDistanceMeters() ? lapLeft.getIntensity()
										: lapRight.getIntensity()))
						: (lapLeft.getIntensity()))
				: (!Objects.isNull(lapRight.getIntensity()) ? (lapRight.getIntensity()) : null);
	}

	@Override
	public void calculateLapValues(Lap lap) {
		// Fill altitude fields
		calculateAltitude(lap);
		// Fill aggregate heart rate fields
		calculateAggregateHeartRate(lap);
		boolean hasSpeedValues = hasLapTrackpointValue(lap, track -> !Objects.isNull(track.getSpeed()));
		if (hasSpeedValues)
			calculateAggregateSpeed(lap);
	}

	@Override
	public void resetAggregateValues(Lap lap) {
		lap.setAverageHearRate(null);
		lap.setMaximumHeartRate(null);
		lap.setAverageSpeed(null);
		lap.setMaximumSpeed(null);
	}

	@Override
	public void setTotalValuesLap(Lap lap) {
		Optional<TrackPoint> optLastTrackPoint = getLastTrackPoint(lap);
		optLastTrackPoint
				.map(TrackPoint::getDate)
				.map(lastLocalDateTimeTrackPoint ->
						toTimeMillis(lastLocalDateTimeTrackPoint).orElse(0L) -
								toTimeMillis(lap.getTracks().get(0).getDate()).orElse(0L))
				.map(CommonUtils::millisToSeconds)
				.ifPresent(totalTimeSeconds -> lap.setTotalTimeSeconds(totalTimeSeconds));
		optLastTrackPoint
				.map(TrackPoint::getDistanceMeters)
				.map(lastDistanceMetersTrackPoint ->
						lastDistanceMetersTrackPoint.doubleValue() -
								lap.getTracks().get(0).getDistanceMeters().doubleValue())
				.ifPresent(distanceLap -> lap.setDistanceMeters(distanceLap));
	}

	private Optional<TrackPoint> getLastTrackPoint(Lap lap){
		Function<Integer, Integer> decreaseUnit = (number) -> number - 1;
		return ofNullable(lap)
				.map(Lap::getTracks)
				.map(List::size)
				.map(decreaseUnit)
				.map(lap.getTracks()::get);
	}

	@Override
	public void resetTotals(Lap lap) {
		lap.setTotalTimeSeconds(null);
		lap.setDistanceMeters(null);
	}

	@Override
	public void calculateAltitude(Lap lap) {
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
				Map<String, String> elevations = googleMapsService.getAltitude(positions);
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

	@Override
	public void calculateDistanceLap(Lap lap, TrackPoint previousLapLastTrackpoint) {
		lap.getTracks().forEach(track -> {
			int indexTrack = lap.getTracks().indexOf(track);
			double distance = 0.0;
			switch (indexTrack) {
			case 0:
				if (!Objects.isNull(previousLapLastTrackpoint)) {
					distance = trackpointUtilsService.calculateDistance(track.getPosition(),
							previousLapLastTrackpoint.getPosition());
					track.setDistanceMeters(
							new BigDecimal(previousLapLastTrackpoint.getDistanceMeters().doubleValue() + distance));
				} else
					track.setDistanceMeters(new BigDecimal(0.0));
				break;
			default:
				TrackPoint previousTrackpoint = lap.getTracks().get(indexTrack - 1);
				distance = trackpointUtilsService.calculateDistance(previousTrackpoint.getPosition(),
						track.getPosition());
				track.setDistanceMeters(
						new BigDecimal(previousTrackpoint.getDistanceMeters().doubleValue() + distance));
				break;
			}
		});
	}

	@Override
	public void calculateSpeedLap(Lap lap, TrackPoint previousLapLastTrackpoint) {
		lap.getTracks().forEach(track -> {
			int indexTrack = lap.getTracks().indexOf(track);
			double speed = 0.0;
			if (Objects.isNull(track.getSpeed())) {
				switch (indexTrack) {
				case 0:
					speed = trackpointUtilsService.calculateSpeed(previousLapLastTrackpoint, track);
					track.setSpeed(new BigDecimal(speed));
					break;
				default:
					TrackPoint previousTrackpoint = lap.getTracks().get(indexTrack - 1);
					speed = trackpointUtilsService.calculateSpeed(previousTrackpoint, track);
					track.setSpeed(new BigDecimal(speed));
					break;
				}
			}
		});
		setTotalValuesLap(lap);
		calculateAggregateSpeed(lap);
	}

	@Override
	public boolean fulfillCriteriaPositionTime(Lap lap, Position positionParam, Long timeInMillis, Integer index) {
		return ofNullable(lap)
				.map(Lap::getTracks)
				.flatMap(tracks ->
						ofNullable(positionParam).map(position ->
								tracks.stream().anyMatch(track ->
										trackpointUtilsService.isThisTrack(track, position, timeInMillis, index)
								)
						)
				).orElse(false);
	}

	@Override
	public int indexOfTrackpoint(Activity activity, Integer indexLap, Position position, Long time, Integer index) {
		TrackPoint trackPoint = activity.getLaps().get(indexLap).getTracks().stream().filter(track -> {
			return trackpointUtilsService.isThisTrack(track, position, time, index);
		}).findFirst().orElse(null);
		return !Objects.isNull(trackPoint) ? activity.getLaps().get(indexLap).getTracks().indexOf(trackPoint) : -1;
	}

	@Override
	public void calculateAggregateValuesLap(Lap lap) {
		calculateAggregateHeartRate(lap);
		calculateAggregateSpeed(lap);
	}

	@Override
	public void createSplittLap(Lap lap, Lap newLap, int initIndex, int endIndex) {
		int sizeOfTrackPoints = lap.getTracks().size();
		resetAggregateValues(newLap);
		resetTotals(newLap);
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
		setTotalValuesLap(newLap);
		calculateAggregateValuesLap(newLap);
	}

	/**
	 * 
	 * @param lap
	 */
	private void calculateAggregateSpeed(Lap lap) {
		boolean hasSpeedValues = hasLapTrackpointValue(lap, (track) -> !Objects.isNull(track.getSpeed()));
		boolean hasSpeedAggregateValues = !Objects.isNull(lap.getAverageSpeed())
				&& !Objects.isNull(lap.getMaximumSpeed());
		if (hasSpeedValues && !hasSpeedAggregateValues) {
			// Max Speed
			Optional<Double> maxSpeed = getMaxValueTrackPoint(lap, (track1, track2) -> Double
					.compare(track1.getSpeed().doubleValue(), track2.getSpeed().doubleValue())).map(TrackPoint::getSpeed)
							.map(BigDecimal::doubleValue);
			if (!Objects.isNull(lap.getMaximumSpeed()) && lap.getMaximumSpeed().doubleValue() > 0) {
				Optional<TrackPoint> trackpoint = existsValue(lap,
						track -> track.getSpeed().doubleValue() == lap.getMaximumSpeed().doubleValue());
				maxSpeed.filter(speed -> !trackpoint.isPresent()).ifPresent(lap::setMaximumSpeed);
			} else {
				lap.setMaximumSpeed(maxSpeed.orElse(null));
			}
			// Average Speed
			OptionalDouble avgSpeed = getAvgValueTrackPoint(lap, trackpoint -> trackpoint.getSpeed().doubleValue());
			lap.setAverageSpeed(avgSpeed.isPresent() ? avgSpeed.getAsDouble() : null);
		}
	}

	/**
	 * 
	 * @param lap
	 */
	private void calculateAggregateHeartRate(Lap lap) {
		boolean hasHeartRateValues = hasLapTrackpointValue(lap, (track) -> !Objects.isNull(track.getHeartRateBpm()));
		boolean hasHeartRateAggregateValues = !Objects.isNull(lap.getAverageHearRate())
				&& !Objects.isNull(lap.getMaximumHeartRate());
		// Heart Rate
		if (hasHeartRateValues && !hasHeartRateAggregateValues) {
			// Max Heart Rate
			Optional<Integer> maxBpm = getMaxValueTrackPoint(lap,
					(track1, track2) -> Integer.compare(track1.getHeartRateBpm(), track2.getHeartRateBpm()))
							.map(TrackPoint::getHeartRateBpm);
			if (!Objects.isNull(lap.getMaximumHeartRate()) && lap.getMaximumHeartRate().doubleValue() > 0) {
				Optional<TrackPoint> trackpoint = existsValue(lap,
						track -> track.getHeartRateBpm().intValue() == lap.getMaximumHeartRate().intValue());
				maxBpm.filter(bpm -> !trackpoint.isPresent()).ifPresent(lap::setMaximumHeartRate);
			} else {
				lap.setMaximumHeartRate(maxBpm.orElse(null));
			}
			// Average heart rate
			OptionalDouble avgHeartRate = getAvgValueTrackPoint(lap, trackpoint -> trackpoint.getHeartRateBpm().doubleValue());
			lap.setAverageHearRate(avgHeartRate.isPresent() ? avgHeartRate.getAsDouble() : null);
		}
	}

	/**
	 * 
	 * @param lap
	 * @param predicate
	 * @return
	 */
	private Optional<TrackPoint> existsValue(Lap lap, Predicate<TrackPoint> predicate) {
		return getValue(lap, Lap::getTracks).orElseGet(Collections::emptyList)
					.stream().filter(predicate).findAny();
	}

	/**
	 * 
	 * @param lap
	 * @param function
	 * @return
	 */
	private boolean hasLapTrackpointValue(Lap lap, Function<TrackPoint, Boolean> function) {
		return getValue(lap, Lap::getTracks).orElseGet(Collections::emptyList)
					.stream().map(function).reduce(Boolean::logicalAnd).orElse(false);
	}

	/**
	 * 
	 * @param lap
	 * @param comparator
	 * @return
	 */
	private Optional<TrackPoint> getMaxValueTrackPoint(Lap lap, Comparator<TrackPoint> comparator) {
		return getValue(lap, Lap::getTracks).orElseGet(Collections::emptyList)
					.stream().max(comparator);
	}

	/**
	 * 
	 */
	private OptionalDouble getAvgValueTrackPoint(Lap lap, ToDoubleFunction<TrackPoint> function) {
		return getValue(lap, Lap::getTracks).orElseGet(Collections::emptyList)
				.stream().mapToDouble(function).average();
	}
}
