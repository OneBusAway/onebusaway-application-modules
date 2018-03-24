/**
 * Copyright (C) 2016 Cambridge Systematics, Inc.
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
package org.onebusaway.api.actions.siri.model;

public enum DetailLevel {
  MINIMUM(0), BASIC(1), NORMAL(2), CALLS(3), STOPS(4), FULL(5);
  
  private int _type;
  
  DetailLevel(){
    _type = 2;
  }
  
  DetailLevel(int type){
    _type = type;
  }
  
  public int valueOf(){
    return _type;
  }
  
  public static boolean contains(String type){
        for(DetailLevel DetailLevel:values())
             if (DetailLevel.name().equalsIgnoreCase(type)) 
                return true;
        return false;
  } 
}
