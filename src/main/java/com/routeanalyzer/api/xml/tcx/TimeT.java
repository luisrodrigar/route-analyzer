//
// Este archivo ha sido generado por la arquitectura JavaTM para la implantación de la referencia de enlace (JAXB) XML v2.2.11 
// Visite <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Todas las modificaciones realizadas en este archivo se perderán si se vuelve a compilar el esquema de origen. 
// Generado el: 2017.01.20 a las 05:42:52 PM CET 
//


package com.routeanalyzer.api.xml.tcx;

import javax.xml.bind.annotation.*;


/**
 * <p>Clase Java para Time_t complex type.
 * 
 * <p>El siguiente fragmento de esquema especifica el contenido que se espera que haya en esta clase.
 * 
 * <pre>
 * &lt;complexType name="Time_t"&gt;
 *   &lt;complexContent&gt;
 *     &lt;extension base="{http://www.garmin.com/xmlschemas/TrainingCenterDatabase/v2}Duration_t"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="Seconds" type="{http://www.w3.org/2001/XMLSchema}unsignedShort"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/extension&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "Time_t", propOrder = {
    "seconds"
})
public class TimeT
    extends DurationT
{

    @XmlElement(name = "Seconds")
    @XmlSchemaType(name = "unsignedShort")
    protected int seconds;

    /**
     * Obtiene el valor de la propiedad seconds.
     * 
     */
    public int getSeconds() {
        return seconds;
    }

    /**
     * Define el valor de la propiedad seconds.
     * 
     */
    public void setSeconds(int value) {
        this.seconds = value;
    }

}
