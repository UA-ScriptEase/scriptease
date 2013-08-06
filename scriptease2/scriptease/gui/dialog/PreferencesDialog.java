package scriptease.gui.dialog;

import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.SpinnerModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import scriptease.ScriptEase;
import scriptease.gui.WindowFactory;

/*
 * TODO This belongs in a museum... Or in WindowManager.
 * 
 * We should have a preferences manager.
 */

/**
 * Builds the SE2 preferences dialog and displays it.
 * 
 * @author graves
 * @author jyuen
 */
public class PreferencesDialog {

	private JDialog dialog;

	// Variables to hold temporary preferences.
	private Integer maxUndoSteps;
	private Boolean useJavaUI;
	private Integer fontSize;
	private String outputDirectory;
	private Boolean debugMode;

	private void loadCurrentPreferences() {
		ScriptEase instance = ScriptEase.getInstance();
		try {
			this.maxUndoSteps = Integer.parseInt(instance
					.getPreference(ScriptEase.UNDO_STACK_SIZE_KEY));
		} catch (NumberFormatException e) {
			this.maxUndoSteps = 50; // If the input is invalid, use the default.
		}
		try {
			this.fontSize = Integer.parseInt(instance
					.getPreference(ScriptEase.FONT_SIZE_KEY));
		} catch (NumberFormatException e) {
			this.fontSize = 12;
		}

		// Invalid input results in false being returned.
		this.useJavaUI = Boolean.parseBoolean(instance
				.getPreference(ScriptEase.LOOK_AND_FEEL_KEY));
		this.debugMode = Boolean.parseBoolean(instance
				.getPreference(ScriptEase.DEBUG_KEY));

		this.outputDirectory = instance
				.getPreference(ScriptEase.OUTPUT_DIRECTORY_KEY);
		if (this.outputDirectory == null)
			this.outputDirectory = "output";
	}

