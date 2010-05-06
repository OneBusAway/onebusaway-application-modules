import java.util.Properties;

public class PrintProperties {
  public static void main(String[] args) {
    Properties props = System.getProperties();
    for (Object key : props.keySet()) {
      Object value = props.get(key);
      System.out.println(key + "," + value);
    }
  }
}
