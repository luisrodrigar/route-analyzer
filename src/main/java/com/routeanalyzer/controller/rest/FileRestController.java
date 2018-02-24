package com.routeanalyzer.controller.rest;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import org.springframework.context.ApplicationContext;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.amazonaws.AmazonClientException;
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
import com.routeanalyzer.services.XMLReader;
import com.routeanalyzer.xml.gpx11.GpxType;
import com.routeanalyzer.xml.tcx.TrainingCenterDatabaseT;
import com.sun.org.apache.xerces.internal.dom.ElementNSImpl;

@CrossOrigin(origins = "http://localhost:3000", maxAge = 3600)
@RestController()
@RequestMapping("/file")
public class FileRestController {
	
	private static final String BUCKET_NAME = "xml-files-storage";
	AS3Service aS3Service = new AS3Service(BUCKET_NAME);
	
	private XMLReader reader = new XMLReader();
	
	private GsonBuilder gsonBuilder = new GsonBuilder();
    private Gson gson = gsonBuilder.create();
    
    @RequestMapping(value="/upload", method=RequestMethod.POST,
			headers = "content-type=multipart/form-data", produces = "application/json; charset=UTF-8")
    public ResponseEntity<String> uploadFile(@RequestParam("file") final MultipartFile multiPart, @RequestParam("type") final String type){
    	try {
	    	switch(type){
	    	case "tcx":
					return new ResponseEntity<String>(uploadTCXFile(multiPart), HttpStatus.ACCEPTED);
	    	case "gpx":
	    		return new ResponseEntity<String>(uploadGPXFile(multiPart), HttpStatus.ACCEPTED);
	    	}
    	} catch (IOException | AmazonClientException e) {
    		String errorValue = "{"
        			+ "\"error\":true,"
        			+ "\"description\":\"Problem with the type of the file which you want to upload\","
        			+ "\"exception\":\"" + e.getMessage()+"\""
        			+ "}";
        	return new ResponseEntity<String>(errorValue, HttpStatus.INTERNAL_SERVER_ERROR);
		}
    	String errorValue = "{"
    			+ "\"error\":true,"
    			+ "\"description\":\"Problem with the type of the file which you want to upload\""
    			+ "}";
    	return new ResponseEntity<String>(errorValue, HttpStatus.BAD_REQUEST);
    }
    
    @RequestMapping(value="/get", method=RequestMethod.GET)
    public ResponseEntity<String> getFile(@RequestParam("id-file") final String id, @RequestParam("type") final String type){
    	HttpHeaders responseHeaders = new HttpHeaders();
    	if(type!=null && !type.isEmpty()){
    		try {
    			
				BufferedReader bufReader = aS3Service.getFile(id+"."+type);
				responseHeaders.add("Content-Type", "application/xml; charset=utf-8");
				// Se utiliza 
				String xml = bufReader.lines().collect(Collectors.joining("\n"));
				return new ResponseEntity<String>(xml, responseHeaders, HttpStatus.INTERNAL_SERVER_ERROR);
				
			} catch (AmazonClientException amazonException ) {
				responseHeaders.add("Content-Type", "application/json; charset=utf-8");
				String json = "{"
						+ "error:true,"
						+ "description: 'Problem trying to get the file :: Amazon S3 Problem'"
						+ "exception: " + amazonException.getMessage()
						+ " }";
				return new ResponseEntity<String>(json, responseHeaders, HttpStatus.INTERNAL_SERVER_ERROR);
			} catch (IOException iOException){
				responseHeaders.add("Content-Type", "application/json; charset=utf-8");
				String json =  "{"
						+ "error:true,"
						+ "description: 'Problem trying to get the file :: Input/Output Problem'"
						+ "exception: " + iOException.getMessage()
						+ " }";
				return new ResponseEntity<String>(json, responseHeaders, HttpStatus.INTERNAL_SERVER_ERROR);
			}
    	} else{
    		responseHeaders.add("Content-Type", "application/json; charset=utf-8");
    		String json = "{"
    				+ "error:true,"
    				+ "description:'Problem with the type of the file which you want to get'"
    				+ "}";
    		return new ResponseEntity<String>(json, responseHeaders, HttpStatus.BAD_REQUEST);
    	}
    }
  
