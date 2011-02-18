package org.onebusaway.api.model.transit;

import java.io.Serializable;

public final class EntryWithReferencesBean<T> implements Serializable {

  private static final long serialVersionUID = 1L;

  private ReferencesBean references;

  private T entry;

  public EntryWithReferencesBean() {

  }

  public EntryWithReferencesBean(T entry, ReferencesBean references) {
    this.entry = entry;
    this.references = references;
  }

  public ReferencesBean getReferences() {
    return references;
  }

  public void setReferences(ReferencesBean references) {
    this.references = references;
  }

  public T getEntry() {
    return entry;
  }

  public void setEntry(T entry) {
    this.entry = entry;
  }

}
