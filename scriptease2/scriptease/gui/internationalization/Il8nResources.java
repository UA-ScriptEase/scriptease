package scriptease.gui.internationalization;

import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import javax.swing.JOptionPane;

/**
 * This class is used to incorporate Internationalization into ScriptEase 2. Any
 * words that need to be internationalized should be acquired with
 * {@link Il8nResources#getString(String)}. The Il8nResources class must be
 * initialized before any other use by calling
 * {@link Il8nResources#init(Locale)}.
 * 
 * @author Douglas Schneider
 */
public class Il8nResources {
	private static Locale currentLocale;
	private static ResourceBundle resourceBundle;

	/**
	 * Initialize the Il8nResources class to work with the properties file that
	 * corresponds with the given locale. Can be set to the system's default
	 * Locale by passing <code>Locale.getDefault()</code>.
	 * 
	 * @param newLocale
	 *            locale that the Il8nResources class should be initialized to
	 *            use.
	 */
	public static void init(Locale newLocale) {
		Il8nResources.currentLocale = newLocale;
		String path = "scriptease/resources/Il8nResources";
		resourceBundle = ResourceBundle.getBundle(path, currentLocale);
	}

	/**
	 * Searches the current local's property file for the chosen string and
	 * returns the new string that corresponds to the current locale.
	 * 
	 * @param string
	 *            the keyword string to internationalize
	 * @return the Il8n string.
	 */
	public static String getString(String string) {
		String newString;

		if (Il8nResources.resourceBundle == null) {
			newString = string;
		} else {
			try {
				newString = Il8nResources.resourceBundle.getString(string);
			} catch (MissingResourceException e) {
				JOptionPane
						.showMessageDialog(
								null,
								"Sorry, I couldn't find the label \""
										+ string
										+ "\" in the "
										+ resourceBundle.getLocale()
												.getDisplayName()
										+ " language file. \nUntil this is fixed, I will use \""
										+ string + "\" as a label.");
				newString = string;
			}
		}

		return newString;
	}
}
