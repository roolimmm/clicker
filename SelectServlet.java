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
        
        // Get the choice parameter from the request
        String choice = request.getParameter("choice");
        
        // JDBC variables
        Connection conn = null;
        Statement stmt = null;
        
        try {
            // Register JDBC driver
            Class.forName(JDBC_DRIVER);
            
            // Open a connection
            conn = DriverManager.getConnection(DB_URL, USER, PASS);
            
            // Execute SQL query
            stmt = conn.createStatement();
            String sqlStr = "INSERT INTO responses (questionNo, choice) VALUES (8, '" + choice + "')";
            int count = stmt.executeUpdate(sqlStr);
            
            // Display results
            out.println("<html><body>");
            out.println("<h2>Response recorded successfully!</h2>");
            out.println("<p>Choice: " + choice + "</p>");
            out.println("<p>Question Number: 8</p>");
            
            // Get the count of this response
            String countQuery = "SELECT count(*) FROM responses WHERE questionNo=8 AND choice='" + choice + "'";
            ResultSet rs = stmt.executeQuery(countQuery);
            
            if (rs.next()) {
                int choiceCount = rs.getInt(1);
                out.println("<p>Total count for choice '" + choice + "': " + choiceCount + "</p>");
            }
            
            out.println("</body></html>");
            
            // Clean-up
            rs.close();
            stmt.close();
            conn.close();
        } catch (Exception e) {
            // Handle errors
            out.println("<html><body>");
            out.println("<h2>Error occurred!</h2>");
            out.println("<p>" + e.getMessage() + "</p>");
            out.println("</body></html>");
            e.printStackTrace();
        } finally {
            // Close resources
            try {
                if (stmt != null) stmt.close();
                if (conn != null) conn.close();
            } catch (SQLException se) {
                se.printStackTrace();
            }
        }
    }
}