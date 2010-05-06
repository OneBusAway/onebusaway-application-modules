/**
 * 
 */
package org.onebusaway.kcmetro2gtfs.handlers;

import org.onebusaway.kcmetro2gtfs.model.MetroKCTransLink;

public class TransLinkHandler extends EntityHandler<Integer, MetroKCTransLink> {

  private static final String[] TRANS_LINK_FIELDS = {
      "id", "dbModDate", "street_name_id", "trans_node_from", "trans_node_to",
      "ignore=aboveBelowFlag", "addrLeftFrom", "addrLeftTo", "addrRightFrom",
      "addrRightTo", "countyClass", "ignore=hovFlag", "link_len",
      "ignore=transLinkStatus", "ignore=tigerClass", "ignore=trafficFlow",
      "ignore=transitFlag", "ignore=weightUsage", "zipLeft", "zipRight"};

  public TransLinkHandler() {
    super(MetroKCTransLink.class, TRANS_LINK_FIELDS);
  }

  public int getStreetNameId(int transLinkId) {
    MetroKCTransLink transLink = getEntity(transLinkId);
    return transLink.getStreetNameId();
  }
}