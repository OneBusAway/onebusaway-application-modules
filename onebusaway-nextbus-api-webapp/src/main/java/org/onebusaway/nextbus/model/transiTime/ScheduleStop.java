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
package org.onebusaway.nextbus.model.transiTime;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;
import com.thoughtworks.xstream.annotations.XStreamOmitField;

@XStreamAlias("stop")
public class ScheduleStop {
	
	@XStreamAsAttribute 
	private String tag;

	@XStreamAsAttribute
	@XStreamAlias("epochTime")
	private long timeSecs;
	
	private String timeStr;

	public String getTag() {
		return tag;
	}

	public void setTag(String tag) {
		this.tag = tag;
	}

	public long getTimeSecs() {
		return timeSecs;
	}

	public void setTimeSecs(long timeSecs) {
		this.timeSecs = timeSecs;
	}

	public String getValue() {
		return timeStr;
	}

	public void setValue(String timeStr) {
		this.timeStr = timeStr;
	}

	
	@Override
    public int hashCode() {
      final int prime = 31;
      int result = 1;
      result = prime * result + ((tag == null) ? 0 : tag.hashCode());
      return result;
    }
	
	@Override
    public boolean equals(Object obj) {
      if (this == obj)
        return true;
      if (obj == null)
        return false;
      if (getClass() != obj.getClass())
        return false;
      
      ScheduleStop other = (ScheduleStop) obj;
      if(this.getTag().equals(other.getTag()))
    	  return true;
      return false;
    }
	

}
