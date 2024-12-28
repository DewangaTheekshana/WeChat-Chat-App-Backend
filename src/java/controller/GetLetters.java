package controller;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import entity.User;
import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import model.HibernateUtil;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;

@WebServlet(name = "GetLetters", urlPatterns = {"/GetLetters"})
public class GetLetters extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

        String mobile = req.getParameter("mobile");

        Gson gson = new Gson();
        JsonObject responseJson = new JsonObject();
        responseJson.addProperty("letters", "");

        Session session = HibernateUtil.getSessionFactory().openSession();

        Criteria criteria1 = session.createCriteria(User.class);
        criteria1.add(Restrictions.eq("mobile", mobile));

        if (!criteria1.list().isEmpty()) {
            //user found

            User user = (User) criteria1.uniqueResult();
            String letters = user.getFirst_name().charAt(0) + "" + user.getLast_name().charAt(0);
            responseJson.addProperty("letters", letters);

        }
        
        session.close();
        resp.setContentType("application/json");
        resp.getWriter().write(gson.toJson(responseJson));

    }
}
