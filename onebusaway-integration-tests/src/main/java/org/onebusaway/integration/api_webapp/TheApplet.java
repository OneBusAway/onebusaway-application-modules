package org.onebusaway.integration.api_webapp;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.JPanel;

import org.onebusaway.geospatial.model.CoordinateBounds;

public class TheApplet {

  private static DateFormat _formatA = new SimpleDateFormat(
      "yyyy_MM_dd-HH_mm_ss");

  private static DateFormat _formatB = DateFormat.getTimeInstance();

  private static NumberFormat _indexFormat = new DecimalFormat("000000");

  private static final Color[] COLORS = {
      Color.BLACK, Color.BLUE, Color.CYAN, Color.DARK_GRAY, Color.GRAY,
      Color.GREEN, Color.LIGHT_GRAY, Color.MAGENTA, Color.ORANGE, Color.PINK,
      Color.RED, Color.YELLOW};

  public static void main(String[] args) throws Exception {

    BufferedImage image = ImageIO.read(new File(
        "/Users/bdferris/Desktop/Seattle.png"));

    CoordinateBounds bounds = new CoordinateBounds(47.596737878383564,
        -122.38232162369592, 47.66418689295867, -122.24887344466346);

    BufferedImage output = new BufferedImage(640, 480,
        BufferedImage.TYPE_INT_RGB);
    Graphics2D g = output.createGraphics();

    Dimension dims = new Dimension(640, 480);

    MyPanel applet = new MyPanel();
    applet.setSize(dims);
    applet.setPreferredSize(dims);
    applet.setBackgroundImage(image);

    JFrame frame = new JFrame();
    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    frame.getContentPane().add(applet);
    frame.pack();

    frame.setVisible(true);

    File root = new File("/tmp/logs");
    int index = 0;

    for (File file : root.listFiles()) {

      Date date = _formatA.parse(file.getName().replaceAll(".csv", ""));
      String label = _formatB.format(date);

      BufferedReader reader = new BufferedReader(new FileReader(file));
      String line = null;

      Map<String, Point> points = new HashMap<String, Point>();

      while ((line = reader.readLine()) != null) {
        String[] tokens = line.split(",");
        String tripId = tokens[0];
        double lat = Double.parseDouble(tokens[1]);
        double lon = Double.parseDouble(tokens[2]);
        Point p = getAsPoint(bounds, dims, lat, lon);
        points.put(tripId, p);
      }

      applet.setVehicles(label, points);

      reader.close();

      applet.repaint();
      applet.paint(g);

      Thread.sleep(200);

      ImageIO.write(output, "PNG", new File("/Users/bdferris/Desktop/images/"
          + _indexFormat.format(index++) + ".png"));
    }

    System.exit(0);

  }

  private static Point getAsPoint(CoordinateBounds bounds, Dimension dim,
      double lat, double lon) {
    double rx = (lon - bounds.getMinLon())
        / (bounds.getMaxLon() - bounds.getMinLon());
    double ry = (lat - bounds.getMinLat())
        / (bounds.getMaxLat() - bounds.getMinLat());
    int x = (int) (rx * dim.width);
    int y = (int) (dim.height - ry * dim.height);
    return new Point(x, y);
  }

  private static class MyPanel extends JPanel {

    private static final long serialVersionUID = 1L;

    private BufferedImage _image;

    private String _label = "";

    private Map<String, Point> _vehicleLocations = new HashMap<String, Point>();

    private Map<String, Color> _tripColors = new HashMap<String, Color>();

    public void setBackgroundImage(BufferedImage image) {
      _image = image;
    }

    public synchronized void setVehicles(String label, Map<String, Point> points) {
      _label = label;
      _vehicleLocations = points;
    }

    @Override
    public synchronized void paint(Graphics g) {
      super.paint(g);

      g.drawImage(_image, 0, 0, null);

      for (Map.Entry<String, Point> entry : _vehicleLocations.entrySet()) {
        String tripId = entry.getKey();
        Point point = entry.getValue();
        g.setColor(Color.BLACK);
        g.drawOval(point.x, point.y, 7, 7);
        g.setColor(getColorForTrip(tripId));
        g.fillOval(point.x, point.y, 7, 7);
      }

      g.setColor(Color.BLACK);
      g.drawString(_label, 5, 470);
    }

    private Color getColorForTrip(String tripId) {
      Color color = _tripColors.get(tripId);
      if (color == null) {
        color = COLORS[(int) (Math.random() * COLORS.length)];
        _tripColors.put(tripId, color);
      }
      return color;
    }

  }

}
