package Servlets;

import DAO.PostDao;
import Models.Post;
import com.google.gson.stream.JsonReader;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.Properties;

import static Utility.ServletUtility.*;

@WebServlet(name = "PostsServlet", urlPatterns = "/posts")
public class PostsServlet extends HttpServlet {
    private static PostDao dao;

    @Override
    public void init() throws ServletException {
        Properties props = new Properties();
        try {
            props.load(Resources.getResourceAsReader("properties.properties"));
            String mBatisResource = props.getProperty("mb_resource");
            try (Reader reader = Resources.getResourceAsReader(mBatisResource)) {
                SqlSessionFactory sessionFactory = new SqlSessionFactoryBuilder().build(reader, props);
                dao = new PostDao(sessionFactory);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if (!checkAuth(req)) {
            resp.sendError(403);
            return;
        }

        String pathInfo = req.getPathInfo();
        if (pathInfo == null) {
            Post[] allPosts = dao.getAllPosts();
            sendJsonBody(resp, allPosts);
            return;
        }

        if (pathInfo.endsWith("comments")) {
            String[] split = pathInfo.split("/");
            RequestDispatcher commentsServlet = req.getRequestDispatcher("/comments?postId=" + split[1]);
            commentsServlet.forward(req, resp);
        }

        int i = Integer.parseInt(pathInfo.substring(1));
        Post post = dao.getPost(i);
        sendJsonBody(resp, post);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if (!checkAuth(req)) {
            resp.sendError(403);
            return;
        }

        String json = receiveJsonBody(req);
        JsonReader reader = new JsonReader(new StringReader(json));
        reader.setLenient(true);
        Post post = GSON.fromJson(reader, Post.class);
        dao.addPost(post);
        sendJsonBody(resp, post);
    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if (!checkAuth(req)) {
            resp.sendError(403);
            return;
        }

        String pathInfo = req.getPathInfo();
        if (pathInfo == null) {
            resp.sendError(400);
            return;
        }

        String json = receiveJsonBody(req);
        Post post = GSON.fromJson(json, Post.class);
        post.id = Integer.parseInt(pathInfo.substring(1));
        dao.updatePost(post);
        sendJsonBody(resp, post);
    }

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if (!checkAuth(req)) {
            resp.sendError(403);
            return;
        }

        String pathInfo = req.getPathInfo();
        if (pathInfo == null) {
            resp.sendError(400);
            return;
        }

        int id = Integer.parseInt(pathInfo.substring(1));
        dao.deletePost(id);
    }
}
