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
package org.onebusaway.admin.service.bundle.hastus.xml;

public class PttRoute {
  private String id;

  private String description;

  private String publicId;

  private String firstDirectionName;

  private String secondDirectionName;

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public String getPublicId() {
    return publicId;
  }

  public void setPublicId(String publicId) {
    this.publicId = publicId;
  }

  public String getFirstDirectionName() {
    return firstDirectionName;
  }

  public void setFirstDirectionName(String firstDirectionName) {
    this.firstDirectionName = firstDirectionName;
  }

  public String getSecondDirectionName() {
    return secondDirectionName;
  }

  public void setSecondDirectionName(String secondDirectionName) {
    this.secondDirectionName = secondDirectionName;
  }
}
