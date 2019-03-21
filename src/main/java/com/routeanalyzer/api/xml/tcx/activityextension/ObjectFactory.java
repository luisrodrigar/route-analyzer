//
// Este archivo ha sido generado por la arquitectura JavaTM para la implantación de la referencia de enlace (JAXB) XML v2.2.8-b130911.1802 
// Visite <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Todas las modificaciones realizadas en este archivo se perderán si se vuelve a compilar el esquema de origen. 
// Generado el: 2018.03.31 a las 12:10:25 AM CEST 
//


package com.routeanalyzer.xml.tcx.activityextension;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlElementDecl;
import javax.xml.bind.annotation.XmlRegistry;
import javax.xml.namespace.QName;


/**
 * This object contains factory methods for each 
 * Java content interface and Java element interface 
 * generated in the com.garmin.xmlschemas.activityextension.v2 package. 
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

    private final static QName _TPX_QNAME = new QName("http://www.garmin.com/xmlschemas/ActivityExtension/v2", "TPX");
    private final static QName _LX_QNAME = new QName("http://www.garmin.com/xmlschemas/ActivityExtension/v2", "LX");

    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: com.garmin.xmlschemas.activityextension.v2
     * 
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link ActivityLapExtensionT }
     * 
     */
    public ActivityLapExtensionT createActivityLapExtensionT() {
        return new ActivityLapExtensionT();
    }

    /**
     * Create an instance of {@link ActivityTrackpointExtensionT }
     * 
     */
    public ActivityTrackpointExtensionT createActivityTrackpointExtensionT() {
        return new ActivityTrackpointExtensionT();
    }

    /**
     * Create an instance of {@link ExtensionsT }
     * 
     */
    public ExtensionsT createExtensionsT() {
        return new ExtensionsT();
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link ActivityTrackpointExtensionT }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.garmin.com/xmlschemas/ActivityExtension/v2", name = "TPX")
    public JAXBElement<ActivityTrackpointExtensionT> createTPX(ActivityTrackpointExtensionT value) {
        return new JAXBElement<ActivityTrackpointExtensionT>(_TPX_QNAME, ActivityTrackpointExtensionT.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link ActivityLapExtensionT }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.garmin.com/xmlschemas/ActivityExtension/v2", name = "LX")
    public JAXBElement<ActivityLapExtensionT> createLX(ActivityLapExtensionT value) {
        return new JAXBElement<ActivityLapExtensionT>(_LX_QNAME, ActivityLapExtensionT.class, null, value);
    }

}
