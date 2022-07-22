import java.io.IOException;
import java.net.URLEncoder;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import SearchTools.wordSearcher;

public class Search {

	private String terms;
	private final String basURL = "http://ww.google.com/search";
	private String useragent;
	private wordSearcher s = new wordSearcher();
	private static double org = 0;
	static int i = 0;
	private String results;

	public void start() throws IOException {
		// post get-request to google to get results from the search
		Document doc = Jsoup.connect("https://www.google.com/search?q=" + URLEncoder.encode(terms, "UTF-8") + "&num=75")
				.userAgent(useragent).get();
		// gets all the link elements url
		Elements results = doc.select("a[href]");
		// thread pool to handle all the article searches
		ExecutorService pool = Executors.newCachedThreadPool();
		// split each word into a array
		String[] term = terms.split("[ :;'?=()!\\[\\]-]+|(?<=\\d)(?=\\D)");

		// go through each element
		for (Element result : results) {
			// check for google in the result because we don´t want any of googles links
			if (!s.hasWordInIt(result.attr("abs:href").toString(), "google")) {
				// starts to search the results of the link
				ArticleSearch art = new ArticleSearch(result.attr("abs:href").toString(), useragent, term);
				pool.execute(art);
			}
		}
		// when all the articles are done computing request termination of the
		// threadpool
		pool.shutdown();

		try {
			// await intil the threadpool is terminated
			if (pool.awaitTermination(1, TimeUnit.MINUTES)) {
				// get the average result
				System.out.println("done!!" + (org / i));
				calcResults();
			}
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
    
	public void calcResults() {

	}
    //returns the average originality score 
	static String getOriginality() {
		String temp = (org / i) + "";
		temp.substring(3, temp.length());
		return temp;
	}

	static int getProductCount() {
		return 0;
	}

	public void setTerms(String t) {
		this.terms = t.toLowerCase();
	}

	public void setUserAgent(String t) {
		this.useragent = t;
	}

	public String getTerms() {
		return this.terms;
	}

	public String getUserAgent() {
		return this.useragent;
	}

	public static void addOrg(double count) {
		i++;
		org += count;
	}

	public String getResults() {
		return this.results;
	}
}
