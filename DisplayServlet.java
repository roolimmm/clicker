// DisplayServlet.java
import java.io.*;
import java.sql.*;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import jakarta.servlet.annotation.WebServlet;
import java.util.*;

@WebServlet("/display")
public class DisplayServlet extends HttpServlet {
    
    // Database connection parameters - replace with your actual values
    private static final String JDBC_DRIVER = "com.mysql.jdbc.Driver";
    private static final String DB_URL = "jdbc:mysql://localhost:3306/clicker";
    private static final String USER = "myuser";
    private static final String PASS = "xxxx";
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        response.setContentType("text/html");
        PrintWriter out = response.getWriter();
        
        // Get the questionNo parameter, default to 8 if not provided
        String questionNoParam = request.getParameter("questionNo");
        int questionNo = 8; // Default question number
        if (questionNoParam != null && !questionNoParam.isEmpty()) {
            try {
                questionNo = Integer.parseInt(questionNoParam);
            } catch (NumberFormatException e) {
                // Invalid number, use default
            }
        }
        
        // JDBC variables
        Connection conn = null;
        Statement stmt = null;
        
        try {
            // Register JDBC driver
            Class.forName(JDBC_DRIVER);
            
            // Open a connection
            conn = DriverManager.getConnection(DB_URL, USER, PASS);
            
            // Begin HTML response
            out.println("<!DOCTYPE html>");
            out.println("<html>");
            out.println("<head>");
            out.println("<title>Clicker Statistics</title>");
            out.println("<style>");
            out.println("body { font-family: Arial, sans-serif; margin: 20px; }");
            out.println("h1 { color: #2c3e50; }");
            out.println("table { border-collapse: collapse; width: 50%; margin-top: 20px; }");
            out.println("th, td { border: 1px solid #ddd; padding: 12px; text-align: left; }");
            out.println("th { background-color: #3498db; color: white; }");
            out.println("tr:nth-child(even) { background-color: #f2f2f2; }");
            out.println("tr:hover { background-color: #e6e6e6; }");
            out.println(".total { font-weight: bold; background-color: #eee; }");
            out.println(".question-selector { margin-bottom: 20px; }");
            out.println(".question-selector select { padding: 8px; }");
            out.println(".question-selector button { padding: 8px 16px; background-color: #3498db; color: white; border: none; cursor: pointer; }");
            out.println(".question-selector button:hover { background-color: #2980b9; }");
            out.println("</style>");
            out.println("</head>");
            out.println("<body>");
            
            out.println("<h1>Clicker Response Statistics</h1>");
            
            // Question selector form
            out.println("<div class='question-selector'>");
            out.println("<form method='get'>");
            out.println("Question Number: <select name='questionNo'>");
            
            // Get all available question numbers
            stmt = conn.createStatement();
            String distinctQuestionsQuery = "SELECT DISTINCT questionNo FROM responses ORDER BY questionNo";
            ResultSet distinctQuestions = stmt.executeQuery(distinctQuestionsQuery);
            
            while (distinctQuestions.next()) {
                int qNo = distinctQuestions.getInt("questionNo");
                out.println("<option value='" + qNo + "'" + (qNo == questionNo ? " selected" : "") + ">" + qNo + "</option>");
            }
            
            out.println("</select>");
            out.println("<button type='submit'>View Statistics</button>");
            out.println("</form>");
            out.println("</div>");
            
            out.println("<h2>Statistics for Question #" + questionNo + "</h2>");
            
            // Execute SQL query to get counts for each choice
            String sqlStr = "SELECT choice, COUNT(*) as count FROM responses WHERE questionNo = ? GROUP BY choice ORDER BY choice";
            PreparedStatement pstmt = conn.prepareStatement(sqlStr);
            pstmt.setInt(1, questionNo);
            ResultSet rs = pstmt.executeQuery();
            
            // Display results in a table
            out.println("<table>");
            out.println("<tr><th>Choice</th><th>Count</th><th>Percentage</th></tr>");
            
            Map<String, Integer> choiceCounts = new HashMap<>();
            int totalResponses = 0;
            
            // First pass to get total count
            while (rs.next()) {
                String choice = rs.getString("choice");
                int count = rs.getInt("count");
                choiceCounts.put(choice, count);
                totalResponses += count;
            }
            
            // Display each choice with count and percentage
            for (char choice = 'a'; choice <= 'd'; choice++) {
                String choiceStr = String.valueOf(choice);
                int count = choiceCounts.getOrDefault(choiceStr, 0);
                double percentage = totalResponses > 0 ? ((double) count / totalResponses) * 100 : 0;
                
                out.println("<tr>");
                out.println("<td>" + choiceStr.toUpperCase() + "</td>");
                out.println("<td>" + count + "</td>");
                out.println("<td>" + String.format("%.1f%%", percentage) + "</td>");
                out.println("</tr>");
            }
            
            // Display total row
            out.println("<tr class='total'>");
            out.println("<td>Total</td>");
            out.println("<td>" + totalResponses + "</td>");
            out.println("<td>100%</td>");
            out.println("</tr>");
            
            out.println("</table>");
            
            // Add a simple bar chart visualization
            out.println("<h2>Visualization</h2>");
            out.println("<div style='margin-top: 20px;'>");
            
            for (char choice = 'a'; choice <= 'd'; choice++) {
                String choiceStr = String.valueOf(choice);
                int count = choiceCounts.getOrDefault(choiceStr, 0);
                double percentage = totalResponses > 0 ? ((double) count / totalResponses) * 100 : 0;
                
                String barColor;
                switch(choice) {
                    case 'a': barColor = "#3498db"; break; // Blue
                    case 'b': barColor = "#2ecc71"; break; // Green
                    case 'c': barColor = "#f1c40f"; break; // Yellow
                    case 'd': barColor = "#e74c3c"; break; // Red
                    default: barColor = "#95a5a6"; break;  // Gray
                }
                
                out.println("<div style='margin-bottom: 10px;'>");
                out.println("<div style='display: flex; align-items: center;'>");
                out.println("<div style='width: 20px; text-align: center;'>" + choiceStr.toUpperCase() + "</div>");
                out.println("<div style='margin-left: 10px; width: 300px; background-color: #eee;'>");
                out.println("<div style='width: " + (percentage * 3) + "px; height: 24px; background-color: " + barColor + ";'></div>");
                out.println("</div>");
                out.println("<div style='margin-left: 10px;'>" + String.format("%.1f%%", percentage) + " (" + count + ")</div>");
                out.println("</div>");
                out.println("</div>");
            }
            
            out.println("</div>");
            
            // Add auto-refresh script
            out.println("<script>");
            out.println("setTimeout(function() { location.reload(); }, 5000);"); // Refresh every 5 seconds
            out.println("</script>");
            
            out.println("</body>");
            out.println("</html>");
            
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