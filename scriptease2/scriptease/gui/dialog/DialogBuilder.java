package scriptease.gui.dialog;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.lang.Thread.UncaughtExceptionHandler;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Vector;

import javax.swing.BoxLayout;
import javax.swing.DefaultListCellRenderer;
import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JRootPane;
import javax.swing.JTextField;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.filechooser.FileNameExtensionFilter;

import scriptease.gui.ExceptionDialog;
import scriptease.gui.PanelFactory;
import scriptease.gui.StatusManager;
import scriptease.gui.WindowFactory;
import scriptease.model.LibraryModel;
import scriptease.model.PatternModelManager;
import scriptease.model.StoryModel;
import scriptease.translator.Translator;
import scriptease.translator.Translator.DescriptionKeys;
import scriptease.translator.TranslatorManager;
import scriptease.translator.apimanagers.AutomaticsManager;
import scriptease.translator.io.model.GameModule;

public class DialogBuilder {
	/**
	 * Simple renderer that replaces Translators with their names in a JList.
	 */
	@SuppressWarnings("serial")
	private class TranslatorListRenderer extends DefaultListCellRenderer {
		@Override
		public Component getListCellRendererComponent(JList list, Object value,
				int index, boolean isSelected, boolean cellHasFocus) {
			if (value instanceof Translator)
				value = ((Translator) value).getName();

			return super.getListCellRendererComponent(list, value, index,
					isSelected, cellHasFocus);
		}
	}

	private static DialogBuilder instance = new DialogBuilder();

	public static DialogBuilder getInstance() {
		return instance;
	}

	private DialogBuilder() {
	}

	/**
	 * Creates a progress bar in a JDialog for the given task.
	 * 
	 * @param parent
	 * @return
	 */
	public JDialog createProgressBar(Frame parent, String text) {
		final JDialog dialog = new JDialog(parent, true);

		UIManager.put("ProgressBar.selectionForeground", Color.black);
		UIManager.put("ProgressBar.selectionBackground", Color.black);
		JProgressBar progressBar = new JProgressBar();
		progressBar.setIndeterminate(true);
		progressBar.setPreferredSize(new Dimension(200, 30));
		// progressBar.setOpaque(false);
		progressBar.setString(text);
		progressBar.setStringPainted(true);

		dialog.add(progressBar);

		dialog.setUndecorated(true);
		dialog.getRootPane().setOpaque(false);
		dialog.getRootPane().setWindowDecorationStyle(JRootPane.NONE);
		dialog.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		dialog.setSize(dialog.getPreferredSize());
		dialog.setLocationRelativeTo(dialog.getParent());
		return dialog;
	}

	/**
	 * Creates an Exception Dialog with the given parent frame
	 * 
	 * @param parent
	 *            frame
	 * @return exception dialog
	 */
	public ExceptionDialog createExceptionDialog(Frame parent) {
		ExceptionDialog exceptionDialog = new ExceptionDialog(parent);
		return exceptionDialog;
	}

