//
// Este archivo ha sido generado por la arquitectura JavaTM para la implantación de la referencia de enlace (JAXB) XML v2.2.11 
// Visite <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Todas las modificaciones realizadas en este archivo se perderán si se vuelve a compilar el esquema de origen. 
// Generado el: 2017.01.20 a las 05:42:52 PM CET 
//


package com.routeanalyzer.api.xml.tcx;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Clase Java para Cadence_t complex type.
 * 
 * <p>El siguiente fragmento de esquema especifica el contenido que se espera que haya en esta clase.
 * 
 * <pre>
 * &lt;complexType name="Cadence_t"&gt;
 *   &lt;complexContent&gt;
 *     &lt;extension base="{http://www.garmin.com/xmlschemas/TrainingCenterDatabase/v2}Target_t"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="Low" type="{http://www.w3.org/2001/XMLSchema}double"/&gt;
 *         &lt;element name="High" type="{http://www.w3.org/2001/XMLSchema}double"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/extension&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "Cadence_t", propOrder = {
    "low",
    "high"
})
public class CadenceT
    extends TargetT
{

    @XmlElement(name = "Low")
    protected double low;
    @XmlElement(name = "High")
    protected double high;

    /**
     * Obtiene el valor de la propiedad low.
     * 
     */
    public double getLow() {
        return low;
    }

    /**
     * Define el valor de la propiedad low.
     * 
     */
    public void setLow(double value) {
        this.low = value;
    }

    /**
     * Obtiene el valor de la propiedad high.
     * 
     */
    public double getHigh() {
        return high;
    }

    /**
     * Define el valor de la propiedad high.
     * 
     */
    public void setHigh(double value) {
        this.high = value;
    }

}
