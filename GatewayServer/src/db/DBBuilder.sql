



CREATE DATABASE IF NOT EXISTS AdminDB;

USE AdminDB;

CREATE TABLE IF NOT EXISTS Companies (
	Company_ID INT PRIMARY KEY AUTO_INCREMENT,
	Company_Name VARCHAR(50)
);

CREATE TABLE IF NOT EXISTS Contacts (
	Contact_ID INT PRIMARY KEY AUTO_INCREMENT,
	Company_ID INT,
	Contact_Name VARCHAR(50),
	Contact_Number VARCHAR(20),
	Address VARCHAR(50),
	Credit_Card VARCHAR(20),
	Expiry_Date DATE,
	Security_Code CHAR(3),
	FOREIGN KEY (Company_ID) REFERENCES Companies(Company_ID)
);

CREATE TABLE IF NOT EXISTS Products (
	Product_ID INT PRIMARY KEY AUTO_INCREMENT,
	Company_ID INT,
	Product_Name VARCHAR(50),
	Description VARCHAR(500),
	FOREIGN KEY (Company_ID) REFERENCES Companies(Company_ID)
);




