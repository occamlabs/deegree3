//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, vhudson-jaxb-ri-2.1-792 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2010.03.09 at 01:38:37 PM MEZ 
//


package org.deegree.feature.types.jaxb;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;
import javax.xml.namespace.QName;

public class CustomComplexPropertyDecl
    extends JAXBElement<CustomComplexPropertyDecl.Type>
{

    protected final static QName NAME = new QName("http://www.deegree.org/feature/featuretype", "CustomComplexProperty");

    public CustomComplexPropertyDecl(CustomComplexPropertyDecl.Type value) {
        super(NAME, ((Class) CustomComplexPropertyDecl.Type.class), null, value);
    }

    public CustomComplexPropertyDecl() {
        super(NAME, ((Class) CustomComplexPropertyDecl.Type.class), null, null);
    }


    /**
     * <p>Java class for anonymous complex type.
     * 
     * <p>The following schema fragment specifies the expected content contained within this class.
     * 
     * <pre>
     * &lt;complexType>
     *   &lt;complexContent>
     *     &lt;extension base="{http://www.deegree.org/feature/featuretype}AbstractPropertyType">
     *     &lt;/extension>
     *   &lt;/complexContent>
     * &lt;/complexType>
     * </pre>
     * 
     * 
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "")
    public static class Type
        extends AbstractPropertyDecl
    {


    }

}
