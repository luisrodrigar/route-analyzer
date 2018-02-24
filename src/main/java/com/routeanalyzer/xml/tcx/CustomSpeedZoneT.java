//
// Este archivo ha sido generado por la arquitectura JavaTM para la implantación de la referencia de enlace (JAXB) XML v2.2.11 
// Visite <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Todas las modificaciones realizadas en este archivo se perderán si se vuelve a compilar el esquema de origen. 
// Generado el: 2017.01.20 a las 05:42:52 PM CET 
//


package com.routeanalyzer.xml.tcx;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Clase Java para CustomSpeedZone_t complex type.
 * 
 * <p>El siguiente fragmento de esquema especifica el contenido que se espera que haya en esta clase.
 * 
 * <pre>
 * &lt;complexType name="CustomSpeedZone_t"&gt;
 *   &lt;complexContent&gt;
 *     &lt;extension base="{http://www.garmin.com/xmlschemas/TrainingCenterDatabase/v2}Zone_t"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="ViewAs" type="{http://www.garmin.com/xmlschemas/TrainingCenterDatabase/v2}SpeedType_t"/&gt;
 *         &lt;element name="LowInMetersPerSecond" type="{http://www.garmin.com/xmlschemas/TrainingCenterDatabase/v2}SpeedInMetersPerSecond_t"/&gt;
 *         &lt;element name="HighInMetersPerSecond" type="{http://www.garmin.com/xmlschemas/TrainingCenterDatabase/v2}SpeedInMetersPerSecond_t"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/extension&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CustomSpeedZone_t", propOrder = {
    "viewAs",
    "lowInMetersPerSecond",
    "highInMetersPerSecond"
})
public class CustomSpeedZoneT
    extends ZoneT
{

    @XmlElement(name = "ViewAs", required = true)
    @XmlSchemaType(name = "token")
    protected SpeedTypeT viewAs;
    @XmlElement(name = "LowInMetersPerSecond")
    protected double lowInMetersPerSecond;
    @XmlElement(name = "HighInMetersPerSecond")
    protected double highInMetersPerSecond;

    /**
     * Obtiene el valor de la propiedad viewAs.
     * 
     * @return
     *     possible object is
     *     {@link SpeedTypeT }
     *     
     */
    public SpeedTypeT getViewAs() {
        return viewAs;
    }

    /**
     * Define el valor de la propiedad viewAs.
     * 
     * @param value
     *     allowed object is
     *     {@link SpeedTypeT }
     *     
     */
    public void setViewAs(SpeedTypeT value) {
        this.viewAs = value;
    }

    /**
     * Obtiene el valor de la propiedad lowInMetersPerSecond.
     * 
     */
    public double getLowInMetersPerSecond() {
        return lowInMetersPerSecond;
    }

    /**
     * Define el valor de la propiedad lowInMetersPerSecond.
     * 
     */
    public void setLowInMetersPerSecond(double value) {
        this.lowInMetersPerSecond = value;
    }

    /**
     * Obtiene el valor de la propiedad highInMetersPerSecond.
     * 
     */
    public double getHighInMetersPerSecond() {
        return highInMetersPerSecond;
    }

    /**
     * Define el valor de la propiedad highInMetersPerSecond.
     * 
     */
    public void setHighInMetersPerSecond(double value) {
        this.highInMetersPerSecond = value;
    }

}
