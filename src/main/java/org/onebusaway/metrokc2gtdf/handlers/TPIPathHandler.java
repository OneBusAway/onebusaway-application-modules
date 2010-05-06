package org.onebusaway.metrokc2gtdf.handlers;

import edu.washington.cs.rse.collections.FactoryMap;

import edu.emory.mathcs.backport.java.util.Collections;

import org.onebusaway.metrokc2gtdf.TranslationContext;
import org.onebusaway.metrokc2gtdf.model.MetroKCPatternTimepoint;
import org.onebusaway.metrokc2gtdf.model.ServicePatternKey;
import org.onebusaway.metrokc2gtdf.model.MetroKCTPIPath;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

public class TPIPathHandler extends InputHandler {

  private static final String[] TPI_PATH_FIELDS = {
      "id", "sequence", "effectiveDate", "trans_link", "dbModDate",
      "flow_direction", "status"};

  private PatternTimepointsHandler _ptHandler;

  private Map<Integer, List<MetroKCTPIPath>> _tpipsById = new FactoryMap<Integer, List<MetroKCTPIPath>>(
      new ArrayList<MetroKCTPIPath>());

  private Set<Integer> _transLinkIds = new HashSet<Integer>();

  public TPIPathHandler(TranslationContext context) {
    super(MetroKCTPIPath.class, TPI_PATH_FIELDS);
    _ptHandler = context.getHandler(PatternTimepointsHandler.class);
  }

  public boolean isTransLinkIdActive(int id) {
    return _transLinkIds.contains(id);
  }

  public List<MetroKCTPIPath> getTPIPathsById(int id) {
    if (!_tpipsById.containsKey(id))
      throw new IllegalStateException("no such tpipath id=" + id);
    return _tpipsById.get(id);
  }

  public SortedMap<MetroKCPatternTimepoint, List<MetroKCTPIPath>> getTPIPathsByServicePatternId(
      ServicePatternKey servicePatternId) {

    List<MetroKCPatternTimepoint> patternTimepoints = _ptHandler.getPatternTimepointsByServicePattern(servicePatternId);
    SortedMap<MetroKCPatternTimepoint, List<MetroKCTPIPath>> results = new TreeMap<MetroKCPatternTimepoint, List<MetroKCTPIPath>>();
    for (MetroKCPatternTimepoint pt : patternTimepoints) {
      List<MetroKCTPIPath> paths = pt.getTpiId() == 0 ? new ArrayList<MetroKCTPIPath>()
          : getTPIPathsById(pt.getTpiId());
      Collections.sort(paths);
      results.put(pt, paths);
    }
    return results;
  }

  public void handleEntity(Object bean) {

    MetroKCTPIPath tpip = (MetroKCTPIPath) bean;

    if (!_ptHandler.isActiveTPI(tpip.getId()))
      return;

    List<MetroKCTPIPath> tpips = _tpipsById.get(tpip.getId());
    tpips.add(tpip);

    _transLinkIds.add(tpip.getTransLink());
  }
}
