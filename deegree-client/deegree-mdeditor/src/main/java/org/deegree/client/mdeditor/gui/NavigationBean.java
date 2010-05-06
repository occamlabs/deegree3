//$HeadURL: svn+ssh://lbuesching@svn.wald.intevation.de/deegree/base/trunk/resources/eclipse/files_template.xml $
/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2010 by:
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
package org.deegree.client.mdeditor.gui;

import java.io.Serializable;
import java.util.UUID;

import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.RequestScoped;
import javax.faces.context.FacesContext;
import javax.servlet.http.HttpSession;

import org.deegree.client.mdeditor.config.ConfigurationException;
import org.deegree.client.mdeditor.config.FormConfigurationFactory;
import org.deegree.client.mdeditor.controller.DatasetWriter;
import org.deegree.client.mdeditor.model.FormConfiguration;
import org.deegree.client.mdeditor.model.FormFieldPath;

/**
 * TODO add class documentation here
 * 
 * @author <a href="mailto:buesching@lat-lon.de">Lyn Buesching</a>
 * @author last edited by: $Author: lyn $
 * 
 * @version $Revision: $, $Date: $
 */
@ManagedBean
@RequestScoped
public class NavigationBean implements Serializable {

    private static final long serialVersionUID = 9025028665690108601L;

    public Object saveDataset() {
        String id = UUID.randomUUID().toString();
        FacesContext fc = FacesContext.getCurrentInstance();
        fc.getELContext();
        FormFieldBean formfields = (FormFieldBean) fc.getApplication().getELResolver().getValue( fc.getELContext(),
                                                                                                 null, "formFieldBean" );

        HttpSession session = (HttpSession) fc.getExternalContext().getSession( false );
        try {
            FormConfiguration manager = FormConfigurationFactory.getOrCreateFormConfiguration( session.getId() );

            FormFieldPath pathToIdentifier = manager.getPathToIdentifier();
            Object value = formfields.getFormFields().get( pathToIdentifier.toString() ).getValue();
            id = String.valueOf( value );

            DatasetWriter.writeElements( id, formfields.getFormGroups() );

        } catch ( Exception e ) {
            FacesMessage msg = GuiUtils.getFacesMessage( fc, FacesMessage.SEVERITY_FATAL, "ERROR.SAVE_DATASET",
                                                         e.getMessage() );
            fc.addMessage( "SAVE_FAILED", msg );
        }

        FacesMessage msg = GuiUtils.getFacesMessage( fc, FacesMessage.SEVERITY_INFO, "SUCCESS.SAVE_DATASET", id );
        fc.addMessage( "SAVE_SUCCESS", msg );

        return "/page/form/successPage.xhtml";
    }

    public Object reloadForm() {
        FacesContext fc = FacesContext.getCurrentInstance();
        HttpSession session = (HttpSession) fc.getExternalContext().getSession( false );
        try {
            FormConfigurationFactory.reloadFormConfiguration( session.getId() );
        } catch ( ConfigurationException e ) {
            FacesMessage msg = GuiUtils.getFacesMessage( fc, FacesMessage.SEVERITY_FATAL, "ERROR.CONF.RELOAD",
                                                         e.getMessage() );
            fc.addMessage( "RELOAD_FAILED", msg );
            return "/page/form/errorPage.xhtml";
        }

        fc.getELContext();
        FormCreatorBean formCreator = (FormCreatorBean) fc.getApplication().getELResolver().getValue(
                                                                                                      fc.getELContext(),
                                                                                                      null,
                                                                                                      "formCreatorBean" );
        formCreator.forceReloaded();

        MenuCreatorBean menuCreator = (MenuCreatorBean) fc.getApplication().getELResolver().getValue(
                                                                                                      fc.getELContext(),
                                                                                                      null,
                                                                                                      "menuCreatorBean" );
        menuCreator.forceReloaded();

        FormFieldBean ff = (FormFieldBean) fc.getApplication().getELResolver().getValue( fc.getELContext(), null,
                                                                                         "formFieldBean" );
        ff.forceReloaded();

        FacesMessage msg = GuiUtils.getFacesMessage( fc, FacesMessage.SEVERITY_INFO, "SUCCESS.RELOAD" );
        fc.addMessage( "RELOAD_SUCCESS", msg );
        return "/page/form/successPage.xhtml";
    }

    public Object loadDataset() {
        return null;
    }

}
