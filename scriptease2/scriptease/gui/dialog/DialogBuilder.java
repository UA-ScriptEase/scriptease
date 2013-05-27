package scriptease.gui.dialog;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Frame;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Vector;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultListCellRenderer;
import javax.swing.GroupLayout;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.JTextField;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import scriptease.gui.ExceptionDialog;
import scriptease.gui.StatusManager;
import scriptease.gui.WindowFactory;
import scriptease.model.LibraryModel;
import scriptease.model.SEModelManager;
import scriptease.model.StoryModel;
import scriptease.translator.Translator;
import scriptease.translator.Translator.DescriptionKeys;
import scriptease.translator.TranslatorManager;
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

	/**
	 * Creates a new library wizard that lets the user create a new library for
	 * the passed in translator.
	 * 
	 * @param translator
	 *            The translator to create a library for. This must not be null.
	 * 
	 */
	public void showNewLibraryWizard(final Translator translator) {
		if (translator == null)
			throw new IllegalArgumentException("We can't create a library for "
					+ "a null translator.");

		final String TITLE = "New Library Wizard";
		final int TEXTFIELD_COLUMNS = 20;

		final JPanel newStoryPanel = new JPanel();

		final JLabel authorLabel = new JLabel("Author: ");
		final JLabel titleLabel = new JLabel("Title: ");
		final JLabel descriptionLabel = new JLabel("Description: ");

		final JTextField authorField = new JTextField(TEXTFIELD_COLUMNS);
		final JTextField titleField = new JTextField(TEXTFIELD_COLUMNS);
		final JTextField descriptionField = new JTextField(TEXTFIELD_COLUMNS);

		final GroupLayout layout = new GroupLayout(newStoryPanel);

		final WizardDialog wizard;
		final Runnable onFinish;

		newStoryPanel.setLayout(layout);

		// horizontal perspective
		layout.setHorizontalGroup(layout.createParallelGroup()
				.addComponent(titleLabel).addComponent(titleField)
				.addComponent(authorLabel).addComponent(authorField)
				.addComponent(descriptionLabel).addComponent(descriptionField));

		// vertical perspective
		layout.setVerticalGroup(layout.createSequentialGroup()
				.addComponent(titleLabel).addComponent(titleField)
				.addComponent(authorLabel).addComponent(authorField)
				.addComponent(descriptionLabel).addComponent(descriptionField));

		onFinish = new Runnable() {
			@Override
			public void run() {
				final String title = titleField.getText();
				final String author = authorField.getText();

				final StatusManager statusManager = StatusManager.getInstance();

				statusManager.setStatus("Creating New Library ...");
				TranslatorManager.getInstance().setActiveTranslator(translator);

				final LibraryModel model;

				model = new LibraryModel(title, author, translator);

				// Set the default format to the same one we use in the default
				// library.
				model.getEventSlotManager().setDefaultFormatKeyword(
						translator.getSlotManager().getDefaultFormatKeyword());

				SEModelManager.getInstance().addAndActivate(model);
				translator.addOptionalLibrary(model);
			}
		};

		// Create the wizard
		wizard = new WizardDialog(TITLE, newStoryPanel, onFinish);

		// Listeners
		titleField.getDocument().addDocumentListener(new DocumentListener() {
			@Override
			public void changedUpdate(DocumentEvent e) {
			}

			@Override
			public void insertUpdate(DocumentEvent e) {
				this.updateButton();
			}

			@Override
			public void removeUpdate(DocumentEvent e) {
				this.updateButton();
			}

			private void updateButton() {
				wizard.setFinishEnabled(!titleField.getText().isEmpty());
			}
		});

		// Display the wizard
		wizard.setVisible(true);
	}

	/**
	 * Creates a new story wizard that lets people create a story.
	 * 
	 */
	public void showNewStoryWizard() {
		final String TITLE = "New Story Wizard";
		final int TEXTFIELD_COLUMNS = 20;

		final JPanel newStoryPanel = new JPanel();
		final JPanel modulePanel = new JPanel();

		final JLabel statusLabel = new JLabel();
		final JLabel authorLabel = new JLabel("Author: ");
		final JLabel titleLabel = new JLabel("Title: ");
		final JLabel descriptionLabel = new JLabel("Description: ");
		final JLabel moduleLabel = new JLabel("Module: ");
		final JLabel gameLabel = new JLabel("Game: ");

		final JTextField authorField = new JTextField(TEXTFIELD_COLUMNS);
		final JTextField titleField = new JTextField(TEXTFIELD_COLUMNS);
		final JTextField descriptionField = new JTextField(TEXTFIELD_COLUMNS);
		final JTextField moduleField = new JTextField(TEXTFIELD_COLUMNS);

		final JButton moduleButton = new JButton("Browse...");
		final JComboBox translatorBox = this.createTranslatorSelectionBox();

		final GroupLayout layout = new GroupLayout(newStoryPanel);

		final WizardDialog wizard;
		final Runnable onFinish;

		moduleField.setEnabled(false);
		moduleButton.setEnabled(false);

		modulePanel.setLayout(new BoxLayout(modulePanel, BoxLayout.LINE_AXIS));
		modulePanel.add(moduleField);
		modulePanel.add(moduleButton);

		newStoryPanel.setLayout(layout);

		// horizontal perspective
		layout.setHorizontalGroup(layout.createParallelGroup()
				.addComponent(statusLabel).addComponent(titleLabel)
				.addComponent(titleField).addComponent(authorLabel)
				.addComponent(authorField).addComponent(descriptionLabel)
				.addComponent(descriptionField).addComponent(gameLabel)
				.addComponent(translatorBox).addComponent(moduleLabel)
				.addComponent(modulePanel));

		// vertical perspective
		layout.setVerticalGroup(layout.createSequentialGroup()
				.addComponent(statusLabel).addComponent(titleLabel)
				.addComponent(titleField).addComponent(authorLabel)
				.addComponent(authorField).addComponent(descriptionLabel)
				.addComponent(descriptionField).addComponent(gameLabel)
				.addComponent(translatorBox).addComponent(moduleLabel)
				.addComponent(modulePanel));

		onFinish = new Runnable() {
			@Override
			public void run() {
				final File location = new File(moduleField.getText());
				final String title = titleField.getText();
				final String author = authorField.getText();

				final StatusManager statusManager = StatusManager.getInstance();
				final TranslatorManager translatorManager;
				final Translator selectedTranslator;
				final Translator oldTranslator;

				final GameModule module;

				translatorManager = TranslatorManager.getInstance();

				selectedTranslator = (Translator) translatorBox
						.getSelectedItem();
				oldTranslator = translatorManager.getActiveTranslator();

				statusManager.setStatus("Creating New Story ...");
				translatorManager.setActiveTranslator(selectedTranslator);

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
					translatorManager.setActiveTranslator(oldTranslator);
					statusManager
							.setStatus("Story creation aborted: module failed to load.");

					return;
				} else {
					final StoryModel model;

					model = new StoryModel(module, title, author,
							selectedTranslator, new ArrayList<LibraryModel>());

					SEModelManager.getInstance().addAndActivate(model);
				}
			}
		};

		// Create the wizard
		wizard = new WizardDialog(TITLE, newStoryPanel, onFinish);

		// Listeners
		titleField.getDocument().addDocumentListener(new DocumentListener() {
			@Override
			public void changedUpdate(DocumentEvent e) {
			}

			@Override
			public void insertUpdate(DocumentEvent e) {
				this.updateButton();
			}

			@Override
			public void removeUpdate(DocumentEvent e) {
				this.updateButton();
			}

			private void updateButton() {
				final File location = new File(moduleField.getText());

				if (location.exists()
						&& translatorBox.getSelectedItem() != null) {
					statusLabel.setText("");
					statusLabel.setIcon(null);
					wizard.setFinishEnabled(!titleField.getText().isEmpty());
				} else
					wizard.setFinishEnabled(false);
			}
		});

		moduleButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				final Translator translator;
				final File location;
				File defaultLocation;

				translator = ((Translator) translatorBox.getSelectedItem());

				if (translator == null)
					return;

				// Build the filter based on the translator selected
				defaultLocation = translator
						.getPathProperty(DescriptionKeys.GAME_DIRECTORY);

				if (defaultLocation == null || !defaultLocation.exists())
					defaultLocation = translator.getLocation().getParentFile();

				if (!translator.moduleLoadsDirectories()) {
					location = WindowFactory.getInstance().showFileChooser(
							"Select", "", translator.createModuleFileFilter(),
							defaultLocation);
				} else {
					location = WindowFactory
							.getInstance()
							.showDirectoryChooser("Select", "", defaultLocation);
				}

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
				this.updateButton();
			}

			@Override
			public void removeUpdate(DocumentEvent e) {
				this.updateButton();
			}

			private void updateButton() {
				final File location = new File(moduleField.getText());

				final String status;
				final Icon icon;
				final boolean finishEnabled;

				if (location.exists()) {
					status = "";
					icon = null;
					finishEnabled = !titleField.getText().isEmpty();
				} else {
					status = "Module does not exist";
					icon = UIManager.getIcon("OptionPane.errorIcon");
					finishEnabled = false;
				}

				statusLabel.setText(status);
				statusLabel.setIcon(icon);

				wizard.setFinishEnabled(finishEnabled);
			}
		});

		translatorBox.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				// a bit of a hack to force the module text field to revalidate
				moduleField.setText(moduleField.getText());

				if (translatorBox.getSelectedItem() != null) {
					moduleField.setEnabled(true);
					moduleButton.setEnabled(true);
				}
			}
		});

		// Display the wizard
		wizard.setVisible(true);
	}

	/**
	 * Creates a selection box for all of the available translators.
	 * 
	 * @return
	 */
	private JComboBox createTranslatorSelectionBox() {
		final Vector<Translator> translators;
		final JComboBox translatorBox;

		translators = new Vector<Translator>(TranslatorManager.getInstance()
				.getTranslators());

		Collections.sort(translators, new Comparator<Translator>() {
			@Override
			public int compare(Translator t1, Translator t2) {
				return t1.getName().compareTo(t2.getName());
			}
		});

		translatorBox = new JComboBox(translators);

		translatorBox.setRenderer(new TranslatorListRenderer());
		translatorBox.setSelectedIndex(-1);

		return translatorBox;
	}

	/**
	 * WizardDialog represents a generic Wizard navigator. It uses the current
	 * frame, the given JPanel, and the runnable to execute when completed.
	 * 
	 * The reason we keep this is separate from {@link DialogBuilder} is because
	 * the {@link Progress} aspect links to the
	 * {@link #actionPerformed(ActionEvent)} method to show a progress bar.
	 * 
	 * @author mfchurch
	 * @author kschenk
	 * 
	 */
	@SuppressWarnings("serial")
	public class WizardDialog extends JDialog {
		private final JButton finishButton;

		private WizardDialog(String title, JPanel panel, final Runnable finish) {
			super(WindowFactory.getInstance().getCurrentFrame(), true);
			this.finishButton = new JButton("Finish");

			final JPanel buttonPanel;
			final JButton cancelButton;

			final Box buttonBox;

			buttonPanel = new JPanel();
			buttonBox = new Box(BoxLayout.X_AXIS);
			cancelButton = new JButton("Cancel");

			panel.setBorder(new EmptyBorder(new Insets(5, 10, 5, 10)));

			cancelButton.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {

					WizardDialog.this.setVisible(false);
					WizardDialog.this.dispose();
				}
			});

			this.finishButton.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					WizardDialog.this.setVisible(false);
					WizardDialog.this.dispose();
					WindowFactory.showProgressBar("Loading...", finish);
				}
			});

			// Finalize the layout
			buttonPanel.setLayout(new BorderLayout());
			buttonPanel.add(new JSeparator(), BorderLayout.NORTH);

			buttonBox.setBorder(new EmptyBorder(new Insets(5, 10, 5, 10)));
			buttonBox.add(Box.createHorizontalStrut(30));
			buttonBox.add(this.finishButton);
			buttonBox.add(Box.createHorizontalStrut(10));
			buttonBox.add(cancelButton);
			buttonPanel.add(buttonBox, BorderLayout.EAST);

			this.add(panel, BorderLayout.NORTH);
			this.add(buttonPanel, BorderLayout.SOUTH);

			this.setResizable(false);
			this.setFinishEnabled(false);
			this.setLocationRelativeTo(this.getParent());
			this.setTitle(title);
		}

		/**
		 * Enables/Disables the finished button
		 * 
		 * @param value
		 */
		private void setFinishEnabled(boolean value) {
			this.finishButton.setEnabled(value);
			this.pack();
		}
	}
}
