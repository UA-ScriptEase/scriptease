package scriptease.gui.action.story;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ContainerEvent;
import java.awt.event.ContainerListener;
import java.awt.event.KeyEvent;

import javax.swing.Action;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.KeyStroke;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;

import scriptease.gui.PanelFactory;
import scriptease.gui.SEFrame;
import scriptease.gui.action.ActiveModelSensitiveAction;
import scriptease.gui.storycomponentpanel.StoryComponentPanelManager;
import scriptease.gui.storycomponentpanel.StoryComponentPanelTree;
import scriptease.model.PatternModel;
import scriptease.model.PatternModelManager;
import scriptease.model.StoryModel;

/**
 * Represents and performs the Delete StoryComponent command, as well as
 * encapsulates its enabled and name display state.
 * 
 * @author remiller
 */
@SuppressWarnings("serial")
public final class DeleteStoryComponentAction extends
		ActiveModelSensitiveAction implements TreeSelectionListener,
		ContainerListener {
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
		PatternModelManager.getInstance().addPatternModelPoolObserver(this);

		SEFrame.getInstance().getStoryTabPane().addContainerListener(this);
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
		final JPanel activeStory;
		final SEFrame mainFrame = SEFrame.getInstance();

		if (mainFrame != null && mainFrame.getFrame().isFocused()) {
			if (mainFrame.getStoryTabPane().getSelectedComponent() instanceof JPanel) {
				activeStory = (JPanel) mainFrame.getStoryTabPane()
						.getSelectedComponent();

				final PatternModel model;
				model = PanelFactory.getInstance()
						.getModelForComponent(activeStory);

				if (activeStory != null && model instanceof StoryModel) {
					return PanelFactory.getInstance()
							.getTreeForComponent(activeStory).getSelectionManager();

				}
			}
		}
		return null;
	}

	@Override
	public void componentAdded(ContainerEvent e) {
		final Component child = e.getChild();

		if (child instanceof JPanel) {
			JScrollPane treePane = PanelFactory.getInstance().getTreeForComponent(
					(JPanel) child);

			if (treePane != null && treePane instanceof StoryComponentPanelTree)
				((StoryComponentPanelTree) treePane)
						.addTreeSelectionListener(this);

			this.updateEnabledState();
		}
	}

	@Override
	public void componentRemoved(ContainerEvent e) {
		this.updateEnabledState();
	}
}
