package scriptease.gui.dialog;

import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.TitledBorder;
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
		final String BROWSE_TEXT = "Browse";
		final int TEXT_WIDTH = 30;

		final JPanel compilerPathPanel;
		final JPanel gameDirectoryPanel;

		final JTextField compilerPathTextField;
		final JTextField gameDirectoryTextField;

		final JButton compilerPathBrowseButton;
		final JButton gameDirectoryBrowseButton;

		final JCheckBox compilerCheckBox;

		// Load the current settings before starting.
		this.loadPreferences();

		compilerPathPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		gameDirectoryPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));

		compilerPathTextField = new JTextField(this.compilerPath);
		gameDirectoryTextField = new JTextField(this.gameDirectoryPath);

		compilerPathBrowseButton = new JButton(BROWSE_TEXT);
		gameDirectoryBrowseButton = new JButton(BROWSE_TEXT);

		compilerCheckBox = new JCheckBox();

		// Set up the compiler path panel

		// Set compiler path to deselected by default if there is no available
		// compiler
		if (TranslatorManager.getInstance().getActiveTranslator()
				.getProperty(Translator.DescriptionKeys.COMPILER_PATH)
				.equals(false)) {
			compilerCheckBox.setSelected(false);
			compilerPathTextField.setEnabled(false);
			compilerPathBrowseButton.setEnabled(false);
		} else
			compilerCheckBox.setSelected(true);
		compilerCheckBox.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				if (!compilerCheckBox.isSelected()) {
					compilerPathTextField.setEnabled(false);
					compilerPathBrowseButton.setEnabled(false);
				} else {
					compilerPathTextField.setEnabled(true);
					compilerPathBrowseButton.setEnabled(true);
				}
			}
		});
		compilerPathTextField.setColumns(TEXT_WIDTH);
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
		compilerPathPanel.setBorder(BorderFactory.createTitledBorder(
				BorderFactory.createLineBorder(Color.gray), "Compiler Path",
				TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.TOP, new Font(
						"SansSerif", Font.PLAIN, 12), Color.black));
		compilerPathPanel.add(compilerCheckBox);
		compilerPathPanel.add(compilerPathTextField);
		compilerPathPanel.add(compilerPathBrowseButton);

		// Set up the game directory path panel
		gameDirectoryTextField.setColumns(TEXT_WIDTH);
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

				filePath = WindowFactory.getInstance().showDirectoryChooser(
						"Select", "", null);

				if (filePath != null)
					gameDirectoryTextField.setText(filePath.getAbsolutePath());
			}
		});
		gameDirectoryPanel.setBorder(BorderFactory.createTitledBorder(
				BorderFactory.createLineBorder(Color.gray), "Game Directory",
				TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.TOP, new Font(
						"SansSerif", Font.PLAIN, 12), Color.black));
		gameDirectoryPanel.add(gameDirectoryTextField);
		gameDirectoryPanel.add(gameDirectoryBrowseButton);

		// Create the main dialog box.
		this.setTitle(TRANS_PREFERENCES_TEXT);
		this.getContentPane().setLayout(
				new BoxLayout(this.getContentPane(), BoxLayout.Y_AXIS));

		this.add(gameDirectoryPanel);
		this.add(compilerPathPanel);
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
