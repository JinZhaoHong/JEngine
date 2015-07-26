import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.util.ArrayList;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

/**
 * Servlet implementation class Jservlet
 * @param <DBManager>
 */
@WebServlet("/Jservlet")
public class Jservlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private DBManager db;
	private ArrayList<Integer> urlList;
	private String[][] urlmatrix;

	/**
	 * @see HttpServlet#HttpServlet()
	 */
	public Jservlet() {
		super();
		// TODO Auto-generated constructor stub
	}

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doGet(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		// Set response content type
		response.setContentType("text/html");
		try {
			String keyword = request.getParameter("keyword").toLowerCase();
	    	System.out.println(System.getProperty("user.dir"));
	    	System.out.println(keyword);
	    	initiateDB();
	    	System.out.println(keyword);
			seturlList(keyword);
			System.out.println(urlList);
			urlmatrix = geturlMatrix(urlList);
		} catch (SQLException | ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NullPointerException e) {
			// if we have invalid input, direct the user to the error handling servlet
			RequestDispatcher rd = request.getRequestDispatcher("ErrorHandleServlet");
			rd.forward(request,response);
			return;
		}
		

		PrintWriter out = response.getWriter();
		String title = "Search Result";
		String docType = "<!doctype html public \"-//w3c//dtd html 4.0 "
				+ "transitional//en\">\n";
		System.out.println(System.getProperty("user.dir"));
		String output = docType
				+ "<html>\n"
				+ 	"<head> \n"
				+ 		"<meta charset='UTF-8'> \n"
				+ 		"<link type='text/css' rel='stylesheet' href='E:/workstation/JEngine/WebContent/CSS/stylesheet.css'/> \n"
				+       "<style> \n"
				+       "</style> \n"
				+ 		"<title> \n"
				+ 			title
				+ 		"</title></head>\n"
				+ 	"<body>\n"
				+ 		"<div id = 'background1' class = 'background' style = 'background-color: #F5F5F5; top: -10px; height : 100px;'>\n"
				+ 			"<div id = 'left'> \n"
				+ 				"<a href = 'http://localhost:8080/JEngine/'>\n"
				+ 					"<img src= 'http://fineprintnyc.com/images/blog/history-of-logos/google/google-logo.png' height='70' width='110'>\n"
				+ 				"</a> \n"
				+ 				"<form name='Jform' method='post' action='Jservlet'> \n" //recursive calling of Jservlet
				+ 					"<input type='text' name='keyword'> \n"
				+ 					"<input type='submit' name='Search'> \n"
			    + 				"</form> \n"
				+ 			"</div> \n"
		        + 		"</div> \n";
		int len = urlmatrix.length;
		System.out.println(len);
	    int i = 0;
		while(i < len) {
			output = output
					+ "<div id = " + i + " > \n"
					+ 	"<a href = " + urlmatrix[i][0] + " >" + "<p> <font size = 5>" + getTitle(urlmatrix[i][0]) +"</font> </p> </a> \n"
					+ 	"<p>" + urlmatrix[i][1] + "</p> \n"
					+ "</div> \n";
			i += 1;
		}
		output = output + "</body></html>";
		System.out.println(output);
		out.print(output);
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doPost(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		doGet(request, response);
	}


	/**
	 * 
	 * @throws IOException
	 * @throws SQLException
	 * @throws ClassNotFoundException 
	 */
	public void initiateDB() throws IOException, SQLException, ClassNotFoundException {
		db = new DBManager();
		db.readProperties();
		if (!db.tableExist("URLS")) {
		    db.createURLDescriptionTable();
		}
		if (!db.tableExist("Words")) {
		    db.createURLWordTable();
		}
	}
	
	/**
	 * set the list of urls contain this word
	 * @param word
	 * @throws SQLException 
	 */
	public void seturlList(String word) throws SQLException {
		urlList = db.geturlid(word);
	}
	
	/**
	 * 
	 * @param rank
	 * @return url and its description
	 * @throws SQLException 
	 */
	public String[] geturlInfo(int rank) throws SQLException {
		if (urlList == null) return null;
		String[] info = new String[2];
		String url = db.geturl(rank);
		String description = db.getDescription(rank);
		info[0] = url;
		info[1] = description;
		return info;
	}
	
	/**
	 * 
	 * @param count
	 * @return the set of urls and descriptions
	 * @throws SQLException 
	 */
	public String[][] geturlMatrix(ArrayList<Integer> urlid) throws SQLException {
		if (urlid == null) return null;
		int size = urlid.size();
		String[][] matrix = new String[size][];
		for (int i = 0; i < size; i++) {
			matrix[i] = geturlInfo(urlid.get(i)); 
		}
		return matrix;
	}
	
	/**
	 * get the title of a url page
	 * @param url
	 * @return title
	 * @throws IOException 
	 */
	public String getTitle(String url) throws IOException {
		try {
			Document document = Jsoup.connect(url).get();
			String title = document.select("title").text();
			return title;
		} catch (IllegalArgumentException e) {
			return "NO TITLE";
		}
	}


}
