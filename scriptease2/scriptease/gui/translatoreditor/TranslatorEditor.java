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
import scriptease.model.TranslatorModel;
import scriptease.model.semodel.librarymodel.LibraryModel;
import scriptease.util.ListOp;

@SuppressWarnings("serial")
public class TranslatorEditor extends JPanel {
	private static final String DEFAULT_BUTTON_TEXT = "Choose a Library";

	public TranslatorEditor(TranslatorModel translator) {
		final List<LibraryModel> libraries = new ArrayList<LibraryModel>(
				translator.getLibraries());

		Collections.sort(libraries);

		final LibraryPanel leftLibraryPanel = new LibraryPanel();
		final LibraryPanel rightLibraryPanel = new LibraryPanel();

		final JPanel leftPanel = this.buildLibraryChooser(libraries,
				leftLibraryPanel, rightLibraryPanel, false);

		final JPanel rightPanel = this.buildLibraryChooser(libraries,
				rightLibraryPanel, leftLibraryPanel, true);

		final JPanel controlPanel = new JPanel();

		final JSplitPane splitPane;

		splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);

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
			final LibraryPanel otherLibraryPanel, boolean left) {

		final JPanel panel = new JPanel();

		final JComboBox libraryChooser = new JComboBox();
		final JButton copyButton;

		// TODO need a nicer way to determine which way the arrow should point
		if (left)
			copyButton = ComponentFactory
					.buildLeftArrowButton(ScriptEaseUI.SE_ORANGE);
		else
			copyButton = ComponentFactory
					.buildRightArrowButton(ScriptEaseUI.SE_ORANGE);
		copyButton.setEnabled(false);

		copyButton.setPreferredSize(new Dimension(1, 25));

		libraryChooser.addItem(null);

		for (LibraryModel library : libraries) {
			libraryChooser.addItem(library.getTitle());
		}

		copyButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				final LibraryModel otherLibrary = ListOp.head(otherLibraryPanel
						.getLibraries());

				if (!UndoManager.getInstance().hasOpenUndoableAction())
					UndoManager.getInstance().startUndoableAction(
							"Copying story component to library.");

				for (StoryComponentPanel selected : thisLibraryPanel
						.getSelected()) {
					// TODO Undoability.
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

				// TODO Need more checks to see if we can copy stuff over, like
				// if a library is selected in the other panel, etc. Disable
				// button until it's safe to do so

				if (selected == null) {
					copyButton.setEnabled(false);
					// copyButton.setText(DEFAULT_BUTTON_TEXT);
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
					copyButton.setEnabled(true);
					// copyButton.setText("Copy");
					// TODO
					// SEModelManager.getInstance().add(found);
					thisLibraryPanel.setLibraries(found);
				}

				// TODO Need to be able to get the other's library chooser..
				// maybe.
			}
		});

		panel.setLayout(new BorderLayout());
		panel.setOpaque(false);
		panel.add(libraryChooser, BorderLayout.NORTH);
		panel.add(thisLibraryPanel, BorderLayout.CENTER);
		panel.add(copyButton, BorderLayout.SOUTH);
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
