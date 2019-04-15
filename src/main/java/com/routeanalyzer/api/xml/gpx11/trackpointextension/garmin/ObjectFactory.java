//
// Este archivo ha sido generado por la arquitectura JavaTM para la implantación de la referencia de enlace (JAXB) XML v2.2.8-b130911.1802 
// Visite <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Todas las modificaciones realizadas en este archivo se perderán si se vuelve a compilar el esquema de origen. 
// Generado el: 2018.03.30 a las 11:23:58 PM CEST 
//


package com.routeanalyzer.api.xml.gpx11.trackpointextension.garmin;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlElementDecl;
import javax.xml.bind.annotation.XmlRegistry;
import javax.xml.namespace.QName;


/**
 * This object contains factory methods for each 
 * Java content interface and Java element interface 
 * generated in the com.garmin.xmlschemas.trackpointextension.v1 package. 
 * <p>An ObjectFactory allows you to programatically 
 * construct new instances of the Java representation 
 * for XML content. The Java representation of XML 
 * content can consist of schema derived interfaces 
 * and classes representing the binding of schema 
 * type definitions, element declarations and model 
 * groups.  Factory methods for each of these are 
 * provided in this class.
 * 
 */
@XmlRegistry
public class ObjectFactory {

    private final static QName _TrackPointExtension_QNAME = new QName("http://www.garmin.com/xmlschemas/TrackPointExtension/v1", "TrackPointExtension");

    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: com.garmin.xmlschemas.trackpointextension.v1
     * 
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link TrackPointExtensionT }
     * 
     */
    public TrackPointExtensionT createTrackPointExtensionT() {
        return new TrackPointExtensionT();
    }

    /**
     * Create an instance of {@link ExtensionsT }
     * 
     */
    public ExtensionsT createExtensionsT() {
        return new ExtensionsT();
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link TrackPointExtensionT }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.garmin.com/xmlschemas/TrackPointExtension/v1", name = "TrackPointExtension")
    public JAXBElement<TrackPointExtensionT> createTrackPointExtension(TrackPointExtensionT value) {
        return new JAXBElement<TrackPointExtensionT>(_TrackPointExtension_QNAME, TrackPointExtensionT.class, null, value);
    }

}
