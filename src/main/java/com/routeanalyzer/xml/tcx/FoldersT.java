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
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Clase Java para Folders_t complex type.
 * 
 * <p>El siguiente fragmento de esquema especifica el contenido que se espera que haya en esta clase.
 * 
 * <pre>
 * &lt;complexType name="Folders_t"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="History" type="{http://www.garmin.com/xmlschemas/TrainingCenterDatabase/v2}History_t" minOccurs="0"/&gt;
 *         &lt;element name="Workouts" type="{http://www.garmin.com/xmlschemas/TrainingCenterDatabase/v2}Workouts_t" minOccurs="0"/&gt;
 *         &lt;element name="Courses" type="{http://www.garmin.com/xmlschemas/TrainingCenterDatabase/v2}Courses_t" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "Folders_t", propOrder = {
    "history",
    "workouts",
    "courses"
})
public class FoldersT {

    @XmlElement(name = "History")
    protected HistoryT history;
    @XmlElement(name = "Workouts")
    protected WorkoutsT workouts;
    @XmlElement(name = "Courses")
    protected CoursesT courses;

    /**
     * Obtiene el valor de la propiedad history.
     * 
     * @return
     *     possible object is
     *     {@link HistoryT }
     *     
     */
    public HistoryT getHistory() {
        return history;
    }

    /**
     * Define el valor de la propiedad history.
     * 
     * @param value
     *     allowed object is
     *     {@link HistoryT }
     *     
     */
    public void setHistory(HistoryT value) {
        this.history = value;
    }

    /**
     * Obtiene el valor de la propiedad workouts.
     * 
     * @return
     *     possible object is
     *     {@link WorkoutsT }
     *     
     */
    public WorkoutsT getWorkouts() {
        return workouts;
    }

    /**
     * Define el valor de la propiedad workouts.
     * 
     * @param value
     *     allowed object is
     *     {@link WorkoutsT }
     *     
     */
    public void setWorkouts(WorkoutsT value) {
        this.workouts = value;
    }

    /**
     * Obtiene el valor de la propiedad courses.
     * 
     * @return
     *     possible object is
     *     {@link CoursesT }
     *     
     */
    public CoursesT getCourses() {
        return courses;
    }

    /**
     * Define el valor de la propiedad courses.
     * 
     * @param value
     *     allowed object is
     *     {@link CoursesT }
     *     
     */
    public void setCourses(CoursesT value) {
        this.courses = value;
    }

}
