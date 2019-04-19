package com.routeanalyzer.api.services;

import org.xml.sax.SAXParseException;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import java.io.InputStream;

public interface XMLService<T> {
	
	T readXML(InputStream inputFileXML) throws JAXBException, SAXParseException;
	String createXML(JAXBElement<T> object) throws JAXBException;
	
}
