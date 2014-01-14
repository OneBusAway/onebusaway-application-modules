/**
 * Copyright (C) 2011 Brian Ferris <bdferris@onebusaway.org>
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
package org.onebusaway.transit_data.model.blocks;

public class BlockDetailsBean {

  private String blockId;

  private BlockBean block;

  private BlockStatusBean status;

  public String getBlockId() {
    return blockId;
  }

  public void setBlockId(String blockId) {
    this.blockId = blockId;
  }

  public BlockBean getBlock() {
    return block;
  }

  public void setBlock(BlockBean block) {
    this.block = block;
  }

  public BlockStatusBean getStatus() {
    return status;
  }

  public void setStatus(BlockStatusBean status) {
    this.status = status;
  }
}
