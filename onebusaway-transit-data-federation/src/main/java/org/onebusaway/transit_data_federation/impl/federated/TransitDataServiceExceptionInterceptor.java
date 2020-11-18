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
package org.onebusaway.transit_data_federation.impl.federated;

import org.onebusaway.exceptions.NoSuchStopServiceException;
import org.onebusaway.exceptions.ServiceException;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class TransitDataServiceExceptionInterceptor {

  private final Logger _log = LoggerFactory.getLogger(TransitDataServiceExceptionInterceptor.class);

  @Around("execution(* org.onebusaway.transit_data_federation.impl.federated.TransitDataServiceImpl.*(..))")
  public Object doBasicProfiling(ProceedingJoinPoint pjp) throws Throwable {

    try {
      return pjp.proceed();
    } catch (ServiceException ex) {
      throw ex;
    } catch (Throwable ex) {
      if (!(ex instanceof NoSuchStopServiceException)) {
        // quiet the logs on no such stop service
        _log.error("error executing TransitDataService method", ex.getClass().getName() + ":" + ex.getMessage());
        if (_log.isDebugEnabled())
          _log.debug("detailed message", ex);
      }
      throw new ServiceException(ex);
    }
  }
}
