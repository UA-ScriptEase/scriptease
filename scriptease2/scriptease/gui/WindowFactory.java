package scriptease.gui;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.KeyboardFocusManager;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.WindowEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.swing.BorderFactory;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.ParallelGroup;
import javax.swing.GroupLayout.SequentialGroup;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenuBar;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JRootPane;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;

import scriptease.ScriptEase;
import scriptease.controller.StoryAdapter;
import scriptease.controller.logger.NetworkHandler;
import scriptease.controller.modelverifier.problem.StoryProblem;
import scriptease.controller.observer.ResourceTreeAdapter;
import scriptease.controller.observer.SEModelEvent;
import scriptease.controller.observer.SEModelObserver;
import scriptease.gui.action.libraryeditor.MergeLibraryAction;
import scriptease.gui.component.UserInformationPane;
import scriptease.gui.component.UserInformationPane.UserInformationType;
import scriptease.gui.dialog.DialogBuilder;
import scriptease.gui.dialog.PreferencesDialog;
import scriptease.gui.pane.PanelFactory;
import scriptease.gui.pane.ResourcePanel;
import scriptease.gui.storycomponentpanel.StoryComponentPanel;
import scriptease.gui.storycomponentpanel.StoryComponentPanelFactory;
import scriptease.gui.ui.ScriptEaseUI;
import scriptease.model.StoryComponent;
import scriptease.model.atomic.KnowIt;
import scriptease.model.atomic.knowitbindings.KnowItBindingFunction;
import scriptease.model.semodel.SEModel;
import scriptease.model.semodel.SEModelManager;
import scriptease.model.semodel.StoryModel;
import scriptease.model.semodel.librarymodel.LibraryModel;
import scriptease.translator.Translator;
import scriptease.translator.TranslatorManager;
import scriptease.util.StringOp;

/**
 * The WindowFactory class is a facade responsible for all of the windows,
 * pop-ups and dialogs that ScriptEase uses. Other classes are to request the
 * window manager to perform any window displays but <b>not closes</b>. Those
 * windows opened through the window manager will deal with closing themselves. <br>
 * <br>
 * If, in the future, we have multiple perspectives (and a single frame) or
 * other such major frame states, the window manager should probably deal with
 * that, too. Similar story for dynamic GUI operations.<br>
 * <br>
 * WindowFactory implements the singleton design pattern.
 * 
 * @author remiller
 * @author lari
 * @author kschenk
 * @author jyuen
 */
public final class WindowFactory {
	private static final String CODE_GENERATION_PROBLEM = "Code Generation Problem";
	private static final String ABOUT_SCRIPTEASE_TITLE = "About ScriptEase II";

	public static final String ABOUT_SCRIPTEASE_MESSAGE = "<html><b><font size=\"4\">ScriptEase II</font></b><br>"
			+ "<font size=\"2\">Version: Beta "
			+ ScriptEase.getInstance().getVersion()
			+ "<br>Revision: "
			+ ScriptEase.getInstance().getCommitHash()
			+ "</font><br><br>"
			+ "Game Scripting and Code Generation for any game!<br><br>"
			+ "<b>Contributors:</b><br>"
			+ "<br><b>Professors:</b> Mike Carbonaro, Jonathan Schaeffer, Duane Szafron"
			+ "<br><b>Ph.D. Students:</b> Neesha Desai, Richard Zhao"
			+ "<br><b>Programmer Analysts:</b> Matthew Church, Jason Duncan, Eric Graves, Adel Lari, Robin Miller, Kevin Schenk, Jessica Yuen"
			+ "<br><b>Artists:</b> Wei Li, Jamie Schmitt"
			+ "<br><b>Interns:</b> Delia Cormier, Alex Czeto, Kirsten Svidal"
			+ "<br><br>Visit us online at http://www.cs.ualberta.ca/~script/<br></html>";
	private static final String LAST_DIRECTORY_KEY = "LastDirectory_FilterType";
	private static final String CONFIRM_OVERWRITE_TITLE = "Save As";
	private static final String CONFIRM_OVERWRITE_TEXT = "Are you sure you want to overwrite it?";
	private static final String CONFIRM_CLOSE_TITLE = "Save Resources";
	private static final String CONFIRM_CLOSE_TEXT = "Save Changes?";
	private static final String CONFIRM_MODEL_TITLE = "Confirm Solutions";
	private static final String CRITICAL_ERROR_TITLE = "Critical Error";
	private static final String ERROR_MESSAGE = "ScriptEase has encountered a critical error and will now exit.\nWe apologize for this problem, but there's nothing to do but crash.";
	private static final String RETRY_TITLE_START = "Problem with";
	private static final String CANCEL = "Cancel";

	private JFrame mainFrame = null;

	/**
	 * The sole instance of this class as per the singleton pattern.
	 */
	private static final WindowFactory instance = new WindowFactory();

	/**
	 * exceptionShowing is a safety flag to prevent multiple ExceptionDialogs
	 * being opened simultaneously.
	 */
	private static boolean exceptionShowing = false;

	/**
	 * @return the sole instance of WindowManager
	 */
	public static WindowFactory getInstance() {
		return WindowFactory.instance;
	}

	/**
	 * Shows the main ScriptEase frame.
	 */
	public void buildAndShowMainFrame() {
		this.mainFrame = this.buildScriptEaseFrame(ScriptEase.TITLE + " "
				+ ScriptEase.getInstance().getVersion());
		this.mainFrame.setJMenuBar(MenuFactory.createMainMenuBar(null));

		if (!this.mainFrame.isVisible())
			this.mainFrame.setVisible(true);
	}

