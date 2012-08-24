package scriptease.gui.action;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import javax.swing.Action;
import javax.swing.KeyStroke;

import scriptease.gui.ComponentFocusManager;
import scriptease.gui.graph.GraphPanel;
import scriptease.gui.storycomponentpanel.StoryComponentPanel;
import scriptease.gui.storycomponentpanel.StoryComponentPanelJList;
import scriptease.model.LibraryModel;
import scriptease.model.PatternModel;
import scriptease.model.PatternModelManager;
import scriptease.translator.APIDictionary;
import scriptease.translator.Translator;
import scriptease.translator.TranslatorManager;

/**
 * Represents and performs the Delete command, as well as encapsulates its
 * enabled and name display state.
 * 
 * @author remiller
 * @author kschenk
 */
@SuppressWarnings("serial")
public final class DeleteAction extends ActiveModelSensitiveAction {
	private static final String DELETE_TEXT = "Delete";

	private static final Action instance = new DeleteAction();

	/**
	 * Gets the sole instance of this particular type of Action
	 * 
	 * @return The sole instance of this particular type of Action
	 */
	public static Action getInstance() {
		return DeleteAction.instance;
	}

	/**
	 * Updates the action to either be enabled or disabled depending on the
	 * current selection.
	 */
	protected boolean isLegal() {
		final PatternModel activeModel;

		activeModel = PatternModelManager.getInstance().getActiveModel();

		return activeModel != null;
	}

	/**
	 * Defines a <code>DeleteStoryComponentAction</code> object with no icon.
	 */
	private DeleteAction() {
		super(DeleteAction.DELETE_TEXT);

		this.putValue(Action.MNEMONIC_KEY, KeyEvent.VK_D);
		this.putValue(Action.ACCELERATOR_KEY,
				KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0));

		PatternModelManager.getInstance().addPatternModelPoolObserver(this);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		final Component focusOwner;

		focusOwner = ComponentFocusManager.getInstance().getFocus();

		if (focusOwner instanceof StoryComponentPanel) {
			// Delete StoryComponentPanels
			final StoryComponentPanel panel;
			panel = (StoryComponentPanel) focusOwner;

			panel.getSelectionManager().deleteSelected();
		} else if (focusOwner instanceof StoryComponentPanelJList
				&& PatternModelManager.getInstance().getActiveModel() instanceof LibraryModel) {
			// Delete elements from StoryComponentPanelJList
			final StoryComponentPanelJList list;
			list = (StoryComponentPanelJList) focusOwner;

			for (Object selectedObject : list.getSelectedValues()) {
				final StoryComponentPanel selectedPanel;
				selectedPanel = (StoryComponentPanel) selectedObject;

				final Translator activeTranslator;
				final APIDictionary apiDictionary;
				final LibraryModel libraryModel;

				activeTranslator = TranslatorManager.getInstance()
						.getActiveTranslator();
				apiDictionary = activeTranslator.getApiDictionary();
				libraryModel = apiDictionary.getLibrary();

				libraryModel.remove(selectedPanel.getStoryComponent());
			}
		} else if (focusOwner instanceof GraphPanel) {
			//Delete QuestNode
			
			// final GraphPanel graphPanel;
			// graphPanel = (GraphPanel) focusOwner;

			/*
			 * TODO Since graph points, quest points, quest point nodes,
			 * listeners, etc are wiggidy-wack, we can't implement this right
			 * now. Once we are able to get the currently selected quest node,
			 * though, we'll be able to.
			 * 
			 * We really need to implement better listeners for our graphs, and
			 * a better way of managing them.
			 */
		}
	}
}
