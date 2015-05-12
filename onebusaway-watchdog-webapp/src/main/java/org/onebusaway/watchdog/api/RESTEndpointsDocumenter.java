package org.onebusaway.watchdog.api;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import javax.ws.rs.DELETE;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Request;

import org.apache.wink.common.annotations.Parent;

/**
 * Explores the Java classes in a given package, looking for annotations 
 *  indicating REST endpoints. These are written to an HTML table, documenting
 *  basic information about all the known endpoints.
 * 
 * @author Dale Lane (dale.lane@gmail.com)
 */
public class RESTEndpointsDocumenter {

    public static void main(String[] args){
        RESTEndpointsDocumenter red = new RESTEndpointsDocumenter();
        try {
            // the root package where Java classes implementing web services
            //  endpoints can be found - the place to start the search from
            String packagename = "uk.co.dalelane.myproject.rest";
            
            // the file location where the HTML table listing endpoints 
            //  information will be written
            // this should be in a directory that already exists, and contains
            //  the unzipped contents of 
            //  http://dalelane.co.uk/files/120114-datatables-assets.zip
            String destinationHtmlPath = "/path/to/my/doc/folder/index.html";
            
            List<Endpoint> endpoints = red.findRESTEndpoints(packagename);
            File endpointsDoc = red.outputEndpointsTable(endpoints, destinationHtmlPath);
            System.out.println("Complete. Written to " + endpointsDoc.getAbsolutePath());
        } 
        catch (IOException e) {
            e.printStackTrace();
        } 
        catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }
    
    
    /**
     * Writes the provided REST endpoints to an HTML file.
     */
    public File outputEndpointsTable(List<Endpoint> endpoints, String htmlpath) throws IOException {
        File docfile = new File(htmlpath);
        checkHtmlAssetFiles(docfile.getAbsoluteFile().getParentFile());
        
        FileWriter fstream = new FileWriter(docfile);
        BufferedWriter out = new BufferedWriter(fstream);
        
        out.write("<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.01//EN\" \"http://www.w3.org/TR/html4/strict.dtd\">" + NEWLINE);
        out.write("<html>");
        out.write("<head>");
        out.write("<style type=\"text/css\">" + NEWLINE);
        out.write("@import \"demo_page.css\";" + NEWLINE);
        out.write("@import \"header.ccss\";" + NEWLINE);
        out.write("@import \"demo_table.css\";" + NEWLINE);
        out.write("</style>" + NEWLINE);
        out.write("<script type=\"text/javascript\" charset=\"utf-8\" src=\"jquery.js\"></script>" + NEWLINE);
        out.write("<script type=\"text/javascript\" charset=\"utf-8\" src=\"jquery.dataTables.js\"></script>" + NEWLINE);
        out.write("<script type=\"text/javascript\" charset=\"utf-8\" src=\"FixedColumns.js\"></script>" + NEWLINE);
        out.write("<script type=\"text/javascript\" charset=\"utf-8\" src=\"RowGroupingWithFixedColumn.js\"></script>" + NEWLINE);
        out.write("</head>" + NEWLINE);
        out.write("<body id=\"dt_example\">" + NEWLINE);
        out.write("<table cellpadding=\"0\" cellspacing=\"0\" border=\"0\" class=\"display\" id=\"endpoints\">" + NEWLINE);
        out.write("<thead><tr><th>URI</th><th>REST</th><th>java class</th><th>method</th><th>parameters</th></tr></thead>" + NEWLINE);
        out.write("<tbody>" + NEWLINE);
        for (Endpoint endpoint : endpoints) {
            switch(endpoint.method){
                case GET:    out.write("<tr class='gradeA'>"); break;
                case PUT:    out.write("<tr class='gradeC'>"); break;
                case POST:   out.write("<tr class='gradeU'>"); break;
                case DELETE: out.write("<tr class='gradeX'>"); break;
                default: out.write("<tr>");
            }
            out.write("<td>" + endpoint.uri + "</td>");
            out.write("<td>" + endpoint.method + "</td>");
            out.write("<td>" + endpoint.javaClass + "</td>");
            out.write("<td>" + endpoint.javaMethodName + "</td>");
            out.write("<td>");
            for (EndpointParameter parameter : endpoint.pathParameters) {
                out.write("path {" + parameter.name + "}  (" + parameter.javaType + ")<br/>");                
            }
            for (EndpointParameter parameter : endpoint.queryParameters) {
                out.write("query: " + parameter.name + " (" + parameter.javaType + ") ");
                if (parameter.defaultValue != null){
                    out.write("default = \"" + parameter.defaultValue + "\"");
                }
                out.write("<br/>");
            }
            for (EndpointParameter parameter : endpoint.payloadParameters) {
                out.write("payload : " + parameter.javaType + "<br/>");
            }
            out.write("</td>");
            out.write("</tr>" + NEWLINE);
        }
        out.write("</tbody>");
        out.write("</table>");
        out.write("</body></html>");
        
        out.close();
        fstream.close();
        return docfile;
    }

