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
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import java.util.ArrayList;
import java.util.List;


/**
 * <p>Clase Java para Repeat_t complex type.
 * 
 * <p>El siguiente fragmento de esquema especifica el contenido que se espera que haya en esta clase.
 * 
 * <pre>
 * &lt;complexType name="Repeat_t"&gt;
 *   &lt;complexContent&gt;
 *     &lt;extension base="{http://www.garmin.com/xmlschemas/TrainingCenterDatabase/v2}AbstractStep_t"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="Repetitions" type="{http://www.garmin.com/xmlschemas/TrainingCenterDatabase/v2}Repetitions_t"/&gt;
 *         &lt;element name="Child" type="{http://www.garmin.com/xmlschemas/TrainingCenterDatabase/v2}AbstractStep_t" maxOccurs="unbounded"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/extension&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "Repeat_t", propOrder = {
    "repetitions",
    "child"
})
public class RepeatT
    extends AbstractStepT
{

    @XmlElement(name = "Repetitions")
    @XmlSchemaType(name = "positiveInteger")
    protected int repetitions;
    @XmlElement(name = "Child", required = true)
    protected List<AbstractStepT> child;

    /**
     * Obtiene el valor de la propiedad repetitions.
     * 
     */
    public int getRepetitions() {
        return repetitions;
    }

    /**
     * Define el valor de la propiedad repetitions.
     * 
     */
    public void setRepetitions(int value) {
        this.repetitions = value;
    }

    /**
     * Gets the value of the child property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the child property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getChild().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link AbstractStepT }
     * 
     * 
     */
    public List<AbstractStepT> getChild() {
        if (child == null) {
            child = new ArrayList<AbstractStepT>();
        }
        return this.child;
    }

}
