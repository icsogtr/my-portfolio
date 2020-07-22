// Copyright 2019 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     https://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.google.sps.servlets;

import com.google.appengine.api.datastore.*;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import java.io.IOException;
import com.google.appengine.api.datastore.Query.SortDirection;
import java.io.PrintWriter;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet("/home")
public class HomeServlet extends HttpServlet {

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    response.setContentType("text/html");
    PrintWriter out = response.getWriter();
    UserService userService = UserServiceFactory.getUserService();

    // If user is not logged in, show a login form (could also redirect to a login page)
    if (!userService.isUserLoggedIn()) {
      String loginUrl = userService.createLoginURL("/home");
      out.println("<p>Login <a href=\"" + loginUrl + "\">here</a>.</p>");
      return;
    }

    // If user has not set a nickname, redirect to nickname page
    //String nickname = getUserNickname(userService.getCurrentUser().getUserId());
    /*if (nickname == null) {
      response.sendRedirect("/nickname");
      return;
    }*/

    // User is logged in and has a nickname, so the request can proceed
    String logoutUrl = userService.createLogoutURL("/home");
    out.println("<h1>Home</h1>");
    //out.println("<p>Hello " + nickname + "!</p>");
    out.println("<p>Logout <a href=\"" + logoutUrl + "\">here</a>.</p>");
    //out.println("<p>Change your nickname <a href=\"/nickname\">here</a>.</p>");
    response.setContentType("text/html;");
    PrintWriter out2 = response.getWriter();
    out2.println("<h1>Shoutbox</h1>");

    // Only logged-in users can see the form
    userService = UserServiceFactory.getUserService();
    if (userService.isUserLoggedIn()) {
      out2.println("<p>Hello " + userService.getCurrentUser().getEmail() + "!</p>");
      out2.println("<p>Type a message and click submit:</p>");
      out2.println("<form method=\"POST\" action=\"/home\">");
      out2.println("<textarea name=\"text\"></textarea>");
      out2.println("<br/>");
      out2.println("<button>Submit</button>");
      out2.println("</form>");
    } else {
      String loginUrl = userService.createLoginURL("/home");
      out.println("<p>Login <a href=\"" + loginUrl + "\">here</a>.</p>");
    }

    // Everybody can see the messages
    out.println("<ul>");
    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    Query query = new Query("Message").addSort("timestamp", SortDirection.DESCENDING);
    PreparedQuery results = datastore.prepare(query);
    for (Entity entity : results.asIterable()) {
      String text = (String) entity.getProperty("text");
      String email = (String) entity.getProperty("email");
      out.println("<li>" + email + ": " + text + "</li>");
    }
    out.println("</ul>");
  }

  /** Returns the nickname of the user with id, or null if the user has not set a nickname. */
  private String getUserNickname(String id) {
    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    Query query =
        new Query("UserInfo")
            .setFilter(new Query.FilterPredicate("id", Query.FilterOperator.EQUAL, id));
    PreparedQuery results = datastore.prepare(query);
    Entity entity = results.asSingleEntity();
    if (entity == null) {
      return null;
    }
    String nickname = (String) entity.getProperty("nickname");
    return nickname;
  }
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {

    UserService userService = UserServiceFactory.getUserService();

    // Only logged-in users can post messages
    if (!userService.isUserLoggedIn()) {
      response.sendRedirect("/home");
      return;
    }

    String text = request.getParameter("text");
    String email = userService.getCurrentUser().getEmail();

    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    Entity messageEntity = new Entity("Message");
    messageEntity.setProperty("text", text);
    messageEntity.setProperty("email", email);
    messageEntity.setProperty("timestamp", System.currentTimeMillis());
    datastore.put(messageEntity);

    // Redirect to /shoutbox. The request will be routed to the doGet() function above.
    response.sendRedirect("/home");
  }
}