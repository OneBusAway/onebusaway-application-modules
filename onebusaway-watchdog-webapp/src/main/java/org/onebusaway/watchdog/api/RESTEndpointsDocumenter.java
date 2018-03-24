/**
 * Copyright (C) 2015 Cambridge Systematics, Inc.
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
package org.onebusaway.watchdog.api;

import java.io.File;
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
 *  Borrowed from http://dalelane.co.uk/blog/?p=1871
 * 
 *  Borrowed from http://dalelane.co.uk/blog/?p=1871
 *
 * @author Dale Lane (dale.lane@gmail.com)
 */
public class RESTEndpointsDocumenter {

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
