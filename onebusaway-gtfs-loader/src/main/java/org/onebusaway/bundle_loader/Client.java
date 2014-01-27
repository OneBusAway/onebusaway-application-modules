package org.onebusaway.bundle_loader;

import java.io.File;

import org.onebusaway.transit_data.services.TransitDataService;
import org.onebusaway.transit_data_federation.services.FederatedTransitDataBundle;
import org.onebusaway.transit_data_federation.services.transit_graph.TransitGraphDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class Client {

	private static TransitGraphDao transitGraph;
	
	public static void main(String[] args) {
		// eventually we would like the bundle_path to be an arg.  For now we can default to the spring config
		if (args.length > 1) {
			System.err.println("usage: basename bundle_path");
			return;
		}
		
		String bundleLocation = null;
		if (args.length > 0 && !args[0].startsWith("-D")) {
			bundleLocation = args[0];
			File bundleDirectory = new File(bundleLocation);
			if (!bundleDirectory.exists() || !bundleDirectory.isDirectory()) {
				System.err.println("invalid bundle directory provided: " + bundleLocation);
				return;
			}
		} else {
			System.err.println("using spring configured bundle location");
		}
		
		System.err.println("loading spring....");
		String[] files = {"data-sources.xml", "org/onebusaway/bundle_loader/application-context.xml"};
		ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext(files);
		System.err.println("spring loaded successfully!");
		
		
		TransitDataService tds = (TransitDataService) context.getBean("transitDataServiceImpl");
		if (tds == null) {
			System.err.println("Spring configuration error:  TDS not available");
			return;
		}
		
		transitGraph = (TransitGraphDao) context.getBean("transitGraphDaoImpl");

		if (transitGraph == null) {
			System.err.println("Spring configuration error:  transitGraph not provided");
			return;
		}

		// then load the bundle
		System.err.println("agencies=" + transitGraph.getAllAgencies());
		// then run the bundle stats 
		
		// then output results to file
		
	}
}
