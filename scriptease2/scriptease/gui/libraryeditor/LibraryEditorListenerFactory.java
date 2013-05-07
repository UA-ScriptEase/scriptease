package scriptease.gui.libraryeditor;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;

import scriptease.controller.StoryAdapter;
import scriptease.controller.StoryVisitor;
import scriptease.controller.observer.storycomponent.StoryComponentEvent;
import scriptease.controller.observer.storycomponent.StoryComponentEvent.StoryComponentChangeEnum;
import scriptease.controller.observer.storycomponent.StoryComponentObserver;
import scriptease.gui.storycomponentpanel.StoryComponentPanel;
import scriptease.model.CodeBlock;
import scriptease.model.LibraryModel;
import scriptease.model.SEModelManager;
import scriptease.model.StoryComponent;
import scriptease.model.atomic.KnowIt;
import scriptease.model.complex.ScriptIt;
import scriptease.translator.Translator;
import scriptease.translator.TranslatorManager;
import scriptease.translator.apimanagers.GameTypeManager;

/**
 * Factory for listeners for the Story Component Builder.
 * 
 * @author kschenk
 * 
 */
public class LibraryEditorListenerFactory {

	private static LibraryEditorListenerFactory instance = new LibraryEditorListenerFactory();

	// These need to be instance variables or else they get garbage collected.
	// XXX Note that this will cause some listeners to get garbage collected
	// when we have two library editors open.
	private StoryComponentObserver storyComponentObserver;
	// Call refreshCodeBlockComponentObserverList when a new codeblock selected.
	private List<StoryComponentObserver> codeBlockComponentObservers = new ArrayList<StoryComponentObserver>();

	/**
	 * Returns the sole instance of the UIListenerFactory.
	 * 
	 * @return
	 */
	protected static LibraryEditorListenerFactory getInstance() {
		return instance;
	}

	/**
	 * Builds a tree selection listener for the StoryComponentLibrary. It may be
	 * possible to use this in the future to make a general
	 * TreeSelectionListener for more LibraryPanes.
	 * 
	 * @parma storyVisitor The StoryVisitor that determines action when a
	 *        specific story component is selected.
	 * @return
	 */
	protected MouseListener buildStoryComponentMouseListener(
			final StoryVisitor storyVisitor) {
		return new MouseAdapter() {
			@Override
			public void mouseReleased(MouseEvent e) {
				if (SEModelManager.getInstance().getActiveModel() instanceof LibraryModel)
					if (e.getSource() instanceof JList) {
						final JList componentList;
						final StoryComponentPanel componentPanel;
						final StoryComponent component;

						componentList = (JList) e.getSource();

						if (componentList.getSelectedValue() instanceof StoryComponentPanel) {
							componentPanel = (StoryComponentPanel) componentList
									.getSelectedValue();
							component = componentPanel.getStoryComponent();

							component.process(storyVisitor);
						} else if (componentList.getSelectedValue() != null) {
							throw new ClassCastException(
									"StoryComponentPanel expected but "
											+ e.getSource().getClass()
											+ " found.");
						}
					}
			}
		};
	}

	/**
	 * Builds a new observer for the script it editor. Note that calling this
	 * method again or {@link #buildKnowItEditorObserver(Runnable)} will cause
	 * this listener to be garbage collected.
	 * 
	 * @param runnable
	 * @return
	 */
	protected StoryComponentObserver buildScriptItEditorObserver(
			final Runnable runnable) {
		this.storyComponentObserver = new StoryComponentObserver() {
			@Override
			public void componentChanged(StoryComponentEvent event) {
				final StoryComponentChangeEnum type;

				type = event.getType();

				if (type == StoryComponentChangeEnum.CHANGE_CODEBLOCK_ADDED
						|| type == StoryComponentChangeEnum.CHANGE_CODEBLOCK_REMOVED)
					runnable.run();
			}
		};

		return this.storyComponentObserver;
	}

