/**
 * 
 */
package org.onebusaway.metrokc2gtdf.model;

import org.onebusaway.csv.CsvFields;
import org.onebusaway.gtdf.model.IdentityBean;

@CsvFields(filename = "routes.csv")
public class MetroKCRoute extends IdentityBean<Integer> {

  private static final long serialVersionUID = 1L;

  private Integer id;

  private int number;

  public Integer getId() {
    return id;
  }

  public void setId(Integer id) {
    this.id = id;
  }

  public int getNumber() {
    return number;
  }

  public void setNumber(int number) {
    this.number = number;
  }
}