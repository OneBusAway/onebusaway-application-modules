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
import java.util.List;

import org.apache.struts2.convention.annotation.Result;
import org.apache.struts2.convention.annotation.Results;
import org.json.JSONException;
import org.onebusaway.agency_metadata.model.AgencyMetadata;
import org.onebusaway.agency_metadata.service.AgencyMetadataService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.opensymphony.xwork2.ActionSupport;

/**
 * Allows admin to create, update, and delete agency metadata.
 *
 */
@Results({@Result(type = "redirectAction", name = "redirect", params = {
    "actionName", "agency-metadata"})})
public class AgencyMetadataAction extends ActionSupport {
  private static final long serialVersionUID = 1L;
  private static Logger _log = LoggerFactory.getLogger(AgencyMetadataAction.class);

  @Autowired
  private AgencyMetadataService _agencyMetadataService;

  List<AgencyMetadata> _agencyMetadata;

  public List<AgencyMetadata> getAgencyMetadata() {
    return _agencyMetadata;
  }
  public void setAgencyMetadata(List<AgencyMetadata> agencyMetadata) {
    _agencyMetadata = agencyMetadata;
  }

  @Override
  public String execute() throws IOException, JSONException {
    try {
      _agencyMetadata = _agencyMetadataService.getAllAgencyMetadata();
    } catch (Exception ex) {
      _log.error("Exception getting agency metadata: " + ex.getMessage());
    }

    return SUCCESS;
  }
}
