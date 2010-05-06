package org.onebusaway.gtfs_diff.impl.serialization;

import org.onebusaway.gtfs.csv.schema.BeanWrapper;
import org.onebusaway.gtfs.csv.schema.BeanWrapperFactory;
import org.onebusaway.gtfs.model.IdentityBean;
import org.onebusaway.gtfs_diff.model.ArrayIndexMatch;
import org.onebusaway.gtfs_diff.model.ArrayLengthMismatch;
import org.onebusaway.gtfs_diff.model.EntityMatch;
import org.onebusaway.gtfs_diff.model.EntityMismatch;
import org.onebusaway.gtfs_diff.model.EntityPropertyMatch;
import org.onebusaway.gtfs_diff.model.EntityPropertyMismatch;
import org.onebusaway.gtfs_diff.model.GtfsDifferences;
import org.onebusaway.gtfs_diff.model.MatchCollection;
import org.onebusaway.gtfs_diff.model.PotentialEntityMatch;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;

public class XStreamSourceImpl {

  public XStream createXStream() {

    XStream xstream = new XStream();
    xstream.setMode(XStream.NO_REFERENCES);

    xstream.alias("match-collection", MatchCollection.class);
    xstream.alias("gtfs-differences", GtfsDifferences.class);
    xstream.alias("entity-match", EntityMatch.class);
    xstream.alias("entity-property-match", EntityPropertyMatch.class);
    xstream.alias("potential-entity-match", PotentialEntityMatch.class);
    xstream.alias("array-index-match", ArrayIndexMatch.class);
    xstream.alias("entity-mismatch", EntityMismatch.class);
    xstream.alias("entity-property-mismatch", EntityPropertyMismatch.class);
    xstream.alias("array–length-mismatch", ArrayLengthMismatch.class);

    xstream.registerConverter(new MatchCollectionConverter());
    xstream.registerConverter(new EntityMatchConverter());
    xstream.registerConverter(new EntityPropertyMatchConverter());
    xstream.registerConverter(new PotentialEntityMatchConverter());
    xstream.registerConverter(new ArrayIndexMatchConverter());

    xstream.registerConverter(new EntityMismatchConverter());
    xstream.registerConverter(new EntityPropertyMismatchConverter());
    xstream.registerConverter(new IdentityBeanConverter());

    return xstream;
  }

  private class IdentityBeanConverter implements Converter {

    public void marshal(Object source, HierarchicalStreamWriter writer,
        MarshallingContext context) {
      IdentityBean<?> bean = (IdentityBean<?>) source;
      Object id = bean.getId();
      writer.addAttribute("entityId", id.toString());
      String type = source.getClass().getName();
      int index = type.lastIndexOf('.');
      if (index != -1)
        type = type.substring(index + 1);
      writer.addAttribute("entityType", type);
    }

    public Object unmarshal(HierarchicalStreamReader reader,
        UnmarshallingContext context) {
      throw new UnsupportedOperationException();
    }

    @SuppressWarnings("unchecked")
    public boolean canConvert(Class type) {
      return IdentityBean.class.isAssignableFrom(type);
    }
  }

  private class MatchCollectionConverter implements Converter {

    private Class<?> _type;

    public MatchCollectionConverter() {
      this(MatchCollection.class);
    }

    public MatchCollectionConverter(Class<?> type) {
      _type = type;
    }

    public void marshal(Object source, HierarchicalStreamWriter writer,
        MarshallingContext context) {
      marshalAttributes(source, writer, context);
      marshalNodes(source, writer, context);
    }

    public Object unmarshal(HierarchicalStreamReader reader,
        UnmarshallingContext context) {
      throw new UnsupportedOperationException();
    }

    @SuppressWarnings("unchecked")
    public boolean canConvert(Class type) {
      return type.equals(_type);
    }

    protected void marshalAttributes(Object source,
        HierarchicalStreamWriter writer, MarshallingContext context) {

    }

    protected void marshalNodes(Object source, HierarchicalStreamWriter writer,
        MarshallingContext context) {
      MatchCollection match = (MatchCollection) source;
      if (!match.getMatches().isEmpty()) {
        writer.startNode("matches");
        context.convertAnother(match.getMatches());
        writer.endNode();
      }
      if (!match.getMismatches().isEmpty()) {
        writer.startNode("mismatches");
        context.convertAnother(match.getMismatches());
        writer.endNode();
      }
    }
  }

  private class EntityMatchConverter extends MatchCollectionConverter {

    public EntityMatchConverter() {
      super(EntityMatch.class);
    }

