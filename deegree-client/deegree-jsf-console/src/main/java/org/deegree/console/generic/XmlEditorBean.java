/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2013 by:
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
package org.deegree.console.generic;

import static javax.faces.application.FacesMessage.SEVERITY_ERROR;
import static org.deegree.commons.config.ResourceState.StateType.deactivated;
import static org.deegree.commons.xml.XMLAdapter.DEFAULT_URL;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Serializable;
import java.io.StringReader;
import java.net.URL;

import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.deegree.commons.config.DeegreeWorkspace;
import org.deegree.commons.config.Resource;
import org.deegree.commons.config.ResourceManager;
import org.deegree.commons.config.ResourceState;
import org.deegree.commons.xml.XMLAdapter;
import org.deegree.console.workspace.WorkspaceBean;
import org.deegree.services.controller.OGCFrontController;

@ManagedBean
@ViewScoped
public class XmlEditorBean implements Serializable {

    private static final long serialVersionUID = -2345424266499294734L;

    private String id;

    private String fileName;

    private String schemaUrl;

    private String resourceManagerClass;

    private String nextView;

    private String content;

    private ResourceManager resourceManager;

    private String schemaAsText;

    public String getFileName() {
        return fileName;
    }

    public void setFileName( String fileName ) {
        this.fileName = fileName;
    }

    public String getId() {
        return id;
    }

    public void setId( String id ) {
        this.id = id;
    }

    public String getSchemaUrl() {
        return schemaUrl;
    }

    public void setSchemaUrl( String schemaUrl ) {
        this.schemaUrl = schemaUrl;
        if ( schemaUrl != null ) {
            try {
                schemaAsText = IOUtils.toString( new URL( schemaUrl ).openStream(), "UTF-8" );
            } catch ( IOException e ) {
                e.printStackTrace();
            }
        }
    }

    public String getNextView() {
        return nextView;
    }

    public void setNextView( String nextView ) {
        this.nextView = nextView;
    }

    public String getResourceManagerClass() {
        return resourceManagerClass;
    }

    public void setResourceManagerClass( String resourceManagerClass )
                            throws ClassNotFoundException {
        this.resourceManagerClass = resourceManagerClass;
        if ( resourceManagerClass != null && !resourceManagerClass.isEmpty() ) {
            DeegreeWorkspace ws = OGCFrontController.getServiceWorkspace();
            @SuppressWarnings("unchecked")
            Class<? extends ResourceManager> cl = (Class<? extends ResourceManager>) ws.getModuleClassLoader().loadClass( resourceManagerClass );
            this.resourceManager = (ResourceManager) ws.getSubsystemManager( cl );
        }
    }

    public String getContent()
                            throws IOException {
        if ( content == null ) {
            content = FileUtils.readFileToString( new File( fileName ) );
        }
        return content;
    }

    public void setContent( String content ) {
        this.content = content;
    }

    public String getSchemaAsText() {
        return schemaAsText;
    }

    public void setSchemaAsText( String schemaAsText ) {
        this.schemaAsText = schemaAsText;
    }

    public String cancel() {
        return nextView;
    }

    public String save() {
        try {
            XMLAdapter adapter = new XMLAdapter( new StringReader( content ), DEFAULT_URL );
            OutputStream os = new FileOutputStream( fileName );
            adapter.getRootElement().serialize( os );
            os.close();
        } catch ( Exception e ) {
            String msg = "Error saving XML configuration file: " + e.getMessage();
            FacesMessage fm = new FacesMessage( SEVERITY_ERROR, msg, null );
            FacesContext.getCurrentInstance().addMessage( null, fm );
            return nextView;
        }
        if ( resourceManager != null ) {
            activate();
        }
        return nextView;
    }

    private void activate() {
        ResourceState<? extends Resource> state = resourceManager.getState( id );

        try {
            if ( state.getType() == deactivated ) {
                resourceManager.activate( id );
            } else {
                resourceManager.deactivate( id );
                resourceManager.activate( id );
            }
        } catch ( Exception e ) {
            e.printStackTrace();
            String msg = "Error applying resource configuration changes: " + e.getMessage();
            state = resourceManager.getState( id );
            if ( state != null && state.getLastException() != null ) {
                msg = state.getLastException().getMessage();
            }
            FacesMessage fm = new FacesMessage( SEVERITY_ERROR, msg, null );
            FacesContext.getCurrentInstance().addMessage( null, fm );
            return;
        }
        state = resourceManager.getState( id );
        if ( state != null && state.getLastException() != null ) {
            String msg = state.getLastException().getMessage();
            FacesMessage fm = new FacesMessage( SEVERITY_ERROR, msg, null );
            FacesContext.getCurrentInstance().addMessage( null, fm );
        }

        ExternalContext ctx = FacesContext.getCurrentInstance().getExternalContext();
        WorkspaceBean ws = (WorkspaceBean) ctx.getApplicationMap().get( "workspace" );
        ws.setModified();
    }
}
