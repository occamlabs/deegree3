package org.deegree.services.wfs.te;

class AbstractAdapter<T> {

    protected final T adaptee;

    protected AbstractAdapter( final T adaptee ) {
        this.adaptee = getAdaptee();
    }

    public T getAdaptee() {
        return adaptee;
    }
}
