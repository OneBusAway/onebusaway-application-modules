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

import org.onebusaway.agency_metadata.model.AgencyMetadata;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class AgencyMetadataServiceImpl implements AgencyMetadataService {

  @Autowired
  private AgencyMetadataDao _agencyMetadataDao;

  @Override
  public void createAgencyMetadata(String gtfsId, String name, String shortName, String legacyId, 
      String gtfsFeedUrl, String gtfsRtFeedUrl, String boundingBox, String ntdId) {
    AgencyMetadata model = new AgencyMetadata();
    model.setId(0L);      // Create a new record with generated id.
    model.setGtfsId(gtfsId);
    model.setName(name);
    model.setShortName(shortName);
    model.setLegacyId(legacyId);
    model.setGtfsFeedUrl(gtfsFeedUrl);
    model.setGtfsRtFeedUrl(gtfsRtFeedUrl);
    model.setBoundingBox(boundingBox);
    model.setNtdId(ntdId);
    
    _agencyMetadataDao.saveOrUpdate(model);
  }

  @Override
  public void updateAgencyMetadata(long id, String gtfsId, String name, String shortName, String legacyId, 
      String gtfsFeedUrl, String gtfsRtFeedUrl, String boundingBox, String ntdId) {
    AgencyMetadata model = new AgencyMetadata();
    model.setId(id);
    model.setGtfsId(gtfsId);
    model.setName(name);
    model.setShortName(shortName);
    model.setLegacyId(legacyId);
    model.setGtfsFeedUrl(gtfsFeedUrl);
    model.setGtfsRtFeedUrl(gtfsRtFeedUrl);
    model.setBoundingBox(boundingBox);
    model.setNtdId(ntdId);
    
    _agencyMetadataDao.saveOrUpdate(model);
  }
  
  @Override
  public void delete(long id) {
    _agencyMetadataDao.delete(id);
  }

  @Override
  public List<AgencyMetadata> getAllAgencyMetadata() {
     return _agencyMetadataDao.getAllAgencyMetadata();
  }

  @Override
  public List<AgencyMetadata> getAgencyMetadataForGtfsId(String gtfsId) {
    return _agencyMetadataDao.getAgencyMetadataForGtfsId(gtfsId);
  }

  @Override
  public List<AgencyMetadata> getAgencyMetadataForName(String name) {
    return _agencyMetadataDao.getAgencyMetadataForName(name);
  }

  @Override
  public List<AgencyMetadata> getAgencyMetadataForShortName(String shortName) {
    return _agencyMetadataDao.getAgencyMetadataForShortName(shortName);
  }

  @Override
  public List<AgencyMetadata> getAgencyMetadataForLegacyId(String legacyId) {
    return _agencyMetadataDao.getAgencyMetadataForLegacyId(legacyId);
  }

  @Override
  public List<AgencyMetadata> getAgencyMetadataForNtdId(String ntdId) {
    return _agencyMetadataDao.getAgencyMetadataForNtdId(ntdId);
  }

}
