package scriptease.gui.translatoreditor;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JSplitPane;

import scriptease.controller.undo.UndoManager;
import scriptease.gui.WidgetDecorator;
import scriptease.gui.component.ComponentFactory;
import scriptease.gui.pane.LibraryPanel;
import scriptease.gui.storycomponentpanel.StoryComponentPanel;
import scriptease.gui.ui.ScriptEaseUI;
import scriptease.model.semodel.librarymodel.LibraryModel;
import scriptease.translator.Translator;
import scriptease.util.ListOp;

@SuppressWarnings("serial")
public class TranslatorEditor extends JPanel {

	/**
	 * Creates the translator editor for a translator.
	 * 
	 * @param translator
	 */
	public TranslatorEditor(Translator translator) {
		final List<LibraryModel> libraries = new ArrayList<LibraryModel>(
				translator.getLibraries());

		Collections.sort(libraries);

		final LibraryPanel leftLibraryPanel = new LibraryPanel();
		final LibraryPanel rightLibraryPanel = new LibraryPanel();

		final JPanel controlPanel = new JPanel();
		final JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);

		final JButton rightCopyButton;
		final JButton leftCopyButton;
		final JPanel leftPanel;
		final JPanel rightPanel;

		rightCopyButton = ComponentFactory
				.buildRightArrowButton(ScriptEaseUI.SE_ORANGE);
		leftCopyButton = ComponentFactory
				.buildLeftArrowButton(ScriptEaseUI.SE_ORANGE);

		leftPanel = this.buildLibraryChooser(libraries, leftLibraryPanel,
				rightLibraryPanel, rightCopyButton, leftCopyButton);
		rightPanel = this.buildLibraryChooser(libraries, rightLibraryPanel,
				leftLibraryPanel, leftCopyButton, rightCopyButton);

		splitPane.setTopComponent(leftPanel);
		splitPane.setBottomComponent(rightPanel);
		splitPane.setOpaque(false);
		splitPane.setResizeWeight(0.5);
		splitPane.setBorder(null);
		WidgetDecorator.setSimpleDivider(splitPane);

		controlPanel.setLayout(new FlowLayout(FlowLayout.TRAILING));
		controlPanel.setOpaque(false);

		this.setBackground(ScriptEaseUI.SECONDARY_UI);
		this.setLayout(new BorderLayout());
		this.add(splitPane, BorderLayout.CENTER);
		this.add(controlPanel, BorderLayout.SOUTH);
	}

	private JPanel buildLibraryChooser(
			final Collection<LibraryModel> libraries,
			final LibraryPanel thisLibraryPanel,
			final LibraryPanel otherLibraryPanel, final JButton thisCopyButton,
			final JButton otherCopyButton) {

		final JPanel panel = new JPanel();

		final JComboBox libraryChooser = new JComboBox();

		thisCopyButton.setEnabled(false);
		thisCopyButton.setPreferredSize(new Dimension(1, 25));

		libraryChooser.addItem(null);

		for (LibraryModel library : libraries) {
			libraryChooser.addItem(library.getTitle());
		}

		thisCopyButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				final LibraryModel otherLibrary = ListOp.head(otherLibraryPanel
						.getLibraries());

				if (!UndoManager.getInstance().hasOpenUndoableAction())
					UndoManager.getInstance().startUndoableAction(
							"Copying story component to library.");

				for (StoryComponentPanel selected : thisLibraryPanel
						.getSelected()) {
					otherLibrary.add(selected.getStoryComponent().clone());
				}

				if (UndoManager.getInstance().hasOpenUndoableAction())
					UndoManager.getInstance().endUndoableAction();
			}
		});

		libraryChooser.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				final Object selected = libraryChooser.getSelectedItem();

				if (selected == null) {
					thisCopyButton.setEnabled(false);
					otherCopyButton.setEnabled(false);

					thisLibraryPanel.clearLibraries();
					return;
				}

				LibraryModel found = null;

				for (LibraryModel library : libraries) {
					if (library.getTitle().equals((String) selected)) {
						found = library;
						break;
					}
				}

				if (found != null) {
					thisLibraryPanel.setLibraries(found);

					final boolean enable = !otherLibraryPanel.getLibraries()
							.isEmpty();

					thisCopyButton.setEnabled(enable);
					otherCopyButton.setEnabled(enable);
				}
			}
		});

		panel.setLayout(new BorderLayout());
		panel.setOpaque(false);
		panel.add(libraryChooser, BorderLayout.NORTH);
		panel.add(thisLibraryPanel, BorderLayout.CENTER);
		panel.add(thisCopyButton, BorderLayout.SOUTH);
		return panel;
	}

	public static JPanel buildTranslatorEditorPanel() {
		final JPanel choices;

		choices = new JPanel();

		// TODO Add "Game Directory" and "Compiler Path" things to here instead
		// of the dialog box thing.

		choices.setBackground(ScriptEaseUI.SECONDARY_UI);
		return choices;
	}
}
