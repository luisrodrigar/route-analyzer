package com.routeanalyzer.api.logic.impl;

import com.google.common.base.Predicates;
import com.routeanalyzer.api.common.CommonUtils;
import com.routeanalyzer.api.common.DateUtils;
import com.routeanalyzer.api.common.MathUtils;
import com.routeanalyzer.api.logic.ActivityOperations;
import com.routeanalyzer.api.logic.LapsOperations;
import com.routeanalyzer.api.model.Activity;
import com.routeanalyzer.api.model.Lap;
import com.routeanalyzer.api.model.Position;
import com.routeanalyzer.api.model.TrackPoint;
import org.apache.commons.lang3.SerializationUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.IntStream;

import static java.util.Optional.of;
import static java.util.Optional.ofNullable;
import static com.routeanalyzer.api.common.CommonUtils.toValueOrNull;

@Service
public class ActivityOperationsImpl implements ActivityOperations {

	private LapsOperations lapsOperationsService;

	@Autowired
	public ActivityOperationsImpl(LapsOperations lapsOperations) {
		this.lapsOperationsService = lapsOperations;
	}

	@Override
	public Activity removePoint(Activity act, String lat, String lng, String timeInMillis, String indexTrackPoint) {
	    Function<Position, Integer> getLapIndex = position ->
                indexOfALap(act, position, toValueOrNull(timeInMillis, Long::parseLong),
                        toValueOrNull(indexTrackPoint, Integer::parseInt));
		return CommonUtils.toOptPosition(lat, lng)
				.flatMap(position -> of(position)
						.map(getLapIndex)
                        .filter(MathUtils::isPositiveOrZero)
                        .flatMap(indexLap -> of(indexLap)
                                .map(indexLapParam ->
                                        indexOfTrackPoint(act, indexLapParam, position,
                                                toValueOrNull(timeInMillis, Long::parseLong),
                                                toValueOrNull(indexTrackPoint, Integer::parseInt)))
                                .map(indexTrack -> of(indexTrack)
                                        .flatMap(indexTrackParam -> of(indexLap)
												.map(act.getLaps()::get)
												.map(Lap::getTracks)
												.map(List::size))
                                        .filter(sizeTrackPoints -> sizeTrackPoints > 1)
										.filter(sizeTrackPoints -> ofNullable(indexTrack)
                                                .filter(MathUtils::isPositiveOrZero)
                                                .isPresent())
                                        .map(size -> this.removeGeneralPoint(act, indexLap, indexTrack))
                                        .orElseGet(() -> this.removeLap(act, indexLap)))))
                .orElse(null);
	}

	@Override
	public Activity splitLap(Activity activity, String lat, String lng, String timeInMillis, String indexTrackPoint) {
		Long time = !Objects.isNull(timeInMillis) && !timeInMillis.isEmpty() ? Long.parseLong(timeInMillis) : null;
		Integer index = !Objects.isNull(indexTrackPoint) && !indexTrackPoint.isEmpty()
				? Integer.parseInt(indexTrackPoint) : null;

		Position position = new Position(new BigDecimal(lat), new BigDecimal(lng));

		Integer indexLap = indexOfALap(activity, position, time, index);
		if (indexLap > -1) {
			Integer indexPosition = indexOfTrackPoint(activity, indexLap, position, time, index);
			Lap lap = activity.getLaps().get(indexLap);
			int sizeOfTrackPoints = lap.getTracks().size();

			// Track point not start nor end. Thus, it split into two laps
			if (indexPosition > 0 && indexPosition < sizeOfTrackPoints - 1) {

				Lap lapSplitLeft = SerializationUtils.clone(activity.getLaps().get(indexLap));
				lapsOperationsService.createSplitLap(lap, lapSplitLeft, 0, indexPosition);
				// Index the same original lap
				lapSplitLeft.setIndex(lap.getIndex());

				Lap lapSplitRight = SerializationUtils.clone(activity.getLaps().get(indexLap));
				// lap right: add right elements and the current track point
				// which has index = index position
				lapsOperationsService.createSplitLap(lap, lapSplitRight, indexPosition, sizeOfTrackPoints);
				// Index
				lapSplitRight.setIndex(lap.getIndex() + 1);

				// Increment a point the index field of the next elements of the
				// element to delete
				IntStream.range(indexLap + 1, activity.getLaps().size()).forEach(indexEachLap -> {
					activity.getLaps().get(indexEachLap).setIndex(activity.getLaps().get(indexEachLap).getIndex() + 1);
				});

				// Delete lap before splitting
				activity.getLaps().remove(indexLap.intValue());
				// Adding two new laps
				activity.getLaps().add(indexLap.intValue(), lapSplitLeft);
				activity.getLaps().add((indexLap.intValue() + 1), lapSplitRight);

				return activity;
			} else
				return null;
		} else
			return null;
	}

