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

import org.onebusaway.gtfs.model.Stop;
import org.onebusaway.transit_data_federation.services.narrative.NarrativeService;

/**
 * Stop narrative information. Really only includes the direction of travel /
 * orientation a stop relative to the street.
 * 
 * @author bdferris
 * @see Stop
 * @see NarrativeService
 */
public final class StopNarrative implements Serializable {

  private static final long serialVersionUID = 1L;

  private final String direction;

  public static Builder builder() {
    return new Builder();
  }

  private StopNarrative(Builder builder) {
    this.direction = builder.direction;
  }

  public String getDireciton() {
    return direction;
  }

  public static class Builder {
    private String direction;

    public StopNarrative create() {
      return new StopNarrative(this);
    }

    public void setDirection(String direction) {
      this.direction = direction;
    }
  }

}
