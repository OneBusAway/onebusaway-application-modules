/**
 * 
 */
package org.onebusaway.kcmetro_tcip.impl;

import org.onebusaway.kcmetro_tcip.model.TimepointPrediction;
import org.onebusaway.kcmetro_tcip.sdd.SddRecord;
import org.onebusaway.kcmetro_tcip.sdd.SddSchema;
import org.onebusaway.kcmetro_tcip.sdd.SddTable;

import its.backbone.sdd.ContentsParserException;
import its.backbone.sdd.MissingContentsException;
import its.backbone.sdd.MissingSchemaException;
import its.backbone.sdd.SchemaParserException;
import its.backbone.sdd.SddTransmitter;

import java.io.IOException;

public class MockPredictionTransmitter extends SddTransmitter {

  private static SddSchema _schema = createSchema();

  public MockPredictionTransmitter(int requestPort) throws IOException {
    super(requestPort);
    setSerialNumbers("20090707010300000");
  }

  public void sendSchema() throws SchemaParserException, ContentsParserException, MissingSchemaException {
    String schemaString = _schema.getSchemaAsString();
    transmitSchema(schemaString);
    transmitContents("");
  }

  public void sendData(TimepointPrediction prediction) throws ContentsParserException, MissingContentsException, MissingSchemaException {
    SddRecord record = new SddRecord(_schema.getTableForName("PREDICTIONS"));
    record.put("AGENCYID", prediction.getAgencyId());
    record.put("BLOCKID",prediction.getBlockId());
    record.put("TRIPID",prediction.getTrackerTripId());
    record.put("TIMEPOINTID",prediction.getTimepointId());
    record.put("SCHEDTIME",prediction.getScheduledTime());
    record.put("VID",prediction.getVehicleId());
    record.put("TIMESTRING","_");
    record.put("ABSTIME",0);
    record.put("DAYTIME",0);
    record.put("SPEED",0);
    record.put("DISTANCETOGOAL",0);
    record.put("PREDICTORTYPE",prediction.getPredictorType());
    record.put("GOALTIME",prediction.getGoalTime());
    record.put("GOALDEVIATION",prediction.getGoalDeviation());
    transmitData(record.getRecordAsString());
  }

  private static SddSchema createSchema() {
    SddTable table = new SddTable("PREDICTIONS");
    table.addColumn("AGENCYID", "INT");
    table.addColumn("BLOCKID", "INT");
    table.addColumn("TRIPID", "INT");
    table.addColumn("TIMEPOINTID", "INT");
    table.addColumn("SCHEDTIME", "INT");
    table.addColumn("EVENTTYPE", "INT");
    table.addColumn("VID", "INT");
    table.addColumn("TIMESTRING", "CHAR(20)");
    table.addColumn("ABSTIME", "INT");
    table.addColumn("DAYTIME", "INT");
    table.addColumn("SPEED", "FLOAT");
    table.addColumn("DISTANCETOGOAL", "INT");
    table.addColumn("PREDICTORTYPE", "CHAR");
    table.addColumn("GOALTIME", "INT");
    table.addColumn("GOALDEVIATION", "INT");
    SddSchema schema = new SddSchema();
    schema.addTable(table);
    return schema;
  }

}