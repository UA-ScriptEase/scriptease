package scriptease.gui.action.libraryeditor;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import javax.swing.Action;
import javax.swing.KeyStroke;

import scriptease.gui.action.ActiveTranslatorSensitiveAction;
import scriptease.model.LibraryModel;
import scriptease.model.SEModelManager;
import scriptease.model.atomic.describeits.DescribeIt;
import scriptease.model.atomic.describeits.DescribeItNode;

/**
 * Inserts a new Description into the Library.
 * 
 * @author kschenk
 * 
 */
@SuppressWarnings("serial")
public class NewDescriptionAction extends ActiveTranslatorSensitiveAction {
	private static final String NEW_DESCRIPTION_NAME = "Description";

	private static final NewDescriptionAction instance = new NewDescriptionAction();

	public static final NewDescriptionAction getInstance() {
		return instance;
	}

	private NewDescriptionAction() {
		super(NewDescriptionAction.NEW_DESCRIPTION_NAME);

		this.putValue(Action.SHORT_DESCRIPTION,
				NewDescriptionAction.NEW_DESCRIPTION_NAME);
		this.putValue(
				Action.ACCELERATOR_KEY,
				KeyStroke.getKeyStroke(KeyEvent.VK_D, ActionEvent.CTRL_MASK
						+ ActionEvent.SHIFT_MASK));

	}

	@Override
	public void actionPerformed(ActionEvent e) {
		final LibraryModel libraryModel;
		final DescribeItNode describeItNode;
		final DescribeIt describeIt;

		libraryModel = (LibraryModel) SEModelManager.getInstance()
				.getActiveModel();

		describeItNode = new DescribeItNode("Placeholder Node");
		describeIt = new DescribeIt("New DescribeIt", describeItNode);

		libraryModel.add(describeIt);
	}
}
