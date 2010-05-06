/*
 * Copyright 2008 Brian Ferris
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package edu.washington.cs.rse.transit.common.model.aggregate;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.hibernate.annotations.Index;

import edu.washington.cs.rse.transit.common.model.IdentityBean;
import edu.washington.cs.rse.transit.common.model.ServicePattern;
import edu.washington.cs.rse.transit.common.model.StopLocation;
import edu.washington.cs.rse.transit.common.model.Timepoint;

@Entity
@Table(name = "transit_stop_timepoint_interpolations")
public class StopTimepointInterpolation extends IdentityBean {

  private static final long serialVersionUID = 1L;

  @Id
  @GeneratedValue
  private int id;

  @ManyToOne
  private ServicePattern servicePattern;

  @ManyToOne(fetch = FetchType.LAZY)
  private StopLocation stop;

  @Index(name="transit_stop_timepoint_interpolations_stopIndex")
  private int stopIndex;

  @ManyToOne
  private Timepoint fromTimepoint;

  @Index(name = "fromTimepointSequence")
  private int fromTimepointSequence;

  @ManyToOne
  private Timepoint toTimepoint;

  @Index(name = "toTimepointSequence")
  private int toTimepointSequence;

  private double ratio;

  public int getId() {
    return id;
  }

  public void setId(int id) {
    this.id = id;
  }

  public ServicePattern getServicePattern() {
    return servicePattern;
  }

  public void setServicePattern(ServicePattern servicePattern) {
    this.servicePattern = servicePattern;
  }

  public StopLocation getStop() {
    return stop;
  }

  public void setStop(StopLocation stop) {
    this.stop = stop;
  }

  public void setStopIndex(int index) {
    this.stopIndex = index;
  }

  public int getStopIndex() {
    return this.stopIndex;
  }

  public Timepoint getFromTimepoint() {
    return fromTimepoint;
  }

  public void setFromTimepoint(Timepoint fromTimepoint) {
    this.fromTimepoint = fromTimepoint;
  }

  public int getFromTimepointSequence() {
    return fromTimepointSequence;
  }

  public void setFromTimepointSequence(int fromTimepointSequence) {
    this.fromTimepointSequence = fromTimepointSequence;
  }

  public Timepoint getToTimepoint() {
    return toTimepoint;
  }

  public void setToTimepoint(Timepoint toTimepoint) {
    this.toTimepoint = toTimepoint;
  }

  public int getToTimepointSequence() {
    return toTimepointSequence;
  }

  public void setToTimepointSequence(int toTimepointSequence) {
    this.toTimepointSequence = toTimepointSequence;
  }

  public double getRatio() {
    return ratio;
  }

  public void setRatio(double ratio) {
    this.ratio = ratio;
  }
}
