//
// Este archivo ha sido generado por la arquitectura JavaTM para la implantación de la referencia de enlace (JAXB) XML v2.2.8-b130911.1802 
// Visite <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Todas las modificaciones realizadas en este archivo se perderán si se vuelve a compilar el esquema de origen. 
// Generado el: 2018.03.31 a las 12:10:25 AM CEST 
//


package com.routeanalyzer.api.xml.tcx.activityextension;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Clase Java para ActivityLapExtension_t complex type.
 * 
 * <p>El siguiente fragmento de esquema especifica el contenido que se espera que haya en esta clase.
 * 
 * <pre>
 * &lt;complexType name="ActivityLapExtension_t">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="AvgSpeed" type="{http://www.w3.org/2001/XMLSchema}double" minOccurs="0"/>
 *         &lt;element name="MaxBikeCadence" type="{http://www.garmin.com/xmlschemas/ActivityExtension/v2}CadenceValue_t" minOccurs="0"/>
 *         &lt;element name="AvgRunCadence" type="{http://www.garmin.com/xmlschemas/ActivityExtension/v2}CadenceValue_t" minOccurs="0"/>
 *         &lt;element name="MaxRunCadence" type="{http://www.garmin.com/xmlschemas/ActivityExtension/v2}CadenceValue_t" minOccurs="0"/>
 *         &lt;element name="Steps" type="{http://www.w3.org/2001/XMLSchema}unsignedShort" minOccurs="0"/>
 *         &lt;element name="AvgWatts" type="{http://www.w3.org/2001/XMLSchema}unsignedShort" minOccurs="0"/>
 *         &lt;element name="MaxWatts" type="{http://www.w3.org/2001/XMLSchema}unsignedShort" minOccurs="0"/>
 *         &lt;element name="Extensions" type="{http://www.garmin.com/xmlschemas/ActivityExtension/v2}Extensions_t" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ActivityLapExtension_t", propOrder = {
    "avgSpeed",
    "maxBikeCadence",
    "avgRunCadence",
    "maxRunCadence",
    "steps",
    "avgWatts",
    "maxWatts",
    "extensions"
})
public class ActivityLapExtensionT {

    @XmlElement(name = "AvgSpeed")
    protected Double avgSpeed;
    @XmlElement(name = "MaxBikeCadence")
    @XmlSchemaType(name = "unsignedByte")
    protected Short maxBikeCadence;
    @XmlElement(name = "AvgRunCadence")
    @XmlSchemaType(name = "unsignedByte")
    protected Short avgRunCadence;
    @XmlElement(name = "MaxRunCadence")
    @XmlSchemaType(name = "unsignedByte")
    protected Short maxRunCadence;
    @XmlElement(name = "Steps")
    @XmlSchemaType(name = "unsignedShort")
    protected Integer steps;
    @XmlElement(name = "AvgWatts")
    @XmlSchemaType(name = "unsignedShort")
    protected Integer avgWatts;
    @XmlElement(name = "MaxWatts")
    @XmlSchemaType(name = "unsignedShort")
    protected Integer maxWatts;
    @XmlElement(name = "Extensions")
    protected ExtensionsT extensions;

    /**
     * Obtiene el valor de la propiedad avgSpeed.
     * 
     * @return
     *     possible object is
     *     {@link Double }
     *     
     */
    public Double getAvgSpeed() {
        return avgSpeed;
    }

    /**
     * Define el valor de la propiedad avgSpeed.
     * 
     * @param value
     *     allowed object is
     *     {@link Double }
     *     
     */
    public void setAvgSpeed(Double value) {
        this.avgSpeed = value;
    }

    /**
     * Obtiene el valor de la propiedad maxBikeCadence.
     * 
     * @return
     *     possible object is
     *     {@link Short }
     *     
     */
    public Short getMaxBikeCadence() {
        return maxBikeCadence;
    }

    /**
     * Define el valor de la propiedad maxBikeCadence.
     * 
     * @param value
     *     allowed object is
     *     {@link Short }
     *     
     */
    public void setMaxBikeCadence(Short value) {
        this.maxBikeCadence = value;
    }

    /**
     * Obtiene el valor de la propiedad avgRunCadence.
     * 
     * @return
     *     possible object is
     *     {@link Short }
     *     
     */
    public Short getAvgRunCadence() {
        return avgRunCadence;
    }

    /**
     * Define el valor de la propiedad avgRunCadence.
     * 
     * @param value
     *     allowed object is
     *     {@link Short }
     *     
     */
    public void setAvgRunCadence(Short value) {
        this.avgRunCadence = value;
    }

    /**
     * Obtiene el valor de la propiedad maxRunCadence.
     * 
     * @return
     *     possible object is
     *     {@link Short }
     *     
     */
    public Short getMaxRunCadence() {
        return maxRunCadence;
    }

    /**
     * Define el valor de la propiedad maxRunCadence.
     * 
     * @param value
     *     allowed object is
     *     {@link Short }
     *     
     */
    public void setMaxRunCadence(Short value) {
        this.maxRunCadence = value;
    }

    /**
     * Obtiene el valor de la propiedad steps.
     * 
     * @return
     *     possible object is
     *     {@link Integer }
     *     
     */
    public Integer getSteps() {
        return steps;
    }

    /**
     * Define el valor de la propiedad steps.
     * 
     * @param value
     *     allowed object is
     *     {@link Integer }
     *     
     */
    public void setSteps(Integer value) {
        this.steps = value;
    }

    /**
     * Obtiene el valor de la propiedad avgWatts.
     * 
     * @return
     *     possible object is
     *     {@link Integer }
     *     
     */
    public Integer getAvgWatts() {
        return avgWatts;
    }

    /**
     * Define el valor de la propiedad avgWatts.
     * 
     * @param value
     *     allowed object is
     *     {@link Integer }
     *     
     */
    public void setAvgWatts(Integer value) {
        this.avgWatts = value;
    }

    /**
     * Obtiene el valor de la propiedad maxWatts.
     * 
     * @return
     *     possible object is
     *     {@link Integer }
     *     
     */
    public Integer getMaxWatts() {
        return maxWatts;
    }

    /**
     * Define el valor de la propiedad maxWatts.
     * 
     * @param value
     *     allowed object is
     *     {@link Integer }
     *     
     */
    public void setMaxWatts(Integer value) {
        this.maxWatts = value;
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
