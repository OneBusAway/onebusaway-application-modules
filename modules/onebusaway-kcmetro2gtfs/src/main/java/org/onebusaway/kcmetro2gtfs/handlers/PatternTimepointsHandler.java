/**
 * 
 */
package org.onebusaway.kcmetro2gtfs.handlers;

import org.onebusaway.kcmetro2gtfs.TranslationContext;
import org.onebusaway.kcmetro2gtfs.model.MetroKCPatternTimepoint;
import org.onebusaway.kcmetro2gtfs.model.ServicePatternKey;

import edu.washington.cs.rse.collections.FactoryMap;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class PatternTimepointsHandler extends InputHandler {

  private static final String[] PATTERN_TIMEPOINTS_FIELDS = {
      "service_pattern_id", "change_date", "sequence", "dbModDate",
      "timepoint_id", "tpi_id", "effectiveDate", "firstLastFlag"};

  private TranslationContext _context;

  private Set<Integer> _tpis = new HashSet<Integer>();

  private Map<ServicePatternKey, List<MetroKCPatternTimepoint>> _data = new FactoryMap<ServicePatternKey, List<MetroKCPatternTimepoint>>(
      new ArrayList<MetroKCPatternTimepoint>());

  public PatternTimepointsHandler(TranslationContext context) {
    super(MetroKCPatternTimepoint.class, PATTERN_TIMEPOINTS_FIELDS);
    _context = context;
  }

  public boolean isActiveTPI(int tpiId) {
    return _tpis.contains(tpiId);
  }

  public List<MetroKCPatternTimepoint> getPatternTimepointsByServicePattern(
      ServicePatternKey servicePatternId) {
    return _data.get(servicePatternId);
  }

  public Map<ServicePatternKey, List<MetroKCPatternTimepoint>> getData() {
    return _data;
  }

  public void handleEntity(Object bean) {

    MetroKCPatternTimepoint pt = (MetroKCPatternTimepoint) bean;
    ServicePatternKey servicePatternKey = pt.getId();
    ServicePatternHandler handler = _context.getHandler(ServicePatternHandler.class);

    if (!handler.containsKey(servicePatternKey))
      throw new IllegalStateException("unknown service pattern: "
          + servicePatternKey);

    _data.get(servicePatternKey).add(pt);
    _tpis.add(pt.getTpiId());
  }

}