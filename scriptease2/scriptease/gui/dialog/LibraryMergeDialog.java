package scriptease.gui.dialog;

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
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JSplitPane;

import scriptease.controller.FileManager;
import scriptease.controller.undo.UndoManager;
import scriptease.gui.WidgetDecorator;
import scriptease.gui.WindowFactory;
import scriptease.gui.component.ComponentFactory;
import scriptease.gui.pane.LibraryPanel;
import scriptease.gui.storycomponentpanel.StoryComponentPanel;
import scriptease.gui.ui.ScriptEaseUI;
import scriptease.model.semodel.SEModel;
import scriptease.model.semodel.librarymodel.LibraryModel;
import scriptease.translator.Translator;
import scriptease.util.GUIOp;
import scriptease.util.ListOp;

@SuppressWarnings("serial")
public class LibraryMergeDialog extends JDialog {
	private static final String DEFAULT_BUTTON_TEXT = "Choose a Library";

	public LibraryMergeDialog(final Translator translator) {
		super(WindowFactory.getInstance().getCurrentFrame(), "Merge "
				+ translator.getName() + " Libraries",
				ModalityType.APPLICATION_MODAL);
		final List<LibraryModel> libraries = new ArrayList<LibraryModel>(
				translator.getLibraries());

		Collections.sort(libraries);

		final LibraryPanel leftLibraryPanel = new LibraryPanel();
		final LibraryPanel rightLibraryPanel = new LibraryPanel();

		final JPanel leftPanel = this.buildLibraryChooser(libraries,
				leftLibraryPanel, rightLibraryPanel);

		final JPanel rightPanel = this.buildLibraryChooser(libraries,
				rightLibraryPanel, leftLibraryPanel);

		final JPanel content = new JPanel();
		final JPanel controlPanel = new JPanel();

		final JSplitPane splitPane;

		final JButton saveButton;
		final JButton closeButton;

		splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);

		saveButton = ComponentFactory.buildFlatButton(ScriptEaseUI.SE_GREEN,
				"Save");
		closeButton = ComponentFactory.buildFlatButton(
				ScriptEaseUI.SE_BURGUNDY, "Close");

		saveButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				final SEModel leftModel;
				final SEModel rightModel;

				leftModel = ListOp.head(leftLibraryPanel.getLibraries());
				rightModel = ListOp.head(rightLibraryPanel.getLibraries());

				if (leftModel != null)
					FileManager.getInstance().save(leftModel);

				if (rightModel != null)
					FileManager.getInstance().save(rightModel);
			}
		});

		closeButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				// TODO Check for undoable changes.
				LibraryMergeDialog.this.dispose();
			}
		});

		splitPane.setTopComponent(leftPanel);
		splitPane.setBottomComponent(rightPanel);
		splitPane.setOpaque(false);
		splitPane.setResizeWeight(0.5);
		splitPane.setBorder(null);
		WidgetDecorator.setSimpleDivider(splitPane);

		controlPanel.setLayout(new FlowLayout(FlowLayout.TRAILING));
		controlPanel.setOpaque(false);
		GUIOp.addComponents(controlPanel, saveButton, closeButton);

		content.setBackground(ScriptEaseUI.SECONDARY_UI);
		content.setLayout(new BorderLayout());
		content.add(splitPane, BorderLayout.CENTER);
		content.add(controlPanel, BorderLayout.SOUTH);

		this.setSize(800, 800);
		this.setPreferredSize(new Dimension(800, 800));
		this.setLocationRelativeTo(null);
		this.setContentPane(content);
		this.setVisible(true);

		this.pack();
	}

	private JPanel buildLibraryChooser(
			final Collection<LibraryModel> libraries,
			final LibraryPanel thisLibraryPanel,
			final LibraryPanel otherLibraryPanel) {

		final JPanel panel = new JPanel();

		final JComboBox libraryChooser = new JComboBox();
		final JButton copyButton;

		copyButton = ComponentFactory.buildFlatButton(ScriptEaseUI.SE_ORANGE,
				DEFAULT_BUTTON_TEXT);
		copyButton.setEnabled(false);

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

				if (selected == null) {
					copyButton.setEnabled(false);
					copyButton.setText(DEFAULT_BUTTON_TEXT);
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
					copyButton.setText("Copy");
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
}
