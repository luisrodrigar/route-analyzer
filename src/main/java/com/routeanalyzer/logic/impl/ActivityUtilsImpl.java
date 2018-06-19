package com.routeanalyzer.logic.impl;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.stream.IntStream;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

import org.apache.commons.lang3.SerializationUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.xml.sax.SAXParseException;

import com.amazonaws.AmazonClientException;
import com.routeanalyzer.logic.ActivityUtils;
import com.routeanalyzer.logic.LapsUtils;
import com.routeanalyzer.model.Activity;
import com.routeanalyzer.model.Lap;
import com.routeanalyzer.model.Position;
import com.routeanalyzer.model.TrackPoint;
import com.routeanalyzer.services.reader.GPXService;
import com.routeanalyzer.services.reader.TCXService;
import com.routeanalyzer.xml.gpx11.ExtensionsType;
import com.routeanalyzer.xml.gpx11.GpxType;
import com.routeanalyzer.xml.gpx11.MetadataType;
import com.routeanalyzer.xml.gpx11.TrkType;
import com.routeanalyzer.xml.gpx11.TrksegType;
import com.routeanalyzer.xml.gpx11.WptType;
import com.routeanalyzer.xml.gpx11.trackpointextension.garmin.TrackPointExtensionT;
import com.routeanalyzer.xml.tcx.ActivityLapT;
import com.routeanalyzer.xml.tcx.ActivityListT;
import com.routeanalyzer.xml.tcx.ActivityT;
import com.routeanalyzer.xml.tcx.ExtensionsT;
import com.routeanalyzer.xml.tcx.HeartRateInBeatsPerMinuteT;
import com.routeanalyzer.xml.tcx.IntensityT;
import com.routeanalyzer.xml.tcx.PositionT;
import com.routeanalyzer.xml.tcx.SportT;
import com.routeanalyzer.xml.tcx.TrackT;
import com.routeanalyzer.xml.tcx.TrackpointT;
import com.routeanalyzer.xml.tcx.TrainingCenterDatabaseT;
import com.routeanalyzer.xml.tcx.activityextension.ActivityLapExtensionT;
import com.routeanalyzer.xml.tcx.activityextension.ActivityTrackpointExtensionT;

@Service
public class ActivityUtilsImpl implements ActivityUtils {
	
	@Autowired
	private GPXService gpxReader;
	@Autowired
	private TCXService tcxReader;
	@Autowired
	private LapsUtils lapsUtilsService;

	@Override
	public List<Activity> uploadGPXFile(MultipartFile multiPart)
			throws IOException, AmazonClientException, JAXBException, SAXParseException {
		InputStream inputFileGPX = multiPart.getInputStream();
		// Get the object from xml file (type GPX)
		GpxType gpx = gpxReader.readXML(inputFileGPX);
		// Create each activity of the file
		return getListActivitiesFromGPX(gpx);
	}

	@Override
	public List<Activity> uploadTCXFile(MultipartFile multiPart)
			throws IOException, JAXBException, SAXParseException {
		InputStream inputFileTCX = multiPart.getInputStream();
		// Get the object from xml file (type TCX)
		TrainingCenterDatabaseT tcx = tcxReader.readXML(inputFileTCX);
		// Create each activity of the file
		return getListActivitiesFromTCX(tcx);
	}

