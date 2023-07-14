package com.itheima.reggie.filter;
import com.alibaba.fastjson.JSON;
import com.itheima.reggie.common.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.AntPathMatcher;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebFilter(filterName = "LoginCheckFilter", urlPatterns = "/*")
@Slf4j
public class LoginCheckFilter implements Filter {
    public static final AntPathMatcher PATH_MATCHER = new AntPathMatcher();

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        HttpServletResponse response = (HttpServletResponse) servletResponse;

        String requestURI = request.getRequestURI();

        String[] ignoreURLs = new String[] {
                "/employee/login",
                "/employee/logout",
                "/backend/**",
                "/front/**",
        };

        boolean letGo = letGo(ignoreURLs, requestURI);

        if (letGo) {
            filterChain.doFilter(request, response);
            return;
        }

        if (request.getSession().getAttribute("employee") != null) {
            filterChain.doFilter(request, response);
            return;
        }

        response.getWriter().write(JSON.toJSONString(R.error("user not login")));

    }
    private boolean letGo(String[] ignoreURLs, String requestURI) {
        for (String ignoreURL: ignoreURLs) {
            if (PATH_MATCHER.match(ignoreURL, requestURI)) return true;
        }
        return false;
    }
}
