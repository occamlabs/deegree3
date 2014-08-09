package org.deegree.services.wfs.te;

import org.deegree.time.primitive.TimeGeometricPrimitive;

import aero.m_click.wfs_te.model.TimePrimitive;

public class TimePrimitiveAdapter extends AbstractAdapter<TimeGeometricPrimitive> implements TimePrimitive {

    TimePrimitiveAdapter( final TimeGeometricPrimitive adaptee ) {
        super( adaptee );
    }

    @Override
    public boolean anyInteracts( TimePrimitive other ) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean laxDuring( TimePrimitive other ) {
        // TODO Auto-generated method stub
        return false;
    }

}
