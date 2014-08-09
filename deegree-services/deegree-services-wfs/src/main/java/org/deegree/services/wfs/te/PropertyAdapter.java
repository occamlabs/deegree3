package org.deegree.services.wfs.te;

import aero.m_click.wfs_te.model.Property;

public class PropertyAdapter implements Property {

    final org.deegree.commons.tom.gml.property.Property property;

    public PropertyAdapter( final org.deegree.commons.tom.gml.property.Property property ) {
        this.property = property;
    }

    @Override
    public Object getName() {
        return property.getName();
    }
}
