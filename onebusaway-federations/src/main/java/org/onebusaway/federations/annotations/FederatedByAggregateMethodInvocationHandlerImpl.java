package org.onebusaway.federations.annotations;

import org.onebusaway.exceptions.ServiceAreaServiceException;
import org.onebusaway.federations.FederatedService;
import org.onebusaway.federations.FederatedServiceCollection;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Provides a {@link FederatedServiceMethodInvocationHandler} implementation for
 * the {@link FederatedByAggregateMethod} annotation.
 * 
 * @author bdferris
 */
class FederatedByAggregateMethodInvocationHandlerImpl implements FederatedServiceMethodInvocationHandler {

  private EMethodAggregationType _aggregationType;

  public FederatedByAggregateMethodInvocationHandlerImpl(EMethodAggregationType aggregationType) {
    _aggregationType = aggregationType;
  }
  
  public EMethodAggregationType getAggregationType() {
    return _aggregationType;
  }

  public Object invoke(FederatedServiceCollection collection, Method method, Object[] args)
      throws ServiceAreaServiceException, IllegalArgumentException, IllegalAccessException, InvocationTargetException {

    List<Object> results = new ArrayList<Object>();

    for (FederatedService service : collection.getAllServices())
      results.add(method.invoke(service, args));

    return aggregateResults(results);
  }

  private Object aggregateResults(List<Object> results) {
    switch (_aggregationType) {
      case LIST:
        return aggregateResultsAsList(results);
      case MAP:
        return aggregateResultsAsMap(results);
      default:
        throw new IllegalStateException("unknown aggregation type: " + _aggregationType);
    }
  }

  private Object aggregateResultsAsList(List<Object> results) {
    List<Object> asList = new ArrayList<Object>();
    for (Object result : results) {
      List<?> values = (List<?>) result;
      asList.addAll(values);
    }
    return asList;
  }

  private Object aggregateResultsAsMap(List<Object> results) {
    Map<Object, Object> asMap = new HashMap<Object, Object>();
    for (Object result : results) {
      Map<?, ?> values = (Map<?, ?>) result;
      asMap.putAll(values);
    }
    return asMap;
  }
}
