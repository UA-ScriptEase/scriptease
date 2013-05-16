package scriptease.gui;

import java.awt.Color;
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
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;

import javax.swing.BorderFactory;
import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenuBar;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.SwingWorker.StateValue;
import javax.swing.filechooser.FileFilter;

import scriptease.ScriptEase;
import scriptease.controller.StoryAdapter;
import scriptease.controller.logger.NetworkHandler;
import scriptease.controller.modelverifier.problem.StoryProblem;
import scriptease.controller.observer.SEModelEvent;
import scriptease.controller.observer.SEModelObserver;
import scriptease.gui.dialog.DialogBuilder;
import scriptease.gui.dialog.PreferencesDialog;
import scriptease.gui.pane.PanelFactory;
import scriptease.gui.storycomponentpanel.StoryComponentPanel;
import scriptease.gui.storycomponentpanel.StoryComponentPanelFactory;
import scriptease.model.SEModel;
import scriptease.model.SEModelManager;
import scriptease.model.StoryComponent;
import scriptease.model.atomic.KnowIt;
import scriptease.model.atomic.knowitbindings.KnowItBindingFunction;
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
 */
public final class WindowFactory {
	private static final String CODE_GENERATION_PROBLEM = "Code Generation Problem";
	private static final String ABOUT_SCRIPTEASE_TITLE = "About ScriptEase II";

	// TODO we used to use "ScriptEase.getInstance().getVersion()" to get the
	// version, but it was generating a version of "2.-1", which is wrong. Fix
	// that and change this back.
	private static final String ABOUT_SCRIPTEASE_MESSAGE = "<html><b><font size=\"4\">ScriptEase II</font></b><br>"
			+ "<font size=\"2\">Version: Beta<br>"
			+ "Revision: "
			+ ScriptEase.getInstance().getSpecificVersion()
			+ "</font><br><br>"
			+ "Game Scripting and Code Generation for any game!<br><br>"
			+ "<b>Contributors:</b><br>"
			+ "<br><b>Professors:</b> Mike Carbonaro, Jonathan Schaeffer, Duane Szafron"
			+ "<br><b>Ph.D. Students:</b> Neesha Desai, Richard Zhao"
			+ "<br><b>Programmer Analysts:</b> Matthew Church, Jason Duncan, Eric Graves, Adel Lari, Robin Miller, Kevin Schenk, Jessica Yuen"
			+ "<br><b>Artists:</b> Wei Li, Jamie Schmitt"
			+ "<br><b>Interns:</b> Alex Czeto, Kirsten Svidal"
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
	public static boolean progressShowing = false;

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
		final JFrame frame;

		frame = this.buildScriptEaseFrame(ScriptEase.TITLE);

		frame.setJMenuBar(MenuFactory.createMainMenuBar(null));

		if (!frame.isVisible())
			frame.setVisible(true);

		this.mainFrame = frame;
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
	 * Shows the Exception Dialog if it is not already showing. The Error Dialog
	 * is modal and will sit on top of any other window when shown.
	 */
	public void showExceptionDialog() {
		// safety check to avoid infinite dialog spawns
		if (WindowFactory.exceptionShowing)
			return;

		JDialog dialog = DialogBuilder.getInstance().createExceptionDialog(
				this.mainFrame);
		WindowFactory.exceptionShowing = true;

		dialog.setLocationRelativeTo(dialog.getParent());
		dialog.setVisible(true);

		WindowFactory.exceptionShowing = false;
	}

	public void showNewStoryWizardDialog() {
		DialogBuilder.getInstance().showNewStoryWizard();
	}

	public void showNewLibraryWizardDialog() {
		DialogBuilder.getInstance().showNewLibraryWizard();
	}

	/**
	 * Shows a progressBar in a JDialog for the given task. Only one progressBar
	 * can be shown at a time. NOTE: The provided task should start executing
	 * before the progressBar is shown (Swing will block otherwise).
	 * 
	 * @param task
	 *            The task to run while displaying the progress bar.
	 * @param taskName
	 *            The name of the task to be displayed in the progress bar.
	 * 
	 * @param progressBarText
	 *            The text to be shown on the progress bar. This is commonly
	 *            "Loading...", but can be anything you set it to.
	 */
	public void showProgressBar(final SwingWorker<Void, Void> task,
			String progressBarText) {
		if (WindowFactory.progressShowing)
			return;

		final JDialog progressBar = DialogBuilder.getInstance()
				.createProgressBar(this.mainFrame, progressBarText);

		WindowFactory.progressShowing = true;

		task.addPropertyChangeListener(new PropertyChangeListener() {
			@Override
			public void propertyChange(PropertyChangeEvent change) {
				if ("state" == change.getPropertyName()) {
					if (change.getNewValue() == StateValue.DONE) {
						task.removePropertyChangeListener(this);
						progressBar.setVisible(false);
						WindowFactory.progressShowing = false;
					}
				}
			}
		});

		progressBar.setVisible(true);
	}

