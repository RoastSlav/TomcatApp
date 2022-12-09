package Filters;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

public class LoggingFilter implements Filter {
    private static final Logger LOGGER = Logger.getLogger("ServletLogger");
    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        try {
            FileHandler fileHandler = new FileHandler("TomCatPostLog.log", 1000000, 1, true);
            fileHandler.setFormatter(new SimpleFormatter());
            LOGGER.addHandler(fileHandler);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        long startTime = System.currentTimeMillis();
        HttpSession session = ((HttpServletRequest) request).getSession();
        String userName = (String) session.getAttribute("userName");

        chain.doFilter(request, response);

        long elapsedTime = System.currentTimeMillis() - startTime;
        int status = ((HttpServletResponse) response).getStatus();

        StringBuilder message = new StringBuilder();
        message.append("User: ").append(userName).append(" - ");
        message.append("Status: ").append(status).append(" - ");
        message.append("Elapsed Time: ").append(elapsedTime).append("ms").append(" - ");
        message.append("Path: ").append(((HttpServletRequest) request).getRequestURI());

        if (String.valueOf(status).startsWith("2"))
            LOGGER.log(Level.INFO, message.toString());
        else
            LOGGER.log(Level.WARNING, message.toString());
    }
}
