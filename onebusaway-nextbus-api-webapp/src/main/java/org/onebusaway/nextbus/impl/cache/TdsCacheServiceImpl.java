package org.onebusaway.nextbus.impl.cache;

import org.onebusaway.nextbus.impl.util.ConfigurationMapUtil;
import org.onebusaway.nextbus.service.cache.CacheService;
import org.onebusaway.nextbus.service.cache.TdsCacheService;
import org.onebusaway.transit_data.model.AgencyBean;
import org.onebusaway.transit_data.model.StopBean;
import org.onebusaway.transit_data.services.TransitDataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class TdsCacheServiceImpl implements TdsCacheService {

    @Autowired
    protected ConfigurationMapUtil _configMapUtil;

    @Autowired
    protected TransitDataService _transitDataService;

    @Autowired
    private CacheService _cache;

    @Override
    public AgencyBean getCachedAgencyBean(String id) {
        if (_configMapUtil.getConfig(id) != null) {
            AgencyBean bean = _cache.getAgency(id);
            if (bean == null) {
                bean = _transitDataService.getAgency(id);
                if (bean != null)
                    _cache.putAgency(id, bean);
            }
            return bean;
        }
        return null;
    }

    @Override
    public StopBean getCachedStopBean(String id) {
        StopBean stop = _cache.getStop(id);
        if (stop == null) {
            if (_cache.isInvalidStop(id)) {
                return null;
            }

            try {
                stop = _transitDataService.getStop(id);
            } catch (Throwable t) {
                _cache.setInvalidStop(id);
            }
            if (stop != null) {
                _cache.putStop(id, stop);
            } else {
                _cache.setInvalidStop(id);
            }
        }
        return stop;
    }
}
