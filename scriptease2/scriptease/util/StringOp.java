package scriptease.util;

import java.util.Arrays;
import java.util.Collection;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Copied from scriptease 1 by remiller. Ugly as it is, it <i>is</i> handy.<br>
 * <br>
 * Old location: ca.ualberta.cs.games.scriptease.helper
 * 
 * @author: Unattributed. Probably mattm
 */
public class StringOp {
	private static final String XML_SENSITIVE[] = { "&", "\'", "\"", "<", ">" };
	private static final String XML_EQUIV[] = { "&amp;", "&apos;", "&quot;",
			"&lt;", "&gt;" };

	/**
	 * Makes the given string entirely lower case except for the first
	 * character, which is made upper case.
	 * 
	 * @param target
	 *            The string to make proper case.
	 * @return the proper case version of <code>target</code>.
	 */
	public static String toProperCase(String target) {
		String proper = target.toLowerCase();

		if (!proper.isEmpty()) {
			proper = Character.toUpperCase(proper.charAt(0))
					+ proper.substring(1);
		}

		return proper;
	}

	/**
	 * Removes all non-alphanumeric (not A-Z, a-z, 0-9) characters outright from
	 * the given string.
	 * 
	 * @param target
	 *            The string to clean.
	 * @return The <code>target</code> without alphanumeric characters.
	 */
	public static String makeAlphaNumeric(String target) {
		String r = "";

		if (target == null || target.equals("")) {
			target = "No Name Specified.";// Il8nResources.getString("No_Name_Specified");
		} else {
			r = target.replaceAll("[^A-Za-z0-9]", "");
		}
		if (r.equals("")) {
			r = "No Name Specified.";// Il8nResources.getString("No_Name_Specified").replaceAll("[^A-Za-z0-9]",
			// "");
		}

		// Note we check for a digit after so we don't get any funky arab
		// digits messing things up
		if (Character.isDigit(r.charAt(0))) {
			return "_" + r;
		} else {
			return r;
		}
	}

	/**
	 * Prepends each line of the input String with the line number and one
	 * space.
	 * 
	 * @param string
	 * @return
	 */
	public static String numberLines(String string) {
		return StringOp.numberLines(string, 1);
	}

	/**
	 * Prepends each line of the input String with the line number and a number
	 * of spaces equal to the <code>padding</code> parameter.
	 * 
	 * @param string
	 * @param padding
	 * @return
	 */
	public static String numberLines(String string, int padding) {
		StringBuffer text = new StringBuffer();
		String lines[] = string.split("\\n");
		int tab = (Integer.toString(lines.length + 1)).length() + padding;

		for (int i = 0; i < lines.length; i++) {
			String lineNumber = Integer.toString(i + 1);
			text.append(lineNumber);

			for (int j = lineNumber.length(); j < tab; j++)
				text.append(' ');

			text.append(lines[i]).append('\n');
		}

		return text.toString();
	}

	public static String makeXMLSafe(String toProcess) {
		for (int i = 0; i < StringOp.XML_SENSITIVE.length; i++)
			toProcess = StringOp.replaceAll(toProcess,
					StringOp.XML_SENSITIVE[i], StringOp.XML_EQUIV[i]);

		return toProcess;
	}

	/**
	 * Removes whitespace from a string and converts escaped whitespace into
	 * whitespace (ie '\\t' -> '\t'). Currently used by FormatXMLInterpreter.
	 * 
	 * @param toBeTrimmed
	 * @return
	 */
	public static String removeWhiteSpace(String toBeTrimmed) {
		return toBeTrimmed.replaceAll("\\s", "");
	}

	/*
	 * Escape the special characters ($ and \) in the string, then perform the
	 * regular expression match.
	 */
	public static String replaceAll(String toProcess, String regex,
			String replacement) {
		for (int i = 0; i < toProcess.length(); i++) {
			if (toProcess.charAt(i) == '$' || toProcess.charAt(i) == '\\') {
				toProcess = toProcess.substring(0, i) + "\\"
						+ toProcess.substring(i);
				i++;
			}
		}
		return toProcess.replaceAll(regex, replacement);
	}

