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

import scriptease.gui.WidgetDecorator;
import scriptease.gui.WindowFactory;
import scriptease.gui.component.ComponentFactory;
import scriptease.gui.pane.LibraryPanel;
import scriptease.gui.ui.ScriptEaseUI;
import scriptease.model.semodel.librarymodel.LibraryModel;
import scriptease.translator.Translator;
import scriptease.util.GUIOp;
import scriptease.util.ListOp;

@SuppressWarnings("serial")
public class LibraryMergeDialog extends JDialog {
	private static final String DEFAULT_BUTTON_TEXT = "Choose a Library";

	private final LibraryChooser leftChooser;
	private final LibraryChooser rightChooser;

	public LibraryMergeDialog(final Translator translator) {
		super(WindowFactory.getInstance().getCurrentFrame(), "Merge "
				+ translator.getName() + " Libraries",
				ModalityType.APPLICATION_MODAL);
		final List<LibraryModel> libraries = new ArrayList<LibraryModel>(
				translator.getLibraries());

		Collections.sort(libraries);

		this.leftChooser = new LibraryChooser(libraries);
		this.rightChooser = new LibraryChooser(libraries);

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

		closeButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				// TODO Check for undoable changes.
				LibraryMergeDialog.this.dispose();
			}
		});

		splitPane.setTopComponent(this.leftChooser);
		splitPane.setBottomComponent(this.rightChooser);
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

	private class LibraryChooser extends JPanel {
		private final LibraryPanel libraryPanel;

		public LibraryChooser(final Collection<LibraryModel> libraries) {
			this.libraryPanel = new LibraryPanel();

			final JComboBox libraryChooser = new JComboBox();
			final JButton copyButton;

			copyButton = ComponentFactory.buildFlatButton(
					ScriptEaseUI.SE_ORANGE, DEFAULT_BUTTON_TEXT);
			copyButton.setEnabled(false);

			libraryChooser.addItem(null);

			for (LibraryModel library : libraries) {
				libraryChooser.addItem(library.getTitle());
			}

			libraryChooser.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent e) {
					final Object selected = libraryChooser.getSelectedItem();

					if (selected == null) {
						copyButton.setEnabled(false);
						copyButton.setText(DEFAULT_BUTTON_TEXT);
						libraryPanel.clearLibraries();
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
						copyButton.setText("Copy Over");

						libraryPanel.setLibraries(found);
					}
				}
			});

			this.setLayout(new BorderLayout());
			this.setOpaque(false);
			this.add(libraryChooser, BorderLayout.NORTH);
			this.add(this.libraryPanel, BorderLayout.CENTER);
			this.add(copyButton, BorderLayout.SOUTH);
		}

		/**
		 * Gets the library currently selected.
		 * 
		 * @return
		 */
		public LibraryModel getLibrary() {
			return ListOp.head(this.libraryPanel.getLibraries());
		}
	}

}
