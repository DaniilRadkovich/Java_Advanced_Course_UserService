package com.innowise.userservice.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Slf4j
@Component
public class ApiLoggingInterceptor implements HandlerInterceptor {

  @Override
  public boolean preHandle(
      HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull Object handler) {
    request.setAttribute("startTime", System.currentTimeMillis());
    return true;
  }

  @Override
  public void afterCompletion(
      HttpServletRequest request,
      HttpServletResponse response,
      @NonNull Object handler,
      Exception ex) {
    long startTime = (Long) request.getAttribute("startTime");
    long duration = System.currentTimeMillis() - startTime;

    log.info(
        "API Operation: Method=[{}] | URL=[{}] | Status=[{}] | Duration=[{}ms]",
        request.getMethod(),
        request.getRequestURI(),
        response.getStatus(),
        duration);

    if (ex != null) {
      log.error("API Error happen during processing: ", ex);
    }
  }
}
