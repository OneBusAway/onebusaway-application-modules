/**
 * Copyright (C) 2015 Cambridge Systematics, Inc.
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
package org.onebusaway.agency_metadata.service;

import org.onebusaway.agency_metadata.model.AgencyMetadataModel;

import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

public class AgencyMetadataServiceImpl implements AgencyMetadataService {

  @Autowired
  private AgencyMetadataDao _agencyMetadataDao;

  @Override
  public List<AgencyMetadataModel> getAllAgencyMetadata() {
    _agencyMetadataDao.getAllAgencyMetadata();
    return null;
  }

  @Override
  public List<AgencyMetadataModel> getAgencyMetadataForGtfsId(String gtfsId) {
    _agencyMetadataDao.getAgencyMetadataForGtfsId(gtfsId);
    return null;
  }

  @Override
  public List<AgencyMetadataModel> getAgencyMetadataForName(String name) {
    _agencyMetadataDao.getAgencyMetadataForName(name);
    return null;
  }

  @Override
  public List<AgencyMetadataModel> getAgencyMetadataForShortName(String shortName) {
    _agencyMetadataDao.getAgencyMetadataForShortName(shortName);
    return null;
  }

  @Override
  public List<AgencyMetadataModel> getAgencyMetadataForLegacyId(String legacyId) {
    _agencyMetadataDao.getAgencyMetadataForLegacyId(legacyId);
    return null;
  }

  @Override
  public List<AgencyMetadataModel> getAgencyMetadataForNtdId(String ntdId) {
    _agencyMetadataDao.getAgencyMetadataForNtdId(ntdId);
    return null;
  }

}