	@Override
	public String exportAsTCX(Activity act) throws JAXBException {
		TCXService xmlCreator = new TCXService();
		com.routeanalyzer.xml.tcx.ObjectFactory oFactory = new com.routeanalyzer.xml.tcx.ObjectFactory();
		com.routeanalyzer.xml.tcx.activityextension.ObjectFactory extensionFactory = new com.routeanalyzer.xml.tcx.activityextension.ObjectFactory();

		TrainingCenterDatabaseT trainingCenterDatabaseT = new TrainingCenterDatabaseT();
		ActivityListT actListT = new ActivityListT();
		ActivityT activityT = new ActivityT();
		// Sport is an enum
		if (!Objects.isNull(act.getSport())) {
			SportT sport = SportT.valueOf(act.getSport());
			activityT.setSport(sport);
		}
		// Set xml gregorian calendar date
		if (!Objects.isNull(act.getDate())) {
			GregorianCalendar c = new GregorianCalendar();
			c.setTime(act.getDate());
			XMLGregorianCalendar xmlGregorianCalendar;
			try {
				xmlGregorianCalendar = DatatypeFactory.newInstance().newXMLGregorianCalendar(c);
				activityT.setId(xmlGregorianCalendar);
			} catch (DatatypeConfigurationException e) {
				activityT.setId(null);
			}
		}
		act.getLaps().forEach(lap -> {
			ActivityLapT lapT = new ActivityLapT();
			// heart rate average in beats per minute
			HeartRateInBeatsPerMinuteT avgBpm = new HeartRateInBeatsPerMinuteT();
			avgBpm.setValue(!Objects.isNull(lap.getAverageHearRate()) && lap.getAverageHearRate().shortValue() > 0.0
					? lap.getAverageHearRate().shortValue() : 0);
			if (avgBpm.getValue() > 0)
				lapT.setAverageHeartRateBpm(avgBpm);
			// calories
			if (!Objects.isNull(lap.getCalories()))
				lapT.setCalories(lap.getCalories());
			// distance in meters
			if (!Objects.isNull(lap.getDistanceMeters()))
				lapT.setDistanceMeters(lap.getDistanceMeters());
			// max speed in meters per second
			if (!Objects.isNull(lap.getMaximunSpeed()))
				lapT.setMaximumSpeed(lap.getMaximunSpeed());
			// max heart rate in beats per minute
			HeartRateInBeatsPerMinuteT maxBpm = new HeartRateInBeatsPerMinuteT();
			maxBpm.setValue(!Objects.isNull(lap.getMaximunHeartRate()) && lap.getMaximunHeartRate().shortValue() > 0
					? lap.getMaximunHeartRate().shortValue() : 0);
			if (maxBpm.getValue() > 0)
				lapT.setMaximumHeartRateBpm(maxBpm);
			// Start time in xml gregorian calendar
			if (!Objects.isNull(lap.getStartTime())) {
				GregorianCalendar gregorian = new GregorianCalendar();
				gregorian.setTime(lap.getStartTime());
				try {
					lapT.setStartTime(DatatypeFactory.newInstance().newXMLGregorianCalendar(gregorian));
				} catch (DatatypeConfigurationException e) {
					lapT.setStartTime(null);
				}
			} else
				lapT.setStartTime(null);
			// total time in seconds
			if (!Objects.isNull(lap.getTotalTimeSeconds()) && lap.getTotalTimeSeconds() > 0)
				lapT.setTotalTimeSeconds(lap.getTotalTimeSeconds());
			// intensity
			if (!Objects.isNull(lap.getIntensity()))
				lapT.setIntensity(IntensityT.valueOf(lap.getIntensity()));

			if (!Objects.isNull(lap.getAverageSpeed())) {
				ExtensionsT extT = new ExtensionsT();
				ActivityLapExtensionT actExtensionT = new ActivityLapExtensionT();
				actExtensionT.setAvgSpeed(lap.getAverageSpeed());
				extT.addAny(extensionFactory.createLX(actExtensionT));
				lapT.setExtensions(extT);
			}
			TrackT trackT = new TrackT();
			lap.getTracks().forEach(trackPoint -> {
				TrackpointT trackPointT = new TrackpointT();
				if (!Objects.isNull(trackPoint.getDate())) {
					GregorianCalendar gregorian = new GregorianCalendar();
					gregorian.setTime(trackPoint.getDate());
					try {
						trackPointT.setTime(DatatypeFactory.newInstance().newXMLGregorianCalendar(gregorian));
					} catch (DatatypeConfigurationException e) {
						trackPointT.setTime(null);
					}
				}
				if (!Objects.isNull(trackPoint.getPosition())) {
					PositionT positionT = new PositionT();
					positionT.setLatitudeDegrees(trackPoint.getPosition().getLatitudeDegrees().doubleValue());
					positionT.setLongitudeDegrees(trackPoint.getPosition().getLongitudeDegrees().doubleValue());
					trackPointT.setPosition(positionT);
				}
				trackPointT.setAltitudeMeters(
						!Objects.isNull(trackPoint.getAltitudeMeters()) ? trackPoint.getAltitudeMeters().doubleValue() : null);
				trackPointT.setDistanceMeters(
						!Objects.isNull(trackPoint.getDistanceMeters()) ? trackPoint.getDistanceMeters().doubleValue() : null);
				if (!Objects.isNull(trackPoint.getHeartRateBpm())) {
					HeartRateInBeatsPerMinuteT bpm = new HeartRateInBeatsPerMinuteT();
					bpm.setValue(trackPoint.getHeartRateBpm().shortValue());
					trackPointT.setHeartRateBpm(bpm);
				}
				if (!Objects.isNull(trackPoint.getSpeed())) {
					ExtensionsT extension = new ExtensionsT();

					ActivityTrackpointExtensionT trackPointExtension = new ActivityTrackpointExtensionT();
					trackPointExtension.setSpeed(trackPoint.getSpeed().doubleValue());
					extension.addAny(extensionFactory.createTPX(trackPointExtension));
					trackPointT.setExtensions(extension);
				}
				trackT.addTrackpoint(trackPointT);
			});
			lapT.addTrack(trackT);
			activityT.addLap(lapT);
		});
		actListT.addActivity(activityT);
		trainingCenterDatabaseT.setActivities(actListT);

		return xmlCreator.createXML(oFactory.createTrainingCenterDatabase(trainingCenterDatabaseT));
	}

