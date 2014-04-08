package de.mclick.ows10;

import java.io.IOException;
import java.io.InputStream;

import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;

import org.apache.commons.io.IOUtils;
import org.deegree.commons.utils.io.StreamBufferStore;

class BufferedHttpServletRequest extends HttpServletRequestWrapper {

    private final StreamBufferStore store;

    BufferedHttpServletRequest( final HttpServletRequest request, final StreamBufferStore store ) throws IOException {
        super( request );
        this.store = store;
        InputStream is = request.getInputStream();
        IOUtils.copyLarge( is, store );
        IOUtils.closeQuietly( is );
        IOUtils.closeQuietly( store );
    }

    @Override
    public ServletInputStream getInputStream()
                            throws IOException {
        return new BufferedServletInputStream( store.getInputStream() );
    }

}
