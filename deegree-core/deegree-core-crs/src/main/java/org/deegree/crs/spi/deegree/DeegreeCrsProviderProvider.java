/*----------------------------------------------------------------------------
 This file is part of deegree
 Copyright (C) 2001-2013 by:
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
package org.deegree.crs.spi.deegree;

import java.net.URL;

import org.deegree.crs.CrsProvider;
import org.deegree.crs.CrsProviderProvider;
import org.deegree.workspace.ResourceLocation;
import org.deegree.workspace.ResourceMetadata;
import org.deegree.workspace.Workspace;

/**
 * SPI implementation for default deegree coordinate system provider.
 * 
 * @author <a href="mailto:schmitz@occamlabs.de">Andreas Schmitz</a>
 * 
 * @since 3.5
 */
public class DeegreeCrsProviderProvider extends CrsProviderProvider {

    private static final String CONFIG_NS = "http://www.deegree.org/crs/stores/deegree";

    private static final URL CONFIG_SCHEMA = CrsProviderProvider.class.getResource( "/META-INF/schemas/crs/deegree/3.4.0/deegree.xsd" );

    @Override
    public String getNamespace() {
        return CONFIG_NS;
    }

    @Override
    public ResourceMetadata<CrsProvider> createFromLocation( Workspace workspace, ResourceLocation<CrsProvider> location ) {
        return new DeegreeCrsProviderMetadata( workspace, location, this );
    }

    @Override
    public URL getSchema() {
        return CONFIG_SCHEMA;
    }

}
