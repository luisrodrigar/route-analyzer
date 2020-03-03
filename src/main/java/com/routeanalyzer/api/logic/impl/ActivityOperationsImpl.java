package com.routeanalyzer.api.logic.impl;

import com.routeanalyzer.api.common.CommonUtils;
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
import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.IntStream;

import static com.routeanalyzer.api.common.Constants.LAP_DELIMITER;
import static com.routeanalyzer.api.common.Constants.SOURCE_GPX_XML;
import static com.routeanalyzer.api.common.Constants.SOURCE_TCX_XML;
import static com.routeanalyzer.api.common.DateUtils.toTimeMillis;
import static com.routeanalyzer.api.common.MathUtils.isPositiveOrZero;
import static com.routeanalyzer.api.common.MathUtils.sortingPositiveValues;
import static io.vavr.API.$;
import static io.vavr.API.Case;
import static io.vavr.API.Match;
import static io.vavr.Predicates.is;
import static java.lang.String.format;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static java.util.Optional.of;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;
import static java.util.Objects.nonNull;

@Slf4j
@Service
@RequiredArgsConstructor
public class ActivityOperationsImpl implements ActivityOperations {

	private final LapsOperations lapsOperationsService;
	private final OriginalActivityRepository aS3Service;
	private final TcxExportFileService tcxExportService;
	private final GpxExportFileService gpxExportService;

	@Override
	public Optional<Activity> removePoint(final Activity activity, final String lat, final String lng, final Long timeInMillis,
								final Integer indexTrackPoint) {
		return arrayIndexOfALap(activity, lat, lng, timeInMillis, indexTrackPoint)
				.flatMap(indexLap -> indexOfTrackPoint(activity, indexLap, lat, lng, timeInMillis, indexTrackPoint)
						.filter(indexTrack -> isPositiveOrZero(indexTrack))
						.filter(indexTrack ->  activity.getLaps().get(indexLap).getTracks().size() > 1)
						.map(indexTrack -> removeGeneralPoint(activity, indexLap, indexTrack))
						.orElseGet(() -> removeLaps(activity,
										singletonList(activity.getLaps().get(indexLap).getStartTime().toInstant().toEpochMilli()),
										singletonList(activity.getLaps().get(indexLap).getIndex()))));
	}

