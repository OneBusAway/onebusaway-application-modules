/**
 * Copyright (C) 2011 Brian Ferris <bdferris@onebusaway.org>
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
package org.onebusaway.transit_data_federation.bundle.model;

import java.io.File;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.onebusaway.transit_data_federation.bundle.FederatedTransitDataBundleCreator;

/**
 * Captures the path to a gtfs feed, along with information about a default
 * agency id for the feed and agency id translation information. The bundle can
 * optionally specify a {@link URL} to a feed to be downloaded, but the file
 * path will take precedence if specified.
 * 
 * @author bdferris
 * @see GtfsBundles
 * @see FederatedTransitDataBundleCreator
 */
public class GtfsBundle {

  private File path;

  private URL url;

  private String defaultAgencyId;

  private Map<String, String> agencyIdMappings = new HashMap<String, String>();

  /**
   * @return the path to the feed (either a directory or zip file)
   */
  public File getPath() {
    return path;
  }

  /**
   * @param path the path to the feed (either a directory or a zip file)
   */
  public void setPath(File path) {
    this.path = path;
  }

  /**
   * @return a url where the feed can be downloaded, though {@link #getPath()}
   *         will take precedence)
   */
  public URL getUrl() {
    return url;
  }

  /**
   * @param url a url where the feed can be downloaded, though
   *          {@link #setPath(File)} will take precedence)
   */
  public void setUrl(URL url) {
    this.url = url;
  }

  /**
   * @return the default agency id for the feed (see
   *         {@link GtfsReader#setDefaultAgencyId(String)})
   */
  public String getDefaultAgencyId() {
    return defaultAgencyId;
  }

  /**
   * 
   * @param defaultAgencyId the default agency id for the feed (see
   *          {@link GtfsReader#setDefaultAgencyId(String)})
   */
  public void setDefaultAgencyId(String defaultAgencyId) {
    this.defaultAgencyId = defaultAgencyId;
  }

  /**
   * @return a set of agency id mappings that will be used to replace any
   *         matching agency i
   */
  public Map<String, String> getAgencyIdMappings() {
    return agencyIdMappings;
  }

  /**
   * @param agencyIdMappings a set of agency id mappings that will be used to
   *          replace any matching agency ids in the feed
   */
  public void setAgencyIdMappings(Map<String, String> agencyIdMappings) {
    this.agencyIdMappings = agencyIdMappings;
  }

}
