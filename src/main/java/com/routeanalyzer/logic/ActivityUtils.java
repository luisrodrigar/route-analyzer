package com.routeanalyzer.logic;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

import org.apache.commons.lang3.SerializationUtils;
import org.springframework.context.ApplicationContext;
import org.springframework.web.multipart.MultipartFile;
import org.xml.sax.SAXParseException;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.routeanalyzer.config.ApplicationContextProvider;
import com.routeanalyzer.database.MongoDBJDBC;
import com.routeanalyzer.database.dao.ActivityDAO;
import com.routeanalyzer.model.Activity;
import com.routeanalyzer.model.Lap;
import com.routeanalyzer.model.Position;
import com.routeanalyzer.model.TrackPoint;
import com.routeanalyzer.services.AS3Service;
import com.routeanalyzer.services.XMLService;
import com.routeanalyzer.xml.gpx11.ExtensionsType;
import com.routeanalyzer.xml.gpx11.GpxType;
import com.routeanalyzer.xml.gpx11.MetadataType;
import com.routeanalyzer.xml.gpx11.TrackPointExtensionT;
import com.routeanalyzer.xml.gpx11.TrkType;
import com.routeanalyzer.xml.gpx11.TrksegType;
import com.routeanalyzer.xml.gpx11.WptType;
import com.routeanalyzer.xml.tcx.ActivityLapExtensionT;
import com.routeanalyzer.xml.tcx.ActivityLapT;
import com.routeanalyzer.xml.tcx.ActivityListT;
import com.routeanalyzer.xml.tcx.ActivityT;
import com.routeanalyzer.xml.tcx.ActivityTrackpointExtensionT;
import com.routeanalyzer.xml.tcx.ExtensionsT;
import com.routeanalyzer.xml.tcx.HeartRateInBeatsPerMinuteT;
import com.routeanalyzer.xml.tcx.IntensityT;
import com.routeanalyzer.xml.tcx.PositionT;
import com.routeanalyzer.xml.tcx.SportT;
import com.routeanalyzer.xml.tcx.TrackT;
import com.routeanalyzer.xml.tcx.TrackpointT;
import com.routeanalyzer.xml.tcx.TrainingCenterDatabaseT;

public class ActivityUtils {

	private static final String BUCKET_NAME = "xml-files-storage";
	private static AS3Service aS3Service = new AS3Service(BUCKET_NAME);

	private static XMLService reader = new XMLService();

	private static GsonBuilder gsonBuilder = new GsonBuilder();
	private static Gson gson = gsonBuilder.create();

	/**
	 * 
	 * @param multiPart
	 * @return
	 * @throws IOException
	 * @throws AmazonClientException
	 * @throws JAXBException
	 * @throws SAXParseException
	 */
	public static String uploadGPXFile(MultipartFile multiPart)
			throws IOException, AmazonClientException, JAXBException, SAXParseException {
		byte[] arrayBytes = multiPart.getBytes();
		InputStream inputFileGPX = multiPart.getInputStream();
		// Get the object from xml file (type GPX)
		GpxType gpx = reader.readXML(GpxType.class, inputFileGPX);
		// Create each activity of the file
		List<Activity> activities = getListActivitiesFromGPX(gpx);
		// Se guarda en la base de datos
		List<String> ids = saveActivity(activities, arrayBytes);
		return gson.toJson(ids);
	}

	/**
	 * 
	 * @param multiPart
	 * @return
	 * @throws IOException
	 * @throws JAXBException
	 * @throws SAXParseException
	 */
	public static String uploadTCXFile(MultipartFile multiPart) throws IOException, JAXBException, SAXParseException {
		byte[] arrayBytes = multiPart.getBytes();
		InputStream inputFileTCX = multiPart.getInputStream();
		// Get the object from xml file (type TCX)
		TrainingCenterDatabaseT tcx = reader.readXML(TrainingCenterDatabaseT.class, inputFileTCX);
		// Create each activity of the file
		List<Activity> activities = getListActivitiesFromTCX(tcx);
		// Se guarda en la base de datos
		List<String> ids = saveActivity(activities, arrayBytes);
		return gson.toJson(ids);
	}

