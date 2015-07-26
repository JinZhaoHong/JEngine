import java.io.IOException;
import java.net.SocketTimeoutException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Set;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.safety.Whitelist;
import org.jsoup.select.Elements;

/**
 *
 * @author Zhaohong Jin
 *
 */
public class Crawler {
	private Queue<String> webqueue = new PriorityQueue<String>();
	// avoid crawling the same url over and over
	private ArrayList<String> urlList = new ArrayList<String>();
	private int MAXURL; // the maxium url
	private int urlid = 0;
	private String starturl;
	private DBManager db;

	/**
	 * start the crawler with default settings
	 * 
	 * @throws SQLException
	 * @throws IOException
	 * @throws ClassNotFoundException
	 */
	public Crawler() throws IOException, SQLException, ClassNotFoundException {
		this(500, "https://www.wikipedia.org/");
	}

	/**
	 * start the crawler with user-specified settings
	 * 
	 * @param maxurl
	 * @param starturl
	 * @throws SQLException
	 * @throws IOException
	 * @throws ClassNotFoundException
	 */
	public Crawler(int maxurl, String starturl) throws IOException,
			SQLException, ClassNotFoundException {
		this.MAXURL = maxurl;
		this.starturl = starturl;
		setUpDB();
	}

	/**
	 * log in mysql
	 * 
	 * @throws IOException
	 * @throws SQLException
	 * @throws ClassNotFoundException
	 */
	private void setUpDB() throws IOException, SQLException,
			ClassNotFoundException {
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
	 * begin crawling with the default url
	 * 
	 * @throws IOException
	 * @throws SQLException
	 * 
	 */
	public void crawl() throws IOException, SQLException {
		System.out.println("Starting crawling....");
		// crawl(starturl); //dfs crawl
		bfscrawl(starturl); // bfs crawl
		System.out.println("crawling complete!");
	}

	/**
	 * begin crawling with a specific url use breadth first search
	 * 
	 * @param starturl
	 * @throws IOException
	 * @throws SQLException
	 */
	public void bfscrawl(String starturl) throws IOException, SQLException {
		if (starturl == null) {
			return;
		}
		webqueue.add(starturl);
		while (!webqueue.isEmpty() && urlid < MAXURL) {
			String url = webqueue.poll();
			Document doc;
			Elements hrefs;
			try {
				doc = Jsoup.connect(url).get();
				hrefs = doc.select("a");
				// if (!urlList.contains(url)) {
				if (!db.urlInDB(url)) {
					String description = topOneHundred(url);
					if (!db.descriptionInDB(description)) {
						// urlList.add(url);
						System.out.println(urlid);
						urlid += 1;
						HashMap<String, Integer> wordMap = parseHTML(getHTMLContent(url));
						insertDBWord(url, wordMap, urlid);
						insertDBDescription(url, description, urlid);
						for (Element e : hrefs) {
							String href = e.attr("href");
							webqueue.add(href);
						}
					}
				}
			} catch (IOException e) {
				// if the url is not valid, stop the crawling process
				System.out.println("IOException : " + url);
				continue;
			} catch (IllegalArgumentException e) {
				System.out.println(e + " Must supply a valid URL : " + url);
				continue;
			}
		}
	}

	/**
	 * begin crawling with a specific url use depth first search
	 * 
	 * @throws IOException
	 * @throws SQLException
	 */
	public void crawl(String starturl) throws IOException, SQLException {
		if (urlid >= MAXURL) // base case
			return;
		Document doc;
		try {
			doc = Jsoup.connect(starturl).get();
		} catch (IOException e) {
			// if the url is not valid, stop the crawling process
			return;
		} catch (IllegalArgumentException e) {
			System.out.println("Must supply a valid URL : " + starturl);
			return;
		}
		if (!urlList.contains(starturl)) {
			urlList.add(starturl);
		}
		// if the url has already been crawled
		else if (urlList.contains(starturl)) {
			return;
		}
		Elements hrefs = doc.select("a");
		urlid += 1;
		// terminate the process if there is no more link in a webpage
		if (hrefs == null || hrefs.size() == 0)
			return;
		HashMap<String, Integer> wordMap = parseHTML(getHTMLContent(starturl));
		insertDBWord(starturl, wordMap, urlid);
		insertDBDescription(starturl, topOneHundred(starturl), urlid);
		for (Element e : hrefs) {
			String href = e.attr("href");
			crawl(href); // depth first search;
		}
	}

	/**
	 * @see jsoup.
	 * @param url
	 * @return the first 500 characters of a webpage
	 * @throws IOException
	 */
	public String topOneHundred(String url) throws IOException {
		try {
			Document doc = Jsoup.connect(url).get();
			String content = doc.body().text();
			if (content.length() < 500)
				return content;
			content = content.substring(0, 500);
			return content;
		} catch (SocketTimeoutException e) {
			System.out.println("time out exception. " + url);
			return "N/A";
		}
	}

	/**
	 * get the html code of a corresponding url
	 * 
	 * @param url
	 * @return the html code of a corresponding url
	 * @throws IOException
	 */
	public String getHTML(String url) throws IOException {
		Document doc = Jsoup.connect(url).get();
		return doc.toString();
	}

	/**
	 * get the content in the html without tags
	 * 
	 * @param url
	 * @return
	 * @throws IOException
	 */
	public String getHTMLContent(String url) throws IOException {
		try {
			Document doc = Jsoup.connect(url).get();
			// Remove html white spaces
			doc.select(":containsOwn(\u00a0)").remove();
			// Remove remaining HTML code
			String content = doc.text();
			content = Jsoup.clean(content, Whitelist.relaxed());
			return content;
		} catch (SocketTimeoutException e) {
			System.out.println("Time out exception, url: " + url);
			return "";
		}
	}

	/**
	 * parse the html, remove all the tags, and count the word
	 * 
	 * @param html
	 * @return
	 */
	public HashMap<String, Integer> parseHTML(String html) {
		if (html.equals("") || html == null)
			return null;
		String[] splitkeys = { ":", ";", ",", ".", "-", "_", "^", "~", "(",
				")", "[", "]", "'", "?", "|", ">", "<", "!", "\"", "{", "}",
				"/", "*", "&", "+", "$", "@", "%", "`", "#", "=", "&", ",",
				"...", "?", "||", "¡°", "¡±", "¡£" };
		for (String s : splitkeys) {
			html.replace(s, "");
		}
		System.out.println(html);
		HashMap<String, Integer> wordMap = new HashMap<String, Integer>();
		String[] words = html.split("\\s+");
		for (String word : words) {
			word = word.toLowerCase(); // Lower case all words
			word = word.replaceAll("[^A-Za-z0-9]", ""); // Remove punctuation
			if (!wordMap.containsKey(word)) {
				wordMap.put(word, 1);
			} else {
				int count = wordMap.get(word);
				wordMap.put(word, count + 1);
			}
		}
		return wordMap;
	}

	/**
	 * 
	 * @param url
	 * @param urlMap
	 * @param urlid
	 * @throws SQLException
	 */
	private void insertDBWord(String url, HashMap<String, Integer> wordMap,
			int urlid) throws SQLException {
		if (wordMap == null)
			return;
		Set<String> words = wordMap.keySet();
		for (String word : words) {
			int count = wordMap.get(word);
			db.insertWordInDB(word, count, urlid, url);
		}
	}

	/**
	 * 
	 * @param url
	 * @param description
	 * @param urlid
	 * @throws IOException
	 * @throws SQLException
	 */
	private void insertDBDescription(String url, String description, int urlid)
			throws SQLException, IOException {
		db.insertURLInDB(urlid, url, description);
	}

}
