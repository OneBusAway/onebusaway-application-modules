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
package org.onebusaway.transit_data_federation.model.bundle;

import java.io.Serializable;

public class BlockRunIndex implements Serializable {
  private static final long serialVersionUID = 1L;
  private final int blockId;
  private final int runId;
  private final int routeKey;
  
  public static Builder builder() {
    return new Builder();
  }
  
  private BlockRunIndex(Builder builder) {
    this.blockId = builder.blockId;
    this.runId = builder.runId;
    this.routeKey = builder.routeKey;
  }
  
  public int getBlockId() {
    return blockId;
  }
  public int getRunId() {
    return runId;
  }
  public int getRouteKey() {
    return routeKey;
  }
  
  public static class Builder {
    private int blockId;
    private int runId;
    private int routeKey;
    
    public BlockRunIndex create() {
      return new BlockRunIndex(this);
    }
    
    public void setBlockId(int blockId) {
      this.blockId = blockId;
    }
    
    public void setRunId(int runId) {
      this.runId = runId;
    }

    public void setRouteKey(int routeKey) {
      this.routeKey = routeKey;
    }
  }
}