	/**
	 * Creates an observer for the parameter panel.
	 * 
	 * @param scriptIt
	 * @param codeBlock
	 * @param parameterPanel
	 * @return
	 */
	protected StoryComponentObserver buildParameterObserver(
			final CodeBlock codeBlock, final JPanel parameterPanel) {
		final StoryComponentObserver parameterPanelObserver;

		parameterPanelObserver = new StoryComponentObserver() {
			@Override
			public void componentChanged(StoryComponentEvent event) {
				final StoryComponentChangeEnum type;
				final StoryComponent component;
				final StoryVisitor storyVisitor;

				type = event.getType();
				component = event.getSource();

				storyVisitor = new StoryAdapter() {
					@Override
					public void processScriptIt(ScriptIt scriptIt) {
						switch (type) {
						case CHANGE_PARAMETER_LIST_ADD:
							final List<KnowIt> knowIts;
							final KnowIt knowItToAdd;

							knowIts = codeBlock.getParameters();
							knowItToAdd = knowIts.get(knowIts.size() - 1);
							parameterPanel.add(LibraryEditorPanelFactory
									.getInstance().buildParameterPanel(
											scriptIt, codeBlock, knowItToAdd));

							parameterPanel.repaint();
							parameterPanel.revalidate();
							break;
						case CHANGE_PARAMETER_LIST_REMOVE:
							parameterPanel.removeAll();
							for (KnowIt knowIt : codeBlock.getParameters()) {
								parameterPanel.add(LibraryEditorPanelFactory
										.getInstance().buildParameterPanel(
												scriptIt, codeBlock, knowIt));
							}

							parameterPanel.repaint();
							parameterPanel.revalidate();
							break;
						default:
							break;
						}
					}
				};
				component.process(storyVisitor);
			}
		};
		this.codeBlockComponentObservers.add(parameterPanelObserver);

		return parameterPanelObserver;
	}

	/**
	 * Observer that observes the slot box and adjusts the implicits label
	 * accordingly.
	 * 
	 * @param codeBlock
	 * @param implicitsLabel
	 * @return
	 */
	protected StoryComponentObserver buildSlotObserver(
			final CodeBlock codeBlock, final JLabel implicitsLabel) {
		final StoryComponentObserver subjectBoxObserver;

		subjectBoxObserver = new StoryComponentObserver() {
			@Override
			public void componentChanged(StoryComponentEvent event) {
				final StoryComponentChangeEnum type;
				final StoryComponent component;
				final StoryVisitor storyVisitor;

				type = event.getType();
				component = event.getSource();
				storyVisitor = new StoryAdapter() {
					@Override
					public void processScriptIt(ScriptIt scriptIt) {
						if (type == StoryComponentChangeEnum.CODE_BLOCK_SLOT_SET) {
							String implicits = "";

							for (KnowIt implicit : codeBlock.getImplicits())
								implicits += "[" + implicit.getDisplayText()
										+ "] ";

							implicitsLabel.setText(implicits.trim());
							implicitsLabel.revalidate();
						}
					}
				};
				component.process(storyVisitor);
			}
		};
		this.codeBlockComponentObservers.add(subjectBoxObserver);

		return subjectBoxObserver;
	}

	/**
	 * Builds an observer for the code block component that sees changes to the
	 * model.
	 * 
	 * @param scriptIt
	 * @param deleteCodeBlockButton
	 * @param codeBlockComponent
	 * @return
	 */
	protected StoryComponentObserver buildCodeBlockComponentObserver(
			final JButton deleteCodeBlockButton) {
		final StoryComponentObserver codeBlockObserver;

		codeBlockObserver = new StoryComponentObserver() {
			@Override
			public void componentChanged(StoryComponentEvent event) {
				final StoryComponentChangeEnum type;
				final StoryComponent component;
				final StoryVisitor storyVisitor;

				type = event.getType();
				component = event.getSource();
				storyVisitor = new StoryAdapter() {
					@Override
					public void processScriptIt(ScriptIt scriptIt) {
						switch (type) {
						case CHANGE_CODEBLOCK_ADDED:
						case CHANGE_CODEBLOCK_REMOVED:
							if (scriptIt.getCodeBlocks().size() > 1)
								deleteCodeBlockButton.setEnabled(true);
							else
								deleteCodeBlockButton.setEnabled(false);
							break;
						default:
							break;
						}
					}
				};

				component.process(storyVisitor);
			}
		};
		this.codeBlockComponentObservers.add(codeBlockObserver);

		return codeBlockObserver;
	}

