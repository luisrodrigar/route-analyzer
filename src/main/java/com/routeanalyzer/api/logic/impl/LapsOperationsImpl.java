package com.routeanalyzer.api.logic.impl;

import com.routeanalyzer.api.common.MathUtils;
import com.routeanalyzer.api.logic.LapsOperations;
import com.routeanalyzer.api.logic.TrackPointOperations;
import com.routeanalyzer.api.model.Lap;
import com.routeanalyzer.api.model.TrackPoint;
import com.routeanalyzer.api.services.googlemaps.GoogleMapsApiService;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.SerializationUtils;
import org.apache.commons.lang3.StringUtils;
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

import static com.routeanalyzer.api.common.Constants.COLOR_DELIMITER;
import static com.routeanalyzer.api.common.Constants.STARTED_HEX_CHAR;
import static com.routeanalyzer.api.common.MathUtils.toBigDecimal;
import static java.util.Arrays.asList;
import static java.util.Objects.isNull;
import static java.util.Optional.of;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toList;

@Service
@RequiredArgsConstructor
public class LapsOperationsImpl implements LapsOperations {

	private final GoogleMapsApiService googleMapsService;
	private final TrackPointOperations trackPointOperationsService;

	@Override
	public Lap joinLaps(final Lap lapLeft, final Lap lapRight) {
		// Join the track points of the two laps
		List<TrackPoint> tracks = joinTrackPointLaps(lapLeft, lapRight);

		recalculateJoinedLap(tracks);

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
	private void recalculateJoinedLap(final List<TrackPoint> trackPoints) {
		IntStream.range(trackPoints.get(0).getIndex(), trackPoints.size() + trackPoints.get(0).getIndex())
				.forEach(index -> trackPoints.get(index-trackPoints.get(0).getIndex()).setIndex(index));
	}

	@Override
	public void calculateLapValues(final Lap lap) {
		// Fill altitude fields
		calculateAltitude(lap);
		// Fill aggregate heart rate fields
		calculateAggregateValuesLap(lap);
	}

	@Override
	public void calculateAltitude(final Lap lap) {
		String okStatus = "OK";
		Predicate<Map<String, String>> isOKResponse = elevations -> okStatus.equalsIgnoreCase(elevations.get("status"));
		Predicate<Lap> nonHasAltitudeValues = (lapParam) ->
				lapTrackPointValueHasCondition(lapParam, TrackPoint::getAltitudeMeters, Objects::isNull);
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
												.map(MathUtils::toBigDecimal)
												.ifPresent(trackPointParam::setAltitudeMeters))));
	}

	@Override
	public void calculateDistanceLap(final Lap lap, final TrackPoint previousLapLastTrackPoint) {
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
	public void calculateSpeedLap(final Lap lap, final TrackPoint previousLapLastTrackPoint) {
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
	public boolean fulfillCriteriaPositionTime(final Lap lap, final String latitude, final String longitude,
											   final Long timeInMillis, final Integer index) {
		return getOptLapField(lap, Lap::getTracks)
				.map(tracks -> tracks.stream().anyMatch(track ->
						trackPointOperationsService.isThisTrack(track, latitude, longitude, timeInMillis, index)))
				.orElse(false);
	}

	@Override
	public TrackPoint getTrackPoint(final Lap lap, final String latitude, final String longitude, final Long time,
									final Integer index) {
		return ofNullable(lap)
				.map(Lap::getTracks)
				.map(List::stream)
				.flatMap(trackPointList ->  trackPointList
						.filter(track -> trackPointOperationsService.isThisTrack(track, latitude, longitude, time, index))
						.findFirst())
				.orElse(null);
	}

	@Override
	public Lap createSplitLap(final Lap lap, final int initTrackPointIndex, final int endTrackPointIndex,
							  final int newLapIndex) {
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
						.filter(MathUtils::isPositiveNonZero)
						.flatMap(sizeOfTrackPointsCalories -> ofNullable(newLap)
								.map(Lap::getTracks)
								.map(List::size)
								.map(sizeNewLapTrackPoints -> sizeNewLapTrackPoints / sizeOfTrackPointsCalories )
								.map(Math::round)
								.map(Long::valueOf)
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
					getOptLapField(lap, Lap::getTracks)
							.ifPresent(trackPoints -> of(MathUtils.sortingPositiveValues(initTrackPointIndex,
									endTrackPointIndex))
									.map(indexes -> IntStream.range(indexes.get(0), indexes.get(1))
											.mapToObj(indexEachTrackPoint ->
													trackPoints.get(indexEachTrackPoint)).collect(Collectors.toList()))
									.map(getSetterIndexTrackPoints::apply)
									.map(setTrackPoints -> setTrackPoints.apply(newLap))
									.map(Lap.class::cast)
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
	public void resetTotals(final Lap lap) {
		ofNullable(lap)
				.ifPresent(lapParam -> {
					lapParam.setTotalTimeSeconds(null);
					lapParam.setDistanceMeters(null);
				});
	}

	@Override
	public void resetAggregateValues(final Lap lap) {
		ofNullable(lap)
				.ifPresent(lapParam -> {
					lapParam.setAverageHearRate(null);
					lapParam.setMaximumHeartRate(null);
					lapParam.setAverageSpeed(null);
					lapParam.setMaximumSpeed(null);
				});
	}

	@Override
	public void setTotalValuesLap(final Lap lap) {
		setTotalTimeSecondsLap(lap);
		setTotalDistanceLap(lap);
	}

	@Override
	public void calculateAggregateValuesLap(final Lap lap) {
		calculateAggregateHeartRate(lap);
		calculateAggregateSpeed(lap);
	}

	@Override
	public Lap setColorLap(final Lap lap, final String lapColors) {
		return assignColorToLap(lap, toHexColors(lapColors));
	}

	private List<String> toHexColors(final String lapColors) {
		// [color(hex)-lightColor(hex)] number without #
		Function<String, String> addHexPrefix = color -> STARTED_HEX_CHAR + color;
		return asList(lapColors.split(COLOR_DELIMITER)).stream()
				.map(addHexPrefix)
				.collect(toList());
	}

	private Lap assignColorToLap(final Lap lap, final List<String> colors) {
		lap.setColor(colors.get(0));
		lap.setLightColor(colors.get(1));
		return lap;
	}

	private Predicate<Lap> hasPositionValues = (lapParam) -> lapTrackPointValueHasCondition(lapParam, TrackPoint::getPosition, Objects::nonNull);

	private Predicate<List<TrackPoint>> isNotEmpty = trackPointList -> !trackPointList.isEmpty();

	private Optional<TrackPoint> getLastTrackPoint(final Lap lap){
		return getOptLapField(lap, Lap::getTracks)
				.flatMap(trackPointList -> ofNullable(trackPointList)
						.filter(isNotEmpty)
						.map(List::size)
						.map(size -> ofNullable(size)
								.filter(MathUtils::isPositiveNonZero)
								.map(MathUtils::decreaseUnit)
								.orElse(0))
						.map(trackPointList::get));
	}

	private Optional<TrackPoint> getFirstTrackPoint(final Lap lap){
		return getOptLapField(lap, Lap::getTracks)
				.filter(isNotEmpty)
				.map(trackPointList -> trackPointList.get(0));
	}

	private List<TrackPoint> joinTrackPointLaps(final Lap lapLeft, final Lap lapRight) {
		return Stream.concat(getTrackPoints(lapLeft).stream(), getTrackPoints(lapRight).stream())
				.collect(Collectors.toList());
	}

	private Optional<Double> sumDoubleFieldLaps(final Lap lapLeft, final Lap lapRight,
												final Function<Lap, Double> methodGetter) {
		return Stream.of(getOptLapField(lapLeft, methodGetter), getOptLapField(lapRight, methodGetter))
				.reduce(Optional.empty(), (l1, l2) -> !l1.isPresent() ? l2 : !l2.isPresent() ? l1 : Optional.of(l1.get() + l2.get()));
	}

	private Optional<Integer> sumIntFieldLaps(final Lap lapLeft, final Lap lapRight,
											  final Function<Lap, Integer> methodGetter) {
		return Stream.of(getOptLapField(lapLeft, methodGetter), getOptLapField(lapRight, methodGetter))
				.reduce(Optional.empty(), (l1, l2) -> !l1.isPresent() ? l2 : !l2.isPresent() ? l1 : Optional.of(l1.get() + l2.get()));
	}

	private String getIntensity(final Lap lapLeft, final Lap lapRight) {
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

	private void calculateSplitLapValues(final Lap lap){
		setTotalValuesLap(lap);
		calculateAggregateValuesLap(lap);
	}

	// Reset methods

	private Lap resetValues(final Lap lap) {
		this.resetAggregateValues(lap);
		this.resetTotals(lap);
		lap.setColor(null);
		lap.setLightColor(null);
		return lap;
	}

	private void setTotalDistanceLap(final Lap lap) {
		getLastTrackPoint(lap)
				.map(TrackPoint::getDistanceMeters)
				.flatMap(distanceMetersLast -> getFirstTrackPoint(lap)
								.map(TrackPoint::getDistanceMeters)
								.map(distanceMetersLast::subtract))
				.ifPresent(totalDistance -> ofNullable(totalDistance)
						.map(BigDecimal::doubleValue)
						.ifPresent(lap::setDistanceMeters));
	}

	private void setTotalTimeSecondsLap(final Lap lap) {
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
	private void calculateAggregateSpeed(final Lap lap) {
		ToDoubleFunction<TrackPoint> getSpeedDoubleValue = trackPoint -> trackPoint.getSpeed().doubleValue();
		Comparator<TrackPoint> comparatorSpeed = Comparator.comparingDouble(getSpeedDoubleValue)
				.thenComparingDouble(getSpeedDoubleValue);
		Function<Lap, Optional<Number>> getMaxSpeedLap = lapParam -> getMaxValueTrackPoint(lapParam, comparatorSpeed)
				.map(TrackPoint::getSpeed).map(BigDecimal::doubleValue);
		Function<Lap, OptionalDouble> getAvgSpeedLap = lapParam ->
				getAvgValueTrackPoint(lap, trackPoint -> trackPoint.getSpeed().doubleValue());
		Consumer<Number> setMaxSpeed = maxSpeedNumber ->
				lap.setMaximumSpeed((Double) maxSpeedNumber);
		Consumer<Number> setAvgSpeed = avgSpeedNumber ->
				lap.setAverageSpeed((Double) avgSpeedNumber);

		calculateLapAggregateValue(lap, TrackPoint::getSpeed, getMaxSpeedLap, getAvgSpeedLap, setMaxSpeed, setAvgSpeed);
	}

	/**
	 * 
	 * @param lap
	 */
	private void calculateAggregateHeartRate(final Lap lap) {
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
				lap.setMaximumHeartRate((Integer) maxHeartRateNumber);
		Consumer<Number> setAvgHeartRate = avgHeartRateNumber ->
				lap.setAverageHearRate((Double) avgHeartRateNumber);

		calculateLapAggregateValue(lap, TrackPoint::getHeartRateBpm, getMaxHeartRateLap, getAvgHeartRateLap,
				setMaxHeartRate, setAvgHeartRate);
	}

	private void calculateLapAggregateValue(final Lap lap, Function<TrackPoint, Object> getterValueMethod,
											final Function<Lap, Optional<Number>> getMaxValueLap,
											final Function<Lap, OptionalDouble> getAvgValueLap,
											final Consumer<Number> setMaxValue, Consumer<Number> setAvgValue) {
		// Check if the lap has heart rate data
		Predicate<Lap> hasLapValues = lapParam -> lapTrackPointValueHasCondition(lapParam, getterValueMethod,
				Objects::nonNull);
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
							.filter(OptionalDouble::isPresent)
							.map(OptionalDouble::getAsDouble)
							.ifPresent(setAvgValue);
				});
	}

	/**
	 *
	 * @param lap
	 * @param methodGetter
	 * @param <T> encapsulated in an Optional value
	 * @return
	 */
	private <T> Optional<T> getOptLapField(final Lap lap, final Function<Lap, T> methodGetter) {
		return ofNullable(lap).map(methodGetter);
	}

	private List<TrackPoint> getTrackPoints(final Lap lap) {
		return getOptLapField(lap, Lap::getTracks).orElseGet(Collections::emptyList);
	}

	/**
	 * 
	 * @param lap
	 * @param function
	 * @param condition
	 * @return
	 */
	private boolean lapTrackPointValueHasCondition(final Lap lap,
												   final Function<TrackPoint, Object> function,
												   final Predicate<Object> condition) {
		return getTrackPoints(lap).stream().map(function).allMatch(condition);
	}

	/**
	 * 
	 * @param lap
	 * @param comparator
	 * @return
	 */
	private Optional<TrackPoint> getMaxValueTrackPoint(final Lap lap, final Comparator<TrackPoint> comparator) {
		return getTrackPoints(lap).stream().max(comparator);
	}

	/**
	 * 
	 */
	private OptionalDouble getAvgValueTrackPoint(final Lap lap, final ToDoubleFunction<TrackPoint> function) {
		return getTrackPoints(lap).stream().mapToDouble(function).average();
	}

}