    public StringBuffer outputEndpointsTable(List<Endpoint> endpoints, List<String> agencyIds, String apiBasePath) throws IOException {
      StringBuffer out = new StringBuffer();
      
      out.append("<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.01//EN\" \"http://www.w3.org/TR/html4/strict.dtd\">" + NEWLINE);
      out.append("<html>");
      out.append("<head>");
//      out.append("<style type=\"text/css\">" + NEWLINE);
//      out.append("@import \"demo_page.css\";" + NEWLINE);
//      out.append("@import \"header.ccss\";" + NEWLINE);
//      out.append("@import \"demo_table.css\";" + NEWLINE);
//      out.append("</style>" + NEWLINE);
//      out.append("<script type=\"text/javascript\" charset=\"utf-8\" src=\"jquery.js\"></script>" + NEWLINE);
//      out.append("<script type=\"text/javascript\" charset=\"utf-8\" src=\"jquery.dataTables.js\"></script>" + NEWLINE);
//      out.append("<script type=\"text/javascript\" charset=\"utf-8\" src=\"FixedColumns.js\"></script>" + NEWLINE);
//      out.append("<script type=\"text/javascript\" charset=\"utf-8\" src=\"RowGroupingWithFixedColumn.js\"></script>" + NEWLINE);
      out.append("</head>" + NEWLINE);
      out.append("<body id=\"dt_example\">" + NEWLINE);
      out.append("<table cellpadding=\"0\" cellspacing=\"0\" border=\"0\" class=\"display\" id=\"endpoints\">" + NEWLINE);
//      out.append("<thead><tr><th>URI</th><th>REST</th><th>java class</th><th>method</th><th>parameters</th></tr></thead>" + NEWLINE);
      out.append("<thead><tr><th>URI</th><th>REST</th><th>link</th></tr></thead>" + NEWLINE);
      out.append("<tbody>" + NEWLINE);
      for (Endpoint endpoint : endpoints) {
        for (String agencyId : agencyIds) {
          switch(endpoint.method){
              case GET:    out.append("<tr class='gradeA'>"); break;
              case PUT:    out.append("<tr class='gradeC'>"); break;
              case POST:   out.append("<tr class='gradeU'>"); break;
              case DELETE: out.append("<tr class='gradeX'>"); break;
              default: out.append("<tr>");
          }
          out.append("<td>" + endpoint.uri + "</td>");
          out.append("<td>" + endpoint.method + "</td>");
          String link = apiBasePath + endpoint.uri.replace("{agencyId}", agencyId);
          out.append("<td><a href=\"" + link + "\">" + link + "</a></td>");
//          out.append("<td>" + endpoint.javaClass + "</td>");
//          out.append("<td>" + endpoint.javaMethodName + "</td>");
          out.append("<td>");
          for (EndpointParameter parameter : endpoint.pathParameters) {
              out.append("path {" + parameter.name + "}  (" + parameter.javaType + ")<br/>");                
          }
          for (EndpointParameter parameter : endpoint.queryParameters) {
              out.append("query: " + parameter.name + " (" + parameter.javaType + ") ");
              if (parameter.defaultValue != null){
                  out.append("default = \"" + parameter.defaultValue + "\"");
              }
              out.append("<br/>");
          }
          for (EndpointParameter parameter : endpoint.payloadParameters) {
              out.append("payload : " + parameter.javaType + "<br/>");
          }
          out.append("</td>");
          out.append("</tr>" + NEWLINE);
        }
      }
      out.append("</tbody>");
      out.append("</table>");
      out.append("</body></html>");
      
      return out;
  }

    
    /**
     * Verifies that the JS and CSS files required by the HTML table are present.
     */
    private void checkHtmlAssetFiles(File directory) throws FileNotFoundException {
        if (directory.exists() == false){
            throw new FileNotFoundException(directory.getAbsolutePath());
        }
        File rowgroupingjs = new File(directory, "RowGroupingWithFixedColumn.js");
        if (rowgroupingjs.exists() == false){
            throw new FileNotFoundException(rowgroupingjs.getAbsolutePath());
        }
        File fixedcolumnsplugin = new File(directory, "FixedColumns.js");
        if (fixedcolumnsplugin.exists() == false){
            throw new FileNotFoundException(fixedcolumnsplugin.getAbsolutePath());
        }
        File pagecss = new File(directory, "demo_page.css");
        if (pagecss.exists() == false){
            throw new FileNotFoundException(pagecss.getAbsolutePath());
        }
        File tablecss = new File(directory, "demo_table.css");
        if (tablecss.exists() == false){
            throw new FileNotFoundException(tablecss.getAbsolutePath());
        }
        File headercss = new File(directory, "header.ccss");
        if (headercss.exists() == false){
            throw new FileNotFoundException(headercss.getAbsolutePath());
        }
        File datatablesjs = new File(directory, "jquery.dataTables.js");
        if (datatablesjs.exists() == false){
            throw new FileNotFoundException(datatablesjs.getAbsolutePath());
        }
        File jqueryjs = new File(directory, "jquery.js");
        if (jqueryjs.exists() == false){
            throw new FileNotFoundException(jqueryjs.getAbsolutePath());
        }
    }
    
