//
// Este archivo ha sido generado por la arquitectura JavaTM para la implantación de la referencia de enlace (JAXB) XML v2.2.8-b130911.1802 
// Visite <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Todas las modificaciones realizadas en este archivo se perderán si se vuelve a compilar el esquema de origen. 
// Generado el: 2018.03.06 a las 10:35:15 AM CET 
//


package com.routeanalyzer.xml.tcx;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Clase Java para CadenceSensorType_t.
 * 
 * <p>El siguiente fragmento de esquema especifica el contenido que se espera que haya en esta clase.
 * <p>
 * <pre>
 * &lt;simpleType name="CadenceSensorType_t">
 *   &lt;restriction base="{http://www.garmin.com/xmlschemas/ActivityExtension/v2}Token_t">
 *     &lt;enumeration value="Footpod"/>
 *     &lt;enumeration value="Bike"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "CadenceSensorType_t")
@XmlEnum
public enum CadenceSensorTypeT {

    @XmlEnumValue("Footpod")
    FOOTPOD("Footpod"),
    @XmlEnumValue("Bike")
    BIKE("Bike");
    private final String value;

    CadenceSensorTypeT(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static CadenceSensorTypeT fromValue(String v) {
        for (CadenceSensorTypeT c: CadenceSensorTypeT.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
