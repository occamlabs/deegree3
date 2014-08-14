package org.deegree.services.wfs.te;

import static org.deegree.gml.GMLInputFactory.createGMLStreamReader;
import static org.deegree.gml.GMLVersion.GML_32;

import java.net.URL;

import org.deegree.feature.FeatureCollection;
import org.deegree.feature.types.AppSchema;
import org.deegree.gml.GMLStreamReader;
import org.deegree.gml.schema.GMLAppSchemaReader;

class Aixm51ExampleDatasetLoader {

    private static final String EVENT_XSD = "aixm/schema/event/Event_Features.xsd";

    private static final String BASIC_MESSAGE_XSD = "aixm/schema/message/AIXM_BasicMessage.xsd";

    FeatureCollection load()
                            throws Exception {
        final URL datasetUrl = DeegreeDynamicFeatureQueryStrategyTest.class.getResource( "aixm/aixm.xml" );
        final GMLStreamReader gmlReader = createGMLStreamReader( GML_32, datasetUrl );
        final AppSchema schema = loadAixm51AppSchema();
        gmlReader.setApplicationSchema( schema );
        return gmlReader.readFeatureCollection();
    }

    private AppSchema loadAixm51AppSchema()
                            throws Exception {
        final String basicMessageSchemaUrl = "" + Aixm51ExampleDatasetLoader.class.getResource( BASIC_MESSAGE_XSD );
        final String eventSchemaUrl = "" + Aixm51ExampleDatasetLoader.class.getResource( EVENT_XSD );
        final String[] schemaUrls = new String[] { basicMessageSchemaUrl, eventSchemaUrl };
        final GMLAppSchemaReader schemaReader = new GMLAppSchemaReader( GML_32, null, schemaUrls );
        return schemaReader.extractAppSchema();
    }

}
