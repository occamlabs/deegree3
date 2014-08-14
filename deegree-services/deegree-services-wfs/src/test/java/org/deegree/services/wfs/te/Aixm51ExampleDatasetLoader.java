package org.deegree.services.wfs.te;

import static org.deegree.gml.GMLInputFactory.createGMLStreamReader;
import static org.deegree.gml.GMLVersion.GML_32;

import java.net.URL;

import org.deegree.feature.Feature;
import org.deegree.feature.types.AppSchema;
import org.deegree.gml.GMLStreamReader;
import org.deegree.gml.schema.GMLAppSchemaReader;

class Aixm51ExampleDatasetLoader {

    private static final String AIXM_FEATURES_XSD = "aixm/schema/AIXM_Features.xsd";

    Feature load()
                            throws Exception {
        final URL datasetUrl = DeegreeDynamicFeatureQueryStrategyTest.class.getResource( "aixm/aixm.xml" );
        final GMLStreamReader gmlReader = createGMLStreamReader( GML_32, datasetUrl );
        final AppSchema schema = loadAixm51AppSchema();
        gmlReader.setApplicationSchema( schema );
        return gmlReader.readFeature();
    }

    private AppSchema loadAixm51AppSchema()
                            throws Exception {
        final String schemaUrl = "" + Aixm51ExampleDatasetLoader.class.getResource( AIXM_FEATURES_XSD );
        final GMLAppSchemaReader schemaReader = new GMLAppSchemaReader( GML_32, null, schemaUrl );
        return schemaReader.extractAppSchema();
    }

}
