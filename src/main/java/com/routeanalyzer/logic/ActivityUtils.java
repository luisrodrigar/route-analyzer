package com.routeanalyzer.logic;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
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

	public static String uploadGPXFile(MultipartFile multiPart)
			throws IOException, AmazonClientException, JAXBException, SAXParseException {
		List<String> ids = new ArrayList<String>();

		byte[] arrayBytes = multiPart.getBytes();
		InputStream inputFileGPX = multiPart.getInputStream();

		GpxType gpx = reader.readXML(GpxType.class, inputFileGPX);

		List<Activity> activities = new ArrayList<Activity>();
		AtomicInteger indexLap = new AtomicInteger(), indexTrackPoint = new AtomicInteger();
		gpx.getTrk().forEach(track -> {
			Activity activity = new Activity();
			activity.setSourceXmlType("gpx");
			activity.setDate(gpx.getMetadata() != null && gpx.getMetadata().getTime() != null && indexLap.get() == 0
					? gpx.getMetadata().getTime().toGregorianCalendar().getTime()
					: (track.getTrkseg() != null && track.getTrkseg().get(0) != null
							&& track.getTrkseg().get(0).getTrkpt() != null
							&& track.getTrkseg().get(0).getTrkpt().get(0) != null
							&& track.getTrkseg().get(0).getTrkpt().get(0).getTime() != null
									? track.getTrkseg().get(0).getTrkpt().get(0).getTime().toGregorianCalendar()
											.getTime()
									: null));
			activity.setDevice(gpx.getCreator());
			activity.setName(track.getName() != null ? track.getName().trim() : null);
			track.getTrkseg().forEach(eachLap -> {
				Lap lap = new Lap();
				if (eachLap.getTrkpt() != null && !eachLap.getTrkpt().isEmpty()
						&& eachLap.getTrkpt().get(0).getTime() != null)
					lap.setStartTime(eachLap.getTrkpt().get(0).getTime().toGregorianCalendar().getTime());
				lap.setIndex(indexLap.incrementAndGet());
				eachLap.getTrkpt().forEach(eachTrackPoint -> {
					// Adding track point only if position is informed
					if(eachTrackPoint.getLat()!=null && eachTrackPoint.getLon()!=null){
						TrackPoint tkp = new TrackPoint(
								(eachTrackPoint.getTime() != null ? eachTrackPoint.getTime().toGregorianCalendar().getTime()
										: null),
								indexTrackPoint.incrementAndGet(),
								new Position(String.valueOf(eachTrackPoint.getLat()),
										String.valueOf(eachTrackPoint.getLon())),
								eachTrackPoint.getEle(), null, null, null);
						if (eachTrackPoint.getExtensions() != null) {
							eachTrackPoint.getExtensions().getAny().stream()
									.filter(item -> (JAXBElement.class.cast(item) != null && item != null
											&& JAXBElement.class.cast(item).getValue() != null
											&& TrackPointExtensionT.class
													.cast(JAXBElement.class.cast(item).getValue()) != null))
									.forEach(item -> {
										TrackPointExtensionT trpExt = TrackPointExtensionT.class
												.cast((JAXBElement.class.cast(item)).getValue());
										if (trpExt.getHr() != null)
											tkp.setHeartRateBpm(trpExt.getHr().intValue());
									});
						}
						lap.addTrack(tkp);
					}
				});
				activity.addLap(lap);
			});
			// Fill altitude fields if it does not exist.
			LapsUtils.calculateAltitude(activity.getLaps());
			activities.add(activity);
		});

		// Se guarda en la base de datos
		ApplicationContext ctxt = (ApplicationContext) ApplicationContextProvider.getApplicationContext();
		MongoDBJDBC mongoDBJDBC = (MongoDBJDBC) ctxt.getBean("mongoDBJDBC");
		ActivityDAO activityDAO = mongoDBJDBC.getActivityDAOImpl();
		activities.forEach(activity -> {
			activityDAO.create(activity);
			ids.add(activity.getId());
			try {
				aS3Service.uploadFile(arrayBytes, activity.getId() + ".gpx");
			} catch (AmazonClientException aS3Exception) {
				System.err.println("Delete activity with id: " + activity.getId()
						+ " due to problems trying to upload file to AS3.");
				int result = activityDAO.deleteById(activity.getId());
				assert result == 1 : String.format("Result value of delete must be (%d)", 1);
				throw aS3Exception;
			}

		});

		return gson.toJson(ids);
	}

	public static String uploadTCXFile(MultipartFile multiPart) throws IOException, JAXBException, SAXParseException {
		byte[] arrayBytes = multiPart.getBytes();
		List<String> ids = new ArrayList<String>();
		AtomicInteger indexLap = new AtomicInteger(), indexTrackPoint = new AtomicInteger();

		InputStream inputFileTCX = multiPart.getInputStream();
		List<Activity> activities = new ArrayList<Activity>();
		TrainingCenterDatabaseT tcx = reader.readXML(TrainingCenterDatabaseT.class, inputFileTCX);
		tcx.getActivities().getActivity().forEach(eachActivity -> {
			Activity activity = new Activity();
			activity.setSourceXmlType("tcx");
			if (eachActivity.getCreator() != null)
				activity.setDevice(eachActivity.getCreator().getName());
			if (eachActivity.getId() != null)
				activity.setDate(eachActivity.getId().toGregorianCalendar().getTime());
			if (eachActivity.getSport() != null)
				activity.setSport(eachActivity.getSport().toString());
			eachActivity.getLap().forEach(eachLap -> {
				Lap lap = new Lap();
				if (eachLap.getAverageHeartRateBpm() != null && eachLap.getAverageHeartRateBpm().getValue() > 0)
					lap.setAverageHearRate(new Double(eachLap.getAverageHeartRateBpm().getValue()));
				if (eachLap.getCalories() > 0)
					lap.setCalories(eachLap.getCalories());
				if (eachLap.getDistanceMeters() > 0)
					lap.setDistanceMeters(eachLap.getDistanceMeters());
				if (eachLap.getMaximumSpeed() != null && eachLap.getMaximumSpeed() > 0)
					lap.setMaximunSpeed(eachLap.getMaximumSpeed());
				if (eachLap.getMaximumHeartRateBpm() != null && eachLap.getMaximumHeartRateBpm().getValue() > 0)
					lap.setMaximunHeartRate(new Integer(eachLap.getMaximumHeartRateBpm().getValue()));
				if (eachLap.getStartTime() != null)
					lap.setStartTime(eachLap.getStartTime().toGregorianCalendar().getTime());
				lap.setIndex(indexLap.incrementAndGet());
				if (eachLap.getTotalTimeSeconds() > 0.0)
					lap.setTotalTimeSeconds(eachLap.getTotalTimeSeconds());
				if (eachLap.getIntensity() != null)
					lap.setIntensity(eachLap.getIntensity().toString());
				if (eachLap.getExtensions() != null && eachLap.getExtensions().getAny() != null
						&& !eachLap.getExtensions().getAny().isEmpty()) {
					eachLap.getExtensions().getAny().stream()
							.filter(extension -> (extension != null && JAXBElement.class.cast(extension) != null
									&& JAXBElement.class.cast(extension).getValue() != null
									&& ActivityLapExtensionT.class
											.cast(JAXBElement.class.cast(extension).getValue()) != null))
							.forEach(extension -> {
								ActivityLapExtensionT actTrackpointExtension = ActivityLapExtensionT.class
										.cast(JAXBElement.class.cast(extension).getValue());
								lap.setAverageSpeed(actTrackpointExtension.getAvgSpeed());
							});
				}
				eachLap.getTrack().forEach(track -> {
					track.getTrackpoint().forEach(trackPoint -> {
						// Adding track point only if position is informed
						if(trackPoint.getPosition()!=null){
							TrackPoint trp = new TrackPoint(
									(trackPoint.getTime() != null ? trackPoint.getTime().toGregorianCalendar().getTime()
											: null),
									indexTrackPoint
											.incrementAndGet(),
									(trackPoint != null && trackPoint.getPosition() != null
											? new Position(String.valueOf(trackPoint.getPosition().getLatitudeDegrees()),
													String.valueOf(trackPoint.getPosition().getLongitudeDegrees()))
											: null),
									trackPoint != null && trackPoint.getAltitudeMeters() != null
											? new BigDecimal(trackPoint.getAltitudeMeters()) : null,
									trackPoint != null && trackPoint.getDistanceMeters() != null
											? new BigDecimal(trackPoint.getDistanceMeters()) : null,
									null, trackPoint != null && trackPoint.getHeartRateBpm() != null
											? new Integer(trackPoint.getHeartRateBpm().getValue()) : null);
							if (trackPoint.getExtensions() != null && trackPoint.getExtensions().getAny() != null
									&& !trackPoint.getExtensions().getAny().isEmpty()) {
								trackPoint.getExtensions().getAny().stream()
										.filter(extension -> (JAXBElement.class.cast(extension) != null && extension != null
												&& (JAXBElement.class.cast(extension)).getValue() != null
												&& ActivityTrackpointExtensionT.class
														.cast((JAXBElement.class.cast(extension)).getValue()) != null))
										.forEach(extension -> {
											ActivityTrackpointExtensionT actTrackpointExtension = ActivityTrackpointExtensionT.class
													.cast((JAXBElement.class.cast(extension)).getValue());
											if (actTrackpointExtension.getSpeed() != null
													&& actTrackpointExtension.getSpeed() > 0)
												trp.setSpeed(new BigDecimal(actTrackpointExtension.getSpeed()));
										});
							}
							lap.addTrack(trp);
						}	
					});
				});
				activity.addLap(lap);
			});
			activities.add(activity);
		});

		// Se guarda en la base de datos
		ApplicationContext ctxt = (ApplicationContext) ApplicationContextProvider.getApplicationContext();
		MongoDBJDBC mongoDBJDBC = (MongoDBJDBC) ctxt.getBean("mongoDBJDBC");
		ActivityDAO activityDAO = mongoDBJDBC.getActivityDAOImpl();
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

		return gson.toJson(ids);
	}

	public static String getActivityAS3(String nameFile)
			throws AmazonServiceException, AmazonClientException, IOException {
		BufferedReader bufReader = aS3Service.getFile(nameFile);
		return bufReader.lines().collect(Collectors.joining("\n"));
	}

	public static String exportAsTCX(String id) throws JAXBException {
		ApplicationContext ctxt = (ApplicationContext) ApplicationContextProvider.getApplicationContext();
		MongoDBJDBC mongoDBJDBC = (MongoDBJDBC) ctxt.getBean("mongoDBJDBC");
		ActivityDAO activityDAO = mongoDBJDBC.getActivityDAOImpl();
		Activity act = activityDAO.readById(id);

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

	public static String exportAsGPX(String id) throws JAXBException {
		ApplicationContext ctxt = (ApplicationContext) ApplicationContextProvider.getApplicationContext();
		MongoDBJDBC mongoDBJDBC = (MongoDBJDBC) ctxt.getBean("mongoDBJDBC");
		ActivityDAO activityDAO = mongoDBJDBC.getActivityDAOImpl();
		Activity act = activityDAO.readById(id);

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
	public static Activity removePoint(String id, String lat, String lng, String timeInMillis, String index) {
		ApplicationContext ctxt = (ApplicationContext) ApplicationContextProvider.getApplicationContext();
		MongoDBJDBC mongoDBJDBC = (MongoDBJDBC) ctxt.getBean("mongoDBJDBC");
		ActivityDAO activityDAO = mongoDBJDBC.getActivityDAOImpl();
		Activity act = activityDAO.readById(id);

		// Remove element from the laps stored in state
		// If it is not at the begining or in the end of a lap
		// the lap splits into two new ones.
		Integer indexLap = LapsUtils.getIndexOfALap(act.getLaps(), lat, lng, timeInMillis, index);
		if (indexLap > -1) {
			Integer indexPosition = LapsUtils.getIndexOfAPosition(act.getLaps(), indexLap, lat, lng, timeInMillis,
					index);
			int sizeOfTrackPoints = act.getLaps().get(indexLap).getTracks().size();

			// Track point not start nor end. Thus, it slipt into two laps
			if (indexPosition > 0 && indexPosition < sizeOfTrackPoints - 1) {
				Lap lapSplitLeft = SerializationUtils.clone(act.getLaps().get(indexLap));
				Lap lapSplitRight = SerializationUtils.clone(act.getLaps().get(indexLap));

				// lap left: Remove elements of the right of the track point to
				// delete
				List<TrackPoint> leftTrackPoints = new ArrayList<TrackPoint>();
				IntStream.range(0, indexPosition).forEach(indexEachTrackPoint -> {
					leftTrackPoints.add(act.getLaps().get(indexLap).getTracks().get(indexEachTrackPoint));
				});
				lapSplitLeft.setTracks(leftTrackPoints);
				// Se cambia el color
				lapSplitLeft.setColor(null);
				lapSplitLeft.setLightColor(null);

				// lap right: Remove elements of the left of the track point to
				// delete, include index of this
				List<TrackPoint> rightTrackPoints = new ArrayList<TrackPoint>();
				IntStream.range(indexPosition + 1, sizeOfTrackPoints).forEach(indexEachTrackPoint -> {
					rightTrackPoints.add(act.getLaps().get(indexLap).getTracks().get(indexEachTrackPoint));
				});
				lapSplitRight.setTracks(rightTrackPoints);
				// Se cambia el color
				lapSplitRight.setColor(null);
				lapSplitRight.setLightColor(null);

				LapsUtils.recalculateLapValues(lapSplitLeft);
				LapsUtils.recalculateLapValues(lapSplitRight);

				lapSplitLeft
						.setCalories(act.getLaps().get(indexLap).getCalories() != null
								? new Long(Math.round(Double.valueOf(lapSplitLeft.getTracks().size())
										/ sizeOfTrackPoints * act.getLaps().get(indexLap).getCalories())).intValue()
								: null);
				lapSplitRight
						.setCalories(
								act.getLaps().get(indexLap).getCalories() != null
										? new Long(Math.round(
												(Double.valueOf(lapSplitRight.getTracks().size()) / sizeOfTrackPoints)
														* act.getLaps().get(indexLap).getCalories())).intValue()
										: null);
				lapSplitRight.setIndex(act.getLaps().get(indexLap).getIndex() + 1);
				// Increment a point the index field of the next elements of the
				// element to delete
				IntStream.range(indexLap + 1, act.getLaps().size()).forEach(indexEachLap -> {
					act.getLaps().get(indexEachLap).setIndex(act.getLaps().get(indexEachLap).getIndex() + 1);
				});

				Lap lapRemoved = act.getLaps().remove(indexLap.intValue());
				if (lapRemoved != null) {
					act.getLaps().add(indexLap.intValue(), lapSplitLeft);
					act.getLaps().add((indexLap.intValue() + 1), lapSplitRight);
				} else
					return null;

			}
			// The lap is only one track point.
			// Delete and delete the lap
			else if (indexPosition == 0 && sizeOfTrackPoints == 1) {
				act.getLaps().get(indexLap.intValue()).getTracks().remove(indexPosition.intValue());
				act.getLaps().remove(indexLap.intValue());
			}
			// Start or end, it does not split into two laps, just delete the
			// track point and recalculate values of the lap.
			else {
				Lap newLap = SerializationUtils.clone(act.getLaps().get(indexLap));
				newLap.getTracks().remove(indexPosition.intValue());
				LapsUtils.recalculateLapValues(newLap);
				act.getLaps().remove(indexLap.intValue());
				act.getLaps().add(indexLap.intValue(), newLap);
			}

			activityDAO.update(act);

			return act;
		} else
			return null;
	}

	public static Activity removeLap(Activity act, Long startTime, Integer indexLap) {

		Lap lapToDelete = act.getLaps().stream().filter((lap) -> {
			return lap.getIndex() == indexLap
					&& (startTime != null
							? startTime == lap.getStartTime().getTime() : true);
		}).findFirst().orElse(null);
		
		act.getLaps().remove(lapToDelete);
	
		return act;
	}

}
