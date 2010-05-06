package org.onebusaway.metrokc2gtfs.model;

import org.onebusaway.common.model.IdentityBean;
import org.onebusaway.csv.CsvFields;

@CsvFields(filename = "trans_node.csv")
public class MetroKCTransNode extends IdentityBean<Integer> {

  private static final long serialVersionUID = 1L;

  private Integer id;

  private double x;

  private double y;

  public Integer getId() {
    return id;
  }

  public void setId(Integer id) {
    this.id = id;
  }

  public double getX() {
    return x;
  }

  public void setX(double x) {
    this.x = x;
  }

  public double getY() {
    return y;
  }

  public void setY(double y) {
    this.y = y;
  }
}
