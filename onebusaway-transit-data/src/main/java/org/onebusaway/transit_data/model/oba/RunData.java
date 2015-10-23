/**
 * Copyright (C) 2015 Cambridge Systematics, Inc.
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
package org.onebusaway.transit_data.model.oba;

import java.io.Serializable;

public class RunData implements Serializable {
  private static final long serialVersionUID = 1L;

  public String initialRun;

  public String reliefRun;
  
  public int reliefTime = -1;

  public RunData(String run1, String run2, int reliefTime) {
    this.initialRun = run1;
    this.reliefRun = run2;
    this.reliefTime = reliefTime;
  }

  public boolean hasRelief() {
    return reliefTime != -1;
  }

  public String toString() {
    return "RunData(" + initialRun + ", " + reliefRun + ", " + reliefTime + ")";
  }
}