	/**
	 * Like String.replaceAll, but doesn't use regular expressions.
	 * 
	 * @param string
	 *            The String to be processed.
	 * @param pattern
	 *            The literal String to be replaced.
	 * @param replacement
	 *            The replacement String.
	 * @return A new String.
	 */
	public static String simpleReplaceAll(String string, String pattern,
			String replacement) {
		StringBuffer sb = new StringBuffer();
		int fromIndex = 0;
		int strlength = string.length();
		int pattlength = pattern.length();
		if (strlength == 0 || strlength < pattlength)
			return string;
		while (true) {
			int indexAt = string.indexOf(pattern, fromIndex);
			if (indexAt == -1) {
				sb.append(string.substring(fromIndex, strlength));
				break;
			}
			if (fromIndex < indexAt)
				sb.append(string.substring(fromIndex, indexAt));
			sb.append(replacement);
			fromIndex = indexAt + pattlength;
		}
		return sb.toString();
	}

	/**
	 * Recursively removes characters which do not match the legalFormat regex.
	 * If 'source' does not match as is, the first offending character is
	 * removed. The system recurses, removing one character at a time, until the
	 * result matches, or is empty.
	 * 
	 * If a null Pattern is given, it just returns the source string
	 * 
	 * @param source
	 * @param legalFormat
	 * @return
	 */
	public static String removeIllegalCharacters(String source,
			Pattern legalFormat) {
		if (legalFormat == null)
			return source;
		Matcher legalMatcher = legalFormat.matcher(source);

		if (source == null || source.isEmpty()) {
			throw new IllegalArgumentException(
					"source string cannot be null or empty");
		}
		if (legalMatcher.matches()) {
			return source;
		} else {
			String newSource = "";
			for (char curChar : source.toCharArray()) {
				legalMatcher = legalFormat.matcher(newSource + curChar);
				newSource += legalMatcher.matches() ? curChar : "";
			}
			return newSource;
		}
	}

	/**
	 * Appends "se_" to the given source string if the first character is not a letter
	 * @param source
	 * @return
	 */
	public static String removeNonCharPrefix(String source) {
		if (source.length() > 0) {
			final char firstChar = source.charAt(0);
			if( !Character.isLetter(firstChar))
			{
				source = "se_" + source;
			}
		}
		return source;
	}

	public static String convertNumberToPattern(String source,
			Pattern legalFormat) {
		if (legalFormat == null)
			return source;
		Matcher legalMatcher = legalFormat.matcher(source);

		if (source == null || source.isEmpty())
			throw new IllegalArgumentException(
					"source string cannot be null or empty");

		if (legalMatcher.matches()) {
			return source;
		} else {
			legalMatcher.find(0);
			return legalMatcher.group();
		}
	}

	public static int wordCount(String source) {
		String wordArray[] = source.split("\\w+");
		return wordArray.length;
	}

	/**
	 * Method that returns a collection of Strings as a single String separated
	 * by the separator.<br>
	 * <br>
	 * An example: <br>
	 * If<br>
	 * <code>Collection&lt;String&gt; collection = ["First"], ["Second"], ["Third"];<br></code>
	 * and <br>
	 * <code>String seperator = ", "</code><br>
	 * then<br>
	 * <code>getCollectionAsString(collection) returns "First, Second, Third"</code>
	 * 
	 * @param strings
	 *            The collection of strings
	 * @param separator
	 *            The separator used to separate the strings.
	 * @return A collection in String representation
	 * @author kschenk
	 */
	public static String getCollectionAsString(Collection<String> strings,
			String separator) {
		String collectionText = "";

		for (String includeText : strings) {
			collectionText += includeText + separator;
		}
		int labelLength = collectionText.length();
		if (labelLength > 0) {
			return collectionText
					.substring(0, labelLength - separator.length());
		} else
			return "";
	}

	public static String join(Collection<String> items, String separator) {
		final StringBuilder builder = new StringBuilder();
		for (String item : items) {
			builder.append(item);
			builder.append(separator);
		}

		builder.delete(builder.length() - separator.length(), builder.length());

		return builder.toString();
	}

	public static String join(String[] items, String separator) {
		return StringOp.join(Arrays.asList(items), separator);
	}
}
