/**
 * Copyright (C) 2014 Kurt Raschke <kurt@kurtraschke.com>
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
package org.onebusaway.utility;

import java.io.Serializable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MavenVersion implements Serializable {

  private static final long serialVersionUID = 1L;

  private String major;
  private String minor;
  private String incremental;
  private String qualifier;

  public String getMajor() {
    return major;
  }

  public String getMinor() {
    return minor;
  }

  public String getIncremental() {
    return incremental;
  }

  public String getQualifier() {
    return qualifier;
  }

  public MavenVersion() {
  }

  public MavenVersion(String version) {
    // TODO: Would be cleaner with named capturing groups but they
    // aren't supported until Java 7
    final Pattern VERSION_PATTERN = Pattern.compile("([0-9]+)\\.([0-9]+)\\.([0-9]+)(?:-(.*))?");

    Matcher m = VERSION_PATTERN.matcher(version);

    if (m.matches()) {
      this.major = m.group(1);
      this.minor = m.group(2);
      this.incremental = m.group(3);
      this.qualifier = m.group(4);
    } else {
      this.major = "";
      this.minor = "";
      this.incremental = "";
      this.qualifier = "";
    }
  }
}