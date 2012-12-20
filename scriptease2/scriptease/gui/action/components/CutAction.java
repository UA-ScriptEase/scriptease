package scriptease.gui.action.components;

import java.awt.Component;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;

import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.KeyStroke;
import javax.swing.TransferHandler;

import scriptease.gui.SEFocusManager;
import scriptease.gui.SEGraph.SEGraph;
import scriptease.gui.action.ActiveModelSensitiveAction;
import scriptease.gui.storycomponentpanel.StoryComponentPanel;
import scriptease.gui.storycomponentpanel.StoryComponentPanelJList;
import scriptease.model.PatternModel;
import scriptease.model.PatternModelManager;

/**
 * Represents and performs the Cut command, as well as encapsulates its enabled
 * and name display state.
 * 
 * @author kschenk
 */
@SuppressWarnings("serial")
public final class CutAction extends ActiveModelSensitiveAction {
	private static final String CUT_TEXT = "Cut";

	private static final Action instance = new CutAction();

	/**
	 * Gets the sole instance of this particular type of Action
	 * 
	 * @return The sole instance of this particular type of Action
	 */
	public static Action getInstance() {
		return CutAction.instance;
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
	 * Defines a <code>CopyAction</code> object with no icon.
	 */
	private CutAction() {
		super(CutAction.CUT_TEXT);

		this.putValue(Action.MNEMONIC_KEY, KeyEvent.VK_X);
		this.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(
				KeyEvent.VK_X, InputEvent.CTRL_DOWN_MASK));
	}

	/**
	 * Cuts the passed in component to the system clipboard.
	 * 
	 * @param component
	 */
	private void cutComponent(JComponent component) {
		component.getTransferHandler().exportToClipboard(component,
				Toolkit.getDefaultToolkit().getSystemClipboard(),
				TransferHandler.MOVE);
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public void actionPerformed(ActionEvent e) {
		final Component focusOwner;

		focusOwner = SEFocusManager.getInstance().getFocus();

		if (focusOwner instanceof StoryComponentPanel) {
			// Cuts individual panels. This does cut multiple if multiple are
			// selected.
			this.cutComponent((StoryComponentPanel) focusOwner);
		} else if (focusOwner instanceof StoryComponentPanelJList) {
			// Cuts from a StoryComponentPanelJList.
			final StoryComponentPanelJList list;
			list = (StoryComponentPanelJList) focusOwner;

			for (Object selectedObject : list.getSelectedValues()) {
				this.cutComponent((StoryComponentPanel) selectedObject);
			}
		} else if (focusOwner instanceof SEGraph) {
			// Cuts the last node in an SEGraph
			final SEGraph graph;
			final JComponent selectedComponent;

			graph = (SEGraph) focusOwner;

			selectedComponent = (JComponent) graph.getNodesToComponentsMap()
					.getValue(graph.getLastSelectedNode());

			this.cutComponent(selectedComponent);
		}
	}
}
