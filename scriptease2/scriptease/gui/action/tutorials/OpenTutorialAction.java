package scriptease.gui.action.tutorials;

import java.awt.Desktop;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;

import javax.swing.AbstractAction;

import scriptease.gui.WindowFactory;

/**
 * Opens the tutorial file.
 * 
 * @author jyuen
 * 
 */
@SuppressWarnings("serial")
public class OpenTutorialAction extends AbstractAction {

	final File tutorial;

	public OpenTutorialAction(File tutorial) {
		super(tutorial.getName());
		this.tutorial = tutorial;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (Desktop.isDesktopSupported()) {
			try {
				Desktop.getDesktop().open(tutorial);
			} catch (IOException ex) {
				WindowFactory
						.getInstance()
						.showWarningDialog(
								"Could not open file",
								"This file could not be opened. Please check if you have the tools to open files of this format.");
			}
		}
	}
}
