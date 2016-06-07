/**
 * Copyright (C) 2015 Cambridge Systematics
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
package org.onebusaway.nextbus.model.transiTime;

import java.util.List;

import org.onebusaway.nextbus.impl.rest.jackson.CapitalizeSerializer;
import org.onebusaway.nextbus.impl.rest.xstream.CapitalizeConverter;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonRootName;
import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;
import com.thoughtworks.xstream.annotations.XStreamConverter;
import com.thoughtworks.xstream.annotations.XStreamImplicit;
import com.thoughtworks.xstream.annotations.XStreamOmitField;

@XStreamAlias("direction")
public class PredictionsDirection {
  
  @XStreamAsAttribute
  @XStreamAlias("title")
  @XStreamConverter(CapitalizeConverter.class)
  @JsonProperty("title")
  @JsonSerialize(using = CapitalizeSerializer.class)
  private String headsign;
  
  @XStreamOmitField
  @JsonIgnore
  private int dir;
  
  @XStreamImplicit
  @JsonProperty("prediction")
  private List<Prediction> pred;

  public PredictionsDirection(){}
  
  public List<Prediction> getPred() {
    return pred;
  }

  public void setPred(List<Prediction> pred) {
    this.pred = pred;
  }

  public String getHeadsign() {
    return headsign;
  }

  public void setHeadsign(String headsign) {
    this.headsign = headsign;
  }

  public int getDir() {
    return dir;
  }

  public void setDir(int dir) {
    this.dir = dir;
  }

}

