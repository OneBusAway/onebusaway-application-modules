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
package org.onebusaway.twilio.actions.stops;

import java.util.Arrays;
import java.util.List;

import org.apache.struts2.convention.annotation.Result;
import org.apache.struts2.convention.annotation.Results;
import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.transit_data.model.StopBean;
import org.onebusaway.twilio.actions.TwilioSupport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Results({
		@Result(name = "back", type = "redirectAction", params = { "namespace",
				"/", "actionName", "index" }),
		@Result(name = "stop-for-code", location = "stop-for-code", type = "chain") })
public class IndexAction extends TwilioSupport {

	private static final long serialVersionUID = 1L;
	private static Logger _log = LoggerFactory.getLogger(IndexAction.class);
	private String _stopCode;
	private StopBean _stop;

	public String getStopCode() {
		return _stopCode;
	}

	public void setStopCode(String stopCode) {
		_stopCode = stopCode;
	}

	public StopBean getStop() {
		return _stop;
	}

	public void setStop(StopBean stop) {
		_stop = stop;
	}

	@Override
	public String execute() throws Exception {
		_log.debug("in stops index with input=" + getInput());

		List<StopBean> stops = (List<StopBean>) sessionMap.get("stops");
		if (stops != null){
			sessionMap.remove("stops");
			setStop(getSelectedMultiStop(stops));
		}

		if (getInput() != null) {
			if (PREVIOUS_MENU_ITEM.equals(getInput())) {
				return "back";
			}
			setStopCode(getInput());
			_log.debug("forwarding to stops");
			return "stop-for-code";
		} else {
			setNextAction("stops/index");
		}

		return INPUT;
	}

	private StopBean getSelectedMultiStop(List<StopBean> stops) {
		if (stops != null) {
			for (int i = 0; i < stops.size(); i++) {
				if (String.valueOf(i + 1).equals(getInput())) {
					StopBean stop = stops.get(i);
					return stop;
				}
			}
		}
		return null;
	}
}
