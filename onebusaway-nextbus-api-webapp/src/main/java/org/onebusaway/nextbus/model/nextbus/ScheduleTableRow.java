package org.onebusaway.nextbus.model.nextbus;

import java.util.ArrayList;
import java.util.List;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;
import com.thoughtworks.xstream.annotations.XStreamImplicit;

@XStreamAlias("tr")
public class ScheduleTableRow {
	@XStreamImplicit
	private List<ScheduleStop> stops = new ArrayList<ScheduleStop>();

	@XStreamAsAttribute
	@XStreamAlias("blockID")
	private String blockId;
	
	public ScheduleTableRow(String blockId){
		this.blockId = blockId;
	}

	public List<ScheduleStop> getStops() {
		return stops;
	}

	public void setStops(List<ScheduleStop> stops) {
		this.stops = stops;
	}

	public String getBlockId() {
		return blockId;
	}

	public void setBlockId(String blockId) {
		this.blockId = blockId;
	}
}
