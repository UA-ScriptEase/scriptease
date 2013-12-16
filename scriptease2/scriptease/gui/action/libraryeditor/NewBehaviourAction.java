package scriptease.gui.action.libraryeditor;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import javax.swing.Action;
import javax.swing.KeyStroke;

import scriptease.gui.action.ActiveModelSensitiveAction;
import scriptease.gui.pane.LibraryPanel;
import scriptease.model.CodeBlock;
import scriptease.model.CodeBlockSource;
import scriptease.model.complex.behaviours.Behaviour;
import scriptease.model.semodel.SEModelManager;
import scriptease.model.semodel.librarymodel.LibraryModel;

/**
 * Inserts a new Behaviour into the Library. Each Behaviour codeblock has a slot
 * that defines implicits for the Initiator and Responder.
 * 
 * @author jyuen
 */
@SuppressWarnings("serial")
public class NewBehaviourAction extends ActiveModelSensitiveAction {
	private static final String NEW_BEHAVIOUR_NAME = "Behaviour";

	private static final NewBehaviourAction instance = new NewBehaviourAction();

	public static final NewBehaviourAction getInstance() {
		return NewBehaviourAction.instance;
	}

	private NewBehaviourAction() {
		super(NewBehaviourAction.NEW_BEHAVIOUR_NAME);

		this.putValue(Action.SHORT_DESCRIPTION,
				NewBehaviourAction.NEW_BEHAVIOUR_NAME);
		this.putValue(
				Action.ACCELERATOR_KEY,
				KeyStroke.getKeyStroke(KeyEvent.VK_B, ActionEvent.CTRL_MASK
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

		final Behaviour newBehaviour;
		final CodeBlock codeBlock;
		final int codeBlockID;
		final String slot;
		
		libraryModel = (LibraryModel) SEModelManager.getInstance()
				.getActiveModel();

		newBehaviour = new Behaviour("New Behaviour");
		codeBlockID = libraryModel.getNextCodeBlockID();
		slot = "onBehaviour";
		
		codeBlock = new CodeBlockSource(codeBlockID);
		codeBlock.setSlot(slot);

		newBehaviour.addCodeBlock(codeBlock);
		newBehaviour.setVisible(true);

		libraryModel.add(newBehaviour);
		LibraryPanel.getInstance().navigateToComponent(newBehaviour);
	}
}
