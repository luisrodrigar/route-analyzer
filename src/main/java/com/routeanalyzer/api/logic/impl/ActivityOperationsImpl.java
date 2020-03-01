package com.routeanalyzer.api.logic.impl;

import com.routeanalyzer.api.common.DateUtils;
import com.routeanalyzer.api.common.MathUtils;
import com.routeanalyzer.api.logic.ActivityOperations;
import com.routeanalyzer.api.logic.LapsOperations;
import com.routeanalyzer.api.logic.file.export.impl.GpxExportFileService;
import com.routeanalyzer.api.logic.file.export.impl.TcxExportFileService;
import com.routeanalyzer.api.logic.file.upload.UploadFileService;
import com.routeanalyzer.api.model.Activity;
import com.routeanalyzer.api.model.Lap;
import com.routeanalyzer.api.model.TrackPoint;
import com.routeanalyzer.api.services.OriginalActivityRepository;
import io.vavr.control.Try;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.SerializationUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static com.routeanalyzer.api.common.Constants.LAP_DELIMITER;
import static com.routeanalyzer.api.common.Constants.SOURCE_GPX_XML;
import static com.routeanalyzer.api.common.Constants.SOURCE_TCX_XML;
import static com.routeanalyzer.api.common.MathUtils.isPositiveOrZero;
import static com.routeanalyzer.api.common.MathUtils.sortingPositiveValues;
import static io.vavr.API.$;
import static io.vavr.API.Case;
import static io.vavr.API.Match;
import static io.vavr.Predicates.is;
import static java.lang.String.format;
import static java.util.Arrays.asList;
import static java.util.Optional.of;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;

@Slf4j
@Service
@RequiredArgsConstructor
public class ActivityOperationsImpl implements ActivityOperations {

	private final LapsOperations lapsOperationsService;
	private final OriginalActivityRepository aS3Service;
	private final TcxExportFileService tcxExportService;
	private final GpxExportFileService gpxExportService;

	@Override
	public Activity removePoint(final Activity act, final String lat, final String lng, final Long timeInMillis,
								final Integer indexTrackPoint) {
		return ofNullable(indexOfALap(act, lat, lng, timeInMillis, indexTrackPoint))
				.flatMap(indexLap -> of(indexLap)
						.map(indexLapParam ->
								indexOfTrackPoint(act, indexLapParam, lat, lng, timeInMillis, indexTrackPoint))
						.map(indexTrack -> of(indexTrack)
								.flatMap(indexTrackParam -> of(indexLap)
										.map(act.getLaps()::get)
										.map(Lap::getTracks)
										.map(List::size))
								.filter(sizeTrackPoints -> sizeTrackPoints > 1)
								.filter(__ -> isPositiveOrZero(indexTrack))
								.map(__ -> this.removeGeneralPoint(act, indexLap, indexTrack))
								.orElseGet(() -> this.removeLap(act, indexLap))))
				.orElse(null);
	}

