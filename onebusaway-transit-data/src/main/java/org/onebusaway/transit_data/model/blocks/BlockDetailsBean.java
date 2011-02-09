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
