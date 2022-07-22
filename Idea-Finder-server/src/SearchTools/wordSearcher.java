package SearchTools;

public class wordSearcher {

	public boolean hasWordInIt(String txt, String word) {

		for (int i = 0; i < (txt.length() - word.length()-1); i++) {
			if (txt.substring(i, i + word.length()).toLowerCase().equals(word)) {
				return true;
			}		
		}
		return false;
	}
}
