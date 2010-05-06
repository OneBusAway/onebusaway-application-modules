/*
 * Copyright 2008 Brian Ferris
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package edu.washington.cs.rse.transit.web.oba.common.client.model;

import java.util.ArrayList;
import java.util.List;

public class ServicePatternBlocksBean extends ApplicationBean {

  private static final long serialVersionUID = 1L;

  private List<ServicePatternBlockBean> blocks = new ArrayList<ServicePatternBlockBean>();

  public List<ServicePatternBlockBean> getBlocks() {
    return blocks;
  }

  public void addBlock(ServicePatternBlockBean block) {
    this.blocks.add(block);
  }
}
