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
package org.onebusaway.nextbus.model.nextbus;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;

@XStreamAlias("message")
public class Message {
	
	@XStreamAsAttribute 
	private String id;
	
	@XStreamAsAttribute 
	private String creator;
	
	@XStreamAsAttribute 
	private boolean sendToBuses;
	
	@XStreamAsAttribute 
	private long  startBoundary;
	
	@XStreamAsAttribute 
	private long endBoundary;

	@XStreamAsAttribute 
	private String  startBoundaryStr;
	
	@XStreamAsAttribute 
	private String endBoundaryStr;
	
	private MessageInterval messageInterval;
	
	@XStreamAlias("phonemeText")
	private MessageText phoneMeText;
	
	@XStreamAlias("text")
	private MessageText messageText;
	
	@XStreamAlias("textSecondaryLanguage")
	private MessageText textSecondaryLanguage;

	public MessageInterval getMessageInterval() {
		return messageInterval;
	}

	public void setMessageInterval(MessageInterval messageInterval) {
		this.messageInterval = messageInterval;
	}

	public MessageText getPhoneMeText() {
		return phoneMeText;
	}

	public void setPhoneMeText(MessageText phoneMeText) {
		this.phoneMeText = phoneMeText;
	}

	public MessageText getTextSecondaryLanguage() {
		return textSecondaryLanguage;
	}

	public void setTextSecondaryLanguage(MessageText textSecondaryLanguage) {
		this.textSecondaryLanguage = textSecondaryLanguage;
	}

	public MessageText getMessageText() {
		return messageText;
	}

	public void setMessageText(MessageText messageText) {
		this.messageText = messageText;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getCreator() {
		return creator;
	}

	public void setCreator(String creator) {
		this.creator = creator;
	}

	public boolean isSendToBuses() {
		return sendToBuses;
	}

	public void setSendToBuses(boolean sendToBuses) {
		this.sendToBuses = sendToBuses;
	}

	public Long getStartBoundary() {
		return startBoundary;
	}

	public void setStartBoundary(Long startBoundary) {
		this.startBoundary = startBoundary;
	}

	public Long getEndBoundary() {
		return endBoundary;
	}

	public void setEndBoundary(Long endBoundary) {
		this.endBoundary = endBoundary;
	}

	public String getStartBoundaryStr() {
		return startBoundaryStr;
	}

	public void setStartBoundaryStr(String startBoundaryStr) {
		this.startBoundaryStr = startBoundaryStr;
	}

	public String getEndBoundaryStr() {
		return endBoundaryStr;
	}

	public void setEndBoundaryStr(String endBoundaryStr) {
		this.endBoundaryStr = endBoundaryStr;
	}

}
