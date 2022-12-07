package Servlets;

import DAO.PostDao;
import Models.Post;
import com.google.gson.stream.JsonReader;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static Utility.ServletUtility.*;
import static javax.servlet.http.HttpServletResponse.SC_BAD_REQUEST;
import static javax.servlet.http.HttpServletResponse.SC_FORBIDDEN;

public class PostsServlet extends HttpServlet {
    private static final Pattern COMMENTS_PATTERN = Pattern.compile("\\/comments\\?postId=(\\d+)");
    private static final Pattern POST_PATTERN = Pattern.compile("\\/(d+)");
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
            resp.sendError(SC_FORBIDDEN);
            return;
        }

        String pathInfo = req.getPathInfo();
        Matcher matcher = COMMENTS_PATTERN.matcher(pathInfo);
        if (matcher.matches()) {
            RequestDispatcher commentsServlet = req.getRequestDispatcher("/comments?postId=" + matcher.group(1));
            commentsServlet.forward(req, resp);
            return;
        }

        matcher = POST_PATTERN.matcher(pathInfo);
        if (matcher.matches()) {
            int i = Integer.parseInt(matcher.group(1));
            Post post = dao.getPost(i);
            sendJsonBody(resp, post);
            return;
        }

        Post[] allPosts = dao.getAllPosts();
        sendJsonBody(resp, allPosts);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if (!checkAuth(req)) {
            resp.sendError(SC_FORBIDDEN);
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
            resp.sendError(SC_FORBIDDEN);
            return;
        }

        String pathInfo = req.getPathInfo();
        if (pathInfo == null) {
            resp.sendError(SC_BAD_REQUEST);
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
            resp.sendError(SC_FORBIDDEN);
            return;
        }

        String pathInfo = req.getPathInfo();
        if (pathInfo == null) {
            resp.sendError(SC_BAD_REQUEST);
            return;
        }

        int id = Integer.parseInt(pathInfo.substring(1));
        dao.deletePost(id);
    }
}
