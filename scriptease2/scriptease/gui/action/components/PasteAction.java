package scriptease.gui.action.components;

import java.awt.Component;
import java.awt.Toolkit;
import java.awt.datatransfer.Transferable;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;

import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.KeyStroke;
import javax.swing.TransferHandler.TransferSupport;

import scriptease.controller.observer.SEFocusObserver;
import scriptease.gui.SEFocusManager;
import scriptease.gui.SEGraph.SEGraph;
import scriptease.gui.action.ActiveModelSensitiveAction;
import scriptease.gui.storycomponentpanel.StoryComponentPanel;

/**
 * Represents and performs the Paste command, as well as encapsulates its
 * enabled and name display state.
 * 
 * @author kschenk
 */
@SuppressWarnings("serial")
public final class PasteAction extends ActiveModelSensitiveAction {
	private static final String PASTE_TEXT = "Paste";

	private static final Action instance = new PasteAction();

	/**
	 * Gets the sole instance of this particular type of Action
	 * 
	 * @return The sole instance of this particular type of Action
	 */
	public static Action getInstance() {
		return PasteAction.instance;
	}

	/**
	 * Defines a <code>DeleteStoryComponentAction</code> object with no icon.
	 */
	private PasteAction() {
		super(PasteAction.PASTE_TEXT);

		this.putValue(Action.MNEMONIC_KEY, KeyEvent.VK_P);
		this.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(
				KeyEvent.VK_V, InputEvent.CTRL_DOWN_MASK));

		SEFocusManager.getInstance().addSEFocusObserver(new SEFocusObserver() {

			@Override
			public void gainFocus(Component oldFocus) {
				PasteAction.this.updateEnabledState();
			}

			@Override
			public void loseFocus(Component oldFocus) {
				PasteAction.this.updateEnabledState();
			}
		});
	}

	/**
	 * Updates the action to either be enabled or disabled depending on the
	 * current selection.
	 */
	protected boolean isLegal() {
		final Component focusOwner;
		final boolean isLegal;

		focusOwner = SEFocusManager.getInstance().getFocus();

		if (focusOwner instanceof StoryComponentPanel) {
			isLegal = ((StoryComponentPanel) focusOwner).getTransferHandler()
					.canImport(
							new TransferSupport(focusOwner, Toolkit
									.getDefaultToolkit().getSystemClipboard()
									.getContents(this)));

			// TODO Disabled for now because of complications with group. Should
			// handle node copying in the future.

			// } else if (focusOwner instanceof SEGraph) {
			// final SEGraph graph;
			// final JComponent selectedComponent;
			//
			// graph = (SEGraph) focusOwner;
			//
			// selectedComponent = (JComponent) graph.getNodesToComponentsMap()
			// .getValue(graph.getLastSelectedNode());
			//
			// isLegal = selectedComponent.getTransferHandler().canImport(
			// new TransferSupport(selectedComponent, Toolkit
			// .getDefaultToolkit().getSystemClipboard()
			// .getContents(this)));
		} else {
			isLegal = false;
		}

		return super.isLegal() && isLegal;
	}

	/**
	 * Pastes the passed in component to the system clipboard.
	 * 
	 * @param component
	 */
	private void pasteComponent(JComponent component) {
		final Transferable transferable = Toolkit.getDefaultToolkit()
				.getSystemClipboard().getContents(this);

		component.getTransferHandler().importData(component, transferable);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		final Component focusOwner;

		focusOwner = SEFocusManager.getInstance().getFocus();

		if (focusOwner instanceof StoryComponentPanel) {
			// Pastes the component in clip board to selected parent.
			this.pasteComponent((StoryComponentPanel) focusOwner);
		}

		else if (focusOwner instanceof SEGraph) {
			// TODO Disabled for now because of complications with group. Should
			// handle node copying in the future.

			// Paste the graph node into another component.
			final SEGraph graph;
			final JComponent selectedComponent;

			graph = (SEGraph) focusOwner;

			selectedComponent = (JComponent) graph.getNodesToComponentsMap()
					.getValue(graph.getLastSelectedNode());

			this.pasteComponent(selectedComponent);
		}

	}
}
