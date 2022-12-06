package Servlets;

import DAO.CommentDao;
import DAO.PostDao;
import Models.Comment;
import com.google.gson.Gson;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.Properties;

public class CommentsServlet extends HttpServlet {
    private static final String JSON_CONTENT_TYPE = "application/json";
    private static CommentDao dao;
    private static final Gson GSON = new Gson();

    @Override
    public void init() throws ServletException {
        Properties props = new Properties();
        try {
            props.load(Resources.getResourceAsReader("properties.properties"));
            String mBatisResource = props.getProperty("mb_resource");
            try (Reader reader = Resources.getResourceAsReader(mBatisResource)) {
                SqlSessionFactory sessionFactory = new SqlSessionFactoryBuilder().build(reader, props);
                dao = new CommentDao(sessionFactory);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType(JSON_CONTENT_TYPE);

        String userId = req.getParameter("postId");
        int id = Integer.parseInt(userId);

        Comment[] comment = dao.getComment(id);
        String serialized = GSON.toJson(comment);

        resp.setContentLength(serialized.getBytes().length);
        ServletOutputStream outputStream = resp.getOutputStream();
        outputStream.print(serialized);
    }
}