    public EntityMatchConverter(Class<?> type) {
      super(type);
    }

    @Override
    protected void marshalAttributes(Object source,
        HierarchicalStreamWriter writer, MarshallingContext context) {
      super.marshalAttributes(source, writer, context);

      EntityMatch<?> match = (EntityMatch<?>) source;
      writer.addAttribute("entityA", value(match.getEntityA()));
      writer.addAttribute("entityB", value(match.getEntityB()));
    }
  }

  private class PotentialEntityMatchConverter extends EntityMatchConverter {

    public PotentialEntityMatchConverter() {
      super(PotentialEntityMatch.class);

    }

    @Override
    protected void marshalAttributes(Object source,
        HierarchicalStreamWriter writer, MarshallingContext context) {
      super.marshalAttributes(source, writer, context);

      PotentialEntityMatch<?> match = (PotentialEntityMatch<?>) source;
      writer.addAttribute("score", Double.toString(match.getScore()));
    }
  }

  private class ArrayIndexMatchConverter extends MatchCollectionConverter {

    public ArrayIndexMatchConverter() {
      super(ArrayIndexMatch.class);
    }

    @Override
    protected void marshalAttributes(Object source,
        HierarchicalStreamWriter writer, MarshallingContext context) {
      super.marshalAttributes(source, writer, context);
      ArrayIndexMatch match = (ArrayIndexMatch) source;
      writer.addAttribute("index", Integer.toString(match.getIndex()));
    }
  }

  private class EntityMismatchConverter extends MatchCollectionConverter {

    public EntityMismatchConverter() {
      super(EntityMismatch.class);
    }

    @Override
    protected void marshalAttributes(Object source,
        HierarchicalStreamWriter writer, MarshallingContext context) {
      super.marshalAttributes(source, writer, context);

      EntityMismatch match = (EntityMismatch) source;
      writer.addAttribute("entityA", value(match.getEntityA()));
      writer.addAttribute("entityB", value(match.getEntityB()));
    }
  }

  private class EntityPropertyMismatchConverter extends
      MatchCollectionConverter {

    public EntityPropertyMismatchConverter() {
      super(EntityPropertyMismatch.class);
    }

    @Override
    protected void marshalAttributes(Object source,
        HierarchicalStreamWriter writer, MarshallingContext context) {
      super.marshalAttributes(source, writer, context);

      EntityPropertyMismatch mismatch = (EntityPropertyMismatch) source;

      BeanWrapper a = wrapBean(mismatch.getEntityA());
      BeanWrapper b = wrapBean(mismatch.getEntityB());

      Object valueA = a.getPropertyValue(mismatch.getPropertyName());
      Object valueB = b.getPropertyValue(mismatch.getPropertyName());

      writer.addAttribute("propertyName", mismatch.getPropertyName());
      writer.addAttribute("entityA", value(valueA));
      writer.addAttribute("entityB", value(valueB));
    }
  }

  private class EntityPropertyMatchConverter extends MatchCollectionConverter {

    public EntityPropertyMatchConverter() {
      super(EntityPropertyMatch.class);
    }

    @Override
    protected void marshalAttributes(Object source,
        HierarchicalStreamWriter writer, MarshallingContext context) {
      super.marshalAttributes(source, writer, context);

      EntityPropertyMatch mismatch = (EntityPropertyMatch) source;

      BeanWrapper a = wrapBean(mismatch.getEntityA());
      BeanWrapper b = wrapBean(mismatch.getEntityB());

      Object valueA = a.getPropertyValue(mismatch.getPropertyName());
      Object valueB = b.getPropertyValue(mismatch.getPropertyName());

      writer.addAttribute("propertyName", mismatch.getPropertyName());
      writer.addAttribute("entityA", value(valueA));
      writer.addAttribute("entityB", value(valueB));
    }
  }

  private class ArrayLengthMismatchConverter extends MatchCollectionConverter {

    public ArrayLengthMismatchConverter() {
      super(ArrayLengthMismatch.class);
    }

    @Override
    protected void marshalAttributes(Object source,
        HierarchicalStreamWriter writer, MarshallingContext context) {
      super.marshalAttributes(source, writer, context);
      ArrayIndexMatch match = (ArrayIndexMatch) source;
      writer.addAttribute("index", Integer.toString(match.getIndex()));
    }
  }

  protected BeanWrapper wrapBean(Object bean) {
    return BeanWrapperFactory.wrap(bean);
  }

  protected String value(Object entity) {
    if (entity == null)
      return "null";
    return entity.toString();
  }
}
