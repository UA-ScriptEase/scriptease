package scriptease.controller.observer;

import java.util.ArrayList;
import java.util.Collection;

import javax.swing.JLabel;

import scriptease.gui.PanelFactory;
import scriptease.translator.Translator;

/**
 * A factory that creates specialized observers and stores them in a list for
 * the lifetime of ScriptEase. All observers that must exist for the lifetime of
 * ScriptEase that are usually weakly referenced should be created here and
 * stored in the constant observer list. This gets rid of a lot of hacked and
 * unnecessary code in other classes. <br>
 * <br>
 * The observers returned should still be added to their managers where they are
 * called instead of in this class, since this class is just building the
 * observers and not actually assigning them to anything. <br>
 * <br>
 * Please note that this class is not related to or affiliated with the Lifetime
 * TV network.
 * 
 * @author kschenk
 * 
 */
public class LifetimeObserverFactory {
	// A list of observers that do not get garbage collected
	private final Collection<Object> constantObserverList;

	private static LifetimeObserverFactory instance = new LifetimeObserverFactory();

	/**
	 * Returns the sole instance of {@link LifetimeObserverFactory}.
	 * 
	 * @return
	 */
	public static LifetimeObserverFactory getInstance() {
		return instance;
	}

	private LifetimeObserverFactory() {
		this.constantObserverList = new ArrayList<Object>();
	}

	/**
	 * Builds an observer for the status panel.
	 * 
	 * @see PanelFactory
	 * @param label
	 *            The JLabel that displays the status
	 * @return
	 */
	public TranslatorObserver buildStatusPanelTranslatorObserver(
			final JLabel label) {
		final TranslatorObserver translatorObserver;

		translatorObserver = new TranslatorObserver() {
			@Override
			public void translatorLoaded(Translator newTranslator) {
				if (newTranslator != null) {
					label.setText(newTranslator.getName());
					label.setEnabled(true);
					label.setIcon(newTranslator.getIcon());
				} else {
					label.setText("-None-");
					label.setEnabled(false);
					label.setIcon(null);
				}
			}
		};

		this.constantObserverList.add(translatorObserver);

		return translatorObserver;
	}
}
