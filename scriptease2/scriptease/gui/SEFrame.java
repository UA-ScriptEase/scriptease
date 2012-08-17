package scriptease.gui;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
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
import javax.swing.Timer;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import scriptease.ScriptEase;
import scriptease.controller.AbstractNoOpGraphNodeVisitor;
import scriptease.controller.AbstractNoOpModelVisitor;
import scriptease.controller.FileManager;
import scriptease.controller.observer.PatternModelPoolEvent;
import scriptease.controller.observer.PatternModelPoolObserver;
import scriptease.controller.observer.TranslatorObserver;
import scriptease.gui.pane.CloseableTab;
import scriptease.gui.quests.QuestPoint;
import scriptease.gui.quests.QuestPointNode;
import scriptease.gui.storycomponentbuilder.StoryComponentBuilderPanelFactory;
import scriptease.model.LibraryModel;
import scriptease.model.PatternModel;
import scriptease.model.StoryModel;
import scriptease.model.PatternModelPool;
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
public final class SEFrame implements PatternModelPoolObserver {
	private static final int MIN_HEIGHT = 480;
	private static final int MIN_WIDTH = 640;

	private final JTabbedPane storyTabs;
	private final JComponent middlePane;
	private JSplitPane rightSplit;
	private JSplitPane leftSplit;
	private TimedLabel statusLabel;
	private QuestPoint startQuestPoint;

	private final JFrame seFrame;

	public static String preferredLayout;

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
		seFrame = WindowManager.getInstance().buildScriptEaseFrame(
				ScriptEase.TITLE);

		seFrame.setMinimumSize(new Dimension(SEFrame.MIN_WIDTH,
				SEFrame.MIN_HEIGHT));
		seFrame.setExtendedState(JFrame.MAXIMIZED_BOTH);

		this.storyTabs = new JTabbedPane();

		// Register a change listener
		this.storyTabs.addChangeListener(new ChangeListener() {
			// This method is called whenever the selected tab changes
			public void stateChanged(ChangeEvent evt) {
				final JPanel frame;

				JTabbedPane pane = (JTabbedPane) evt.getSource();
				// Get the activated frame
				frame = (JPanel) pane.getSelectedComponent();

				if (frame != null) {
					PatternModel model = PanelFactory.getInstance()
							.getModelForPanel(frame);
					PatternModelPool.getInstance().activate(model);
				}
			}
		});

		this.middlePane = new JPanel();
		this.middlePane.setLayout(new GridLayout(1, 1));
		this.middlePane.add(storyTabs);

		this.populate();

		PatternModelPool.getInstance().addPoolChangeObserver(this);
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

		final JComponent objectPane = PanelFactory.getInstance()
				.buildGameObjectPane(null);
		final JComponent statusBar = this.buildStatusBar();

		final GroupLayout layout = new GroupLayout(content);
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

		seFrame.getContentPane().removeAll();
		seFrame.getContentPane().add(content);
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

	private void updateGameObjectPane(StoryModel model) {
		final JPanel newGameObjectPane = PanelFactory.getInstance()
				.buildGameObjectPane(model);

		if (preferredLayout.equalsIgnoreCase(ScriptEase.COMPRESSED_LAYOUT)) {
			this.leftSplit.setBottomComponent(newGameObjectPane);
			this.leftSplit.revalidate();
		} else if (preferredLayout
				.equalsIgnoreCase(ScriptEase.UNCOMPRESSED_LAYOUT)) {
			this.rightSplit.setRightComponent(newGameObjectPane);
			this.rightSplit.revalidate();
		}
	}

	// TODO May not be necessary. We might be able to just combine both into one
	// somehow
	public void createTabForModel(PatternModel model) {
		if (model instanceof StoryModel)
			createTabForModel((StoryModel) model);
		else if (model instanceof LibraryModel)
			createTabForModel((LibraryModel) model);
	}

	public void createTabForModel(LibraryModel model) {
		final Icon icon;

		if (model.getTranslator() != null)
			icon = model.getTranslator().getIcon();
		else
			icon = null;

		final JPanel scbPanel;
		final JComponent editingPane;

		scbPanel = new JPanel();

		editingPane = StoryComponentBuilderPanelFactory.getInstance()
				.buildStoryComponentEditorComponent(
						PanelFactory.getInstance().getMainLibraryPane());

		scbPanel.add(editingPane);

		this.storyTabs.addTab(model.getName(), icon, scbPanel);
		this.storyTabs.setSelectedComponent(scbPanel);
	}

