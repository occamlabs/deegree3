//$HeadURL$
/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2011 by:
 - Department of Geography, University of Bonn -
 and
 - lat/lon GmbH -

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

 e-mail: info@deegree.org
 ----------------------------------------------------------------------------*/
package org.deegree.gml.schema;

import java.util.Set;

import javax.xml.namespace.QName;

import org.deegree.feature.types.AppSchema;
import org.deegree.feature.types.FeatureType;
import org.junit.Before;
import org.junit.Test;

/**
 * TODO add class documentation here
 * 
 * @author <a href="mailto:name@company.com">Your Name</a>
 * @author last edited by: $Author: schneider $
 * 
 * @version $Revision: $, $Date: $
 */
public class GmlNamespaceExtractorTest {

    private GmlNamespaceExtractor extractor;

    @Test
    public void testGetNamespacesInspireAddress()
                            throws ClassCastException, ClassNotFoundException, InstantiationException,
                            IllegalAccessException {
       
        String schemaURL = this.getClass().getResource( "../inspire/schema/Addresses.xsd" ).toString();
        GMLAppSchemaReader adapter = new GMLAppSchemaReader( null, null, schemaURL );
        AppSchema schema = adapter.extractAppSchema();
        FeatureType ft = schema.getFeatureType( QName.valueOf( "{urn:x-inspire:specification:gmlas:Addresses:3.0}Address" ) );
        
        GmlNamespaceExtractor extractor = new GmlNamespaceExtractor( schema );        
        Set<String> namespaces = extractor.getNamespaces( ft );
        for ( String ns : namespaces ) {
            System.out.println( ns );
        }
    }
}