	/**
	 * Builds and shows a custom Frame where the panel content is provided by
	 * the user.
	 * 
	 * @param panel
	 *            The panel contents to be displayed on the frame.
	 * 
	 * @param title
	 *            The title of the window.
	 * 
	 * @param resizable
	 *            Whether the window should be resizable.
	 */
	public void buildAndShowCustomFrame(JPanel panel, String title,
			boolean resizable) {
		JFrame frame;

		frame = new JFrame(title);

		frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		frame.getContentPane().add(panel);
		frame.pack();
		frame.setVisible(true);
		frame.setResizable(resizable);
		frame.setLocationRelativeTo(null);
	}

	private static boolean errorBoxActivated = false;

	/**
	 * Shows a user error box. This is different from a ScriptEase error. A user
	 * error is something that the user tries to perform that is illegal in
	 * ScriptEase. For example, dragging a effect into a StoryPoint component
	 * panel.
	 * 
	 * @param message
	 */
	public void showUserInformationBox(String message, UserInformationType type) {
		if (!errorBoxActivated) {
			final UserInformationPane userErrorPane = new UserInformationPane(
					message, type);

			WindowFactory.getInstance().getCurrentFrame()
					.setGlassPane(userErrorPane);

			userErrorPane.setOpaque(false);
			userErrorPane.setVisible(true);
			errorBoxActivated = true;
		} else {
			((UserInformationPane) WindowFactory.getInstance()
					.getCurrentFrame().getGlassPane()).restart(message, type);
		}
	}

