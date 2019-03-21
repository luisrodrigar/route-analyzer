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
import javax.xml.bind.annotation.adapters.CollapsedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;


/**
 * <p>Clase Java para Step_t complex type.
 * 
 * <p>El siguiente fragmento de esquema especifica el contenido que se espera que haya en esta clase.
 * 
 * <pre>
 * &lt;complexType name="Step_t"&gt;
 *   &lt;complexContent&gt;
 *     &lt;extension base="{http://www.garmin.com/xmlschemas/TrainingCenterDatabase/v2}AbstractStep_t"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="Name" type="{http://www.garmin.com/xmlschemas/TrainingCenterDatabase/v2}RestrictedToken_t" minOccurs="0"/&gt;
 *         &lt;element name="Duration" type="{http://www.garmin.com/xmlschemas/TrainingCenterDatabase/v2}Duration_t"/&gt;
 *         &lt;element name="Intensity" type="{http://www.garmin.com/xmlschemas/TrainingCenterDatabase/v2}Intensity_t"/&gt;
 *         &lt;element name="Target" type="{http://www.garmin.com/xmlschemas/TrainingCenterDatabase/v2}Target_t"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/extension&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "Step_t", propOrder = {
    "name",
    "duration",
    "intensity",
    "target"
})
public class StepT
    extends AbstractStepT
{

    @XmlElement(name = "Name")
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlSchemaType(name = "token")
    protected String name;
    @XmlElement(name = "Duration", required = true)
    protected DurationT duration;
    @XmlElement(name = "Intensity", required = true)
    @XmlSchemaType(name = "token")
    protected IntensityT intensity;
    @XmlElement(name = "Target", required = true)
    protected TargetT target;

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
     * Obtiene el valor de la propiedad duration.
     * 
     * @return
     *     possible object is
     *     {@link DurationT }
     *     
     */
    public DurationT getDuration() {
        return duration;
    }

    /**
     * Define el valor de la propiedad duration.
     * 
     * @param value
     *     allowed object is
     *     {@link DurationT }
     *     
     */
    public void setDuration(DurationT value) {
        this.duration = value;
    }

    /**
     * Obtiene el valor de la propiedad intensity.
     * 
     * @return
     *     possible object is
     *     {@link IntensityT }
     *     
     */
    public IntensityT getIntensity() {
        return intensity;
    }

    /**
     * Define el valor de la propiedad intensity.
     * 
     * @param value
     *     allowed object is
     *     {@link IntensityT }
     *     
     */
    public void setIntensity(IntensityT value) {
        this.intensity = value;
    }

    /**
     * Obtiene el valor de la propiedad target.
     * 
     * @return
     *     possible object is
     *     {@link TargetT }
     *     
     */
    public TargetT getTarget() {
        return target;
    }

    /**
     * Define el valor de la propiedad target.
     * 
     * @param value
     *     allowed object is
     *     {@link TargetT }
     *     
     */
    public void setTarget(TargetT value) {
        this.target = value;
    }

}
