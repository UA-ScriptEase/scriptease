package scriptease.gui.action.components;

import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;

import javax.swing.Action;
import javax.swing.JLabel;
import javax.swing.KeyStroke;

import scriptease.controller.observer.SEFocusObserver;
import scriptease.controller.undo.UndoManager;
import scriptease.gui.SEFocusManager;
import scriptease.gui.SEGraph.SEGraph;
import scriptease.gui.action.ActiveModelSensitiveAction;
import scriptease.gui.component.ScriptWidgetFactory;
import scriptease.gui.pane.DescribeItPanel;
import scriptease.gui.storycomponentpanel.StoryComponentPanel;
import scriptease.gui.storycomponentpanel.StoryComponentPanelJList;
import scriptease.gui.ui.ScriptEaseUI;
import scriptease.model.StoryComponent;
import scriptease.model.complex.StoryPoint;
import scriptease.model.semodel.SEModel;
import scriptease.model.semodel.SEModelManager;
import scriptease.model.semodel.librarymodel.LibraryModel;

/**
 * Represents and performs a disable action on a story component. Disabling a
 * story component is equivalent to commenting it out in code.
 * 
 * @author jyuen
 */
@SuppressWarnings("serial")
public class DisableAction extends ActiveModelSensitiveAction implements
		SEFocusObserver {

	private static final String DISABLE_TEXT = "Disable";

	private static final Action instance = new DisableAction();

	/**
	 * Gets the sole instance of this particular type of Action
	 * 
	 * @return The sole instance of this particular type of Action
	 */
	public static Action getInstance() {
		return DisableAction.instance;
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

		if (focusOwner instanceof StoryComponentPanel) {
			isLegal = ((StoryComponentPanel) focusOwner).isRemovable();
		} else if (focusOwner instanceof StoryComponentPanelJList) {
			isLegal = SEModelManager.getInstance().getActiveModel() instanceof LibraryModel;
		} else if (focusOwner instanceof SEGraph) {
			isLegal = !((SEGraph<?>) focusOwner).isReadOnly();
		} else
			isLegal = false;

		return activeModel != null && isLegal;
	}

	/**
	 * Defines a <code>CopyAction</code> object with no icon.
	 */
	private DisableAction() {
		super(DisableAction.DISABLE_TEXT);

		this.putValue(Action.MNEMONIC_KEY, KeyEvent.VK_E);
		this.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(
				KeyEvent.VK_E, InputEvent.CTRL_DOWN_MASK));

		SEFocusManager.getInstance().addSEFocusObserver(this);
	}

	/**
	 * Disables <code>components</code> visually by graying out the bindings,
	 * text, etc. Enables them again if they are already disabled.
	 * 
	 * Also signifies to code gen to add comments to the piece of code.
	 * 
	 * @param component
	 */
	public void disableComponent(StoryComponentPanel componentPanel) {
		final StoryComponent component = componentPanel.getStoryComponent();

		final boolean enabled = component.isEnabled();

		// Don't want to be enabling the component if it's owner is disabled.
		if (component.getOwner() != null
				&& !(component.getOwner() instanceof StoryPoint)
				&& !component.getOwner().isEnabled())
			return;

		component.setEnabled(!enabled);

		final Component[] components = componentPanel.getLayout()
				.getMainPanel().getComponents();
		
		boolean disableLabelEncountered = true;
		
		// TODO this is gross, need to clean up later.
//		for (Component comp : components) {
//			if (comp instanceof JLabel) {
//				final JLabel label = (JLabel) comp;
//
//				if (label.getText().equals("Disabled")) {
//					if (component.isEnabled())
//						label.setBackground(ScriptWidgetFactory.LABEL_BACKGROUND_COLOUR);
//					else
//						label.setBackground(ScriptEaseUI.COLOUR_DISABLED);
//
//					disableLabelEncountered = true;
//					
//				} else if (disableLabelEncountered){
//					if (component.isEnabled())
//						label.setForeground(Color.black);
//					else
//						label.setForeground(Color.red);
//				}
//				
//				label.repaint();
//			} else if (comp instanceof DescribeItPanel) {
//				final DescribeItPanel describeItPanel = (DescribeItPanel) comp;
//
//				final Component[] describeItPanels = describeItPanel
//						.getScriptItPanel().getComponents();
//				for (Component panel : describeItPanels) {
//					if (panel instanceof JLabel) {
//						final JLabel label = (JLabel) panel;
//						if (!component.isEnabled())
//							label.setForeground(Color.red);
//						else
//							label.setForeground(Color.black);
//
//						label.repaint();
//					}
//				}
//
//			}
//		}

		if (!(component instanceof StoryPoint)) {
			for (StoryComponentPanel childPanel : componentPanel
					.getDescendants()) {
				childPanel.getStoryComponent().setEnabled(!enabled);
				childPanel.repaint();
			}
		}

		componentPanel.repaint();
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		final Component focusOwner;

		focusOwner = SEFocusManager.getInstance().getFocus();

		if (focusOwner instanceof StoryComponentPanel) {
			if (!UndoManager.getInstance().hasOpenUndoableAction())
				UndoManager.getInstance().startUndoableAction("Disable");

			this.disableComponent((StoryComponentPanel) focusOwner);

			if (UndoManager.getInstance().hasOpenUndoableAction())
				UndoManager.getInstance().endUndoableAction();
		}
	}

	@Override
	public void gainFocus(Component oldFocus) {
		this.updateEnabledState();
	}

	@Override
	public void loseFocus(Component oldFocus) {
		this.updateEnabledState();
	}
}
