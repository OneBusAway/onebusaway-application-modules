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
package org.onebusaway.users.model;

import java.io.Serializable;
import java.util.Comparator;

import javax.persistence.Column;
import javax.persistence.Embeddable;

import org.onebusaway.users.services.UserIndexTypes;

/**
 * A type+value tuple that uniquely identifies a {@link UserIndex} object. See
 * {@link UserIndexTypes} for common user index type values.
 * 
 * @author bdferris
 * @see UserIndex
 * @see UserIndexTypes
 */
@Embeddable
public class UserIndexKey implements Serializable, Comparable<UserIndexKey> {

  private static final long serialVersionUID = 1L;

  @Column(columnDefinition = "VARCHAR(50)")
  private String type;

  @Column(columnDefinition = "VARCHAR(200)")
  private String value;

  public UserIndexKey() {

  }

  public UserIndexKey(String type, String value) {
    if (type == null)
      throw new IllegalArgumentException("type cannot be null");
    if (value == null)
      throw new IllegalArgumentException("value cannot be null");
    if (type.indexOf('_') != -1)
      throw new IllegalArgumentException("type cannot contain \"_\" character");
    this.type = type;
    this.value = value;
  }

  public static UserIndexKey parseString(String userIndexKey) {
    int index = userIndexKey.indexOf('_');
    if (index == -1)
      throw new IllegalArgumentException("invalid UserIndexKey: "
          + userIndexKey);
    String type = userIndexKey.substring(0, index);
    String value = userIndexKey.substring(index + 1);
    return new UserIndexKey(type, value);
  }

  public String getType() {
    return type;
  }

  public String getValue() {
    return value;
  }

  /****
   * {@link Comparator} Interface
   ****/

  public int compareTo(UserIndexKey o) {
    int c = this.type.compareTo(o.type);

    if (c == 0)
      c = this.value.compareTo(o.value);

    return c;
  }

  /****
   * {@link Object} Interface
   ****/

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((type == null) ? 0 : type.hashCode());
    result = prime * result + ((value == null) ? 0 : value.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (!(obj instanceof UserIndexKey))
      return false;
    UserIndexKey other = (UserIndexKey) obj;
    if (type == null) {
      if (other.type != null)
        return false;
    } else if (!type.equals(other.type))
      return false;
    if (value == null) {
      if (other.value != null)
        return false;
    } else if (!value.equals(other.value))
      return false;
    return true;
  }

  /**
   * It's important that this method not be changed, since the type_value form
   * is used in serialization / de-serialization
   */
  @Override
  public String toString() {
    return type + "_" + value;
  }

}
