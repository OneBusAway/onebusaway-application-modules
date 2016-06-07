/**
 * Copyright (C) 2011 Brian Ferris <bdferris@onebusaway.org>
 * Copyright (C) 2015 University of South Florida (cagricetin@mail.usf.edu)
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
package org.onebusaway.transit_data_federation.model.narrative;

import java.io.Serializable;

import org.onebusaway.gtfs.model.Agency;
import org.onebusaway.transit_data_federation.services.narrative.NarrativeService;

/**
 * Agency narrative information. Mostly just adds disclaimer information.
 * 
 * @author bdferris
 * @see Agency
 * @see NarrativeService
 */
public final class AgencyNarrative implements Serializable {

  private static final long serialVersionUID = 2L;

  private final String name;

  private final String url;

  private final String timezone;

  private final String lang;

  private final String phone;
  
  private final String fareUrl;

  private final String disclaimer;
  
  private final String email;

  /**
   * If true, indicates the agency provides private service that is not
   * available to the general public.
   */
  private final boolean privateService;

  public static Builder builder() {
    return new Builder();
  }

  private AgencyNarrative(Builder builder) {
    this.name = builder.name;
    this.url = builder.url;
    this.timezone = builder.timezone;
    this.lang = builder.lang;
    this.phone = builder.phone;
    this.disclaimer = builder.disclaimer;
    this.fareUrl = builder.fareUrl;
    this.privateService = builder.privateService;
    this.email = builder.email;
  }

  public String getName() {
    return name;
  }

  public String getUrl() {
    return url;
  }

  public String getTimezone() {
    return timezone;
  }

  public String getLang() {
    return lang;
  }

  public String getPhone() {
    return phone;
  }
  
  public String getEmail() {
    return email;
  }
  
  public String getDisclaimer() {
    return disclaimer;
  }
  
  public String getFareUrl() {
    return fareUrl;
  }

  /**
   * If true, indicates the agency provides private service that is not
   * available to the general public.
   */
  public boolean isPrivateService() {
    return privateService;
  }

  public static class Builder {

    private String name;

    private String url;

    private String timezone;

    private String lang;

    private String phone;

    private String disclaimer;
    
    private String fareUrl;

    private boolean privateService;
    
    private String email;

    public AgencyNarrative create() {
      return new AgencyNarrative(this);
    }

    public void setName(String name) {
      this.name = name;
    }

    public void setUrl(String url) {
      this.url = url;
    }

    public void setTimezone(String timezone) {
      this.timezone = timezone;
    }
    
    public void setLang(String lang) {
      this.lang = lang;
    }

    public void setPhone(String phone) {
      this.phone = phone;
    }

    public void setDisclaimer(String disclaimer) {
      this.disclaimer = disclaimer;
    }
    
    public void setEmail(String email){
      this.email = email;
    }

    public void setFareUrl(String fareUrl) {
      this.fareUrl = fareUrl;
    }
    
    public void setPrivateService(boolean privateService) {
      this.privateService = privateService;
    }
  }
}
