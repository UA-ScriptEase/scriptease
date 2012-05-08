package scriptease.gui;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.KeyboardFocusManager;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.GroupLayout;
import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTree;
import javax.swing.Timer;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;

import scriptease.ScriptEase;
import scriptease.controller.FileManager;
import scriptease.controller.observer.StoryModelPoolEvent;
import scriptease.controller.observer.StoryModelPoolObserver;
import scriptease.controller.observer.TranslatorObserver;
import scriptease.gui.pane.CloseableTab;
import scriptease.gui.pane.DefaultPatternPaneFactory;
import scriptease.gui.pane.LibraryPane;
import scriptease.gui.pane.PaneFactory;
import scriptease.gui.pane.StoryPanel;
import scriptease.model.StoryComponent;
import scriptease.model.StoryModel;
import scriptease.model.StoryModelPool;
import scriptease.translator.Translator;
import scriptease.translator.TranslatorManager;

/**
 * The main application frame. Contains menu bar, dynamic Property Pane and a
 * workspace for all of the models open. <br>
 * <br>
 * SEFrame is a singleton since we should only ever have one main frame at a
 * time.
 * 
 * @author remiller
 * @author mfchurch
 */
@SuppressWarnings("serial")
public final class SEFrame extends JFrame implements StoryModelPoolObserver {
	private static final int MIN_HEIGHT = 480;
	private static final int MIN_WIDTH = 640;
	private static final int PREFERRED_PROPERTIES_THICKNESS = 250;

	private final JTabbedPane storyTabs;
	private final JComponent middlePane;
	private JSplitPane rightSplit;
	private JSplitPane leftSplit;
	private TimedLabel statusLabel;

	public static String preferredLayout;

	private SEFrame() {
		super(ScriptEase.TITLE);

		this.setMinimumSize(new Dimension(SEFrame.MIN_WIDTH, SEFrame.MIN_HEIGHT));
		this.setExtendedState(JFrame.MAXIMIZED_BOTH);

		this.storyTabs = new JTabbedPane();
		// Register a change listener
		this.storyTabs.addChangeListener(new ChangeListener() {
			// This method is called whenever the selected tab changes
			public void stateChanged(ChangeEvent evt) {
				final StoryPanel frame;

				JTabbedPane pane = (JTabbedPane) evt.getSource();
				// Get the activated frame
				frame = (StoryPanel) pane.getSelectedComponent();

				StoryModel model = (frame == null ? null : frame.getModel());
				StoryModelPool.getInstance().activate(model);
			}
		});

		// if (ScriptEase.DEBUG_MODE) {
		// this.middlePane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, true);
		// ((JSplitPane) this.middlePane).setTopComponent(this.storyPane);
		// ((JSplitPane) this.middlePane).setResizeWeight(1.0);
		// } else {
		this.middlePane = new JPanel();
		this.middlePane.setLayout(new GridLayout(1, 1));
		this.middlePane.add(storyTabs);

		this.populate();

		StoryModelPool.getInstance().addPoolChangeObserver(this);
	}

	/**
	 * Sets the Status Label to the given message
	 * 
	 * @param message
	 *            The message to display.
	 */
	public void setStatus(final String message) {
		if (this.statusLabel != null) {
			this.statusLabel.queueText(message);
		} else
			System.err
					.println("Tried to set the message of a null status label");
	}