	public void showNewStoryWizard(Frame parent) {
		final JPanel newStoryPanel;
		final JLabel statusLabel;
		final JLabel authorLabel;
		final JTextField authorField;
		final JLabel titleLabel;
		final JTextField titleField;
		final JLabel descriptionLabel;
		final JTextField descriptionText;
		final JPanel modulePanel;
		final JLabel moduleLabel;
		final JTextField moduleField;
		final JButton moduleButton;
		final JLabel gameLabel;
		final JComboBox gameComboBox;
		final GroupLayout layout;
		final Vector<Translator> translators;

		// Sort it like a boss.
		translators = new Vector<Translator>(TranslatorManager.getInstance()
				.getTranslators());
		Collections.sort(translators, new Comparator<Translator>() {
			@Override
			public int compare(Translator t1, Translator t2) {
				return t1.getName().compareTo(t2.getName());
			}
		});

		// Construct the New Story JPanel
		newStoryPanel = new JPanel();
		statusLabel = new JLabel();

		authorLabel = new JLabel("Author: ");
		authorField = new JTextField(20);

		titleLabel = new JLabel("Title: ");
		titleField = new JTextField(20);

		descriptionLabel = new JLabel("Description: ");
		descriptionText = new JTextField(20);

		modulePanel = new JPanel();
		modulePanel.setLayout(new BoxLayout(modulePanel, BoxLayout.LINE_AXIS));
		moduleLabel = new JLabel("Module: ");
		moduleField = new JTextField(20);
		moduleButton = new JButton("Browse...");

		modulePanel.add(moduleField);
		modulePanel.add(moduleButton);

		gameLabel = new JLabel("Game: ");
		gameComboBox = new JComboBox(translators);
		gameComboBox.setRenderer(new TranslatorListRenderer());
		gameComboBox.setSelectedIndex(-1);

		// Construct the content panel
		layout = new GroupLayout(newStoryPanel);
		newStoryPanel.setLayout(layout);

		// horizontal perspective
		layout.setHorizontalGroup(layout.createParallelGroup()
				.addComponent(statusLabel).addComponent(titleLabel)
				.addComponent(titleField).addComponent(authorLabel)
				.addComponent(authorField).addComponent(descriptionLabel)
				.addComponent(descriptionText).addComponent(gameLabel)
				.addComponent(gameComboBox).addComponent(moduleLabel)
				.addComponent(modulePanel));

		// vertical perspective
		layout.setVerticalGroup(layout.createSequentialGroup()
				.addComponent(statusLabel).addComponent(titleLabel)
				.addComponent(titleField).addComponent(authorLabel)
				.addComponent(authorField).addComponent(descriptionLabel)
				.addComponent(descriptionText).addComponent(gameLabel)
				.addComponent(gameComboBox).addComponent(moduleLabel)
				.addComponent(modulePanel));

		newStoryPanel.setBorder(new EmptyBorder(5, 10, 5, 10));

		Runnable onFinish = new Runnable() {
			@Override
			public void run() {
				final File location = new File(moduleField.getText());
				final String title = titleField.getText();
				final String author = authorField.getText();
				final GameModule module;
				final StatusManager statusManager = StatusManager.getInstance();
				final Translator selectedTranslator;
				final Translator oldTranslator;
				final StoryModel model;
				final TranslatorManager translatorMgr = TranslatorManager
						.getInstance();

				// do everything in a try because otherwise the run will swallow

				// any exceptions.
				try {
					statusManager.setStatus("Creating New Story ...");
					selectedTranslator = (Translator) gameComboBox
							.getSelectedItem();
					oldTranslator = translatorMgr.getActiveTranslator();

					translatorMgr.setActiveTranslator(selectedTranslator);

					if (selectedTranslator == null) {
						WindowFactory
								.getInstance()
								.showProblemDialog("No translator",
										"No translator was chosen. I can't make a story without it.");
						statusManager
								.setStatus("Story creation aborted: no translator chosen.");
						return;
					}
					module = selectedTranslator.loadModule(location);

					if (module == null) {
						translatorMgr.setActiveTranslator(oldTranslator);
						statusManager
								.setStatus("Story creation aborted: module failed to load.");

						return;
					} else {

						model = new StoryModel(module, title, author,
								selectedTranslator);

						PatternModelManager.getInstance().add(model, true);
						PanelFactory.getInstance().createTabForModel(model);

						// Process automatics and add them to the StoryModel
						AutomaticsManager automaticsManager = new AutomaticsManager();
						automaticsManager.resolveAndAddAutomatics(module,
								selectedTranslator, model);
					}
				} catch (Throwable e) {
					UncaughtExceptionHandler handler = Thread
							.getDefaultUncaughtExceptionHandler();
					handler.uncaughtException(
							Thread.currentThread(),
							new IllegalStateException(
									"Exception while creating a new Story. ", e));
				}
			}
		};

		// Build the pages
		Collection<JPanel> pages = new ArrayList<JPanel>();
		pages.add(newStoryPanel);

		// Create the wizard
		final WizardDialog wizard = new WizardDialog(parent,
				"New Story Wizard", pages, onFinish);

		// Listeners
		titleField.getDocument().addDocumentListener(new DocumentListener() {
			@Override
			public void changedUpdate(DocumentEvent e) {
			}

			@Override
			public void insertUpdate(DocumentEvent e) {
				updateButton();
			}

			@Override
			public void removeUpdate(DocumentEvent e) {
				updateButton();
			}

			private void updateButton() {
				final File location = new File(moduleField.getText());
				final Translator selectedTranslator = (Translator) gameComboBox
						.getSelectedItem();
				if (location.exists() && selectedTranslator != null) {
					if (selectedTranslator.supportsModuleFile(location)) {
						statusLabel.setText("");
						statusLabel.setIcon(null);
						wizard.pack();
						if (!titleField.getText().isEmpty())
							wizard.setFinishEnabled(true);
						else
							wizard.setFinishEnabled(false);
					}
				} else
					wizard.setFinishEnabled(false);
			}
		});

		moduleButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				final Translator selectedTranslator = ((Translator) gameComboBox
						.getSelectedItem());
				final String[] legalExtensions;
				FileNameExtensionFilter filter = null;
				File defaultLocation = null;
				File location;

				// Build the filter based on the translator selected
				if (selectedTranslator != null) {
					legalExtensions = selectedTranslator.getLegalExtensions();

					if (legalExtensions.length != 0) {
						filter = new FileNameExtensionFilter(selectedTranslator
								.getName() + " Game Files", legalExtensions);
					}
					defaultLocation = selectedTranslator
							.getPathProperty(DescriptionKeys.GAME_DIRECTORY);

					if (defaultLocation == null || !defaultLocation.exists())
						defaultLocation = selectedTranslator.getLocation()
								.getParentFile();
				}

				location = WindowFactory.getInstance().showFileChooser(
						"Select", "", filter, defaultLocation);

				if (location != null)
					moduleField.setText(location.getAbsolutePath());
			}
		});

		moduleField.getDocument().addDocumentListener(new DocumentListener() {
			@Override
			public void changedUpdate(DocumentEvent e) {
			}

			@Override
			public void insertUpdate(DocumentEvent e) {
				updateButton();
			}

			@Override
			public void removeUpdate(DocumentEvent e) {
				updateButton();
			}

			private void updateButton() {
				File location = new File(moduleField.getText());
				Translator selectedTranslator = (Translator) gameComboBox
						.getSelectedItem();
				if (location.exists()) {
					if (selectedTranslator != null) {
						if (selectedTranslator.supportsModuleFile(location)) {
							statusLabel.setText("");
							statusLabel.setIcon(null);
							wizard.pack();
							if (!titleField.getText().isEmpty())
								wizard.setFinishEnabled(true);
							else
								wizard.setFinishEnabled(false);
						} else {
							statusLabel
									.setText("Module is not valid for the selected Game");
							statusLabel.setIcon(UIManager
									.getIcon("OptionPane.errorIcon"));
							wizard.pack();
							wizard.setFinishEnabled(false);
						}
					} else {
						statusLabel.setText("Select a Game");
						statusLabel.setIcon(UIManager
								.getIcon("OptionPane.errorIcon"));
						wizard.pack();
						wizard.setFinishEnabled(false);
					}
				} else {
					statusLabel.setText("Module does not exist");
					statusLabel.setIcon(UIManager
							.getIcon("OptionPane.errorIcon"));
					wizard.pack();
					wizard.setFinishEnabled(false);
				}
			}
		});

		gameComboBox.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				// a bit of a hack to force the module text field to revalidate
				moduleField.setText(moduleField.getText());
			}
		});

		// Display the wizard
		wizard.display();
	}

	/**
	 * This is never used. Translator assigned to new Library Model may be
	 * wrong. Check before implementing this.
	 * 
	 * @deprecated
	 * 
	 * @param parent
	 * @return
	 */
	public LibraryModel showNewLibraryWizard(Frame parent) {
		final JPanel newLibraryPanel;
		final JLabel authorLabel;
		final JTextField authorField;
		final JLabel titleLabel;
		final JTextField titleField;
		final GroupLayout layout;
		final LibraryModel model;

		// Construct the New Library JPanel
		newLibraryPanel = new JPanel();

		model = new LibraryModel();

		authorLabel = new JLabel("Author: ");
		authorField = new JTextField(20);

		titleLabel = new JLabel("Title: ");
		titleField = new JTextField(20);

		// Construct the content panel
		layout = new GroupLayout(newLibraryPanel);
		newLibraryPanel.setLayout(layout);

		// horizontal perspective
		layout.setHorizontalGroup(layout.createParallelGroup()
				.addComponent(titleLabel).addComponent(titleField)
				.addComponent(authorLabel).addComponent(authorField));

		// vertical perspective
		layout.setVerticalGroup(layout.createSequentialGroup()
				.addComponent(titleLabel).addComponent(titleField)
				.addComponent(authorLabel).addComponent(authorField));

		newLibraryPanel.setBorder(new EmptyBorder(5, 10, 5, 10));

		Runnable onFinish = new Runnable() {
			@Override
			public void run() {
				model.setTitle(titleField.getText());
				model.setAuthor(authorField.getText());
			}
		};

		// Build the pages
		Collection<JPanel> pages = new ArrayList<JPanel>();
		pages.add(newLibraryPanel);

		// Create the wizard
		final WizardDialog wizard = new WizardDialog(parent,
				"New Library Wizard", pages, onFinish);

		// Listeners
		titleField.getDocument().addDocumentListener(new DocumentListener() {
			@Override
			public void changedUpdate(DocumentEvent e) {
			}

			@Override
			public void insertUpdate(DocumentEvent e) {
				updateButton();
			}

			@Override
			public void removeUpdate(DocumentEvent e) {
				updateButton();
			}

			private void updateButton() {
				if (!titleField.getText().isEmpty())
					wizard.setFinishEnabled(true);
				else
					wizard.setFinishEnabled(false);
			}
		});

		// Display the wizard
		wizard.display();
		// If the title wasn't set, return null
		if (!model.getTitle().isEmpty())
			return model;
		else
			return null;
	}
}
