/**
 * Copyright (C) 2015 Cambridge Systematics, Inc.
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
package org.onebusaway.admin.model.ui;

/**
 * DTO for sending existing directory to the UI
 * @author abelsare
 *
 */
public class ExistingDirectory implements Comparable {
	private String name;
	private String type;
	private String creationTimestamp;
	
	
	public ExistingDirectory(String name, String type, String creationTimestamp) {
		super();
		this.name = name;
		this.type = type;
		this.creationTimestamp = creationTimestamp;
	}

	public String toString() {
		return "{"
				+ name
				+ " : "
				+ type
				+ " : "
				+ creationTimestamp
				+ "}";
	}

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}
	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}
	/**
	 * @return the type
	 */
	public String getType() {
		return type;
	}
	/**
	 * @param type the type to set
	 */
	public void setType(String type) {
		this.type = type;
	}
	/**
	 * @return the creationTimestamp
	 */
	public String getCreationTimestamp() {
		return creationTimestamp;
	}
	/**
	 * @param creationTimestamp the creationTimestamp to set
	 */
	public void setCreationTimestamp(String creationTimestamp) {
		this.creationTimestamp = creationTimestamp;
	}
	
	public int compareTo(Object directory) {
		try {
			return this.getName().compareTo(
					((ExistingDirectory) directory).getName());
		} catch (Exception e) {
			return 0;
		}
	}
}