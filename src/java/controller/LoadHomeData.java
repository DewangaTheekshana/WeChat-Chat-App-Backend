package controller;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import entity.Chat;
import entity.User;
import entity.User_Status;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import model.HibernateUtil;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;

@WebServlet(name = "LoadHomeData", urlPatterns = {"/LoadHomeData"})
public class LoadHomeData extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        Gson gson = new Gson();

        JsonObject responseJson = new JsonObject();
        responseJson.addProperty("success", false);
        responseJson.addProperty("message", "Unable to process your request");

        try {

            Session session = HibernateUtil.getSessionFactory().openSession();

            //get user id from reqest parameter
            String userId = request.getParameter("id");

            User user = (User) session.get(User.class, Integer.parseInt(userId));
            
            //// get User Status 1 = (Online)
            User_Status user_Status = (User_Status) session.get(User_Status.class, 1);

            //update user Status
            user.setUser_Status(user_Status);
            session.update(user);

            //get Othor Users
            
            Criteria criteria1 = session.createCriteria(User.class);
            criteria1.add(Restrictions.ne("id", user.getId()));

            List<User> otherUserList = criteria1.list();

            JsonArray jsonChatArray = new JsonArray();
            //get other user one by one
            for (User otherUser : otherUserList) {

                //get Last convercation
                Criteria criteria2 = session.createCriteria(Chat.class);
                criteria2.add(
                        Restrictions.or(
                                Restrictions.and(
                                        Restrictions.eq("from_user", user),
                                        Restrictions.eq("to_user", otherUser)
                                ),
                                Restrictions.and(
                                        Restrictions.eq("from_user", otherUser),
                                        Restrictions.eq("to_user", user)
                                )
                        )
                );

                criteria2.addOrder(Order.desc("id"));
                criteria2.setMaxResults(1);

                //create chat item to send frontend data
                JsonObject jsonChatItem = new JsonObject();
                jsonChatItem.addProperty("other_user_id", otherUser.getId());
                jsonChatItem.addProperty("other_user_mobile", otherUser.getMobile());
                jsonChatItem.addProperty("other_user_name", otherUser.getFirst_name() + " " + otherUser.getLast_name());
                jsonChatItem.addProperty("other_user_status", otherUser.getUser_Status().getId());  //1 = online,2 = offline

                //check avetar images
                String serverPath = request.getServletContext().getRealPath("");
                String otherUserAvetarImagePath = serverPath + File.separator + "AvatarImages" + File.separator + otherUser.getMobile() + ".png";
                File otherUserAvatarImageFile = new File(otherUserAvetarImagePath);

                if (otherUserAvatarImageFile.exists()) {
                    //avetar image not found
                    jsonChatItem.addProperty("avatar_image_found", true);
                } else {
                    //aveter image not found
                    jsonChatItem.addProperty("avatar_image_found", false);
                    jsonChatItem.addProperty("other_user_avatar_letters", otherUser.getFirst_name().charAt(0) + "" + otherUser.getLast_name().charAt(0));
                }

                //get Chat List
                List<Chat> dbChatList = criteria2.list();
                SimpleDateFormat dateFormat = new SimpleDateFormat("hh:mm a");

                if (dbChatList.isEmpty()) {
                    //no chat 
                    jsonChatItem.addProperty("message", "Let's Start New Convercation");
                    jsonChatItem.addProperty("datetime", "");
                    jsonChatItem.addProperty("chat_status_id", 1); //1 = seen,2 = unseen

                } else {
                    //found last chat 
                    jsonChatItem.addProperty("message", dbChatList.get(0).getMessage());
                    jsonChatItem.addProperty("datetime", dateFormat.format(dbChatList.get(0).getDate_time()));
                    jsonChatItem.addProperty("chat_status_id", dbChatList.get(0).getChat_Status().getId());
                }

                //get Last Converstion
                jsonChatArray.add(jsonChatItem);
                
            }

            //send Users
            responseJson.addProperty("success", true);
            responseJson.addProperty("message", "Success");
            responseJson.add("jsonChatArray", gson.toJsonTree(jsonChatArray));

            session.beginTransaction().commit();
            session.close();

        } catch (Exception e) {

        }

        response.setContentType("application/json");
        response.getWriter().write(gson.toJson(responseJson));
    }

}
