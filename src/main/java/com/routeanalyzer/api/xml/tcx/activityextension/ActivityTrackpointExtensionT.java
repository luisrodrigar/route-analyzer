//
// Este archivo ha sido generado por la arquitectura JavaTM para la implantación de la referencia de enlace (JAXB) XML v2.2.8-b130911.1802 
// Visite <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Todas las modificaciones realizadas en este archivo se perderán si se vuelve a compilar el esquema de origen. 
// Generado el: 2018.03.31 a las 12:10:25 AM CEST 
//


package com.routeanalyzer.xml.tcx.activityextension;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Clase Java para ActivityTrackpointExtension_t complex type.
 * 
 * <p>El siguiente fragmento de esquema especifica el contenido que se espera que haya en esta clase.
 * 
 * <pre>
 * &lt;complexType name="ActivityTrackpointExtension_t">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="Speed" type="{http://www.w3.org/2001/XMLSchema}double" minOccurs="0"/>
 *         &lt;element name="RunCadence" type="{http://www.garmin.com/xmlschemas/ActivityExtension/v2}CadenceValue_t" minOccurs="0"/>
 *         &lt;element name="Watts" type="{http://www.w3.org/2001/XMLSchema}unsignedShort" minOccurs="0"/>
 *         &lt;element name="Extensions" type="{http://www.garmin.com/xmlschemas/ActivityExtension/v2}Extensions_t" minOccurs="0"/>
 *       &lt;/sequence>
 *       &lt;attribute name="CadenceSensor" type="{http://www.garmin.com/xmlschemas/ActivityExtension/v2}CadenceSensorType_t" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ActivityTrackpointExtension_t", propOrder = {
    "speed",
    "runCadence",
    "watts",
    "extensions"
})
public class ActivityTrackpointExtensionT {

    @XmlElement(name = "Speed")
    protected Double speed;
    @XmlElement(name = "RunCadence")
    @XmlSchemaType(name = "unsignedByte")
    protected Short runCadence;
    @XmlElement(name = "Watts")
    @XmlSchemaType(name = "unsignedShort")
    protected Integer watts;
    @XmlElement(name = "Extensions")
    protected ExtensionsT extensions;
    @XmlAttribute(name = "CadenceSensor")
    protected CadenceSensorTypeT cadenceSensor;

    /**
     * Obtiene el valor de la propiedad speed.
     * 
     * @return
     *     possible object is
     *     {@link Double }
     *     
     */
    public Double getSpeed() {
        return speed;
    }

    /**
     * Define el valor de la propiedad speed.
     * 
     * @param value
     *     allowed object is
     *     {@link Double }
     *     
     */
    public void setSpeed(Double value) {
        this.speed = value;
    }

    /**
     * Obtiene el valor de la propiedad runCadence.
     * 
     * @return
     *     possible object is
     *     {@link Short }
     *     
     */
    public Short getRunCadence() {
        return runCadence;
    }

    /**
     * Define el valor de la propiedad runCadence.
     * 
     * @param value
     *     allowed object is
     *     {@link Short }
     *     
     */
    public void setRunCadence(Short value) {
        this.runCadence = value;
    }

    /**
     * Obtiene el valor de la propiedad watts.
     * 
     * @return
     *     possible object is
     *     {@link Integer }
     *     
     */
    public Integer getWatts() {
        return watts;
    }

    /**
     * Define el valor de la propiedad watts.
     * 
     * @param value
     *     allowed object is
     *     {@link Integer }
     *     
     */
    public void setWatts(Integer value) {
        this.watts = value;
    }

    /**
     * Obtiene el valor de la propiedad extensions.
     * 
     * @return
     *     possible object is
     *     {@link ExtensionsT }
     *     
     */
    public ExtensionsT getExtensions() {
        return extensions;
    }

    /**
     * Define el valor de la propiedad extensions.
     * 
     * @param value
     *     allowed object is
     *     {@link ExtensionsT }
     *     
     */
    public void setExtensions(ExtensionsT value) {
        this.extensions = value;
    }

    /**
     * Obtiene el valor de la propiedad cadenceSensor.
     * 
     * @return
     *     possible object is
     *     {@link CadenceSensorTypeT }
     *     
     */
    public CadenceSensorTypeT getCadenceSensor() {
        return cadenceSensor;
    }

    /**
     * Define el valor de la propiedad cadenceSensor.
     * 
     * @param value
     *     allowed object is
     *     {@link CadenceSensorTypeT }
     *     
     */
    public void setCadenceSensor(CadenceSensorTypeT value) {
        this.cadenceSensor = value;
    }

}