	@Override
	public String exportAsGPX(Activity act) throws JAXBException {
		GPXService xmlCreator = new GPXService();
		com.routeanalyzer.xml.gpx11.ObjectFactory oFactory = new com.routeanalyzer.xml.gpx11.ObjectFactory();
		com.routeanalyzer.xml.gpx11.trackpointextension.garmin.ObjectFactory extensionFactory = new com.routeanalyzer.xml.gpx11.trackpointextension.garmin.ObjectFactory();

		GpxType gpx = new GpxType();
		if (!Objects.isNull(act.getDate())) {
			MetadataType metadata = new MetadataType();
			GregorianCalendar gregorian = new GregorianCalendar();
			gregorian.setTime(act.getDate());
			try {
				metadata.setTime(DatatypeFactory.newInstance().newXMLGregorianCalendar(gregorian));
			} catch (DatatypeConfigurationException e) {
				metadata.setTime(null);
			}
		}
		gpx.setCreator(act.getDevice());
		TrkType trk = new TrkType();
		act.getLaps().forEach(lap -> {
			TrksegType trkSeg = new TrksegType();
			lap.getTracks().forEach(trackPoint -> {
				WptType wpt = new WptType();
				if (!Objects.isNull(trackPoint.getDate())) {
					GregorianCalendar gregorianCalendar = new GregorianCalendar();
					gregorianCalendar.setTime(trackPoint.getDate());
					try {
						wpt.setTime(DatatypeFactory.newInstance().newXMLGregorianCalendar(gregorianCalendar));
					} catch (DatatypeConfigurationException e) {
						wpt.setTime(null);
					}
				}
				wpt.setLat(!Objects.isNull(trackPoint.getPosition()) && !Objects.isNull(trackPoint.getPosition().getLatitudeDegrees())
						? trackPoint.getPosition().getLatitudeDegrees() : null);
				wpt.setLon(!Objects.isNull(trackPoint.getPosition()) && !Objects.isNull(trackPoint.getPosition().getLongitudeDegrees())
						? trackPoint.getPosition().getLongitudeDegrees() : null);
				wpt.setEle(!Objects.isNull(trackPoint.getAltitudeMeters()) ? trackPoint.getAltitudeMeters() : null);
				if (!Objects.isNull(trackPoint.getHeartRateBpm())) {
					ExtensionsType extensions = new ExtensionsType();
					TrackPointExtensionT trptExtension = new TrackPointExtensionT();
					trptExtension.setHr(trackPoint.getHeartRateBpm().shortValue());
					extensions.addAny(extensionFactory.createTrackPointExtension(trptExtension));
					wpt.setExtensions(extensions);
				}
				trkSeg.addTrkpt(wpt);
			});
			trk.addTrkseg(trkSeg);
		});
		gpx.addTrk(trk);

		return xmlCreator.createXML(oFactory.createGpx(gpx));
	}

