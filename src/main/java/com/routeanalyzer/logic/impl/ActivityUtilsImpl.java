package com.routeanalyzer.logic.impl;

import com.routeanalyzer.logic.ActivityUtils;
import com.routeanalyzer.logic.LapsUtils;
import com.routeanalyzer.model.Activity;
import com.routeanalyzer.model.Lap;
import com.routeanalyzer.model.Position;
import com.routeanalyzer.model.TrackPoint;
import com.routeanalyzer.services.reader.GPXService;
import com.routeanalyzer.services.reader.TCXService;
import org.apache.commons.lang3.SerializationUtils;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.ZoneId;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.IntStream;

import static java.util.Optional.ofNullable;

@Service
public class ActivityUtilsImpl implements ActivityUtils {

	private LapsUtils lapsUtilsService;

	public ActivityUtilsImpl(GPXService gpxService, TCXService tcxService, LapsUtils lapsUtils) {
		this.lapsUtilsService = lapsUtils;
	}

	@Override
	public Activity removePoint(Activity act, String lat, String lng, String timeInMillis, String indexTrackPoint) {
//		Predicate<Integer> nonNegative = num -> num >= 0;
//		ofNullable(toPosition(lat, lng))
//				.flatMap(position ->
//						ofNullable(act)
//								.flatMap(activity ->
//										ofNullable(timeInMillis)
//												.filter(StringUtils::isNotEmpty)
//												.map(Long::parseLong)
//												.flatMap(time -> ofNullable(indexTrackPoint)
//														.filter(StringUtils::isNotEmpty)
//														.map(Integer::parseInt)
//														.flatMap(index -> ofNullable(index)
//																.map(indexModel -> indexOfALap(activity, position, time, indexModel))
//																.flatMap(indexLap -> ofNullable(indexLap)
//																		.map(indexLapParam ->
//																				lapsUtilsService.indexOfTrackpoint(activity, indexLap, position, time, index))
//																		.flatMap(indexTrack -> ofNullable(indexTrack)
//																				.map(indexTrackParam -> activity.getLaps().get(indexLap).getTracks().size())
//																				.filter(size -> indexTrack > 0 && size > 1))
//																				.map(size -> SerializationUtils.clone(activity.getLaps().get(indexLap)))
//																				.filter(newLap -> )
//
//																)
//																.map(indexLap ->
//																		)
//																.flatMap(indexPosition -> ofNullable()))
//												)
//								)
//				)
//				.orElse(null);
		Long time = !Objects.isNull(timeInMillis) && !timeInMillis.isEmpty() ? Long.parseLong(timeInMillis) : null;
		Integer index = !Objects.isNull(indexTrackPoint) && !indexTrackPoint.isEmpty()
				? Integer.parseInt(indexTrackPoint) : null;

		Position position = new Position(new BigDecimal(lat), new BigDecimal(lng));
		// Remove element from the laps stored in state
		// If it is not at the begining or in the end of a lap
		// the lap splits into two new ones.
		Integer indexLap = indexOfALap(act, position, time, index);
		if (indexLap > -1) {
			Integer indexPosition = lapsUtilsService.indexOfTrackpoint(act, indexLap, position, time, index);
			int sizeOfTrackPoints = act.getLaps().get(indexLap).getTracks().size();

			// The lap is only one track point.
			// Delete both lap and track point. No recalculations.
			if (indexPosition == 0 && sizeOfTrackPoints == 1) {
				act.getLaps().get(indexLap.intValue()).getTracks().remove(indexPosition.intValue());
				act.getLaps().remove(indexLap.intValue());
			}
			// Delete the trackpoint and calculate lap values.
			else {
				Lap newLap = SerializationUtils.clone(act.getLaps().get(indexLap));
				// Reset speed and distance of the next track point of the
				// removed track point
				// calculateDistanceSpeedValues calculate this params.
				if (indexPosition.intValue() < newLap.getTracks().size() - 1) {
					newLap.getTracks().get(indexPosition.intValue() + 1).setSpeed(null);
					newLap.getTracks().get(indexPosition.intValue() + 1).setDistanceMeters(null);
				}
				// Remove track point
				newLap.getTracks().remove(indexPosition.intValue());
				act.getLaps().remove(indexLap.intValue());
				act.getLaps().add(indexLap.intValue(), newLap);
				lapsUtilsService.resetAggregateValues(newLap);
				lapsUtilsService.resetTotals(newLap);
				calculateDistanceSpeedValues(act);
				lapsUtilsService.setTotalValuesLap(newLap);
				lapsUtilsService.calculateAggregateValuesLap(newLap);
			}
			return act;
		} else
			return null;
	}

