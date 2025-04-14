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
    
    // Total number of questions in sequence
    private static final int TOTAL_QUESTIONS = 5;
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        response.setContentType("text/html");
        PrintWriter out = response.getWriter();
        
        // Get the questionNo parameter, default to 1 if not provided
        String questionNoParam = request.getParameter("questionNo");
        int questionNo = 1; // Default to question 1
        if (questionNoParam != null && !questionNoParam.isEmpty()) {
            try {
                questionNo = Integer.parseInt(questionNoParam);
                // Ensure questionNo is within valid range
                if (questionNo < 1) questionNo = 1;
                if (questionNo > TOTAL_QUESTIONS) questionNo = TOTAL_QUESTIONS;
            } catch (NumberFormatException e) {
                // Invalid number, use default
            }
        }
        
        // Determine view mode (question, statistics, or end)
        String mode = request.getParameter("mode");
        boolean showStats = (mode != null && mode.equals("stats"));
        boolean showEndOfQuiz = (mode != null && mode.equals("end"));
        
        // Auto-show end of quiz if we're at the last question and in stats mode
        if (questionNo > TOTAL_QUESTIONS && showStats) {
            showEndOfQuiz = true;
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
            out.println("<title>Bahoot.io!</title>");
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
            out.println(".navigation { margin: 20px 0; }");
            out.println(".navigation a { padding: 8px 16px; background-color: #3498db; color: white; border: none; cursor: pointer; text-decoration: none; display: inline-block; margin-right: 10px; }");
            out.println(".navigation a:hover { background-color: #2980b9; }");
            out.println(".question-content { background-color: #f9f9f9; padding: 20px; border-radius: 5px; margin-bottom: 20px; }");
            out.println(".question-content h3 { margin-top: 0; }");
            out.println(".answers { margin-top: 15px; }");
            out.println(".answers div { margin-bottom: 10px; }");
            out.println(".progress { margin-bottom: 15px; font-weight: bold; }");
            out.println(".end-of-quiz { text-align: center; background-color: #f9f9f9; padding: 40px; border-radius: 10px; margin: 40px auto; max-width: 600px; }");
            out.println(".end-of-quiz h2 { color: #3498db; margin-bottom: 20px; }");
            out.println(".end-of-quiz .summary { font-size: 18px; margin-bottom: 30px; }");
            out.println(".end-of-quiz .restart { padding: 12px 24px; background-color: #3498db; color: white; border: none; cursor: pointer; text-decoration: none; font-size: 16px; border-radius: 5px; }");
            out.println(".end-of-quiz .restart:hover { background-color: #2980b9; }");
            out.println("</style>");
            out.println("</head>");
            out.println("<body>");
            
            out.println("<h1>Bahoot.io!</h1>");
            
            if (showEndOfQuiz) {
                // Show end of quiz summary
                showEndOfQuiz(out, conn);
            } else if (showStats) {
                // Show statistics view
                showStatistics(out, conn, questionNo);
                
                // Navigation buttons
                out.println("<div class='navigation'>");
                out.println("<a href='display?questionNo=" + questionNo + "'>Back to Question</a>");
                if (questionNo < TOTAL_QUESTIONS) {
                    out.println("<a href='display?questionNo=" + (questionNo + 1) + "'>Next Question</a>");
                } else {
                    // If we're at the last question, show the End Quiz button
                    out.println("<a href='display?mode=end'>End Quiz</a>");
                }
                out.println("</div>");
            } else {
                // Show question view
                showQuestion(out, conn, questionNo);
                
                // View Statistics button
                out.println("<div class='navigation'>");
                out.println("<a href='display?questionNo=" + questionNo + "&mode=stats'>View Statistics</a>");
                out.println("</div>");
            }
            
            out.println("</body>");
            out.println("</html>");
            
        } catch (Exception e) {
            // Handle errors
            out.println("<html><body>");
            out.println("<h2>Error occurred!</h2>");
            out.println("<p>" + e.getMessage() + "</p>");
            out.println("</body></html>");
            e.printStackTrace();
        } 
    }
    
    private void showQuestion(PrintWriter out, Connection conn, int questionNo) throws SQLException {
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        
        try {
            // Query to get question text
            // Assuming you have a 'questions' table with this structure
            String sql = "SELECT questionText, optionA, optionB, optionC, optionD FROM questions WHERE questionNo = ?";
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, questionNo);
            rs = pstmt.executeQuery();
            
            if (rs.next()) {
                String questionText = rs.getString("questionText");
                String optionA = rs.getString("optionA");
                String optionB = rs.getString("optionB");
                String optionC = rs.getString("optionC");
                String optionD = rs.getString("optionD");
                
                out.println("<div class='question-content'>");
                out.println("<h3>Question #" + questionNo + "</h3>");
                out.println("<p>" + questionText + "</p>");
                
                out.println("<div class='answers'>");
                out.println("<div><strong>A:</strong> " + optionA + "</div>");
                out.println("<div><strong>B:</strong> " + optionB + "</div>");
                out.println("<div><strong>C:</strong> " + optionC + "</div>");
                out.println("<div><strong>D:</strong> " + optionD + "</div>");
                out.println("</div>");
                out.println("</div>");
                
            } 
        } finally {
            if (rs != null) rs.close();
            if (pstmt != null) pstmt.close();
        }
    }
    
    private void showStatistics(PrintWriter out, Connection conn, int questionNo) throws SQLException {
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        
        try {
            // First, get the question text
            String questionSql = "SELECT questionText FROM questions WHERE questionNo = ?";
            pstmt = conn.prepareStatement(questionSql);
            pstmt.setInt(1, questionNo);
            rs = pstmt.executeQuery();
            
            if (rs.next()) {
                out.println("<div class='question-content'>");
                out.println("<p>" + rs.getString("questionText") + "</p>");
                out.println("</div>");
            }
            
            rs.close();
            pstmt.close();
            
            // Execute SQL query to get counts for each choice
            String sqlStr = "SELECT choice, COUNT(*) as count FROM responses WHERE questionNo = ? GROUP BY choice ORDER BY choice";
            pstmt = conn.prepareStatement(sqlStr);
            pstmt.setInt(1, questionNo);
            rs = pstmt.executeQuery();
            
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
            
            // Add auto-refresh script for statistics view
            out.println("<script>");
            out.println("setTimeout(function() { location.reload(); }, 5000);"); // Refresh every 5 seconds
            out.println("</script>");
        } finally {
            if (rs != null) rs.close();
            if (pstmt != null) pstmt.close();
        }
    }
    
    private void showEndOfQuiz(PrintWriter out, Connection conn) throws SQLException {
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        
        try {
            // Query to get total responses across all questions
            String sql = "SELECT COUNT(*) as total_responses FROM responses";
            pstmt = conn.prepareStatement(sql);
            rs = pstmt.executeQuery();
            
            int totalResponses = 0;
            
            if (rs.next()) {
                totalResponses = rs.getInt("total_responses");
            }
            
            out.println("<div class='end-of-quiz'>");
            out.println("<h2>End of Quiz</h2>");
            out.println("<div class='summary'>");
            out.println("<p>Thank you for participating in this quiz!</p>");
            out.println("</div>");
            
            // Navigation to restart or review questions
            out.println("<div>");
            out.println("<a href='display?questionNo=1' class='restart'>Start New Quiz</a>");
            out.println("</div>");
            
            // Question review links
            out.println("<div style='margin-top: 30px;'>");
            out.println("<h3>Review Questions</h3>");
            out.println("<div style='display: flex; flex-wrap: wrap; justify-content: center;'>");
            
            for (int i = 1; i <= TOTAL_QUESTIONS; i++) {
                out.println("<a href='display?questionNo=" + i + "&mode=stats' style='margin: 5px; padding: 10px 15px; background-color: #eee; border-radius: 5px; text-decoration: none; color: #333;'>Q" + i + "</a>");
            }
            
            out.println("</div>");
            out.println("</div>");
            out.println("</div>");
            
        } finally {
            if (rs != null) rs.close();
            if (pstmt != null) pstmt.close();
        }
    }
}