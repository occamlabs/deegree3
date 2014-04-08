package de.mclick.ows10;

import java.io.IOException;

import javax.servlet.ServletOutputStream;

import org.deegree.commons.utils.io.StreamBufferStore;

class BufferingServletOutputStream extends ServletOutputStream {

    private final ServletOutputStream parent;

    private final StreamBufferStore store;

    BufferingServletOutputStream( final ServletOutputStream parent, final StreamBufferStore store ) {
        this.parent = parent;
        this.store = store;
    }

    @Override
    public void write( int b )
                            throws IOException {
        store.write( b );
        parent.write( b );
    }

    @Override
    public void write( final byte[] b )
                            throws IOException {
        store.write( b );
        parent.write( b, 0, b.length );
    }

    @Override
    public void write( final byte[] b, final int off, final int len )
                            throws IOException {
        store.write( b, off, len );
        parent.write( b, off, len );
    }

}