	public PreferencesDialog(Frame owner) {
		final String PROGRAM_RESTART_REQUIRED_TEXT = "Changes to Preferences may not take effect until ScriptEase is restarted.";
		final String PROGRAM_RESTART_REQUIRED_TITLE = "Program restart required";

		final String PREFERENCES_TEXT = "Preferences";
		final String GENERAL_TEXT = "General";
		final String APPEARANCE_TEXT = "Appearance";
		final String OKAY_TEXT = "Okay";
		final String CANCEL_TEXT = "Cancel";

		final Box buttonBox;
		final JButton okButton;
		final JButton cancelButton;
		final JTabbedPane preferencesPane;

		// Load the current preferences settings before starting.
		this.loadCurrentPreferences();

		// Create the main dialog box.
		this.dialog = new JDialog(owner, PREFERENCES_TEXT, true);
		this.dialog.getContentPane().setLayout(
				new BoxLayout(this.dialog.getContentPane(), BoxLayout.Y_AXIS));

		// Build the row of buttons.
		buttonBox = new Box(BoxLayout.X_AXIS);

		okButton = new JButton(OKAY_TEXT);
		okButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				// Set the preferences.
				ScriptEase instance = ScriptEase.getInstance();
				instance.setPreference(ScriptEase.UNDO_STACK_SIZE_KEY,
						PreferencesDialog.this.maxUndoSteps.toString());
				instance.setPreference(ScriptEase.LOOK_AND_FEEL_KEY,
						PreferencesDialog.this.useJavaUI.toString());
				instance.setPreference(ScriptEase.FONT_SIZE_KEY,
						PreferencesDialog.this.fontSize.toString());
				instance.setPreference(ScriptEase.OUTPUT_DIRECTORY_KEY,
						PreferencesDialog.this.outputDirectory);
				instance.setPreference(ScriptEase.DEBUG_KEY,
						PreferencesDialog.this.debugMode.toString());

				// Write the preferences to file.
				instance.saveUserPrefs();

				// Notify user that effects only work after restart.
				WindowFactory.getInstance().showInformationDialog(
						PROGRAM_RESTART_REQUIRED_TITLE,
						PROGRAM_RESTART_REQUIRED_TEXT);

				// Close the dialog.
				PreferencesDialog.this.dialog.setVisible(false);
				PreferencesDialog.this.dialog.dispose();
			}
		});
		buttonBox.add(okButton);

		cancelButton = new JButton(CANCEL_TEXT);
		cancelButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				// Close the dialog.
				PreferencesDialog.this.dialog.setVisible(false);
				PreferencesDialog.this.dialog.dispose();
			}
		});
		buttonBox.add(cancelButton);

		// Create the tabbed pane for the preferences panes.
		preferencesPane = new JTabbedPane(SwingConstants.TOP);
		preferencesPane.addTab(GENERAL_TEXT, createGeneralPanel());
		preferencesPane.addTab(APPEARANCE_TEXT, createAppearancePanel());

		this.dialog.add(preferencesPane);
		this.dialog.add(buttonBox);
	}

	/**
	 * Create the panel for the general preferences.
	 * 
	 * @return
	 */
	private JPanel createGeneralPanel() {
		final String UNDO_STACK_SIZE_TEXT = "Undo stack size:";
		final String OUTPUT_DIR_TEXT = "Output directory:";
		final String DEBUG_TEXT = "Run in debug mode:";

		final JPanel generalPanel;

		final JPanel undoStackSizePanel;
		final SpinnerModel undoStackSizeSpinnerModel;

		final JPanel outputDirectoryPanel;
		final JTextField outputDirectoryTextField;

		final JPanel debugModePanel;
		final JCheckBox debugModeCheckBox;

		generalPanel = new JPanel();
		generalPanel.setLayout(new BoxLayout(generalPanel, BoxLayout.Y_AXIS));

		// Create the Undo stack size spinner.
		undoStackSizePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		undoStackSizeSpinnerModel = new SpinnerNumberModel(this.maxUndoSteps,
				new Integer(0), new Integer(10000), new Integer(1));
		undoStackSizeSpinnerModel.addChangeListener(new ChangeListener() {

			@Override
			public void stateChanged(ChangeEvent e) {
				PreferencesDialog.this.maxUndoSteps = (Integer) undoStackSizeSpinnerModel
						.getValue();
			}
		});

		undoStackSizePanel.add(new JLabel(UNDO_STACK_SIZE_TEXT));
		undoStackSizePanel.add(new JSpinner(undoStackSizeSpinnerModel));

		// Create the output directory Textfield.
		outputDirectoryPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		outputDirectoryTextField = new JTextField(this.outputDirectory);
		outputDirectoryTextField.addCaretListener(new CaretListener() {

			@Override
			public void caretUpdate(CaretEvent e) {
				PreferencesDialog.this.outputDirectory = outputDirectoryTextField
						.getText();
			}
		});
		outputDirectoryPanel.add(new JLabel(OUTPUT_DIR_TEXT));
		outputDirectoryPanel.add(outputDirectoryTextField);

		// Create the debug mode checkbox.
		debugModePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		debugModeCheckBox = new JCheckBox();
		debugModeCheckBox.setSelected(this.debugMode);
		debugModeCheckBox.addChangeListener(new ChangeListener() {

			@Override
			public void stateChanged(ChangeEvent e) {
				PreferencesDialog.this.debugMode = debugModeCheckBox
						.isSelected();
			}
		});
		debugModePanel.add(new JLabel(DEBUG_TEXT));
		debugModePanel.add(debugModeCheckBox);

		generalPanel.add(undoStackSizePanel);
		generalPanel.add(outputDirectoryPanel);
		generalPanel.add(debugModePanel);

		return generalPanel;
	}

	/**
	 * Create the panel for the appearance preferences.
	 * 
	 * @return
	 */
	private JPanel createAppearancePanel() {
		final String USE_JAVA_UI_TEXT = "Use Java UI:";
		final String FONT_SIZE_TEXT = "Font Size:";

		final JPanel appearancePanel;

		final JPanel useJavaUIPanel;
		final JCheckBox useJavaUICheckBox;

		final JPanel fontSizePanel;
		final SpinnerModel fontSizeSpinnerModel;

		appearancePanel = new JPanel();
		appearancePanel.setLayout(new BoxLayout(appearancePanel,
				BoxLayout.Y_AXIS));

		// Create the Use Java UI check box.
		useJavaUIPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		useJavaUIPanel.add(new JLabel(USE_JAVA_UI_TEXT));
		useJavaUICheckBox = new JCheckBox();
		useJavaUICheckBox.setSelected(this.useJavaUI);
		useJavaUICheckBox.addChangeListener(new ChangeListener() {

			@Override
			public void stateChanged(ChangeEvent e) {
				PreferencesDialog.this.useJavaUI = useJavaUICheckBox
						.isSelected();
			}
		});
		useJavaUIPanel.add(useJavaUICheckBox);

		// Create the fontSize spinner.
		fontSizePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		fontSizePanel.add(new JLabel(FONT_SIZE_TEXT));
		fontSizeSpinnerModel = new SpinnerNumberModel(this.fontSize,
				new Integer(2), new Integer(20), new Integer(1));
		fontSizeSpinnerModel.addChangeListener(new ChangeListener() {

			@Override
			public void stateChanged(ChangeEvent e) {
				PreferencesDialog.this.fontSize = (Integer) fontSizeSpinnerModel
						.getValue();
			}
		});
		fontSizePanel.add(new JSpinner(fontSizeSpinnerModel));

		appearancePanel.add(useJavaUIPanel);
		appearancePanel.add(fontSizePanel);

		return appearancePanel;
	}

	public void display() {
		// Show the dialog.
		this.dialog.setResizable(false);
		this.dialog.pack();
		this.dialog.setLocationRelativeTo(null);
		this.dialog.setVisible(true);
	}
}