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

import java.util.List;

public interface AgencyMetadataService {
  
  void createAgencyMetadata(String gtfs_id, String name, String short_name, String legacy_id, 
      String gtfs_feed_url, String gtfs_rt_feed_url, String bounding_box, String ntd_id,
      String agency_message);

  void createAgencyMetadata(AgencyMetadata model);
  
  void updateAgencyMetadata(long id, String gtfs_id, String name, String short_name, String legacy_id, 
      String gtfs_feed_url, String gtfs_rt_feed_url, String bounding_box, String ntd_id,
      String agency_message);
  
  void updateAgencyMetadata(AgencyMetadata model);

  void delete(long id);

  void removeAgencyMetadata(String agencyMetadataId);

  List<AgencyMetadata> getAllAgencyMetadata();

  List<AgencyMetadata> getAgencyMetadataForId(String id);
  
  List<AgencyMetadata> getAgencyMetadataForGtfsId(String gtfsId);

  List<AgencyMetadata> getAgencyMetadataForName(String name);

  List<AgencyMetadata> getAgencyMetadataForShortName(String shortName);

  List<AgencyMetadata> getAgencyMetadataForLegacyId(String legacyId);

  List<AgencyMetadata> getAgencyMetadataForNtdId(String ntdId);

  String getAgencyMetadataMessage(String id);

}
