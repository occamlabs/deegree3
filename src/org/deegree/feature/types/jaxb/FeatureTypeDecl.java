//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, vhudson-jaxb-ri-2.1-792 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2009.10.06 at 05:10:14 PM MESZ 
//


package org.deegree.feature.types.jaxb;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlType;
import javax.xml.namespace.QName;


/**
 * Declaration of a feature type of the schema.
 * 
 * <p>Java class for anonymous complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType>
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element ref="{http://www.deegree.org/feature/featuretype}AbstractProperty" maxOccurs="unbounded" minOccurs="0"/>
 *       &lt;/sequence>
 *       &lt;attribute name="name" type="{http://www.w3.org/2001/XMLSchema}QName" />
 *       &lt;attribute name="abstract" type="{http://www.w3.org/2001/XMLSchema}boolean" />
 *       &lt;attribute name="substitutionGroup" type="{http://www.w3.org/2001/XMLSchema}QName" />
 *       &lt;attribute name="javaClass" type="{http://www.w3.org/2001/XMLSchema}string" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "abstractProperty"
})
public class FeatureTypeDecl {

    @XmlElementRef(name = "AbstractProperty", namespace = "http://www.deegree.org/feature/featuretype", type = JAXBElement.class)
    protected List<JAXBElement<? extends AbstractPropertyDecl>> abstractProperty;
    @XmlAttribute
    protected QName name;
    @XmlAttribute(name = "abstract")
    protected Boolean _abstract;
    @XmlAttribute
    protected QName substitutionGroup;
    @XmlAttribute
    protected String javaClass;

    /**
     * Gets the value of the abstractProperty property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the abstractProperty property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getAbstractProperty().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link JAXBElement }{@code <}{@link GeometryPropertyDecl }{@code >}
     * {@link JAXBElement }{@code <}{@link AbstractPropertyDecl }{@code >}
     * {@link JAXBElement }{@code <}{@link FeaturePropertyDecl }{@code >}
     * {@link CustomComplexPropertyDecl }
     * {@link JAXBElement }{@code <}{@link SimplePropertyDecl }{@code >}
     * 
     * 
     */
    public List<JAXBElement<? extends AbstractPropertyDecl>> getAbstractProperty() {
        if (abstractProperty == null) {
            abstractProperty = new ArrayList<JAXBElement<? extends AbstractPropertyDecl>>();
        }
        return this.abstractProperty;
    }

    /**
     * Gets the value of the name property.
     * 
     * @return
     *     possible object is
     *     {@link QName }
     *     
     */
    public QName getName() {
        return name;
    }

    /**
     * Sets the value of the name property.
     * 
     * @param value
     *     allowed object is
     *     {@link QName }
     *     
     */
    public void setName(QName value) {
        this.name = value;
    }

    /**
     * Gets the value of the abstract property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public Boolean isAbstract() {
        return _abstract;
    }

    /**
     * Sets the value of the abstract property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setAbstract(Boolean value) {
        this._abstract = value;
    }

    /**
     * Gets the value of the substitutionGroup property.
     * 
     * @return
     *     possible object is
     *     {@link QName }
     *     
     */
    public QName getSubstitutionGroup() {
        return substitutionGroup;
    }

    /**
     * Sets the value of the substitutionGroup property.
     * 
     * @param value
     *     allowed object is
     *     {@link QName }
     *     
     */
    public void setSubstitutionGroup(QName value) {
        this.substitutionGroup = value;
    }

    /**
     * Gets the value of the javaClass property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getJavaClass() {
        return javaClass;
    }

    /**
     * Sets the value of the javaClass property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setJavaClass(String value) {
        this.javaClass = value;
    }

}
