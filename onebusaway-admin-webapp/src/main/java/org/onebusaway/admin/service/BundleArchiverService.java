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
package org.onebusaway.admin.service;

import javax.ws.rs.core.Response;

public interface BundleArchiverService {

  public Response getArchiveBundleList();

  public Response getFileByName(String dataset, String name, String file);

  public Response getFileById(String id, String file);

  public Response getArchiveBundleByName(String dataset, String name);

  public Response getArchiveBundleById(String id);
  
  public Response getArchiveBundleById(String id, String filter);

}
