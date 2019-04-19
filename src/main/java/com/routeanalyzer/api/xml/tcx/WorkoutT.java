//
// Este archivo ha sido generado por la arquitectura JavaTM para la implantación de la referencia de enlace (JAXB) XML v2.2.11 
// Visite <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Todas las modificaciones realizadas en este archivo se perderán si se vuelve a compilar el esquema de origen. 
// Generado el: 2017.01.20 a las 05:42:52 PM CET 
//


package com.routeanalyzer.api.xml.tcx;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.CollapsedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import javax.xml.datatype.XMLGregorianCalendar;
import java.util.ArrayList;
import java.util.List;


/**
 * <p>Clase Java para Workout_t complex type.
 * 
 * <p>El siguiente fragmento de esquema especifica el contenido que se espera que haya en esta clase.
 * 
 * <pre>
 * &lt;complexType name="Workout_t"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="Name" type="{http://www.garmin.com/xmlschemas/TrainingCenterDatabase/v2}RestrictedToken_t"/&gt;
 *         &lt;element name="Step" type="{http://www.garmin.com/xmlschemas/TrainingCenterDatabase/v2}AbstractStep_t" maxOccurs="unbounded"/&gt;
 *         &lt;element name="ScheduledOn" type="{http://www.w3.org/2001/XMLSchema}date" maxOccurs="unbounded" minOccurs="0"/&gt;
 *         &lt;element name="Notes" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/&gt;
 *         &lt;element name="Creator" type="{http://www.garmin.com/xmlschemas/TrainingCenterDatabase/v2}AbstractSource_t" minOccurs="0"/&gt;
 *         &lt;element name="Extensions" type="{http://www.garmin.com/xmlschemas/TrainingCenterDatabase/v2}Extensions_t" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *       &lt;attribute name="Sport" use="required" type="{http://www.garmin.com/xmlschemas/TrainingCenterDatabase/v2}Sport_t" /&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "Workout_t", propOrder = {
    "name",
    "step",
    "scheduledOn",
    "notes",
    "creator",
    "extensions"
})
public class WorkoutT {

    @XmlElement(name = "Name", required = true)
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlSchemaType(name = "token")
    protected String name;
    @XmlElement(name = "Step", required = true)
    protected List<AbstractStepT> step;
    @XmlElement(name = "ScheduledOn")
    @XmlSchemaType(name = "date")
    protected List<XMLGregorianCalendar> scheduledOn;
    @XmlElement(name = "Notes")
    protected String notes;
    @XmlElement(name = "Creator")
    protected AbstractSourceT creator;
    @XmlElement(name = "Extensions")
    protected ExtensionsT extensions;
    @XmlAttribute(name = "Sport", required = true)
    protected SportT sport;

    /**
     * Obtiene el valor de la propiedad name.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getName() {
        return name;
    }

    /**
     * Define el valor de la propiedad name.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setName(String value) {
        this.name = value;
    }

    /**
     * Gets the value of the step property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the step property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getStep().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link AbstractStepT }
     * 
     * 
     */
    public List<AbstractStepT> getStep() {
        if (step == null) {
            step = new ArrayList<AbstractStepT>();
        }
        return this.step;
    }

    /**
     * Gets the value of the scheduledOn property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the scheduledOn property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getScheduledOn().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link XMLGregorianCalendar }
     * 
     * 
     */
    public List<XMLGregorianCalendar> getScheduledOn() {
        if (scheduledOn == null) {
            scheduledOn = new ArrayList<XMLGregorianCalendar>();
        }
        return this.scheduledOn;
    }

    /**
     * Obtiene el valor de la propiedad notes.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getNotes() {
        return notes;
    }

    /**
     * Define el valor de la propiedad notes.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setNotes(String value) {
        this.notes = value;
    }

    /**
     * Obtiene el valor de la propiedad creator.
     * 
     * @return
     *     possible object is
     *     {@link AbstractSourceT }
     *     
     */
    public AbstractSourceT getCreator() {
        return creator;
    }

    /**
     * Define el valor de la propiedad creator.
     * 
     * @param value
     *     allowed object is
     *     {@link AbstractSourceT }
     *     
     */
    public void setCreator(AbstractSourceT value) {
        this.creator = value;
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

    /**
     * Obtiene el valor de la propiedad sport.
     * 
     * @return
     *     possible object is
     *     {@link SportT }
     *     
     */
    public SportT getSport() {
        return sport;
    }

    /**
     * Define el valor de la propiedad sport.
     * 
     * @param value
     *     allowed object is
     *     {@link SportT }
     *     
     */
    public void setSport(SportT value) {
        this.sport = value;
    }

}
