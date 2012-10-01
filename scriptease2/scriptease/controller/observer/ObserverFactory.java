package scriptease.controller.observer;

import java.util.ArrayList;
import java.util.Collection;

import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenuBar;
import javax.swing.JSplitPane;
import javax.swing.SwingUtilities;

import scriptease.ScriptEase;
import scriptease.controller.ModelAdapter;
import scriptease.gui.MenuFactory;
import scriptease.model.LibraryModel;
import scriptease.model.PatternModel;
import scriptease.model.PatternModelManager;
import scriptease.model.StoryModel;
import scriptease.translator.Translator;

/**
 * A factory that creates specialized observers. All observers that must exist
 * in ScriptEase should be stored in the constant observer list.
 * 
 * @author kschenk
 * 
 */
public class ObserverFactory {
	// A list of observers that do not get garbage collected
	private final Collection<Object> constantObserverList;

	private static ObserverFactory instance = new ObserverFactory();

	/**
	 * Returns the sole instance of ObserverFactory.
	 * 
	 * @return
	 */
	public static ObserverFactory getInstance() {
		return instance;
	}

	private ObserverFactory() {
		this.constantObserverList = new ArrayList<Object>();
	}

	/**
	 * Creates an observer for the main ScriptEase frame. Observes changes to
	 * Pattern Models and changes the title and JMenuBar of the frame
	 * appropriately.
	 * 
	 * @param frame
	 *            The frame to act upon.
	 * @return
	 */
	public PatternModelObserver buildFrameModelObserver(final JFrame frame) {
		final PatternModelObserver modelObserver;

		modelObserver = new PatternModelObserver() {
			@Override
			public void modelChanged(PatternModelEvent event) {
				final short eventType;
				final PatternModel activeModel;

				eventType = event.getEventType();
				activeModel = PatternModelManager.getInstance()
						.getActiveModel();

				if (eventType == PatternModelEvent.PATTERN_MODEL_ACTIVATED
						|| (eventType == PatternModelEvent.PATTERN_MODEL_REMOVED && activeModel == null)) {
					final JMenuBar bar;

					bar = MenuFactory.createMainMenuBar(activeModel);

					frame.setJMenuBar(bar);

					// Create the title for the frame
					String newTitle = "";
					if (activeModel != null) {
						String modelTitle = activeModel.getTitle();
						if (!modelTitle.isEmpty())
							newTitle += modelTitle + " - ";
					}
					newTitle += ScriptEase.TITLE;

					frame.setTitle(newTitle);

					// We need to revalidate the menu bar.
					// http://bugs.sun.com/view_bug.do?bug_id=4949810
					bar.revalidate();
				}
			}
		};

		this.constantObserverList.add(modelObserver);

		return modelObserver;
	}

	/**
	 * Builds an observer for the status bar.
	 * 
	 * @param label
	 * @return
	 */
	public TranslatorObserver buildStatusBarTranslatorObserver(
			final JLabel label) {
		final TranslatorObserver translatorObserver;

		translatorObserver = new TranslatorObserver() {
			@Override
			public void translatorLoaded(Translator newTranslator) {
				if (newTranslator != null) {
					label.setText(newTranslator.getName());
					label.setEnabled(true);
					label.setIcon(newTranslator.getIcon());
				} else {
					label.setText("-None-");
					label.setEnabled(false);
					label.setIcon(null);
				}
			}
		};

		this.constantObserverList.add(translatorObserver);

		return translatorObserver;
	}

	/**
	 * Builds an observer for the story library pane.
	 * 
	 * @param librarySplitPane
	 * @param storyJComponents
	 * @return
	 */
	public PatternModelObserver buildStoryLibraryPaneObserver(
			final JSplitPane librarySplitPane,
			final Collection<JComponent> storyJComponents) {
		final PatternModelObserver observer;

		observer = new PatternModelObserver() {
			public void modelChanged(PatternModelEvent event) {

				if (event.getEventType() == PatternModelEvent.PATTERN_MODEL_ACTIVATED) {
					event.getPatternModel().process(new ModelAdapter() {

						@Override
						public void processLibraryModel(
								LibraryModel libraryModel) {
							for (JComponent component : storyJComponents)
								component.setVisible(false);
						}

						@Override
						public void processStoryModel(StoryModel storyModel) {
							for (JComponent component : storyJComponents)
								component.setVisible(true);
							SwingUtilities.invokeLater(new Runnable() {
								@Override
								public void run() {
									librarySplitPane.setDividerLocation(0.5);
								}
							});
						}
					});
				}
			}
		};

		this.constantObserverList.add(observer);

		return observer;
	}
}
