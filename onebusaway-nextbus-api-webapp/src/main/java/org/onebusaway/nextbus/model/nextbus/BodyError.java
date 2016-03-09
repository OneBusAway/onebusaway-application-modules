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
package org.onebusaway.nextbus.model.nextbus;

import com.fasterxml.jackson.annotation.JsonRawValue;
import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;


@XStreamAlias("Error")
public class BodyError {
  
  public BodyError(){}
  
  public BodyError(String content){
    this.content = content;
  }
  
  public BodyError(String content, Object...objects){
    this.content = String.format(content, objects);
  }
  
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
  
  public String toString(){
	  return content;
  }
  

}