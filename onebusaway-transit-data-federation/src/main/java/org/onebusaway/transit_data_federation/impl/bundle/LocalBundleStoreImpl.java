package org.onebusaway.transit_data_federation.impl.bundle;

import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.gtfs.model.calendar.CalendarServiceData;
import org.onebusaway.gtfs.model.calendar.ServiceDate;
import org.onebusaway.transit_data_federation.model.bundle.BundleItem;
import org.onebusaway.transit_data_federation.services.bundle.BundleStoreService;
import org.onebusaway.utility.ObjectSerializationLibrary;

/*import org.joda.time.DateTime;*/
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * A bundle source backed by an on-disk directory.
 * 
 * @author jmaki
 *
 */
public class LocalBundleStoreImpl implements BundleStoreService {

	private static Logger _log = LoggerFactory.getLogger(LocalBundleStoreImpl.class);

	private String _bundleRootPath = null;

	public LocalBundleStoreImpl(String bundleRootPath) {
		_bundleRootPath = bundleRootPath;
	}

	@Override
	public List<BundleItem> getBundles() {
		ArrayList<BundleItem> output = new ArrayList<BundleItem>();

		File bundleRoot = new File(_bundleRootPath);

		if(!bundleRoot.isDirectory()) {    
			return output;
		}

		for(String filename : bundleRoot.list()) {
			File possibleBundle = new File(bundleRoot, filename);

			if(possibleBundle.isDirectory()) {
				File calendarServiceObjectFile = new File(possibleBundle, "CalendarServiceData.obj");

				if(!calendarServiceObjectFile.exists()) {
					_log.info("Could not find CalendarServiceData.obj in local bundle '" + filename + "'; skipping. Not a bundle?");
					continue;
				}

				// get data to fill in the BundleItem for this bundle.
				ServiceDate minServiceDate = null;
				ServiceDate maxServiceDate = null;

				try {
					CalendarServiceData data = 
							ObjectSerializationLibrary.readObject(calendarServiceObjectFile);

					// loop through all service IDs and find the minimum and max--most likely they'll all
					// be the same range, but not necessarily...
					for(AgencyAndId serviceId : data.getServiceIds()) {
						for(ServiceDate serviceDate : data.getServiceDatesForServiceId(serviceId)) {
							if(minServiceDate == null || serviceDate.compareTo(minServiceDate) <= 0) {
								minServiceDate = serviceDate;
							}

							if(maxServiceDate == null || serviceDate.compareTo(maxServiceDate) >= 0) {
								maxServiceDate = serviceDate;
							}
						}
					}             
				} catch(Exception e) {
					_log.info("Deserialization of CalendarServiceData.obj in local bundle " + filename + "; skipping.");
					continue;
				}        

				BundleItem validLocalBundle = new BundleItem();
				validLocalBundle.setId(filename);
				validLocalBundle.setName(filename);

				validLocalBundle.setServiceDateFrom(minServiceDate);
				validLocalBundle.setServiceDateTo(maxServiceDate);  

				/*validLocalBundle.setCreated(new DateTime());
				validLocalBundle.setUpdated(new DateTime());*/

				output.add(validLocalBundle);

				_log.info("Found local bundle " + filename + " with service range " + 
						minServiceDate + " => " + maxServiceDate);
			}
		}

		return output;
	}

}
