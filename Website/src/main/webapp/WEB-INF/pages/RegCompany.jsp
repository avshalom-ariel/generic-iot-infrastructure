
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head>
    <title>Register Company Request</title>
    <style>
        <%@include file="/WEB-INF/css/styling.css"%>
    </style>
</head>
<body>
<br><br>
<h1> Register Company Request</h1><br><br>
<form action="/RegCompany" method="POST">
    <label for="comp_name" style="font-size: 1.5em;">Company Name:</label>
    <input type="text" id="comp_name" name="comp_name" required><br><br>
    <label for="cont_name">Contact Name:</label>
    <input type="text" id="cont_name" name="cont_name" required><br><br>
    <label for="cont_num">Contact Number:</label>
    <input type="text" id="cont_num" name="cont_num" pattern="^05\d{8}$" placeholder="05XXXXXXXX" required><br><br>
    <label for="add">Address:</label>
    <input type="text" id="add" name="add" required><br><br>
    <label for="crd_card">Credit Card (16 digits):</label>
    <input type="text" id="crd_card" name="crd_card" pattern="^\d{16}$" placeholder="Enter 16-digit card number" required><br><br>
    <label for="exp_date">Expiry Date (mm/yy):</label>
    <input type="text" id="exp_date" name="exp_date" pattern="^(0[1-9]|1[0-2])\/\d{2}$" placeholder="MM/YY" required><br><br>
    <label for="sec_code">Security Code:</label>
    <input type="number" min="100" max="999" id="sec_code" name="sec_code" required><br><br>
    <input class="options" type="submit" value="Submit">
    <a href="/" class="options">Go Home</a>
</form>
</body>
</html>
