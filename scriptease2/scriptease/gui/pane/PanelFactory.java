package scriptease.gui.pane;

import java.awt.BorderLayout;
import java.awt.Dimension;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSplitPane;

import scriptease.controller.observer.SEModelEvent;
import scriptease.controller.observer.SEModelObserver;
import scriptease.controller.observer.StatusObserver;
import scriptease.controller.observer.TranslatorObserver;
import scriptease.gui.StatusManager;
import scriptease.gui.WidgetDecorator;
import scriptease.gui.filters.CategoryFilter;
import scriptease.gui.storycomponentpanel.StoryComponentPanelJList;
import scriptease.gui.ui.ScriptEaseUI;
import scriptease.model.StoryComponent;
import scriptease.model.atomic.Note;
import scriptease.model.semodel.SEModelManager;
import scriptease.model.semodel.StoryModel;
import scriptease.model.semodel.librarymodel.LibraryModel;
import scriptease.translator.Translator;
import scriptease.translator.TranslatorManager;

/**
 * A factory class for different panels. All major panel construction should go
 * in here. This class implements the singleton design pattern.
 * 
 * @author kschenk
 * 
 */
public final class PanelFactory {
	private static PanelFactory instance = new PanelFactory();

	/**
	 * Gets the instance of PanelFactory.
	 * 
	 * @return
	 */
	public static PanelFactory getInstance() {
		return PanelFactory.instance;
	}

	private PanelFactory() {
	}

	public SEModelTabbedPane buildModelTabPanel() {
		return new SEModelTabbedPane();
	}

	/**
	 * Builds a JSplitPane that is used for pattern models which contains a
	 * LibraryPanel and a GameConstantPanel. The GameConstantPanel is hidden
	 * when a non-StoryModel PatternModel is opened up.
	 * 
	 * @return
	 */
	public JSplitPane buildLibrarySplitPane() {
		final JSplitPane librarySplitPane;
		final JPanel libraryPanel;
		final JPanel resourcePanel;

		librarySplitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
		resourcePanel = ResourcePanel.getInstance();
		libraryPanel = new JPanel();

		libraryPanel
				.setLayout(new BoxLayout(libraryPanel, BoxLayout.PAGE_AXIS));
		libraryPanel.add(LibraryPanel.getMainLibraryPanel());

		libraryPanel.add(this.buildNotePane());

		libraryPanel.setBackground(ScriptEaseUI.SECONDARY_UI);

		librarySplitPane.setBottomComponent(resourcePanel);
		librarySplitPane.setTopComponent(libraryPanel);

		librarySplitPane.setResizeWeight(0.5d);

		WidgetDecorator.setSimpleDivider(librarySplitPane);
		librarySplitPane.setBorder(null);

		return librarySplitPane;
	}

	/**
	 * Builds a panel with a {@link Note} component.
	 * 
	 * @return
	 */
	private JPanel buildNotePane() {
		final int HEIGHT_OF_NOTE = 32;
		final Dimension notePaneSize = new Dimension(0, HEIGHT_OF_NOTE);

		final StoryComponentPanelJList noteList;
		final JPanel notePane;

		noteList = new StoryComponentPanelJList(new CategoryFilter(
				StoryComponent.Type.NOTE), false);
		notePane = new JPanel();

		notePane.setLayout(new BorderLayout());
		notePane.add(noteList, BorderLayout.CENTER);

		notePane.setPreferredSize(notePaneSize);
		notePane.setMinimumSize(notePaneSize);
		notePane.setMaximumSize(new Dimension(ScriptEaseUI.MAX_SCREEN_WIDTH,
				HEIGHT_OF_NOTE));

		notePane.setBorder(BorderFactory.createMatteBorder(0, 1, 1, 1,
				ScriptEaseUI.SE_BLACK));

		noteList.addStoryComponents(LibraryModel.getCommonLibrary()
				.getNoteContainer().getChildren());

		notePane.setVisible(SEModelManager.getInstance().getActiveModel() instanceof StoryModel);

		SEModelManager.getInstance().addSEModelObserver(notePane,
				new SEModelObserver() {
					@Override
					public void modelChanged(SEModelEvent event) {
						if (event.getEventType() == SEModelEvent.Type.ACTIVATED) {
							// If a model is activated
							notePane.setVisible(event.getPatternModel() instanceof StoryModel);
						} else if (event.getEventType() == SEModelEvent.Type.REMOVED
								&& SEModelManager.getInstance()
										.getActiveModel() == null) {
							notePane.setVisible(false);
						}
					}
				});

		return notePane;
	}

	/**
	 * Builds a panel that displays the status of the game based on what has
	 * been passed to the {@link StatusManager}.
	 * 
	 * @return
	 */
	public JPanel buildStatusPanel() {
		final String NO_TRANSLATOR = "-None-";
		final String transPrefix = "Game: ";

		final JPanel statusPanel;
		final JLabel timedLabel;
		final JLabel currentTranslatorLabel;
		final JLabel currentTranslatorNameLabel;

		final TranslatorObserver translatorObserver;
		final StatusObserver statusObserver;

		statusPanel = new JPanel();
		timedLabel = new JLabel();
		currentTranslatorLabel = new JLabel(transPrefix);
		currentTranslatorNameLabel = new JLabel(NO_TRANSLATOR);

		timedLabel.setForeground(ScriptEaseUI.PRIMARY_UI);
		currentTranslatorLabel.setForeground(ScriptEaseUI.PRIMARY_UI);
		currentTranslatorNameLabel.setForeground(ScriptEaseUI.PRIMARY_UI);

		translatorObserver = new TranslatorObserver() {
			@Override
			public void translatorLoaded(Translator newTranslator) {
				if (newTranslator != null) {
					currentTranslatorNameLabel
							.setText(newTranslator.getTitle());
					currentTranslatorNameLabel.setEnabled(true);
					currentTranslatorNameLabel.setIcon(newTranslator.getIcon());
				} else {
					currentTranslatorNameLabel.setText(NO_TRANSLATOR);
					currentTranslatorNameLabel.setEnabled(false);
					currentTranslatorNameLabel.setIcon(null);
				}
			}
		};

		statusObserver = new StatusObserver() {
			@Override
			public void statusChanged(String newText) {
				timedLabel.setText(newText);
			}
		};

		TranslatorManager.getInstance().addTranslatorObserver(statusPanel,
				translatorObserver);
		StatusManager.getInstance().addStatusObserver(statusObserver);

		currentTranslatorNameLabel.setEnabled(false);
		currentTranslatorNameLabel.setBorder(BorderFactory.createEmptyBorder(0,
				5, 0, 5));

		statusPanel.setLayout(new BoxLayout(statusPanel, BoxLayout.LINE_AXIS));

		statusPanel.add(timedLabel);
		statusPanel.add(Box.createGlue());
		statusPanel.add(currentTranslatorLabel);
		statusPanel.add(currentTranslatorNameLabel);

		statusPanel.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
		statusPanel.setBackground(ScriptEaseUI.SECONDARY_UI);

		return statusPanel;
	}
}