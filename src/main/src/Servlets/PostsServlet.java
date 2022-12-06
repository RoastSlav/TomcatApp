package Servlets;

import DAO.PostDao;
import Models.Post;
import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.Properties;

@WebServlet(name = "PostsServlet", urlPatterns = "/posts")
public class PostsServlet extends HttpServlet {
    private static final String JSON_CONTENT_TYPE = "application/json";
    private static PostDao dao;
    private static final Gson GSON = new Gson();

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
        String json = receiveJsonBody(req);
        JsonReader reader = new JsonReader(new StringReader(json));
        reader.setLenient(true);
        Post post = GSON.fromJson(reader, Post.class);
        dao.addPost(post);
        sendJsonBody(resp, post);
    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
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
        String pathInfo = req.getPathInfo();
        if (pathInfo == null) {
            resp.sendError(400);
            return;
        }

        int id = Integer.parseInt(pathInfo.substring(1));
        dao.deletePost(id);
    }

    private static String receiveJsonBody(HttpServletRequest req) throws IOException {
        BufferedReader reader = req.getReader();
        StringBuilder sb = new StringBuilder();
        char[] charBuffer = new char[128];
        while ((reader.read(charBuffer)) != -1) {
            sb.append(charBuffer);
        }
        return sb.toString();
    }

    private static void sendJsonBody(HttpServletResponse resp, Object body) throws IOException {
        resp.setContentType(JSON_CONTENT_TYPE);
        String serialized = GSON.toJson(body);
        resp.setContentLength(serialized.getBytes().length);
        ServletOutputStream outputStream = resp.getOutputStream();
        outputStream.print(serialized);
    }
}
