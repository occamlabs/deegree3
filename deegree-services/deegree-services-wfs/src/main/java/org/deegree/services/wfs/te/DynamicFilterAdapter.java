package org.deegree.services.wfs.te;

import static java.lang.Boolean.TRUE;

import org.deegree.filter.te.DynamicFeatureFilter;

import aero.m_click.wfs_te.model.SnapshotGeneration;
import aero.m_click.wfs_te.model.TimePrimitive;
import aero.m_click.wfs_te.model.filter.DynamicFilter;
import aero.m_click.wfs_te.model.filter.StaticFilter;

public class DynamicFilterAdapter extends AbstractAdapter<DynamicFeatureFilter> implements DynamicFilter {

    public DynamicFilterAdapter( final DynamicFeatureFilter adaptee ) {
        super( adaptee );
    }

    @Override
    public SnapshotGeneration getSnapshotParams() {
        return new SnapshotGeneration() {
            @Override
            public TimePrimitive getSnapshotTime() {
                if ( adaptee.getTimeIndicator() == null ) {
                    return null;
                }
                return new TimePrimitiveAdapter( adaptee.getTimeIndicator() );
            }

            @Override
            public boolean getEvaluateSchedules() {
                return TRUE.equals( adaptee.getEvaluateSchedules() );
            }
        };
    }

    @Override
    public StaticFilter getStaticFilter() {
        return new StaticFilterAdapter( adaptee.getFeatureFilter() );
    }
}
