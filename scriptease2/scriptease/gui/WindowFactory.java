package scriptease.gui;

import java.awt.Color;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.event.WindowEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;

import javax.swing.BorderFactory;
import javax.swing.GroupLayout;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.SwingWorker.StateValue;
import javax.swing.filechooser.FileFilter;

import scriptease.ScriptEase;
import scriptease.controller.StoryAdapter;
import scriptease.controller.modelverifier.problem.StoryProblem;
import scriptease.controller.observer.LifetimeObserverFactory;
import scriptease.controller.observer.PatternModelObserver;
import scriptease.gui.dialog.DialogBuilder;
import scriptease.gui.storycomponentpanel.StoryComponentPanel;
import scriptease.gui.storycomponentpanel.StoryComponentPanelFactory;
import scriptease.model.LibraryManager;
import scriptease.model.LibraryModel;
import scriptease.model.PatternModel;
import scriptease.model.PatternModelManager;
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
	private static final String ABOUT_SCRIPTEASE_TITLE = "About ScriptEase 2";
	private static final String ABOUT_SCRIPTEASE_MESSAGE = "ScriptEase 2\n"
			+ "Version: " + ScriptEase.getInstance().getVersion() + "\n"
			+ "Revision: " + ScriptEase.getInstance().getSpecificVersion()
			+ "\n\n" + "Game Scripting and Code Generation for any game!\n"
			+ "Visit us online at http://www.cs.ualberta.ca/~script/\n";
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

	/*
	 * This is never actually set, so it's just null. All of these windows are
	 * thus just created on the root pane. However, if we ever have multiple
	 * windows, this will be useful.
	 * 
	 * TODO Either implmement this so it can be "useful" or get rid of it. If we
	 * added everything that could possibly be useful in the future, ScriptEase
	 * would be twice the size with half the functionality.
	 */
	private JFrame currentFrame = null;

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

		this.currentFrame = frame;
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
				JOptionPane.showMessageDialog(WindowFactory.this.currentFrame,
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
		return JOptionPane.showOptionDialog(this.currentFrame, message, title,
				JOptionPane.DEFAULT_OPTION, JOptionPane.QUESTION_MESSAGE, null,
				options, null);
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
				this.currentFrame);
		WindowFactory.exceptionShowing = true;

		dialog.setLocationRelativeTo(dialog.getParent());
		dialog.setVisible(true);

		WindowFactory.exceptionShowing = false;
	}

	public void showNewStoryWizardDialog() {
		DialogBuilder.getInstance().showNewStoryWizard(this.currentFrame);
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
				.createProgressBar(this.currentFrame, progressBarText);

		WindowFactory.progressShowing = true;
		task.addPropertyChangeListener(new PropertyChangeListener() {
			@Override
			public void propertyChange(PropertyChangeEvent change) {
				if ("state" == change.getPropertyName()) {
					if (change.getNewValue() == StateValue.DONE) {
						task.removePropertyChangeListener(this);
						progressBar.setVisible(false);
						progressBar.dispose();
						WindowFactory.progressShowing = false;
					}
				}
			}
		});

		progressBar.setVisible(true);
	}

	/**
	 * This is never used. Translator assigned to new Library Model in
	 * DialogBuilder.showNewLibraryWizard may be wrong. Check before
	 * implementing this.
	 * 
	 * @deprecated
	 * 
	 * @return
	 */
	public LibraryModel buildNewLibraryWizardDialog() {
		return DialogBuilder.getInstance().showNewLibraryWizard(
				this.currentFrame);
	}

	/**
	 * Shows a Library Picker and returns the choice may return null if no
	 * selection is made
	 * 
	 * @return
	 */
	public Object showLibraryPicker() {
		Collection<LibraryModel> libraries = LibraryManager.getInstance()
				.getUserLibraries();
		Object[] options = new Object[libraries.size() + 1];
		Object[] libraryArray = libraries.toArray();
		for (int i = 0; i < libraries.size(); i++) {
			options[i] = libraryArray[i];
		}
		options[libraries.size()] = "New Library...";
		// libraries.add("test");
		return JOptionPane.showInputDialog(this.currentFrame,
				"Select a Library: ", "Library Selector",
				JOptionPane.PLAIN_MESSAGE, null, options, null);
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

		JOptionPane.showMessageDialog(this.currentFrame, panel,
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

		int choice = JOptionPane.showConfirmDialog(this.currentFrame, panel,
				WindowFactory.CONFIRM_MODEL_TITLE, JOptionPane.YES_NO_OPTION);
		return choice == JOptionPane.OK_OPTION;
	}

	/**
	 * Shows a confirm dialog for overwriting a file.
	 * 
	 * @return True if the user confirmed the overwrite, false otherwise.
	 */
	public boolean showConfirmOverwrite(File location) {
		int choice = JOptionPane.showConfirmDialog(this.currentFrame,
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
	public int showConfirmClose(PatternModel model) {
		int choice = JOptionPane.showConfirmDialog(this.currentFrame,
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
		JOptionPane.showMessageDialog(this.currentFrame,
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

		int choice = JOptionPane.showOptionDialog(this.currentFrame, message,
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
				JOptionPane.showMessageDialog(WindowFactory.this.currentFrame,
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
				JOptionPane.showMessageDialog(WindowFactory.this.currentFrame,
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
				JOptionPane.showMessageDialog(WindowFactory.this.currentFrame,
						message, "ScriptEase: " + title,
						JOptionPane.INFORMATION_MESSAGE);
			}
		});
	}

	public File showFileChooser(String operation, FileFilter filter) {
		return this.showFileChooser(operation, filter, null);
	}

	/**
	 * Shows a customised file chooser that is child of the main ScriptEase
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
	public File showFileChooser(String operation, FileFilter filter,
			File directoryPath) {
		final JFileChooser chooser;

		chooser = new JFileChooser();

		// disable "All Files" option
		chooser.setAcceptAllFileFilterUsed(false);

		final ScriptEase se = ScriptEase.getInstance();
		final int buttonChoice;
		final File choice;
		File lastChoicePath = null;
		final boolean isNullFilter = (filter == null);
		String lastDirectory = null;
		String lastDirectoryKey = null;

		chooser.resetChoosableFileFilters();
		chooser.setAcceptAllFileFilterUsed(true);
		if (!isNullFilter) {
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
		chooser.setSelectedFile(new File(""));

		buttonChoice = chooser.showDialog(this.currentFrame, operation);

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
				this.currentFrame);
		preferencesDialog.display();
	}

	public JDialog buildDialog(String title) {
		final JDialog dialog = new JDialog(this.currentFrame, title,
				Dialog.ModalityType.DOCUMENT_MODAL);

		dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		return dialog;
	}

	/**
	 * Returns the current focused frame.
	 * 
	 * @return
	 */
	public JFrame getCurrentFrame() {
		return this.currentFrame;
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
		final String preferredLayout;

		final PatternModelObserver modelObserver;

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
		// Get the preferred layout.
		preferredLayout = ScriptEase.getInstance().getPreference(
				ScriptEase.PREFERRED_LAYOUT_KEY);

		modelObserver = LifetimeObserverFactory.getInstance()
				.buildFrameModelObserver(frame);

		frame.setMinimumSize(new Dimension(MIN_WIDTH, MIN_HEIGHT));
		frame.setExtendedState(Frame.MAXIMIZED_BOTH);

		middlePane.setLayout(new GridLayout(1, 1));
		middlePane.add(PanelFactory.getInstance().getModelTabPane());

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
		if (preferredLayout.equalsIgnoreCase(ScriptEase.UNCOMPRESSED_LAYOUT)) {
			// Uncompressed Layout.
			// TODO Do something special if layout is uncompressed. Removed this
			// when building of the Library Pane was moved to panelfactory.
		}

		PatternModelManager.getInstance()
				.addPatternModelObserver(modelObserver);

		frame.getContentPane().add(content);

		return frame;
	}
}
