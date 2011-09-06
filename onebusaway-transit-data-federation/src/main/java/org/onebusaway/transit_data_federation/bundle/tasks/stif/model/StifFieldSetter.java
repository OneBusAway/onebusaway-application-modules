/**
 * Copyright (c) 2011 Metropolitan Transportation Authority
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package org.onebusaway.transit_data_federation.bundle.tasks.stif.model;

public abstract class StifFieldSetter<T extends StifRecord> {
	protected byte[] bytes;
	protected int start;
	protected int end;
	public void setData(byte[] bytes, int start, int end) {
		this.bytes = bytes;
		this.start = start;
		this.end = end;
	}
	public abstract void setField(T record);
	
	public String getStringData() {
		return new String(bytes, start, end - start).trim();
	}
	
	public int getInteger() {
		return Integer.parseInt(getStringData());
	}

	public int getTimeFromCentiminutes() {
		int centiminutes = getInteger();
		return (centiminutes * 60) / 100;
	}
	
	public float getDecimalFixedPoint(int digits) {
		while(bytes[start] == ' ') {
			start += 1;
		}
		if (bytes[start] == '-') {
			digits += 1;
		}
		String beforePoint = new String(bytes, start, digits).trim();
		String afterPoint = new String(bytes, start + digits, (end - start) - digits).trim();
		return Float.parseFloat(beforePoint + "." + afterPoint);
	}
}
