package scriptease.gui.action.storycomponentbuilder;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Collection;

import javax.swing.Action;
import javax.swing.KeyStroke;

import scriptease.controller.VisibilityManager;
import scriptease.controller.apimanagers.GameTypeManager;
import scriptease.gui.action.ActiveTranslatorSensitiveAction;
import scriptease.model.CodeBlock;
import scriptease.model.CodeBlockSource;
import scriptease.model.LibraryModel;
import scriptease.model.atomic.KnowIt;
import scriptease.model.complex.ScriptIt;
import scriptease.translator.APIDictionary;
import scriptease.translator.Translator;
import scriptease.translator.TranslatorManager;
import scriptease.translator.codegenerator.code.fragments.Fragment;
import scriptease.translator.io.model.GameType;

/**
 * Inserts a new ScriptIt into the library. The new ScriptIt is completely
 * empty, which makes the library automatically identify it as an effect.
 * 
 * @author kschenk
 * 
 */
@SuppressWarnings("serial")
public class NewEffectAction extends ActiveTranslatorSensitiveAction {
	private static final String NEW_EFFECT_NAME = "Effect";

	private static final NewEffectAction instance = new NewEffectAction();

	public static NewEffectAction getInstance() {
		return instance;
	}

	protected NewEffectAction() {
		super(NEW_EFFECT_NAME);
		this.putValue(SHORT_DESCRIPTION, NEW_EFFECT_NAME);
		this.putValue(Action.ACCELERATOR_KEY,
				KeyStroke.getKeyStroke(KeyEvent.VK_E, ActionEvent.CTRL_MASK + ActionEvent.SHIFT_MASK));
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		final Translator activeTranslator;
		final APIDictionary apiDictionary;
		final LibraryModel libraryModel;

		final ScriptIt newCause;
		final CodeBlock codeBlock;

		activeTranslator = TranslatorManager.getInstance()
				.getActiveTranslator();
		apiDictionary = activeTranslator.getApiDictionary();
		libraryModel = apiDictionary.getLibrary();

		newCause = new ScriptIt("Do Something");

		
		
		final int id = apiDictionary.getNextCodeBlockID();
		codeBlock = new CodeBlockSource(id);

		newCause.addCodeBlock(codeBlock);
		newCause.setDisplayText("Do Something");

		// Set the visibility
		VisibilityManager.getInstance().setVisibility(newCause, true);

		libraryModel.add(newCause);
	}
}