	private String uploadTCXFile(final MultipartFile multiPart) throws IOException{
		byte[] arrayBytes = multiPart.getBytes();
		List<String> ids = new ArrayList<String>();
		AtomicInteger indexLap = new AtomicInteger(), indexTrackPoint = new AtomicInteger();
		
			InputStream inputFileTCX = multiPart.getInputStream();
			List<Activity> activities = new ArrayList<Activity>();
			TrainingCenterDatabaseT tcx = reader.readXML(TrainingCenterDatabaseT.class, inputFileTCX);
			tcx.getActivities().getActivity().forEach(eachActivity->{
				Activity activity = new Activity();
				activity.setSourceXmlType("tcx");
				activity.setDevice(eachActivity.getCreator().getName());
				activity.setDate(eachActivity.getId().toGregorianCalendar().getTime());
				activity.setSport(eachActivity.getSport().toString());
				eachActivity.getLap().forEach(eachLap->{
					Lap lap = new Lap();
					lap.setAverageHearRate(new Integer(eachLap.getAverageHeartRateBpm().getValue()));
					lap.setCalories(eachLap.getCalories());
					lap.setDistanceMeters(eachLap.getDistanceMeters());
					lap.setMaximunSpeed(eachLap.getMaximumSpeed());
					lap.setMaximunHeartRate(new Integer(eachLap.getMaximumHeartRateBpm().getValue()));
					lap.setStartTime(eachLap.getStartTime()!=null ? eachLap.getStartTime().toGregorianCalendar().getTime() : null);
					lap.setIndex(indexLap.incrementAndGet());
					lap.setTotalTimeSeconds(eachLap.getTotalTimeSeconds());
					lap.setIntensity(eachLap.getIntensity().toString());
					
					eachLap.getExtensions().getAny().stream()
						.filter(extension->(
							extension instanceof ElementNSImpl
							&& ((ElementNSImpl)extension).getFirstChild() instanceof ElementNSImpl
							&& ((ElementNSImpl)extension).getFirstChild().getLocalName().equals("AvgSpeed")
						)).forEach(extension->{
							lap.setAverageSpeed(Double.parseDouble(((ElementNSImpl)extension).getFirstChild().getFirstChild().getNodeValue()));
						});
					
					eachLap.getTrack()
					.forEach(track->{
						track.getTrackpoint().forEach(trackPoint->{
							TrackPoint trp = new TrackPoint(
									(trackPoint.getTime()!=null ?
											trackPoint.getTime().toGregorianCalendar().getTime() : null), 
									(trackPoint==null || indexTrackPoint.get() >= 0 ?
											indexTrackPoint.incrementAndGet() : null),
									(trackPoint!=null && trackPoint.getPosition()!=null ?
									new Position(
											String.valueOf(trackPoint.getPosition().getLatitudeDegrees()), 
											String.valueOf(trackPoint.getPosition().getLongitudeDegrees())) 
									: null
									),
									trackPoint!=null && trackPoint.getAltitudeMeters()!=null?
											new BigDecimal(trackPoint.getAltitudeMeters()):null, 
									trackPoint!=null && trackPoint.getDistanceMeters()!=null?
											new BigDecimal(trackPoint.getDistanceMeters()):null, 
									new BigDecimal(0), 
									trackPoint!=null && trackPoint.getHeartRateBpm()!=null?
											new Integer(trackPoint.getHeartRateBpm().getValue()):null);
							if(trackPoint.getExtensions()!=null){
								trackPoint.getExtensions().getAny().stream()
									.filter(extension -> ( 
											extension instanceof ElementNSImpl
											&& ((ElementNSImpl)extension).getFirstChild() instanceof ElementNSImpl
											&& ((ElementNSImpl)extension).getFirstChild().getLocalName().equals("Speed")
											))
									.forEach(extension -> {
										trp.setSpeed(new BigDecimal(((ElementNSImpl)extension).getFirstChild().getFirstChild().getNodeValue()));
									});
							}
							lap.addTrack(trp);		
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
			activities.forEach(activity->{
				activityDAO.create(activity);
				ids.add(activity.getId());
				try{
					aS3Service.uploadFile(arrayBytes, activity.getId() + "." + activity.getSourceXmlType());
				} catch(AmazonClientException aS3Exception){
					System.err.println("Delete activity with id: " + activity.getId() + " due to problems trying to upload file to AS3.");
					activityDAO.deleteById(activity.getId());
					throw aS3Exception;
				} 
			});
			
		
		return gson.toJson(ids);	
	}
	
	private String uploadGPXFile(final MultipartFile multiPart) throws IOException, AmazonClientException{
		List<String> ids = new ArrayList<String>();
		
			byte[] arrayBytes = multiPart.getBytes();
			InputStream inputFileGPX = multiPart.getInputStream();
			
			GpxType gpx = reader.readXML(GpxType.class, inputFileGPX);
			
			List<Activity> activities = new ArrayList<Activity>();
			AtomicInteger indexLap = new AtomicInteger(), indexTrackPoint = new AtomicInteger();
			gpx.getTrk().forEach(track->{
				Activity activity = new Activity();
				activity.setSourceXmlType("gpx");
				activity.setDate(gpx.getMetadata()!=null && gpx.getMetadata().getTime()!=null && indexLap.get()==0
						? gpx.getMetadata().getTime().toGregorianCalendar().getTime():
						(track.getTrkseg()!=null && track.getTrkseg().get(0)!=null
							&&track.getTrkseg().get(0).getTrkpt()!=null
							&& track.getTrkseg().get(0).getTrkpt().get(0)!=null 
							&& track.getTrkseg().get(0).getTrkpt().get(0).getTime()!=null?
								track.getTrkseg().get(0).getTrkpt().get(0).getTime().toGregorianCalendar().getTime():null));
				activity.setDevice(gpx.getCreator());
				activity.setName(track.getName()!=null? track.getName().trim():null);
				track.getTrkseg().forEach(eachLap->{
					Lap lap = new Lap();
					if(eachLap.getTrkpt()!=null && !eachLap.getTrkpt().isEmpty() 
							&& eachLap.getTrkpt().get(0).getTime()!=null)
						lap.setStartTime(eachLap.getTrkpt().get(0).getTime().toGregorianCalendar().getTime());
					lap.setIndex(indexLap.incrementAndGet());
					eachLap.getTrkpt().forEach(eachTrackPoint->{
						// Si no viene informada la fecha del track point, se define como fecha la actual
						// Se hace esto para que despues puedan ser comparados por fecha (metodo compareTo)
						TrackPoint tkp = new TrackPoint((eachTrackPoint.getTime()!=null && indexTrackPoint.get()==0 ?
								eachTrackPoint.getTime().toGregorianCalendar().getTime():null),
								eachTrackPoint.getTime()==null || indexTrackPoint.get()>0 ? indexTrackPoint.incrementAndGet():null,
								new Position(String.valueOf(eachTrackPoint.getLat()), String.valueOf(eachTrackPoint.getLon())), 
								eachTrackPoint.getEle(), null, null, null);
						if(eachTrackPoint.getExtensions()!=null){
							eachTrackPoint.getExtensions().getAny().stream()
								.filter(item->(item instanceof ElementNSImpl 
									&& ((ElementNSImpl)item).getFirstChild() instanceof ElementNSImpl 
									&& ((ElementNSImpl)item).getFirstChild().getLocalName().equals("hr")))
								.forEach(item->{
									tkp.setHeartRateBpm(Integer.parseInt(((ElementNSImpl)item).getFirstChild().getFirstChild().getNodeValue()));
								});
						}
						lap.addTrack(tkp);
					});
					activity.addLap(lap);
				});
				activities.add(activity);
			});
			
			// Se guarda en la base de datos
			ApplicationContext ctxt = (ApplicationContext) ApplicationContextProvider.getApplicationContext();
			MongoDBJDBC mongoDBJDBC = (MongoDBJDBC) ctxt.getBean("mongoDBJDBC");
			ActivityDAO activityDAO = mongoDBJDBC.getActivityDAOImpl();
			activities.forEach(activity->{
				activityDAO.create(activity);
				ids.add(activity.getId());
				try{
					aS3Service.uploadFile(arrayBytes, activity.getId() + ".gpx");
				} catch(AmazonClientException aS3Exception){
					System.err.println("Delete activity with id: " + activity.getId() + " due to problems trying to upload file to AS3.");
					int result = activityDAO.deleteById(activity.getId());
					assert result == 1 : String.format("Result value of delete must be (%d)", 1);
					throw aS3Exception;
				} 
				
			});
			
		
		return gson.toJson(ids);
	}
	
	

}
