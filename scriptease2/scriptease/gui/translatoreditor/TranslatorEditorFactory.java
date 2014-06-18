package scriptease.gui.translatoreditor;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.JTextField;
import javax.swing.filechooser.FileNameExtensionFilter;

import scriptease.controller.StoryComponentUtils;
import scriptease.controller.observer.TranslatorObserver;
import scriptease.controller.undo.UndoManager;
import scriptease.gui.WidgetDecorator;
import scriptease.gui.WindowFactory;
import scriptease.gui.component.ComponentFactory;
import scriptease.gui.pane.LibraryPanel;
import scriptease.gui.storycomponentpanel.StoryComponentPanel;
import scriptease.gui.ui.ScriptEaseUI;
import scriptease.model.semodel.librarymodel.LibraryModel;
import scriptease.translator.Translator;
import scriptease.translator.TranslatorManager;
import scriptease.util.ListOp;

/**
 * Builds panels used to edit properties of the translator and to edit its
 * libraries.
 * 
 * @author scriptease
 * @author kschenk
 * 
 */
public class TranslatorEditorFactory {

	/**
	 * Creates the translator editor for a translator.
	 * 
	 * @param translator
	 */
	public static JPanel buildLibraryMerger(Translator translator) {
		final List<LibraryModel> libraries = new ArrayList<LibraryModel>(
				translator.getLibraries());

		Collections.sort(libraries);

		final JPanel editor = new JPanel();
		final LibraryPanel leftLibraryPanel = new LibraryPanel();
		final LibraryPanel rightLibraryPanel = new LibraryPanel();

		final JPanel controlPanel = new JPanel();
		final JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);

		final JComboBox leftComboBox = new JComboBox();
		final JComboBox rightComboBox = new JComboBox();

		final JButton rightCopyButton;
		final JButton leftCopyButton;
		final JPanel leftPanel;
		final JPanel rightPanel;

		rightCopyButton = ComponentFactory
				.buildRightArrowButton(ScriptEaseUI.SE_ORANGE);
		leftCopyButton = ComponentFactory
				.buildLeftArrowButton(ScriptEaseUI.SE_ORANGE);

		leftPanel = buildLibraryChooser(libraries, leftLibraryPanel,
				rightLibraryPanel, leftComboBox, rightComboBox,
				rightCopyButton, leftCopyButton);
		rightPanel = buildLibraryChooser(libraries, rightLibraryPanel,
				leftLibraryPanel, rightComboBox, leftComboBox, leftCopyButton,
				rightCopyButton);

		splitPane.setTopComponent(leftPanel);
		splitPane.setBottomComponent(rightPanel);
		splitPane.setOpaque(false);
		splitPane.setResizeWeight(0.5);
		splitPane.setBorder(null);
		WidgetDecorator.setSimpleDivider(splitPane);

		controlPanel.setLayout(new FlowLayout(FlowLayout.TRAILING));
		controlPanel.setOpaque(false);

		editor.setBackground(ScriptEaseUI.SECONDARY_UI);
		editor.setLayout(new BorderLayout());
		editor.add(splitPane, BorderLayout.CENTER);
		editor.add(controlPanel, BorderLayout.SOUTH);

