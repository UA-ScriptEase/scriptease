package scriptease.gui.pane;

import javax.swing.JPanel;

import scriptease.model.StoryModel;
import scriptease.translator.Translator;
import scriptease.translator.codegenerator.CauseEffectPicker;
import scriptease.translator.codegenerator.GameObjectPicker;

/**
 * Factory for creating the various ScriptEase Pattern Constructor panes. There
 * are methods for creating a Game Object Picker, and a Story Editor Pane.
 * 
 * @author remiller
 * @author mfchurch
 */
public class PaneFactory {

	/**
	 * Builds a pane containing all game objects in the active module, organized
	 * by category, allowing the user to drag them onto bindings in a Story.
	 * 
	 * @return A JPanel GameObject picker.
	 * @author graves
	 * @author mfchurch
	 */
	public static JPanel buildGameObjectPane(StoryModel model) {
		GameObjectPicker picker;

		if (model != null) {
			Translator translator = model.getTranslator();
			if (translator != null) {
				// Get the picker
				if ((picker = translator.getCustomGameObjectPicker()) == null) {
					picker = new GameObjectPane();
				}
				return picker.getPickerPanel();
			}
		}
		// otherwise return an empty hidden JPanel
		JPanel jPanel = new JPanel();
		jPanel.setVisible(false);
		return jPanel;
	}

	/**
	 * Builds a pane containing all causes, effects, descriptions, and folders,
	 * allowing the user to drag them into the story.
	 * 
	 * @param model
	 * @return
	 */
	public static JPanel buildCauseEffectPane(StoryModel model) {
		CauseEffectPicker picker;

		if (model != null) {
			Translator translator = model.getTranslator();
			if (translator != null) {
				// Get the picker
				if ((picker = translator.getCustomCauseEffectPicker()) == null) {
					picker = new CauseEffectPane();
				}
				return picker.getCauseEffectPanel();
			}
		}

		// otherwise return an empty hidden JPanel
		JPanel jPanel = new JPanel();
		jPanel.setVisible(false);
		return jPanel;
	}
}
