package org.onebusaway.nextbus.model.nextbus;

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
	
	@XStreamImplicit
	private List<BodyError> errors;

	public List<T> getResponse() {
		return response;
	}

	public void setResponse(List<T> response) {
		this.response = response;
	}
	
	public List<BodyError> getErrors() {
		if(errors == null)
			 errors = new ArrayList<BodyError>();
		return errors;
	}

	public void setErrors(List<BodyError> errors) {
		this.errors = errors;
	}
}
