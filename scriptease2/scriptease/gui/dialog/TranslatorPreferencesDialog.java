package scriptease.gui.dialog;

import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import javax.swing.filechooser.FileNameExtensionFilter;

import scriptease.gui.WindowFactory;
import scriptease.translator.Translator;
import scriptease.translator.TranslatorManager;

/**
 * TranslatorPreferencesDialog allows the user to set preferences in the
 * translator.ini file, including the compiler path and the game directory
 * location.
 * 
 * @author jyuen
 */
@SuppressWarnings("serial")
public class TranslatorPreferencesDialog extends JDialog {

	private String compilerPath;
	private String gameDirectoryPath;

	private void loadPreferences() {
		final Translator translator;

		translator = TranslatorManager.getInstance().getActiveTranslator();

		this.compilerPath = translator
				.getProperty(Translator.DescriptionKeys.COMPILER_PATH);
		this.gameDirectoryPath = translator
				.getProperty(Translator.DescriptionKeys.GAME_DIRECTORY);
	}

	public TranslatorPreferencesDialog() {
		final String TRANS_PREFERENCES_TEXT = "Translator Preferences";
		final String COMPILER_PATH_TEXT = "Compiler path:";
		final String GAME_DIRECTORY_TEXT = "Game directory:";
		final String BROWSE_TEXT = "Browse";

		final JPanel compilerPathPanel;
		final JPanel gameDirectoryPanel;

		final JTextField compilerPathTextField;
		final JTextField gameDirectoryTextField;

		final JButton compilerPathBrowseButton;
		final JButton gameDirectoryBrowseButton;

		// Load the current settings before starting.
		this.loadPreferences();

		compilerPathPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		gameDirectoryPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));

		compilerPathTextField = new JTextField(this.compilerPath);
		gameDirectoryTextField = new JTextField(this.gameDirectoryPath);

		compilerPathBrowseButton = new JButton(BROWSE_TEXT);
		gameDirectoryBrowseButton = new JButton(BROWSE_TEXT);

		// Set up the compiler path panel
		compilerPathTextField.addCaretListener(new CaretListener() {

			@Override
			public void caretUpdate(CaretEvent e) {
				TranslatorPreferencesDialog.this.compilerPath = compilerPathTextField
						.getText();
			}
		});
		compilerPathBrowseButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				final File filePath;

				filePath = WindowFactory.getInstance().showFileChooser(
						"Select", "", createFileFilter("exe"));

				if (filePath != null)
					compilerPathTextField.setText(filePath.getAbsolutePath());
			}
		});
		compilerPathPanel.add(new JLabel(COMPILER_PATH_TEXT));
		compilerPathPanel.add(compilerPathTextField);
		compilerPathPanel.add(compilerPathBrowseButton);

		// Set up the game directory path panel
		gameDirectoryTextField.addCaretListener(new CaretListener() {

			@Override
			public void caretUpdate(CaretEvent e) {
				TranslatorPreferencesDialog.this.gameDirectoryPath = gameDirectoryTextField
						.getText();
			}
		});
		gameDirectoryBrowseButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				final File filePath;

				filePath = WindowFactory.getInstance().showFileChooser("Select",
						"", null);

				if (filePath != null)
					gameDirectoryTextField.setText(filePath.getAbsolutePath());
			}
		});
		gameDirectoryPanel.add(new JLabel(GAME_DIRECTORY_TEXT));
		gameDirectoryPanel.add(gameDirectoryTextField);
		gameDirectoryPanel.add(gameDirectoryBrowseButton);

		// Create the main dialog box.
		this.setTitle(TRANS_PREFERENCES_TEXT);
		this.getContentPane().setLayout(
				new BoxLayout(this.getContentPane(), BoxLayout.Y_AXIS));

		this.add(compilerPathPanel);
		this.add(gameDirectoryPanel);
		this.add(createButtons(compilerPathTextField, gameDirectoryTextField));
	}

	private Box createButtons(final JTextField compilerPathTextField,
			final JTextField gameDirectoryTextField) {
		final String PROGRAM_RESTART_REQUIRED_TEXT = "Changes to Preferences may not take effect until ScriptEase is restarted.";
		final String PROGRAM_RESTART_REQUIRED_TITLE = "Program restart required.";

		final String OKAY_TEXT = "Okay";
		final String CANCEL_TEXT = "Cancel";

		final Box buttonBox;
		final JButton okButton;
		final JButton cancelButton;

		buttonBox = new Box(BoxLayout.X_AXIS);

		okButton = new JButton(OKAY_TEXT);
		okButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				// Set the preferences.
				final Translator translator;

				translator = TranslatorManager.getInstance()
						.getActiveTranslator();

				translator.setPreference(
						Translator.DescriptionKeys.COMPILER_PATH,
						compilerPathTextField.getText());
				translator.setPreference(
						Translator.DescriptionKeys.GAME_DIRECTORY,
						gameDirectoryTextField.getText());

				translator.saveTranslatorPreferences();

				// Notify user that effects only work after restart.
				WindowFactory.getInstance().showInformationDialog(
						PROGRAM_RESTART_REQUIRED_TITLE,
						PROGRAM_RESTART_REQUIRED_TEXT);

				// Close the dialog.
				TranslatorPreferencesDialog.this.closeDialog();
			}
		});
		buttonBox.add(okButton);

		cancelButton = new JButton(CANCEL_TEXT);
		cancelButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				TranslatorPreferencesDialog.this.closeDialog();
			}
		});
		buttonBox.add(cancelButton);

		return buttonBox;
	}

	/**
	 * Creates a new file filter to isolate "ini" files.
	 * 
	 * @return
	 */
	private javax.swing.filechooser.FileFilter createFileFilter(
			String filterString) {
		final javax.swing.filechooser.FileFilter filter;

		filter = new FileNameExtensionFilter(filterString, filterString);
		return filter;
	}

	/**
	 * Close the dialog.
	 */
	private void closeDialog() {
		this.setVisible(false);
		this.dispose();
	}

	/**
	 * Display the dialog.
	 */
	public void display() {
		this.setResizable(false);
		this.pack();
		this.setLocationRelativeTo(null);
		this.setVisible(true);
	}
}
