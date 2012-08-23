package scriptease.resources.schema;

import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSpinner;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.SpinnerModel;
import javax.swing.SpinnerNumberModel;
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
	private final Box buttonBox;
	private final JButton okButton;
	private final JButton cancelButton;
	private final JTabbedPane preferencesPane;
	private final JPanel generalPanel;
	private final JPanel appearancePanel;
	private final JRadioButton uncompressedLayoutButton;
	private final JRadioButton compressedLayoutButton;
	private final ButtonGroup layoutsGroup;

	// Variables to hold temporary preferences.
	private Integer maxUndoSteps;
	private Boolean useJavaUI;
	private Integer fontSize;
	private String outputDirectory;
	private Boolean debugMode;
	private String preferredLayout;

	private void loadCurrentPreferences() {
		ScriptEase instance = ScriptEase.getInstance();
		try {
			maxUndoSteps = Integer.parseInt(instance
					.getPreference(ScriptEase.UNDO_STACK_SIZE_KEY));
		} catch (NumberFormatException e) {
			maxUndoSteps = 50; // If the input is invalid, use the default.
		}
		try {
			fontSize = Integer.parseInt(instance
					.getPreference(ScriptEase.FONT_SIZE_KEY));
		} catch (NumberFormatException e) {
			fontSize = 12;
		}

		// Invalid input results in false being returned.
		useJavaUI = Boolean.parseBoolean(instance
				.getPreference(ScriptEase.LOOK_AND_FEEL_KEY));
		debugMode = Boolean.parseBoolean(instance
				.getPreference(ScriptEase.DEBUG_KEY));

		preferredLayout = instance
				.getPreference(ScriptEase.PREFERRED_LAYOUT_KEY);
		// If the input is invalid, set it to the default.
		if (preferredLayout == null
				|| !(preferredLayout
						.equalsIgnoreCase(ScriptEase.COMPRESSED_LAYOUT) || preferredLayout
						.equalsIgnoreCase(ScriptEase.UNCOMPRESSED_LAYOUT))) {
			preferredLayout = ScriptEase.COMPRESSED_LAYOUT;
		}

		outputDirectory = instance
				.getPreference(ScriptEase.OUTPUT_DIRECTORY_KEY);
		if (outputDirectory == null)
			outputDirectory = "output";
	}

	public PreferencesDialog(Frame owner) {
		// Load the current preferences settings before starting.
		loadCurrentPreferences();

		// Create the modal dialog for the preferences window.
		dialog = new JDialog(owner, Il8nResources.getString("Preferences"),
				true);

		dialog.getContentPane().setLayout(
				new BoxLayout(dialog.getContentPane(), BoxLayout.Y_AXIS));

		// Build the row of buttons.
		buttonBox = new Box(BoxLayout.X_AXIS);

		okButton = new JButton(Il8nResources.getString("Okay"));
		okButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				// Set the preferences.
				ScriptEase instance = ScriptEase.getInstance();
				instance.setPreference(ScriptEase.UNDO_STACK_SIZE_KEY,
						maxUndoSteps.toString());
				instance.setPreference(ScriptEase.LOOK_AND_FEEL_KEY,
						useJavaUI.toString());
				instance.setPreference(ScriptEase.FONT_SIZE_KEY,
						fontSize.toString());
				instance.setPreference(ScriptEase.OUTPUT_DIRECTORY_KEY,
						outputDirectory);
				instance.setPreference(ScriptEase.DEBUG_KEY,
						debugMode.toString());
				instance.setPreference(ScriptEase.PREFERRED_LAYOUT_KEY,
						preferredLayout);

				// Write the preferences to file.
				instance.saveUserPrefs();

				// Notify the user that changes take effect after restarting the
				// program.
				WindowFactory.getInstance().showInformationDialog(
						PROGRAM_RESTART_REQUIRED_TITLE,
						PROGRAM_RESTART_REQUIRED_TEXT);

				// Close the dialog.
				dialog.setVisible(false);
				dialog.dispose();
			}
		});
		buttonBox.add(okButton);

		cancelButton = new JButton(Il8nResources.getString("Cancel"));
		cancelButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				// Close the dialog.
				dialog.setVisible(false);
				dialog.dispose();
			}
		});
		buttonBox.add(cancelButton);

		// Create the tabbed pane for the preferences panes.
		preferencesPane = new JTabbedPane(JTabbedPane.TOP);

		// Create the panel for the general preferences.
		generalPanel = new JPanel();
		generalPanel.setLayout(new BoxLayout(generalPanel, BoxLayout.Y_AXIS));

		// Create the Undo stack size spinner.
		final JPanel undoStackSizePanel = new JPanel(new FlowLayout(
				FlowLayout.LEFT));
		final SpinnerModel undoStackSizeSpinnerModel = new SpinnerNumberModel(
				maxUndoSteps, new Integer(0), new Integer(10000),
				new Integer(1));
		undoStackSizeSpinnerModel.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				maxUndoSteps = (Integer) undoStackSizeSpinnerModel.getValue();
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
				outputDirectory);
		outputDirectoryTextField.addCaretListener(new CaretListener() {
			@Override
			public void caretUpdate(CaretEvent e) {
				outputDirectory = outputDirectoryTextField.getText();
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
		debugModeCheckBox.setSelected(debugMode);
		debugModeCheckBox.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				debugMode = debugModeCheckBox.isSelected();
			}
		});
		debugModePanel.add(new JLabel(Il8nResources
				.getString("Run_in_debug_mode") + ":"));
		debugModePanel.add(debugModeCheckBox);
		generalPanel.add(debugModePanel);

		// TODO: Add new preferences here.

		// Create the panel for the appearance preferences.
		appearancePanel = new JPanel();
		appearancePanel.setLayout(new BoxLayout(appearancePanel,
				BoxLayout.Y_AXIS));

		// Create the radio buttons for selecting preferred layout.
		final JPanel preferredLayoutPanel = new JPanel(new FlowLayout(
				FlowLayout.LEFT));
		preferredLayoutPanel.add(new JLabel(Il8nResources.getString("Layout")
				+ ":"));

		final JPanel layoutSelectionPanel = new JPanel();
		layoutSelectionPanel.setLayout(new BoxLayout(layoutSelectionPanel,
				BoxLayout.Y_AXIS));

		// Create the radio button for the uncompressed layout.
		uncompressedLayoutButton = new JRadioButton(
				Il8nResources.getString("Uncompressed_Layout"));
		uncompressedLayoutButton.setMnemonic(KeyEvent.VK_T);
		uncompressedLayoutButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				// Set the preferred layout to be the uncompressed layout.
				preferredLayout = ScriptEase.UNCOMPRESSED_LAYOUT;
			}
		});

		// Create the radio button for the compressed layout.
		compressedLayoutButton = new JRadioButton(
				Il8nResources.getString("Compressed_Layout"));
		compressedLayoutButton.setMnemonic(KeyEvent.VK_C);
		compressedLayoutButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				// Set the preferred layout to be the compressed layout.
				preferredLayout = ScriptEase.COMPRESSED_LAYOUT;
			}
		});

		// Create the button group for the layout radio buttons.
		layoutsGroup = new ButtonGroup();
		layoutsGroup.add(compressedLayoutButton);
		layoutsGroup.add(uncompressedLayoutButton);

		// Initially select the proper radio button.
		if (preferredLayout.equalsIgnoreCase(ScriptEase.UNCOMPRESSED_LAYOUT)) {
			layoutsGroup.setSelected(uncompressedLayoutButton.getModel(), true);
		} else if (preferredLayout
				.equalsIgnoreCase(ScriptEase.COMPRESSED_LAYOUT)) {
			layoutsGroup.setSelected(compressedLayoutButton.getModel(), true);
		}

		// Add the radio buttons to the appearance panel.
		layoutSelectionPanel.add(uncompressedLayoutButton);
		layoutSelectionPanel.add(compressedLayoutButton);
		preferredLayoutPanel.add(layoutSelectionPanel);
		appearancePanel.add(preferredLayoutPanel);

		// Create the useJavaUI checkbox.
		final JPanel useJavaUIPanel = new JPanel(
				new FlowLayout(FlowLayout.LEFT));
		useJavaUIPanel.add(new JLabel(Il8nResources.getString("Use_Java_UI")
				+ ":"));
		final JCheckBox useJavaUICheckBox = new JCheckBox();
		useJavaUICheckBox.setSelected(useJavaUI);
		useJavaUICheckBox.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				useJavaUI = useJavaUICheckBox.isSelected();
			}
		});
		useJavaUIPanel.add(useJavaUICheckBox);
		appearancePanel.add(useJavaUIPanel);

		// Create the fontSize spinner.
		final JPanel fontSizePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		fontSizePanel
				.add(new JLabel(Il8nResources.getString("Font_Size") + ":"));
		final SpinnerModel fontSizeSpinnerModel = new SpinnerNumberModel(
				fontSize, new Integer(2), new Integer(20), new Integer(1));
		fontSizeSpinnerModel.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				fontSize = (Integer) fontSizeSpinnerModel.getValue();
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
		dialog.add(preferencesPane);

		// Add the Ok and Cancel buttons to the preferences dialog window.
		dialog.add(buttonBox);
	}

	public void display() {
		// Show the dialog.
		dialog.setResizable(false);
		dialog.pack();
		dialog.setLocationRelativeTo(null);
		dialog.setVisible(true);
	}
}