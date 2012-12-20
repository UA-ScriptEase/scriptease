package scriptease.gui.action.libraryeditor;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Collection;

import javax.swing.Action;
import javax.swing.KeyStroke;

import scriptease.gui.WindowFactory;
import scriptease.gui.action.ActiveTranslatorSensitiveAction;
import scriptease.model.CodeBlock;
import scriptease.model.CodeBlockSource;
import scriptease.model.LibraryModel;
import scriptease.model.atomic.KnowIt;
import scriptease.model.complex.ScriptIt;
import scriptease.translator.APIDictionary;
import scriptease.translator.TranslatorManager;
import scriptease.translator.apimanagers.GameTypeManager;
import scriptease.translator.io.model.GameType;

/**
 * Inserts a new ScriptIt into the library. The new ScriptIt defaults to the
 * first type it finds in the GameTypeManager that has slots. This automatically
 * identifies the new ScriptIt as a cause, rather than an effect.
 * 
 * @author kschenk
 * 
 */
@SuppressWarnings("serial")
public class NewCauseAction extends ActiveTranslatorSensitiveAction {
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
	public void actionPerformed(ActionEvent e) {
		final APIDictionary apiDictionary;
		final LibraryModel libraryModel;
		final GameTypeManager gameTypeManager;

		final ScriptIt newCause;
		final CodeBlock codeBlock;
		final KnowIt parameter;
		final Collection<KnowIt> parameters;

		final String SUBJECT = "subject";

		GameType type;

		apiDictionary = TranslatorManager.getInstance()
				.getActiveAPIDictionary();
		libraryModel = apiDictionary.getLibrary();
		gameTypeManager = TranslatorManager.getInstance()
				.getActiveGameTypeManager();

		newCause = new ScriptIt("When <" + SUBJECT + ">");

		type = null;
		parameter = new KnowIt(SUBJECT);
		parameters = new ArrayList<KnowIt>();

		parameters.add(parameter);

		for (GameType gameType : gameTypeManager.getGameTypes()) {
			if (!gameType.getSlots().isEmpty()) {
				final Collection<String> types;

				type = gameType;
				types = new ArrayList<String>();
				types.add(gameType.getKeyword());
				parameter.setTypes(types);

				break;
			}
		}

		if (type != null) {
			final int id = apiDictionary.getNextCodeBlockID();
			final String slot = type.getSlots().iterator().next();

			codeBlock = new CodeBlockSource(SUBJECT, slot, parameters, id);

			newCause.addCodeBlock(codeBlock);
			newCause.setDisplayText("When <" + SUBJECT + ">");
			newCause.setVisible(true);

			libraryModel.add(newCause);
		} else {
			WindowFactory.getInstance().showWarningDialog(
					"No Types With Slots Found",
					"I couldn't find any game types with slots.\n\n"
							+ "Add a type with a slot, or a slot to an\n"
							+ "existing type before trying this again.");
		}
	}
}
