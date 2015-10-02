package org.onebusaway.nextbus.impl;

public class ConfigurationUtil {
	
	private String transitTimeHost;
	private String transitTimePort;
	
	public String getTransitTimeHost() {
		return transitTimeHost;
	}

	public void setTransitTimeHost(String transitTimeHost) {
		this.transitTimeHost = transitTimeHost;
	}

	public String getTransitTimePort() {
		return transitTimePort;
	}

	public void setTransitTimePort(String transitTimePort) {
		this.transitTimePort = transitTimePort;
	}

	public ConfigurationUtil(){}
}
