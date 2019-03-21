//
// Este archivo ha sido generado por la arquitectura JavaTM para la implantación de la referencia de enlace (JAXB) XML v2.2.11 
// Visite <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Todas las modificaciones realizadas en este archivo se perderán si se vuelve a compilar el esquema de origen. 
// Generado el: 2017.01.20 a las 05:42:52 PM CET 
//


package com.routeanalyzer.xml.tcx;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Clase Java para ActivityList_t complex type.
 * 
 * <p>El siguiente fragmento de esquema especifica el contenido que se espera que haya en esta clase.
 * 
 * <pre>
 * &lt;complexType name="ActivityList_t"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="Activity" type="{http://www.garmin.com/xmlschemas/TrainingCenterDatabase/v2}Activity_t" maxOccurs="unbounded" minOccurs="0"/&gt;
 *         &lt;element name="MultiSportSession" type="{http://www.garmin.com/xmlschemas/TrainingCenterDatabase/v2}MultiSportSession_t" maxOccurs="unbounded" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ActivityList_t", propOrder = {
    "activity",
    "multiSportSession"
})
public class ActivityListT {

    @XmlElement(name = "Activity")
    protected List<ActivityT> activity;
    @XmlElement(name = "MultiSportSession")
    protected List<MultiSportSessionT> multiSportSession;

    public ActivityListT(){
    	this.activity = new ArrayList<ActivityT>();
    }
    
    /**
     * Gets the value of the activity property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the activity property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getActivity().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link ActivityT }
     * 
     * 
     */
    public List<ActivityT> getActivity() {
        if (activity == null) {
            activity = new ArrayList<ActivityT>();
        }
        return this.activity;
    }
    
    public void addActivity(ActivityT act) {
        activity.add(act);
    }

    /**
     * Gets the value of the multiSportSession property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the multiSportSession property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getMultiSportSession().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link MultiSportSessionT }
     * 
     * 
     */
    public List<MultiSportSessionT> getMultiSportSession() {
        if (multiSportSession == null) {
            multiSportSession = new ArrayList<MultiSportSessionT>();
        }
        return this.multiSportSession;
    }

}
