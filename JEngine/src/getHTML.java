import java.io.IOException;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;


/**
 * 
 * @author Zhaohong Jin
 *
 */
public class getHTML {
    public static void main(String args[]) throws IOException {
    	Document doc = Jsoup.connect("https://www.google.com/webhp?client=aff-cs-360se&ie=UTF-8#q=google").get();
    	System.out.println(doc.toString());
    }
}
