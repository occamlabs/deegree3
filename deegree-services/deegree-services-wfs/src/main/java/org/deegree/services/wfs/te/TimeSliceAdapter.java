package org.deegree.services.wfs.te;

import aero.m_click.wfs_te.model.Interpretation;
import aero.m_click.wfs_te.model.Property;
import aero.m_click.wfs_te.model.TimePrimitive;
import aero.m_click.wfs_te.model.TimeSlice;

public class TimeSliceAdapter extends AbstractAdapter<org.deegree.commons.tom.gml.property.Property> implements
                                                                                                    TimeSlice {

    public TimeSliceAdapter( final org.deegree.commons.tom.gml.property.Property adaptee ) {
        super( adaptee );
    }

    @Override
    public TimePrimitive getValidTime() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Interpretation getInterpretation() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Integer getSequenceNumber() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Integer getCorrectionNumber() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Iterable<Property> getNonSpecialProperties() {
        // TODO Auto-generated method stub
        return null;
    }
}