	/**
	 * Shows the Error Dialog if it is not already showing. The Error Dialog is
	 * modal and will sit on top of any other window when shown.<br>
	 * <br>
	 * Once dismissed, the ScriptEase will exit with an error code.
	 */
	public void showErrorDialog() {
		// do this on the swing queue so that we can be assured that it isn't
		// being called as a result of a problem in the SEFrame constructor.
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				JOptionPane.showMessageDialog(WindowFactory.this.mainFrame,
						WindowFactory.ERROR_MESSAGE,
						WindowFactory.CRITICAL_ERROR_TITLE,
						JOptionPane.ERROR_MESSAGE);

				ScriptEase.getInstance()
						.exit(ScriptEase.ERROR_CODE_UNSPECIFIED);
			}
		});
	}

	/**
	 * Shows a simple question dialog where the user must select one item from a
	 * list.
	 * 
	 * @param message
	 *            The message to display.
	 * @param title
	 *            The title of the dialog.
	 * @param options
	 *            The list of options to give to the user.
	 * @return The index into <code>options</code> that the user selected, or
	 *         {@link JOptionPane#CLOSED_OPTION} if the user closed the dialog
	 */
	public int showOptionsDialog(String message, String title, Object[] options) {
		return JOptionPane.showOptionDialog(this.mainFrame, message, title,
				JOptionPane.DEFAULT_OPTION, JOptionPane.QUESTION_MESSAGE, null,
				options, null);
	}

	/**
	 * Shows a dialog where the user can choose yes or no.
	 * 
	 * @param message
	 * @param title
	 * @return
	 */
	public boolean showYesNoConfirmDialog(String message, String title) {
		final int option;

		option = JOptionPane.showConfirmDialog(this.mainFrame, message, title,
				JOptionPane.YES_NO_OPTION);

		return option == JOptionPane.YES_OPTION;
	}

	/**
	 * Shows the progress bar with the passed in text for the duration of the
	 * passed in runnable. Calling this method when a progress bar is already
	 * showing will create a new progress bar in front of it, giving the effect
	 * of the text changing.
	 * 
	 * @param text
	 *            The text to be shown on the progress bar. This is commonly
	 *            "Loading...", but can be anything you set it to.
	 * 
	 * @param run
	 *            The runnable to run. The progress bar will show as long as
	 *            this is running.
	 * 
	 * @see #hideProgressBar()
	 */
	public static void showProgressBar(final String text, final Runnable run) {
		WindowFactory.getInstance().getCurrentFrame()
				.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

		final JProgressBar progressBar;
		final JDialog progressBarDialog;
		final SwingWorker<Void, Void> worker;

		progressBar = new JProgressBar();
		progressBarDialog = new JDialog(WindowFactory.getInstance()
				.getCurrentFrame(), true);

		// The SwingWorker allows us to use the runnable in a new thread, which
		// prevents the JDialog from blocking the program.
		worker = new SwingWorker<Void, Void>() {
			@Override
			protected Void doInBackground() throws Exception {
				StatusManager.getInstance().set(text);
				run.run();
				return null;
			}

			@Override
			protected void done() {
				StatusManager.getInstance().clear(text);
				progressBarDialog.setVisible(false);
				WindowFactory.getInstance().getCurrentFrame()
						.setCursor(Cursor.getDefaultCursor());
			}
		};

		progressBar.setIndeterminate(true);
		progressBar.setPreferredSize(new Dimension(200, 30));
		progressBar.setOpaque(false);
		progressBar.setString(text);
		progressBar.setStringPainted(true);

		progressBarDialog.add(progressBar);

		progressBarDialog.setUndecorated(true);
		progressBarDialog.getRootPane().setOpaque(false);
		progressBarDialog.getRootPane()
				.setWindowDecorationStyle(JRootPane.NONE);
		progressBarDialog.setSize(progressBarDialog.getPreferredSize());
		progressBarDialog.setLocationRelativeTo(progressBarDialog.getParent());

		worker.execute();
		progressBarDialog.setVisible(true);
	}

	/**
	 * Shows the Exception Dialog if it is not already showing. The Error Dialog
	 * is modal and will sit on top of any other window when shown.
	 * 
	 * @param title
	 *            The title of the dialog
	 * @param messageBrief
	 *            The bold part of the message
	 * @param message
	 *            The message shown
	 * @param dialogIcon
	 *            The icon displayed for the exception
	 * @param e
	 *            Exception that is being thrown - can be null if it is not
	 *            known.
	 */
	public void showExceptionDialog(String title, String messageBrief,
			String message, Icon dialogIcon, Exception e) {
		// safety check to avoid infinite dialog spawns
		if (WindowFactory.exceptionShowing)
			return;

		JDialog dialog = DialogBuilder.getInstance().createExceptionDialog(
				this.mainFrame, title, messageBrief, message, dialogIcon, e);
		WindowFactory.exceptionShowing = true;

		dialog.setLocationRelativeTo(dialog.getParent());
		dialog.setVisible(true);

		WindowFactory.exceptionShowing = false;
	}

	public void showNewStoryWizardDialog() {
		DialogBuilder.getInstance().showNewStoryWizard();
	}

	/**
	 * Creates a new library for the passed in translator.
	 * 
	 * @param translator
	 *            The translator to create a library for. This must not be null.
	 */
	public void showNewLibraryWizardDialog(Translator translator) {
		DialogBuilder.getInstance().showNewLibraryWizard(translator);
	}

	public void showCompileProblems(Collection<StoryProblem> storyProblems) {
		final JPanel panel = new JPanel();

		final Collection<StoryProblem> visibleProblems;

		visibleProblems = new ArrayList<StoryProblem>();

		for (StoryProblem problem : storyProblems) {
			// Show only visible problems
			if (problem.shouldNotify())
				visibleProblems.add(problem);
		}

		final int numberOfProblems = visibleProblems.size();

		panel.setLayout(new GridLayout(numberOfProblems + 2, 1));

		{
			final String problem;
			final String has;

			if (numberOfProblems == 1) {
				problem = " problem ";
				has = "has ";
			} else {
				problem = " problems ";
				has = "have ";
			}

			panel.add(new JLabel(numberOfProblems + problem + has
					+ "been detected. The following" + problem
					+ "must be resolved to generate code:"));
		}

		panel.add(new JPanel());

		int problemCount = 1;
		for (StoryProblem problem : visibleProblems) {
			final String problemText;
			final String description;

			problemText = "<html><b>Problem " + problemCount++ + ": </b>";
			description = StringOp.makeXMLSafe(problem.getDescription());

			panel.add(new JLabel(problemText + description));
		}

		JOptionPane.showMessageDialog(this.mainFrame, panel,
				WindowFactory.CODE_GENERATION_PROBLEM,
				JOptionPane.WARNING_MESSAGE);
	}

	/**
	 * Show a window detailing the given problems with the model, and ask the
	 * user if they wish to apply the changes in order to continue.
	 * 
	 * @param storyProblems
	 * @return
	 */
	public boolean showSolvableProblems(Collection<StoryProblem> storyProblems) {
		JPanel panel = new JPanel();

		// Show only visible problems
		Collection<StoryProblem> visibleProblems = new ArrayList<StoryProblem>();
		for (StoryProblem problem : storyProblems) {
			if (problem.shouldNotify())
				visibleProblems.add(problem);
		}

		panel.setLayout(new GridLayout(visibleProblems.size() + 2, 1));
		panel.add(new JLabel(
				"Problems have been detected. In order to proceed the following actions must be taken:\n"));

		for (StoryProblem problem : visibleProblems) {
			final StoryComponent component = problem.getComponent();
			final String description = problem.getDescription();
			final JPanel problemPanel = new JPanel();
			problemPanel.setOpaque(true);
			problemPanel.setBackground(Color.white);
			problemPanel.setBorder(BorderFactory.createLineBorder(Color.black,
					2));

			// if available, show the panel being changed, otherwise use the
			// display text
			component.process(new StoryAdapter() {
				@Override
				public void processKnowIt(final KnowIt knowIt) {

					// Show describeIts (KnowItBindingFunction) differently
					final StoryComponent owner = knowIt.getOwner();
					if (owner != null
							&& !(knowIt.getBinding() instanceof KnowItBindingFunction))
						this.defaultProcess(owner);
					else
						this.defaultProcess(knowIt);
				}

				@Override
				protected void defaultProcess(StoryComponent component) {
					StoryComponentPanel componentPanel = StoryComponentPanelFactory
							.getInstance().buildStoryComponentPanel(component);
					if (componentPanel != null) {
						// Setup the panel
						componentPanel.setEditable(false);
						componentPanel.setOpaque(false);
						componentPanel.setShowChildren(false);
						componentPanel.setRemovable(false);
						componentPanel.setSelectable(false);
						problemPanel.add(componentPanel);
						problemPanel.add(new JLabel(" : " + description));
					} else
						problemPanel.add(new JLabel(component.getDisplayText()
								+ " : " + description));
				}
			});
			panel.add(problemPanel);
		}

		panel.add(new JLabel("Do you wish to continue?"));

		int choice = JOptionPane.showConfirmDialog(this.mainFrame, panel,
				WindowFactory.CONFIRM_MODEL_TITLE, JOptionPane.YES_NO_OPTION);
		return choice == JOptionPane.OK_OPTION;
	}

	/**
	 * Shows a confirm dialog for overwriting a file.
	 * 
	 * @return True if the user confirmed the overwrite, false otherwise.
	 */
	public boolean showConfirmOverwrite(File location) {
		int choice = JOptionPane.showConfirmDialog(this.mainFrame,
				"The file: \"" + location.getAbsolutePath()
						+ "\" already exists.\n"
						+ WindowFactory.CONFIRM_OVERWRITE_TEXT,
				WindowFactory.CONFIRM_OVERWRITE_TITLE,
				JOptionPane.YES_NO_OPTION);
		return choice == JOptionPane.OK_OPTION;
	}

	/**
	 * Shows a confirm dialog for overwriting a file.
	 * 
	 * @return One of <code>JOptionPane.YES_OPTION</code>,
	 *         <code>JOptionPane.NO_OPTION</code>, or
	 *         <code>JOptionPane.CANCEL_OPTION</code>
	 */
	public int showConfirmClose(SEModel model) {
		int choice = JOptionPane.showConfirmDialog(this.mainFrame,
				"The story \"" + model + "\" has been modified. "
						+ WindowFactory.CONFIRM_CLOSE_TEXT,
				WindowFactory.CONFIRM_CLOSE_TITLE,
				JOptionPane.YES_NO_CANCEL_OPTION);

		return choice;
	}

	/**
	 * Same as {@link #showRetryProblemDialog(String, String, String)} but using
	 * the default accepting text.
	 * 
	 * @see #showRetryProblemDialog(String, String, String)
	 */
	public boolean showRetryProblemDialog(String operationName, String message) {
		return this.showRetryProblemDialog(operationName, message, null);
	}

	/**
	 * Shows a dialog that tells the user the standard 'About' information
	 * (version, authors, etc.)
	 */
	public void showAboutScreen() {
		JOptionPane.showMessageDialog(this.mainFrame,
				WindowFactory.ABOUT_SCRIPTEASE_MESSAGE,
				WindowFactory.ABOUT_SCRIPTEASE_TITLE,
				JOptionPane.INFORMATION_MESSAGE);
	}

	/**
	 * Shows a dialog that will let the user either try to retry an action or
	 * cancel.
	 * 
	 * @param operationName
	 *            The name of the action that has failed.
	 * @param message
	 *            The message to display. If this is null or an empty string,
	 *            then the actionName will be used in a default error message.
	 * @param acceptText
	 *            The button text to display on the accept button.
	 * @return True if the user has selected retry, false if they select cancel.
	 */
	public boolean showRetryProblemDialog(String operationName, String message,
			String acceptText) {
		if (message == null || message.equals(""))
			message = "ScriptEase has encountered a problem during "
					+ operationName + ".";

		if (acceptText == null || acceptText.equals("")) {
			message += "\n\nWould you like to try again?";
			acceptText = "Retry";
		}

		String[] values = { acceptText, WindowFactory.CANCEL };

		int choice = JOptionPane.showOptionDialog(this.mainFrame, message,
				WindowFactory.RETRY_TITLE_START + " " + operationName,
				JOptionPane.OK_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE,
				null, values, values[0]);
		return choice == JOptionPane.OK_OPTION;
	}

	/**
	 * Shows a message dialog dressed as a warning that is anchored to the main
	 * window.
	 * 
	 * @param title
	 *            The frame title of the problem dialog box. The title is
	 *            prepended with "ScriptEase: "
	 * @param message
	 *            The message to display to the user. Try to keep it friendly.
	 *            Remember, this dialog tells them bad news - it's a good idea
	 *            to not be uncaring and robotic.
	 */
	public void showProblemDialog(final String title, final String message) {
		// do this on the swing queue so that we can be assured that it isn't
		// being called as a result of a problem in the SEFrame constructor.
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				JOptionPane.showMessageDialog(WindowFactory.this.mainFrame,
						message, "ScriptEase: " + title,
						JOptionPane.ERROR_MESSAGE);
			}
		});
	}

	/**
	 * Shows a message dialog dressed as a warning that is anchored to the main
	 * window.
	 * 
	 * @param title
	 *            The frame title of the problem dialog box. The title is
	 *            prepended with "ScriptEase: "
	 * @param message
	 *            The message to display to the user. Try to keep it friendly.
	 *            Remember, this dialog tells them bad news - it's a good idea
	 *            to not be uncaring and robotic.
	 */
	public void showWarningDialog(final String title, final String message) {
		// do this on the swing queue so that we can be assured that it isn't
		// being called as a result of a problem in the SEFrame constructor.
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				JOptionPane.showMessageDialog(WindowFactory.this.mainFrame,
						message, "ScriptEase: " + title,
						JOptionPane.WARNING_MESSAGE);
			}
		});
	}

	/**
	 * Shows a message dialog that is anchored to the main window.
	 * 
	 * @param title
	 *            The frame title of the information dialog box. The title is
	 *            prepended with "ScriptEase: "
	 * @param message
	 */
	public void showInformationDialog(final String title, final String message) {
		// do this on the swing queue so that we can be assured that it isn't
		// being called as a result of a problem in the SEFrame constructor.
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				JOptionPane.showMessageDialog(WindowFactory.this.mainFrame,
						message, "ScriptEase: " + title,
						JOptionPane.INFORMATION_MESSAGE);
			}
		});
	}

	public File showFileChooser(String operation, String defaultFileName,
			FileFilter filter) {
		return this.showFileChooser(operation, defaultFileName, filter, null);
	}

	/**
	 * Shows a directory chooser that is child of the main ScriptEase frame. The
	 * chooser is customized to use the given operation name as the button's
	 * text and frame title. For that reason, the operation name should be
	 * short, like "Save Story".
	 * 
	 * @param operation
	 *            The name of the operation to be performed.
	 * 
	 * @return The file selected. This <b>can</b> be null if the chooser window
	 *         is dismissed without accepting (closed or cancelled).
	 */
	public File showDirectoryChooser(String operation, String defaultFileName,
			File directoryPath) {
		final JFileChooser chooser = new JFileChooser();

		chooser.resetChoosableFileFilters();
		chooser.setAcceptAllFileFilterUsed(false);
		chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

		chooser.setCurrentDirectory(directoryPath);

		final int buttonChoice;
		final File choice;

		buttonChoice = chooser.showDialog(this.mainFrame, operation);

		if (buttonChoice == JFileChooser.APPROVE_OPTION) {
			choice = chooser.getSelectedFile();
		} else
			choice = null;

		return choice;
	}

	/**
	 * Shows a customized file chooser that is a child of the main ScriptEase
	 * frame. The chooser is customized to use the given operation name as the
	 * button's text and frame title. For that reason, the operation name should
	 * be short, like "Save Story".
	 * 
	 * @param operation
	 *            The name of the operation to be performed.
	 * @param extensions
	 *            The file extensions to accept in the filter
	 * 
	 * 
	 * @return The file selected. This <b>can</b> be null if the chooser window
	 *         is dismissed without accepting (closed or cancelled).
	 */
	public File showFileChooser(String operation, String defaultFileName,
			FileFilter filter, File directoryPath) {
		final JFileChooser chooser;

		chooser = new JFileChooser();

		final ScriptEase se = ScriptEase.getInstance();
		final int buttonChoice;
		final File choice;
		File lastChoicePath = null;
		String lastDirectory = null;
		String lastDirectoryKey = null;

		chooser.resetChoosableFileFilters();
		chooser.setAcceptAllFileFilterUsed(true);
		if (filter != null) {
			lastDirectoryKey = WindowFactory.LAST_DIRECTORY_KEY
					+ StringOp.makeAlphaNumeric(filter.getDescription());
			lastDirectory = se.getPreference(lastDirectoryKey);
			chooser.setFileFilter(filter);
		}

		if (lastDirectory != null)
			lastChoicePath = new File(lastDirectory);
		if (directoryPath != null)
			chooser.setCurrentDirectory(directoryPath);
		else
			chooser.setCurrentDirectory(lastChoicePath);

		// clear the selected file (since it shows up by default and isn't
		// usually the correct extension)
		chooser.setSelectedFile(new File(defaultFileName));

		buttonChoice = chooser.showDialog(this.mainFrame, operation);

		if (buttonChoice == JFileChooser.APPROVE_OPTION) {
			choice = chooser.getSelectedFile();
			lastChoicePath = chooser.getCurrentDirectory();

			se.setPreference(lastDirectoryKey, lastChoicePath.getAbsolutePath());
		} else
			choice = null;

		return choice;
	}

	/**
	 * Shows the Preferences dialog.
	 */
	public void showPreferencesDialog() {
		PreferencesDialog preferencesDialog = new PreferencesDialog(
				this.mainFrame);
		preferencesDialog.display();
	}

	/**
	 * Creates a new, empty dialog.
	 * 
	 * @param title
	 * @return
	 */
	public JDialog buildDialog(String title) {
		final JDialog dialog = new JDialog(this.mainFrame, title,
				Dialog.ModalityType.DOCUMENT_MODAL);

		dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		return dialog;
	}

	/**
	 * Creates a dialog that lets the user choose which library they would like
	 * to merge into the existing one.
	 * 
	 * @param translator
	 */
	public JDialog buildMergeLibraryChoiceDialog(final Translator translator) {
		final String TITLE = "Library to Merge";

		final JDialog dialog;

		final JPanel content;
		final JLabel message;
		final JComboBox libraryChoice;
		final JButton mergeButton;
		final JButton cancelButton;

		final GroupLayout layout;

		dialog = this.buildDialog(TITLE);

		content = new JPanel();
		message = new JLabel("Which Library would you like to merge?");
		libraryChoice = new JComboBox();
		mergeButton = new JButton("Merge");
		cancelButton = new JButton("Cancel");

		layout = new GroupLayout(content);

		content.setLayout(layout);

		final SEModel currentLibrary = SEModelManager.getInstance()
				.getActiveModel();

		if (translator.getLibrary() != currentLibrary)
			libraryChoice.addItem(translator.getLibrary());

		for (LibraryModel library : translator.getOptionalLibraries()) {
			if (library != currentLibrary)
				libraryChoice.addItem(library);
		}

		mergeButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				final Object selectedItem = libraryChoice.getSelectedItem();

				if (selectedItem instanceof LibraryModel) {
					final LibraryModel library = (LibraryModel) selectedItem;
					MergeLibraryAction.getInstance().mergeLibrary(library);
				}

				dialog.dispose();
			}
		});

		cancelButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				dialog.setVisible(false);
				dialog.dispose();
			}
		});

		final JSeparator separator = new JSeparator(SwingConstants.HORIZONTAL);

		layout.setHorizontalGroup(layout
				.createParallelGroup()
				.addComponent(message)
				.addComponent(libraryChoice)
				.addComponent(separator)
				.addGroup(
						GroupLayout.Alignment.TRAILING,
						layout.createSequentialGroup()
								.addComponent(mergeButton)
								.addComponent(cancelButton)));

		layout.setVerticalGroup(layout
				.createSequentialGroup()
				.addComponent(message)
				.addComponent(libraryChoice)
				.addComponent(separator)
				.addGroup(
						layout.createParallelGroup().addComponent(mergeButton)
								.addComponent(cancelButton)).addGap(0));

		layout.setAutoCreateGaps(true);
		layout.setAutoCreateContainerGaps(true);

		dialog.setContentPane(content);
		dialog.pack();
		dialog.setResizable(false);
		dialog.setLocationRelativeTo(dialog.getParent());

		return dialog;
	}

	/**
	 * Opens the Library Model that the user chooses from the libraries in the
	 * translator.
	 * 
	 * @param translator
	 * @return
	 */
	public JDialog buildLibraryEditorChoiceDialog(final Translator translator) {
		final String TITLE = "Choose a Library";

		final JDialog dialog;

		final JPanel content;
		final JLabel message;
		final JComboBox libraryChoice;
		final JButton sendButton;
		final JButton cancelButton;

		final GroupLayout layout;

		final String newLibrary;

		dialog = this.buildDialog(TITLE);

		content = new JPanel();
		message = new JLabel("Which Library would you like to edit?");
		libraryChoice = new JComboBox();
		sendButton = new JButton("Edit");
		cancelButton = new JButton("Cancel");

		layout = new GroupLayout(content);

		newLibrary = "New...";

		content.setLayout(layout);

		libraryChoice.addItem(translator.getLibrary());

		for (LibraryModel library : translator.getOptionalLibraries()) {
			libraryChoice.addItem(library);
		}

		libraryChoice.addItem(newLibrary);

		sendButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				final Object selectedItem = libraryChoice.getSelectedItem();

				if (selectedItem == newLibrary) {
					WindowFactory.getInstance().showNewLibraryWizardDialog(
							translator);
					dialog.dispose();
				} else if (selectedItem instanceof LibraryModel) {
					TranslatorManager.getInstance().setActiveTranslator(
							translator);
					SEModelManager.getInstance().addAndActivate(
							(LibraryModel) selectedItem);
					dialog.dispose();
				}
			}
		});

		cancelButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				dialog.setVisible(false);
				dialog.dispose();
			}
		});

		final JSeparator separator = new JSeparator(SwingConstants.HORIZONTAL);

		layout.setHorizontalGroup(layout
				.createParallelGroup()
				.addComponent(message)
				.addComponent(libraryChoice)
				.addComponent(separator)
				.addGroup(
						GroupLayout.Alignment.TRAILING,
						layout.createSequentialGroup().addComponent(sendButton)
								.addComponent(cancelButton)));

		layout.setVerticalGroup(layout
				.createSequentialGroup()
				.addComponent(message)
				.addComponent(libraryChoice)
				.addComponent(separator)
				.addGroup(
						layout.createParallelGroup().addComponent(sendButton)
								.addComponent(cancelButton))
				// this removes the gap between buttons & bottom of dialog
				.addGap(0));

		layout.setAutoCreateGaps(true);
		layout.setAutoCreateContainerGaps(true);

		dialog.setContentPane(content);
		dialog.pack();
		dialog.setResizable(false);
		dialog.setLocationRelativeTo(dialog.getParent());

		return dialog;
	}

	/**
	 * Opens the Library Model that the user chooses from the libraries in the
	 * translator.
	 * 
	 * @param translator
	 * @return
	 */
	public JDialog buildBehaviourEditor(final Translator translator) {
		// TODO Might not even need this if we use the library editor.

		return null;
	}

	/**
	 * Create a dialog with the provided panel.
	 * 
	 * @param title
	 * 
	 * @param panel
	 * 
	 * @param resizable
	 * 
	 * @return
	 */
	public JDialog buildDialog(String title, JPanel panel, boolean resizable) {
		final JDialog dialog = this.buildDialog(title);

		dialog.setContentPane(panel);
		dialog.pack();
		dialog.setResizable(resizable);
		dialog.setLocationRelativeTo(null);

		return dialog;
	}

	/**
	 * Shows a send feedback dialog.
	 * 
	 * @return
	 */
	public JDialog buildFeedbackDialog() {
		final String TITLE = "Send Feedback";
		final int MAX_ATTACHMENTS = 3;
		final JDialog feedbackDialog;

		final JPanel content;
		final JButton sendButton;
		final JButton cancelButton;
		final JTextArea commentArea;
		final JScrollPane areaScrollPane;
		final JLabel emailLabel;
		final JTextField emailField;
		// final JLabel fileLabel;
		final Map<JTextField, JButton> fileFields;
		final GroupLayout layout;

		feedbackDialog = this.buildDialog(TITLE);

		content = new JPanel();
		sendButton = new JButton("Send");
		cancelButton = new JButton("Cancel");
		commentArea = new JTextArea();
		areaScrollPane = new JScrollPane(commentArea);
		emailLabel = new JLabel("Email: ");
		emailField = new JTextField();
		// fileLabel = new JLabel("Story Files(Optional): ");
		fileFields = new HashMap<JTextField, JButton>();

		for (int i = 0; i < MAX_ATTACHMENTS; i++)
			fileFields.put(new JTextField(), new JButton("Browse"));

		for (final Entry<JTextField, JButton> fileField : fileFields.entrySet()) {
			JButton browseButton = fileField.getValue();

			browseButton.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent arg0) {
					final File filePath;

					filePath = WindowFactory.getInstance().showFileChooser(
							"Select", "",
							new FileNameExtensionFilter("ses", "ses"));

					if (filePath != null)
						fileField.getKey().setText(filePath.getAbsolutePath());
				}
			});
		}

		commentArea.setFont(new Font("SansSerif", Font.PLAIN, 12));
		commentArea.setLineWrap(true);
		commentArea.setWrapStyleWord(true);

		areaScrollPane
				.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		areaScrollPane.setPreferredSize(new Dimension(350, 350));
		areaScrollPane.setBorder(BorderFactory.createTitledBorder("Feedback"));

		layout = new GroupLayout(content);

		content.setLayout(layout);

		commentArea.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				if (e.getKeyCode() == KeyEvent.VK_TAB) {
					KeyboardFocusManager.getCurrentKeyboardFocusManager()
							.focusNextComponent();
					e.consume();
					// \(^o^)/ nom nom nom
				}
			}
		});

		sendButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				NetworkHandler.getInstance().sendFeedback(
						commentArea.getText(), emailField.getText());
				feedbackDialog.setVisible(false);
				feedbackDialog.dispose();
			}
		});

		cancelButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				feedbackDialog.setVisible(false);
				feedbackDialog.dispose();
			}
		});

		ParallelGroup parallelGroup;
		SequentialGroup sequentialGroup;

		parallelGroup = layout
				.createParallelGroup()
				.addComponent(areaScrollPane)
				.addGroup(
						GroupLayout.Alignment.CENTER,
						layout.createSequentialGroup().addComponent(emailLabel)
								.addComponent(emailField));
		sequentialGroup = layout
				.createSequentialGroup()
				.addComponent(areaScrollPane)
				.addGroup(
						layout.createParallelGroup().addComponent(emailLabel)
								.addComponent(emailField)).addGap(5);

		/**
		 * TODO: Uncomment once I get back to ticket 55016874
		 */
		// for (final Entry<JTextField, JButton> fileField :
		// fileFields.entrySet()) {
		// final JTextField field = fileField.getKey();
		// final JButton button = fileField.getValue();
		//
		// parallelGroup = parallelGroup.addGroup(
		// GroupLayout.Alignment.LEADING, layout
		// .createSequentialGroup().addComponent(fileLabel)
		// .addComponent(field).addComponent(button));
		//
		// sequentialGroup = sequentialGroup.addGroup(layout
		// .createParallelGroup().addComponent(fileLabel)
		// .addComponent(field).addComponent(button));
		// }

		parallelGroup = parallelGroup.addGroup(GroupLayout.Alignment.TRAILING,
				layout.createSequentialGroup().addComponent(sendButton)
						.addComponent(cancelButton));
		sequentialGroup = sequentialGroup.addGap(10).addGroup(
				layout.createParallelGroup().addComponent(sendButton)
						.addComponent(cancelButton));

		layout.setHorizontalGroup(parallelGroup);
		layout.setVerticalGroup(sequentialGroup);

		feedbackDialog.setContentPane(content);
		feedbackDialog.pack();
		feedbackDialog.setResizable(false);
		feedbackDialog.setLocationRelativeTo(feedbackDialog.getParent());

		return feedbackDialog;
	}

	/**
	 * Shows a bug report dialog.
	 * 
	 * @return
	 */
	public JDialog buildBugReportDialog() {
		final String TITLE = "Send Bug Report";
		final JDialog bugReportDialog;

		final JPanel content;
		final JButton sendButton;
		final JButton cancelButton;
		final JTextArea commentArea;
		final JScrollPane areaScrollPane;

		final GroupLayout layout;

		bugReportDialog = this.buildDialog(TITLE);

		content = new JPanel();
		sendButton = new JButton("Send");
		cancelButton = new JButton("Cancel");
		commentArea = new JTextArea();
		areaScrollPane = new JScrollPane(commentArea);

		commentArea.setFont(new Font("SansSerif", Font.PLAIN, 12));
		commentArea.setLineWrap(true);
		commentArea.setWrapStyleWord(true);

		areaScrollPane
				.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		areaScrollPane.setPreferredSize(new Dimension(250, 250));
		areaScrollPane.setBorder(BorderFactory
				.createTitledBorder("Bug Description"));

		layout = new GroupLayout(content);

		content.setLayout(layout);

		sendButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				NetworkHandler.getInstance().sendReport(commentArea.getText());
				bugReportDialog.setVisible(false);
				bugReportDialog.dispose();
			}
		});

		cancelButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				bugReportDialog.setVisible(false);
				bugReportDialog.dispose();
			}
		});

		layout.setHorizontalGroup(layout
				.createParallelGroup()
				.addComponent(areaScrollPane)
				.addGroup(
						GroupLayout.Alignment.TRAILING,
						layout.createSequentialGroup().addComponent(sendButton)
								.addComponent(cancelButton)));

		layout.setVerticalGroup(layout
				.createSequentialGroup()
				.addComponent(areaScrollPane)
				.addGroup(
						layout.createParallelGroup().addComponent(sendButton)
								.addComponent(cancelButton)));

		bugReportDialog.setContentPane(content);
		bugReportDialog.pack();
		bugReportDialog.setResizable(false);
		bugReportDialog.setLocationRelativeTo(bugReportDialog.getParent());

		return bugReportDialog;
	}

	/**
	 * Returns the current focused frame.
	 * 
	 * @return
	 */
	public JFrame getCurrentFrame() {
		return this.mainFrame;
	}

	/**
	 * Creates a new SEFrame.
	 * 
	 * @return
	 */
	@SuppressWarnings("serial")
	private JFrame buildScriptEaseFrame(String title) {
		final int MIN_HEIGHT = 480;
		final int MIN_WIDTH = 640;

		final JFrame frame;

		final JPanel content;
		final JPanel middlePane;

		final JSplitPane middleSplit;
		final JSplitPane librarySplit;

		final JComponent statusBar;
		final GroupLayout contentLayout;

		final Runnable yetAnotherSwingHack;

		final SEModelObserver modelObserver;

		frame = new JFrame(title) {
			@Override
			protected void processWindowEvent(WindowEvent e) {
				if (e.getID() == WindowEvent.WINDOW_CLOSING)
					ScriptEase.getInstance().exit();
				else
					super.processWindowEvent(e);
			}
		};

		content = new JPanel();
		middlePane = new JPanel();

		middleSplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
		middleSplit.setDividerLocation(320);
		librarySplit = PanelFactory.getInstance().buildLibrarySplitPane();

		statusBar = PanelFactory.getInstance().buildStatusPanel();

		contentLayout = new GroupLayout(content);

		// We need to invoke a runnable for this later because Swing.
		yetAnotherSwingHack = new Runnable() {
			@Override
			public void run() {
				// I hope this fixes it, because JSplitPainInTheAss refuses to
				// resize when we change the size of the bottom component.
				// Sometimes. Sometimes it works, though. It may depend on the
				// moon cycle.
				final double thisShouldntBeNecessary = 0.5d;
				librarySplit.setResizeWeight(thisShouldntBeNecessary);
				librarySplit.setDividerLocation(thisShouldntBeNecessary);
			}
		};

		modelObserver = new SEModelObserver() {
			@Override
			public void modelChanged(SEModelEvent event) {
				final SEModelEvent.Type eventType;
				final SEModel activeModel;
				final boolean activated;

				eventType = event.getEventType();
				activeModel = SEModelManager.getInstance().getActiveModel();
				activated = eventType == SEModelEvent.Type.ACTIVATED;

				if (activated
						|| (eventType == SEModelEvent.Type.REMOVED && activeModel == null)
						|| (eventType == SEModelEvent.Type.TITLECHANGED)) {

					final JMenuBar bar;

					bar = MenuFactory.createMainMenuBar(activeModel);
					frame.setJMenuBar(bar);

					// Create the title for the frame
					String newTitle = "";
					if (activeModel != null) {
						String modelTitle = activeModel.getTitle();
						String moduleTitle = "";

						if (activeModel instanceof StoryModel) {
							final StoryModel story = (StoryModel) activeModel;
							moduleTitle = story.getModule().getLocation()
									.getName();
							if (!modelTitle.isEmpty() && !moduleTitle.isEmpty())
								newTitle += modelTitle + " [" + moduleTitle
										+ " ] - ";
						} else {
							if (!modelTitle.isEmpty())
								newTitle += modelTitle + " - ";
						}
					}
					newTitle += ScriptEase.TITLE + " "
							+ ScriptEase.getInstance().getVersion();

					frame.setTitle(newTitle);

					// We need to re-validate the menu bar.
					// http://bugs.sun.com/view_bug.do?bug_id=4949810
					bar.revalidate();
				}
			}
		};

		ResourcePanel.getInstance().addObserver(middleSplit,
				new ResourceTreeAdapter() {
					@Override
					public void resourceTreeFilled() {
						SwingUtilities.invokeLater(yetAnotherSwingHack);
					}
				});

		WidgetDecorator.setSimpleDivider(middleSplit);

		frame.setMinimumSize(new Dimension(MIN_WIDTH, MIN_HEIGHT));
		frame.setExtendedState(Frame.MAXIMIZED_BOTH);

		middlePane.setLayout(new GridLayout(1, 1));
		middlePane.add(PanelFactory.getInstance().buildModelTabPanel());

		content.setLayout(contentLayout);

		middleSplit.setTopComponent(librarySplit);
		middleSplit.setBottomComponent(middlePane);
		middleSplit.setBorder(BorderFactory.createEmptyBorder());

		content.add(middleSplit);
		content.add(statusBar);

		contentLayout.setHorizontalGroup(contentLayout.createParallelGroup()
				.addComponent(middleSplit).addComponent(statusBar));

		contentLayout
				.setVerticalGroup(contentLayout
						.createSequentialGroup()
						.addComponent(middleSplit)
						.addComponent(statusBar, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE));

		SEModelManager.getInstance().addSEModelObserver(this, modelObserver);

		frame.getContentPane().add(content);

		middlePane.setBackground(ScriptEaseUI.SECONDARY_UI);

		SwingUtilities.invokeLater(yetAnotherSwingHack);

		return frame;
	}
}
