package scriptease.controller.observer;

import scriptease.translator.Translator;

/**
 * Allows implementers to be notified of new translators being loaded.
 * 
 * @author remiller
 */
public interface TranslatorObserver { 
	/**
	 * Tells the <code>TranslatorObserver</code> that there has been a new
	 * Translator loaded.
	 */
	public void translatorLoaded(Translator newTranslator);
}
