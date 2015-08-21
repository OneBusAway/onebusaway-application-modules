package org.onebusaway.presentation.impl;

import java.util.Comparator;

import org.onebusaway.transit_data.model.RouteBean;
import org.onebusaway.util.comparators.AlphanumComparator;

public class RouteComparator implements Comparator<RouteBean> {
	
	private Comparator<String> alphaNumComparator = new AlphanumComparator();

    @Override
    public int compare(RouteBean t, RouteBean t1) {
        if (t.getShortName() != null && t1.getShortName() != null) {
        	return alphaNumComparator.compare(t.getShortName(), t1.getShortName());
        } else {
            return t.getId().compareTo(t1.getId());
        }
    }
}