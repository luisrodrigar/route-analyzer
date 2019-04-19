package com.routeanalyzer.api.services.reader;

import com.routeanalyzer.api.common.ThrowingFunction;
import com.routeanalyzer.api.common.ThrowingSupplier;
import com.routeanalyzer.api.services.XMLService;
import org.apache.commons.lang3.StringUtils;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.transform.stream.StreamSource;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.function.Function;
import java.util.function.Supplier;

import static java.util.Optional.of;
import static java.util.Optional.ofNullable;

public abstract class AbstractXMLService<T> implements XMLService<T> {
	
	protected Class<T> type;
	
	public AbstractXMLService(Class<T> type){
		this.type = type;
	}

	private Function<Marshaller, Marshaller> setPropertyJAXBFormatted =
			ThrowingFunction.unchecked(marshaller -> {
				marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
				return marshaller;
			});

	@Override
	public T readXML(InputStream inputFileXML) {
		return ofNullable(ThrowingSupplier.unchecked(() -> getJAXBContext()))
				.map(Supplier::get)
				.map(ThrowingFunction.unchecked(JAXBContext::createUnmarshaller))
				.flatMap(unmarshaller -> ofNullable(inputFileXML)
					.map(StreamSource::new)
					.map(ThrowingFunction.unchecked(streamSource -> unmarshaller.unmarshal(streamSource, type)))
					.map(JAXBElement::getValue)
					.map(type::cast))
				.orElse(null);

	}

	@Override
	public String createXML(JAXBElement<T> object) {
		Function<Marshaller, StringWriter> getMarshalStringWriter=
				ThrowingFunction.unchecked(marshaller -> {
					StringWriter builderXml = new StringWriter();
					marshaller.marshal(object, builderXml);
					return builderXml;
				});
		return of(ThrowingSupplier.unchecked(() -> getJAXBContext()))
				.map(Supplier::get)
				.map(ThrowingFunction.unchecked(JAXBContext::createMarshaller))
				.map(setPropertyJAXBFormatted)
				.map(getMarshalStringWriter)
				.map(StringWriter::toString)
				.orElse(StringUtils.EMPTY);
	}
	
	protected abstract JAXBContext getJAXBContext() throws JAXBException;

}
