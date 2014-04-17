package scriptease.gui.action.libraryeditor;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Collection;

import javax.swing.Action;
import javax.swing.KeyStroke;

import scriptease.ScriptEase;
import scriptease.gui.WindowFactory;
import scriptease.gui.action.ActiveModelSensitiveAction;
import scriptease.gui.pane.LibraryPanel;
import scriptease.model.CodeBlock;
import scriptease.model.CodeBlockSource;
import scriptease.model.atomic.KnowIt;
import scriptease.model.complex.CauseIt;
import scriptease.model.semodel.SEModelManager;
import scriptease.model.semodel.librarymodel.LibraryModel;
import scriptease.translator.TranslatorManager;
import scriptease.translator.io.model.GameType;
import scriptease.util.ListOp;

/**
 * Inserts a new ScriptIt into the library. The new ScriptIt defaults to the
 * first type it finds in the GameTypeManager that has slots. This automatically
 * identifies the new ScriptIt as a cause, rather than an effect.
 * 
 * @author kschenk
 * 
 */
@SuppressWarnings("serial")
public class NewCauseAction extends ActiveModelSensitiveAction {
	private static final String NEW_CAUSE_NAME = "Cause";

	private static final NewCauseAction instance = new NewCauseAction();

	public static NewCauseAction getInstance() {
		return instance;
	}

	private NewCauseAction() {
		super(NewCauseAction.NEW_CAUSE_NAME);
		this.putValue(Action.SHORT_DESCRIPTION, NewCauseAction.NEW_CAUSE_NAME);
		this.putValue(
				Action.ACCELERATOR_KEY,
				KeyStroke.getKeyStroke(KeyEvent.VK_C, ActionEvent.CTRL_MASK
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

		final CauseIt newCause;
		final CodeBlock codeBlock;
		final KnowIt parameter;
		final Collection<KnowIt> parameters;
		final String subject = "subject";

		GameType type;

		library = (LibraryModel) SEModelManager.getInstance().getActiveModel();

		if (!library.getReadOnly() || ScriptEase.DEBUG_MODE) {
			newCause = new CauseIt(library, library.getNextID(), "When <"
					+ subject + ">");

			type = null;
			parameter = new KnowIt(library, library.getNextID(), subject);
			parameters = new ArrayList<KnowIt>();

			parameters.add(parameter);

			for (GameType gameType : TranslatorManager.getInstance()
					.getActiveDefaultLibrary().getGameTypes()) {
				// We just need the first cause with slots, so we can just
				// search
				// the default library.
				if (!gameType.getSlots().isEmpty()) {
					type = gameType;
					parameter.setTypes(ListOp.createList(type));
					break;
				}
			}

			if (type != null) {
				final String slot = type.getSlots().iterator().next();

				codeBlock = new CodeBlockSource(subject, slot, parameters,
						library, library.getNextID());

				newCause.addCodeBlock(codeBlock);
				newCause.setDisplayText("When <" + subject + ">");
				newCause.setVisible(true);

				library.add(newCause);
				LibraryPanel.getInstance().navigateToComponent(newCause);
			} else {
				WindowFactory.getInstance().showWarningDialog(
						"No Types With Slots Found",
						"I couldn't find any game types with slots.\n\n"
								+ "Add a type with a slot, or a slot to an\n"
								+ "existing type before trying this again.");

			}
		}
	}
}
