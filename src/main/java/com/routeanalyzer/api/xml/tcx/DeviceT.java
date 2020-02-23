//
// Este archivo ha sido generado por la arquitectura JavaTM para la implantación de la referencia de enlace (JAXB) XML v2.2.11 
// Visite <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Todas las modificaciones realizadas en este archivo se perderán si se vuelve a compilar el esquema de origen. 
// Generado el: 2017.01.20 a las 05:42:52 PM CET 
//


package com.routeanalyzer.api.xml.tcx;

import javax.xml.bind.annotation.*;


/**
 * Identifies the originating GPS device that tracked a run or
 *                                used to identify the type of device capable of handling
 *                                the data for loading.
 * 
 * <p>Clase Java para Device_t complex type.
 * 
 * <p>El siguiente fragmento de esquema especifica el contenido que se espera que haya en esta clase.
 * 
 * <pre>
 * &lt;complexType name="Device_t"&gt;
 *   &lt;complexContent&gt;
 *     &lt;extension base="{http://www.garmin.com/xmlschemas/TrainingCenterDatabase/v2}AbstractSource_t"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="UnitId" type="{http://www.w3.org/2001/XMLSchema}unsignedInt"/&gt;
 *         &lt;element name="ProductID" type="{http://www.w3.org/2001/XMLSchema}unsignedShort"/&gt;
 *         &lt;element name="Version" type="{http://www.garmin.com/xmlschemas/TrainingCenterDatabase/v2}Version_t"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/extension&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "Device_t", propOrder = {
    "unitId",
    "productID",
    "version"
})
public class DeviceT
    extends AbstractSourceT
{

    @XmlElement(name = "UnitId")
    @XmlSchemaType(name = "unsignedInt")
    protected long unitId;
    @XmlElement(name = "ProductID")
    @XmlSchemaType(name = "unsignedShort")
    protected int productID;
    @XmlElement(name = "Version", required = true)
    protected VersionT version;

    /**
     * Obtiene el valor de la propiedad unitId.
     * 
     */
    public long getUnitId() {
        return unitId;
    }

    /**
     * Define el valor de la propiedad unitId.
     * 
     */
    public void setUnitId(long value) {
        this.unitId = value;
    }

    /**
     * Obtiene el valor de la propiedad productID.
     * 
     */
    public int getProductID() {
        return productID;
    }

    /**
     * Define el valor de la propiedad productID.
     * 
     */
    public void setProductID(int value) {
        this.productID = value;
    }

    /**
     * Obtiene el valor de la propiedad version.
     * 
     * @return
     *     possible object is
     *     {@link VersionT }
     *     
     */
    public VersionT getVersion() {
        return version;
    }

    /**
     * Define el valor de la propiedad version.
     * 
     * @param value
     *     allowed object is
     *     {@link VersionT }
     *     
     */
    public void setVersion(VersionT value) {
        this.version = value;
    }

}
