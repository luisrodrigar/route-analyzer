package com.routeanalyzer.controller.rest;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.DoubleAdder;
import java.util.stream.IntStream;

import javax.xml.bind.JAXBException;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

import org.apache.commons.lang3.SerializationUtils;
import org.springframework.context.ApplicationContext;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.routeanalyzer.config.ApplicationContextProvider;
import com.routeanalyzer.database.MongoDBJDBC;
import com.routeanalyzer.database.dao.ActivityDAO;
import com.routeanalyzer.model.Activity;
import com.routeanalyzer.model.Lap;
import com.routeanalyzer.model.Position;
import com.routeanalyzer.model.TrackPoint;
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

@CrossOrigin(origins = "http://localhost:3000", maxAge = 3600)
@RestController
@RequestMapping("activity")
public class ActivityRestController {

	@RequestMapping(value = "/{id}", method = RequestMethod.GET, produces = "application/json;")
	public @ResponseBody ResponseEntity<Activity> getActivityById(@PathVariable String id) {
		ApplicationContext ctxt = (ApplicationContext) ApplicationContextProvider.getApplicationContext();
		MongoDBJDBC mongoDBJDBC = (MongoDBJDBC) ctxt.getBean("mongoDBJDBC");
		ActivityDAO activityDAO = mongoDBJDBC.getActivityDAOImpl();
		Activity act = activityDAO.readById(id);
		return act != null ? new ResponseEntity<Activity>(act, HttpStatus.ACCEPTED)
				: new ResponseEntity<Activity>(act, HttpStatus.INTERNAL_SERVER_ERROR);
	}

	@RequestMapping(value = "/{id}/export/{type}", method = RequestMethod.GET)
	public ResponseEntity<String> exportAs(@PathVariable final String id, @PathVariable final String type) {
		switch (type.toLowerCase()) {
		case "tcx":
			try {
				HttpHeaders responseHeaders = new HttpHeaders();
				responseHeaders.add("Content-Type", "application/octet-stream");
				responseHeaders.add("Content-Disposition", "attachment;filename=" + id + "_tcx.xml");
				return new ResponseEntity<String>(exportAsTCX(id), responseHeaders, HttpStatus.ACCEPTED);
			} catch (JAXBException e1) {
				HttpHeaders responseHeaders = new HttpHeaders();
				responseHeaders.add("Content-Type", "application/json; charset=utf-8");
				String errorValue = "{" + "\"error\":true,"
						+ "\"description\":\"Problem with the file format uploaded.\"," + "\"exception\":\""
						+ e1.getMessage() + "\"" + "}";
				return new ResponseEntity<String>(errorValue, responseHeaders, HttpStatus.INTERNAL_SERVER_ERROR);
			}
		case "gpx":
			try {
				HttpHeaders responseHeaders = new HttpHeaders();
				responseHeaders.add("Content-Type", "application/octet-stream");
				responseHeaders.add("Content-Disposition", "attachment;filename=" + id + "_gpx.xml");
				return new ResponseEntity<String>(exportAsGPX(id), responseHeaders, HttpStatus.ACCEPTED);
			} catch (JAXBException e) {
				HttpHeaders responseHeaders = new HttpHeaders();
				responseHeaders.add("Content-Type", "application/json; charset=utf-8");
				String errorValue = "{" + "\"error\":true,"
						+ "\"description\":\"Problem with the file format uploaded.\"," + "\"exception\":\""
						+ e.getMessage() + "\"" + "}";
				return new ResponseEntity<String>(errorValue, responseHeaders, HttpStatus.INTERNAL_SERVER_ERROR);
			}
		default:
			HttpHeaders responseHeaders = new HttpHeaders();
			responseHeaders.add("Content-Type", "application/json; charset=utf-8");
			String errorValue = "{" + "\"error\":true," + "\"description\":\"Select a correct type for export it.\""
					+ "}";
			return new ResponseEntity<String>(errorValue, responseHeaders, HttpStatus.BAD_REQUEST);
		}
	}

