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
 * Identifies a PC software application.
 * 
 * <p>Clase Java para Application_t complex type.
 * 
 * <p>El siguiente fragmento de esquema especifica el contenido que se espera que haya en esta clase.
 * 
 * <pre>
 * &lt;complexType name="Application_t"&gt;
 *   &lt;complexContent&gt;
 *     &lt;extension base="{http://www.garmin.com/xmlschemas/TrainingCenterDatabase/v2}AbstractSource_t"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="Build" type="{http://www.garmin.com/xmlschemas/TrainingCenterDatabase/v2}Build_t"/&gt;
 *         &lt;element name="LangID" type="{http://www.garmin.com/xmlschemas/TrainingCenterDatabase/v2}LangID_t"/&gt;
 *         &lt;element name="PartNumber" type="{http://www.garmin.com/xmlschemas/TrainingCenterDatabase/v2}PartNumber_t"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/extension&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "Application_t", propOrder = {
    "build",
    "langID",
    "partNumber"
})
public class ApplicationT
    extends AbstractSourceT
{

    @XmlElement(name = "Build", required = true)
    protected BuildT build;
    @XmlElement(name = "LangID", required = true)
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlSchemaType(name = "token")
    protected String langID;
    @XmlElement(name = "PartNumber", required = true)
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlSchemaType(name = "token")
    protected String partNumber;

    /**
     * Obtiene el valor de la propiedad build.
     * 
     * @return
     *     possible object is
     *     {@link BuildT }
     *     
     */
    public BuildT getBuild() {
        return build;
    }

    /**
     * Define el valor de la propiedad build.
     * 
     * @param value
     *     allowed object is
     *     {@link BuildT }
     *     
     */
    public void setBuild(BuildT value) {
        this.build = value;
    }

    /**
     * Obtiene el valor de la propiedad langID.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getLangID() {
        return langID;
    }

    /**
     * Define el valor de la propiedad langID.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setLangID(String value) {
        this.langID = value;
    }

    /**
     * Obtiene el valor de la propiedad partNumber.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getPartNumber() {
        return partNumber;
    }

    /**
     * Define el valor de la propiedad partNumber.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setPartNumber(String value) {
        this.partNumber = value;
    }

}
