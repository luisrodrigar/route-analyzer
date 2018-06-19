package com.routeanalyzer.logic;

import java.io.IOException;
import java.util.List;
import javax.xml.bind.JAXBException;
import org.springframework.web.multipart.MultipartFile;
import org.xml.sax.SAXParseException;

import com.amazonaws.AmazonClientException;
import com.routeanalyzer.model.Activity;

public interface ActivityUtils {
	
	public List<Activity> uploadGPXFile(MultipartFile multiPart)
			throws IOException, AmazonClientException, JAXBException, SAXParseException;

	/**
	 * 
	 * @param multiPart
	 * @return
	 * @throws IOException
	 * @throws JAXBException
	 * @throws SAXParseException
	 */
	public List<Activity> uploadTCXFile(MultipartFile multiPart)
			throws IOException, JAXBException, SAXParseException;

	/**
	 * 
	 * @param id
	 * @return
	 * @throws JAXBException
	 */
	public String exportAsTCX(Activity act) throws JAXBException;

	/**
	 * 
	 * @param id
	 * @return
	 * @throws JAXBException
	 */
	public String exportAsGPX(Activity act) throws JAXBException;

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
	public Activity removePoint(Activity act, String lat, String lng, String timeInMillis, String indexTrackPoint);
	
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
	public Activity splitLap(Activity act, String lat, String lng, String timeInMillis, String indexTrackPoint);

	/**
	 * Join two laps, the result is one lap with the mixed values
	 * @param idActivity
	 * @param indexLap1
	 * @param indexLap2
	 * @return
	 */
	public Activity joinLap(Activity act, Integer indexLap1, Integer indexLap2);

	/**
	 * Delete a lap from an activity
	 * @param act
	 * @param startTime
	 * @param indexLap
	 * @return
	 */
	public Activity removeLap(Activity act, Long startTime, Integer indexLap);


}
