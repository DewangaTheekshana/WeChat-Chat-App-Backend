/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/JSP_Servlet/Servlet.java to edit this template
 */
package controller;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import entity.Chat;
import java.io.IOException;
import java.io.PrintWriter;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import model.HibernateUtil;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;

/**
 *
 * @author Admin
 */
@WebServlet(name = "DeleteMessage", urlPatterns = {"/DeleteMessage"})
public class DeleteMessage extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        System.out.println("hi hi hi");

        int id = Integer.parseInt(request.getParameter("id"));
        String side = request.getParameter("side");
        String msg = request.getParameter("msg");
        
        System.out.println(id);
        System.out.println(side);
        System.out.println(msg);

        Session session = HibernateUtil.getSessionFactory().openSession();
        Gson gson = new Gson();
        JsonObject responseJson = new JsonObject();

        if (side.equals("right")) {

            if (msg.equals("You deleted this message")) {
                Criteria criteria1 = session.createCriteria(Chat.class);
                criteria1.add(Restrictions.eq("id", id));

                Chat chat = (Chat) criteria1.uniqueResult();
                session.delete(chat);
                responseJson.addProperty("message", "delete");
                responseJson.addProperty("success", true);

            } else {
                Criteria criteria2 = session.createCriteria(Chat.class);
                criteria2.add(Restrictions.eq("id", id));

                Chat chat2 = (Chat) criteria2.uniqueResult();
                chat2.setMessage("You deleted this message");
                session.update(chat2);
                responseJson.addProperty("message", "updeted");
                responseJson.addProperty("success", true);

            }

        } else {

            Criteria criteria3 = session.createCriteria(Chat.class);
            criteria3.add(Restrictions.eq("id", id));

            Chat chat = (Chat) criteria3.uniqueResult();
            session.delete(chat);
            responseJson.addProperty("message", "other user delete");
            responseJson.addProperty("success", true);
        }

        session.beginTransaction().commit();
        session.close();

        response.setContentType("application/json");
        response.getWriter().write(gson.toJson(responseJson));

    }

}