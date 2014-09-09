/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2014 by:
 - Department of Geography, University of Bonn -
 and
 - lat/lon GmbH -
 and
 - Occam Labs UG (haftungsbeschränkt) -

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

 lat/lon GmbH
 Aennchenstr. 19, 53177 Bonn
 Germany
 http://lat-lon.de/

 Department of Geography, University of Bonn
 Prof. Dr. Klaus Greve
 Postfach 1147, 53001 Bonn
 Germany
 http://www.geographie.uni-bonn.de/deegree/

 Occam Labs UG (haftungsbeschränkt)
 Godesberger Allee 139, 53175 Bonn
 Germany

 e-mail: info@deegree.org
 ----------------------------------------------------------------------------*/
package org.deegree.services.wms.controller.capabilities.theme;

import java.util.ArrayList;
import java.util.List;

import javax.xml.namespace.QName;

import org.deegree.commons.ows.metadata.DatasetMetadata;
import org.deegree.commons.tom.ows.CodeType;
import org.deegree.commons.tom.ows.LanguageString;
import org.deegree.commons.utils.Pair;
import org.deegree.commons.utils.StringPair;

/**
 * Merges {@link DatasetMetadata} instances.
 * 
 * @author <a href="mailto:schneider@occamlabs.de">Markus Schneider</a>
 * 
 * @since 3.3
 */
class DatasetMetadataMerger {

    /**
     * Merges two {@link DatasetMetadata} instances.
     * 
     * @param providerMetadata
     *            metadata from provider (takes precedence), can be <code>null</code>
     * @param layerMetadata
     *            metadata from layer, can be <code>null</code>
     * @return merged metadata, can be <code>null</code>
     */
    DatasetMetadata merge( final DatasetMetadata providerMetadata, final DatasetMetadata layerMetadata ) {
        if ( providerMetadata == null ) {
            return layerMetadata;
        } else if ( layerMetadata == null ) {
            return providerMetadata;
        }
        final QName name = layerMetadata.getQName();
        final List<LanguageString> titles = merge( providerMetadata.getTitles(), layerMetadata.getTitles() );
        final List<LanguageString> abstracts = merge( providerMetadata.getAbstracts(), layerMetadata.getAbstracts() );
        final List<Pair<List<LanguageString>, CodeType>> keywords = new ArrayList<Pair<List<LanguageString>, CodeType>>();
        if ( providerMetadata.getKeywords() != null ) {
            keywords.addAll( providerMetadata.getKeywords() );
        }
        if ( layerMetadata.getKeywords() != null ) {
            keywords.addAll( layerMetadata.getKeywords() );
        }
        final String url = providerMetadata.getUrl() != null ? providerMetadata.getUrl() : layerMetadata.getUrl();
        final List<StringPair> externalUrls = new ArrayList<StringPair>();
        if ( providerMetadata.getExternalUrls() != null ) {
            externalUrls.addAll( providerMetadata.getExternalUrls() );
        }
        if ( layerMetadata.getExternalUrls() != null ) {
            externalUrls.addAll( layerMetadata.getExternalUrls() );
        }
        return new DatasetMetadata( name, titles, abstracts, keywords, url, externalUrls );
    }

    private List<LanguageString> merge( final List<LanguageString> first, final List<LanguageString> second ) {
        final List<LanguageString> merged = new ArrayList<LanguageString>();
        if ( first != null ) {
            merged.addAll( first );
        }
        if ( second != null ) {
            merged.addAll( second );
        }
        return merged;
    }

}
