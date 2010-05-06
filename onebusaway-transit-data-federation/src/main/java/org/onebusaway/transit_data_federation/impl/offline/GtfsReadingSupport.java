package org.onebusaway.transit_data_federation.impl.offline;

import java.io.IOException;
import java.util.Map;

import org.onebusaway.gtfs.csv.schema.DefaultEntitySchemaFactory;
import org.onebusaway.gtfs.serialization.GtfsEntitySchemaFactory;
import org.onebusaway.gtfs.serialization.GtfsReader;
import org.onebusaway.gtfs.services.GenericMutableDao;
import org.onebusaway.transit_data_federation.bundle.model.GtfsBundle;
import org.onebusaway.transit_data_federation.bundle.model.GtfsBundles;
import org.onebusaway.transit_data_federation.services.offline.EntityReplacementStrategy;
import org.springframework.context.ApplicationContext;

public class GtfsReadingSupport {

  public static void readGtfsIntoStore(ApplicationContext context,
      GenericMutableDao store) throws IOException {
    readGtfsIntoStore(context, store,
        GtfsEntitySchemaFactory.createEntitySchemaFactory());
  }

  public static void readGtfsIntoStore(ApplicationContext context,
      GenericMutableDao store, DefaultEntitySchemaFactory factory)
      throws IOException {

    GtfsMultiReaderImpl multiReader = new GtfsMultiReaderImpl();
    multiReader.setStore(store);

    if (context.containsBean("entityReplacementStrategy")) {
      EntityReplacementStrategy strategy = (EntityReplacementStrategy) context.getBean("entityReplacementStrategy");
      multiReader.setEntityReplacementStrategy(strategy);
    }

    GtfsBundles gtfsBundles = getGtfsBundles(context);

    for (GtfsBundle gtfsBundle : gtfsBundles.getBundles()) {

      System.out.println("gtfs=" + gtfsBundle.getPath());

      GtfsReader reader = new GtfsReader();
      reader.setEntitySchemaFactory(factory);
      reader.setInputLocation(gtfsBundle.getPath());

      if (gtfsBundle.getDefaultAgencyId() != null)
        reader.setDefaultAgencyId(gtfsBundle.getDefaultAgencyId());

      for (Map.Entry<String, String> entry : gtfsBundle.getAgencyIdMappings().entrySet())
        reader.addAgencyIdMapping(entry.getKey(), entry.getValue());

      multiReader.addGtfsReader(reader);
    }

    multiReader.run();
  }

  private static GtfsBundles getGtfsBundles(ApplicationContext context) {

    GtfsBundles bundles = (GtfsBundles) context.getBean("gtfs-bundles");
    if (bundles != null)
      return bundles;

    GtfsBundle bundle = (GtfsBundle) context.getBean("gtfs-bundle");
    if (bundle != null) {
      bundles = new GtfsBundles();
      bundles.getBundles().add(bundle);
      return bundles;
    }

    throw new IllegalStateException(
        "must define either \"gtfs-bundles\" or \"gtfs-bundle\" in config");
  }
}
