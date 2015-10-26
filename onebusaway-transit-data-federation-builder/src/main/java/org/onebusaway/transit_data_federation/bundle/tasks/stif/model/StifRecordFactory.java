/**
 * Copyright (C) 2011 Metropolitan Transportation Authority
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
package org.onebusaway.transit_data_federation.bundle.tasks.stif.model;

public abstract class StifRecordFactory<T extends StifRecord> {
	
	public abstract StifFieldDefinition<T>[] getFields();
	
	public abstract T createEmptyRecord();
	
	public T createRecord(byte[] bytes, int start, int end) {
		T record = createEmptyRecord();
		for (StifFieldDefinition<T> f : getFields()) {
			if (f.setter != null) {
				f.setter.setData(bytes, start, start + f.length); 
				f.setter.setField(record);
			}
			start += f.length;
			if (start >= end) {
			  //fell off end of record (because STIF is from an earlier version than parser)
			  break;
			}
		}
		return record;
	}
	
	
}
