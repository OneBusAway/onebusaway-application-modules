package org.onebusaway.nextbus.model.transiTime;

import java.util.ArrayList;
import java.util.List;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;

@XStreamAlias("direction")
public class PredictionsDirection {
  
  @XStreamAsAttribute
  @XStreamAlias("title")
  private String headsign;
  
  @XStreamAlias("predictions")
  private List<Prediction> pred;

  public PredictionsDirection(){}
  
  public List<Prediction> getPred() {
    return pred;
  }

  public void setPred(List<Prediction> predictions) {
    this.pred = predictions;
  }

  public String getHeadsign() {
    return headsign;
  }

  public void setHeadsign(String headsign) {
    this.headsign = headsign;
  }

}