    /**
     * Returns REST endpoints defined in Java classes in the specified package.
     */
    @SuppressWarnings("rawtypes")
    public List<Endpoint> findRESTEndpoints(String basepackage) throws IOException, ClassNotFoundException {
        List<Endpoint> endpoints = new ArrayList<Endpoint>();
        
        List<Class> classes = getClasses(basepackage);

        for (Class<?> clazz : classes) {
            Annotation annotation = clazz.getAnnotation(Path.class);
            if (annotation != null) {
                
                String basePath = getRESTEndpointPath(clazz);                    
                Method[] methods = clazz.getMethods();
                for (Method method : methods) {
                    if (method.isAnnotationPresent(GET.class)){
                        endpoints.add(createEndpoint(method, MethodEnum.GET, clazz, basePath));
                    }
                    else if (method.isAnnotationPresent(PUT.class)){
                        endpoints.add(createEndpoint(method, MethodEnum.PUT, clazz, basePath));                          
                    }
                    else if (method.isAnnotationPresent(POST.class)){
                        endpoints.add(createEndpoint(method, MethodEnum.POST, clazz, basePath));                           
                    }
                    else if (method.isAnnotationPresent(DELETE.class)){
                        endpoints.add(createEndpoint(method, MethodEnum.DELETE, clazz, basePath));
                    }
                }
            }
        }
        
        return endpoints;
    }
    
    
    /**
     * Create an endpoint object to represent the REST endpoint defined in the 
     *  specified Java method.
     */
    private Endpoint createEndpoint(Method javaMethod, MethodEnum restMethod, Class<?> clazz, String classUri){
        Endpoint newEndpoint = new Endpoint();
        newEndpoint.method = restMethod;
        newEndpoint.javaMethodName = javaMethod.getName();
        newEndpoint.javaClass = clazz.getName();
        
        Path path = javaMethod.getAnnotation(Path.class);
        if (path != null){
            newEndpoint.uri = classUri + path.value();
        }
        else {
            newEndpoint.uri = classUri;
        }
        discoverParameters(javaMethod, newEndpoint);
        return newEndpoint;
    }
    