	@Override
	public Activity splitLap(Activity act, String lat, String lng, String timeInMillis, String indexTrackPoint) {
		Long time = !Objects.isNull(timeInMillis) && !timeInMillis.isEmpty() ? Long.parseLong(timeInMillis) : null;
		Integer index = !Objects.isNull(indexTrackPoint) && !indexTrackPoint.isEmpty()
				? Integer.parseInt(indexTrackPoint) : null;

		Position position = new Position(new BigDecimal(lat), new BigDecimal(lng));

		Integer indexLap = indexOfALap(act, position, time, index);
		if (indexLap > -1) {
			Integer indexPosition = lapsUtilsService.indexOfTrackpoint(act, indexLap, position, time, index);
			Lap lap = act.getLaps().get(indexLap);
			int sizeOfTrackPoints = lap.getTracks().size();

			// Track point not start nor end. Thus, it split into two laps
			if (indexPosition > 0 && indexPosition < sizeOfTrackPoints - 1) {

				Lap lapSplitLeft = SerializationUtils.clone(act.getLaps().get(indexLap));
				lapsUtilsService.createSplittLap(lap, lapSplitLeft, 0, indexPosition);
				// Index the same original lap
				lapSplitLeft.setIndex(lap.getIndex());

				Lap lapSplitRight = SerializationUtils.clone(act.getLaps().get(indexLap));
				// lap right: add right elements and the current track point
				// which has index = index position
				lapsUtilsService.createSplittLap(lap, lapSplitRight, indexPosition, sizeOfTrackPoints);
				// Index
				lapSplitRight.setIndex(lap.getIndex() + 1);

				// Increment a point the index field of the next elements of the
				// element to delete
				IntStream.range(indexLap + 1, act.getLaps().size()).forEach(indexEachLap -> {
					act.getLaps().get(indexEachLap).setIndex(act.getLaps().get(indexEachLap).getIndex() + 1);
				});

				// Delete lap before splitting
				act.getLaps().remove(indexLap.intValue());
				// Adding two new laps
				act.getLaps().add(indexLap.intValue(), lapSplitLeft);
				act.getLaps().add((indexLap.intValue() + 1), lapSplitRight);

				return act;
			} else
				return null;
		} else
			return null;
	}

	@Override
	public Activity joinLap(Activity act, Integer indexLap1, Integer indexLap2) {
		if (Objects.isNull(indexLap1) || Objects.isNull(indexLap2) || Objects.isNull(act))
			return null;
		int indexLapLeft = indexLap1, indexLapRight = indexLap2;
		if (indexLap1.compareTo(indexLap2) > 0) {
			indexLapLeft = indexLap2;
			indexLapRight = indexLap1;
		}

		Lap lapLeft = act.getLaps().get(indexLapLeft);
		Lap lapRight = act.getLaps().get(indexLapRight);

		Lap newLap = lapsUtilsService.joinLaps(lapLeft, lapRight);

		act.getLaps().remove(lapLeft);
		act.getLaps().remove(lapRight);

		act.getLaps().add(indexLapLeft, newLap);

		IntStream.range(indexLapRight, act.getLaps().size()).forEach(indexEachLap -> {
			act.getLaps().get(indexEachLap).setIndex(act.getLaps().get(indexEachLap).getIndex() - 1);
		});

		return act;
	}

	@Override
	public Activity removeLap(Activity act, Long startTime, Integer indexLap) {
		if (Objects.isNull(act))
			return null;
		Lap lapToDelete = act.getLaps().stream().filter((lap) -> {
			return lap.getIndex() == indexLap && (!Objects.isNull(startTime)
					? startTime == lap.getStartTime().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli() : true);
		}).findFirst().orElse(null);

		act.getLaps().remove(lapToDelete);

		return act;
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
				lapsUtilsService.calculateDistanceLap(lap, null);
				break;
			default:
				Lap previousLap = laps.get(indexLap - 1);
				lapsUtilsService.calculateDistanceLap(lap,
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
				lapsUtilsService.calculateSpeedLap(lap, null);
				break;
			default:
				Lap previousLap = laps.get(indexLap - 1);
				lapsUtilsService.calculateSpeedLap(lap,
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
	 * @return index of the lapº
	 */
	private int indexOfALap(Activity activity, Position position, Long timeInMillis, Integer index) {
		Predicate<Lap> isThisLap = lap ->
				lapsUtilsService.fulfillCriteriaPositionTime(lap, position, timeInMillis, index);
		return ofNullable(activity)
				.map(Activity::getLaps)
				.flatMap(laps -> laps.stream().filter(isThisLap).findFirst().map(laps::indexOf))
				.orElse(-1);
	}

}
