/**
 * Copyright (C) 2011 Brian Ferris <bdferris@onebusaway.org>
 * Copyright (C) 2012 Google, Inc.
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
package org.onebusaway.exceptions;

/**
 * A base exception type that is used by many OneBusAway service methods. I
 * decided to make this base exception a sub-class of {@link RuntimeException},
 * which means that you do no have to explicitly wrap calls to methods that
 * throw {@link ServiceException} with catch or with throws. There is a lot of
 * debate about that behavior in the Java community. Generally speaking, if
 * errors are non-recoverable, the convention is to make them a runtime
 * exception. However, there are definitely a number of uses of
 * {@link ServiceException} and its subclasses that are recoverable. Perhaps
 * some refactoring is in order?
 * 
 * @author bdferris
 */
public class ServiceException extends RuntimeException {

  private static final long serialVersionUID = 1L;

  public ServiceException() {

  }

  public ServiceException(String message) {
    super(message);
  }

  public ServiceException(Throwable ex) {
    super(ex);
  }
  
  public ServiceException(String message, Throwable ex) {
    super(message, ex);
  }
}