	/**
	 * Used by the constructor to set up and lay out the frame's internals. Also
	 * used to rebuild the frame's internals when the preferred layout changes.
	 */
	public void populate() {
		final JPanel content = new JPanel();

		final JComponent libraryPane = new LibraryPane();
		final JComponent objectPane = PaneFactory.buildGameObjectPane(null);
		final JComponent statusBar = this.buildStatusBar();

		// this.updatePropertiesPane();

		final GroupLayout layout = new GroupLayout(content);
		content.setLayout(layout);

		// Get the preferred layout.
		preferredLayout = ScriptEase.getInstance().getPreference(
				ScriptEase.PREFERRED_LAYOUT_KEY);

		// Compressed Layout
		if (preferredLayout.equalsIgnoreCase(ScriptEase.COMPRESSED_LAYOUT)) {
			leftSplit = new JSplitPane(JSplitPane.VERTICAL_SPLIT, libraryPane,
					objectPane);
			rightSplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, leftSplit,
					this.middlePane);

			content.add(rightSplit);
			content.add(statusBar);

			leftSplit.setResizeWeight(0.5);

			layout.setHorizontalGroup(layout.createParallelGroup()
					.addComponent(rightSplit).addComponent(statusBar));

			layout.setVerticalGroup(layout
					.createSequentialGroup()
					.addComponent(rightSplit)
					.addComponent(statusBar, GroupLayout.PREFERRED_SIZE,
							GroupLayout.PREFERRED_SIZE,
							GroupLayout.PREFERRED_SIZE));
		} else
		// Uncompressed Layout.
		if (preferredLayout.equalsIgnoreCase(ScriptEase.UNCOMPRESSED_LAYOUT)) {
			final int dir = JSplitPane.HORIZONTAL_SPLIT;

			leftSplit = new JSplitPane(dir, libraryPane, this.middlePane);
			rightSplit = new JSplitPane(dir, leftSplit, objectPane);

			content.add(rightSplit);
			content.add(statusBar);

			leftSplit.setResizeWeight(0.5);

			// stretch the split panels
			layout.setHorizontalGroup(layout.createParallelGroup()
					.addComponent(rightSplit).addComponent(statusBar));

			// status label is at the bottom
			layout.setVerticalGroup(layout
					.createSequentialGroup()
					.addComponent(rightSplit)
					.addComponent(statusBar, GroupLayout.PREFERRED_SIZE,
							GroupLayout.PREFERRED_SIZE,
							GroupLayout.PREFERRED_SIZE));

		}

