package test.server;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;


import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Enumeration;
import java.util.Locale;

public class LogFilter implements Filter {

    private static final Logger log = Logger.getLogger(LogFilter.class);

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {

    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        String remoteAddress = null;

        long start = 0;
        String url = "";
        String method = "";
        Throwable throwable = null;  // TODO fix exception case
        String agent = null;
        HttpServletRequest req = null;
        HttpServletResponse res = null;
        try {
            req = (HttpServletRequest) request;
            res = (HttpServletResponse) response;
            url = req.getRequestURI();
            method = req.getMethod();
            String qs = req.getQueryString();
            agent = req.getHeader("User-Agent");
            if (qs != null) {
                url += "?" + qs;
            }
            String forwarded = req.getHeader("X-Forwarded-For");
            if (forwarded != null) {
                remoteAddress = forwarded;
            }
            else {
                remoteAddress = request.getRemoteAddr();
            }

            StringBuilder msg = new StringBuilder();
            msg.append("request start ").append(method).append(" UA=").append(agent).
                    append(" url=").append(url).append(" remote=").append(remoteAddress);
            log.info(msg);
            start = System.nanoTime();

            chain.doFilter(request, response);
        }
        catch (Throwable e) {  // be sure to get all errors
            throwable = e;
            throw new ServletException(e);
        }
        finally {
            long time = System.nanoTime() - start;
            // try to get OID again at request end
            int statusCode = res.getStatus();
            StringBuilder msg = new StringBuilder();
            msg.append("request done ").
                    append(" code=").append(statusCode).
                    append(" time=").append(formatNanos(time)).append("ms");
            log.info(msg);
        }
    }

    @Override
    public void destroy() {

    }

    private String formatNanos(long nanos) {
        // show millis with 3 digits (i.e. us)
        return new DecimalFormat("#.###", DecimalFormatSymbols.getInstance(Locale.ENGLISH)).format(nanos / 1000000.0);
    }
}