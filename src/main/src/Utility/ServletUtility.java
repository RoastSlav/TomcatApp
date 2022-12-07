package Utility;

import com.google.gson.Gson;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.http.Part;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

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

    public static String getValueFromPart(String partName, HttpServletRequest req) throws ServletException, IOException {
        Part part = req.getPart(partName);
        StringBuilder sb = new StringBuilder();

        InputStream inputStream = part.getInputStream();
        while (inputStream.available() != 0)
            sb.append((char) inputStream.read());

        return sb.toString();
    }

    public static String encryptPassword(String password) {
        MessageDigest crypt = null;
        try {
            crypt = MessageDigest.getInstance("SHA-1");
        } catch (NoSuchAlgorithmException ignored) {
            return password;
        }

        crypt.reset();
        crypt.update(password.getBytes());

        byte[] digest = crypt.digest();
        StringBuilder sb = new StringBuilder();
        for (byte b : digest) {
            sb.append((char) b);
        }
        return sb.toString();
    }
}
