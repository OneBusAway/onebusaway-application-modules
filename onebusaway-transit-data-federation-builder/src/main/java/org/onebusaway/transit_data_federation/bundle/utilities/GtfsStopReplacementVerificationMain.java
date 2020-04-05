/**
 * Copyright (C) 2011 Brian Ferris <bdferris@onebusaway.org>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.onebusaway.transit_data_federation.bundle.utilities;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.onebusaway.csv_entities.EntityHandler;
import org.onebusaway.gtfs.model.Agency;
import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.gtfs.model.Stop;
import org.onebusaway.gtfs.serialization.GtfsReader;
import org.onebusaway.transit_data_federation.bundle.model.GtfsBundle;
import org.onebusaway.transit_data_federation.bundle.tasks.EntityReplacementStrategyFactory;
import org.onebusaway.util.AgencyAndIdLibrary;
import org.onebusaway.utility.IOLibrary;

/**
 * Given a stop-consolidation list, verifies that the specified stops still
 * exist in the target GTFS feeds.
 * 
 * @author bdferris
 * @see EntityReplacementStrategyFactory
 */
public class GtfsStopReplacementVerificationMain {

  @SuppressWarnings("unchecked")
  public static void main(String[] args) throws IOException {

    if (args.length == 0) {
      System.err.println("usage: stop-consolidation-file gtfs_path [gtfs_path:defaultAgencyId data-sources.xml ...]");
      System.exit(-1);
    }

    EntityHandlerImpl handler = new EntityHandlerImpl();

    List<String> paths = new ArrayList<String>();
    for (int i = 1; i < args.length; i++) {
      paths.add(args[i]);
    }

    List<GtfsBundle> bundles = UtilityLibrary.getGtfsBundlesForArguments(paths);

    for (GtfsBundle bundle : bundles) {

      GtfsReader reader = new GtfsReader();
      reader.setDefaultAgencyId(bundle.getDefaultAgencyId());
      reader.setInputLocation(bundle.getPath());
      for (Map.Entry<String, String> entry : bundle.getAgencyIdMappings().entrySet())
        reader.addAgencyIdMapping(entry.getKey(), entry.getValue());
      reader.getEntityClasses().retainAll(
          Arrays.asList(Agency.class, Stop.class));
      reader.addEntityHandler(handler);
      reader.run();
    }

    Set<AgencyAndId> ids = handler.getIds();

    InputStream in = IOLibrary.getPathAsInputStream(args[0]);
    BufferedReader reader = new BufferedReader(new InputStreamReader(in));

    String line = null;

    while ((line = reader.readLine()) != null) {
      if (line.startsWith("#") || line.startsWith("{{{")
          || line.startsWith("}}}") || line.length() == 0)
        continue;
      String[] tokens = line.split("\\s+");
      AgencyAndId id = AgencyAndIdLibrary.convertFromString(tokens[0]);
      if (!ids.contains(id))
        System.out.println(id + " <- " + line);
    }

  }

  private static class EntityHandlerImpl implements EntityHandler {

    private Set<AgencyAndId> _ids = new HashSet<AgencyAndId>();

    public Set<AgencyAndId> getIds() {
      return _ids;
    }

    @Override
    public void handleEntity(Object bean) {
      if (!(bean instanceof Stop))
        return;
      Stop stop = (Stop) bean;
      _ids.add(stop.getId());
    }

  }
}