	@Override
	public Activity removePoint(Activity act, String lat, String lng, String timeInMillis, String indexTrackPoint) {
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
		if (Objects.isNull(indexLap1) || Objects.isNull(indexLap2))
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
		Lap lapToDelete = act.getLaps().stream().filter((lap) -> {
			return lap.getIndex() == indexLap
					&& (!Objects.isNull(startTime) ? startTime == lap.getStartTime().getTime() : true);
		}).findFirst().orElse(null);

		act.getLaps().remove(lapToDelete);

		return act;
	}

	/**
	 * 
	 * @param gpx
	 * @return
	 */
	private List<Activity> getListActivitiesFromGPX(GpxType gpx) {
		List<Activity> activities = new ArrayList<Activity>();
		AtomicInteger indexLap = new AtomicInteger(), indexTrackPoint = new AtomicInteger();
		gpx.getTrk().forEach(track -> {
			Activity activity = new Activity();
			activity.setSourceXmlType("gpx");
			activity.setDate(!Objects.isNull(gpx.getMetadata()) && !Objects.isNull(gpx.getMetadata().getTime())
					&& !Objects.isNull(indexLap.get())
							? gpx.getMetadata().getTime().toGregorianCalendar().getTime()
							: (!Objects.isNull(track.getTrkseg()) && !Objects.isNull(track.getTrkseg().get(0))
									&& !Objects.isNull(track.getTrkseg().get(0).getTrkpt())
									&& !Objects.isNull(track.getTrkseg().get(0).getTrkpt().get(0))
									&& !Objects.isNull(track.getTrkseg().get(0).getTrkpt().get(0).getTime())
											? track.getTrkseg().get(0).getTrkpt().get(0).getTime().toGregorianCalendar()
													.getTime()
											: null));
			activity.setDevice(gpx.getCreator());
			activity.setName(!Objects.isNull(track.getName()) ? track.getName().trim() : null);
			track.getTrkseg().forEach(eachLap -> {
				Lap lap = new Lap();
				if (!Objects.isNull(eachLap.getTrkpt()) && !eachLap.getTrkpt().isEmpty()
						&& !Objects.isNull(eachLap.getTrkpt().get(0).getTime()))
					lap.setStartTime(eachLap.getTrkpt().get(0).getTime().toGregorianCalendar().getTime());
				lap.setIndex(indexLap.incrementAndGet());
				eachLap.getTrkpt().forEach(eachTrackPoint -> {
					// Adding track point only if position is informed
					if (!Objects.isNull(eachTrackPoint.getLat()) && !Objects.isNull(eachTrackPoint.getLon())) {
						TrackPoint tkp = new TrackPoint(
								(!Objects.isNull(eachTrackPoint.getTime())
										? eachTrackPoint.getTime().toGregorianCalendar().getTime() : null),
								indexTrackPoint.incrementAndGet(),
								new Position(eachTrackPoint.getLat(), eachTrackPoint.getLon()), eachTrackPoint.getEle(),
								null, null, null);
						if (!Objects.isNull(eachTrackPoint.getExtensions())) {
							eachTrackPoint.getExtensions().getAny()
									.stream().filter(
											item -> (!Objects.isNull(JAXBElement.class.cast(item))
													&& !Objects.isNull(item)
													&& !Objects.isNull(JAXBElement.class.cast(item).getValue())
													&& !Objects.isNull(TrackPointExtensionT.class
															.cast(JAXBElement.class.cast(item).getValue()))))
									.forEach(item -> {
										TrackPointExtensionT trpExt = TrackPointExtensionT.class
												.cast((JAXBElement.class.cast(item)).getValue());
										if (!Objects.isNull(trpExt.getHr()))
											tkp.setHeartRateBpm(trpExt.getHr().intValue());
									});
						}
						lap.addTrack(tkp);
					}
				});
				lapsUtilsService.calculateLapValues(lap);
				activity.addLap(lap);
			});
			// Check if no speed nor distance values
			calculateDistanceSpeedValues(activity);

			activities.add(activity);
		});
		return activities;
	}

