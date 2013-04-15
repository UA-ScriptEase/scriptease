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
import scriptease.gui.internationalization.Il8nResources;

/*
 * TODO This belongs in a museum... Or in WindowManager.
 * 
 * We should have a preferences manager.
 */

/**
 * Builds the SE2 preferences dialog and displays it.
 * 
 * @author graves
 */
public class PreferencesDialog {
	private static final String PROGRAM_RESTART_REQUIRED_TEXT = "Changes to Preferences may not take effect until ScriptEase is restarted.";
	private static final String PROGRAM_RESTART_REQUIRED_TITLE = "Program restart required";

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
		final Box buttonBox;
		final JButton okButton;
		final JButton cancelButton;
		final JTabbedPane preferencesPane;
		final JPanel generalPanel;
		final JPanel appearancePanel;

		// Load the current preferences settings before starting.
		this.loadCurrentPreferences();

		// Create the modal dialog for the preferences window.
		this.dialog = new JDialog(owner,
				Il8nResources.getString("Preferences"), true);

		this.dialog.getContentPane().setLayout(
				new BoxLayout(this.dialog.getContentPane(), BoxLayout.Y_AXIS));

		// Build the row of buttons.
		buttonBox = new Box(BoxLayout.X_AXIS);

		okButton = new JButton(Il8nResources.getString("Okay"));
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

				// Notify the user that changes take effect after restarting the
				// program.
				WindowFactory.getInstance().showInformationDialog(
						PreferencesDialog.PROGRAM_RESTART_REQUIRED_TITLE,
						PreferencesDialog.PROGRAM_RESTART_REQUIRED_TEXT);

				// Close the dialog.
				PreferencesDialog.this.dialog.setVisible(false);
				PreferencesDialog.this.dialog.dispose();
			}
		});
		buttonBox.add(okButton);

		cancelButton = new JButton(Il8nResources.getString("Cancel"));
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

		// Create the panel for the general preferences.
		generalPanel = new JPanel();
		generalPanel.setLayout(new BoxLayout(generalPanel, BoxLayout.Y_AXIS));

		// Create the Undo stack size spinner.
		final JPanel undoStackSizePanel = new JPanel(new FlowLayout(
				FlowLayout.LEFT));
		final SpinnerModel undoStackSizeSpinnerModel = new SpinnerNumberModel(
				this.maxUndoSteps, new Integer(0), new Integer(10000),
				new Integer(1));
		undoStackSizeSpinnerModel.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				PreferencesDialog.this.maxUndoSteps = (Integer) undoStackSizeSpinnerModel
						.getValue();
			}
		});
		JSpinner undoStackSizeSpinner = new JSpinner(undoStackSizeSpinnerModel);
		undoStackSizePanel.add(new JLabel(Il8nResources
				.getString("Undo_Stack_Size") + ":"));
		undoStackSizePanel.add(undoStackSizeSpinner);
		generalPanel.add(undoStackSizePanel);

		// Create the output directory Textfield.
		final JPanel outputDirectoryPanel = new JPanel(new FlowLayout(
				FlowLayout.LEFT));
		final JTextField outputDirectoryTextField = new JTextField(
				this.outputDirectory);
		outputDirectoryTextField.addCaretListener(new CaretListener() {
			@Override
			public void caretUpdate(CaretEvent e) {
				PreferencesDialog.this.outputDirectory = outputDirectoryTextField
						.getText();
			}
		});
		outputDirectoryPanel.add(new JLabel(Il8nResources
				.getString("Output_directory") + ":"));
		outputDirectoryPanel.add(outputDirectoryTextField);
		generalPanel.add(outputDirectoryPanel);

		// Create the debug mode checkbox.
		final JPanel debugModePanel = new JPanel(
				new FlowLayout(FlowLayout.LEFT));
		final JCheckBox debugModeCheckBox = new JCheckBox();
		debugModeCheckBox.setSelected(this.debugMode);
		debugModeCheckBox.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				PreferencesDialog.this.debugMode = debugModeCheckBox
						.isSelected();
			}
		});
		debugModePanel.add(new JLabel(Il8nResources
				.getString("Run_in_debug_mode") + ":"));
		debugModePanel.add(debugModeCheckBox);
		generalPanel.add(debugModePanel);

		// Create the panel for the appearance preferences.
		appearancePanel = new JPanel();
		appearancePanel.setLayout(new BoxLayout(appearancePanel,
				BoxLayout.Y_AXIS));

		// Create the useJavaUI checkbox.
		final JPanel useJavaUIPanel = new JPanel(
				new FlowLayout(FlowLayout.LEFT));
		useJavaUIPanel.add(new JLabel(Il8nResources.getString("Use_Java_UI")
				+ ":"));
		final JCheckBox useJavaUICheckBox = new JCheckBox();
		useJavaUICheckBox.setSelected(this.useJavaUI);
		useJavaUICheckBox.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				PreferencesDialog.this.useJavaUI = useJavaUICheckBox
						.isSelected();
			}
		});
		useJavaUIPanel.add(useJavaUICheckBox);
		appearancePanel.add(useJavaUIPanel);

		// Create the fontSize spinner.
		final JPanel fontSizePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		fontSizePanel
				.add(new JLabel(Il8nResources.getString("Font_Size") + ":"));
		final SpinnerModel fontSizeSpinnerModel = new SpinnerNumberModel(
				this.fontSize, new Integer(2), new Integer(20), new Integer(1));
		fontSizeSpinnerModel.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				PreferencesDialog.this.fontSize = (Integer) fontSizeSpinnerModel
						.getValue();
			}
		});
		JSpinner fontSizeSpinner = new JSpinner(fontSizeSpinnerModel);
		fontSizePanel.add(fontSizeSpinner);
		appearancePanel.add(fontSizePanel);

		// Add the general panel to the tabbed pane.
		preferencesPane
				.addTab(Il8nResources.getString("General"), generalPanel);

		// Add the appearance panel to the tabbed pane.
		preferencesPane.addTab(Il8nResources.getString("Appearance"),
				appearancePanel);

		// Add the tabbed pane to the preferences dialog window.
		this.dialog.add(preferencesPane);

		// Add the Ok and Cancel buttons to the preferences dialog window.
		this.dialog.add(buttonBox);
	}

	public void display() {
		// Show the dialog.
		this.dialog.setResizable(false);
		this.dialog.pack();
		this.dialog.setLocationRelativeTo(null);
		this.dialog.setVisible(true);
	}
}