package org.onebusaway.federations.annotations;

import java.util.List;
import java.util.Map;

/**
 * Controls the type of return value aggregation used in a
 * {@link FederatedServiceMethodInvocationHandler} as annotated with
 * {@link FederatedByAggregateMethod} annotation.
 * 
 * @author bdferris
 */
public enum EMethodAggregationType {

  /**
   * Results are aggregated to a {@link List}
   */
  LIST,

  /**
   * Results are aggregated to a {@link Map}
   */
  MAP
}
