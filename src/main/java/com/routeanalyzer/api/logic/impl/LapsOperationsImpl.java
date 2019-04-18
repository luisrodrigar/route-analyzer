package com.routeanalyzer.api.logic.impl;

import com.routeanalyzer.api.common.MathUtils;
import com.routeanalyzer.api.logic.LapsOperations;
import com.routeanalyzer.api.logic.TrackPointOperations;
import com.routeanalyzer.api.model.Lap;
import com.routeanalyzer.api.model.Position;
import com.routeanalyzer.api.model.TrackPoint;
import com.routeanalyzer.api.services.googlemaps.GoogleMapsServiceImpl;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.SerializationUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.ToDoubleFunction;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static com.routeanalyzer.api.common.MathUtils.toBigDecimal;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static java.util.Optional.of;
import static java.util.Optional.ofNullable;

@Service
public class LapsOperationsImpl implements LapsOperations {

	@Autowired
	private GoogleMapsServiceImpl googleMapsService;
	@Autowired
	private TrackPointOperations trackPointOperationsService;

	// Index lap splitter
    public static String COMMA_DELIMITER = ",";
	// Lap splitter: @
	public static String LAP_DELIMITER = "@";
	// Color splitter: -
	public static String COLOR_DELIMITER = "-";
	// First char in hexadecimal number: #
	public static String STARTED_HEX_CHAR = "#";

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

		calculateAggregateValuesLap(newLap);

