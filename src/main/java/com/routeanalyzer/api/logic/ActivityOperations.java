package com.routeanalyzer.api.logic;

import com.routeanalyzer.api.logic.file.upload.UploadFileService;
import com.routeanalyzer.api.model.Activity;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;

public interface ActivityOperations {

	/**
	 * Remove point: - Remove lap if it is the last point of the lap - Split lap
	 * into two ones if the point is between start point and end point (not
	 * included). - Remove point if point is start or end and modify gloval
	 * values of the lap
	 * 
	 * @param act
	 *            of the activity
	 * @param lat
	 *            of the position
	 * @param lng
	 *            of the position
	 * @param timeInMillis:
	 *            time in milliseconds
	 * @param indexTrackPoint:
	 *            order of creation
	 * @return activity or null if there was any error.
	 */
	Activity removePoint(final Activity act, final String lat, final String lng, final Long timeInMillis,
						 final Integer indexTrackPoint);
	
	/**
	 * Split a lap into two laps with one track point as the divider.
	 * 
	 * @param activity
	 *            of the activity
	 * @param lat
	 *            of the position
	 * @param lng
	 *            of the position
	 * @param timeInMillis
	 *            timi millis
	 * @param indexTrackPoint
	 * 			  index lap to split up
	 *            of the track point which will be the divider
	 * @return activity with the new laps.
	 */
	Activity splitLap(final Activity activity, final String lat, final String lng, final Long timeInMillis,
					  final Integer indexTrackPoint);

	/**
	 * Join two laps, the result is one lap with the mixed values
	 * @param activity
	 * @param index1
	 * @param index2
	 * @return
	 */
	Activity joinLaps(final Activity activity, final Integer index1, final Integer index2);

	/**
	 * Delete a lap from an activity
	 * @param act
	 * @param startTime optional (if it is not informed, just pay attention to the index lap)
	 * @param indexLap
	 * @return
	 */
	Activity removeLaps(final Activity act, final List<Long> startTime, final List<Integer> indexLap);

	/**
	 * Calculate total distance and speed of an activity.
	 * @param activity
	 */
	void calculateDistanceSpeedValues(final Activity activity);

	/**
	 * Method which returns an index corresponding to the track point with the
	 * latitude, longitude and ( time or index) contained in the parameters.
	 *
	 * @param activity:
	 *            activity
	 * @param indexLap:
	 *            index of the lap
	 * @param latitude:
	 *            latitude position
	 * @param longitude:
	 *            longitude position
	 * @param time:
	 *            time in milliseconds
	 * @param index:
	 *            index of the position in the array
	 * @return index of a track point
	 */
	int indexOfTrackPoint(final Activity activity, final Integer indexLap, final String latitude,
						  final String longitude, final Long time, final Integer index);

	/**
	 * Set regular and light color to each activity's lap
	 * @param activity activity where the colors want to apply
	 * @param dataColors separated each color lap by ',' regular and light color lap separated by '#'
	 * @return activity with the colors applied
	 */
	Activity setColorsGetActivity(final Activity activity, final String dataColors);

	/**
	 * Parser and transform the xml file to data model List
	 * @param multiPartFile : xml file contains the activity data
	 * @param fileService : file service to use
	 * @return stored activity ids
	 */
	Optional<List<Activity>> upload(final MultipartFile multiPartFile, final UploadFileService fileService);

	/**
	 * Push the original file to S3 bucket.
	 * @param activities all the activities
	 * @param multiPart original file
	 * @return list of activities
	 */
	List<Activity> pushToS3(final List<Activity> activities, final MultipartFile multiPart);

	/**
	 * Export activity by type
	 * @param type of the file to export
	 * @param activity to export
	 * @return
	 */
	String exportByType(final String type, final Activity activity);

	/**
	 * Getting original input xml file
	 * @param id activity identification
	 * @param type of the xml file
	 * @return optional string file value
	 */
	Optional<String> getOriginalFile(final String id, final String type);



}
