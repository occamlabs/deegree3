/*----------------------------------------------------------------------------
 This file is part of deegree
 Copyright (C) 2001-2014 by:
 - Department of Geography, University of Bonn -
 and
 - lat/lon GmbH -
 and
 - Occam Labs UG (haftungsbeschränkt) -
 and others

 This library is free software; you can redistribute it and/or modify it under
 the terms of the GNU Lesser General Public License as published by the Free
 Software Foundation; either version 2.1 of the License, or (at your option)
 any later version.
 This library is distributed in the hope that it will be useful, but WITHOUT
 ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 details.
 You should have received a copy of the GNU Lesser General Public License
 along with this library; if not, write to the Free Software Foundation, Inc.,
 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA

 Contact information:

 e-mail: info@deegree.org
 website: http://www.deegree.org/
----------------------------------------------------------------------------*/
package org.deegree.commons.gdal.pool;

import java.io.Closeable;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * A "keyed" resource pool implementation that pools {@link KeyedResource} object instances and guarantees a maximum
 * limit of open resources.
 * 
 * @author <a href="mailto:schneider@occamlabs.de">Markus Schneider</a>
 * 
 * @since 3.4
 */
public class LimitedKeyedResourcePool<T extends KeyedResource> implements Closeable {

    private final KeyedResourceFactory<T> factory;

    private final int maxResources;

    private final Map<String, BlockingQueue<T>> keyToIdleQueue = new HashMap<String, BlockingQueue<T>>();

    private final BlockingQueue<T> returnQueue = new LinkedBlockingQueue<T>();

    private final Lock checkForAvailableResources = new ReentrantLock();

    private int activeResources;

    /**
     * Creates a new {@link LimitedKeyedResourcePool} instance.
     * 
     * @param factory
     *            factory for creating new KeyedResource instances, must not be <code>null</code>
     * @param maxResources
     *            maximum number of open KeyedResource instances
     */
    public LimitedKeyedResourcePool( final KeyedResourceFactory<T> factory, final int maxResources ) {
        this.factory = factory;
        this.maxResources = maxResources;
    }

    public T borrow( final String key )
                            throws InterruptedException, IOException {

        checkForAvailableResources.lock();
        copyReturnQueueToIdleQueues();
        T resource = checkForIdleResource( key );
        checkForAvailableResources.unlock();

        if ( resource == null ) {
            checkForAvailableResources.lock();
            resource = ensureSlotForResource( key );
            checkForAvailableResources.unlock();
            if ( resource == null ) {
                resource = factory.create( key );
            }
            if ( resource != null && !resource.getKey().equals( key ) ) {
                resource.close();
                resource = factory.create( key );
            }
        }
        return resource;
    }

    public void returnObject( final T resource ) {
        returnQueue.add( resource );
    }

    @Override
    public void close() {

    }

    private void copyReturnQueueToIdleQueues() {
        T resource = null;
        while ( ( resource = returnQueue.poll() ) != null ) {
            final String key = resource.getKey();
            final BlockingQueue<T> queue = getQueue( key );
            queue.add( resource );
        }
    }

    private T checkForIdleResource( final String key ) {
        BlockingQueue<T> queue = getQueue( key );
        return queue.poll();
    }

    private T ensureSlotForResource( String key )
                            throws InterruptedException {
        activeResources++;
        T resource = null;
        while ( activeResources > maxResources ) {
            resource = takeNextCloseableResource();
            activeResources--;
        }
        return resource;
    }

    private T takeNextCloseableResource()
                            throws InterruptedException {
        for ( final BlockingQueue<T> queue : keyToIdleQueue.values() ) {
            final T resource = queue.poll();
            if ( resource != null ) {
                return resource;
            }
        }
        return returnQueue.take();
    }

    private synchronized BlockingQueue<T> getQueue( final String key ) {
        BlockingQueue<T> queue = keyToIdleQueue.get( key );
        if ( queue == null ) {
            queue = new LinkedBlockingQueue<T>( maxResources );
            keyToIdleQueue.put( key, queue );
        }
        return queue;
    }

}
