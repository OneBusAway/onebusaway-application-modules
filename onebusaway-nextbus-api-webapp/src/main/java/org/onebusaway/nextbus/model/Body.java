package org.onebusaway.nextbus.model;

import java.util.ArrayList;
import java.util.List;

import org.onebusaway.nextbus.impl.conversion.ListToStringConverter;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamConverter;
import com.thoughtworks.xstream.annotations.XStreamImplicit;

@XStreamAlias("body")
public class Body<T> {
	@XStreamImplicit
	private List<T> response = new ArrayList<T>();
	
	@XStreamAlias("errors")
	private List<String> errors;

	public List<T> getResponse() {
		return response;
	}

	public void setResponse(List<T> response) {
		this.response = response;
	}
	
	public List<String> getErrors() {
		if(errors == null)
			 errors = new ArrayList<String>();
		return errors;
	}

	public void setErrors(List<String> errors) {
		this.errors = errors;
	}
}