	/**
	 * 
	 * @param tcx
	 * @return
	 */
	private List<Activity> getListActivitiesFromTCX(TrainingCenterDatabaseT tcx) {

		List<Activity> activities = new ArrayList<Activity>();

		AtomicInteger indexLap = new AtomicInteger(), indexTrackPoint = new AtomicInteger();
		if (!Objects.isNull(tcx.getActivities())) {
			tcx.getActivities().getActivity().forEach(eachActivity -> {
				Activity activity = new Activity();
				activity.setSourceXmlType("tcx");
				if (!Objects.isNull(eachActivity.getCreator()))
					activity.setDevice(eachActivity.getCreator().getName());
				if (!Objects.isNull(eachActivity.getId()))
					activity.setDate(eachActivity.getId().toGregorianCalendar().getTime());
				if (!Objects.isNull(eachActivity.getSport()))
					activity.setSport(eachActivity.getSport().toString());
				eachActivity.getLap().forEach(eachLap -> {
					Lap lap = new Lap();
					if (!Objects.isNull(eachLap.getAverageHeartRateBpm())
							&& eachLap.getAverageHeartRateBpm().getValue() > 0)
						lap.setAverageHearRate(new Double(eachLap.getAverageHeartRateBpm().getValue()));
					if (eachLap.getCalories() > 0)
						lap.setCalories(eachLap.getCalories());
					if (eachLap.getDistanceMeters() > 0)
						lap.setDistanceMeters(eachLap.getDistanceMeters());
					if (!Objects.isNull(eachLap.getMaximumSpeed()) && eachLap.getMaximumSpeed() > 0)
						lap.setMaximunSpeed(eachLap.getMaximumSpeed());
					if (!Objects.isNull(eachLap.getMaximumHeartRateBpm())
							&& eachLap.getMaximumHeartRateBpm().getValue() > 0)
						lap.setMaximunHeartRate(new Integer(eachLap.getMaximumHeartRateBpm().getValue()));
					if (!Objects.isNull(eachLap.getStartTime()))
						lap.setStartTime(eachLap.getStartTime().toGregorianCalendar().getTime());
					lap.setIndex(indexLap.incrementAndGet());
					if (eachLap.getTotalTimeSeconds() > 0.0)
						lap.setTotalTimeSeconds(eachLap.getTotalTimeSeconds());
					if (!Objects.isNull(eachLap.getIntensity()))
						lap.setIntensity(eachLap.getIntensity().toString());
					if (!Objects.isNull(eachLap.getExtensions()) && !Objects.isNull(eachLap.getExtensions().getAny())
							&& !eachLap.getExtensions().getAny().isEmpty()) {
						eachLap.getExtensions().getAny().stream()
								.filter(extension -> (!Objects.isNull(extension)
										&& !Objects.isNull(JAXBElement.class.cast(extension))
										&& !Objects.isNull(JAXBElement.class.cast(extension).getValue())
										&& !Objects.isNull(ActivityLapExtensionT.class
												.cast(JAXBElement.class.cast(extension).getValue()))))
								.forEach(extension -> {
									ActivityLapExtensionT actTrackpointExtension = ActivityLapExtensionT.class
											.cast(JAXBElement.class.cast(extension).getValue());
									lap.setAverageSpeed(actTrackpointExtension.getAvgSpeed());
								});
					}
					eachLap.getTrack().forEach(track -> {
						track.getTrackpoint().forEach(trackPoint -> {
							// Adding track point only if position is informed
							if (!Objects.isNull(trackPoint.getPosition())) {
								TrackPoint trp = new TrackPoint(
										(!Objects.isNull(trackPoint.getTime())
												? trackPoint.getTime().toGregorianCalendar().getTime() : null),
										indexTrackPoint.incrementAndGet(),
										(!Objects.isNull(trackPoint) && !Objects.isNull(trackPoint.getPosition())
												? new Position(
														new BigDecimal(trackPoint.getPosition().getLatitudeDegrees()),
														new BigDecimal(trackPoint.getPosition().getLongitudeDegrees()))
												: null),
										!Objects.isNull(trackPoint) && !Objects.isNull(trackPoint.getAltitudeMeters())
												? new BigDecimal(trackPoint.getAltitudeMeters()) : null,
										!Objects.isNull(trackPoint) && !Objects.isNull(trackPoint.getDistanceMeters())
												? new BigDecimal(trackPoint.getDistanceMeters()) : null,
										null,
										!Objects.isNull(trackPoint) && !Objects.isNull(trackPoint.getHeartRateBpm())
												? new Integer(trackPoint.getHeartRateBpm().getValue()) : null);
								if (!Objects.isNull(trackPoint.getExtensions())
										&& !Objects.isNull(trackPoint.getExtensions().getAny())) {
									trackPoint.getExtensions().getAny().stream()
											.filter(extension -> (!Objects.isNull(JAXBElement.class.cast(extension))
													&& !Objects.isNull(extension)
													&& !Objects.isNull((JAXBElement.class.cast(extension)).getValue())
													&& !Objects.isNull(ActivityTrackpointExtensionT.class
															.cast((JAXBElement.class.cast(extension)).getValue()))))
											.forEach(extension -> {
												ActivityTrackpointExtensionT actTrackpointExtension = ActivityTrackpointExtensionT.class
														.cast((JAXBElement.class.cast(extension)).getValue());
												if (!Objects.isNull(actTrackpointExtension.getSpeed())
														&& actTrackpointExtension.getSpeed() >= 0)
													trp.setSpeed(new BigDecimal(actTrackpointExtension.getSpeed()));
											});
								}
								lap.addTrack(trp);
							}
						});
					});
					// Calculate values not informed of a lap.
					lapsUtilsService.calculateLapValues(lap);
					activity.addLap(lap);
				});
				calculateDistanceSpeedValues(activity);
				activities.add(activity);
			});
		} else if (!Objects.isNull(tcx.getCourses())) {
			tcx.getCourses().getCourse().forEach(course -> {
				Activity activity = new Activity();
				activity.setSourceXmlType("tcx");
				activity.setName(course.getName());
				course.getLap().forEach(eachLap -> {
					Lap lap = new Lap();
					lap.setAverageHearRate(!Objects.isNull(eachLap.getAverageHeartRateBpm())
							? Double.valueOf(eachLap.getAverageHeartRateBpm().getValue()) : null);
					lap.setTotalTimeSeconds(eachLap.getTotalTimeSeconds());
					lap.setDistanceMeters(eachLap.getDistanceMeters());
					lap.setIndex(indexLap.getAndIncrement());
					Position initial = new Position(new BigDecimal(eachLap.getBeginPosition().getLatitudeDegrees()),
							new BigDecimal(eachLap.getBeginPosition().getLongitudeDegrees()));
					Position end = new Position(new BigDecimal(eachLap.getEndPosition().getLatitudeDegrees()),
							new BigDecimal(eachLap.getEndPosition().getLongitudeDegrees()));
					AtomicInteger eachIndex = new AtomicInteger();
					AtomicInteger indexStart = new AtomicInteger(), indexEnd = new AtomicInteger();
					course.getTrack().forEach(track -> {
						track.getTrackpoint().forEach(trackpoint -> {
							if (trackpoint.getPosition().getLatitudeDegrees() == initial.getLatitudeDegrees()
									.doubleValue()
									&& trackpoint.getPosition().getLongitudeDegrees() == initial.getLongitudeDegrees()
											.doubleValue())
								indexStart.set(eachIndex.get());
							else if (trackpoint.getPosition().getLatitudeDegrees() == end.getLatitudeDegrees()
									.doubleValue()
									&& trackpoint.getPosition().getLongitudeDegrees() == end.getLongitudeDegrees()
											.doubleValue())
								indexEnd.set(eachIndex.get());
							eachIndex.incrementAndGet();

						});
					});
					IntStream.range(indexStart.get(), indexEnd.get() + 1).forEach(index -> {
						TrackpointT trT = course.getTrack().get(0).getTrackpoint().get(index);
						TrackPoint trackpoint = new TrackPoint(trT.getTime().toGregorianCalendar().getTime(),
								indexTrackPoint.getAndIncrement(),
								new Position(new BigDecimal(trT.getPosition().getLatitudeDegrees()),
										new BigDecimal(trT.getPosition().getLongitudeDegrees())),
								new BigDecimal(trT.getAltitudeMeters().doubleValue()),
								!Objects.isNull(trT.getDistanceMeters())
										? new BigDecimal(trT.getDistanceMeters().doubleValue()) : null,
								null, !Objects.isNull(trT.getHeartRateBpm())
										? Integer.valueOf(trT.getHeartRateBpm().getValue()) : null);
						if (!Objects.isNull(trT.getExtensions()) && !Objects.isNull(trT.getExtensions().getAny())) {
							trT.getExtensions().getAny().stream()
									.filter(extension -> (!Objects.isNull(JAXBElement.class.cast(extension))
											&& !Objects.isNull(extension)
											&& !Objects.isNull((JAXBElement.class.cast(extension)).getValue())
											&& !Objects.isNull(ActivityTrackpointExtensionT.class
													.cast((JAXBElement.class.cast(extension)).getValue()))))
									.forEach(extension -> {
										ActivityTrackpointExtensionT actTrackpointExtension = ActivityTrackpointExtensionT.class
												.cast((JAXBElement.class.cast(extension)).getValue());
										if (!Objects.isNull(actTrackpointExtension.getSpeed())
												&& actTrackpointExtension.getSpeed() >= 0)
											trackpoint.setSpeed(new BigDecimal(actTrackpointExtension.getSpeed()));
									});
						}
						lap.addTrack(trackpoint);
					});
					// Calculate values not informed of a lap.
					lapsUtilsService.calculateLapValues(lap);
					activity.addLap(lap);
				});
				calculateDistanceSpeedValues(activity);
				activities.add(activity);
			});
		}
		return activities;
	}

	private void calculateDistanceSpeedValues(Activity activity) {
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
				lapsUtilsService.calculateDistanceLap(lap, previousLap.getTracks().get(previousLap.getTracks().size() - 1));
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
				lapsUtilsService.calculateSpeedLap(lap, previousLap.getTracks().get(previousLap.getTracks().size() - 1));
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
	 * @return index of the lapº
	 */
	private int indexOfALap(Activity activity, Position position, Long timeInMillis, Integer index) {
		List<Lap> laps = activity.getLaps();
		Lap lap = laps.stream().filter(eachLap -> {
			return lapsUtilsService.fulfillCriteriaPositionTime(eachLap, position, timeInMillis, index);
		}).findFirst().orElse(null);

		return !Objects.isNull(lap) ? laps.indexOf(lap) : -1;
	}

}
