package scriptease.gui.action;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ContainerEvent;
import java.awt.event.ContainerListener;
import java.awt.event.KeyEvent;

import javax.swing.Action;
import javax.swing.FocusManager;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.KeyStroke;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;

import scriptease.gui.PanelFactory;
import scriptease.gui.SEFrame;
import scriptease.gui.graph.GraphPanel;
import scriptease.gui.storycomponentpanel.StoryComponentPanel;
import scriptease.gui.storycomponentpanel.StoryComponentPanelJList;
import scriptease.gui.storycomponentpanel.StoryComponentPanelManager;
import scriptease.gui.storycomponentpanel.StoryComponentPanelTree;
import scriptease.model.PatternModel;
import scriptease.model.PatternModelManager;
import scriptease.model.StoryModel;

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

		return true;
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

		focusOwner = FocusManager.getCurrentManager().getFocusOwner();
		
		if (focusOwner instanceof StoryComponentPanel) {
			//Leave this be. It lets us delete many at once.
			final StoryComponentPanel panel;
			panel = (StoryComponentPanel) focusOwner;
			
			panel.getSelectionManager().deleteSelected();
			//this.getActiveSelectionManager().deleteSelected();
		} else if (focusOwner instanceof StoryComponentPanelJList) {
			final StoryComponentPanelJList list;
			list = (StoryComponentPanelJList) focusOwner;
			
			//Git zee selected story components and EXTERMEENATE ZEM!
		} else if (focusOwner instanceof GraphPanel) {
			final GraphPanel graphPanel;
			graphPanel = (GraphPanel) focusOwner;
			
			//Get the selected quest point and 0bl173r4t3 1t
		}
	}
}
