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