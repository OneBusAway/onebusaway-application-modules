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
package org.onebusaway.agency_metadata.model;

import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Index;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name="agency_metadata")
@org.hibernate.annotations.Table(appliesTo ="agency_metadata", indexes = {
    @Index(name = "agency_id_idx", columnNames = {"id"}),
    })
@org.hibernate.annotations.Entity(mutable = true)
  
public class AgencyMetadata {
  
  /* Sound Transit constants */
  private static final int GTFS_ID_LENGTH = 35;
  private static final int NAME_LENGTH = 40;
  private static final int SHORT_NAME_LENGTH = 10;
  private static final int LEGACY_ID_LENGTH = 35;
  private static final int GTFS_FEED_URL_LENGTH = 300;
  private static final int GTFS_RT_FEED_URL_LENGTH = 300;
  private static final int BOUNDING_BOX_LENGTH = 300;
  private static final int NTD_ID_LENGTH = 5;
  private static final int AGENCY_MESSAGE = 300;
  
  
  
  @Id
  @GeneratedValue(generator = "increment")
  @GenericGenerator(name = "increment", strategy = "increment")
  private long id;
  @Column(nullable = true, name="gtfs_id", length = GTFS_ID_LENGTH)
  private String gtfsId;
  @Column(nullable = true, name="name", length = NAME_LENGTH)
  private String name;
  @Column(nullable = true, name="short_name", length = SHORT_NAME_LENGTH)
  private String shortName;
  @Column(nullable = true, name="legacy_id", length = LEGACY_ID_LENGTH)
  private String legacyId;
  @Column(nullable = true, name="gtfs_feed_url", length = GTFS_FEED_URL_LENGTH)
  private String gtfsFeedUrl;
  @Column(nullable = true, name="gtfs_rt_feed_url", length = GTFS_RT_FEED_URL_LENGTH)
  private String gtfsRtFeedUrl;
  @Column(nullable = true, name="bounding_box", length = BOUNDING_BOX_LENGTH)
  private String boundingBox;
  @Column(nullable = true, name="ntd_id", length = NTD_ID_LENGTH)
  private String ntdId;
  @Column(nullable = true, name="agency_message", length = AGENCY_MESSAGE)
  private String agencyMessage;
  
  public long getId() {
    return id;
  }

  public void setId(long id) {
    this.id = id;
  }

  public String getGtfsId() {
    return gtfsId;
  }

  public void setGtfsId(String gtfsId) {
    this.gtfsId = gtfsId;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getShortName() {
    return shortName;
  }

  public void setShortName(String shortName) {
    this.shortName = shortName;
  }

  public String getLegacyId() {
    return legacyId;
  }

  public void setLegacyId(String legacyId) {
    this.legacyId = legacyId;
  }

  public String getGtfsFeedUrl() {
    return gtfsFeedUrl;
  }

  public void setGtfsFeedUrl(String gtfsFeedUrl) {
    this.gtfsFeedUrl = gtfsFeedUrl;
  }

  public String getGtfsRtFeedUrl() {
    return gtfsRtFeedUrl;
  }

  public void setGtfsRtFeedUrl(String gtfsRtFeedUrl) {
    this.gtfsRtFeedUrl = gtfsRtFeedUrl;
  }

  public String getBoundingBox() {
    return boundingBox;
  }

  public void setBoundingBox(String boundingBox) {
    this.boundingBox = boundingBox;
  }

  public String getNtdId() {
    return ntdId;
  }

  public void setNtdId(String ntdId) {
    this.ntdId = ntdId;
  }
  
  public String getAgencyMessage() {
    return agencyMessage;
  }

  public void setAgencyMessage(String agencyMessage) {
		this.agencyMessage = agencyMessage;
	}

	public String toString() {
    return "{AgencyMetadata={id:" + id + ",gtfsId:" + getGtfsId() + ",name:" + getName() + ",shortName:" 
        + getShortName() + ",legacyId:" + getLegacyId() + ",gtfsFeedUrl:" + getGtfsFeedUrl()
        + ",gtfsRtFeedUrl:" + getGtfsRtFeedUrl()+ ",ntdId:" + getNtdId() + "}}";
  }

}
