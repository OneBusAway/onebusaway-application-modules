package org.onebusaway.kcmetro2gtfs.impl;

import org.onebusaway.kcmetro2gtfs.TranslationContext;
import org.onebusaway.kcmetro2gtfs.handlers.TPIPathHandler;
import org.onebusaway.kcmetro2gtfs.model.MetroKCPatternTimepoint;
import org.onebusaway.kcmetro2gtfs.model.MetroKCTPIPath;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class TPIPModificationFactory implements ModificationFactory {

  public void register(List<Map<String, String>> configs, TranslationContext context) {

    final TPIPathModificationsImpl modifications = new TPIPathModificationsImpl();

    for (Map<String, String> config : configs) {

      TPIPathMod mod = new TPIPathMod();

      for (String servicePatternId : config.get("servicePatterns").split(","))
        mod.servicePatternIds.add(Integer.parseInt(servicePatternId));
      parseTPIPathProxies(config.get("tpipsIn"), mod.pathsIn);
      parseTPIPathProxies(config.get("tpipsOut"), mod.pathsOut);
      modifications.addMod(mod);
    }

    context.addContextListener(new TranslationContextListener() {
      public void onHandlerRegistered(Class<?> type, Object handler) {
        if (type.equals(TPIPathHandler.class)) {
          TPIPathHandler tpipathHandler = (TPIPathHandler) handler;
          tpipathHandler.addModification(modifications);
        }
      }
    });
  }

  private void parseTPIPathProxies(String line, List<TPIPathProxy> proxies) {
    for (String token : line.split(",")) {
      String[] kvp = token.split(":");
      if (kvp.length != 2)
        throw new IllegalStateException("invalid line=" + line);
      TPIPathProxy proxy = new TPIPathProxy();
      proxy.transLinkId = Integer.parseInt(kvp[0]);
      proxy.flowDirection = Integer.parseInt(kvp[1]);
      proxies.add(proxy);
    }
  }

  private class TPIPathModificationsImpl implements TPIPathModificationStrategy {

    private List<TPIPathMod> _mods = new ArrayList<TPIPathMod>();

    public void addMod(TPIPathMod mod) {
      _mods.add(mod);
    }

    public void modify(MetroKCPatternTimepoint patternTimepoint, List<MetroKCTPIPath> paths) {

      for (TPIPathMod mod : _mods) {

        if (!mod.servicePatternIds.contains(patternTimepoint.getId().getId()))
          continue;

        boolean hit = false;

        for (int i = 0; i < paths.size(); i++) {
          if (hasMatch(paths, i, mod.pathsIn)) {

            System.out.println("applying modification: " + patternTimepoint.getId());

            for (int x = 0; x < mod.pathsIn.size(); x++)
              paths.remove(i);
            for (int x = 0; x < mod.pathsOut.size(); x++) {
              TPIPathProxy proxy = mod.pathsOut.get(x);
              MetroKCTPIPath path = new MetroKCTPIPath();
              path.setId(-1);
              path.setSequence(-1);
              path.setTransLink(proxy.transLinkId);
              path.setFlowDirection(proxy.flowDirection);
              paths.add(i + x, path);
            }

            for (int x = 0; x < paths.size(); x++)
              paths.get(x).setSequence(x);

            hit = true;
          }
        }

        if (!hit) {
          System.out.println("hit!");
        }
      }
    }

    private boolean hasMatch(List<MetroKCTPIPath> paths, int index, List<TPIPathProxy> proxies) {

      if (index + proxies.size() > paths.size())
        return false;

      for (int i = 0; i < proxies.size(); i++) {
        MetroKCTPIPath a = paths.get(index + i);
        TPIPathProxy b = proxies.get(i);
        if (a.getTransLink() != b.transLinkId)
          return false;
        if (a.getFlowDirection() != b.flowDirection)
          return false;
      }

      return true;
    }
  }

  private static class TPIPathMod {
    Set<Integer> servicePatternIds = new HashSet<Integer>();
    List<TPIPathProxy> pathsIn = new ArrayList<TPIPathProxy>();
    List<TPIPathProxy> pathsOut = new ArrayList<TPIPathProxy>();
  }

  private class TPIPathProxy {
    int transLinkId;
    int flowDirection;
  }
}