		this.getContentPane().removeAll();
		this.getContentPane().add(content);
	}

	/**
	 * JLabel with a TranslatorObserver since we cannot use WeakReferences
	 * 
	 * @author mfchurch
	 * 
	 */
	private class GameLabel extends JLabel implements TranslatorObserver {
		private final static String noTransText = "-None-";

		public GameLabel() {
			super(noTransText);

			TranslatorManager.getInstance().addTranslatorObserver(this);
		}

		@Override
		public void translatorLoaded(Translator newTranslator) {
			if (newTranslator != null) {
				this.setText(newTranslator.getName());
				this.setEnabled(true);
				this.setIcon(newTranslator.getIcon());
			} else {
				this.setText(noTransText);
				this.setEnabled(false);
				this.setIcon(null);
			}
		}
	}

	private JComponent buildStatusBar() {
		final Box statusBar = Box.createHorizontalBox();
		final JLabel currentTranslatorLabel;
		final JLabel currentTranslatorNameLabel;
		final String transPrefix = "Game: ";

		currentTranslatorLabel = new JLabel(transPrefix);
		currentTranslatorNameLabel = new GameLabel();

		currentTranslatorNameLabel.setEnabled(false);
		currentTranslatorNameLabel.setBorder(BorderFactory.createEmptyBorder(0,
				5, 0, 5));

		this.statusLabel = new TimedLabel(1000, 3500);
		statusBar.add(this.statusLabel);
		statusBar.add(Box.createGlue());
		statusBar.add(currentTranslatorLabel);
		statusBar.add(currentTranslatorNameLabel);

		statusBar.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));

		return statusBar;
	}

	private static final SEFrame instance = new SEFrame();

	/**
	 * Gets the sole instance of SEFrame.
	 * 
	 * @return
	 */
	public static final SEFrame getInstance() {
		return SEFrame.instance;
	}

	// Overridden to provide custom exiting behavior.
	@Override
	protected void processWindowEvent(WindowEvent e) {
		if (e.getID() == WindowEvent.WINDOW_CLOSING)
			ScriptEase.getInstance().exit();
		else
			super.processWindowEvent(e);
	}

	private void updateGameObjectPane(StoryModel model) {
		final JPanel newGameObjectPane = PaneFactory.buildGameObjectPane(model);

		if (preferredLayout.equalsIgnoreCase(ScriptEase.COMPRESSED_LAYOUT)) {
			this.leftSplit.setBottomComponent(newGameObjectPane);
			this.leftSplit.revalidate();
		} else if (preferredLayout
				.equalsIgnoreCase(ScriptEase.UNCOMPRESSED_LAYOUT)) {
			this.rightSplit.setRightComponent(newGameObjectPane);
			this.rightSplit.revalidate();
		}
	}

	/**
	 * Determines the selected StoryComponent in the internal frame with focus
	 * and configures the properties pane to display its properties. If there is
	 * no selection or no frames with focus, then an empty properties pane is
	 * displayed.
	 */
	public void updatePropertiesPane() {
		if (!ScriptEase.DEBUG_MODE)
			return;
		final List<StoryComponent> selection = this.getSelection();
		final StoryModel activeModel = StoryModelPool.getInstance()
				.getActiveModel();

		final JPanel newProps;
		final DefaultPatternPaneFactory factory;
		final StoryComponent firstSelection;

		if ((selection.size() != 1) || (activeModel == null)
				|| ((firstSelection = selection.get(0)) == null)) {
			newProps = this.buildEmptyPropertiesPanel();
		} else {
			factory = new DefaultPatternPaneFactory();
			newProps = factory.buildPane(firstSelection);
		}

		newProps.setPreferredSize(new Dimension(PREFERRED_PROPERTIES_THICKNESS,
				PREFERRED_PROPERTIES_THICKNESS));
		((JSplitPane) this.middlePane).setBottomComponent(newProps);
		this.validate();
	}

	/**
	 * Get the component in focus from the KeyboardFocusManager, can return null
	 * 
	 * @return
	 */
	public Component getSelectedComponent() {
		KeyboardFocusManager focusManager = KeyboardFocusManager
				.getCurrentKeyboardFocusManager();
		Component focusedComp = focusManager.getFocusOwner();
		return focusedComp;
	}

	/**
	 * Determines the current StoryComponent that is selected. If there are no
	 * model frames open or there isn't a selected <code>StoryComponent</code>
	 * in the currently active frame, then an empty list is returned.
	 * 
	 * @return A list of the currently selected StoryComponents. The list is
	 *         empty if there is nothing selected.
	 */
	public List<StoryComponent> getSelection() {
		List<StoryComponent> selection = new ArrayList<StoryComponent>();
		Component focusOwner = getSelectedComponent();

		if (focusOwner != null && focusOwner instanceof JTree) {
			JTree tree = (JTree) focusOwner;
			selection = getTreeSelection(tree);
		}

		return selection;
	}

	public List<StoryComponent> getTreeSelection(JTree tree) {
		final List<StoryComponent> selection = new ArrayList<StoryComponent>();
		final TreePath[] selectionPaths = tree.getSelectionPaths();

		if (selectionPaths != null)
			for (TreePath selectionPath : selectionPaths) {
				// Added instanceof check since this has/may throw a class cast
				// exception
				Object userObject = ((DefaultMutableTreeNode) selectionPath
						.getLastPathComponent()).getUserObject();
				if (userObject instanceof StoryComponent)
					selection.add((StoryComponent) userObject);
			}

		return selection;
	}

	private JPanel buildEmptyPropertiesPanel() {
		final JLabel emptyPropertiesLabel = new JLabel(
				" - Select a single pattern to see its properties - ");
		final JPanel propertyPane = new JPanel();

		emptyPropertiesLabel.setEnabled(false);
		propertyPane.add(emptyPropertiesLabel);

		return propertyPane;
	}

	/**
	 * Creates a tab for the given StoryModel defaulting to the starting
	 * QuestPoint as editing
	 * 
	 * @param model
	 */
	public void createTabForModel(StoryModel model) {
		final Icon icon = model.getTranslator().getIcon();

		final StoryPanel newPanel = new StoryPanel(model);
		final CloseableTab newTab = new CloseableTab(this.storyTabs, newPanel,
				icon);

		this.storyTabs.addTab(newPanel.getTitle(), icon, newPanel);
		this.storyTabs.setTabComponentAt(
				this.storyTabs.indexOfComponent(newPanel), newTab);
		this.storyTabs.setSelectedComponent(newPanel);

	}

	/**
	 * Gets the tab pane for displaying all Stories;
	 * 
	 * @return The tab pane for the Stories;
	 */
	public JTabbedPane getStoryTabPane() {
		return this.storyTabs;
	}

	/**
	 * @param model
	 *            The StoryModel whose storyPanel is to be removed.
	 */
	public void removeStoryPanelsForModel(final StoryModel model) {
		for (int i = 0; i < this.storyTabs.getTabCount(); i++) {
			StoryPanel panel = (StoryPanel) this.storyTabs.getComponentAt(i);
			if (panel.represents(model))
				this.removeStoryPanelTab(panel);
		}
	}

	/**
	 * Removes the given StoryPanel from list of StoryTabs and the list of
	 * StoryPanels for the given model. storyTabs.remove should not be called
	 * outside of this method.
	 * 
	 * @param panel
	 * @param model
	 */
	public void removeStoryPanelTab(StoryPanel panel) {
		final StoryModel model = panel.getModel();

		// remove the panel
		this.storyTabs.remove(panel);
		StoryPanel.removeStoryPanelForModel(model, panel);

		// check if there are any other tabs for the same model
		if (!this.containsTabForModel(model)
				&& FileManager.getInstance().hasUnsavedChanges(model)) {
			// otherwise, close the StoryModel
			FileManager.getInstance().close(model);
		}
	}

	/**
	 * Checks if there exists any StoryPanel tabs which represent the given
	 * model
	 * 
	 * @param model
	 * @return
	 */
	private boolean containsTabForModel(StoryModel model) {
		for (Component panel : this.storyTabs.getComponents()) {
			if (panel instanceof StoryPanel
					&& ((StoryPanel) panel).represents(model)) {
				return true;
			}
		}
		return false;
	}

	@Override
	public void modelChanged(StoryModelPoolEvent event) {
		final short eventType = event.getEventType();
		final StoryModel model = event.getStoryModel();

		if (eventType == StoryModelPoolEvent.STORY_MODEL_ACTIVATED) {
			this.updateGameObjectPane(model);

			// Update the frame title
			String newTitle = "";
			if (model != null) {
				String modelTitle = model.getTitle();
				if (!modelTitle.isEmpty())
					newTitle += modelTitle + " - ";
			}

			newTitle += ScriptEase.TITLE;

			this.setTitle(newTitle);
		}
	}

	private class TimedLabel extends JLabel {
		private Queue<String> messages;
		final Timer textQueue;
		final Timer textClear;

		public TimedLabel(int QueueTimer, int ClearTimer) {
			super();
			messages = new LinkedList<String>();
			textQueue = new Timer(QueueTimer, new ActionListener() {
				public void actionPerformed(ActionEvent arg0) {
					if (!messages.isEmpty())
						setText(messages.poll());
				};
			});
			textClear = new Timer(ClearTimer, new ActionListener() {
				public void actionPerformed(ActionEvent arg0) {
					TimedLabel.super.setText("");
					messages.clear();
				};
			});

			textQueue.setRepeats(false);
			textClear.setRepeats(false);
		}

		public void setText(String text) {
			super.setText(text);
			if (textQueue != null)
				textQueue.restart();
			if (textClear != null)
				textClear.restart();
		};

		public void queueText(String text) {
			messages.add(text);
			if (this.getText().isEmpty())
				setText(text);
		}
	}

	public StoryPanel getActiveTab() {
		final int selectedIndex = this.storyTabs.getSelectedIndex();
		final StoryModel activeModel;

		// assumes all tabs contain story panes as added in createTabForModel
		StoryPanel storyPanel = (StoryPanel) this.storyTabs
				.getComponentAt(selectedIndex);
		activeModel = storyPanel.getModel();

		// this is just a sanity check.
		if (activeModel != StoryModelPool.getInstance().getActiveModel())
			throw new IllegalStateException(
					"Active tab is not representing active model");

		return storyPanel;
	}
}