	/**
	 * Creates a tab for the given StoryModel defaulting to the starting
	 * QuestPoint as editing
	 * 
	 * @param model
	 */
	public void createTabForModel(StoryModel model) {
		final Icon icon = model.getTranslator().getIcon();

		model.getRoot().getStartPoint()
				.process(new AbstractNoOpGraphNodeVisitor() {
					@Override
					public void processQuestPointNode(
							QuestPointNode questPointNode) {

						startQuestPoint = questPointNode.getQuestPoint();
					}
				});

		System.out.println("Start Quest Point is: "
				+ startQuestPoint.toString());

		if (startQuestPoint != null) {
			final JPanel newPanel = PanelFactory.getInstance().buildStoryPanel(
					model, startQuestPoint);
			final CloseableTab newTab = new CloseableTab(this.storyTabs, model,
					icon);

			// TODO Get the actual title!
			String temporaryTitleString = "TemporaryTitleString";
			/*
			 * Get Title Method
			 * 
			 * final StoryModel model = this.model; final String title;
			 * 
			 * String modelTitle = model.getTitle(); if (modelTitle == null ||
			 * modelTitle.equals("")) modelTitle = "<Untitled>";
			 * 
			 * title = modelTitle + "(" +
			 * model.getModule().getLocation().getName() + ")";
			 * 
			 * return title;
			 */

			this.storyTabs.addTab(temporaryTitleString, icon, newPanel);
			this.storyTabs.setTabComponentAt(
					this.storyTabs.indexOfComponent(newPanel), newTab);
			this.storyTabs.setSelectedComponent(newPanel);
		}
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
			this.removeStoryPanelTab(model);
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
	public void removeStoryPanelTab(PatternModel model) {
		final List<JPanel> panels = PanelFactory.getInstance()
				.getPanelsForModel(model);

		// remove the panel
		for (JPanel panel : panels) {
			this.storyTabs.remove(panel);
			PanelFactory.getInstance().removeStoryPanelForModel(model, panel);
		}
		// check if there are any unsaved changes
		if (FileManager.getInstance().hasUnsavedChanges(model)) {
			// otherwise, close the StoryModel

			model.process(new AbstractNoOpModelVisitor() {
				@Override
				public void processLibraryModel(LibraryModel libraryModel) {
					// TODO Should close the translator if it's not open
					// anywhere else.
					// We can use the usingTranslator in PatternModelPool to
					// check for this.
				};

				@Override
				public void processStoryModel(StoryModel storyModel) {
					FileManager.getInstance().close(storyModel);
				}
			});
		}
	}

	@Override
	public void modelChanged(PatternModelPoolEvent event) {
		final short eventType = event.getEventType();
		final PatternModel model = event.getPatternModel();

		if (eventType == PatternModelPoolEvent.PATTERN_MODEL_ACTIVATED) {

			model.process(new AbstractNoOpModelVisitor() {
				@Override
				public void processLibraryModel(LibraryModel libraryModel) {
					// TODO Do stuff when model changed to a librarymodel
				}

				@Override
				public void processStoryModel(StoryModel storyModel) {
					SEFrame.this.updateGameObjectPane(storyModel);

					// Update the frame title
					String newTitle = "";
					if (model != null) {
						String modelTitle = model.getTitle();
						if (!modelTitle.isEmpty())
							newTitle += modelTitle + " - ";
					}

					newTitle += ScriptEase.TITLE;

					seFrame.setTitle(newTitle);
				}
			});
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

	/**
	 * Returns the active model.
	 * 
	 * TODO This should be in ModelPool class, really. Not sure why it's here.
	 * XXX This used to return a storypanel, so that's why things are weird.
	 * 
	 * 
	 * @return
	 */
	public PatternModel getActiveModel() {
		final int selectedIndex;
		final JPanel modelPanel;
		final PatternModel activeModel;

		selectedIndex = this.storyTabs.getSelectedIndex();
		modelPanel = (JPanel) this.storyTabs.getComponentAt(selectedIndex);
		activeModel = PanelFactory.getInstance().getModelForPanel(modelPanel);

		// this is just a sanity check.
		if (activeModel != PatternModelPool.getInstance().getActiveModel())
			throw new IllegalStateException(
					"Active tab is not representing active model");

		return activeModel;
	}
	/*
	 * TODO IS THIS NECESSARY?
	 *//**
	 * Activates the StoryPanel for the given model and questPoint. If it's
	 * not found, does nothing.
	 * 
	 * @param questPoint
	 * @param model
	 */
	/*
	 * public void activatePanelForQuestPoint(StoryModel model, QuestPoint
	 * questPoint) { //TODO IMplement this again model.setTree(questPoint); }
	 */
}