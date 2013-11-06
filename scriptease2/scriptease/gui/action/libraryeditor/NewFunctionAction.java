package scriptease.gui.action.libraryeditor;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import javax.swing.Action;
import javax.swing.KeyStroke;

import scriptease.gui.action.ActiveModelSensitiveAction;
import scriptease.gui.pane.LibraryPanel;
import scriptease.model.complex.FunctionIt;
import scriptease.model.semodel.SEModelManager;
import scriptease.model.semodel.librarymodel.LibraryModel;

/**
 * Inserts a new FunctionIt into the Library. 
 * 
 * @author jyuen
 */
@SuppressWarnings("serial")
public class NewFunctionAction extends ActiveModelSensitiveAction {
	private static final String NEW_FUNCTION_NAME = "Function";

	private static final NewFunctionAction instance = new NewFunctionAction();

	public static final NewFunctionAction getInstance() {
		return NewFunctionAction.instance;
	}

	private NewFunctionAction() {
		super(NewFunctionAction.NEW_FUNCTION_NAME);

		this.putValue(Action.SHORT_DESCRIPTION,
				NewFunctionAction.NEW_FUNCTION_NAME);
		this.putValue(
				Action.ACCELERATOR_KEY,
				KeyStroke.getKeyStroke(KeyEvent.VK_F, ActionEvent.CTRL_MASK
						+ ActionEvent.SHIFT_MASK));
	}

	@Override
	protected boolean isLegal() {
		return super.isLegal()
				&& SEModelManager.getInstance().getActiveModel() instanceof LibraryModel;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		final LibraryModel libraryModel;

		final FunctionIt newFunctionIt;
		
		libraryModel = (LibraryModel) SEModelManager.getInstance()
				.getActiveModel();

		newFunctionIt = new FunctionIt("New Function");
		newFunctionIt.setVisible(true);
		newFunctionIt.setDisplayText("New Function");

		libraryModel.add(newFunctionIt);
		LibraryPanel.getInstance().navigateToComponent(newFunctionIt);
	}
}
