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
package org.onebusaway.aws.monitoring.model.metrics;

public class MetricResponse {
	private Double responseTime;
	private Double metric;
	
	public MetricResponse(){}
	
	public MetricResponse(Double metric){
		this.metric = metric;
	}
	
	public MetricResponse(Double metric, Double responseTime){
		this.responseTime =  responseTime;
		this.metric = metric;
	}
	
	public Double getResponseTime() {
		return responseTime;
	}
	public void setResponseTime(Double responseTime) {
		this.responseTime = responseTime;
	}
	public Double getMetric() {
		return metric;
	}
	public void setMetric(Double metric) {
		this.metric = metric;
	}
	
}
