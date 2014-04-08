package de.mclick.ows10;

import java.io.IOException;
import java.io.InputStream;

import javax.servlet.ServletInputStream;

class BufferedServletInputStream extends ServletInputStream {

    private final InputStream parent;

    BufferedServletInputStream( final InputStream inputStream ) {
        this.parent = inputStream;
    }

    @Override
    public int read()
                            throws IOException {
        int b = parent.read();
        return b;
    }

    @Override
    public int read( final byte[] b )
                            throws IOException {
        int numBytes = parent.read( b );
        return numBytes;
    }

    @Override
    public int read( final byte[] b, final int off, final int len )
                            throws IOException {
        int numBytes = parent.read( b, off, len );
        return numBytes;
    }
}