	/**
	 * Builds an observer for when a parameters' types are set.
	 * 
	 * @param scriptIt
	 * @param deleteCodeBlockButton
	 * @param codeBlockComponent
	 * @return
	 */
	protected StoryComponentObserver buildParameterTypeObserver(
			final KnowIt knowIt, final JComboBox defaultTypeBox) {
		final StoryComponentObserver codeBlockObserver;

		codeBlockObserver = new StoryComponentObserver() {
			@Override
			public void componentChanged(StoryComponentEvent event) {
				final StoryComponentChangeEnum type;
				final StoryComponent component;
				final StoryVisitor storyVisitor;

				final Translator activeTranslator;
				final GameTypeManager gameTypeManager;

				activeTranslator = TranslatorManager.getInstance()
						.getActiveTranslator();
				gameTypeManager = activeTranslator.getGameTypeManager();

				type = event.getType();
				component = event.getSource();
				storyVisitor = new StoryAdapter() {
					@Override
					public void processScriptIt(ScriptIt ScriptIt) {
						switch (type) {
						case CHANGE_PARAMETER_TYPES_SET:
							final String initialDefaultType;
							initialDefaultType = (String) defaultTypeBox
									.getSelectedItem();

							defaultTypeBox.removeAllItems();

							for (String type : knowIt.getTypes()) {
								defaultTypeBox.addItem(gameTypeManager
										.getDisplayText(type) + " - " + type);
							}

							defaultTypeBox.setSelectedItem(initialDefaultType);

							defaultTypeBox.revalidate();
						default:
							break;
						}
					}
				};

				component.process(storyVisitor);
			}
		};
		this.codeBlockComponentObservers.add(codeBlockObserver);

		return codeBlockObserver;
	}

	/**
	 * Builds an observer for when a parameter's default type is set.
	 * 
	 * @param scriptIt
	 * @param deleteCodeBlockButton
	 * @param codeBlockComponent
	 * @return
	 */
	protected StoryComponentObserver buildParameterDefaultTypeObserver() {
		final StoryComponentObserver codeBlockObserver;

		codeBlockObserver = new StoryComponentObserver() {
			@Override
			public void componentChanged(StoryComponentEvent event) {
				final StoryComponentChangeEnum type;
				final StoryComponent component;
				final StoryVisitor storyVisitor;

				type = event.getType();
				component = event.getSource();
				storyVisitor = new StoryAdapter() {
					@Override
					public void processScriptIt(ScriptIt scriptIt) {
						switch (type) {
						case CHANGE_PARAMETER_DEFAULT_TYPE_SET:

							scriptIt.notifyObservers(new StoryComponentEvent(
									scriptIt,
									StoryComponentChangeEnum.CODE_BLOCK_SUBJECT_SET));
						default:
							break;
						}
					}
				};

				component.process(storyVisitor);
			}
		};
		this.codeBlockComponentObservers.add(codeBlockObserver);

		return codeBlockObserver;
	}

	/**
	 * Call this before adding more observers to code blocks, so that the old
	 * ones can get garbage collected.
	 */
	protected void refreshCodeBlockComponentObserverList() {
		this.codeBlockComponentObservers
				.removeAll(this.codeBlockComponentObservers);
	}
}