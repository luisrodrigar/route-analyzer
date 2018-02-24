package com.routeanalyzer.services;

import java.io.InputStream;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.stream.StreamSource;

public class XMLReader {
	
	public <T> T readXML(Class<T> type, InputStream inputFileXML){
		JAXBContext ctx;
		  try {
		   ctx = JAXBContext.newInstance(type);
		   Unmarshaller u = ctx.createUnmarshaller();
		    
		   return type.cast(u.unmarshal(new StreamSource(inputFileXML), type).getValue());
		   
		  } catch (Exception e) {
		   e.printStackTrace();
		  }
		return null;
	}
	
}
