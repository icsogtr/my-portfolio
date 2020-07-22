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
// See the License for the specific language gover


package com.google.sps.servlets;

import com.google.appengine.api.datastore.*;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.SortDirection;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import java.io.IOException;
import com.google.sps.data.Task;
import java.util.Arrays;
import com.google.sps.data.ServerStats;
import com.google.gson.Gson;
import java.util.Date;
import java.io.PrintWriter;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.List;

/** Servlet that returns some example content. TODO: modify this file to handle comments data */
@WebServlet("/data")
public final class DataServlet extends HttpServlet {
    private final Date startTime = new Date();
  @Override
  
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    response.setContentType("text/html;");
    response.getWriter().println("Hello Chenxi!");




    ///*
    Date currentTime = new Date();
    long maxMemory = Runtime.getRuntime().maxMemory();
    long usedMemory = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();

    // Convert the server stats to JSON
    ServerStats serverStats = new ServerStats(startTime, currentTime, maxMemory, usedMemory);
    String json = convertToJson(serverStats);
    // Send the JSON as the response
    response.setContentType("application/json;");
    response.getWriter().println(json);


     Query query = new Query("Task").addSort("timestamp", SortDirection.DESCENDING);

    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    PreparedQuery results = datastore.prepare(query);

    List<Task> tasks = new ArrayList<>();
    for (Entity entity : results.asIterable()) {
      long id = entity.getKey().getId();
      String title = (String) entity.getProperty("title");
      long timestamp = (long) entity.getProperty("timestamp");

      Task task = new Task(id, title, timestamp);
      tasks.add(task);
    }

    Gson gson = new Gson();

    response.setContentType("application/json;");
    response.getWriter().println(gson.toJson(tasks));

    //*/
    

  }
  
  private String convertToJson(ServerStats serverStats) {
    String json = "{";
    
    json += "\"startTime\": ";
    json += "\"" + serverStats.getStartTime() + "\"";
    json += ", ";
    json += "\"currentTime\": ";
    json += "\"" + serverStats.getCurrentTime() + "\"";
    json += ", ";
    json += "\"maxMemory\": ";
    json += serverStats.getMaxMemory();
    json += ", ";
    json += "\"usedMemory\": ";
    json += serverStats.getUsedMemory();
    json += "}";
    return json;
  }

  
  private String convertToJsonUsingGson(ServerStats serverStats) {
    Gson gson = new Gson();
    String json = gson.toJson(serverStats);
    return json;
  }
   public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
    // Get the input from the form.
    
    String text = getParameter(request, "text-input", "");
    boolean upperCase = Boolean.parseBoolean(getParameter(request, "upper-case", "false"));
    boolean sort = Boolean.parseBoolean(getParameter(request, "sort", "false"));

    // Convert the text to upper case.
    if (upperCase) {
      text = text.toUpperCase();
    }

    // Break the text into individual words.
    String[] words = text.split("\\s*,\\s*");

    // Sort the words.
    if (sort) {
      Arrays.sort(words);
    }

    // Respond with the result.
    response.setContentType("text/html;");
    response.getWriter().println(Arrays.toString(words));

    String title = request.getParameter("title");
    long timestamp = System.currentTimeMillis();

    Entity taskEntity = new Entity("Task");
    taskEntity.setProperty("title", title);
    taskEntity.setProperty("timestamp", timestamp);

    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    datastore.put(taskEntity);
    response.sendRedirect("/index.html");
    
  }

  /**
   * @return the request parameter, or the default value if the parameter
   *         was not specified by the client
   */
  private String getParameter(HttpServletRequest request, String name, String defaultValue) {
    String value = request.getParameter(name);
    if (value == null) {
      return defaultValue;
    }
    return value;
  }
  }