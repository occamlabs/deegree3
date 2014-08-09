package org.deegree.services.wfs.te;

import org.deegree.filter.Filter;

import aero.m_click.wfs_te.model.filter.StaticFilter;

public class StaticFilterAdapter extends AbstractAdapter<Filter> implements StaticFilter {

    public StaticFilterAdapter( final Filter adaptee ) {
        super( adaptee );
    }
}
