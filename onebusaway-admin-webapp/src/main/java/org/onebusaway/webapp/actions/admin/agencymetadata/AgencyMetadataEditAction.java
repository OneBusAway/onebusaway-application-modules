/**
 * Copyright (C) 2016 Cambridge Systematics, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.onebusaway.webapp.actions.admin.agencymetadata;

import java.io.IOException;

import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.AllowedMethods;
import org.apache.struts2.convention.annotation.ParentPackage;
import org.apache.struts2.convention.annotation.Result;
import org.apache.struts2.convention.annotation.Results;
import org.json.JSONException;
import org.onebusaway.agency_metadata.model.AgencyMetadata;
import org.onebusaway.agency_metadata.service.AgencyMetadataService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.opensymphony.xwork2.ActionSupport;
import com.opensymphony.xwork2.ModelDriven;

@ParentPackage("onebusaway-admin-webapp-default")
@Results({
  @Result(type = "redirectAction", name = "deleteResult", params = {
      "actionName", "agency-metadata", "parse", "true"}),
  @Result(type = "redirectAction", name = "submitSuccess", params = {
      "actionName", "agency-metadata", "id", "${id}", "parse", "true"}),
  @Result(type = "redirectAction", name = "cancelResult", params = {
      "actionName", "agency-metadata", "id", "${id}", "parse", "true"})
  })
@AllowedMethods({"submit", "cancel", "deleteAgencyMetadata"})
public class AgencyMetadataEditAction  extends ActionSupport implements
    ModelDriven<AgencyMetadata> {
  private static final long serialVersionUID = 1L;
  private static Logger _log = LoggerFactory.getLogger(AgencyMetadataEditAction.class);

  @Autowired
  private AgencyMetadataService _agencyMetadataService;
  private AgencyMetadata _model;
  private String _agencyMetadataId;
  private boolean _newAgencyMetadata = false;

  @Override
  public AgencyMetadata getModel() {
    if (_model == null) {
      _model = new AgencyMetadata();
    }
    return _model;
  }

  public void setModel(AgencyMetadata model) {
    this._model = model;
  }

  public String getAgencyMetadataId() {
    return _agencyMetadataId;
  }

  public void setAgencyMetadataId(String agencyMetadataId) {
    _agencyMetadataId = agencyMetadataId;
  }

  public boolean isNewAgencyMetadata() {
    return _newAgencyMetadata;
  }

  public void setNewAgencyMetadata(boolean newAgencyMetadata) {
    _newAgencyMetadata = newAgencyMetadata;
  }

  @Override
  public String execute() {
    try {
      if (_agencyMetadataId != null && !_agencyMetadataId.trim().isEmpty())
        _model = _agencyMetadataService.getAgencyMetadataForId(String.valueOf(_agencyMetadataId)).get(0);
    } catch (RuntimeException e) {
      _log.error("Unable to retrieve Service Alert", e);
      throw e;
    }
    return "SUCCESS";
  }

  @Action("submit")
  public String submit() throws IOException, JSONException {

    try {
      if (_model.getId() == 0L ) {
        _agencyMetadataService.createAgencyMetadata(_model);
      }
      else {
        _agencyMetadataService.updateAgencyMetadata(_model);
      }
    } catch (RuntimeException e) {
      _log.error("Error creating or updating Service Alert", e);
      throw e;
    }
    return "submitSuccess";
  }

  @Action("cancel")
  public String cancel() {
    _log.info("Agency Metadata cleared");
    return "cancelResult"; 
  }

  public String deleteAgencyMetadata() {
    try {
      _agencyMetadataService.removeAgencyMetadata(_agencyMetadataId);
    } catch (RuntimeException e) {
      _log.error("Error deleting agency metadata", e);
      throw e;
    }
    return "deleteResult";
  }
}