	@Override
	public Activity splitLap(final Activity activity, final String lat, final String lng, final Long timeInMillis,
							 final Integer indexTrackPoint) {
		return ofNullable(activity)
				.map(Activity::getLaps)
				.flatMap(laps -> ofNullable(indexOfALap(activity, lat, lng, timeInMillis, indexTrackPoint))
						.flatMap(indexLap -> of(indexLap)
								.map(indexLapParam ->
										indexOfTrackPoint(activity, indexLapParam, lat, lng, timeInMillis, indexTrackPoint))
								.flatMap(indexPosition -> of(indexPosition)
										.filter(MathUtils::isPositiveNonZero)
										.flatMap(indexTrackParam -> of(indexLap)
												.map(laps::get)
												.map(Lap::getTracks)
												.map(List::size)
												.map(MathUtils::decreaseUnit))
										.filter(lastPosition -> indexPosition < lastPosition)
										.map(lastPosition -> indexPosition))
								.flatMap(indexPosition -> ofNullable(indexLap)
										.map(laps::get)
										.flatMap(lap -> of(lap)
												.map(SerializationUtils::clone)
												.map(lapSplitLeft -> lapsOperationsService.createSplitLap(lap, 0, indexPosition, lap.getIndex()))
												.flatMap(lapSplitLeft -> of(lap)
														.map(SerializationUtils::clone)
														.flatMap(lapSplitRight -> of(lap)
																.map(Lap::getIndex)
																.map(MathUtils::increaseUnit)
																.map(indexRightLap ->
																		lapsOperationsService.createSplitLap(lap, indexPosition, lap.getTracks().size(), indexRightLap)))
														.flatMap(lapSplitRight -> of(indexLap)
																.map(MathUtils::increaseUnit)
																.map(indexLapParam -> {
																	increaseIndexFollowingLaps(indexLapParam, laps);
																	return indexLapParam;
																})
																.map(indexRightLapParam -> {
																	laps.remove(indexLap.intValue());
																	laps.add(indexLap.intValue(), lapSplitLeft);
																	laps.add(indexRightLapParam.intValue(), lapSplitRight);
																	return activity;
																})))))))
				.orElse(null);
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
	 * @param index1 lap index 1 to join
	 * @param index2 lap index 2 to join
	 * @return activity with the joined laps
	 */
	@Override
	public Activity joinLaps(final Activity activity, final Integer index1, final Integer index2) {
		ofNullable(activity)
				.orElseThrow(() -> new IllegalArgumentException("Activity"));
		List<Integer> sortedIndexes = sortingPositiveValues(index1, index2);
		Lap indexLeftLap = activity.getLaps().get(sortedIndexes.get(0));
		Lap indexRightLap = activity.getLaps().get(sortedIndexes.get(1));
		Lap joinedLap = lapsOperationsService.joinLaps(indexLeftLap, indexRightLap);
		activity.getLaps().removeAll(asList(indexLeftLap, indexRightLap));
		activity.getLaps().add(sortedIndexes.get(0), joinedLap);
		decreaseIndexFollowingLaps(sortedIndexes.get(1), activity.getLaps());
		return activity;
	}

	@Override
	public Activity removeLaps(final Activity act, final List<Long> startTimeList, final List<Integer> indexLapList) {
		Predicate<Object> isIndexIncluded = indexLap -> ofNullable(indexLapList)
				.map(indexLapListParam -> indexLapListParam.stream()
						.anyMatch(indexLapEle -> indexLapEle.equals(indexLap)))
				.orElse(false);
		Predicate<Long> isTimeMillisIncluded = timeMillis -> ofNullable(startTimeList)
				.map(startTimeListParam -> startTimeListParam.stream()
						.anyMatch(timeMillisEle -> timeMillisEle.equals(timeMillis)))
				.orElse(true);
		ofNullable(act)
				.map(Activity::getLaps)
				.ifPresent(laps -> laps.stream()
						.filter(lapParam -> ofNullable(lapParam)
								.map(Lap::getIndex)
								.filter(isIndexIncluded)
								.isPresent())
						.filter(lapParam -> ofNullable(lapParam)
								.map(Lap::getStartTime)
								.flatMap(DateUtils::toTimeMillis)
								.filter(isTimeMillisIncluded)
								.isPresent())
						.collect(Collectors.toList())
						.forEach(laps::remove));
		return act;
	}

	@Override
	public int indexOfTrackPoint(final Activity activity, final Integer indexLap, final String latitude,
								 final String longitude, final Long time, final Integer index) {
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
						.map(lap -> lapsOperationsService.getTrackPoint(lap, latitude, longitude, time, index)))
				.flatMap(indexOfTrackPoint)
				.orElse(-1);
	}

	@Override
	public Activity setColorsGetActivity(final Activity activity, final String dataColors) {
		// [color1(hex)-lightColor1(hex)]@[color2(hex)-lightColor2(hex)]@[...]...
		AtomicInteger indexLap = new AtomicInteger();
		ofNullable(activity)
				.map(Activity::getLaps)
				.ifPresent(laps -> asList(dataColors.split(LAP_DELIMITER)).stream()
						.forEach(lapColors -> lapsOperationsService
								.setColorLap(laps.get(indexLap.getAndIncrement()), lapColors)));
		return activity;
	}

	@Override
	public String exportByType(final String type, final Activity activity) {
		return Match(type.toLowerCase()).option(
				Case($(is(SOURCE_TCX_XML)), tcxFile -> tcxExportService.export(activity)),
				Case($(is(SOURCE_GPX_XML)), gpxFile -> gpxExportService.export(activity)))
				.getOrElseThrow(() -> new IllegalArgumentException("Bad type file."));
	}

