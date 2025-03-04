<%--
  Created by IntelliJ IDEA.
  User: avshalom
  Date: 10/11/2024
  Time: 16:55
  To change this template use File | Settings | File Templates.
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head>
    <title>Register Product Request</title>
    <style>
        <%@include file="/WEB-INF/css/styling.css"%>
    </style>
</head>
<body>

<br><br>
<h1> Register Product Request</h1><br><br>

<form action="/RegProduct" method="POST" >
    <label for="comp_id">Company ID:</label>
    <input type="number" id="comp_id" name="comp_id" required><br><br>
    <label for="prod_name">Product Name:</label>
    <input type="text" id="prod_name" name="prod_name" required><br><br>
<%--    <textarea id="myTextarea" name="myTextarea" maxlength="300" rows="4" cols="50"></textarea>--%>
<%--    <label for="desc">Description:></label>--%>
    <label for="desc">Description (max 300 chars):</label><br>
    <textarea id="desc" name="desc" maxlength="300" rows="4" cols="50" required></textarea><br><br>
    <input class="options" type="submit" value="Submit">
    <a href="/" class="options">Go Home</a>
</form>
</body>
</html>
