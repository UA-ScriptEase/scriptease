package scriptease.gui;

import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.LinkedList;
import java.util.Queue;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.GroupLayout;
import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenuBar;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.Timer;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import scriptease.ScriptEase;
import scriptease.controller.AbstractNoOpGraphNodeVisitor;
import scriptease.controller.AbstractNoOpModelVisitor;
import scriptease.controller.FileManager;
import scriptease.controller.observer.PatternModelEvent;
import scriptease.controller.observer.PatternModelObserver;
import scriptease.controller.observer.TranslatorObserver;
import scriptease.gui.SETree.ui.ScriptEaseUI;
import scriptease.gui.pane.CloseableModelTab;
import scriptease.gui.quests.QuestPoint;
import scriptease.gui.quests.QuestPointNode;
import scriptease.model.LibraryModel;
import scriptease.model.PatternModel;
import scriptease.model.PatternModelManager;
import scriptease.model.StoryModel;
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
 * @author kschenk
 */

@SuppressWarnings("serial")
public final class SEFrame implements PatternModelObserver {
	private final JTabbedPane storyTabs;
	private final JComponent middlePane;
	private JSplitPane rightSplit;
	private JSplitPane leftSplit;
	private TimedLabel statusLabel;

	private final JFrame seFrame;

	private static final SEFrame instance = new SEFrame();

	/**
	 * Gets the sole instance of SEFrame.
	 * 
	 * @return
	 */
	public static final SEFrame getInstance() {
		return SEFrame.instance;
	}

	private SEFrame() {
		this.seFrame = WindowFactory.getInstance().buildScriptEaseFrame(
				ScriptEase.TITLE);
		this.storyTabs = new JTabbedPane();
		this.middlePane = new JPanel();

		final int MIN_HEIGHT = 480;
		final int MIN_WIDTH = 640;

		this.seFrame.setMinimumSize(new Dimension(MIN_WIDTH, MIN_HEIGHT));
		this.seFrame.setExtendedState(JFrame.MAXIMIZED_BOTH);

		// Register a change listener
		this.storyTabs.addChangeListener(new ChangeListener() {
			// This method is called whenever the selected tab changes
			public void stateChanged(ChangeEvent evt) {
				final JComponent tab;

				JTabbedPane pane = (JTabbedPane) evt.getSource();
				// Get the activated frame
				tab = (JComponent) pane.getSelectedComponent();

				if (tab != null) {
					PatternModel model = PanelFactory.getInstance()
							.getModelForComponent(tab);
					PatternModelManager.getInstance().activate(model);
				}
			}
		});

		this.middlePane.setLayout(new GridLayout(1, 1));
		this.middlePane.add(storyTabs);

		this.populate();

		PatternModelManager.getInstance().addPatternModelPoolObserver(this);
	}

	/**
	 * Returns the JFrame representing ScriptEase.
	 * 
	 * @return
	 */
	public JFrame getFrame() {
		return seFrame;
	}