	/**
	 * The method follow the next steps (in every step it makes sure the objects are not null):
	 * 1st: check left and right index, if the one is located first than the second, if not swapping values.
	 * 2nd: Get the laps with the indexes.
	 * 3rd: Remove from the laps the two laps
	 * 4th: Join the two laps
	 * 5th: decrease the index of the laps in the index right and from now on
	 *
	 * @param activity activity which contains all the data
	 * @param indexLeft index lap located in the left side (just before the right lap)
	 * @param indexRight index lap located in the right side (just after the right lap)
	 * @return activity with the joined laps
	 */
	@Override
	public Activity joinLaps(Activity activity, String indexLeft, String indexRight) {
		Function<Integer[], Integer> toLeftIndex = indexes -> indexes[0];
		Function<Integer[], Integer> toRightIndex = indexes -> indexes[1];
		return getResultIndexLaps(indexLeft, indexRight)
				.flatMap(resultIndexes -> ofNullable(activity)
						.map(Activity::getLaps)
						.flatMap(laps -> ofNullable(resultIndexes)
										.map(toLeftIndex)
										.map(laps::get)
										.flatMap(lapLeftParam -> ofNullable(resultIndexes)
												.map(toRightIndex)
												.map(laps::get)
												.map(lapRightParam -> {
													laps.remove(lapLeftParam);
													laps.remove(lapRightParam);
													return lapRightParam;
												})
												.flatMap(lapRightParam -> ofNullable(
														lapsOperationsService.joinLaps(lapLeftParam, lapRightParam))
														.map(newLap -> {
															ofNullable(resultIndexes)
																.map(toLeftIndex)
																.ifPresent(leftIndex -> laps.add(leftIndex, newLap));
															return newLap;
														})
														.map(newLap ->  {
															ofNullable(resultIndexes)
																.map(toRightIndex)
																.ifPresent(rightIndex ->
																		decreaseIndexFollowingLaps(rightIndex, laps));
															return activity;
														})
												)
										)
								)
				).orElse(activity);
	}

	@Override
	public Activity removeLap(Activity act, Long startTime, Integer indexLap) {
		Predicate<Object> isEqualIndex = ofNullable(indexLap)
				.map(Predicate::isEqual).orElseGet(() -> Predicates.alwaysFalse());
		Predicate<Object> isEqualTimeMillis = ofNullable(startTime)
				.map(Predicate::isEqual).orElseGet(() -> Predicates.alwaysTrue());
		ofNullable(act)
				.map(Activity::getLaps)
				.ifPresent(laps -> ofNullable(laps)
						.map(lapsStream -> lapsStream.stream()
								.filter(lapParam -> ofNullable(lapParam)
										.map(Lap::getIndex)
										.filter(isEqualIndex)
										.isPresent())
								.filter(lapParam -> ofNullable(lapParam)
										.map(Lap::getStartTime)
										.flatMap(DateUtils::toTimeMillis)
										.filter(isEqualTimeMillis)
										.isPresent())
								.findFirst()
								.orElse(null))
						.ifPresent(laps::remove));
		return act;
	}

	@Override
	public int indexOfTrackPoint(Activity activity, Integer indexLap, Position position, Long time, Integer index) {
		Function<TrackPoint, Optional<Integer>> indexOfTrackPoint = trackPoint -> ofNullable(activity)
				.map(Activity::getLaps)
				.flatMap(laps -> ofNullable(indexLap)
						.map(laps::get)
						.map(Lap::getTracks)
						.flatMap(tracks -> ofNullable(trackPoint)
								.map(tracks::indexOf)));
		return ofNullable(activity)
				.map(Activity::getLaps)
				.flatMap(laps -> ofNullable(indexLap)
						.map(laps::get)
						.map(lap -> lapsOperationsService.getTrackPoint(lap, position, time, index)))
				.flatMap(indexOfTrackPoint)
				.orElse(-1);
	}

	private Activity removeLap(Activity activity, Integer indexLap) {
		Function<Lap, Activity> removeLap = lap -> {
			activity.getLaps().remove(lap);
			return activity;
		};
		return ofNullable(indexLap)
				.map(activity.getLaps()::get)
				.map(removeLap)
				.orElse(activity);
	}

	private Activity removeGeneralPoint(Activity activity, Integer indexLap, Integer indexCurrentTrackPoint) {
		Predicate<List<TrackPoint>> isNotLastTrackPoint = trackList -> of(trackList)
				.map(List::size)
				.map(MathUtils::decreaseUnit)
				.filter(lastIndexTrackPoint -> indexCurrentTrackPoint < lastIndexTrackPoint)
				.isPresent();
		return ofNullable(activity)
				.map(Activity::getLaps)
				.flatMap(laps -> ofNullable(indexLap)
						.map(laps::get)
						.map(SerializationUtils::clone)
						.flatMap(newLap -> of(newLap)
								.map(Lap::getTracks)
								.map(tracks -> of(tracks)
										.filter(isNotLastTrackPoint)
										.flatMap(tracksParam -> of(indexCurrentTrackPoint)
												.map(MathUtils::increaseUnit)
												.map(indexPosition -> {
													tracksParam.get(indexPosition.intValue()).setSpeed(null);
													tracksParam.get(indexPosition.intValue()).setDistanceMeters(null);
													return tracksParam;
												}))
										.orElse(tracks))
								.map(tracks -> {
									tracks.remove(indexCurrentTrackPoint.intValue());
									laps.remove(indexLap.intValue());
									laps.add(indexLap.intValue(), newLap);
									lapsOperationsService.resetAggregateValues(newLap);
									lapsOperationsService.resetTotals(newLap);
									calculateDistanceSpeedValues(activity);
									lapsOperationsService.setTotalValuesLap(newLap);
									lapsOperationsService.calculateAggregateValuesLap(newLap);
									return activity;
								})))
				.orElse(activity);
	}

