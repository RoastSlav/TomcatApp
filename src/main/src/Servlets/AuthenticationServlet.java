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
import java.io.PrintWriter;
import java.io.Reader;
import java.util.Properties;

public class AuthenticationServlet extends HttpServlet {
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
        String username = req.getParameter("username");
        String password = req.getParameter("password");

        User user = dao.getUser(username);
        if (user != null && user.password.equals(password)) {
            HttpSession session = req.getSession(true);
            session.setAttribute("userName", username);
            return;
        }

        PrintWriter writer = resp.getWriter();
        writer.println("Forbidden!");
    }
}
