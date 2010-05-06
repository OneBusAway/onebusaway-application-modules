/*
 * Copyright 2008 Brian Ferris
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
package edu.washington.cs.rse.transit.phone;

public class PathSupportGo {
	public static final String ASTERISK_DIR_PROPERTY = "asterisk.dir";

	public static final String ASTERISK_DIR_DEFAULT = "/var/lib/asterisk";

	public static final String SOX_BIN_PROPERTY = "sox.bin";

	public static final String SOX_BIN_DEFAULT = "sox";

	public static final String SWIFT_BIN_PROPERTY = "swift.bin";

	public static final String SWIFT_BIN_DEFAULT = "swift";

	public static final String getAsteriskDir() {
		return System.getProperty(ASTERISK_DIR_PROPERTY, ASTERISK_DIR_DEFAULT);
	}
	
	public static final String getSwiftBin() {
		return System.getProperty(SWIFT_BIN_PROPERTY,SWIFT_BIN_DEFAULT);
	}
	
	public static final String getSoxBin() {
		return System.getProperty(SOX_BIN_PROPERTY,SOX_BIN_DEFAULT);
	}
}
