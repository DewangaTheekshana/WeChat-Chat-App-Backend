/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/JSP_Servlet/Servlet.java to edit this template
 */
package controller;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import entity.User;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;
import model.HibernateUtil;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;

@MultipartConfig
@WebServlet(name = "ProfileUpdate", urlPatterns = {"/ProfileUpdate"})
public class ProfileUpdate extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        System.out.println("asd");
        Gson gson = new Gson();
        JsonObject responsejson = new JsonObject();
        responsejson.addProperty("message", "Error");

        String mobile = request.getParameter("mobile");
        String firstName = request.getParameter("firstName");
        String lastName = request.getParameter("lastName");
        Part avaterImage = request.getPart("avaterImage");
        System.out.println(avaterImage);

        if (mobile.isEmpty()) {
            responsejson.addProperty("message", "mobile is empty");
        } else if (firstName.isEmpty()) {
            responsejson.addProperty("message", "first Name is empty");
        } else if (lastName.isEmpty()) {
            responsejson.addProperty("message", "last Name is empty");
        } else {
            try {

                Session session = HibernateUtil.getSessionFactory().openSession();

                Criteria criteria1 = session.createCriteria(User.class);
                criteria1.add(Restrictions.eq("mobile", mobile));

                User user = (User) criteria1.uniqueResult();
                user.setFirst_name(firstName);
                user.setLast_name(lastName);

                if (avaterImage != null) {
                    System.out.println("bla");
                    String serverPath = request.getServletContext().getRealPath("");
                    String avatarImagePath = serverPath + File.separator + "AvatarImages" + File.separator + mobile + ".png";
                    System.out.println(avatarImagePath);
                    File file = new File(avatarImagePath);
                    Files.copy(avaterImage.getInputStream(), file.toPath(), StandardCopyOption.REPLACE_EXISTING);
                }

                session.update(user);
                responsejson.addProperty("success", true);
                responsejson.addProperty("message", "profile updated1");

//                System.out.println(user.getFirst_name());
                responsejson.add("user", gson.toJsonTree(user));
                session.beginTransaction().commit();
                session.close();
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
        response.setContentType("application/json");
        response.getWriter().write(gson.toJson(responsejson));

    }

}
