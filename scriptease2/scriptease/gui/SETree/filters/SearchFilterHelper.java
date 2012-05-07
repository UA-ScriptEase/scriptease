package scriptease.gui.SETree.filters;

import java.util.Collection;
import java.util.StringTokenizer;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * Static helper class for common methods and variables used in SearchFiltering
 * 
 * @author mfchurch
 * 
 */
public class SearchFilterHelper {
	final public static String WHITESPACE_AND_QUOTES = " \t\r\n\"";
	final public static String DOUBLE_QUOTE = "\"";
	final public static String QUOTES_ONLY = "\"";

	/**
	 * Counts the number of key matches in the given collection of tokens
	 * 
	 * @param tokens
	 * @param key
	 * @return
	 */
	public static Integer countKeyMatches(Collection<String> searchableData,
			String key) {
		int count = 0;
		Collection<String> parsedKeys = parseKey(key);
		for (String parsedKey : parsedKeys) {
			int localCount = 0;
			for (String data : searchableData) {
				if (data.toLowerCase().contains(parsedKey.toLowerCase()))
					localCount++;
			}
			if (localCount == 0)
				return 0;
			count += localCount;
		}
		return count;
	}

	/**
	 * Returns a Collection of String tokens representing either words, or
	 * phrases in quotations from the given text based on
	 * http://www.javapractices.com/topic/TopicAction.do?Id=87
	 * 
	 * @param text
	 * @return
	 */
	private static Collection<String> parseKey(String text) {
		Collection<String> result = new CopyOnWriteArraySet<String>();

		String currentDelims = WHITESPACE_AND_QUOTES;
		boolean returnTokens = true;

		StringTokenizer parser = new StringTokenizer(text, currentDelims,
				returnTokens);

		while (parser.hasMoreTokens()) {
			String token = parser.nextToken(currentDelims);
			if (!token.equals(DOUBLE_QUOTE)) {
				token = token.trim();
				if (!token.equals("")) {
					result.add(token);
				}
			} else {
				currentDelims = flipDelimiters(currentDelims);
			}
		}
		return result;
	}

	private static String flipDelimiters(String currentDelims) {
		if (currentDelims.equals(WHITESPACE_AND_QUOTES))
			return QUOTES_ONLY;
		else
			return WHITESPACE_AND_QUOTES;
	}
}
