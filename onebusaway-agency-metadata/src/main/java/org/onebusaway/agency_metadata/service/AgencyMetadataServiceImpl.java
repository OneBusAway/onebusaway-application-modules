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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class AgencyMetadataServiceImpl implements AgencyMetadataService {

  protected static Logger _log = LoggerFactory.getLogger(AgencyMetadataServiceImpl.class);

  // Regexes for validating Well Known Text format for bounding box Polygon
  private static final String POINT = " *\\d+\\.\\d+ -\\d+\\.\\d+,";
  private static final String LASTPOINT = " *\\d+\\.\\d+ -\\d+\\.\\d+";    // No trailing comma
  private static final String POLYGON = "^POLYGON[ ?]\\(\\(" + POINT + POINT + POINT + POINT + LASTPOINT + "\\)\\)";

  @Autowired
  private AgencyMetadataDao _agencyMetadataDao;

  @Override
  public void createAgencyMetadata(String gtfsId, String name, String shortName, String legacyId, 
      String gtfsFeedUrl, String gtfsRtFeedUrl, String boundingBox, String ntdId,
      String agencyMessage) {

    // Verify that boundingBox is a valid Well Known Text Polygon format for an agency bounding box.
    if (!isValidBoundingBox(boundingBox)) {
      boundingBox = null;
    }

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
    model.setAgencyMessage(agencyMessage);
    
    _agencyMetadataDao.saveOrUpdate(model);
  }

  @Override
  public void createAgencyMetadata(AgencyMetadata model) {
    model.setId(0L);      // Create a new record with generated id.
    _agencyMetadataDao.saveOrUpdate(model);
  }

  @Override
  public void updateAgencyMetadata(long id, String gtfsId, String name, String shortName, String legacyId, 
      String gtfsFeedUrl, String gtfsRtFeedUrl, String boundingBox, String ntdId,
      String agencyMessage) {
  	AgencyMetadata model = getAgencyMetadataForId(String.valueOf(id)).get(0);
  	if (gtfsId != null) {
  		model.setGtfsId(gtfsId);
  	}
  	if (name != null) {
  		model.setName(name);
  	}
  	if (shortName != null) {
  	    model.setShortName(shortName);
  	}
  	if (legacyId != null) {
  	    model.setLegacyId(legacyId);
  	}
  	if (gtfsFeedUrl != null) {
  	    model.setGtfsFeedUrl(gtfsFeedUrl);
  	}
  	if (gtfsRtFeedUrl != null) {
  	    model.setGtfsRtFeedUrl(gtfsRtFeedUrl);
  	}
  	// Verify that boundingBox is a valid Well Known Text Polygon format for an agency bounding box.
  	if (!isValidBoundingBox(boundingBox)) {
  	  boundingBox = null;
  	}
  	if (boundingBox != null) {
  	    model.setBoundingBox(boundingBox);
  	}
  	if (ntdId != null) {
  	    model.setNtdId(ntdId);
  	}
  	if (agencyMessage != null) {
  	    model.setAgencyMessage(agencyMessage);
  	}

    _agencyMetadataDao.saveOrUpdate(model);
  }

  @Override
  public void updateAgencyMetadata(AgencyMetadata model) {
    _agencyMetadataDao.saveOrUpdate(model);
  }

  @Override
  public void delete(long id) {
    _agencyMetadataDao.delete(id);
  }

  @Override
  public void removeAgencyMetadata(String agencyMetadataId) {
    _agencyMetadataDao.removeAgencyMetadata(agencyMetadataId);
  }

  @Override
  public List<AgencyMetadata> getAllAgencyMetadata() {
     return _agencyMetadataDao.getAllAgencyMetadata();
  }

  @Override
  public List<AgencyMetadata> getAgencyMetadataForId(String id) {
    return _agencyMetadataDao.getAgencyMetadataForId(id);
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

  @Override
  public String getAgencyMetadataMessage(String id) {
    String message = "";
    List<AgencyMetadata> agencyMetadataList =  _agencyMetadataDao.getAgencyMetadataForId(id);
    if (agencyMetadataList != null && agencyMetadataList.size() > 0) {
      message = agencyMetadataList.get(0).getAgencyMessage();
    }
    return message;
  }

  /* Private functions */
  private boolean isValidBoundingBox(String wkt) { 
    _log.info("Validating: " + wkt);
    if (wkt == null || wkt.length() == 0) {
      return false;
    }
    if (!wkt.matches(POLYGON)) {
      _log.info("Validation of POLYGON against " + POLYGON + " failed");
      return false;
    }
    _log.info("Validation of POLYGON succeeded");
    return true;
  }

}
