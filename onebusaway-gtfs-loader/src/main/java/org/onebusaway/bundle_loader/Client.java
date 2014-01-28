package org.onebusaway.bundle_loader;

import java.io.File;
import java.util.Collection;

import org.onebusaway.gtfs.model.Agency;
import org.onebusaway.gtfs.services.calendar.CalendarService;
import org.onebusaway.transit_data.services.TransitDataService;
import org.onebusaway.transit_data_federation.bundle.tasks.GtfsCsvLogger;
import org.onebusaway.transit_data_federation.services.ExtendedCalendarService;
import org.onebusaway.transit_data_federation.services.FederatedTransitDataBundle;
import org.onebusaway.transit_data_federation.services.transit_graph.AgencyEntry;
import org.onebusaway.transit_data_federation.services.transit_graph.TransitGraphDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.conveyal.gtfs.Statistic;

public class Client {

	private static Logger _log = LoggerFactory.getLogger(Client.class);
	private static TransitGraphDao transitGraph;
	
	public static void main(String[] args) {
		
		
		String basePath = System.getProperty("bundle.path");
		File bundleDirectory = new File(basePath);
		if (!bundleDirectory.exists() || !bundleDirectory.isDirectory()) {
			System.err.println("invalid bundle.path provided: " + basePath);
			return;
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

		ExtendedCalendarService ecsi = (ExtendedCalendarService) context.getBean("extendedCalendarServiceImpl");

		if (ecsi == null) {
			System.err.println("Spring configuration error:  calendar service not provided");
			return;
		}

		// then load the bundle
		System.err.println("agencies=" + transitGraph.getAllAgencies());
		// then run the bundle stats 
		
		BundleStatistics stats = new BundleStatistics();
		stats.setTrasitGraphDao(transitGraph);
		stats.setExtendedCalendarService(ecsi);
		GtfsCsvLogger csvLogger = new GtfsCsvLogger();
		csvLogger.setFilename("bundle-stats.csv");
		csvLogger.setBasePath(bundleDirectory);
		csvLogger.open();

		
		// per agency status
		Collection<AgencyEntry> agencies = transitGraph.getAllAgencies();
		for (AgencyEntry agency:agencies) {
			_log.info("processing stats for agency: " + agency.getId() + " (" + agency.toString() + ")");
			csvLogger.logStat(agency.getId(), stats.getStatistic(agency.getId()));
		}

		// overall stats/totals
		Statistic all = new Statistic();
		Agency allAgency = new Agency();
		allAgency.setId("TOTAL");
		all.setAgencyId("TOTAL");
		all.setRouteCount(stats.getRouteCount());
		all.setTripCount(stats.getTripCount());
		all.setStopCount(stats.getStopCount());
		all.setStopTimeCount(stats.getStopTimesCount());
		all.setCalendarStartDate(stats.getCalendarServiceRangeStart());
		all.setCalendarEndDate(stats.getCalendarServiceRangeEnd());

		csvLogger.logStat(allAgency.getId(), all);
		
		_log.info("cleaning up");
		// cleanup
		csvLogger.close();
		_log.info("exiting");
		
	}
}
