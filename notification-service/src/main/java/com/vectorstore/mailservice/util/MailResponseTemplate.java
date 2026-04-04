package com.vectorstore.mailservice.util;

public class MailResponseTemplate {
    public String template(){
        return  """
<!DOCTYPE html>
<html>
<head>
    <style>
        body {
            font-family: Arial;
            background-color: #f4f4f4;
        }
        .container {
            background-color: white;
            padding: 20px;
            margin: 20px auto;
            width: 400px;
            border-radius: 10px;
            text-align: center;
        }
        .btn {
            background-color: #4CAF50;
            color: white;
            padding: 10px 20px;
            text-decoration: none;
            border-radius: 5px;
        }
        h2 {
            color: #333;
        }
    </style>
</head>
<body>
    <div class="container">
        <h2>Welcome to Our Service 🚀</h2>
        <p>Your account has been successfully created.</p>
        <a href="#" class="btn">Login Now</a>
    </div>
</body>
</html>
""";
    }
}