	@Override
	@SuppressWarnings("unchecked")
	public Optional<List<Activity>> upload(final MultipartFile multiPart, final UploadFileService service) {
		return (Optional<List<Activity>>) service.upload(multiPart)
				.toJavaOptional()
				.map(service::toListActivities);
	}

	@Override
	public List<Activity> pushToS3(final List<Activity> activities, final MultipartFile multiPart) {
		return activities.stream()
				.flatMap(activity -> Try.of(() -> multiPart.getBytes())
						.onFailure(err -> log.error("Error trying to convert the file to bytes", err))
						.flatMap(arrayBytes -> Try
								.run(() -> aS3Service
										.uploadFile(arrayBytes,
												format("%s.%s", activity.getId(), activity.getSourceXmlType())))
								.onFailure(err -> log.error("Error trying to upload to S3Bucket.", err))
								.map(__ -> activity))
						.toJavaStream())
				.collect(toList());
	}

	@Override
	public Optional<String> getOriginalFile(final String id, final String type ){
		return aS3Service.getFile(format("%s.%s", id, type))
				.map(InputStreamReader::new)
				.map(BufferedReader::new)
				.map(BufferedReader::lines)
				.map(streamLines -> streamLines.collect(joining("\n")));
	}

	private Activity removeLap(final Activity activity, final Integer indexLap) {
		Function<Lap, Activity> removeLap = lap -> {
			activity.getLaps().remove(lap);
			return activity;
		};
		return ofNullable(indexLap)
				.map(activity.getLaps()::get)
				.map(removeLap)
				.orElse(activity);
	}

	private Activity removeGeneralPoint(final Activity activity, final Integer indexLap,
										final Integer indexCurrentTrackPoint) {
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




	private void consumerElementsFollowingElements(int fromIndex, List<Lap> laps, Function<Integer, Consumer<List<Lap>>> consumerFunction) {
		int lastIndexExcluded = laps.size();
		IntStream.range(fromIndex, lastIndexExcluded).forEach(indexEachLap -> consumerFunction.apply(indexEachLap).accept(laps));
	}

	private void increaseIndexFollowingLaps(int fromIndex, List<Lap> laps) {
		consumerElementsFollowingElements(fromIndex, laps, increaseIndex);
	}

	private void decreaseIndexFollowingLaps(int fromIndex, List<Lap> laps) {
		consumerElementsFollowingElements(fromIndex, laps, decreaseIndex);
	}

	private Function<Integer, Consumer<List<Lap>>> increaseIndex = genericConsumerEachLap(MathUtils::increaseUnit);

	private Function<Integer, Consumer<List<Lap>>> decreaseIndex = genericConsumerEachLap(MathUtils::decreaseUnit);

	private Function<Integer, Consumer<List<Lap>>> genericConsumerEachLap(Function<Integer, Integer> applyEachElement) {
		return indexLap -> laps -> ofNullable(indexLap)
				.map(laps::get)
				.ifPresent(lapToSetIndex ->
						ofNullable(lapToSetIndex)
								.map(Lap::getIndex)
								.map(applyEachElement)
								.ifPresent(lapToSetIndex::setIndex));
	}

	public void calculateDistanceSpeedValues(final Activity activity) {
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

	private void calculateDistanceMeters(final Activity activity) {
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

	private void calculateSpeedValues(final Activity activity) {
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

	private boolean hasActivityTrackPointsValue(final Activity activity, final Function<TrackPoint, Boolean> function) {
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
	 * @param latitude:
	 *            latitude position
	 * @param longitude:
	 *            longitude position
	 * @param timeInMillis:
	 *            time in milliseconds
	 * @param index:
	 *            index of the lap in the array
	 * @return index of the lap
	 */
	private Integer indexOfALap(final Activity activity, final String latitude, final String longitude,
								final Long timeInMillis, final Integer index) {
		Predicate<Lap> isThisLap = lap ->
				lapsOperationsService.fulfillCriteriaPositionTime(lap, latitude, longitude, timeInMillis, index);
		return ofNullable(activity)
				.map(Activity::getLaps)
				.flatMap(laps -> laps.stream()
						.filter(isThisLap)
						.findFirst()
						.map(laps::indexOf))
				.orElse(null);
	}

}
