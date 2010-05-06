package org.onebusaway.metrokc2gtdf.model;

import org.onebusaway.csv.CsvFields;
import org.onebusaway.gtdf.model.IdentityBean;

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
