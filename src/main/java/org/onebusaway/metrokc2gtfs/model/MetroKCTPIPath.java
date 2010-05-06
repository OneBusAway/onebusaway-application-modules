/**
 * 
 */
package org.onebusaway.metrokc2gtfs.model;

import org.onebusaway.csv.CsvFields;

@CsvFields(filename = "tpi_path.csv")
public class MetroKCTPIPath implements Comparable<MetroKCTPIPath> {

  private int id;

  private int sequence;

  private int flowDirection;

  private int transLink;

  public int getId() {
    return id;
  }

  public void setId(int id) {
    this.id = id;
  }

  public int getSequence() {
    return sequence;
  }

  public void setSequence(int sequence) {
    this.sequence = sequence;
  }

  public int getFlowDirection() {
    return flowDirection;
  }

  public void setFlowDirection(int flowDirection) {
    this.flowDirection = flowDirection;
  }

  public int getTransLink() {
    return transLink;
  }

  public void setTransLink(int transLink) {
    this.transLink = transLink;
  }

  public int compareTo(MetroKCTPIPath o) {
    return this.sequence == o.sequence ? 0 : (this.sequence < o.sequence ? -1
        : 1);
  }
}