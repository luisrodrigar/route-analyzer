package com.routeanalyzer.services;

import java.io.InputStream;
import java.io.StringWriter;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.stream.StreamSource;

import org.xml.sax.SAXParseException;

public class XMLService {

	public <T> T readXML(Class<T> type, InputStream inputFileXML) throws JAXBException, SAXParseException {
		JAXBContext ctx;
		ctx = JAXBContext.newInstance(type);
		Unmarshaller u = ctx.createUnmarshaller();

		return type.cast(u.unmarshal(new StreamSource(inputFileXML), type).getValue());

	}

	public <T> String createXML(Class<T> type, JAXBElement<T> object) throws JAXBException {
		JAXBContext jaxbContext = JAXBContext.newInstance(type);
		Marshaller marshaller = jaxbContext.createMarshaller();
		marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
		StringWriter builderXml = new StringWriter();
		marshaller.marshal(object, builderXml);

		return builderXml.toString();
	}

}