		return newLap;
	}

	@Override
	public void calculateLapValues(Lap lap) {
		// Fill altitude fields
		calculateAltitude(lap);
		// Fill aggregate heart rate fields
		calculateAggregateValuesLap(lap);
	}

	@Override
	public void calculateAltitude(Lap lap) {
		String okStatus = "OK";
		Predicate<Map<String, String>> isOKResponse = elevations -> okStatus.equalsIgnoreCase(elevations.get("status"));
		Predicate<Lap> nonHasAltitudeValues = (lapParam) -> hasLapTrackPointValue(lapParam,
				track -> isNull(track.getAltitudeMeters()));
		ofNullable(lap)
				.filter(nonHasAltitudeValues)
				.filter(hasPositionValues)
				.map(Lap::getTracks)
				.map(googleMapsService::createPositionsRequest)
				.filter(StringUtils::isNotEmpty)
				.map(googleMapsService::getAltitude)
				.filter(isOKResponse)
				.ifPresent(elevations -> lap.getTracks().forEach(trackPoint ->
						ofNullable(trackPoint)
								.filter(trackPointParam ->  isNull(trackPointParam.getAltitudeMeters()))
								.ifPresent(trackPointParam ->
										ofNullable(trackPointParam)
												.map(googleMapsService::getCoordinatesCode)
												.map(elevations::get)
												.flatMap(MathUtils::toBigDecimal)
												.ifPresent(trackPointParam::setAltitudeMeters))));
	}

	@Override
	public void calculateDistanceLap(Lap lap, TrackPoint previousLapLastTrackPoint) {
		ofNullable(lap)
				.map(Lap::getTracks)
				.ifPresent(trackPoints -> trackPoints.forEach(trackPoint ->
						ofNullable(trackPoint)
								.map(trackPoints::indexOf)
								.map(indexTrackPointParam ->
										ofNullable(
												ofNullable(indexTrackPointParam)
														.map(MathUtils::decreaseUnit)
														.filter(MathUtils::isPositiveOrZero)
														.map(trackPoints::get)
														.orElse(previousLapLastTrackPoint)
										).flatMap(previousTrack ->
												ofNullable(previousTrack)
														.map(TrackPoint::getDistanceMeters)
														.map(BigDecimal::doubleValue)
														.map(distance -> distance +
																trackPointOperationsService
																		.calculateDistance(previousTrack, trackPoint))
														.map(MathUtils::toBigDecimal)
										).orElseGet(() -> toBigDecimal(0.0))
								).ifPresent(trackPoint::setDistanceMeters)
				));
	}

	@Override
	public void calculateSpeedLap(Lap lap, TrackPoint previousLapLastTrackPoint) {
		ofNullable(lap)
				.map(Lap::getTracks)
				.ifPresent(trackPoints -> trackPoints.forEach(trackPoint ->
						ofNullable(trackPoint)
								.map(trackPoints::indexOf)
								.map(indexTrackPoint ->
												ofNullable(
														ofNullable(indexTrackPoint)
																.map(MathUtils::decreaseUnit)
																.filter(MathUtils::isPositiveOrZero)
																.map(trackPoints::get)
																.orElse(previousLapLastTrackPoint)
												).flatMap(previousTrack -> ofNullable(previousTrack)
														.map(previousTrackParam ->
																trackPointOperationsService
																		.calculateSpeed(previousTrackParam, trackPoint))
														.map(MathUtils::toBigDecimal)
												).orElseGet(() -> toBigDecimal(0.0))
								).ifPresent(trackPoint::setSpeed)
				));
		setTotalValuesLap(lap);
		calculateAggregateSpeed(lap);
	}

	@Override
	public boolean fulfillCriteriaPositionTime(Lap lap, Position positionParam, Long timeInMillis, Integer index) {
		return getLapField(lap, Lap::getTracks)
				.flatMap(tracks ->
						ofNullable(positionParam)
								.map(position ->
										tracks.stream().anyMatch(track ->
												trackPointOperationsService
														.isThisTrack(track, position, timeInMillis, index)))
				).orElse(false);
	}

	@Override
	public TrackPoint getTrackPoint(Lap lap, Position position, Long time, Integer index) {
		return ofNullable(lap)
				.map(Lap::getTracks)
				.map(List::stream)
				.flatMap(trackPointList ->  trackPointList
						.filter(track -> trackPointOperationsService.isThisTrack(track, position, time, index))
						.findFirst())
				.orElse(null);
	}

	@Override
	public Lap createSplitLap(Lap lap, int initTrackPointIndex, int endTrackPointIndex, int newLapIndex) {
		Function<List<TrackPoint>, Function<Lap, Lap>> getSetterIndexTrackPoints = trackPoints -> newLap -> {
			ofNullable(newLap)
					.ifPresent(newLapParam -> ofNullable(trackPoints)
							.ifPresent(newLapParam::setTracks));
			return newLap;
		};
		Function<Lap, Lap> setStartTime = lapParam -> {
			getFirstTrackPoint(lapParam)
					.map(TrackPoint::getDate)
					.ifPresent(lapParam::setStartTime);
			return lapParam;
		};
		Function<List<TrackPoint>, Function<Lap, Lap>> getCalculatorCalories = trackPoints -> newLap ->
				ofNullable(trackPoints)
				.map(List::size)
				.filter(MathUtils::isPositiveNonZero)
				.flatMap(sizeOfTrackPoints -> ofNullable(lap)
						.map(Lap::getCalories)
						.map(calories -> calories * sizeOfTrackPoints))
				.flatMap(sizeOfTrackPointsCalories -> ofNullable(newLap)
						.map(Lap::getTracks)
						.map(List::size)
						.map(sizeNewLapTrackPoints -> sizeNewLapTrackPoints / sizeOfTrackPointsCalories )
						.map(Math::round)
						.map(Long::new)
						.map(Long::intValue))
				.map(calories -> {
					newLap.setCalories(calories);
					return newLap;
				})
				.orElse(null);
		return ofNullable(lap)
				.map(SerializationUtils::clone)
				.map(newLap -> {
					newLap.setIndex(newLapIndex);
					getLapField(lap, Lap::getTracks)
							.ifPresent(trackPoints -> ofNullable(
									MathUtils.sortingPositiveValues(initTrackPointIndex, endTrackPointIndex))
									.filter(ArrayUtils::isNotEmpty)
									.map(indexes -> IntStream.range(indexes[0], indexes[1])
											.mapToObj(indexEachTrackPoint ->
													trackPoints.get(indexEachTrackPoint)).collect(Collectors.toList()))
									.map(getSetterIndexTrackPoints::apply)
									.map(setTrackPoints -> setTrackPoints.apply(newLap))
									.map(this::resetValues)
									.map(newLapParam -> ofNullable(trackPoints)
													.map(getCalculatorCalories::apply)
													.map(calculateCalories -> calculateCalories.apply(newLap))
													.orElse(newLap))
									.map(setStartTime)
									.ifPresent(this::calculateSplitLapValues));
					return newLap;
				}).orElse(null);
	}

	@Override
	public void resetTotals(Lap lap) {
		ofNullable(lap)
				.ifPresent(lapParam -> {
					lapParam.setTotalTimeSeconds(null);
					lapParam.setDistanceMeters(null);
				});
	}

	@Override
	public void resetAggregateValues(Lap lap) {
		ofNullable(lap)
				.ifPresent(lapParam -> {
					lapParam.setAverageHearRate(null);
					lapParam.setMaximumHeartRate(null);
					lapParam.setAverageSpeed(null);
					lapParam.setMaximumSpeed(null);
				});
	}

	@Override
	public void setTotalValuesLap(Lap lap) {
		setTotalTimeSecondsLap(lap);
		setTotalDistanceLap(lap);
	}

	@Override
	public void calculateAggregateValuesLap(Lap lap) {
		calculateAggregateHeartRate(lap);
		calculateAggregateSpeed(lap);
	}

	private Predicate<Lap> hasPositionValues = (lapParam) -> hasLapTrackPointValue(lapParam,
			track -> nonNull(track.getPosition()));

	private Predicate<List<TrackPoint>> isNotEmpty = trackPointList -> !trackPointList.isEmpty();

	private Optional<TrackPoint> getLastTrackPoint(Lap lap){
		return getLapField(lap, Lap::getTracks)
				.flatMap(trackPointList -> ofNullable(trackPointList)
						.filter(isNotEmpty)
						.map(List::size)
						.map(size -> ofNullable(size)
								.filter(MathUtils::isPositiveNonZero)
								.map(MathUtils::decreaseUnit)
								.orElse(0))
						.map(trackPointList::get));
	}

	private Optional<TrackPoint> getFirstTrackPoint(Lap lap){
		Function<List<TrackPoint>, TrackPoint> firstTrackPoint = trackPointList -> trackPointList.get(0);
		return getLapField(lap, Lap::getTracks)
				.filter(isNotEmpty)
				.map(firstTrackPoint);
	}

	private List<TrackPoint> joinTrackPointLaps(Lap lapLeft, Lap lapRight) {
		return Stream
				.concat(ofNullable(lapLeft).map(Lap::getTracks).orElseGet(Collections::emptyList).stream(),
						ofNullable(lapRight).map(Lap::getTracks).orElseGet(Collections::emptyList).stream())
				.collect(Collectors.toList());
	}

	private Optional<Double> sumDoubleFieldLaps(Lap lapLeft, Lap lapRight, Function<Lap, Double> methodGetter) {
		return Stream.of(getLapField(lapLeft, methodGetter), getLapField(lapRight, methodGetter))
				.reduce(Optional.empty(), (l1, l2) -> !l1.isPresent() ? l2 : !l2.isPresent() ? l1 : Optional.of(l1.get() + l2.get()));
	}

	private Optional<Integer> sumIntFieldLaps(Lap lapLeft, Lap lapRight, Function<Lap, Integer> methodGetter) {
		return Stream.of(getLapField(lapLeft, methodGetter), getLapField(lapRight, methodGetter))
				.reduce(Optional.empty(), (l1, l2) -> !l1.isPresent() ? l2 : !l2.isPresent() ? l1 : Optional.of(l1.get() + l2.get()));
	}

	private String getIntensity(Lap lapLeft, Lap lapRight) {
		return ofNullable(lapLeft)
				.map(Lap::getIntensity)
				.map(intensityLapLeft -> ofNullable(lapRight)
						.map(Lap::getIntensity)
						.map(intensityLapRight -> {
							if(intensityLapLeft.equalsIgnoreCase(intensityLapRight))
								return intensityLapLeft;
							else
							if (lapLeft.getDistanceMeters() > lapRight.getDistanceMeters())
								return intensityLapLeft;
							else
								return intensityLapRight;
						}).orElse(intensityLapLeft))
				.orElseGet(() -> ofNullable(lapRight)
						.map(Lap::getIntensity)
						.orElse(null));
	}

	private void calculateSplitLapValues(Lap lap){
		setTotalValuesLap(lap);
		calculateAggregateValuesLap(lap);
	}

	// Reset methods

	private Lap resetValues(Lap lap) {
		this.resetAggregateValues(lap);
		this.resetTotals(lap);
		lap.setColor(null);
		lap.setLightColor(null);
		return lap;
	}

	private void setTotalDistanceLap(Lap lap) {
		getLastTrackPoint(lap)
				.map(TrackPoint::getDistanceMeters)
				.flatMap(distanceMetersLast -> getFirstTrackPoint(lap)
								.map(TrackPoint::getDistanceMeters)
								.map(distanceMetersLast::subtract))
				.ifPresent(totalDistance -> ofNullable(totalDistance)
						.map(BigDecimal::doubleValue)
						.ifPresent(lap::setDistanceMeters));
	}

	private void setTotalTimeSecondsLap(Lap lap) {
		getLastTrackPoint(lap)
				.map(TrackPoint::getDate)
				.flatMap(timeMillisLastTrack -> getFirstTrackPoint(lap)
						.map(TrackPoint::getDate)
						.map(timeMillisFirstTrack -> Duration.between(timeMillisFirstTrack, timeMillisLastTrack))
						.map(Duration::toNanos)
						.map(Double::valueOf)
						.map(nanoseconds -> nanoseconds / 1E9))
				.ifPresent(totalTimeSeconds -> ofNullable(totalTimeSeconds)
						.ifPresent(lap::setTotalTimeSeconds));
	}

	/**
	 * 
	 * @param lap
	 */
	private void calculateAggregateSpeed(Lap lap) {
		ToDoubleFunction<TrackPoint> getSpeedDoubleValue = trackPoint -> trackPoint.getSpeed().doubleValue();
		Comparator<TrackPoint> comparatorSpeed = Comparator.comparingDouble(getSpeedDoubleValue)
				.thenComparingDouble(getSpeedDoubleValue);
		Function<Lap, Optional<Number>> getMaxSpeedLap = lapParam -> getMaxValueTrackPoint(lapParam, comparatorSpeed)
				.map(TrackPoint::getSpeed).map(BigDecimal::doubleValue);
		Function<Lap, OptionalDouble> getAvgSpeedLap = lapParam ->
				getAvgValueTrackPoint(lap, trackPoint -> trackPoint.getSpeed().doubleValue());
		Consumer<Number> setMaxSpeed = maxSpeedNumber ->
				lap.setMaximumSpeed(Double.class.cast(maxSpeedNumber));
		Consumer<Number> setAvgSpeed = avgSpeedNumber ->
				lap.setAverageSpeed(Double.class.cast(avgSpeedNumber));

		calculateLapAggregateValue(lap, TrackPoint::getSpeed, getMaxSpeedLap, getAvgSpeedLap, setMaxSpeed, setAvgSpeed);
	}

	/**
	 * 
	 * @param lap
	 */
	private void calculateAggregateHeartRate(Lap lap) {
		// Heart rate comparator
		Comparator<TrackPoint> comparatorHeartRate = Comparator.comparingInt(TrackPoint::getHeartRateBpm)
				.thenComparingInt(TrackPoint::getHeartRateBpm);
		// Get max average heart rate value
		Function<Lap, Optional<Number>> getMaxHeartRateLap = lapParam ->
				getMaxValueTrackPoint(lap, comparatorHeartRate).map(TrackPoint::getHeartRateBpm);
		// Get average heart rate value
		Function<Lap, OptionalDouble> getAvgHeartRateLap = lapParam ->
				getAvgValueTrackPoint(lap, trackPoint -> trackPoint.getHeartRateBpm().doubleValue());
		Consumer<Number> setMaxHeartRate = maxHeartRateNumber ->
				lap.setMaximumHeartRate(Integer.class.cast(maxHeartRateNumber));
		Consumer<Number> setAvgHeartRate = avgHeartRateNumber ->
				lap.setAverageHearRate(Double.class.cast(avgHeartRateNumber));

		calculateLapAggregateValue(lap, TrackPoint::getHeartRateBpm, getMaxHeartRateLap, getAvgHeartRateLap,
				setMaxHeartRate, setAvgHeartRate);
	}

	private void calculateLapAggregateValue(Lap lap, Function<TrackPoint, Number> getterValueMethod,
											Function<Lap, Optional<Number>> getMaxValueLap,
											Function<Lap, OptionalDouble> getAvgValueLap,
											Consumer<Number> setMaxValue, Consumer<Number> setAvgValue) {
		// Check if the lap has heart rate data
		Predicate<Lap> hasLapValues = lapParam ->
				hasLapTrackPointValue(lapParam, (track) -> ofNullable(track)
						.map(getterValueMethod).map(Objects::nonNull).orElse(false));
		// Check if the lap has aggregate heart rate values such as average or maximum.
		// Heart Rate
		ofNullable(lap)
				.filter(hasLapValues)
				.ifPresent(lapParam -> {
					ofNullable(lapParam)
							.flatMap(getMaxValueLap)
							.ifPresent(setMaxValue);
					ofNullable(lapParam)
							.map(getAvgValueLap)
							.map(OptionalDouble::getAsDouble)
							.ifPresent(setAvgValue);
				});
	}

	/**
	 *
	 * @param lap
	 * @param methodGetter
	 * @param <T>
	 * @return
	 */
	private <T> Optional<T> getLapField(Lap lap, Function<Lap, T> methodGetter) {
		return ofNullable(lap).map(methodGetter);
	}

	/**
	 * 
	 * @param lap
	 * @param function
	 * @return
	 */
	private boolean hasLapTrackPointValue(Lap lap, Function<TrackPoint, Boolean> function) {
		return getLapField(lap, Lap::getTracks).orElseGet(Collections::emptyList)
					.stream().map(function).reduce(Boolean::logicalAnd).orElse(false);
	}

	/**
	 * 
	 * @param lap
	 * @param comparator
	 * @return
	 */
	private Optional<TrackPoint> getMaxValueTrackPoint(Lap lap, Comparator<TrackPoint> comparator) {
		return getLapField(lap, Lap::getTracks).orElseGet(Collections::emptyList)
					.stream().max(comparator);
	}

	/**
	 * 
	 */
	private OptionalDouble getAvgValueTrackPoint(Lap lap, ToDoubleFunction<TrackPoint> function) {
		return getLapField(lap, Lap::getTracks).orElseGet(Collections::emptyList)
				.stream().mapToDouble(function).average();
	}

}
