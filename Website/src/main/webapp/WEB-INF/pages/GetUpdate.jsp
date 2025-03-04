<%--
  Created by IntelliJ IDEA.
  User: avshalom
  Date: 28/11/2024
  Time: 12:10
  To change this template use File | Settings | File Templates.
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head>
  <title>Get Product Request</title>
  <style>
    <%@include file="/WEB-INF/css/styling.css"%>
  </style>
</head>
<body>
  <br><br>
  <h1> Get Update Request</h1><br>
  <form action="/GetUpdate" method="GET">
    <label for="comp_id">Company ID:</label>
    <input type="number" id="comp_id" name="comp_id" required><br><br>
    <label for="prod_id">Product ID:</label>
    <input type="number" id="prod_id" name="prod_id" required><br><br>
    <label for="device_id">IOT Device ID:</label>
    <input type="number" id="device_id" name="device_id" required><br><br>
    <label for="update_id">Update ID:</label>
    <input type="number" id="update_id" name="update_id" required><br><br>
    <input class="options" type="submit" value="Submit">
    <a href="/" class="options">Go Home</a>

  </form>
</body>
</html>
