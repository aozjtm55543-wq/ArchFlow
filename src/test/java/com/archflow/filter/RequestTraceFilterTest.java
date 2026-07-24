package com.archflow.filter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

class RequestTraceFilterTest {

    @Test
    void generatesRequestIdWhenClientDoesNotProvideOne() throws Exception {
        RequestTraceFilter filter = new RequestTraceFilter();
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(new MockHttpServletRequest(), response, (FilterChain) (request, servletResponse) -> { });

        assertNotNull(response.getHeader("X-Request-Id"));
    }

    @Test
    void returnsClientRequestIdWhenItIsValid() throws Exception {
        RequestTraceFilter filter = new RequestTraceFilter();
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("X-Request-Id", "portfolio-demo-001");
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, (FilterChain) (servletRequest, servletResponse) -> { });

        assertEquals("portfolio-demo-001", response.getHeader("X-Request-Id"));
    }
}
