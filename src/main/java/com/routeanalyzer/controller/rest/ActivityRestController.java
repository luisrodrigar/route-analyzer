package com.routeanalyzer.controller.rest;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.DoubleAdder;
import java.util.stream.IntStream;

import org.apache.commons.lang3.SerializationUtils;
import org.springframework.context.ApplicationContext;
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
		final Integer indexLap = getIndexOfALap(act, lat, lng, timeInMillis, index);
		if (indexLap > -1) {
			final Integer indexPosition = getIndexOfAPosition(act, indexLap, lat, lng, timeInMillis, index);
			int sizeOfTrackPoints = act.getLaps().get(indexLap).getTracks().size();

			// Track point not start nor end. Thus, it slipt into two laps
			if (indexPosition > 0 && indexPosition < sizeOfTrackPoints) {
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

				lapSplitLeft.setCalories(act.getLaps().get(indexLap).getCalories() != null
						? new Long(Math.round(Double.valueOf(lapSplitLeft.getTracks().size()) / sizeOfTrackPoints
								* act.getLaps().get(indexLap).getCalories())).intValue()
						: null);
				lapSplitRight.setCalories(act.getLaps().get(indexLap).getCalories() != null
						? new Long(Math.round((Double.valueOf(lapSplitRight.getTracks().size()) / sizeOfTrackPoints)
								* act.getLaps().get(indexLap).getCalories())).intValue()
						: null);
				lapSplitRight.setIndex(act.getLaps().get(indexLap).getIndex() + 1);
				// Increment a point the index field of the next elements of the
				// element to delete
				IntStream.range(indexLap + 1, act.getLaps().size()).forEach(indexEachLap -> {
					act.getLaps().get(indexEachLap).setIndex(act.getLaps().get(indexEachLap).getIndex() + 1);
				});
				
				Lap lapRemoved = act.getLaps().remove(indexLap.intValue());
				act.getLaps().add(indexLap.intValue(), lapSplitLeft);
				act.getLaps().add((indexLap.intValue() + 1), lapSplitRight);

			}
			// Start or end, it doesnt split into two laps, just delete the
			// track
			// point.
			else {
				Lap newLap = SerializationUtils.clone(act.getLaps().get(indexLap));
				newLap.getTracks().remove(indexPosition);
				act.getLaps().remove(indexLap);
				act.getLaps().add(indexLap, newLap);

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
				long timeBetween = (track.getDate().getTime() - previousTrack.getDate().getTime()) / 1000;
				double currentSpeed = currentDistance / timeBetween;

				totalDistance.add(currentDistance);
				totalTime.add(timeBetween);
				totalSpeed.add(currentSpeed);
				maxSpeed.add((maxSpeed.doubleValue() < currentSpeed) ? currentSpeed - maxSpeed.doubleValue() : maxSpeed.doubleValue());
				maxBpm.set((maxBpm.get() < track.getHeartRateBpm()) ? track.getHeartRateBpm() : maxBpm.get());
				totalBpm.addAndGet(track.getHeartRateBpm());
				// Distance in meters
				if (track.getDistanceMeters() == null)
					track.setDistanceMeters(new BigDecimal(currentDistance));
				// Speed in meters per second
				if (track.getSpeed() == null)
					track.setSpeed(new BigDecimal(currentSpeed));
			}
		});

		lap.setDistanceMeters(totalDistance.doubleValue());
		lap.setTotalTimeSeconds(totalTime.doubleValue());
		lap.setAverageHearRate(lap.getTracks().size() > 0 ? totalBpm.get() / lap.getTracks().size() : 0.0);
		lap.setAverageSpeed(lap.getTracks().size() > 0 ? totalSpeed.doubleValue() / lap.getTracks().size() : 0.0);
		lap.setMaximunSpeed(maxSpeed.doubleValue());
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
				return isThisTrack(track, lat, lng, timeInMillis != null ? Long.parseLong(timeInMillis) : null,
						index != null ? Integer.parseInt(index) : null);
			});
		}).findFirst().orElse(null);

		return lap!=null ? act.getLaps().indexOf(lap) : -1;
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
			return isThisTrack(track, lat, lng, timeInMillis != null ? Long.parseLong(timeInMillis) : null,
					index != null ? Integer.parseInt(index) : null);
		}).findFirst().orElse(null);
		return trackPoint!=null ? act.getLaps().get(indexLap).getTracks().indexOf(trackPoint) : -1;
	}

	/**
	 * Check if the track point corresponds with the values of the params
	 * @param track point
	 * @param lat: latitude degrees
	 * @param lng: longitude degrees
	 * @param timeInMillis: time in milliseconds
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
