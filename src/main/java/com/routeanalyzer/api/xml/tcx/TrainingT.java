//
// Este archivo ha sido generado por la arquitectura JavaTM para la implantación de la referencia de enlace (JAXB) XML v2.2.11 
// Visite <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Todas las modificaciones realizadas en este archivo se perderán si se vuelve a compilar el esquema de origen. 
// Generado el: 2017.01.20 a las 05:42:52 PM CET 
//


package com.routeanalyzer.api.xml.tcx;

import javax.xml.bind.annotation.*;


/**
 * <p>Clase Java para Training_t complex type.
 * 
 * <p>El siguiente fragmento de esquema especifica el contenido que se espera que haya en esta clase.
 * 
 * <pre>
 * &lt;complexType name="Training_t"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="QuickWorkoutResults" type="{http://www.garmin.com/xmlschemas/TrainingCenterDatabase/v2}QuickWorkout_t" minOccurs="0"/&gt;
 *         &lt;element name="Plan" type="{http://www.garmin.com/xmlschemas/TrainingCenterDatabase/v2}Plan_t" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *       &lt;attribute name="VirtualPartner" use="required" type="{http://www.w3.org/2001/XMLSchema}boolean" /&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "Training_t", propOrder = {
    "quickWorkoutResults",
    "plan"
})
public class TrainingT {

    @XmlElement(name = "QuickWorkoutResults")
    protected QuickWorkoutT quickWorkoutResults;
    @XmlElement(name = "Plan")
    protected PlanT plan;
    @XmlAttribute(name = "VirtualPartner", required = true)
    protected boolean virtualPartner;

    /**
     * Obtiene el valor de la propiedad quickWorkoutResults.
     * 
     * @return
     *     possible object is
     *     {@link QuickWorkoutT }
     *     
     */
    public QuickWorkoutT getQuickWorkoutResults() {
        return quickWorkoutResults;
    }

    /**
     * Define el valor de la propiedad quickWorkoutResults.
     * 
     * @param value
     *     allowed object is
     *     {@link QuickWorkoutT }
     *     
     */
    public void setQuickWorkoutResults(QuickWorkoutT value) {
        this.quickWorkoutResults = value;
    }

    /**
     * Obtiene el valor de la propiedad plan.
     * 
     * @return
     *     possible object is
     *     {@link PlanT }
     *     
     */
    public PlanT getPlan() {
        return plan;
    }

    /**
     * Define el valor de la propiedad plan.
     * 
     * @param value
     *     allowed object is
     *     {@link PlanT }
     *     
     */
    public void setPlan(PlanT value) {
        this.plan = value;
    }

    /**
     * Obtiene el valor de la propiedad virtualPartner.
     * 
     */
    public boolean isVirtualPartner() {
        return virtualPartner;
    }

    /**
     * Define el valor de la propiedad virtualPartner.
     * 
     */
    public void setVirtualPartner(boolean value) {
        this.virtualPartner = value;
    }

}
