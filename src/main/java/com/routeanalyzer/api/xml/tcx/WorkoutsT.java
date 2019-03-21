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
 * <p>Clase Java para Workouts_t complex type.
 * 
 * <p>El siguiente fragmento de esquema especifica el contenido que se espera que haya en esta clase.
 * 
 * <pre>
 * &lt;complexType name="Workouts_t"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="Running" type="{http://www.garmin.com/xmlschemas/TrainingCenterDatabase/v2}WorkoutFolder_t"/&gt;
 *         &lt;element name="Biking" type="{http://www.garmin.com/xmlschemas/TrainingCenterDatabase/v2}WorkoutFolder_t"/&gt;
 *         &lt;element name="Other" type="{http://www.garmin.com/xmlschemas/TrainingCenterDatabase/v2}WorkoutFolder_t"/&gt;
 *         &lt;element name="Extensions" type="{http://www.garmin.com/xmlschemas/TrainingCenterDatabase/v2}Extensions_t" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "Workouts_t", propOrder = {
    "running",
    "biking",
    "other",
    "extensions"
})
public class WorkoutsT {

    @XmlElement(name = "Running", required = true)
    protected WorkoutFolderT running;
    @XmlElement(name = "Biking", required = true)
    protected WorkoutFolderT biking;
    @XmlElement(name = "Other", required = true)
    protected WorkoutFolderT other;
    @XmlElement(name = "Extensions")
    protected ExtensionsT extensions;

    /**
     * Obtiene el valor de la propiedad running.
     * 
     * @return
     *     possible object is
     *     {@link WorkoutFolderT }
     *     
     */
    public WorkoutFolderT getRunning() {
        return running;
    }

    /**
     * Define el valor de la propiedad running.
     * 
     * @param value
     *     allowed object is
     *     {@link WorkoutFolderT }
     *     
     */
    public void setRunning(WorkoutFolderT value) {
        this.running = value;
    }

    /**
     * Obtiene el valor de la propiedad biking.
     * 
     * @return
     *     possible object is
     *     {@link WorkoutFolderT }
     *     
     */
    public WorkoutFolderT getBiking() {
        return biking;
    }

    /**
     * Define el valor de la propiedad biking.
     * 
     * @param value
     *     allowed object is
     *     {@link WorkoutFolderT }
     *     
     */
    public void setBiking(WorkoutFolderT value) {
        this.biking = value;
    }

    /**
     * Obtiene el valor de la propiedad other.
     * 
     * @return
     *     possible object is
     *     {@link WorkoutFolderT }
     *     
     */
    public WorkoutFolderT getOther() {
        return other;
    }

    /**
     * Define el valor de la propiedad other.
     * 
     * @param value
     *     allowed object is
     *     {@link WorkoutFolderT }
     *     
     */
    public void setOther(WorkoutFolderT value) {
        this.other = value;
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
