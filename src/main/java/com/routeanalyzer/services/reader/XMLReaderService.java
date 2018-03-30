package com.routeanalyzer.services.reader;

import java.io.InputStream;
import java.io.StringWriter;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.stream.StreamSource;

import org.xml.sax.SAXParseException;

public abstract class XMLReaderService<T> {
	
	protected Class<T> type;
	
	public XMLReaderService(Class<T> type){
		this.type = type;
	}

	public T readXML(InputStream inputFileXML) throws JAXBException, SAXParseException {
		JAXBContext ctx = getJAXBContext();
		Unmarshaller u = ctx.createUnmarshaller();

		return type.cast(u.unmarshal(new StreamSource(inputFileXML), type).getValue());

	}

	public String createXML(JAXBElement<T> object) throws JAXBException {
		JAXBContext jaxbContext = getJAXBContext();
		Marshaller marshaller = jaxbContext.createMarshaller();
		marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
		StringWriter builderXml = new StringWriter();
		marshaller.marshal(object, builderXml);

		return builderXml.toString();
	}
	
	protected abstract JAXBContext getJAXBContext() throws JAXBException;

}
