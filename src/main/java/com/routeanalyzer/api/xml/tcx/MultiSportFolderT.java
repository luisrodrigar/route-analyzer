//
// Este archivo ha sido generado por la arquitectura JavaTM para la implantación de la referencia de enlace (JAXB) XML v2.2.11 
// Visite <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Todas las modificaciones realizadas en este archivo se perderán si se vuelve a compilar el esquema de origen. 
// Generado el: 2017.01.20 a las 05:42:52 PM CET 
//


package com.routeanalyzer.api.xml.tcx;

import javax.xml.bind.annotation.*;
import java.util.ArrayList;
import java.util.List;


/**
 * <p>Clase Java para MultiSportFolder_t complex type.
 * 
 * <p>El siguiente fragmento de esquema especifica el contenido que se espera que haya en esta clase.
 * 
 * <pre>
 * &lt;complexType name="MultiSportFolder_t"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="Folder" type="{http://www.garmin.com/xmlschemas/TrainingCenterDatabase/v2}MultiSportFolder_t" maxOccurs="unbounded" minOccurs="0"/&gt;
 *         &lt;element name="MultisportActivityRef" type="{http://www.garmin.com/xmlschemas/TrainingCenterDatabase/v2}ActivityReference_t" maxOccurs="unbounded" minOccurs="0"/&gt;
 *         &lt;element name="Week" type="{http://www.garmin.com/xmlschemas/TrainingCenterDatabase/v2}Week_t" maxOccurs="unbounded" minOccurs="0"/&gt;
 *         &lt;element name="Notes" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/&gt;
 *         &lt;element name="Extensions" type="{http://www.garmin.com/xmlschemas/TrainingCenterDatabase/v2}Extensions_t" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *       &lt;attribute name="Name" use="required" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "MultiSportFolder_t", propOrder = {
    "folder",
    "multisportActivityRef",
    "week",
    "notes",
    "extensions"
})
public class MultiSportFolderT {

    @XmlElement(name = "Folder")
    protected List<MultiSportFolderT> folder;
    @XmlElement(name = "MultisportActivityRef")
    protected List<ActivityReferenceT> multisportActivityRef;
    @XmlElement(name = "Week")
    protected List<WeekT> week;
    @XmlElement(name = "Notes")
    protected String notes;
    @XmlElement(name = "Extensions")
    protected ExtensionsT extensions;
    @XmlAttribute(name = "Name", required = true)
    protected String name;

    /**
     * Gets the value of the folder property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the folder property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getFolder().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link MultiSportFolderT }
     * 
     * 
     */
    public List<MultiSportFolderT> getFolder() {
        if (folder == null) {
            folder = new ArrayList<MultiSportFolderT>();
        }
        return this.folder;
    }

    /**
     * Gets the value of the multisportActivityRef property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the multisportActivityRef property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getMultisportActivityRef().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link ActivityReferenceT }
     * 
     * 
     */
    public List<ActivityReferenceT> getMultisportActivityRef() {
        if (multisportActivityRef == null) {
            multisportActivityRef = new ArrayList<ActivityReferenceT>();
        }
        return this.multisportActivityRef;
    }

    /**
     * Gets the value of the week property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the week property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getWeek().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link WeekT }
     * 
     * 
     */
    public List<WeekT> getWeek() {
        if (week == null) {
            week = new ArrayList<WeekT>();
        }
        return this.week;
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

}
