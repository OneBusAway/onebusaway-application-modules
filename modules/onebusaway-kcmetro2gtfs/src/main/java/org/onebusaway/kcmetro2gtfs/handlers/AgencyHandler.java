package org.onebusaway.kcmetro2gtfs.handlers;

import org.onebusaway.gtfs.csv.CsvEntityWriter;
import org.onebusaway.gtfs.model.Agency;
import org.onebusaway.kcmetro2gtfs.TranslationContext;
import org.onebusaway.kcmetro2gtfs.model.MetroKCAgency;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import edu.emory.mathcs.backport.java.util.Collections;

public class AgencyHandler extends InputHandler {

  private static final String[] AGENCY_FIELDS = {"name", "url", "timezone"};

  private TranslationContext _context;

  public AgencyHandler(TranslationContext context) {
    super(MetroKCAgency.class, AGENCY_FIELDS);
    _context = context;
  }

  public void handleEntity(Object bean) {

    MetroKCAgency agency = (MetroKCAgency) bean;

    Agency a = new Agency();
    a.setId(_context.getAgencyId());
    a.setName(agency.getName());
    a.setTimezone(agency.getTimezone());
    a.setUrl(agency.getUrl());

    _context.addAgency(a);
  }

  public void close() {

    CsvEntityWriter writer = _context.getWriter();

    List<Agency> agencies = new ArrayList<Agency>(_context.getAgencies());
    Collections.sort(agencies, new AgencyComparator());

    for (Agency agency : agencies)
      writer.handleEntity(agency);
  }

  private static class AgencyComparator implements Comparator<Agency> {

    public int compare(Agency o1, Agency o2) {
      return o1.getId().compareTo(o2.getId());
    }

  }
}
