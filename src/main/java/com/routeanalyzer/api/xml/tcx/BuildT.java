//
// Este archivo ha sido generado por la arquitectura JavaTM para la implantación de la referencia de enlace (JAXB) XML v2.2.11 
// Visite <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Todas las modificaciones realizadas en este archivo se perderán si se vuelve a compilar el esquema de origen. 
// Generado el: 2017.01.20 a las 05:42:52 PM CET 
//


package com.routeanalyzer.api.xml.tcx;

import javax.xml.bind.annotation.*;
import javax.xml.bind.annotation.adapters.CollapsedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;


/**
 * <p>Clase Java para Build_t complex type.
 * 
 * <p>El siguiente fragmento de esquema especifica el contenido que se espera que haya en esta clase.
 * 
 * <pre>
 * &lt;complexType name="Build_t"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="Version" type="{http://www.garmin.com/xmlschemas/TrainingCenterDatabase/v2}Version_t"/&gt;
 *         &lt;element name="Type" type="{http://www.garmin.com/xmlschemas/TrainingCenterDatabase/v2}BuildType_t" minOccurs="0"/&gt;
 *         &lt;element name="Time" type="{http://www.garmin.com/xmlschemas/TrainingCenterDatabase/v2}Token_t" minOccurs="0"/&gt;
 *         &lt;element name="Builder" type="{http://www.garmin.com/xmlschemas/TrainingCenterDatabase/v2}Token_t" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "Build_t", propOrder = {
    "version",
    "type",
    "time",
    "builder"
})
public class BuildT {

    @XmlElement(name = "Version", required = true)
    protected VersionT version;
    @XmlElement(name = "Type")
    @XmlSchemaType(name = "token")
    protected BuildTypeT type;
    @XmlElement(name = "Time")
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlSchemaType(name = "token")
    protected String time;
    @XmlElement(name = "Builder")
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlSchemaType(name = "token")
    protected String builder;

    /**
     * Obtiene el valor de la propiedad version.
     * 
     * @return
     *     possible object is
     *     {@link VersionT }
     *     
     */
    public VersionT getVersion() {
        return version;
    }

    /**
     * Define el valor de la propiedad version.
     * 
     * @param value
     *     allowed object is
     *     {@link VersionT }
     *     
     */
    public void setVersion(VersionT value) {
        this.version = value;
    }

    /**
     * Obtiene el valor de la propiedad type.
     * 
     * @return
     *     possible object is
     *     {@link BuildTypeT }
     *     
     */
    public BuildTypeT getType() {
        return type;
    }

    /**
     * Define el valor de la propiedad type.
     * 
     * @param value
     *     allowed object is
     *     {@link BuildTypeT }
     *     
     */
    public void setType(BuildTypeT value) {
        this.type = value;
    }

    /**
     * Obtiene el valor de la propiedad time.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getTime() {
        return time;
    }

    /**
     * Define el valor de la propiedad time.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setTime(String value) {
        this.time = value;
    }

    /**
     * Obtiene el valor de la propiedad builder.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getBuilder() {
        return builder;
    }

    /**
     * Define el valor de la propiedad builder.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setBuilder(String value) {
        this.builder = value;
    }

}
