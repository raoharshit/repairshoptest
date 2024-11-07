package com.repairshoptest.security;
import java.io.IOException;
import java.util.ArrayList;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.repairshoptest.exception.ResourceNotFoundException;
import com.repairshoptest.model.Clerk;
import com.repairshoptest.model.Customer;
import com.repairshoptest.model.RepairPerson;
import com.repairshoptest.model.User;
import com.repairshoptest.service.UserService;
import com.repairshoptest.utils.JwtUtil;

@Component
public class JwtAuthFilter extends OncePerRequestFilter {

    @Autowired
    private UserService userService;
    
    @Autowired 
    private JwtUtil jwtUtil;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        
    	String requestPath = request.getRequestURI();
        if (requestPath.equals("/login") || requestPath.equals("/register") || requestPath.equals("/forgotpassword") || requestPath.equals("/forgotpassword/verifyotp") || requestPath.equals("/forgotpassword/resendotp")) {
            filterChain.doFilter(request, response);
            return;
        }
    	String jwt = getJwtFromCookie(request);
        if (jwt != null && !jwtUtil.isTokenExpired(jwt)) {
            int userId = jwtUtil.extractUserId(jwt);
            User user;
            try {
            	user = userService.findById(userId);
            }catch(ResourceNotFoundException ex) {
            	response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid user");
            	return;
            }
            
            String requestUri = request.getRequestURI();
            if (requestUri.startsWith("/clerk") && !(user instanceof Clerk)) {
                response.sendError(HttpServletResponse.SC_FORBIDDEN, "User does not have access to Clerk resources.");
                return;
            } else if (requestUri.startsWith("/customer") && !(user instanceof Customer)) {
                response.sendError(HttpServletResponse.SC_FORBIDDEN, "User does not have access to Customer resources.");
                return;
            } else if (requestUri.startsWith("/repairperson") && !(user instanceof RepairPerson)) {
                response.sendError(HttpServletResponse.SC_FORBIDDEN, "User does not have access to Repair Person resources.");
                return;
            }
            
            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(Integer.toString(userId), null, new ArrayList<>());

            authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            SecurityContextHolder.getContext().setAuthentication(authentication);
        }else {
        	response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized user");
        	return ;
        }

            filterChain.doFilter(request, response);

        }
    
    
    private String getJwtFromCookie(HttpServletRequest request) {
        if (request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                if ("jwt".equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }
        return null;
    }

}