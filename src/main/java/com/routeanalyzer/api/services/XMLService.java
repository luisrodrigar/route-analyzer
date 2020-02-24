package com.routeanalyzer.api.services;

import io.vavr.control.Try;
import org.xml.sax.SAXParseException;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import java.io.InputStream;

public interface XMLService<T> {
	
	Try<T> readXML(InputStream inputFileXML) throws JAXBException, SAXParseException;
	Try<String> createXML(JAXBElement<T> object) throws JAXBException;
	
}