	/**
	 * 
	 * @param nameFile
	 * @return
	 * @throws AmazonServiceException
	 * @throws AmazonClientException
	 * @throws IOException
	 */
	public static String getActivityAS3(String nameFile)
			throws AmazonServiceException, AmazonClientException, IOException {
		BufferedReader bufReader = aS3Service.getFile(nameFile);
		return bufReader.lines().collect(Collectors.joining("\n"));
	}

	/**
	 * 
	 * @param id
	 * @return
	 * @throws JAXBException
	 */
	public static String exportAsTCX(String id) throws JAXBException {
		Activity act = getActivityById(id);

		XMLService xmlService = new XMLService();
		com.routeanalyzer.xml.tcx.ObjectFactory oFactory = new com.routeanalyzer.xml.tcx.ObjectFactory();

		TrainingCenterDatabaseT trainingCenterDatabaseT = new TrainingCenterDatabaseT();
		ActivityListT actListT = new ActivityListT();
		ActivityT activityT = new ActivityT();
		// Sport is an enum
		if (act.getSport() != null) {
			SportT sport = SportT.valueOf(act.getSport());
			activityT.setSport(sport);
		}
		// Set xml gregorian calendar date
		if (act.getDate() != null) {
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
			avgBpm.setValue(lap.getAverageHearRate() != null && lap.getAverageHearRate().shortValue() > 0.0
					? lap.getAverageHearRate().shortValue() : 0);
			if (avgBpm.getValue() > 0)
				lapT.setAverageHeartRateBpm(avgBpm);
			// calories
			if (lap.getCalories() != null)
				lapT.setCalories(lap.getCalories());
			// distance in meters
			if (lap.getDistanceMeters() != null)
				lapT.setDistanceMeters(lap.getDistanceMeters());
			// max speed in meters per second
			if (lap.getMaximunSpeed() != null)
				lapT.setMaximumSpeed(lap.getMaximunSpeed());
			// max heart rate in beats per minute
			HeartRateInBeatsPerMinuteT maxBpm = new HeartRateInBeatsPerMinuteT();
			maxBpm.setValue(lap.getMaximunHeartRate() != null && lap.getMaximunHeartRate().shortValue() > 0
					? lap.getMaximunHeartRate().shortValue() : 0);
			if (maxBpm.getValue() > 0)
				lapT.setMaximumHeartRateBpm(maxBpm);
			// Start time in xml gregorian calendar
			if (lap.getStartTime() != null) {
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
			if (lap.getTotalTimeSeconds() != null && lap.getTotalTimeSeconds() > 0)
				lapT.setTotalTimeSeconds(lap.getTotalTimeSeconds());
			// intensity
			if (lap.getIntensity() != null)
				lapT.setIntensity(IntensityT.valueOf(lap.getIntensity()));

			if (lap.getAverageSpeed() != null) {
				ExtensionsT extT = new ExtensionsT();
				ActivityLapExtensionT actExtensionT = new ActivityLapExtensionT();
				actExtensionT.setAvgSpeed(lap.getAverageSpeed());
				extT.addAny(oFactory.createLX(actExtensionT));
				lapT.setExtensions(extT);
			}
			TrackT trackT = new TrackT();
			lap.getTracks().forEach(trackPoint -> {
				TrackpointT trackPointT = new TrackpointT();
				if (trackPoint.getDate() != null) {
					GregorianCalendar gregorian = new GregorianCalendar();
					gregorian.setTime(trackPoint.getDate());
					try {
						trackPointT.setTime(DatatypeFactory.newInstance().newXMLGregorianCalendar(gregorian));
					} catch (DatatypeConfigurationException e) {
						trackPointT.setTime(null);
					}
				}
				if (trackPoint.getPosition() != null) {
					PositionT positionT = new PositionT();
					positionT.setLatitudeDegrees(Double.parseDouble(trackPoint.getPosition().getLatitudeDegrees()));
					positionT.setLongitudeDegrees(Double.parseDouble(trackPoint.getPosition().getLongitudeDegrees()));
					trackPointT.setPosition(positionT);
				}
				trackPointT.setAltitudeMeters(
						trackPoint.getAltitudeMeters() != null ? trackPoint.getAltitudeMeters().doubleValue() : null);
				trackPointT.setDistanceMeters(
						trackPoint.getDistanceMeters() != null ? trackPoint.getDistanceMeters().doubleValue() : null);
				if (trackPoint.getHeartRateBpm() != null) {
					HeartRateInBeatsPerMinuteT bpm = new HeartRateInBeatsPerMinuteT();
					bpm.setValue(trackPoint.getHeartRateBpm().shortValue());
					trackPointT.setHeartRateBpm(bpm);
				}
				if (trackPoint.getSpeed() != null) {
					ExtensionsT extension = new ExtensionsT();

					ActivityTrackpointExtensionT trackPointExtension = new ActivityTrackpointExtensionT();
					trackPointExtension.setSpeed(trackPoint.getSpeed().doubleValue());
					extension.addAny(oFactory.createTPX(trackPointExtension));
					trackPointT.setExtensions(extension);
				}
				trackT.addTrackpoint(trackPointT);
			});
			lapT.addTrack(trackT);
			activityT.addLap(lapT);
		});
		actListT.addActivity(activityT);
		trainingCenterDatabaseT.setActivities(actListT);

		return xmlService.createXML(TrainingCenterDatabaseT.class,
				oFactory.createTrainingCenterDatabase(trainingCenterDatabaseT));
	}