	public void showCompileProblems(Collection<StoryProblem> storyProblems) {
		JPanel panel = new JPanel();

		// Show only visible problems
		Collection<StoryProblem> visibleProblems = new ArrayList<StoryProblem>();
		for (StoryProblem problem : storyProblems) {
			if (problem.shouldNotify())
				visibleProblems.add(problem);
		}

		panel.setLayout(new GridLayout(visibleProblems.size() + 2, 1));
		panel.add(new JLabel(
				"Problems have been detected. In order to generate code the following problems must be resolved:\n"));

		for (StoryProblem problem : visibleProblems) {
			final StoryComponent component = problem.getComponent();
			final String description = problem.getDescription();

			JPanel problemPanel = new JPanel();
			// if avaliable, show the panel being changed, otherwise use the
			// display text
			StoryComponentPanel componentPanel = StoryComponentPanelFactory
					.getInstance().buildStoryComponentPanel(component);
			if (componentPanel != null) {
				componentPanel.setEnabled(false);
				problemPanel.add(componentPanel);
				problemPanel.add(new JLabel(" : " + description));
			} else
				problemPanel.add(new JLabel(component.getDisplayText() + " : "
						+ description));
			panel.add(problemPanel);
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
	 * chooser is customised to use the given operation name as the button's
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
	 * Shows a customised file chooser that is a child of the main ScriptEase
	 * frame. The chooser is customised to use the given operation name as the
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
	 * Shows a send feedback dialog.
	 * 
	 * @return
	 */
	public JDialog buildFeedbackDialog() {
		final String TITLE = "Send Feedback";
		final JDialog feedbackDialog;

		final JPanel content;
		final JButton sendButton;
		final JButton cancelButton;
		final JTextArea commentArea;
		final JScrollPane areaScrollPane;
		final JLabel emailLabel;
		final JTextField emailField;

		final GroupLayout layout;

		feedbackDialog = this.buildDialog(TITLE);

		content = new JPanel();
		sendButton = new JButton("Send");
		cancelButton = new JButton("Cancel");
		commentArea = new JTextArea();
		areaScrollPane = new JScrollPane(commentArea);
		emailLabel = new JLabel("Email");
		emailField = new JTextField();

		commentArea.setFont(new Font("SansSerif", Font.PLAIN, 12));
		commentArea.setLineWrap(true);
		commentArea.setWrapStyleWord(true);

		areaScrollPane
				.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		areaScrollPane.setPreferredSize(new Dimension(250, 250));
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

		emailField.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				NetworkHandler.getInstance().sendFeedback(
						commentArea.getText(), emailField.getText());
				feedbackDialog.setVisible(false);
				feedbackDialog.dispose();
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

		layout.setHorizontalGroup(layout
				.createParallelGroup()
				.addComponent(areaScrollPane)
				.addGroup(
						GroupLayout.Alignment.CENTER,
						layout.createSequentialGroup().addComponent(emailLabel)
								.addComponent(emailField))
				.addGroup(
						GroupLayout.Alignment.TRAILING,
						layout.createSequentialGroup().addComponent(sendButton)
								.addComponent(cancelButton)));

		layout.setVerticalGroup(layout
				.createSequentialGroup()
				.addComponent(areaScrollPane)
				.addGroup(
						layout.createParallelGroup().addComponent(emailLabel)
								.addComponent(emailField))
				.addGroup(
						layout.createParallelGroup().addComponent(sendButton)
								.addComponent(cancelButton)));

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

		final JComponent statusBar;
		final GroupLayout contentLayout;

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

		statusBar = PanelFactory.getInstance().buildStatusPanel();

		contentLayout = new GroupLayout(content);

		modelObserver = new SEModelObserver() {
			@Override
			public void modelChanged(SEModelEvent event) {
				final SEModelEvent.Type eventType;
				final SEModel activeModel;

				eventType = event.getEventType();
				activeModel = SEModelManager.getInstance().getActiveModel();

				if (eventType == SEModelEvent.Type.ACTIVATED
						|| (eventType == SEModelEvent.Type.REMOVED && activeModel == null)) {
					final JMenuBar bar;

					bar = MenuFactory.createMainMenuBar(activeModel);
					frame.setJMenuBar(bar);

					// Create the title for the frame
					String newTitle = "";
					if (activeModel != null) {
						String modelTitle = activeModel.getTitle();
						if (!modelTitle.isEmpty())
							newTitle += modelTitle + " - ";
					}
					newTitle += ScriptEase.TITLE;

					frame.setTitle(newTitle);

					// We need to revalidate the menu bar.
					// http://bugs.sun.com/view_bug.do?bug_id=4949810
					bar.revalidate();
				}
			}
		};

		frame.setMinimumSize(new Dimension(MIN_WIDTH, MIN_HEIGHT));
		frame.setExtendedState(Frame.MAXIMIZED_BOTH);

		middlePane.setLayout(new GridLayout(1, 1));
		middlePane.add(PanelFactory.getInstance().buildModelTabPanel());

		content.setLayout(contentLayout);

		// Compressed Layout
		middleSplit.setTopComponent(PanelFactory.getInstance()
				.buildLibrarySplitPane());
		middleSplit.setBottomComponent(middlePane);

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

		return frame;
	}
}
