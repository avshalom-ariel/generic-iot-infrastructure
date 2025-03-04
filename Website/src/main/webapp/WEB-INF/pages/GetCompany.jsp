
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head>
    <title>Get Company Request</title>
    <style>
        <%@include file="/WEB-INF/css/styling.css"%>
    </style>
</head>
<body>
<br><br>
<h1 id="title"> Get Company Request</h1><br><br>
<form action="/GetCompany" method="GET">
    <label for="comp_id">Company ID:</label>
    <input type="number" id="comp_id" name="comp_id" required><br><br>
    <input class="options" type="submit" value="Submit">
    <a href="/" class="options">Go Home</a>
</form>
</body>
</html>
