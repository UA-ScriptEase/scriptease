package scriptease.gui.storycomponentbuilder;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Vector;

import javax.swing.BoxLayout;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

import scriptease.gui.SETree.filters.TranslatorFilter;
import scriptease.gui.pane.LibraryPane;
import scriptease.gui.storycomponentpanel.setting.StoryComponentPanelStorySetting;
import scriptease.translator.Translator;
import scriptease.translator.TranslatorManager;

/**
 * @author lari
 * @author remiller
 */
@SuppressWarnings("serial")
public class StoryComponentLibraryPanel extends JPanel {
	private LibraryPane actualLibraryPane;

	public StoryComponentLibraryPanel() {
		final Translator activeTranslator;

		this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		this.add(buildTranslatorLoaderPanel());

		activeTranslator = TranslatorManager.getInstance()
				.getActiveTranslator();

		this.actualLibraryPane = new LibraryPane(
				new StoryComponentPanelStorySetting());

		this.actualLibraryPane.getSCPTree().updateFilter(
				new TranslatorFilter(activeTranslator));

		this.add(this.actualLibraryPane);
	}

	public LibraryPane getLibPane() {
		return this.actualLibraryPane;
	}

	private JPanel buildTranslatorLoaderPanel() {
		final JComboBox libSelector;
		final JPanel panel = new JPanel();
		final Collection<Translator> translators = new ArrayList<Translator>();
		final Translator activeTranslator = TranslatorManager.getInstance()
				.getActiveTranslator();

		translators.add(null);
		translators.addAll(TranslatorManager.getInstance().getTranslators());

		libSelector = new JComboBox(new Vector<Translator>(translators));

		if (activeTranslator != null)
			libSelector.setSelectedItem(activeTranslator.getApiDictionary()
					.getLibrary());

		libSelector.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				setTranslator((Translator) libSelector.getSelectedItem());
			}
		});

		panel.add(new JLabel("Currently loaded translator: "));
		panel.add(libSelector);

		return panel;
	}

	// This method is separated to allow for the loading bar aspect to have
	// something convenient to latch on to. Yes, I know it's one line. That's on
	// purpose. - remiller
	private void setTranslator(Translator t) {
		TranslatorManager.getInstance().setActiveTranslator(t);
	}
}
