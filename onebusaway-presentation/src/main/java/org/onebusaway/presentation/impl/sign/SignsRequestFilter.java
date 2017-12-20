package org.onebusaway.presentation.impl.sign;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;

public class SignsRequestFilter implements Filter {

    private static final String URL_LEGACY_SIGN_PATH = "/where/sign/stop.action";
    
    private static final String URL_SIGN_PATH = "/sign/sign";
   
    private static final String PARAM_LEGACY_STOP_ID = "id=";
    
    private static final String PARAM_STOP_ID = "stopIds=";
    
    private static final String PARAM_LEGACY_ROUTE_ID = "route=";
    
    private static final String PARAM_ROUTE_ID = "routeId=";

	@Override
	public void doFilter(ServletRequest request, ServletResponse response,
			FilterChain chain) throws IOException, ServletException {
		HttpServletRequestWrapper wrapper = new HttpServletRequestWrapper((HttpServletRequest)request);
        
		String url = wrapper.getRequestURL().toString();

        if(url.contains(URL_LEGACY_SIGN_PATH)) {
   
            String queryString = wrapper.getQueryString().toString();
        	
        	if(queryString.contains(PARAM_LEGACY_STOP_ID)){
        		queryString = StringUtils.replace(queryString, PARAM_LEGACY_STOP_ID, PARAM_STOP_ID);
        	}
        	if(queryString.contains(PARAM_LEGACY_ROUTE_ID)){
        		queryString = StringUtils.replace(queryString, PARAM_LEGACY_ROUTE_ID, PARAM_ROUTE_ID);
        	}

        	final String filtered = wrapper.getContextPath() + URL_SIGN_PATH + "?" + queryString;
        	
        	HttpServletResponse httpResponse = (HttpServletResponse) response;
        	httpResponse.sendRedirect(filtered);

        } else {
            chain.doFilter(wrapper, response);
        }
		
	}
	
	@Override
	public void init(FilterConfig filterConfig) throws ServletException {
	}
	
	@Override
	public void destroy() {
	}
}