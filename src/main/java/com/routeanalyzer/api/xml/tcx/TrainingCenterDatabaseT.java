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
 * <p>Clase Java para TrainingCenterDatabase_t complex type.
 * 
 * <p>El siguiente fragmento de esquema especifica el contenido que se espera que haya en esta clase.
 * 
 * <pre>
 * &lt;complexType name="TrainingCenterDatabase_t"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="Folders" type="{http://www.garmin.com/xmlschemas/TrainingCenterDatabase/v2}Folders_t" minOccurs="0"/&gt;
 *         &lt;element name="Activities" type="{http://www.garmin.com/xmlschemas/TrainingCenterDatabase/v2}ActivityList_t" minOccurs="0"/&gt;
 *         &lt;element name="Workouts" type="{http://www.garmin.com/xmlschemas/TrainingCenterDatabase/v2}WorkoutList_t" minOccurs="0"/&gt;
 *         &lt;element name="Courses" type="{http://www.garmin.com/xmlschemas/TrainingCenterDatabase/v2}CourseList_t" minOccurs="0"/&gt;
 *         &lt;element name="Author" type="{http://www.garmin.com/xmlschemas/TrainingCenterDatabase/v2}AbstractSource_t" minOccurs="0"/&gt;
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
@XmlType(name = "TrainingCenterDatabase_t", propOrder = {
    "folders",
    "activities",
    "workouts",
    "courses",
    "author",
    "extensions"
})
public class TrainingCenterDatabaseT {

    @XmlElement(name = "Folders")
    protected FoldersT folders;
    @XmlElement(name = "Activities")
    protected ActivityListT activities;
    @XmlElement(name = "Workouts")
    protected WorkoutListT workouts;
    @XmlElement(name = "Courses")
    protected CourseListT courses;
    @XmlElement(name = "Author")
    protected AbstractSourceT author;
    @XmlElement(name = "Extensions")
    protected ExtensionsT extensions;

    /**
     * Obtiene el valor de la propiedad folders.
     * 
     * @return
     *     possible object is
     *     {@link FoldersT }
     *     
     */
    public FoldersT getFolders() {
        return folders;
    }

    /**
     * Define el valor de la propiedad folders.
     * 
     * @param value
     *     allowed object is
     *     {@link FoldersT }
     *     
     */
    public void setFolders(FoldersT value) {
        this.folders = value;
    }

    /**
     * Obtiene el valor de la propiedad activities.
     * 
     * @return
     *     possible object is
     *     {@link ActivityListT }
     *     
     */
    public ActivityListT getActivities() {
        return activities;
    }

    /**
     * Define el valor de la propiedad activities.
     * 
     * @param value
     *     allowed object is
     *     {@link ActivityListT }
     *     
     */
    public void setActivities(ActivityListT value) {
        this.activities = value;
    }

    /**
     * Obtiene el valor de la propiedad workouts.
     * 
     * @return
     *     possible object is
     *     {@link WorkoutListT }
     *     
     */
    public WorkoutListT getWorkouts() {
        return workouts;
    }

    /**
     * Define el valor de la propiedad workouts.
     * 
     * @param value
     *     allowed object is
     *     {@link WorkoutListT }
     *     
     */
    public void setWorkouts(WorkoutListT value) {
        this.workouts = value;
    }

    /**
     * Obtiene el valor de la propiedad courses.
     * 
     * @return
     *     possible object is
     *     {@link CourseListT }
     *     
     */
    public CourseListT getCourses() {
        return courses;
    }

    /**
     * Define el valor de la propiedad courses.
     * 
     * @param value
     *     allowed object is
     *     {@link CourseListT }
     *     
     */
    public void setCourses(CourseListT value) {
        this.courses = value;
    }

    /**
     * Obtiene el valor de la propiedad author.
     * 
     * @return
     *     possible object is
     *     {@link AbstractSourceT }
     *     
     */
    public AbstractSourceT getAuthor() {
        return author;
    }

    /**
     * Define el valor de la propiedad author.
     * 
     * @param value
     *     allowed object is
     *     {@link AbstractSourceT }
     *     
     */
    public void setAuthor(AbstractSourceT value) {
        this.author = value;
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
