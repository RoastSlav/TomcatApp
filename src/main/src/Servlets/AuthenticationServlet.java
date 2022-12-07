package Servlets;

import DAO.UserDao;
import Models.User;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.io.Reader;
import java.util.Properties;
import java.util.regex.Pattern;

import static Utility.ServletUtility.*;
import static javax.servlet.http.HttpServletResponse.SC_FORBIDDEN;
import static javax.servlet.http.HttpServletResponse.SC_NOT_FOUND;

public class AuthenticationServlet extends HttpServlet {
    private static final Pattern REGISTER_PATTERN = Pattern.compile("\\/register");
    private static final Pattern LOGIN_PATTERN = Pattern.compile("\\/login");
    UserDao dao;

    @Override
    public void init() throws ServletException {
        Properties props = new Properties();
        try {
            props.load(Resources.getResourceAsReader("properties.properties"));
            String mBatisResource = props.getProperty("mb_resource");
            try (Reader reader = Resources.getResourceAsReader(mBatisResource)) {
                SqlSessionFactory sessionFactory = new SqlSessionFactoryBuilder().build(reader, props);
                dao = new UserDao(sessionFactory);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String pathInfo = req.getPathInfo();
        if (REGISTER_PATTERN.matcher(pathInfo).matches()) {
            registerUser(req);
            return;
        }

        if (LOGIN_PATTERN.matcher(pathInfo).matches()) {
            if (!loginUser(req))
                resp.sendError(SC_FORBIDDEN, "Invalid credentials");
            return;
        }

        resp.sendError(SC_NOT_FOUND);
    }

    private boolean loginUser(HttpServletRequest req) throws ServletException, IOException {
        String username = getValueFromPart("username", req);
        String password = getValueFromPart("password", req);
        String encryptedPass = encryptPassword(password);

        User user = dao.getUser(username);
        if (user != null && user.password.equals(encryptedPass)) {
            HttpSession session = req.getSession(true);
            session.setAttribute("userName", username);
            return true;
        }
        return false;
    }

    private void registerUser(HttpServletRequest req) throws IOException {
        String jsonBody = receiveJsonBody(req);
        User user = GSON.fromJson(jsonBody, User.class);
        user.password = encryptPassword(user.password);
        dao.createUser(user);
        HttpSession session = req.getSession();
        session.setAttribute("userName", user.username);
    }
}