	@Override
	public Optional<Activity> splitLap(final Activity activity, final String lat, final String lng, final Long timeInMillis,
							 final Integer indexTrackPoint) {
		return ofNullable(activity)
				.map(Activity::getLaps)
				.flatMap(laps -> arrayIndexOfALap(activity, lat, lng, timeInMillis, indexTrackPoint)
						.flatMap(indexLap -> indexOfTrackPoint(activity, indexLap, lat, lng, timeInMillis, indexTrackPoint)
								.flatMap(indexPosition -> of(indexPosition)
										.filter(MathUtils::isPositiveNonZero)
										.flatMap(indexTrackParam -> of(indexLap)
												.map(laps::get)
												.map(Lap::getTracks)
												.map(List::size)
												.map(MathUtils::decreaseUnit))
										.filter(lastPosition -> indexPosition < lastPosition)
										.map(lastPosition -> indexPosition))
								.flatMap(indexPosition -> ofNullable(laps.get(indexLap))
										.flatMap(lap -> ofNullable(SerializationUtils.clone(lap))
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
																})))))));
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
	public Optional<Activity> removeLaps(final Activity activity, final List<Long> startTimeList, final List<Integer> indexLapList) {
		AtomicInteger newIndexesLap = new AtomicInteger(1);
		AtomicInteger newIndexesTrackPoint = new AtomicInteger(1);
		return getLapsToDelete(activity, startTimeList, indexLapList)
				.flatMap(lapsToDelete -> of(lapsToDelete)
						.map(List::size)
						.map(numLapsToDelete -> {
							activity.getLaps().removeAll(lapsToDelete);
							activity.getLaps().forEach(lap -> lap.setIndex(newIndexesLap.getAndIncrement()));
							activity.getLaps().forEach(lap -> lap.getTracks().forEach(trackPoint ->
									trackPoint.setIndex(newIndexesTrackPoint.getAndIncrement())));
							return activity;
						}));
	}

	@Override
	public Optional<Integer> indexOfTrackPoint(final Activity activity, final Integer indexLap, final String latitude,
								 final String longitude, final Long time, final Integer index) {
		return ofNullable(activity)
				.map(Activity::getLaps)
				.flatMap(laps -> ofNullable(indexLap)
						.map(laps::get)
						.map(lap -> lapsOperationsService.getTrackPoint(lap, latitude, longitude, time, index)))
				.map(trackPoint -> activity.getLaps()
						.get(indexLap)
						.getTracks()
						.indexOf(trackPoint));
	}

	@Override
	public Optional<Activity> setColorsGetActivity(final Activity activity, final String dataColors) {
		return getColorsLaps(activity, dataColors)
				.map(colorLaps -> {
					activity.setLaps(colorLaps);
					return activity;
				});
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
				.flatMap(activity -> Try.of(multiPart::getBytes)
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

	private Optional<List<Lap>> getColorsLaps(final Activity activity, final String dataColors) {
		// [color1(hex)-lightColor1(hex)]@[color2(hex)-lightColor2(hex)]@[...]...
		AtomicInteger indexLap = new AtomicInteger();
		return ofNullable(activity)
				.map(Activity::getLaps)
				.map(laps -> asList(dataColors.split(LAP_DELIMITER))
						.stream()
						.map(colorsLap -> lapsOperationsService.setColorLap(laps.get(indexLap.getAndIncrement()), colorsLap))
						.collect(toList()));
	}

	private Optional<List<Lap>> getLapsToDelete(final Activity activity, final List<Long> startTimeList,
												final List<Integer> indexLapList) {
		return ofNullable(activity)
				.filter(__ -> nonNull(indexLapList) && nonNull(startTimeList))
				.filter(__ -> !indexLapList.isEmpty() && !startTimeList.isEmpty())
				.filter(__ -> indexLapList.size() == startTimeList.size())
				.filter(__ -> indexLapList.stream().allMatch(MathUtils::isPositiveOrZero))
				.filter(__ -> indexLapList.stream().allMatch(index -> index <= activity.getLaps().size()))
				.map(Activity::getLaps)
				.map(laps -> laps.stream()
						.filter(lapParam ->
								isIndexIncluded(lapParam, indexLapList) || isTimeInMillisIncluded(lapParam, startTimeList))
						.collect(toList()));
	}

	private boolean isTimeInMillisIncluded(final Lap lap, final List<Long> times) {
		return of(lap)
				.map(Lap::getStartTime)
				.map(ZonedDateTime::toInstant)
				.map(Instant::toEpochMilli)
				.filter(__ -> nonNull(times))
				.filter(__ -> !times.isEmpty())
				.filter(timeInMillis -> times.contains(timeInMillis))
				.isPresent();
	}

	private boolean isIndexIncluded(final Lap lap, final List<Integer> indexes) {
		return of(lap)
				.map(Lap::getIndex)
				.filter(__ -> nonNull(indexes))
				.filter(__ -> !indexes.isEmpty())
				.filter(index -> indexes.contains(index))
				.isPresent();
	}

	private Optional<Activity> removeGeneralPoint(final Activity activity, final Integer indexLap,
										final Integer indexCurrentTrackPoint) {
		Predicate<List<TrackPoint>> isNotLastTrackPoint = trackList -> of(trackList)
				.map(List::size)
				.map(MathUtils::decreaseUnit)
				.filter(lastIndexTrackPoint -> indexCurrentTrackPoint < lastIndexTrackPoint)
				.isPresent();
		AtomicInteger newIndexTrackPoints = new AtomicInteger(1);
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
									activity.getLaps().forEach(lap -> lap.getTracks().forEach(trackPoint ->
											trackPoint.setIndex(newIndexTrackPoints.getAndIncrement())));
									return activity;
								})));
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
				.ifPresent(lapToSetIndex -> of(lapToSetIndex)
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
	 * @return optional index of the lap
	 */
	private Optional<Integer> arrayIndexOfALap(final Activity activity, final String latitude, final String longitude,
											   final Long timeInMillis, final Integer index) {
		Predicate<Lap> isThisLap = lap ->
				lapsOperationsService.fulfillCriteriaPositionTime(lap, latitude, longitude, timeInMillis, index);
		return ofNullable(activity)
				.map(Activity::getLaps)
				.flatMap(laps -> laps.stream()
						.filter(isThisLap)
						.findFirst()
						.map(laps::indexOf));
	}

}