		return editor;
	}

	private static JPanel buildLibraryChooser(
			final Collection<LibraryModel> libraries,
			final LibraryPanel thisLibraryPanel,
			final LibraryPanel otherLibraryPanel, final JComboBox thisComboBox,
			final JComboBox otherComboBox, final JButton thisCopyButton,
			final JButton otherCopyButton) {

		final JPanel panel = new JPanel();

		thisCopyButton.setEnabled(false);
		thisCopyButton.setPreferredSize(new Dimension(1, 25));

		thisComboBox.addItem(null);

		for (LibraryModel library : libraries) {
			thisComboBox.addItem(library.getTitle());
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
					StoryComponentUtils.duplicate(selected.getStoryComponent(),
							otherLibrary);
				}

				if (UndoManager.getInstance().hasOpenUndoableAction())
					UndoManager.getInstance().endUndoableAction();
			}
		});

		thisComboBox.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				final Object selected = thisComboBox.getSelectedItem();
				LibraryModel found = null;

				if (selected == null) {
					thisCopyButton.setEnabled(false);
					otherCopyButton.setEnabled(false);

					thisLibraryPanel.clearLibraries();
				} else {
					for (LibraryModel library : libraries) {
						if (library.getTitle().equals((String) selected)) {
							found = library;
							break;
						}
					}

					if (found != null) {
						thisLibraryPanel.setLibraries(found);

						final boolean enable = !otherLibraryPanel
								.getLibraries().isEmpty();

						thisCopyButton.setEnabled(enable);
						otherCopyButton.setEnabled(enable);
					}
				}

				final ActionListener[] listeners;

				listeners = otherComboBox.getActionListeners();

				for (ActionListener listener : listeners) {
					otherComboBox.removeActionListener(listener);
				}

				otherComboBox.removeAllItems();
				otherComboBox.addItem(null);
				for (LibraryModel library : libraries) {
					if (library != found)
						otherComboBox.addItem(library.getTitle());
				}

				for (ActionListener listener : listeners) {
					otherComboBox.addActionListener(listener);
				}
			}
		});

		panel.setLayout(new BorderLayout());
		panel.setOpaque(false);
		panel.add(thisComboBox, BorderLayout.NORTH);
		panel.add(thisLibraryPanel, BorderLayout.CENTER);
		panel.add(thisCopyButton, BorderLayout.SOUTH);
		return panel;
	}

	/**
	 * Builds a panel that allows people to edit the Translator's settings.
	 * 
	 * @return
	 */
	public static JPanel buildTranslatorEditorPanel() {
		final JPanel editorPanel;
		final TranslatorObserver observer;

		editorPanel = new JPanel();

		observer = new TranslatorObserver() {
			@Override
			public void translatorLoaded(final Translator translator) {
				editorPanel.removeAll();

				if (translator == null)
					return;

				final JPanel interiorPanel = new JPanel();

				final int FIELD_SIZE = 30;
				final String BROWSE_TEXT = "Browse";

				final String compilerPath;
				final String gameDirectoryPath;

				final Runnable commitCompilePath;
				final Runnable commitGameDirectory;

				final JLabel compilerLabel = new JLabel("Compiler");
				final JLabel gameDirectoryLabel = new JLabel("Game Directory");

				final JTextField compilerPathTextField;
				final JTextField gameDirectoryTextField;

				final JButton compilerPathBrowseButton;
				final JButton gameDirectoryBrowseButton;

				final JCheckBox compilerCheckBox;
				final boolean hasCompilerPath;

				final GroupLayout layout;

				layout = new GroupLayout(interiorPanel);

				interiorPanel.setLayout(layout);

				compilerPath = translator
						.getProperty(Translator.DescriptionKeys.COMPILER_PATH);
				gameDirectoryPath = translator
						.getProperty(Translator.DescriptionKeys.GAME_DIRECTORY);

				compilerPathTextField = new JTextField(compilerPath);
				gameDirectoryTextField = new JTextField(gameDirectoryPath);

				compilerPathBrowseButton = ComponentFactory.buildFlatButton(
						ScriptEaseUI.SE_BLUE, BROWSE_TEXT);
				gameDirectoryBrowseButton = ComponentFactory.buildFlatButton(
						ScriptEaseUI.SE_BLUE, BROWSE_TEXT);

				compilerCheckBox = new JCheckBox();

				commitCompilePath = new Runnable() {

					@Override
					public void run() {
						final String compilerPath;

						if (!compilerCheckBox.isSelected())
							compilerPath = "false";
						else
							compilerPath = compilerPathTextField.getText();

						translator.setPreference(
								Translator.DescriptionKeys.COMPILER_PATH,
								compilerPath);

						translator.saveTranslatorPreferences();
					}
				};

				commitGameDirectory = new Runnable() {

					@Override
					public void run() {
						translator.setPreference(
								Translator.DescriptionKeys.GAME_DIRECTORY,
								gameDirectoryTextField.getText());
						translator.saveTranslatorPreferences();
					}
				};

				// Set up the compiler path panel

				// Set compiler path to deselected by default if there
				// is no available compiler
				hasCompilerPath = !TranslatorManager.getInstance()
						.getActiveTranslator()
						.getProperty(Translator.DescriptionKeys.COMPILER_PATH)
						.equals("false");

				compilerCheckBox.setSelected(hasCompilerPath);
				compilerCheckBox.setOpaque(false);

				compilerPathTextField.setEnabled(hasCompilerPath);
				compilerPathBrowseButton.setEnabled(hasCompilerPath);

				compilerLabel.setForeground(ScriptEaseUI.PRIMARY_UI);
				gameDirectoryLabel.setForeground(ScriptEaseUI.PRIMARY_UI);

				WidgetDecorator.decorateJTextFieldForFocusEvents(
						compilerPathTextField, commitCompilePath, false);
				WidgetDecorator.decorateJTextFieldForFocusEvents(
						gameDirectoryTextField, commitGameDirectory, false);

				compilerCheckBox.addActionListener(new ActionListener() {

					@Override
					public void actionPerformed(ActionEvent e) {
						final boolean isSelected = compilerCheckBox
								.isSelected();
						compilerPathTextField.setEnabled(isSelected);
						compilerPathBrowseButton.setEnabled(isSelected);
						commitCompilePath.run();
					}
				});
				compilerPathBrowseButton
						.addActionListener(new ActionListener() {

							@Override
							public void actionPerformed(ActionEvent arg0) {
								final File filePath;

								filePath = WindowFactory.getInstance()
										.showFileChooser(
												"Select",
												"",
												new FileNameExtensionFilter(
														"exe", "exe"));

								if (filePath != null) {
									compilerPathTextField.setText(filePath
											.getAbsolutePath());
									commitCompilePath.run();
								}
							}
						});

				// Set up the game directory path panel
				gameDirectoryBrowseButton
						.addActionListener(new ActionListener() {

							@Override
							public void actionPerformed(ActionEvent arg0) {
								final File filePath;

								filePath = WindowFactory.getInstance()
										.showDirectoryChooser("Select", "",
												null);

								if (filePath != null) {
									gameDirectoryTextField.setText(filePath
											.getAbsolutePath());
									commitGameDirectory.run();
								}
							}
						});

				layout.setHorizontalGroup(layout
						.createParallelGroup()
						.addGroup(
								layout.createSequentialGroup()
										.addComponent(compilerLabel)
										.addComponent(compilerCheckBox))
						.addComponent(gameDirectoryLabel)
						.addComponent(compilerPathBrowseButton,
								Alignment.TRAILING)
						.addComponent(gameDirectoryBrowseButton,
								Alignment.TRAILING)
						.addComponent(compilerPathTextField)
						.addComponent(gameDirectoryTextField));

				layout.setVerticalGroup(layout
						.createSequentialGroup()
						.addGroup(
								layout.createParallelGroup(Alignment.BASELINE)
										.addComponent(compilerLabel)
										.addComponent(compilerCheckBox)
										.addComponent(compilerPathBrowseButton))
						.addComponent(compilerPathTextField, FIELD_SIZE,
								FIELD_SIZE, FIELD_SIZE)
						.addGap(FIELD_SIZE / 2)
						.addGroup(
								layout.createParallelGroup(Alignment.BASELINE)
										.addComponent(gameDirectoryLabel)
										.addComponent(gameDirectoryBrowseButton))
						.addComponent(gameDirectoryTextField, FIELD_SIZE,
								FIELD_SIZE, FIELD_SIZE));

				interiorPanel.setOpaque(false);
				editorPanel.add(interiorPanel, new GridBagConstraints());
			}
		};

		TranslatorManager.getInstance().addTranslatorObserver(editorPanel,
				observer);

		editorPanel.setLayout(new GridBagLayout());
		editorPanel.setBackground(ScriptEaseUI.SECONDARY_UI);

		return editorPanel;
	}
}
