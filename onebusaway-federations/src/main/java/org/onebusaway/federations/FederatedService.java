package org.onebusaway.federations;

import java.lang.reflect.Proxy;
import java.util.List;
import java.util.Map;

import org.onebusaway.federations.annotations.FederatedServiceMethodInvocationHandlerFactory;
import org.onebusaway.geospatial.model.CoordinateBounds;

/**
 * A FederatedService is one that can be federated, or split, across a
 * geographic area. We use this idea with respect to transit agencies, as they
 * each typically provide service over a particular geographic region. Instead
 * of creating one unified process that can serve data for all potential transit
 * agencies, we federate the data into clusters of data which can be served by
 * independent processes. Typically, agencies that have overlapping coverage
 * areas would need to be in the same federated cluster. So for example,
 * agencies in the Puget Sound region near Seattle would all be in one
 * FederatedService cluster, while agencies in Portland, OR would be in another.
 * 
 * We mean to apply this federation to a Java service interface. That is, a Java
 * service interface might define methods that provide generic transit data
 * operations. That service interface would extend from {@link FederatedService}
 * to indicate that the interface can be split across federated regions. The
 * regions for each instance of the federated service are determined by the
 * values returned by {@link #getAgencyIdsWithCoverageArea()}. The federated
 * service instances are all seamlessly linked together as a virtual
 * {@link Proxy} using the {@link FederatedServiceInvocationHandler}, passing
 * method invocations to the service interface to the appropriate federate
 * service instance based on method arguments such as agency ids or lat-lon
 * coordinates.
 * 
 * How are method service interface method arguments used to determine where to
 * dispatch a method? Each method in a service interface must be annotated to
 * indicate how the arguments should be evaluated to determine the target agency
 * id or coordinate location for a particular method call. The task of
 * processing these method annotations is handled by
 * {@link FederatedServiceMethodInvocationHandlerFactory}. See the documentation
 * of that class for a full list of available annotations.
 * 
 * @author bdferris
 * @see FederatedServiceFactoryBean
 * @see FederatedServiceCollection
 * @see FederatedServiceInvocationHandler
 * @see FederatedServiceMethodInvocationHandlerFactory
 */
public interface FederatedService {
  public Map<String, List<CoordinateBounds>> getAgencyIdsWithCoverageArea();
}
