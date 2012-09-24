package scriptease.gui;

import java.awt.Dimension;
import java.awt.Frame;
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
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import scriptease.ScriptEase;
import scriptease.controller.FileManager;
import scriptease.controller.ModelAdapter;
import scriptease.controller.observer.PatternModelEvent;
import scriptease.controller.observer.PatternModelObserver;
import scriptease.controller.observer.TranslatorObserver;
import scriptease.gui.pane.CloseableModelTab;
import scriptease.gui.ui.ScriptEaseUI;
import scriptease.model.LibraryModel;
import scriptease.model.PatternModel;
import scriptease.model.PatternModelManager;
import scriptease.model.StoryModel;
import scriptease.model.complex.StoryPoint;
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
	private final JTabbedPane modelTabs;
	private final JComponent middlePane;
	private final JSplitPane middleSplit;
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
		this.modelTabs = new JTabbedPane();
		this.middlePane = new JPanel();
		this.middleSplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);

		final int MIN_HEIGHT = 480;
		final int MIN_WIDTH = 640;

		this.seFrame.setMinimumSize(new Dimension(MIN_WIDTH, MIN_HEIGHT));
		this.seFrame.setExtendedState(Frame.MAXIMIZED_BOTH);

		// Register a change listener
		this.modelTabs.addChangeListener(new ChangeListener() {
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
		this.middlePane.add(this.modelTabs);

		this.populate();

		PatternModelManager.getInstance().addPatternModelObserver(this);
	}

	/**
	 * Returns the JFrame representing ScriptEase.
	 * 
	 * @return
	 */
	public JFrame getFrame() {
		return this.seFrame;
	}

	/**
	 * Used by the constructor to set up and lay out the frame's internals. Also
	 * used to rebuild the frame's internals when the preferred layout changes.
	 */
	public void populate() {
		final JPanel content = new JPanel();

		final JComponent statusBar = this.buildStatusBar();

		final GroupLayout layout = new GroupLayout(content);
		final String preferredLayout;

		content.setLayout(layout);

		// Get the preferred layout.
		preferredLayout = ScriptEase.getInstance().getPreference(
				ScriptEase.PREFERRED_LAYOUT_KEY);

		// Compressed Layout

		SEFrame.this.middleSplit.setTopComponent(PanelFactory.getInstance()
				.buildStoryLibraryPane());
		this.middleSplit.setBottomComponent(this.middlePane);
		content.add(this.middleSplit);
		content.add(statusBar);

		layout.setHorizontalGroup(layout.createParallelGroup()
				.addComponent(this.middleSplit).addComponent(statusBar));

		layout.setVerticalGroup(layout
				.createSequentialGroup()
				.addComponent(this.middleSplit)
				.addComponent(statusBar, GroupLayout.PREFERRED_SIZE,
						GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE));
		if (preferredLayout.equalsIgnoreCase(ScriptEase.UNCOMPRESSED_LAYOUT)) {
			// Uncompressed Layout.

			// TODO Do something special if layout is uncompressed. Removed this
			// when building the Library Pane was moved to panelfactory.
		}

		this.seFrame.getContentPane().removeAll();
		this.seFrame.getContentPane().add(content);
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

		model.process(new ModelAdapter() {
			@Override
			public void processLibraryModel(LibraryModel libraryModel) {
				// Creates a Library Editor panel
				final JScrollPane scbScrollPane;
				final CloseableModelTab newTab;

				scbScrollPane = PanelFactory.getInstance()
						.buildLibraryEditorPanel(libraryModel);
				newTab = new CloseableModelTab(SEFrame.this.modelTabs,
						scbScrollPane, libraryModel, icon);

				scbScrollPane.getVerticalScrollBar().setUnitIncrement(
						ScriptEaseUI.VERTICAL_SCROLLBAR_INCREMENT);

				SEFrame.this.modelTabs.addTab(libraryModel.getName()
						+ "[Editor]", icon, scbScrollPane);
				SEFrame.this.modelTabs.setTabComponentAt(
						SEFrame.this.modelTabs.indexOfComponent(scbScrollPane),
						newTab);
			}

			@Override
			public void processStoryModel(final StoryModel storyModel) {
				// Creates a story editor panel with a story graph
				final StoryPoint startStoryPoint;
				final JSplitPane newPanel;
				final CloseableModelTab newTab;
				final String title;
				String modelTitle;

				startStoryPoint = storyModel.getRoot();
				newPanel = PanelFactory.getInstance().buildStoryPanel(
						storyModel, startStoryPoint);
				newTab = new CloseableModelTab(SEFrame.this.modelTabs,
						newPanel, storyModel, icon);
				modelTitle = storyModel.getTitle();

				if (modelTitle == null || modelTitle.equals(""))
					modelTitle = "<Untitled>";

				title = modelTitle + "("
						+ storyModel.getModule().getLocation().getName() + ")";

				SEFrame.this.modelTabs.addTab(title, icon, newPanel);
				SEFrame.this.modelTabs.setTabComponentAt(
						SEFrame.this.modelTabs.indexOfComponent(newPanel),
						newTab);
				SEFrame.this.modelTabs.setSelectedComponent(newPanel);

				/*
				 * Setting the divider needs to occur here because the
				 * JSplitPane needs to actually be drawn before this works.
				 * According to Sun, this is WAD. I would tend to disagree, but
				 * at least this is nicer than subclassing JSplitPane.
				 */
				SwingUtilities.invokeLater(new Runnable() {
					@Override
					public void run() {
						newPanel.setDividerLocation(0.3);
					}
				});
			}
		});
	}

	/**
	 * Gets the tab pane for displaying all models.
	 * 
	 * @return The tab pane for the Stories;
	 */
	public JTabbedPane getModelTabPane() {
		return this.modelTabs;
	}

	/**
	 * Removes the given model component from list of ModelTabs and the list of
	 * model components for the given model. modelTabs.remove should not be
	 * called outside of this method.
	 * 
	 * @param component
	 * @param model
	 */
	public void removeModelComponent(JComponent component, PatternModel model) {
		// remove the panel
		PanelFactory.getInstance().removeComponentForModel(model, component);

		this.modelTabs.remove(component);

		// check if there are any unsaved changes
		if (FileManager.getInstance().hasUnsavedChanges(model)) {
			// otherwise, close the StoryModel

			model.process(new ModelAdapter() {
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

			model.process(new ModelAdapter() {

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

					SEFrame.this.seFrame.setTitle(newTitle);
				}

				@Override
				public void processLibraryModel(LibraryModel libraryModel) {
					final int prevLocation;
					// This sucks, but we need to revalidate the menu bar.
					// http://bugs.sun.com/view_bug.do?bug_id=4949810
					final JMenuBar bar;
					
					prevLocation = SEFrame.this.middleSplit
							.getDividerLocation();
					bar = MenuFactory.createMainMenuBar(true);
					
					SEFrame.this.getFrame().setJMenuBar(bar);
					
					SwingUtilities.invokeLater(new Runnable() {
						@Override
						public void run() {
							SEFrame.this.middleSplit
									.setDividerLocation(prevLocation);
						}
					});
					
					this.setScriptEaseTitle();
					bar.revalidate();

				}

				@Override
				public void processStoryModel(StoryModel storyModel) {
					final int prevLocation;
					// This sucks, but we need to revalidate the menu bar.
					// http://bugs.sun.com/view_bug.do?bug_id=4949810
					final JMenuBar bar;
					
					prevLocation = SEFrame.this.middleSplit
							.getDividerLocation();
					bar = MenuFactory.createMainMenuBar(false);
					SEFrame.this.getFrame().setJMenuBar(bar);

					/*
					 * Setting the divider needs to occur here because the
					 * JSplitPane needs to actually be drawn before this works.
					 * According to Sun, this is WAD. I would tend to disagree,
					 * but at least this is nicer than subclassing JSplitPane.
					 */
					SwingUtilities.invokeLater(new Runnable() {
						@Override
						public void run() {
							SEFrame.this.middleSplit
									.setDividerLocation(prevLocation);
						}
					});

					this.setScriptEaseTitle();
					bar.revalidate();
				}
			});
		} else if (eventType == PatternModelEvent.PATTERN_MODEL_REMOVED
				&& PatternModelManager.getInstance().getActiveModel() == null) {
			// This sucks, but we need to revalidate the menu bar.
			// http://bugs.sun.com/view_bug.do?bug_id=4949810
			final JMenuBar bar = MenuFactory.createMainMenuBar(false);
			this.seFrame.setJMenuBar(bar);
			this.seFrame.setTitle(ScriptEase.TITLE);
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
			this.messages = new LinkedList<String>();
			this.textQueue = new Timer(QueueTimer, new ActionListener() {
				public void actionPerformed(ActionEvent arg0) {
					if (!TimedLabel.this.messages.isEmpty())
						TimedLabel.this.setText(TimedLabel.this.messages.poll());
				};
			});
			this.textClear = new Timer(ClearTimer, new ActionListener() {
				public void actionPerformed(ActionEvent arg0) {
					TimedLabel.super.setText("");
					TimedLabel.this.messages.clear();
				};
			});

			this.textQueue.setRepeats(false);
			this.textClear.setRepeats(false);
		}

		public void setText(String text) {
			super.setText(text);
			if (this.textQueue != null)
				this.textQueue.restart();
			if (this.textClear != null)
				this.textClear.restart();
		};

		public void queueText(String text) {
			this.messages.add(text);
			if (this.getText().isEmpty())
				this.setText(text);
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
			super(GameLabel.noTransText);

			TranslatorManager.getInstance().addTranslatorObserver(this);
		}

		@Override
		public void translatorLoaded(Translator newTranslator) {
			if (newTranslator != null) {
				this.setText(newTranslator.getName());
				this.setEnabled(true);
				this.setIcon(newTranslator.getIcon());
			} else {
				this.setText(GameLabel.noTransText);
				this.setEnabled(false);
				this.setIcon(null);
			}
		}
	}
}