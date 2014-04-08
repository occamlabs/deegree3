package de.mclick.ows10;

import java.io.IOException;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;

import org.deegree.commons.utils.io.StreamBufferStore;

class BufferingHttpServletResponse extends HttpServletResponseWrapper {

    private final HttpServletResponse parent;

    private final StreamBufferStore store;

    BufferingHttpServletResponse( final HttpServletResponse parent, final StreamBufferStore store ) {
        super( parent );
        this.parent = parent;
        this.store = store;
    }

    @Override
    public ServletOutputStream getOutputStream()
                            throws IOException {
        return new BufferingServletOutputStream( parent.getOutputStream(), store );
    }

}
