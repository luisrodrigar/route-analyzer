package com.routeanalyzer.api.services;

import java.io.InputStream;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;

import org.xml.sax.SAXParseException;

public interface XMLService<T> {
	
	public T readXML(InputStream inputFileXML) throws JAXBException, SAXParseException;
	public String createXML(JAXBElement<T> object) throws JAXBException;
	
}
