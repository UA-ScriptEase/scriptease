package scriptease.gui.dialog;

import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.swing.BoxLayout;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JSplitPane;

import scriptease.gui.component.ComponentFactory;
import scriptease.gui.pane.LibraryPanel;
import scriptease.gui.ui.ScriptEaseUI;
import scriptease.model.semodel.librarymodel.LibraryModel;
import scriptease.translator.Translator;

@SuppressWarnings("serial")
public class LibraryMergeDialog extends JDialog {

	public LibraryMergeDialog(final Translator translator) {
		final JPanel content;
		
		final JPanel leftPanel;
		final JPanel rightPanel;
		final JPanel middlePanel;
		
		final JSplitPane leftSplit;
		final JSplitPane rightSplit;

		final JButton copyLeftButton;
		final JButton copyRightButton;

		final JButton saveButton;
		final JButton closeButton;

		// TODO Update the boxes whenever we select a library so we can't
		// have two of the same.
		final JComboBox leftLibraryBox;
		final JComboBox rightLibraryBox;

		final LibraryPanel leftList;
		final LibraryPanel rightList;

		//final GroupLayout layout;

		content = new JPanel();
		leftPanel = new JPanel();
		rightPanel = new JPanel();
		middlePanel = new JPanel();
		leftSplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
		rightSplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);

		leftLibraryBox = new JComboBox();
		rightLibraryBox = new JComboBox();

		leftList = new LibraryPanel();
		rightList = new LibraryPanel();

		// TODO actually draw the arrows.
		copyLeftButton = ComponentFactory.buildFlatButton(
				ScriptEaseUI.SE_ORANGE, "<-");
		copyRightButton = ComponentFactory.buildFlatButton(
				ScriptEaseUI.SE_ORANGE, "->");

		saveButton = ComponentFactory.buildFlatButton(ScriptEaseUI.SE_GREEN,
				"Save");
		closeButton = ComponentFactory.buildFlatButton(
				ScriptEaseUI.SE_BURGUNDY, "Close");

		//layout = new GroupLayout(content);

		content.setBackground(ScriptEaseUI.SECONDARY_UI);
		
		leftPanel.setLayout(new BoxLayout(leftPanel, BoxLayout.PAGE_AXIS));
		rightPanel.setLayout(new BoxLayout(rightPanel, BoxLayout.PAGE_AXIS));
		middlePanel.setLayout(new BoxLayout(middlePanel, BoxLayout.PAGE_AXIS));

		/* TODO Test code for GUI */
		leftLibraryBox.addItem(null);
		rightLibraryBox.addItem(null);

		final List<LibraryModel> libraries = new ArrayList<LibraryModel>(
				translator.getLibraries());

		Collections.sort(libraries);

		for (LibraryModel library : libraries) {
			leftLibraryBox.addItem(library.getTitle());
			rightLibraryBox.addItem(library.getTitle());
		}

		leftLibraryBox.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				final Object selected = leftLibraryBox.getSelectedItem();

				if (selected == null)
					return;

				final LibraryModel library;

				library = translator.findLibrary((String) selected);

				leftList.setLibraries(library);
			}
		});

		rightLibraryBox.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				final Object selected = rightLibraryBox.getSelectedItem();

				if (selected == null)
					return;

				final LibraryModel library;

				library = translator.findLibrary((String) selected);

				rightList.setLibraries(library);
			}
		});

		closeButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				// TODO Check for undoable changes.
				LibraryMergeDialog.this.dispose();
			}
		});

		
		leftPanel.add(leftLibraryBox);
		leftPanel.add(copyRightButton);
		leftPanel.add(leftList);
		
		rightPanel.add(rightLibraryBox);
		rightPanel.add(copyLeftButton);
		rightPanel.add(rightList);
		
		middlePanel.add(copyLeftButton);
		middlePanel.add(copyRightButton);
		
		//leftSplit.setTopComponent(leftPanel);
		//leftSplit.setBottomComponent(middlePanel);
		rightSplit.setTopComponent(leftPanel);
		rightSplit.setBottomComponent(rightPanel);
		
	//	content.add(leftSplit);
		content.add(rightSplit);
		//content.setLayout(layout);

		/*
		layout.setAutoCreateGaps(true);
		layout.setAutoCreateContainerGaps(true);

		layout.setHorizontalGroup(layout
				.createSequentialGroup()
				.addGroup(
						layout.createParallelGroup()
								.addComponent(leftLibraryBox)
								.addComponent(leftList))
				.addGroup(
						layout.createParallelGroup()
								.addComponent(copyLeftButton)
								.addComponent(copyRightButton))
				.addGroup(
						layout.createParallelGroup()
								.addComponent(rightLibraryBox)
								.addComponent(rightList)
								.addGroup(
										Alignment.TRAILING,
										layout.createSequentialGroup()
												.addComponent(saveButton)
												.addComponent(closeButton))));

		layout.setVerticalGroup(layout
				.createSequentialGroup()
				.addGroup(
						layout.createParallelGroup()
								.addComponent(leftLibraryBox)
								.addComponent(rightLibraryBox))
				.addGroup(
						layout.createParallelGroup()
								.addComponent(leftList)
								.addGroup(
										layout.createSequentialGroup()
												.addComponent(copyLeftButton)
												.addComponent(copyRightButton))
								.addComponent(rightList))
				.addGroup(
						layout.createParallelGroup().addComponent(saveButton)
								.addComponent(closeButton)));
*/
		this.setModalityType(ModalityType.APPLICATION_MODAL);
		this.setContentPane(content);
		this.pack();
	}
}