	@RequestMapping(value = "/{id}/remove/point", method = RequestMethod.PUT, produces = "application/json;")
	public @ResponseBody ResponseEntity<Object> removePoint(@PathVariable String id, @RequestParam String lat,
			@RequestParam String lng, @RequestParam String timeInMillis, @RequestParam String index) {
		ApplicationContext ctxt = (ApplicationContext) ApplicationContextProvider.getApplicationContext();
		MongoDBJDBC mongoDBJDBC = (MongoDBJDBC) ctxt.getBean("mongoDBJDBC");
		ActivityDAO activityDAO = mongoDBJDBC.getActivityDAOImpl();
		Activity act = activityDAO.readById(id);

		// Remove element from the laps stored in state
		// If it is not at the begining or in the end of a lap
		// the lap splits into two new ones.
		Integer indexLap = getIndexOfALap(act, lat, lng, timeInMillis, index);
		if (indexLap > -1) {
			Integer indexPosition = getIndexOfAPosition(act, indexLap, lat, lng, timeInMillis, index);
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

				// lap right: Remove elements of the left of the track point to
				// delete, include index of this
				List<TrackPoint> rightTrackPoints = new ArrayList<TrackPoint>();
				IntStream.range(indexPosition + 1, sizeOfTrackPoints).forEach(indexEachTrackPoint -> {
					rightTrackPoints.add(act.getLaps().get(indexLap).getTracks().get(indexEachTrackPoint));
				});
				lapSplitRight.setTracks(rightTrackPoints);

				recalculateLapValues(lapSplitLeft);
				recalculateLapValues(lapSplitRight);

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
					return new ResponseEntity<Object>("{" + "\"error\":true,"
							+ "\"description\":\"It could not possible to delete the la.\"" + "}",
							HttpStatus.INTERNAL_SERVER_ERROR);

			} 
			// The lap is only one track point.
			// Delete and delete the lap
			else if(indexPosition==0 && sizeOfTrackPoints == 1){
				act.getLaps().get(indexLap.intValue()).getTracks().remove(indexPosition.intValue());
				act.getLaps().remove(indexLap.intValue());
			}
			// Start or end, it doesnt split into two laps, just delete the
			// track
			// point.
			else {
				Lap newLap = SerializationUtils.clone(act.getLaps().get(indexLap));
				newLap.getTracks().remove(indexPosition.intValue());
				act.getLaps().remove(indexLap.intValue());
				act.getLaps().add(indexLap.intValue(), newLap);
			}

			activityDAO.update(act);

			return act != null ? new ResponseEntity<Object>(act, HttpStatus.ACCEPTED)
					: new ResponseEntity<Object>(
							"{" + "\"error\":true," + "\"description\":\"Error trying to upload activity.\"" + "}",
							HttpStatus.INTERNAL_SERVER_ERROR);
		} else {
			String errorValue = "{" + "\"error\":true,"
					+ "\"description\":\"Not found lap which contains track point.\"" + "}";
			return new ResponseEntity<Object>(errorValue, HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	private String exportAsTCX(String id) throws JAXBException {
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
			if(avgBpm.getValue() > 0)
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
			if(maxBpm.getValue() > 0)
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
			if(lap.getTotalTimeSeconds()!=null && lap.getTotalTimeSeconds() > 0)
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

	private String exportAsGPX(String id) throws JAXBException {
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
	 * Calculate new values of the lap such as: total distance, total time,
	 * average heart rate,...
	 * 
	 * @param lap
	 */
	private void recalculateLapValues(Lap lap) {
		List<TrackPoint> tracks = lap.getTracks();
		// Lap start time corresponds with the date of the first point
		lap.setStartTime(tracks.get(0).getDate());
		AtomicInteger maxBpm = new AtomicInteger(), totalBpm = new AtomicInteger();
		DoubleAdder totalTime = new DoubleAdder(), totalDistance = new DoubleAdder(), totalSpeed = new DoubleAdder(),
				maxSpeed = new DoubleAdder();

		tracks.forEach(track -> {
			int index = tracks.indexOf(track);
			if (index > 0) {
				TrackPoint previousTrack = tracks.get(index - 1);
				double currentDistance = getDistanceBetweenPoints(previousTrack.getPosition(), track.getPosition());
				long timeBetween = 0;
				double currentSpeed = 0.0;
				if (track.getDate() != null) {
					timeBetween = (track.getDate().getTime() - previousTrack.getDate().getTime()) / 1000;
					if (timeBetween != 0)
						currentSpeed = currentDistance / timeBetween;
				}

				totalDistance.add(currentDistance);
				totalTime.add(timeBetween);
				totalSpeed.add(currentSpeed);
				maxSpeed.add((maxSpeed.doubleValue() < currentSpeed) ? currentSpeed - maxSpeed.doubleValue()
						: maxSpeed.doubleValue());
				maxBpm.set(track.getHeartRateBpm() != null
						? (maxBpm.get() < track.getHeartRateBpm()) ? track.getHeartRateBpm() : maxBpm.get()
						: maxBpm.get());
				totalBpm.addAndGet(track.getHeartRateBpm() != null ? track.getHeartRateBpm() : 0);
				// Distance in meters
				if (track.getDistanceMeters() == null && currentDistance != 0.0)
					track.setDistanceMeters(new BigDecimal(currentDistance));
				// Speed in meters per second
				if (track.getSpeed() == null && currentSpeed != 0.0)
					track.setSpeed(new BigDecimal(currentSpeed));
			}
		});
		if (totalDistance != null && totalDistance.doubleValue() > 0.0)
			lap.setDistanceMeters(totalDistance.doubleValue());
		if (totalTime != null && totalTime.doubleValue() > 0.0)
			lap.setTotalTimeSeconds(totalTime.doubleValue());
		if (totalBpm.get() > 0 && lap.getTracks().size() > 0)
			lap.setAverageHearRate(Double.valueOf(totalBpm.get()) / lap.getTracks().size());
		if (totalSpeed.doubleValue() > 0.0 && lap.getTracks().size() > 0)
			lap.setAverageSpeed(lap.getTracks().size() > 0 ? totalSpeed.doubleValue() / lap.getTracks().size() : 0.0);
		if (maxSpeed.doubleValue() > 0)
			lap.setMaximunSpeed(maxSpeed.doubleValue());
		if (maxBpm.doubleValue() > 0)
			lap.setMaximunHeartRate(maxBpm.get());
	}

	/**
	 * Distance between two positions. Degrees to radians and then, radians to
	 * meters.
	 * 
	 * @param origin
	 *            position
	 * @param end
	 *            position
	 * @return distance in meters
	 */
	private double getDistanceBetweenPoints(Position origin, Position end) {
		// Convert degrees to radians
		double latP1 = degrees2Radians(origin.getLatitudeDegrees()),
				lngP1 = degrees2Radians(origin.getLongitudeDegrees());
		double latP2 = degrees2Radians(end.getLatitudeDegrees()), lngP2 = degrees2Radians(end.getLongitudeDegrees());

		// Radius of earth in meters
		double earthRadiusMeters = 6378100.0;

		// Point P
		double rho1 = earthRadiusMeters * Math.cos(latP1);
		double z1 = earthRadiusMeters * Math.sin(latP1);
		double x1 = rho1 * Math.cos(lngP1);
		double y1 = rho1 * Math.sin(lngP1);

		// Point Q
		double rho2 = earthRadiusMeters * Math.cos(latP2);
		double z2 = earthRadiusMeters * Math.sin(latP2);
		double x2 = rho2 * Math.cos(lngP2);
		double y2 = rho2 * Math.sin(lngP2);

		// Dot product
		double dot = (x1 * x2 + y1 * y2 + z1 * z2);
		double cosTheta = dot / (Math.pow(earthRadiusMeters, 2));

		double theta = Math.acos(cosTheta);

		return earthRadiusMeters * theta;

	}

	private double degrees2Radians(String degrees) {
		return Double.parseDouble(degrees) * Math.PI / 180.0;
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
	private int getIndexOfALap(Activity act, String lat, String lng, String timeInMillis, String index) {
		Lap lap = act.getLaps().stream().filter(eachLap -> {
			return eachLap.getTracks().stream().anyMatch(track -> {
				return isThisTrack(track, lat, lng,
						timeInMillis != null && !timeInMillis.isEmpty() ? Long.parseLong(timeInMillis) : null,
						index != null && !index.isEmpty() ? Integer.parseInt(index) : null);
			});
		}).findFirst().orElse(null);

		return lap != null ? act.getLaps().indexOf(lap) : -1;
	}

	/**
	 * Method which returns an index corresponding to the track point with the
	 * latitude, longitude and ( time or index) contained in the parameters.
	 * 
	 * @param act:
	 *            activity
	 * @param indexLap:
	 *            index of the lap which contains track points
	 * @param lat:
	 *            latitude position
	 * @param lng:
	 *            longitude position
	 * @param timeInMillis:
	 *            time in milliseconds
	 * @param index:
	 *            index of the position in the array
	 * @return index of a track point
	 */
	private int getIndexOfAPosition(Activity act, int indexLap, String lat, String lng, String timeInMillis,
			String index) {
		TrackPoint trackPoint = act.getLaps().get(indexLap).getTracks().stream().filter(track -> {
			return isThisTrack(track, lat, lng,
					timeInMillis != null && !timeInMillis.isEmpty() ? Long.parseLong(timeInMillis) : null,
					index != null && !index.isEmpty() ? Integer.parseInt(index) : null);
		}).findFirst().orElse(null);
		return trackPoint != null ? act.getLaps().get(indexLap).getTracks().indexOf(trackPoint) : -1;
	}

	/**
	 * Check if the track point corresponds with the values of the params
	 * 
	 * @param track
	 *            point
	 * @param lat:
	 *            latitude degrees
	 * @param lng:
	 *            longitude degrees
	 * @param timeInMillis:
	 *            time in milliseconds
	 * @param index
	 * @return true or false
	 */
	private boolean isThisTrack(TrackPoint track, String lat, String lng, Long timeInMillis, Integer index) {
		boolean isTrack = track.getPosition() != null && track.getPosition().getLatitudeDegrees().equals(lat)
				&& track.getPosition().getLongitudeDegrees().equals(lng)
				&& ((timeInMillis != null && track.getDate().getTime() == timeInMillis)
						|| (index != null && track.getIndex() == index));
		return isTrack;
	}

}
