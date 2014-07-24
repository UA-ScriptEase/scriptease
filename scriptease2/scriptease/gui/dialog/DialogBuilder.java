package scriptease.gui.dialog;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
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
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import scriptease.ScriptEase;
import scriptease.controller.StoryComponentUtils;
import scriptease.gui.ExceptionDialog;
import scriptease.gui.StatusManager;
import scriptease.gui.WindowFactory;
import scriptease.model.StoryComponent;
import scriptease.model.atomic.Note;
import scriptease.model.complex.ComplexStoryComponent;
import scriptease.model.complex.StoryNode;
import scriptease.model.semodel.SEModelManager;
import scriptease.model.semodel.StoryModel;
import scriptease.model.semodel.librarymodel.LibraryModel;
import scriptease.translator.Translator;
import scriptease.translator.Translator.DescriptionKeys;
import scriptease.translator.TranslatorManager;
import scriptease.translator.io.model.GameModule;

/**
 * @author Previous ScriptEase devs
 * @author jyuen
 * @author zturchan
 */
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
	 * Creates a exception dialog with the given parent frame
	 * 
	 * @param parent
	 *            The parent frame
	 * @param title
	 *            The title of the dialog
	 * @param messageBrief
	 *            The bolded part of the message
	 * @param message
	 *            The message description
	 * @param dialogIcon
	 *            The icon to display along side the message
	 * @param e
	 *            The exception being thrown
	 * @return
	 */
	public ExceptionDialog createExceptionDialog(Frame parent, String title,
			String messageBrief, String message, Icon dialogIcon, Exception e) {
		return new ExceptionDialog(parent, title, messageBrief, message,
				dialogIcon, e);
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

		final int TEXTAREA_COLUMNS = 40;
		final int TEXTAREA_ROWS = 10;

		final JPanel newStoryPanel = new JPanel();

		final JLabel authorLabel = new JLabel("Author: ");
		final JLabel titleLabel = new JLabel("Title: ");
		final JLabel descriptionLabel = new JLabel("Description: ");

		final JTextField authorField = new JTextField(TEXTFIELD_COLUMNS);
		final JTextField titleField = new JTextField(TEXTFIELD_COLUMNS);
		final JTextArea descriptionField = new JTextArea(TEXTAREA_ROWS,
				TEXTAREA_COLUMNS);

		// final JTextField descriptionField = new
		// JTextField(TEXTFIELD_COLUMNS);

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
				final String description = descriptionField.getText();
				final LibraryModel model;

				TranslatorManager.getInstance().setActiveTranslator(translator);

				model = new LibraryModel(title, author, description, translator);

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
				final String description = descriptionField.getText();

				final TranslatorManager translatorManager;
				final Translator selectedTranslator;
				final Translator oldTranslator;

				final GameModule module;

				translatorManager = TranslatorManager.getInstance();

				selectedTranslator = (Translator) translatorBox
						.getSelectedItem();
				oldTranslator = translatorManager.getActiveTranslator();

				translatorManager.setActiveTranslator(selectedTranslator);

				if (selectedTranslator == null) {
					WindowFactory
							.getInstance()
							.showProblemDialog("No translator",
									"No translator was chosen. I can't make a story without it.");
					StatusManager.getInstance().setTemp(
							"Story creation aborted: no translator chosen.");
					return;
				}

				module = selectedTranslator.loadModule(location);

				if (module == null) {
					translatorManager.setActiveTranslator(oldTranslator);
					StatusManager.getInstance().setTemp(
							"Story creation aborted: module failed to load.");

					return;
				} else {
					final StoryModel model;
					final String compatibleVersion;

					compatibleVersion = ScriptEase.getInstance().getVersion();

					model = new StoryModel(module, title, author, description,
							compatibleVersion, selectedTranslator,
							new ArrayList<LibraryModel>());

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

		Collections.sort(translators);

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

	/**
	 * Shows information about the library that is about to be added.
	 */
	public void showAddLibraryInfoDialog(final LibraryModel library) {
		final String ADD_LIBRARY = "Are you sure you want to add "
				+ library.getTitle() + "?";

		final String title;
		final String author;
		final String information;

		final String message;

		title = "<h3>" + library.getTitle() + "</h3>";
		author = "<h4>" + library.getAuthor() + "</h4>";
		information = library.getDescription();

		message = "<html>" + title + author + "<br>" + information
				+ "<br><br><b>" + ADD_LIBRARY + "</b></html>";

		if (WindowFactory.getInstance().showYesNoConfirmDialog(message,
				ADD_LIBRARY)) {
			final StoryModel model;

			model = (StoryModel) SEModelManager.getInstance().getActiveModel();

			model.addLibrary(library);
		}
	}

	/**
	 * Warns the user that a library is about to be removed.
	 */
	public void showRemoveLibraryInfoDialog(final LibraryModel library,
			StoryModel story) {
		final String REMOVE_LIBRARY = "Are you sure you want to remove "
				+ library.getTitle() + "?";
		final String message;

		final Collection<StoryComponent> inLibrary = new ArrayList<StoryComponent>();

		for (StoryNode node : story.getRoot().getDescendants()) {
			for (StoryComponent component : StoryComponentUtils
					.getAllDescendants(node))
				if (component.getLibrary() == library)
					inLibrary.add(component);
		}

		if (!inLibrary.isEmpty()) {
			String componentList = "";

			for (StoryComponent component : inLibrary) {
				componentList += component.getDisplayText() + "<br>";
			}

			message = "<html>You are about to remove the library: <b>"
					+ library.getTitle()
					+ "</b>.<br><br><b>WARNING:</b><br>Removing this library will remove the following story components and their contents from the story:<br><br>"
					+ componentList + "<br>Are you sure you want to proceed?";

			if (WindowFactory.getInstance().showYesNoConfirmDialog(message,
					REMOVE_LIBRARY)) {
				story.removeLibrary(library);
				for (StoryComponent component : inLibrary) {
					final StoryComponent compOwner = component.getOwner();

					if (compOwner instanceof ComplexStoryComponent
							&& compOwner.getLibrary() != library) {
						final ComplexStoryComponent owner;
						final StoryComponent sibling;

						owner = (ComplexStoryComponent) compOwner;

						sibling = owner.getChildAfter(component);

						owner.removeStoryChild(component);
						owner.addStoryChildBefore(
								new Note(LibraryModel.getNonLibrary(),
										"Removed component from " + library
												+ " library: "
												+ component.getDisplayText()),
								sibling);
					}
				}
			}
		} else
			// We don't need to show the dialogue since no components are in the
			// library.
			story.removeLibrary(library);
	}

	/**
	 * Dialog box that shows the story Title, Author and Description
	 */
	public void showStoryPropertiesDialog() {
		final String DIALOG_TITLE = "Story Properties";
		final String TITLE_TEXT = "Title: ";
		final String AUTHOR_TEXT = "Author: ";
		final String DESCRIPTION_TEXT = "Description: ";
		final String OKAY_TEXT = "Okay";
		final String CANCEL_TEXT = "Cancel";

		final int TEXTFIELD_COLUMNS = 20;

		final JDialog dialog;

		final JPanel titlePanel;
		final JPanel authorPanel;
		final JPanel descriptionPanel;
		final JPanel buttonPanel;

		final JTextField titleField;
		final JTextField authorField;
		final JTextField descriptionField;

		final JLabel titleLabel;
		final JLabel authorLabel;
		final JLabel descriptionLabel;

		final JButton okButton;
		final JButton cancelButton;

		final StoryModel model = SEModelManager.getInstance()
				.getActiveStoryModel();

		if (model == null)
			return;

		dialog = new JDialog(WindowFactory.getInstance().getCurrentFrame(),
				DIALOG_TITLE);

		titlePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		authorPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		descriptionPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));

		titleLabel = new JLabel(TITLE_TEXT);
		authorLabel = new JLabel(AUTHOR_TEXT);
		descriptionLabel = new JLabel(DESCRIPTION_TEXT);
		okButton = new JButton(OKAY_TEXT);
		cancelButton = new JButton(CANCEL_TEXT);

		titleField = new JTextField(model.getTitle(), TEXTFIELD_COLUMNS);
		authorField = new JTextField(model.getAuthor(), TEXTFIELD_COLUMNS);
		descriptionField = new JTextField(model.getDescription(),
				TEXTFIELD_COLUMNS);

		titlePanel.add(titleLabel);
		titlePanel.add(titleField);

		authorPanel.add(authorLabel);
		authorPanel.add(authorField);

		descriptionPanel.add(descriptionLabel);
		descriptionPanel.add(descriptionField);

		buttonPanel.add(okButton);
		buttonPanel.add(cancelButton);

		okButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				model.setTitle(titleField.getText());
				model.setAuthor(authorField.getText());
				model.setDescription(descriptionField.getText());
				dialog.dispose();
			}
		});

		cancelButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				dialog.dispose();
			}
		});
		// Create the main dialog box.
		dialog.setTitle(DIALOG_TITLE);
		dialog.getContentPane().setLayout(
				new BoxLayout(dialog.getContentPane(), BoxLayout.Y_AXIS));

		dialog.add(titlePanel);
		dialog.add(authorPanel);
		dialog.add(descriptionPanel);
		dialog.add(buttonPanel);

		dialog.setResizable(false);
		dialog.pack();
		dialog.setLocationRelativeTo(null);
		dialog.setVisible(true);

	}
}