	/**
	 * Used by the constructor to set up and lay out the frame's internals. Also
	 * used to rebuild the frame's internals when the preferred layout changes.
	 */
	public void populate() {
		final JPanel content = new JPanel();

		final JComponent objectPane = PanelFactory.getInstance()
				.buildGameObjectPane(null);
		final JComponent statusBar = this.buildStatusBar();

		final GroupLayout layout = new GroupLayout(content);
		final String preferredLayout;

		content.setLayout(layout);

		// Get the preferred layout.
		preferredLayout = ScriptEase.getInstance().getPreference(
				ScriptEase.PREFERRED_LAYOUT_KEY);

		// Compressed Layout
		if (preferredLayout.equalsIgnoreCase(ScriptEase.COMPRESSED_LAYOUT)) {
			leftSplit = new JSplitPane(JSplitPane.VERTICAL_SPLIT, PanelFactory
					.getInstance().getMainLibraryPane(), objectPane);
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

			leftSplit = new JSplitPane(dir, PanelFactory.getInstance()
					.getMainLibraryPane(), this.middlePane);
			rightSplit = new JSplitPane(dir, leftSplit, objectPane);

			content.add(rightSplit);
			content.add(statusBar);

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

		seFrame.getContentPane().removeAll();
		seFrame.getContentPane().add(content);
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
	 * Creates a status bar.
	 * 
	 * @return
	 */
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

	/**
	 * Updates the game object pane based on the passed in model.
	 * 
	 * @param model
	 */
	private void updateGameObjectPane(StoryModel model) {
		final JPanel newGameObjectPane = PanelFactory.getInstance()
				.buildGameObjectPane(model);
		final String preferredLayout;
		preferredLayout = ScriptEase.getInstance().getPreference(
				ScriptEase.PREFERRED_LAYOUT_KEY);

		if (preferredLayout.equalsIgnoreCase(ScriptEase.COMPRESSED_LAYOUT)) {
			this.leftSplit.setBottomComponent(newGameObjectPane);
			leftSplit.setDividerLocation(this.getFrame().getHeight() / 2);
			this.leftSplit.revalidate();
		} else if (preferredLayout
				.equalsIgnoreCase(ScriptEase.UNCOMPRESSED_LAYOUT)) {
			this.rightSplit.setRightComponent(newGameObjectPane);
			this.rightSplit.revalidate();
		}
	}

	/**
	 * Hides the game object pane from view.
	 */
	private void hideGameObjectPane() {
		final JPanel newPanel;
		final String preferredLayout;
		preferredLayout = ScriptEase.getInstance().getPreference(
				ScriptEase.PREFERRED_LAYOUT_KEY);

		newPanel = new JPanel();
		newPanel.setVisible(false);

		if (preferredLayout.equalsIgnoreCase(ScriptEase.COMPRESSED_LAYOUT)) {
			this.leftSplit.setBottomComponent(newPanel);
			this.leftSplit.revalidate();
		} else if (preferredLayout
				.equalsIgnoreCase(ScriptEase.UNCOMPRESSED_LAYOUT)) {
			this.rightSplit.setRightComponent(newPanel);
			this.rightSplit.revalidate();
		}
	}

	/**
	 * Creates a tab for the given model.
	 * 
	 * @param model
	 */
	public void createTabForModel(PatternModel model) {
		final Icon icon;

		if (model.getTranslator() != null)
			icon = model.getTranslator().getIcon();
		else
			icon = null;

		model.process(new AbstractNoOpModelVisitor() {
			@Override
			public void processLibraryModel(LibraryModel libraryModel) {
				// Creates a Library Editor panel
				final JScrollPane scbScrollPane;
				final CloseableModelTab newTab;

				scbScrollPane = PanelFactory.getInstance()
						.buildLibraryEditorPanel(libraryModel);
				newTab = new CloseableModelTab(SEFrame.this.storyTabs,
						scbScrollPane, libraryModel, icon);

				scbScrollPane.getVerticalScrollBar().setUnitIncrement(
						ScriptEaseUI.VERTICAL_SCROLLBAR_INCREMENT);

				SEFrame.this.storyTabs.addTab(libraryModel.getName()
						+ "[Editor]", icon, scbScrollPane);
				SEFrame.this.storyTabs.setTabComponentAt(
						SEFrame.this.storyTabs.indexOfComponent(scbScrollPane),
						newTab);
			}

			@Override
			public void processStoryModel(final StoryModel storyModel) {
				// Creates a story editor panel with a quest graph
				storyModel.getRoot().getStartPoint()
						.process(new AbstractNoOpGraphNodeVisitor() {
							@Override
							public void processQuestPointNode(
									QuestPointNode questPointNode) {
								final QuestPoint startQuestPoint;
								final JPanel newPanel;
								final CloseableModelTab newTab;
								String modelTitle;
								final String title;

								startQuestPoint = questPointNode
										.getQuestPoint();
								newPanel = PanelFactory.getInstance()
										.buildStoryPanel(storyModel,
												startQuestPoint);
								newTab = new CloseableModelTab(
										SEFrame.this.storyTabs, newPanel,
										storyModel, icon);
								modelTitle = storyModel.getTitle();

								if (modelTitle == null || modelTitle.equals(""))
									modelTitle = "<Untitled>";

								title = modelTitle
										+ "("
										+ storyModel.getModule().getLocation()
												.getName() + ")";

								SEFrame.this.storyTabs.addTab(title, icon,
										newPanel);
								SEFrame.this.storyTabs.setTabComponentAt(
										SEFrame.this.storyTabs
												.indexOfComponent(newPanel),
										newTab);
								SEFrame.this.storyTabs
										.setSelectedComponent(newPanel);
							}
						});
			}
		});
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
	 * Removes the given StoryPanel from list of StoryTabs and the list of
	 * StoryPanels for the given model. storyTabs.remove should not be called
	 * outside of this method.
	 * 
	 * @param component
	 * @param model
	 */
	public void removeModelComponent(JComponent component, PatternModel model) {
		// remove the panel
		PanelFactory.getInstance().removeComponentForModel(model, component);

		this.storyTabs.remove(component);

		// check if there are any unsaved changes
		if (FileManager.getInstance().hasUnsavedChanges(model)) {
			// otherwise, close the StoryModel

			model.process(new AbstractNoOpModelVisitor() {
				@Override
				public void processLibraryModel(LibraryModel libraryModel) {
					// TODO Should close the translator if it's not open
					// anywhere else. We can use the usingTranslator in
					// PatternModelPool to check for this.
				};

				@Override
				public void processStoryModel(StoryModel storyModel) {
					FileManager.getInstance().close(storyModel);
				}
			});
		}
	}

	@Override
	public void modelChanged(PatternModelEvent event) {
		final short eventType = event.getEventType();
		final PatternModel model = event.getPatternModel();

		if (eventType == PatternModelEvent.PATTERN_MODEL_ACTIVATED) {

			model.process(new AbstractNoOpModelVisitor() {

				/**
				 * Sets the ScriptEase title based on the model selected.
				 */
				private void setScriptEaseTitle() {
					String newTitle = "";
					if (model != null) {
						String modelTitle = model.getTitle();
						if (!modelTitle.isEmpty())
							newTitle += modelTitle + " - ";
					}

					newTitle += ScriptEase.TITLE;

					seFrame.setTitle(newTitle);
				}

				@Override
				public void processLibraryModel(LibraryModel libraryModel) {
					// This sucks, but we need to revalidate the menu bar.
					// http://bugs.sun.com/view_bug.do?bug_id=4949810
					final JMenuBar bar = MenuFactory.createMainMenuBar(true);
					SEFrame.this.getFrame().setJMenuBar(bar);
					SEFrame.this.hideGameObjectPane();
					this.setScriptEaseTitle();
					bar.revalidate();

				}

				@Override
				public void processStoryModel(StoryModel storyModel) {
					// This sucks, but we need to revalidate the menu bar.
					// http://bugs.sun.com/view_bug.do?bug_id=4949810
					final JMenuBar bar = MenuFactory.createMainMenuBar(false);
					SEFrame.this.getFrame().setJMenuBar(bar);
					SEFrame.this.updateGameObjectPane(storyModel);
					this.setScriptEaseTitle();
					bar.revalidate();
				}
			});
		} else if (eventType == PatternModelEvent.PATTERN_MODEL_REMOVED
				&& PatternModelManager.getInstance().getActiveModel() == null) {
			this.hideGameObjectPane();
			// This sucks, but we need to revalidate the menu bar.
			// http://bugs.sun.com/view_bug.do?bug_id=4949810
			final JMenuBar bar = MenuFactory.createMainMenuBar(false);
			seFrame.setJMenuBar(bar);
			seFrame.setTitle(ScriptEase.TITLE);
			bar.revalidate();
		}
	}

	// TODO This should be in the "ScriptEase" class itself. Anything should be
	// able to send messages to it, then messages are sent back to this thing.
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
}