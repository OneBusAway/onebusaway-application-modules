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
