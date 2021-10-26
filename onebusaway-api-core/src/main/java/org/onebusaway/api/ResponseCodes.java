/**
 * Copyright (C) 2011 Brian Ferris <bdferris@onebusaway.org>
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
package org.onebusaway.api;

public class ResponseCodes {

  public static final String VERSION_1_0 = "1.0";

  public static final int RESPONSE_OK = 200;

  public static final int RESPONSE_INVALID_ARGUMENT = 400;

  public static final int RESPONSE_UNAUTHORIZED = 401;

  public static final int RESPONSE_RESOURCE_NOT_FOUND = 404;
  
  public static final int RESPONSE_TOO_MANY_REQUESTS = 429;
  
  public static final int RESPONSE_OUT_OF_SERVICE_AREA = 440;

  public static final int RESPONSE_SERVICE_EXCEPTION = 500;

  public static final int RESPONSE_OUT_OF_SERVICE_TIMERANGE = 510;
}