    /**
     * Get the parameters for the specified endpoint from the provided java method.
     */
    @SuppressWarnings("rawtypes")
    private void discoverParameters(Method method, Endpoint endpoint){

        Annotation[][] annotations = method.getParameterAnnotations();
        Class[] parameterTypes = method.getParameterTypes();

        for (int i=0; i < parameterTypes.length; i++){
            Class parameter = parameterTypes[i];
            
            // ignore parameters used to access context
            if ((parameter == Request.class) || 
                (parameter == javax.servlet.http.HttpServletResponse.class) ||
                (parameter == javax.servlet.http.HttpServletRequest.class)){
                continue;
            }
            
            EndpointParameter nextParameter = new EndpointParameter();
            nextParameter.javaType = parameter.getName();
            
            Annotation[] parameterAnnotations = annotations[i];
            for (Annotation annotation : parameterAnnotations) {
                if (annotation instanceof PathParam){
                    nextParameter.parameterType = ParameterType.PATH;
                    PathParam pathparam = (PathParam)annotation;
                    nextParameter.name = pathparam.value();
                }
                else if (annotation instanceof QueryParam) {
                    nextParameter.parameterType = ParameterType.QUERY;
                    QueryParam queryparam = (QueryParam)annotation;
                    nextParameter.name = queryparam.value();
                }
                else if (annotation instanceof DefaultValue) {
                    DefaultValue defaultvalue = (DefaultValue)annotation;
                    nextParameter.defaultValue = defaultvalue.value();
                }
            }
            
            switch (nextParameter.parameterType){
                case PATH:
                    endpoint.pathParameters.add(nextParameter);
                    break;
                case QUERY:
                    endpoint.queryParameters.add(nextParameter);
                    break;
                case PAYLOAD:
                    endpoint.payloadParameters.add(nextParameter);
                    break;
            }
        }
    }
    
    /**
     * Get the REST endpoint path for the specified class. This involves 
     *  (recursively) looking for @Parent annotations and getting the path for
     *  that class before appending the location in the @Path annotation.
     */
    private String getRESTEndpointPath(Class<?> clazz){
        String path = "";
        while (clazz != null){            
            Annotation annotation = clazz.getAnnotation(Path.class);
            if (annotation != null){
                path = ((Path)annotation).value() + path;
            }
            
            Annotation parent = clazz.getAnnotation(Parent.class);
            if (parent != null){
                clazz = ((Parent)parent).value();
            }
            else {
                clazz = null;
            }
        }
        if (path.endsWith("/") == false){
            path = path + "/";
        }
        return path;
    }
    
    
    /**
     * Returns all of the classes in the specified package (including sub-packages).
     */
    @SuppressWarnings("rawtypes")
    private List<Class> getClasses(String pkg) throws IOException, ClassNotFoundException {
        ClassLoader classloader = Thread.currentThread().getContextClassLoader();
        // turn package into the folder equivalent
        String path = pkg.replace('.', '/');
        Enumeration<URL> resources = classloader.getResources(path);
        List<File> dirs = new ArrayList<File>();
        while (resources.hasMoreElements()) {
            URL resource = resources.nextElement();
            dirs.add(new File(resource.getFile()));
        }
        ArrayList<Class> classes = new ArrayList<Class>();
        for (File directory : dirs) {
            classes.addAll(getClasses(directory, pkg));
        }
        return classes;
    }

    /**
     * Returns a list of all the classes from the package in the specified
     *  directory. Calls itself recursively until no more directories are found. 
     */
    @SuppressWarnings("rawtypes")
    private List<Class> getClasses(File dir, String pkg) throws ClassNotFoundException {
        List<Class> classes = new ArrayList<Class>();
        if (!dir.exists()) {
            return classes;
        }
        File[] files = dir.listFiles();
        for (File file : files) {
            if (file.isDirectory()) {
                classes.addAll(getClasses(file, pkg + "." + file.getName()));
            } else if (file.getName().endsWith(".class")) {
                classes.add(Class.forName(pkg + '.' + file.getName().substring(0, file.getName().length() - 6)));
            }
        }
        return classes;
    }
    
    
    
    //
    // used to store the collection of attributes for a web services endpoint
    //
    
    static final String NEWLINE = System.getProperty("line.separator");
    
    enum MethodEnum { PUT, POST, GET, DELETE }
    enum ParameterType { QUERY, PATH, PAYLOAD }
    
    public class Endpoint {
        String uri;
        MethodEnum method;
        
        String javaClass;
        String javaMethodName;
        
        List<EndpointParameter> queryParameters = new ArrayList<RESTEndpointsDocumenter.EndpointParameter>();
        List<EndpointParameter> pathParameters = new ArrayList<RESTEndpointsDocumenter.EndpointParameter>();
        List<EndpointParameter> payloadParameters = new ArrayList<RESTEndpointsDocumenter.EndpointParameter>();        
    }

    public class EndpointParameter {
        ParameterType parameterType = ParameterType.PAYLOAD;
        String javaType;
        String defaultValue;
        String name;
    }
}
