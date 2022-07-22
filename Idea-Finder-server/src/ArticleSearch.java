import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import SearchTools.wordSearcher;

public class ArticleSearch extends Thread {

	private String url, useragent;
	private wordSearcher s = new wordSearcher();
	private String[] terms;
	private double[] termsCount;

	public ArticleSearch(String url, String agent, String[] terms) {
		this.url = url;
		this.useragent = agent;
		this.terms = terms;
		this.termsCount = new double[terms.length];
	}

	public void run() {

		int termCount = 0;
		// arraylist for counting spaces between all the terms
		ArrayList<Integer> termSpace = new ArrayList<Integer>();
		int space = 0;

		try {
			// post GET-request to the url
			Document doc = Jsoup.connect(url).referrer("http://www.google.com").userAgent(useragent).timeout(30000)
					.get();
			// get all the words in the document
			String[] words = doc.body().text().split("[ :;'?=()!\\[\\]-]+|(?<=\\d)(?=\\D)");

			for (String word : words) {

				// compare each term to see if it is equal to the word
				for (int i = 0; i < terms.length; i++)
					/*
					 * if the term is longer then 2 check if the term exist in the word because some
					 * words like 'a, is, are, an' exist in many longer words and we don´t want to
					 * get too much false information. this algorithm isn´t really perfect in any
					 * sense
					 **/
					if (terms[i].length() > 2) {

						if (s.hasWordInIt(word, terms[i]) && terms[i].length() > 2) {
							termsCount[i]++;
							termSpace.add(space);
							space = 0;
						}
					}
					// else compare the term and word directly
					else if (word.toLowerCase().trim().equals(terms[i].toLowerCase())) {
						termsCount[i]++;
						termSpace.add(space);
						space = 0;
					}
				space++;
			}
			// calculate the originality

			double totTermPer = 0;

			for (int i = 0; i < termsCount.length; i++) {
				termsCount[i] /= words.length;
				totTermPer += termsCount[i];
			}

			if (termSpace.size() != 0 && totTermPer < 50) {

				int totRel = 0;
				int maxRel = 100 / termsCount.length;

				for (int i = 0; i < termsCount.length; i++) {
					termsCount[i] /= totTermPer;
					totRel *= (termsCount[i]) * maxRel;
				}

				double avg = 0;
				for (int i : termSpace) {
					avg += i;
				}
				avg = avg / termSpace.size();

				double org = 100 - totTermPer;
				Search.addOrg(org);

			} else {
				Search.addOrg(10);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
