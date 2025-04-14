import java.io.*;
import java.sql.*;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import jakarta.servlet.annotation.WebServlet;

@WebServlet("/select")
public class SelectServlet extends HttpServlet {
    
    // Database connection parameters
    private static final String JDBC_DRIVER = "com.mysql.jdbc.Driver";
    private static final String DB_URL = "jdbc:mysql://localhost:3306/clicker";
    private static final String USER = "myuser";
    private static final String PASS = "xxxx";
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        response.setContentType("text/html");
        PrintWriter out = response.getWriter();
        
        // Get the choice and questionNo parameters from the request
        String choice = request.getParameter("choice");
        String questionNoParam = request.getParameter("questionNo");
        int questionNo = 1; // Default to question 1
        
        if (questionNoParam != null && !questionNoParam.isEmpty()) {
            try {
                questionNo = Integer.parseInt(questionNoParam);
                // Ensure questionNo is a positive number
                if (questionNo < 1) questionNo = 1;
            } catch (NumberFormatException e) {
                // Invalid number, use default
            }
        }
        
        // JDBC variables
        Connection conn = null;
        PreparedStatement pstmt = null;
        
        try {
            // Register JDBC driver
            Class.forName(JDBC_DRIVER);
            
            // Open a connection
            conn = DriverManager.getConnection(DB_URL, USER, PASS);
            
            // First, get the question text to display
            String questionQuery = "SELECT questionText FROM questions WHERE questionNo = ?";
            pstmt = conn.prepareStatement(questionQuery);
            pstmt.setInt(1, questionNo);
            ResultSet questionRs = pstmt.executeQuery();
            
            String questionText = "Question not found";
            if (questionRs.next()) {
                questionText = questionRs.getString("questionText");
            }
            questionRs.close();
            pstmt.close();
            
            // Execute SQL query to insert response
            String insertSql = "INSERT INTO responses (questionNo, choice) VALUES (?, ?)";
            pstmt = conn.prepareStatement(insertSql);
            pstmt.setInt(1, questionNo);
            pstmt.setString(2, choice);
            int count = pstmt.executeUpdate();
            pstmt.close();
            
            // Display results with improved styling
            out.println("<!DOCTYPE html>");
            out.println("<html>");
            out.println("<head>");
            out.println("<title>Response Recorded</title>");
            out.println("<style>");
            out.println("body { font-family: Arial, sans-serif; margin: 20px; text-align: center; }");
            out.println("h2 { color: #2c3e50; }");
            out.println(".container { background-color: #f9f9f9; padding: 20px; border-radius: 10px; max-width: 600px; margin: 0 auto; }");
            out.println(".success-message { color: #27ae60; margin-bottom: 20px; }");
            out.println(".detail { margin: 10px 0; padding: 10px; background-color: #eee; border-radius: 5px; }");
            out.println(".navigation { margin-top: 30px; }");
            out.println(".navigation a { padding: 10px 20px; background-color: #3498db; color: white; text-decoration: none; border-radius: 5px; }");
            out.println(".navigation a:hover { background-color: #2980b9; }");
            out.println("</style>");
            out.println("</head>");
            out.println("<body>");
            
            out.println("<div class='container'>");
            out.println("<h2>Response Recorded Successfully</h2>");
            
            // Get the count of this response
            String countQuery = "SELECT COUNT(*) FROM responses WHERE questionNo = ? AND choice = ?";
            pstmt = conn.prepareStatement(countQuery);
            pstmt.setInt(1, questionNo);
            pstmt.setString(2, choice);
            ResultSet rs = pstmt.executeQuery();
            
            out.println("</div>");
            out.println("</div>");
            
            out.println("</body>");
            out.println("</html>");
            
            // Clean-up
            rs.close();
            pstmt.close();
            conn.close();
            
        } catch (Exception e) {
            // Handle errors
            out.println("<!DOCTYPE html>");
            out.println("<html>");
            out.println("<head>");
            out.println("<title>Error</title>");
            out.println("<style>");
            out.println("body { font-family: Arial, sans-serif; margin: 20px; text-align: center; }");
            out.println(".error { color: #e74c3c; }");
            out.println(".container { background-color: #f9f9f9; padding: 20px; border-radius: 10px; max-width: 600px; margin: 0 auto; }");
            out.println(".navigation { margin-top: 30px; }");
            out.println(".navigation a { padding: 10px 20px; background-color: #3498db; color: white; text-decoration: none; border-radius: 5px; }");
            out.println("</style>");
            out.println("</head>");
            out.println("<body>");
            out.println("<div class='container'>");
            out.println("<h2 class='error'>Error Occurred</h2>");
            out.println("<p>" + e.getMessage() + "</p>");
            out.println("<div class='navigation'>");
            out.println("<a href='display?questionNo=" + questionNo + "'>Try Again</a>");
            out.println("</div>");
            out.println("</div>");
            out.println("</body>");
            out.println("</html>");
            e.printStackTrace();
        } 
    }
}