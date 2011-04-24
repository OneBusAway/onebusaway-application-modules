package org.onebusaway.transit_data_federation.bundle.tasks;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.transit_data_federation.bundle.services.EntityReplacementStrategy;
import org.onebusaway.transit_data_federation.services.AgencyAndIdLibrary;
import org.onebusaway.utility.IOLibrary;

/**
 * Factory for constructing {@link EntityReplacementStrategy} instances from
 * file data. You can setup entity mappings using the
 * {@link #setEntityMappings(Map)} to supply the mappings and then
 * {@link #create()} to generate the strategy instance.
 * 
 * What do the mappings look like? The {@link #setEntityMappings(Map)} method
 * accepts a map of class to file objects, where the class indicates the entity
 * type to perform replacement on and the file specifies the actual id mappings.
 * 
 * The file takes the following format:
 * 
 * <code>
 * # Comments are supported
 * agencyId_replacementEntityIdA agencyId_toBeReplacedEntityIdB [...]
 * agencyId_replacementEntityIdC agencyId_toBeReplacedEntityIdD [...]
 * </code>
 * 
 * Here entity ids are of the string serialized form of {@link AgencyAndId}, as
 * parsed by {@link AgencyAndIdLibrary}. The first entity on a line is the
 * replacement entity id, while all subsequent entities (separated by
 * whitespace) are entity ids that should be replaced with the replacement id.
 * 
 * @author bdferris
 * @see EntityReplacementStrategy
 * @see GtfsReadingSupport
 * @see GtfsMultiReaderImpl
 * @see EntityReplacementStrategyImpl
 */
public class EntityReplacementStrategyFactory {

  private Map<Class<?>, String> _mappings = new HashMap<Class<?>, String>();

  public void setEntityMappings(Map<Class<?>, String> mappings) {
    _mappings.putAll(mappings);
  }
  
  public void test() {
    
  }

  public EntityReplacementStrategy create() throws IOException {
    EntityReplacementStrategyImpl impl = new EntityReplacementStrategyImpl();
    for (Map.Entry<Class<?>, String> entry : _mappings.entrySet()) {

      Class<?> entityClass = entry.getKey();
      InputStream in = IOLibrary.getPathAsInputStream(entry.getValue());

      BufferedReader reader = new BufferedReader(new InputStreamReader(in));
      String line = null;

      while ((line = reader.readLine()) != null) {
        line = line.trim();
        if (line.length() == 0 || line.startsWith("#")
            || line.startsWith("{{{") || line.startsWith("}}}"))
          continue;
        String[] tokens = line.split("\\s+");
        List<AgencyAndId> ids = new ArrayList<AgencyAndId>();
        for (String token : tokens)
          ids.add(AgencyAndIdLibrary.convertFromString(token));
        for (int i = 1; i < ids.size(); i++)
          impl.addEntityReplacement(entityClass, ids.get(i), ids.get(0));
      }
    }
    return impl;
  }
}
