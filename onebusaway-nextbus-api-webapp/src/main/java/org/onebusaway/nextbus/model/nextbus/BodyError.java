package org.onebusaway.nextbus.model.nextbus;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;


@XStreamAlias("Error")
public class BodyError {
  
  public BodyError(){}
  
  public BodyError(String content){
    this.content = content;
  }
  
  @XStreamAsAttribute
  @XStreamAlias("content")
  private String content;
  
  @XStreamAsAttribute
  @XStreamAlias("shouldRetry")
  private boolean shouldRetry;
  
  public String getContent() {
    return content;
  }

  public void setContent(String content) {
    this.content = content;
  }

  public boolean isShouldRetry() {
    return shouldRetry;
  }

  public void setShouldRetry(boolean shouldRetry) {
    this.shouldRetry = shouldRetry;
  }
  

}