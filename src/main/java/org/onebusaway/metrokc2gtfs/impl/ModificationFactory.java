package org.onebusaway.metrokc2gtfs.impl;

import org.onebusaway.metrokc2gtfs.TranslationContext;

import java.util.List;
import java.util.Map;

public interface ModificationFactory {
  public void register(List<Map<String, String>> configs, TranslationContext context);
}
