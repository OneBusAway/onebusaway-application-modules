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
package org.onebusaway.transit_data.model.realtime;

import java.io.Serializable;

public class HistogramBean implements Serializable {

  private static final long serialVersionUID = 1L;

  private double[] values;

  private int[] counts;

  private String[] labels;

  public double[] getValues() {
    return values;
  }

  public void setValues(double[] values) {
    this.values = values;
  }

  public int[] getCounts() {
    return counts;
  }

  public void setCounts(int[] counts) {
    this.counts = counts;
  }

  public String[] getLabels() {
    return labels;
  }

  public void setLabels(String[] labels) {
    this.labels = labels;
  }

  public int getMaxCount() {
    int max = -1;
    for (int count : counts)
      max = Math.max(count, max);
    return max;
  }
}