package org.onebusaway.transit_data_federation_webapp.siri;

import java.io.Reader;
import java.io.Writer;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.onebusaway.collections.CollectionsLibrary;
import org.onebusaway.siri.core.ESiriModuleType;
import org.onebusaway.siri.core.SiriClient;
import org.onebusaway.siri.core.SiriClientSubscriptionRequest;
import org.onebusaway.siri.core.SiriLibrary;
import org.onebusaway.siri.core.SiriRequestFactory;
import org.onebusaway.siri.core.handlers.SiriServiceDeliveryHandler;
import org.onebusaway.transit_data_federation.impl.realtime.siri.SiriEndpointDetails;
import org.onebusaway.transit_data_federation.impl.realtime.siri.SiriService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import uk.org.siri.siri.AbstractServiceDeliveryStructure;
import uk.org.siri.siri.AbstractSubscriptionStructure;
import uk.org.siri.siri.ServiceDelivery;
import uk.org.siri.siri.SubscriptionQualifierStructure;
import uk.org.siri.siri.SubscriptionRequest;

@Controller
public class SiriController extends SiriClient {

  private static Logger _log = LoggerFactory.getLogger(SiriController.class);

  private SiriService _siriService;

  private ServiceDeliveryHandlerImpl _handler = new ServiceDeliveryHandlerImpl();

  private List<String> _endpoints;

  private Map<String, SiriEndpointDetails> _endpointsBySubscriptionId = new HashMap<String, SiriEndpointDetails>();

  @Autowired
  public void setSiriService(SiriService siriService) {
    _siriService = siriService;
  }

  public void setEndpoint(String endpoint) {
    _endpoints = Arrays.asList(endpoint);
  }

  public void setEndpoints(List<String> endpoints) {
    _endpoints = endpoints;
  }

  @PostConstruct
  public void start() {

    addServiceDeliveryHandler(_handler);
    super.start();

    if (!CollectionsLibrary.isEmpty(_endpoints)) {

      SiriRequestFactory factory = new SiriRequestFactory();

      for (String endpoint : _endpoints) {

        Map<String, String> args = SiriLibrary.getLineAsMap(endpoint);
        SiriClientSubscriptionRequest request = factory.createSubscriptionRequest(args);
        SubscriptionRequest subRequest = request.getPayload();

        SiriEndpointDetails details = new SiriEndpointDetails();
        String agencyId = args.get("AgencyId");
        details.setAgencyId(agencyId);

        for (ESiriModuleType moduleType : ESiriModuleType.values()) {

          List<AbstractSubscriptionStructure> subscriptionsForModule = SiriLibrary.getSubscriptionRequestsForModule(
              subRequest, moduleType);

          for (AbstractSubscriptionStructure subscriptionForModule : subscriptionsForModule) {
            SubscriptionQualifierStructure subId = subscriptionForModule.getSubscriptionIdentifier();
            if (subId == null) {
              subId = new SubscriptionQualifierStructure();
              subId.setValue(UUID.randomUUID().toString());
              subscriptionForModule.setSubscriptionIdentifier(subId);
            }
            _endpointsBySubscriptionId.put(subId.getValue(), details);
          }
        }
        
        handleSubscriptionRequestWithResponse(request);
      }
    }
  }

  @PreDestroy
  public void stop() {
    super.stop();
    removeServiceDeliveryHandler(_handler);
  }

  @RequestMapping(value = "/siri.action")
  public void siri(Reader reader, Writer writer) {
    this.handleRawRequest(reader, writer);
  }

  private class ServiceDeliveryHandlerImpl implements
      SiriServiceDeliveryHandler {

    @Override
    public void handleServiceDelivery(ServiceDelivery serviceDelivery) {

      for (ESiriModuleType moduleType : ESiriModuleType.values()) {

        List<AbstractServiceDeliveryStructure> deliveriesForModule = SiriLibrary.getServiceDeliveriesForModule(
            serviceDelivery, moduleType);

        for (AbstractServiceDeliveryStructure deliveryForModule : deliveriesForModule) {

          SubscriptionQualifierStructure subscriptionRef = deliveryForModule.getSubscriptionRef();

          if (subscriptionRef == null || subscriptionRef.getValue() == null) {
            _log.warn("siri delivery without a subscription ref");
            continue;
          }

          SiriEndpointDetails endpoint = _endpointsBySubscriptionId.get(subscriptionRef.getValue());

          if (endpoint == null) {
            _log.warn("could not find siri delivery info with a subscription ref="
                + subscriptionRef.getValue());
            continue;
          }

          _siriService.handleServiceDelivery(serviceDelivery,
              deliveryForModule, moduleType, endpoint);
        }
      }
    }
  }
}