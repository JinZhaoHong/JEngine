

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Servlet implementation class ErrorHandleServlet
 * @author Zhaohong Jin
 */
@WebServlet("/ErrorHandleServlet")
public class ErrorHandleServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public ErrorHandleServlet() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		PrintWriter out = response.getWriter();
		String output = 
		"<html> \n"
		+	"<head> \n"
		+		 "<meta charset='UTF-8'> \n"
		+		"<title>JEngine</title> \n"
		+	"</head> \n"
		+	"<body> \n"
		+		"<div align='center'> \n"
		+			"<img \n"
		+				"src='http://fineprintnyc.com/images/blog/history-of-logos/google/google-logo.png' \n"
		+				"height='175' width='300'> \n"
		+		"</div> \n"
		+		"<div align='center'> \n"
		+			"<form name='Jform' method='post' action='Jservlet'> \n"
		+				"<input type='text' name='keyword'> <input type='submit' \n"
		+					"name='Search'> \n"
		+			"</form> \n"
		+		"</div> \n" 
		+	"</body> + \n"
		+"</html>";
		out.print(output);
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		doGet(request, response);
	}

}
