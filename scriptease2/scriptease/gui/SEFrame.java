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
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenuBar;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.SwingUtilities;
import javax.swing.Timer;

import scriptease.ScriptEase;
import scriptease.controller.ModelAdapter;
import scriptease.controller.observer.PatternModelEvent;
import scriptease.controller.observer.PatternModelObserver;
import scriptease.controller.observer.TranslatorObserver;
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
		this.middleSplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);

		final int MIN_HEIGHT = 480;
		final int MIN_WIDTH = 640;

		final JPanel middlePane;

		middlePane = new JPanel();

		this.seFrame.setMinimumSize(new Dimension(MIN_WIDTH, MIN_HEIGHT));
		this.seFrame.setExtendedState(Frame.MAXIMIZED_BOTH);

		middlePane.setLayout(new GridLayout(1, 1));
		middlePane.add(PanelFactory.getInstance().getModelTabPane());

		final JPanel content;
		final JComponent statusBar;
		final GroupLayout contentLayout;
		final String preferredLayout;

		content = new JPanel();
		statusBar = this.buildStatusBar();

		contentLayout = new GroupLayout(content);

		content.setLayout(contentLayout);

		// Get the preferred layout.
		preferredLayout = ScriptEase.getInstance().getPreference(
				ScriptEase.PREFERRED_LAYOUT_KEY);

		// Compressed Layout
		SEFrame.this.middleSplit.setTopComponent(PanelFactory.getInstance()
				.buildLibrarySplitPane());
		this.middleSplit.setBottomComponent(middlePane);
		content.add(this.middleSplit);
		content.add(statusBar);

		contentLayout.setHorizontalGroup(contentLayout.createParallelGroup()
				.addComponent(this.middleSplit).addComponent(statusBar));

		contentLayout
				.setVerticalGroup(contentLayout
						.createSequentialGroup()
						.addComponent(this.middleSplit)
						.addComponent(statusBar, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE));
		if (preferredLayout.equalsIgnoreCase(ScriptEase.UNCOMPRESSED_LAYOUT)) {
			// Uncompressed Layout.
			// TODO Do something special if layout is uncompressed. Removed this
			// when building of the Library Pane was moved to panelfactory.
		}

		this.seFrame.getContentPane().add(content);

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