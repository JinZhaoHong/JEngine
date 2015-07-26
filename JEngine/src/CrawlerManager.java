import java.io.IOException;
import java.sql.SQLException;

/**
 * 
 * @author JinZhaoHong
 *
 */
public class CrawlerManager {
	/**
	 * 
	 * @param args
	 * @throws SQLException 
	 * @throws IOException 
	 * @throws ClassNotFoundException 
	 */
	public static void main(String args[]) throws IOException, SQLException, ClassNotFoundException {
		int maxurls = 500; //default
		String starturl = "http://www.nytimes.com/"; //default
		if (args.length > 4) {
			String format = "[-u <maxurls>] [-d starturl]";
			System.err.println("Input Format is: " + format);
			return;
		}
		int length = args.length;
		for (int i = 0; i < length; i++) {
			if (args[i] == "-u") {
				i += 1;
				maxurls = Integer.parseInt(args[i]);
			} else if (args[i] == "-d") {
				i += 1;
				starturl = args[i];
			}
		}
		Crawler crawler = new Crawler(maxurls, starturl);
		crawler.crawl();
	}
}
