package com.routeanalyzer.api.services.reader;

import com.routeanalyzer.api.services.XMLService;
import io.vavr.control.Try;
import lombok.extern.slf4j.Slf4j;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.transform.stream.StreamSource;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.function.Function;

@Slf4j
public abstract class AbstractXMLService<T> implements XMLService<T> {
	
	protected Class<T> type;
	
	public AbstractXMLService(Class<T> type){
		this.type = type;
	}

	@Override
	public Try<T> readXML(InputStream inputFileXML) {
		return Try.of(() -> getJAXBContext())
				.onFailure(err -> log.error("Error trying to get the jaxb context", err))
				.flatMap(jAXBContext -> Try.of( () -> jAXBContext.createUnmarshaller())
						.onFailure(err -> log.error("Error trying to create the un-marshaller", err)))
				.flatMap(unmarshaller -> Try.of(() -> unmarshaller.unmarshal(new StreamSource(inputFileXML), type))
						.onFailure(err -> log.error("Error trying to un-marshall the input xml file", err))
						.map(JAXBElement::getValue)
						.map(type::cast));
	}

	@Override
	public Try<String> createXML(JAXBElement<T> object) {
		Function<Marshaller, Try<Marshaller>> setPropertyJAXBFormatted = marshaller ->
				Try.of(() -> {
					marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
					return marshaller;
				}).onFailure(err -> log.error("Error trying to set the property JAXB_FORMATTED_OUTPUT to true", err));
		Function<Marshaller, Try<StringWriter>> getMarshalStringWriter = marshaller ->
				Try.of(() -> {
					StringWriter builderXml = new StringWriter();
					marshaller.marshal(object, builderXml);
					return builderXml;
				});
		return Try.of(() -> getJAXBContext())
				.onFailure(err -> log.error("Error trying to get the jaxb context", err))
				.flatMap(jAXBContext -> Try.of( () -> jAXBContext.createMarshaller())
						.onFailure(err -> log.error("Error trying to create the marshaller", err)))
				.flatMap(setPropertyJAXBFormatted)
				.flatMap(getMarshalStringWriter)
				.map(StringWriter::toString);
	}
	
	protected abstract JAXBContext getJAXBContext() throws JAXBException;

}
