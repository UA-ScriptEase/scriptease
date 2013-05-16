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

import scriptease.controller.observer.SEFocusObserver;
import scriptease.gui.SEFocusManager;
import scriptease.gui.SEGraph.SEGraph;
import scriptease.gui.action.ActiveModelSensitiveAction;
import scriptease.gui.storycomponentpanel.StoryComponentPanel;
import scriptease.gui.storycomponentpanel.StoryComponentPanelJList;
import scriptease.model.SEModel;
import scriptease.model.SEModelManager;

/**
 * Represents and performs the Copy command, as well as encapsulates its enabled
 * and name display state.
 * 
 * @author kschenk
 */
@SuppressWarnings("serial")
public final class CopyAction extends ActiveModelSensitiveAction implements
		SEFocusObserver {
	private static final String COPY_TEXT = "Copy";

	private static final Action instance = new CopyAction();

	/**
	 * Gets the sole instance of this particular type of Action
	 * 
	 * @return The sole instance of this particular type of Action
	 */
	public static Action getInstance() {
		return CopyAction.instance;
	}

	/**
	 * Defines a <code>CopyAction</code> object with no icon.
	 */
	private CopyAction() {
		super(CopyAction.COPY_TEXT);

		this.putValue(Action.MNEMONIC_KEY, KeyEvent.VK_C);
		this.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(
				KeyEvent.VK_C, InputEvent.CTRL_DOWN_MASK));

		SEFocusManager.getInstance().addSEFocusObserver(this);
	}

	/**
	 * Updates the action to either be enabled or disabled depending on the
	 * current selection.
	 */
	@Override
	protected boolean isLegal() {
		final SEModel activeModel;
		final Component focusOwner;
		final boolean isLegal;

		focusOwner = SEFocusManager.getInstance().getFocus();
		activeModel = SEModelManager.getInstance().getActiveModel();

		if (focusOwner instanceof StoryComponentPanel
				|| focusOwner instanceof StoryComponentPanelJList
				|| focusOwner instanceof SEGraph) {
			isLegal = true;
		} else
			isLegal = false;

		return activeModel != null && isLegal;
	}

	@Override
	public void gainFocus(Component oldFocus) {
		this.updateEnabledState();
	}

	@Override
	public void loseFocus(Component oldFocus) {
		this.updateEnabledState();
	}

	/**
	 * Copies the passed in component to the system clipboard.
	 * 
	 * @param component
	 */
	private void copyComponent(JComponent component) {
		component.getTransferHandler().exportToClipboard(component,
				Toolkit.getDefaultToolkit().getSystemClipboard(),
				TransferHandler.COPY);
	}

	@SuppressWarnings({ "rawtypes", "unchecked", "deprecation" })
	@Override
	public void actionPerformed(ActionEvent e) {
		final Component focusOwner;

		focusOwner = SEFocusManager.getInstance().getFocus();

		if (focusOwner instanceof StoryComponentPanel) {
			// Copies individual panels. This does copy multiple if multiple are
			// selected.
			this.copyComponent((StoryComponentPanel) focusOwner);
		} else if (focusOwner instanceof StoryComponentPanelJList) {
			// Copies from a StoryComponentPanelJList.
			final StoryComponentPanelJList list;
			list = (StoryComponentPanelJList) focusOwner;

			for (Object selectedObject : list.getSelectedValues()) {
				this.copyComponent((StoryComponentPanel) selectedObject);
			}
		} else if (focusOwner instanceof SEGraph) {
			// Copy the last node in an SEGraph
			final SEGraph graph;
			final JComponent selectedComponent;

			graph = (SEGraph) focusOwner;

			selectedComponent = (JComponent) graph.getNodesToComponentsMap()
					.getValue(graph.getLastSelectedNode());

			this.copyComponent(selectedComponent);
		}
	}
}