	/**
	 * 
	 * @param id
	 * @return
	 * @throws JAXBException
	 */
	public static String exportAsGPX(String id) throws JAXBException {
		Activity act = getActivityById(id);

		XMLService xmlService = new XMLService();
		com.routeanalyzer.xml.gpx11.ObjectFactory oFactory = new com.routeanalyzer.xml.gpx11.ObjectFactory();

		GpxType gpx = new GpxType();
		if (act.getDate() != null) {
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
				if (trackPoint.getDate() != null) {
					GregorianCalendar gregorianCalendar = new GregorianCalendar();
					gregorianCalendar.setTime(trackPoint.getDate());
					try {
						wpt.setTime(DatatypeFactory.newInstance().newXMLGregorianCalendar(gregorianCalendar));
					} catch (DatatypeConfigurationException e) {
						wpt.setTime(null);
					}
				}
				wpt.setLat(trackPoint.getPosition() != null && trackPoint.getPosition().getLatitudeDegrees() != null
						? new BigDecimal(trackPoint.getPosition().getLatitudeDegrees()) : null);
				wpt.setLon(trackPoint.getPosition() != null && trackPoint.getPosition().getLongitudeDegrees() != null
						? new BigDecimal(trackPoint.getPosition().getLongitudeDegrees()) : null);
				wpt.setEle(trackPoint.getAltitudeMeters() != null ? trackPoint.getAltitudeMeters() : null);
				if (trackPoint.getHeartRateBpm() != null) {
					ExtensionsType extensions = new ExtensionsType();
					TrackPointExtensionT trptExtension = new TrackPointExtensionT();
					trptExtension.setHr(trackPoint.getHeartRateBpm().shortValue());
					extensions.addAny(oFactory.createTrackPointExtension(trptExtension));
					wpt.setExtensions(extensions);
				}
				trkSeg.addTrkpt(wpt);
			});
			trk.addTrkseg(trkSeg);
		});
		gpx.addTrk(trk);

		return xmlService.createXML(GpxType.class, oFactory.createGpx(gpx));
	}

	/**
	 * Remove point: - Remove lap if it is the last point of the lap - Split lap
	 * into two ones if the point is between start point and end point (not
	 * included). - Remove point if point is start or end and modify gloval
	 * values of the lap
	 * 
	 * @param id
	 *            of the activity
	 * @param lat
	 *            of the position
	 * @param lng
	 *            of the position
	 * @param timeInMillis:
	 *            time in milliseconds
	 * @param index:
	 *            order of creation
	 * @return activity or null if there was any error.
	 */
	public static Activity removePoint(String id, String lat, String lng, String timeInMillis, String indexTrackPoint) {
		ActivityDAO activityDAO = getActivityDAO();
		Activity act = activityDAO.readById(id);

		Long time = !Objects.isNull(timeInMillis) && !timeInMillis.isEmpty() ? Long.parseLong(timeInMillis) : null;
		Integer index = !Objects.isNull(indexTrackPoint) && !indexTrackPoint.isEmpty()
				? Integer.parseInt(indexTrackPoint) : null;

		Position position = new Position(lat, lng);

		// Remove element from the laps stored in state
		// If it is not at the begining or in the end of a lap
		// the lap splits into two new ones.
		Integer indexLap = indexOfALap(act, position, time, index);
		if (indexLap > -1) {
			Integer indexPosition = LapsUtils.indexOfTrackpoint(act, indexLap, position, time, index);
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
				// Reset speed and distance of the next track point of the removed track point
				// calculateDistanceSpeedValues calculate this params.
				if(indexPosition.intValue()<newLap.getTracks().size()-1){
					newLap.getTracks().get(indexPosition.intValue()+1).setSpeed(null);
					newLap.getTracks().get(indexPosition.intValue()+1).setDistanceMeters(null);
				}
				// Remove track point
				newLap.getTracks().remove(indexPosition.intValue());
				act.getLaps().remove(indexLap.intValue());
				act.getLaps().add(indexLap.intValue(), newLap);
				LapsUtils.resetAggregateValues(newLap);
				LapsUtils.resetTotals(newLap);
				calculateDistanceSpeedValues(act);
				LapsUtils.setTotalValuesLap(newLap);
				LapsUtils.calculateAggregateValuesLap(newLap);
			}

			activityDAO.update(act);

			return act;
		} else
			return null;
	}

	/**
	 * Split a lap into two laps with one track point as the divider.
	 * 
	 * @param id
	 *            of the activity
	 * @param indexLap
	 *            index lap to split up
	 * @param indexPosition
	 *            of the track point which will be the divider
	 * @return activity with the new laps.
	 */
	public static Activity splitLap(String id, String lat, String lng, String timeInMillis, String indexTrackPoint) {
		ActivityDAO activityDAO = getActivityDAO();
		Activity act = activityDAO.readById(id);
		System.out.println("Activity: " + act);
		Long time = !Objects.isNull(timeInMillis) && !timeInMillis.isEmpty() ? Long.parseLong(timeInMillis) : null;
		Integer index = !Objects.isNull(indexTrackPoint) && !indexTrackPoint.isEmpty()
				? Integer.parseInt(indexTrackPoint) : null;

		Position position = new Position(lat, lng);

		Integer indexLap = indexOfALap(act, position, time, index);
		if (indexLap > -1) {
			Integer indexPosition = LapsUtils.indexOfTrackpoint(act, indexLap, position, time, index);
			Lap lap = act.getLaps().get(indexLap);
			int sizeOfTrackPoints = lap.getTracks().size();

			// Track point not start nor end. Thus, it split into two laps
			if (indexPosition > 0 && indexPosition < sizeOfTrackPoints - 1) {

				Lap lapSplitLeft = SerializationUtils.clone(act.getLaps().get(indexLap));
				LapsUtils.createSplittLap(lap, lapSplitLeft, 0, indexPosition);
				// Index the same original lap
				lapSplitLeft.setIndex(lap.getIndex());
				
				Lap lapSplitRight = SerializationUtils.clone(act.getLaps().get(indexLap));
				// lap right: add right elements and the current track point
				// which has index = index position
				LapsUtils.createSplittLap(lap, lapSplitRight, indexPosition, sizeOfTrackPoints);
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

				activityDAO.update(act);
				return act;
			} else
				return null;
		} else
			return null;
	}

	/**
	 * 
	 * @param idActivity
	 * @param indexLap1
	 * @param indexLap2
	 * @return
	 */
	public static Activity joinLap(String idActivity, Integer indexLap1, Integer indexLap2) {
		ActivityDAO activityDAO = getActivityDAO();
		Activity act = activityDAO.readById(idActivity);

		if (Objects.isNull(indexLap1) || Objects.isNull(indexLap2))
			return null;
		int indexLapLeft = indexLap1, indexLapRight = indexLap2;
		if (indexLap1.compareTo(indexLap2) > 0) {
			indexLapLeft = indexLap2;
			indexLapRight = indexLap1;
		}

		Lap lapLeft = act.getLaps().get(indexLapLeft);
		Lap lapRight = act.getLaps().get(indexLapRight);

		Lap newLap = LapsUtils.joinLaps(lapLeft, lapRight);

		act.getLaps().remove(lapLeft);
		act.getLaps().remove(lapRight);

		act.getLaps().add(indexLapLeft, newLap);

		IntStream.range(indexLapRight, act.getLaps().size()).forEach(indexEachLap -> {
			act.getLaps().get(indexEachLap).setIndex(act.getLaps().get(indexEachLap).getIndex() - 1);
		});

		activityDAO.update(act);

		return act;
	}

	/**
	 * 
	 * @param act
	 * @param startTime
	 * @param indexLap
	 * @return
	 */
	public static Activity removeLap(Activity act, Long startTime, Integer indexLap) {
		Lap lapToDelete = act.getLaps().stream().filter((lap) -> {
			return lap.getIndex() == indexLap
					&& (!Objects.isNull(startTime) ? startTime == lap.getStartTime().getTime() : true);
		}).findFirst().orElse(null);

		act.getLaps().remove(lapToDelete);

		return act;
	}

	/**
	 * 
	 * @return
	 */
	public static ActivityDAO getActivityDAO() {
		ApplicationContext ctxt = (ApplicationContext) ApplicationContextProvider.getApplicationContext();
		MongoDBJDBC mongoDBJDBC = (MongoDBJDBC) ctxt.getBean("mongoDBJDBC");
		return mongoDBJDBC.getActivityDAOImpl();
	}

	/**
	 * 
	 * @param gpx
	 * @return
	 */
	private static List<Activity> getListActivitiesFromGPX(GpxType gpx) {
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
								new Position(String.valueOf(eachTrackPoint.getLat()),
										String.valueOf(eachTrackPoint.getLon())),
								eachTrackPoint.getEle(), null, null, null);
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
				LapsUtils.calculateLapValues(lap);
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
	private static List<Activity> getListActivitiesFromTCX(TrainingCenterDatabaseT tcx) {

		List<Activity> activities = new ArrayList<Activity>();

		AtomicInteger indexLap = new AtomicInteger(), indexTrackPoint = new AtomicInteger();

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
					eachLap.getExtensions().getAny().stream().filter(extension -> (!Objects.isNull(extension)
							&& !Objects.isNull(JAXBElement.class.cast(extension))
							&& !Objects.isNull(JAXBElement.class.cast(extension).getValue())
							&& !Objects.isNull(
									ActivityLapExtensionT.class.cast(JAXBElement.class.cast(extension).getValue()))))
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
													String.valueOf(trackPoint.getPosition().getLatitudeDegrees()),
													String.valueOf(trackPoint.getPosition().getLongitudeDegrees()))
											: null),
									!Objects.isNull(trackPoint) && !Objects.isNull(trackPoint.getAltitudeMeters())
											? new BigDecimal(trackPoint.getAltitudeMeters()) : null,
									!Objects.isNull(trackPoint) && !Objects.isNull(trackPoint.getDistanceMeters())
											? new BigDecimal(trackPoint.getDistanceMeters()) : null,
									null, !Objects.isNull(trackPoint) && !Objects.isNull(trackPoint.getHeartRateBpm())
											? new Integer(trackPoint.getHeartRateBpm().getValue()) : null);
							if (!Objects.isNull(trackPoint.getExtensions())
									&& !Objects.isNull(trackPoint.getExtensions().getAny())
									&& !trackPoint.getExtensions().getAny().isEmpty()) {
								trackPoint.getExtensions().getAny().stream()
										.filter(extension -> (!Objects.isNull(JAXBElement.class.cast(extension))
												&& extension != null
												&& !Objects.isNull((JAXBElement.class.cast(extension)).getValue())
												&& !Objects.isNull(ActivityTrackpointExtensionT.class
														.cast((JAXBElement.class.cast(extension)).getValue()))))
										.forEach(extension -> {
											ActivityTrackpointExtensionT actTrackpointExtension = ActivityTrackpointExtensionT.class
													.cast((JAXBElement.class.cast(extension)).getValue());
											if (!Objects.isNull(actTrackpointExtension.getSpeed())
													&& actTrackpointExtension.getSpeed() > 0)
												trp.setSpeed(new BigDecimal(actTrackpointExtension.getSpeed()));
										});
							}
							lap.addTrack(trp);
						}
					});
				});
				// Calculate values not informed of a lap.
				LapsUtils.calculateLapValues(lap);
				activity.addLap(lap);
			});
			calculateDistanceSpeedValues(activity);
			activities.add(activity);
		});
		return activities;
	}

	private static void calculateDistanceSpeedValues(Activity activity) {
		boolean hasActivityDateTrackPointValues = hasActivityTrackPointsValue(activity,
				track -> !Objects.isNull(track.getDate()));
		if(hasActivityDateTrackPointValues){
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

	private static void calculateDistanceMeters(Activity activity) {
		List<Lap> laps = activity.getLaps();
		laps.forEach(lap -> {
			int indexLap = laps.indexOf(lap);
			switch (indexLap) {
			case 0:
				LapsUtils.calculateDistanceLap(lap, null);
				break;
			default:
				Lap previousLap = laps.get(indexLap - 1);
				LapsUtils.calculateDistanceLap(lap, previousLap.getTracks().get(previousLap.getTracks().size() - 1));
				break;
			}
		});
	}

	private static void calculateSpeedValues(Activity activity) {
		List<Lap> laps = activity.getLaps();
		laps.forEach(lap -> {
			int indexLap = laps.indexOf(lap);
			switch (indexLap) {
			case 0:
				LapsUtils.calculateSpeedLap(lap, null);
				break;
			default:
				Lap previousLap = laps.get(indexLap - 1);
				LapsUtils.calculateSpeedLap(lap, previousLap.getTracks().get(previousLap.getTracks().size() - 1));
				break;
			}
		});
	}

	private static boolean hasActivityTrackPointsValue(Activity activity, Function<TrackPoint, Boolean> function) {
		return activity.getLaps().stream()
				.map(lap -> lap.getTracks().stream().map(function).reduce(Boolean::logicalAnd))
				.map(isValue -> isValue.orElse(false)).reduce(Boolean::logicalAnd).orElse(false);
	}

	/**
	 * 
	 * @param activities
	 * @param arrayBytes
	 * @return
	 * @throws AmazonClientException
	 */
	private static List<String> saveActivity(List<Activity> activities, byte[] arrayBytes)
			throws AmazonClientException {
		List<String> ids = new ArrayList<String>();
		ActivityDAO activityDAO = getActivityDAO();
		activities.forEach(activity -> {
			activityDAO.create(activity);
			ids.add(activity.getId());
			try {
				aS3Service.uploadFile(arrayBytes, activity.getId() + "." + activity.getSourceXmlType());
			} catch (AmazonClientException aS3Exception) {
				System.err.println("Delete activity with id: " + activity.getId()
						+ " due to problems trying to upload file to AS3.");
				activityDAO.deleteById(activity.getId());
				throw aS3Exception;
			}
		});
		return ids;
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
	private static int indexOfALap(Activity activity, Position position, Long timeInMillis, Integer index) {
		List<Lap> laps = activity.getLaps();
		Lap lap = laps.stream().filter(eachLap -> {
			return LapsUtils.fulfillCriteriaPositionTime(eachLap, position, timeInMillis, index);
		}).findFirst().orElse(null);

		return !Objects.isNull(lap) ? laps.indexOf(lap) : -1;
	}

	/**
	 * 
	 * @param id
	 * @return
	 */
	private static Activity getActivityById(String id) {
		return getActivityDAO().readById(id);
	}

}
