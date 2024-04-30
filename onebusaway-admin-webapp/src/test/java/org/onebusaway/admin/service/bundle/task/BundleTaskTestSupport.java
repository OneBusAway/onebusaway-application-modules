package org.onebusaway.admin.service.bundle.task;

import org.mockito.Mockito;
import org.onebusaway.admin.model.BundleBuildRequest;
import org.onebusaway.admin.model.BundleBuildResponse;
import org.onebusaway.admin.model.BundleRequestResponse;
import org.onebusaway.transit_data_federation.bundle.model.GtfsBundle;
import org.onebusaway.transit_data_federation.bundle.model.GtfsBundles;
import org.springframework.context.ApplicationContext;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class BundleTaskTestSupport {

    ApplicationContext context = Mockito.mock(ApplicationContext.class);
    BundleRequestResponse bundleRequestResponse = new BundleRequestResponse();

    List listOfBundles = new ArrayList();

    public BundleTaskTestSupport(){
        bundleSetup();
        requestResponseSetup();
    }

    public void requestResponseSetup(){
        BundleBuildRequest request = new BundleBuildRequest();
        bundleRequestResponse.setRequest(request);
        BundleBuildResponse response = new BundleBuildResponse();
        bundleRequestResponse.setResponse(response);
    }

    public void setConsolidate(boolean bool){
        bundleRequestResponse.getRequest().setConsolidateFlag(bool);
    }

    public void setTmpDir(String dir){
        if(dir == null){
            dir = System.getProperty("java.io.tmpdir");
        }
        bundleRequestResponse.getResponse().setBundleOutputDirectory(dir);
    }

    public BundleRequestResponse getBundleRequestResponse(){
        return bundleRequestResponse;
    }

    public void bundleSetup(){
        GtfsBundles gtfsBundles = new GtfsBundles();
        gtfsBundles.setBundles(listOfBundles);
        Mockito.when(context.getBean("gtfs-bundles")).thenReturn(gtfsBundles);
    }

    public void addBundle(File path, Map<String, String> agencyIdMappings,
                          String defaultAgencyId, URL url){
        //a number of these can be null when testing
        GtfsBundle bundle = new GtfsBundle();
        bundle.setPath(path);
        bundle.setAgencyIdMappings(agencyIdMappings);
        bundle.setDefaultAgencyId(defaultAgencyId);
        bundle.setUrl(url);
        listOfBundles.add(bundle);
    }

    public ApplicationContext getContext(){
        return context;
    }
}
