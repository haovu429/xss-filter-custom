package com.xssFilter.filter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.xssFilter.model.ErrorResponse;
import com.xssFilter.utils.XSSValidationUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.List;
import java.util.Map;

@Component
public class ResponseFilter  implements Filter {

    ObjectMapper objectMapper = new ObjectMapper();


    @Value("#{'${skip_words}'.split(',')}")
    private List<String> skipWords;

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {

        RequestWrapper requestWrapper = new RequestWrapper((HttpServletRequest) servletRequest);

        String uri = requestWrapper.getRequestURI();
        System.out.println("getRequestURI : " + uri);
        // XSS:  Path Variable Validation
        if(!XSSValidationUtils.isValidURL(uri,skipWords)){
            ErrorResponse errorResponse = new ErrorResponse();

            errorResponse.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
            errorResponse.setMessage("XSS attack error");
            throw new ServletException(convertObjectToJson(errorResponse));
        }

        System.out.println("Response output: " + requestWrapper.getBody());
        if(!StringUtils.isEmpty(requestWrapper.getBody())) {

            // XSS:  Post Body data validation
            if (XSSValidationUtils.isValidURLPattern(requestWrapper.getBody(),skipWords)) {

                filterChain.doFilter(requestWrapper, servletResponse);
            } else {
                ErrorResponse errorResponse = new ErrorResponse();

                errorResponse.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
                errorResponse.setMessage("XSS attack error");
                throw new ServletException(convertObjectToJson(errorResponse));

            }
        }else{
            filterChain.doFilter(requestWrapper, servletResponse);
        }
    }


    public String convertObjectToJson(Object object) throws JsonProcessingException {
        if (object == null) {
            return null;
        }
        ObjectMapper mapper = new ObjectMapper();
        return mapper.writeValueAsString(object);
    }

}


