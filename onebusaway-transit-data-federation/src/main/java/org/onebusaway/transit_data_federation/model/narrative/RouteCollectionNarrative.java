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
package org.onebusaway.transit_data_federation.model.narrative;

import java.io.Serializable;

import org.onebusaway.gtfs.model.Agency;
import org.onebusaway.transit_data_federation.services.narrative.NarrativeService;
import org.onebusaway.transit_data_federation.services.transit_graph.RouteCollectionEntry;

/**
 * Route collection narrative information captures the narrative elements of a
 * {@link RouteCollectionEntry}.
 * 
 * @author bdferris
 * @see Agency
 * @see NarrativeService
 */
public final class RouteCollectionNarrative implements Serializable {

  private static final long serialVersionUID = 1L;

  private final String shortName;

  private final String longName;

  private final String description;

  private final int type;

  private final String url;

  private final String color;

  private final String textColor;

  public static Builder builder() {
    return new Builder();
  }

  private RouteCollectionNarrative(Builder builder) {
    this.shortName = builder.shortName;
    this.longName = builder.longName;
    this.description = builder.description;
    this.type = builder.type;
    this.url = builder.url;
    this.color = builder.color;
    this.textColor = builder.textColor;
  }

  public String getShortName() {
    return shortName;
  }

  public String getLongName() {
    return longName;
  }

  public String getDescription() {
    return description;
  }

  public int getType() {
    return type;
  }

  public String getUrl() {
    return url;
  }

  public String getColor() {
    return color;
  }

  public String getTextColor() {
    return textColor;
  }

  public static class Builder {

    private String shortName;

    private String longName;

    private String description;

    private int type;

    private String url;

    private String color;

    private String textColor;

    public RouteCollectionNarrative create() {
      return new RouteCollectionNarrative(this);
    }

    public void setShortName(String shortName) {
      this.shortName = shortName;
    }

    public void setLongName(String longName) {
      this.longName = longName;
    }

    public void setDescription(String description) {
      this.description = description;
    }

    public void setType(int type) {
      this.type = type;
    }

    public void setUrl(String url) {
      this.url = url;
    }

    public void setColor(String color) {
      this.color = color;
    }

    public void setTextColor(String textColor) {
      this.textColor = textColor;
    }
  }
}
