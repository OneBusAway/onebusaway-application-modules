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

import java.io.File;
import java.util.Map;


/**
 * Makes connection to the remote server with the given URL and retrieves the required content
 * @author abelsare
 *
 */
public interface RemoteConnectionService {
	
	/**
	 * Opens a connection to the remote server and retrieves the content. Content is returned as string
	 * separated by new line
	 * @param url url of the remote server
	 * @return the required content as string
	 */
	String getContent(String url);
	String getContent(String url, String sessionId);
	String postContent(String url, Map<String,String> params);
	String postContent(String url, Map<String,String> params, String sessionId);
	
	/**
	 * Posts binary data to the given url and returns response of the given type
	 * @param url remote server url
	 * @param data binary data to post
	 * @param responseType desired type of post response
	 * @return response returned by remote server 
	 */
	<T> T postBinaryData(String url, File data, Class<T> responseType);

}
