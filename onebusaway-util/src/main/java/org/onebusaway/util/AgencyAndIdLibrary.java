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
package org.onebusaway.util;

import org.onebusaway.gtfs.model.AgencyAndId;

/**
 * By convention, we make entity ids unique by prepending the agency id for the
 * resource. So an id will take the form "agencyId_entityId". Agency ids are
 * typically numeric by convention. This library provides convenience methods
 * for parsing and constructing these ids from {@link AgencyAndId} objects.
 * 
 * @author bdferris
 * 
 */
public class AgencyAndIdLibrary {

  public static final char ID_SEPARATOR = '_';

  /**
   * Given an id of the form "agencyId_entityId", parses into a
   * {@link AgencyAndId} id object.
   * 
   * @param value id of the form "agencyId_entityId"
   * @return an id object
   */
  public static AgencyAndId convertFromString(String value) {
    if( value == null || value.isEmpty())
      return null;
    int index = value.indexOf(ID_SEPARATOR);
    if (index == -1) {
      throw new IllegalStateException("invalid agency-and-id: " + value);
    } else {
      return new AgencyAndId(value.substring(0, index),
          value.substring(index + 1));
    }
  }

  /**
   * Given an {@link AgencyAndId} object, creates a string representation of the
   * form "agencyId_entityId"
   * 
   * @param aid an id object
   * @return a string representation of the form "agencyId_entityId"
   */
  public static String convertToString(AgencyAndId aid) {
    if( aid == null)
      return null;
    return aid.getAgencyId() + ID_SEPARATOR + aid.getId();
  }
}
