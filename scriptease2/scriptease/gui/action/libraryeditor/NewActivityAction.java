package scriptease.gui.action.libraryeditor;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.swing.Action;
import javax.swing.KeyStroke;

import scriptease.ScriptEase;
import scriptease.gui.action.ActiveModelSensitiveAction;
import scriptease.model.CodeBlock;
import scriptease.model.CodeBlockSource;
import scriptease.model.complex.ActivityIt;
import scriptease.model.semodel.SEModelManager;
import scriptease.model.semodel.librarymodel.LibraryModel;
import scriptease.translator.codegenerator.code.fragments.AbstractFragment;
import scriptease.translator.codegenerator.code.fragments.FormatReferenceFragment;
import scriptease.translator.io.model.GameType;
import scriptease.util.ListOp;

/**
 * Inserts a new FunctionIt into the Library.
 * 
 * @author jyuen
 */
@SuppressWarnings("serial")
public class NewActivityAction extends ActiveModelSensitiveAction {
	private static final String NEW_ACTIVITY_NAME = "Activity";

	private static final NewActivityAction instance = new NewActivityAction();

	public static final NewActivityAction getInstance() {
		return NewActivityAction.instance;
	}

	private NewActivityAction() {
		super(NewActivityAction.NEW_ACTIVITY_NAME);

		this.putValue(Action.SHORT_DESCRIPTION,
				NewActivityAction.NEW_ACTIVITY_NAME);
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
		final LibraryModel library;

		final ActivityIt newActivityIt;
		final CodeBlock codeBlock;
		final Collection<String> types;
		final List<AbstractFragment> formatRef;

		library = (LibraryModel) SEModelManager.getInstance().getActiveModel();

		if (!library.isReadOnly() || ScriptEase.DEBUG_MODE) {
			newActivityIt = new ActivityIt(library, "New Activity");
			newActivityIt.setVisible(true);  
			newActivityIt.addLabel("A");

			types = ListOp.createList(GameType.DEFAULT_VOID_TYPE);

			formatRef = new ArrayList<AbstractFragment>();
			formatRef.add(new FormatReferenceFragment("activityItChildren",
					FormatReferenceFragment.Type.NONE));

			codeBlock = new CodeBlockSource(library);
			codeBlock.setTypesByName(types);
			codeBlock.setCode(formatRef);

			newActivityIt.addCodeBlock(codeBlock);

			library.add(newActivityIt);
		}
	}
}
