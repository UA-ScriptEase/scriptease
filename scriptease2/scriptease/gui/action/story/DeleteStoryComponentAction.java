package scriptease.gui.action.story;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ContainerEvent;
import java.awt.event.ContainerListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowEvent;
import java.awt.event.WindowFocusListener;

import javax.swing.Action;
import javax.swing.JScrollPane;
import javax.swing.KeyStroke;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;

import scriptease.gui.SEFrame;
import scriptease.gui.action.ActiveModelSensitiveAction;
import scriptease.gui.pane.StoryPanel;
import scriptease.gui.storycomponentpanel.StoryComponentPanelManager;
import scriptease.gui.storycomponentpanel.StoryComponentPanelTree;
import scriptease.model.StoryModelPool;

/**
 * Represents and performs the Delete StoryComponent command, as well as
 * encapsulates its enabled and name display state.
 * 
 * @author remiller
 */
@SuppressWarnings("serial")
public final class DeleteStoryComponentAction extends
		ActiveModelSensitiveAction implements TreeSelectionListener,
		ContainerListener, WindowFocusListener {
	private static final String DELETE_TEXT = "Delete";

	private static final Action instance = new DeleteStoryComponentAction();

	/**
	 * Gets the sole instance of this particular type of Action
	 * 
	 * @return The sole instance of this particular type of Action
	 */
	public static Action getInstance() {
		return DeleteStoryComponentAction.instance;
	}

	/**
	 * Updates the action to either be enabled or disabled depending on the
	 * current selection.
	 */
	protected boolean isLegal() {
		final StoryComponentPanelManager manager = this
				.getActiveSelectionManager();

		return manager != null ? manager.getSelectedPanels().size() > 0 : false;
	}

	/**
	 * Defines a <code>DeleteStoryComponentAction</code> object with no icon.
	 */
	private DeleteStoryComponentAction() {
		super(DeleteStoryComponentAction.DELETE_TEXT);

		this.putValue(Action.MNEMONIC_KEY, KeyEvent.VK_D);
		this.putValue(Action.ACCELERATOR_KEY,
				KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0));
		StoryModelPool.getInstance().addPoolChangeObserver(this);

		SEFrame.getInstance().getStoryTabPane().addContainerListener(this);
	//	StoryComponentBuilder.getInstance().getLibraryTree()
		//				.addTreeSelectionListener(this);
		//StoryComponentBuilder.getInstance().getFrame().addWindowFocusListener(this);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		this.getActiveSelectionManager().deleteSelected();
	}

	@Override
	public void valueChanged(TreeSelectionEvent e) {
		// update on tree selection changes
		this.updateEnabledState();
	}

	private StoryComponentPanelManager getActiveSelectionManager() {
		final StoryPanel activeStory;
		final SEFrame mainFrame = SEFrame.getInstance();
	//	final StoryComponentEditor builder = StoryComponentEditor
	//			.getInstance();

		if (mainFrame != null && mainFrame.getFrame().isFocused()) {
			activeStory = (StoryPanel) mainFrame.getStoryTabPane()
					.getSelectedComponent();
			
			if(activeStory != null && activeStory.getTree() instanceof StoryComponentPanelTree){
				return ((StoryComponentPanelTree) activeStory.getTree()).getSelectionManager();
				
			}
			return null;
	//	} else if (builder != null && builder.getFrame().isFocused()) {
	//		return builder.getLibraryTree().getSelectionManager();
		} else
			return null;
	}

	@Override
	public void componentAdded(ContainerEvent e) {
		final Component child = e.getChild();

		if (child instanceof StoryPanel) {
			JScrollPane treePane = ((StoryPanel) child).getTree();
			if(treePane != null && treePane instanceof StoryComponentPanelTree)
				((StoryComponentPanelTree) treePane).addTreeSelectionListener(this);

			this.updateEnabledState();
		}
	}

	@Override
	public void componentRemoved(ContainerEvent e) {
		this.updateEnabledState();
	}

	@Override
	public void windowGainedFocus(WindowEvent e) {
		this.updateEnabledState();
	}

	@Override
	public void windowLostFocus(WindowEvent e) {
		this.updateEnabledState();
	}
}
