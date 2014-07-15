package scriptease.gui.action.libraryeditor;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.Action;
import javax.swing.KeyStroke;

import scriptease.ScriptEase;
import scriptease.gui.action.ActiveModelSensitiveAction;
import scriptease.model.CodeBlock;
import scriptease.model.CodeBlockSource;
import scriptease.model.complex.behaviours.Behaviour;
import scriptease.model.semodel.SEModelManager;
import scriptease.model.semodel.librarymodel.LibraryModel;
import scriptease.translator.codegenerator.code.fragments.AbstractFragment;
import scriptease.translator.codegenerator.code.fragments.FormatReferenceFragment;
import scriptease.translator.io.model.GameType;
import scriptease.util.ListOp;

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
		final List<AbstractFragment> code;
		final LibraryModel library;
		final Behaviour newBehaviour;
		final CodeBlock codeBlock;

		library = (LibraryModel) SEModelManager.getInstance().getActiveModel();

		if (!library.isReadOnly() || ScriptEase.DEBUG_MODE) {
			code = new ArrayList<AbstractFragment>(1);
			newBehaviour = new Behaviour(library);

			codeBlock = new CodeBlockSource(library);

			code.add(new FormatReferenceFragment("behaviour"));

			codeBlock.setCode(code);
			codeBlock.setTypesByName(ListOp
					.createList(GameType.DEFAULT_VOID_TYPE));

			newBehaviour.addCodeBlock(codeBlock);
			newBehaviour.resetBehaviour();

			library.add(newBehaviour);
		}
	}
}
