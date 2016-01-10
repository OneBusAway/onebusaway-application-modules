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
package org.onebusaway.admin.bundle.model;

public class BundleFile {

  private String filename;
  private String md5;

  public String getFilename() {
    return filename;
  }

  public String getMd5() {
    return md5;
  }

  public void setFilename(String filename) {
    this.filename = filename;
  }

  public void setMd5(String md5) {
    this.md5 = md5;
  }
}
