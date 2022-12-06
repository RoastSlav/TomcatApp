package Utility;

import com.google.gson.Gson;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.BufferedReader;
import java.io.IOException;

public class ServletUtility {
    public static final Gson GSON = new Gson();
    private static final String JSON_CONTENT_TYPE = "application/json";

    public static String receiveJsonBody(HttpServletRequest req) throws IOException {
        BufferedReader reader = req.getReader();
        StringBuilder sb = new StringBuilder();
        char[] charBuffer = new char[128];
        while ((reader.read(charBuffer)) != -1) {
            sb.append(charBuffer);
        }
        return sb.toString();
    }

    public static void sendJsonBody(HttpServletResponse resp, Object body) throws IOException {
        resp.setContentType(JSON_CONTENT_TYPE);
        String serialized = GSON.toJson(body);
        resp.setContentLength(serialized.getBytes().length);
        ServletOutputStream outputStream = resp.getOutputStream();
        outputStream.print(serialized);
    }

    public static boolean checkAuth(HttpServletRequest req) {
        HttpSession session = req.getSession();
        return session.getAttribute("userName") != null;
    }
}
