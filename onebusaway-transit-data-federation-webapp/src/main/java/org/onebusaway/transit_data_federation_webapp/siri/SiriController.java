package org.onebusaway.transit_data_federation_webapp.siri;

import java.io.Reader;
import java.io.Writer;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.onebusaway.collections.CollectionsLibrary;
import org.onebusaway.siri.core.ESiriModuleType;
import org.onebusaway.siri.core.SiriChannelInfo;
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
import uk.org.siri.siri.ServiceDelivery;

@Controller
public class SiriController extends SiriClient {

  private static Logger _log = LoggerFactory.getLogger(SiriController.class);

  private SiriService _siriService;

  private ServiceDeliveryHandlerImpl _handler = new ServiceDeliveryHandlerImpl();

  private List<String> _endpoints;

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

        SiriEndpointDetails context = new SiriEndpointDetails();
        String agencyId = args.get("AgencyId");
        context.setAgencyId(agencyId);

        request.setChannelContext(context);

        handleSubscriptionRequest(request);
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
    public void handleServiceDelivery(SiriChannelInfo channelInfo,
        ServiceDelivery serviceDelivery) {

      SiriEndpointDetails endpoint = channelInfo.getContext();

      if (endpoint == null) {
        _log.warn("could not find siri delivery info");
        return;
      }

      for (ESiriModuleType moduleType : ESiriModuleType.values()) {

        List<AbstractServiceDeliveryStructure> deliveriesForModule = SiriLibrary.getServiceDeliveriesForModule(
            serviceDelivery, moduleType);

        for (AbstractServiceDeliveryStructure deliveryForModule : deliveriesForModule) {

          _siriService.handleServiceDelivery(serviceDelivery,
              deliveryForModule, moduleType, endpoint);
        }
      }
    }
  }
}