	private Optional<Integer[]> getResultIndexLaps(String index1, String index2) {
		return ofNullable(index1)
				.map(Integer::parseInt)
				.flatMap(indexLeftParam -> ofNullable(index2)
						.map(Integer::parseInt)
						.map((indexRightParam) -> swapValues(indexLeftParam, indexRightParam)));
	}

	private void decreaseIndexFollowingLaps(int fromIndex, List<Lap> laps) {
		int lastIndexExcluded = laps.size();
		IntStream.range(fromIndex, lastIndexExcluded).forEach(indexEachLap -> decreaseIndex(indexEachLap, laps));
	}

	private void decreaseIndex(int indexLap, List<Lap> laps){
		ofNullable(indexLap)
				.map(laps::get)
				.ifPresent(lapToSetIndex ->
						ofNullable(lapToSetIndex)
								.map(Lap::getIndex)
								.map(MathUtils::decreaseUnit)
								.ifPresent(lapToSetIndex::setIndex));
	}

	private Integer[] swapValues(Integer indexLeft, Integer indexRight) {
		return ofNullable(indexLeft)
				.flatMap(indexLeftParam -> ofNullable(indexRight)
						.filter(indexRightParam -> indexLeft.compareTo(indexRight) > 0)
						.map(indexRightParam -> MathUtils.swappingValues(indexLeft, indexRight)))
				.orElseGet(() -> new Integer[]{indexLeft, indexRight});
	}

	public void calculateDistanceSpeedValues(Activity activity) {
		boolean hasActivityDateTrackPointValues = hasActivityTrackPointsValue(activity,
				track -> !Objects.isNull(track.getDate()));
		if (hasActivityDateTrackPointValues) {
			// Check if any track point has no distance value calculate distance
			boolean hasActivityNonDistanceValue = hasActivityTrackPointsValue(activity,
					track -> !Objects.isNull(track.getDistanceMeters()));
			if (!hasActivityNonDistanceValue)
				calculateDistanceMeters(activity);
			// Check if any track point has no speed value
			boolean hasActivityNonSpeedValues = hasActivityTrackPointsValue(activity,
					track -> !Objects.isNull(track.getSpeed()));
			if (!hasActivityNonSpeedValues)
				calculateSpeedValues(activity);
		}
	}

	private void calculateDistanceMeters(Activity activity) {
		List<Lap> laps = activity.getLaps();
		laps.forEach(lap -> {
			int indexLap = laps.indexOf(lap);
			switch (indexLap) {
			case 0:
				lapsOperationsService.calculateDistanceLap(lap, null);
				break;
			default:
				Lap previousLap = laps.get(indexLap - 1);
				lapsOperationsService.calculateDistanceLap(lap,
						previousLap.getTracks().get(previousLap.getTracks().size() - 1));
				break;
			}
		});
	}

	private void calculateSpeedValues(Activity activity) {
		List<Lap> laps = activity.getLaps();
		laps.forEach(lap -> {
			int indexLap = laps.indexOf(lap);
			switch (indexLap) {
			case 0:
				lapsOperationsService.calculateSpeedLap(lap, null);
				break;
			default:
				Lap previousLap = laps.get(indexLap - 1);
				lapsOperationsService.calculateSpeedLap(lap,
						previousLap.getTracks().get(previousLap.getTracks().size() - 1));
				break;
			}
		});
	}

	private boolean hasActivityTrackPointsValue(Activity activity, Function<TrackPoint, Boolean> function) {
		return activity.getLaps().stream()
				.map(lap -> lap.getTracks().stream().map(function).reduce(Boolean::logicalAnd))
				.map(isValue -> isValue.orElse(false)).reduce(Boolean::logicalAnd).orElse(false);
	}

	/**
	 * Method which returns an index corresponding to the lap which contains
	 * track point with the latitude, longitude and ( time or index) contained
	 * in the parameters.
	 * 
	 * @param activity:
	 *            activity
	 * @param position:
	 *            latitude position
	 *            longitude position
	 * @param timeInMillis:
	 *            time in milliseconds
	 * @param index:
	 *            index of the lap in the array
	 * @return index of the lap
	 */
	private int indexOfALap(Activity activity, Position position, Long timeInMillis, Integer index) {
		Predicate<Lap> isThisLap = lap ->
				lapsOperationsService.fulfillCriteriaPositionTime(lap, position, timeInMillis, index);
		return ofNullable(activity)
				.map(Activity::getLaps)
				.flatMap(laps -> laps.stream().filter(isThisLap).findFirst().map(laps::indexOf))
				.orElse(-1);
	}

}
