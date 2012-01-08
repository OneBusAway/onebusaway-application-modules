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
package org.onebusaway.geospatial.services;

import static org.junit.Assert.assertEquals;

import org.onebusaway.geospatial.model.CoordinatePoint;
import org.onebusaway.geospatial.model.EncodedPolylineBean;

import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

public class PolylineEncoderTest {

  @Test
  public void testDecode() {
    String polyline = "mz{aHryriV???tE???jE??AlC???lF??AlF??AnF???fF???|F???P?N???lE???T?\\???xD???|@???nD???xB???L?zG???nG?V???jD??qAxC??i@vAKXGTABAJ?B?B??AfF?D@`A??BPBZBr@???d@@`B?V?d@???zBAf@Cb@E^AX??Ad@??AfB?|A??@~G???vF???zF??A`H??WzAUx@??]v@??GL??yAzCg@x@??GH_@b@??c@f@??mC~C??gBtBi@f@??ABMF??cAn@kAl@wE`C??_Aj@C@eAx@a@Z[T??YT]Vg@`@??OVKVGRI`@CVG~@??@fA???pC??@tE??@lB?zA?P??AbN???lL??@hK?vC???N???T??AzN???LAhF?T?fG???dH";
    List<CoordinatePoint> decode = PolylineEncoder.decode(polyline);
    assertEquals(157, decode.size());
    CoordinatePoint p = decode.get(0);
    assertEquals(47.661350000000006, p.getLat(), 1e-5);
    assertEquals(-122.32618000000001, p.getLon(), 1e-5);
  }

  @Test
  public void testEncodedAndDecodeSignedNumber() {

    String value = PolylineEncoder.encodeSignedNumber(10);
    assertEquals(10, PolylineEncoder.decodeSignedNumber(value));

    value = PolylineEncoder.encodeSignedNumber(-10);
    assertEquals(-10, PolylineEncoder.decodeSignedNumber(value));

    value = PolylineEncoder.encodeSignedNumber(123456);
    assertEquals(123456, PolylineEncoder.decodeSignedNumber(value));

    value = PolylineEncoder.encodeSignedNumber(-123456);
    assertEquals(-123456, PolylineEncoder.decodeSignedNumber(value));
  }

  @Test
  public void testEncodeAndDecodeNumber() {
    String result = PolylineEncoder.encodeNumber(10);
    assertEquals(10, PolylineEncoder.decodeNumber(result));

    result = PolylineEncoder.encodeNumber(4712345);
    assertEquals(4712345, PolylineEncoder.decodeNumber(result));
  }

  @Test
  public void testEncoder() {

    List<CoordinatePoint> points = new ArrayList<CoordinatePoint>();
    points.add(new CoordinatePoint(38.5, -120.2));
    points.add(new CoordinatePoint(40.7, -120.95));
    points.add(new CoordinatePoint(43.252, -126.453));

    String expected = "_p~iF~ps|U_ulLnnqC_mqNvxq`@";

    EncodedPolylineBean actual = PolylineEncoder.createEncodings(points, 0);
    Assert.assertEquals(expected, actual.getPoints());

    List<CoordinatePoint> decodedPoints = PolylineEncoder.decode(actual);
    GeospatialTestSupport.assertEqualsPointLists(points, decodedPoints, 1e-5);
  }

  @Test
  public void test2() {

    List<CoordinatePoint> points = new ArrayList<CoordinatePoint>();
    points.add(new CoordinatePoint(47.67839087880088, -122.27878118907307));
    points.add(new CoordinatePoint(47.67845871865856, -122.27342376951559));
    points.add(new CoordinatePoint(47.682076843204875, -122.2735240417865));

    String expected = "}d_bHlqiiVKo`@sUR";

    EncodedPolylineBean actual = PolylineEncoder.createEncodings(points, 0);
    Assert.assertEquals(expected, actual.getPoints());

    List<CoordinatePoint> decodedPoints = PolylineEncoder.decode(actual);
    GeospatialTestSupport.assertEqualsPointLists(points, decodedPoints, 1e-5);
  }

  @Test
  public void test3() {

    double[] lat = {47.67839087880088, 47.67845871865856, 47.682076843204875};
    double[] lon = {
        -122.27878118907307, -122.27342376951559, -122.2735240417865};

    String expected = "}d_bHlqiiVKo`@sUR";

    EncodedPolylineBean actual = PolylineEncoder.createEncodings(lat, lon, 0);
    Assert.assertEquals(expected, actual.getPoints());
  }

  @Test
  public void test4() {

    double[] lat = {
        47.3, 47.67839087880088, 47.67845871865856, 47.682076843204875, 47.4};
    double[] lon = {
        -122.3, -122.27878118907307, -122.27342376951559, -122.2735240417865,
        -122.4};

    String expected = "}d_bHlqiiVKo`@sUR";

    EncodedPolylineBean actual = PolylineEncoder.createEncodings(lat, lon, 1,
        3, 0);
    Assert.assertEquals(expected, actual.getPoints());
  }
}
