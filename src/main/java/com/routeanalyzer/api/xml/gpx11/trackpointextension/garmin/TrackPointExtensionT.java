//
// Este archivo ha sido generado por la arquitectura JavaTM para la implantación de la referencia de enlace (JAXB) XML v2.2.8-b130911.1802 
// Visite <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Todas las modificaciones realizadas en este archivo se perderán si se vuelve a compilar el esquema de origen. 
// Generado el: 2018.03.30 a las 11:23:58 PM CEST 
//


package com.routeanalyzer.api.xml.gpx11.trackpointextension.garmin;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;


/**
 * 
 *     This type contains data fields that cannot
 *     be represented in track points in GPX 1.1 instances.
 *     
 * 
 * <p>Clase Java para TrackPointExtension_t complex type.
 * 
 * <p>El siguiente fragmento de esquema especifica el contenido que se espera que haya en esta clase.
 * 
 * <pre>
 * &lt;complexType name="TrackPointExtension_t">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="atemp" type="{http://www.garmin.com/xmlschemas/TrackPointExtension/v1}DegreesCelsius_t" minOccurs="0"/>
 *         &lt;element name="wtemp" type="{http://www.garmin.com/xmlschemas/TrackPointExtension/v1}DegreesCelsius_t" minOccurs="0"/>
 *         &lt;element name="depth" type="{http://www.garmin.com/xmlschemas/TrackPointExtension/v1}Meters_t" minOccurs="0"/>
 *         &lt;element name="hr" type="{http://www.garmin.com/xmlschemas/TrackPointExtension/v1}BeatsPerMinute_t" minOccurs="0"/>
 *         &lt;element name="cad" type="{http://www.garmin.com/xmlschemas/TrackPointExtension/v1}RevolutionsPerMinute_t" minOccurs="0"/>
 *         &lt;element name="Extensions" type="{http://www.garmin.com/xmlschemas/TrackPointExtension/v1}Extensions_t" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "TrackPointExtension_t", propOrder = {
    "atemp",
    "wtemp",
    "depth",
    "hr",
    "cad",
    "extensions"
})
public class TrackPointExtensionT {

    protected Double atemp;
    protected Double wtemp;
    protected Double depth;
    @XmlSchemaType(name = "unsignedByte")
    protected Short hr;
    @XmlSchemaType(name = "unsignedByte")
    protected Short cad;
    @XmlElement(name = "Extensions")
    protected ExtensionsT extensions;

    /**
     * Obtiene el valor de la propiedad atemp.
     * 
     * @return
     *     possible object is
     *     {@link Double }
     *     
     */
    public Double getAtemp() {
        return atemp;
    }

    /**
     * Define el valor de la propiedad atemp.
     * 
     * @param value
     *     allowed object is
     *     {@link Double }
     *     
     */
    public void setAtemp(Double value) {
        this.atemp = value;
    }

    /**
     * Obtiene el valor de la propiedad wtemp.
     * 
     * @return
     *     possible object is
     *     {@link Double }
     *     
     */
    public Double getWtemp() {
        return wtemp;
    }

    /**
     * Define el valor de la propiedad wtemp.
     * 
     * @param value
     *     allowed object is
     *     {@link Double }
     *     
     */
    public void setWtemp(Double value) {
        this.wtemp = value;
    }

    /**
     * Obtiene el valor de la propiedad depth.
     * 
     * @return
     *     possible object is
     *     {@link Double }
     *     
     */
    public Double getDepth() {
        return depth;
    }

    /**
     * Define el valor de la propiedad depth.
     * 
     * @param value
     *     allowed object is
     *     {@link Double }
     *     
     */
    public void setDepth(Double value) {
        this.depth = value;
    }

    /**
     * Obtiene el valor de la propiedad hr.
     * 
     * @return
     *     possible object is
     *     {@link Short }
     *     
     */
    public Short getHr() {
        return hr;
    }

    /**
     * Define el valor de la propiedad hr.
     * 
     * @param value
     *     allowed object is
     *     {@link Short }
     *     
     */
    public void setHr(Short value) {
        this.hr = value;
    }

    /**
     * Obtiene el valor de la propiedad cad.
     * 
     * @return
     *     possible object is
     *     {@link Short }
     *     
     */
    public Short getCad() {
        return cad;
    }

    /**
     * Define el valor de la propiedad cad.
     * 
     * @param value
     *     allowed object is
     *     {@link Short }
     *     
     */
    public void setCad(Short value) {
        this.cad = value;
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

}
