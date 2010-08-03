package org.onebusaway.transit_data_federation.impl.federated;

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
      _log.error("error executing TransitDataService method", ex);
      throw new ServiceException(ex);
    }
  }
}
