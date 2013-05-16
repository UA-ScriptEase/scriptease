package scriptease.gui.libraryeditor;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.Collection;
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
import scriptease.model.complex.CauseIt;
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
						final Object selected;

						componentList = (JList) e.getSource();
						selected = componentList.getSelectedValue();

						if (selected instanceof StoryComponentPanel) {
							componentPanel = (StoryComponentPanel) selected;
							component = componentPanel.getStoryComponent();
							component.process(storyVisitor);
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
	protected StoryComponentObserver buildParameterPanelObserver(
			final CodeBlock codeBlock, final JPanel parameterPanel,
			final JComboBox subjectBox) {
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
							final Collection<String> slots;
							final KnowIt knowItToAdd;

							knowIts = codeBlock.getParameters();
							knowItToAdd = knowIts.get(knowIts.size() - 1);
							slots = TranslatorManager.getInstance()
									.getActiveTranslator().getGameTypeManager()
									.getSlots(knowItToAdd.getDefaultType());

							parameterPanel.add(LibraryEditorPanelFactory
									.getInstance().buildParameterPanel(
											scriptIt, codeBlock, knowItToAdd));

							if (!slots.isEmpty())
								subjectBox
										.addItem(knowItToAdd.getDisplayText());

							subjectBox.revalidate();

							parameterPanel.repaint();
							parameterPanel.revalidate();
							break;
						case CHANGE_PARAMETER_LIST_REMOVE:
							final List<String> parameterNames;
							KnowIt previousSubject;

							parameterNames = new ArrayList<String>();
							previousSubject = null;

							if (codeBlock.hasSubject())
								previousSubject = codeBlock.getSubject();

							parameterNames.add(null);
							for (KnowIt parameter : scriptIt.getParameters()) {
								final Collection<String> subjectSlots = TranslatorManager
										.getInstance().getActiveTranslator()
										.getGameTypeManager()
										.getSlots(parameter.getDefaultType());

								if (!subjectSlots.isEmpty())
									parameterNames.add(parameter
											.getDisplayText());
							}

							for (int index = 0; index < subjectBox
									.getItemCount(); index++) {
								String boxContent = (String) subjectBox
										.getItemAt(index);
								if (!parameterNames.contains(boxContent))
									subjectBox.removeItem(boxContent);
							}

							if (codeBlock.getParameters().contains(
									previousSubject)) {
								final String subjectName;
								subjectName = previousSubject.getDisplayText();
								subjectBox.setSelectedItem(subjectName);
							} else if (!(scriptIt instanceof CauseIt)) {
								subjectBox.setSelectedItem(null);
							}

							parameterPanel.removeAll();
							for (KnowIt knowIt : codeBlock.getParameters()) {
								parameterPanel.add(LibraryEditorPanelFactory
										.getInstance().buildParameterPanel(
												scriptIt, codeBlock, knowIt));
							}

							subjectBox.revalidate();

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
	 * Observer that observes the subject box and adjusts the slot box
	 * accordingly.
	 * 
	 * @param codeBlock
	 * @param slotBox
	 * @return
	 */
	protected StoryComponentObserver buildSubjectBoxObserver(
			final CodeBlock codeBlock, final JComboBox subjectBox,
			final JComboBox slotBox) {
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
						if (type == StoryComponentChangeEnum.CODE_BLOCK_SUBJECT_SET) {
							if (codeBlock.hasSubject()) {
								KnowIt subject = codeBlock.getSubject();
								if (subject != null
										&& !codeBlock.getSubjectName().equals(
												subjectBox.getSelectedItem())) {
									final Collection<String> subjectSlots;

									subjectSlots = TranslatorManager
											.getInstance()
											.getActiveTranslator()
											.getGameTypeManager()
											.getSlots(subject.getDefaultType());

									slotBox.removeAllItems();

									for (String slot : subjectSlots) {
										slotBox.addItem(slot);
									}
									if (!subjectSlots.isEmpty())
										slotBox.setSelectedItem(subjectSlots
												.toArray()[0]);
									else
										codeBlock.setSlot("");

									slotBox.revalidate();
								}
							}
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
	 * Observer that observes the slot box and adjusts the implicits label
	 * accordingly.
	 * 
	 * @param codeBlock
	 * @param implicitsLabel
	 * @return
	 */
	protected StoryComponentObserver buildSlotBoxObserver(
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
	 * Creates an observer for the parameter's name.
	 * 
	 * @param scriptIt
	 * @param codeBlock
	 * @param parameterPanel
	 * @return
	 */
	protected StoryComponentObserver buildParameterNameObserver(
			final CodeBlock codeBlock, final JComboBox subjectBox) {
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
						case CHANGE_PARAMETER_NAME_SET:
							final List<String> parameterNames;
							final List<String> subjectBoxContents;
							KnowIt previousSubject;

							parameterNames = new ArrayList<String>();
							subjectBoxContents = new ArrayList<String>();
							previousSubject = null;

							if (codeBlock.hasSubject())
								previousSubject = codeBlock.getSubject();

							for (KnowIt parameter : scriptIt.getParameters()) {
								final Collection<String> subjectSlots = TranslatorManager
										.getInstance().getActiveTranslator()
										.getGameTypeManager()
										.getSlots(parameter.getDefaultType());

								if (!subjectSlots.isEmpty())
									parameterNames.add(parameter
											.getDisplayText());
							}

							parameterNames.add(null);
							for (int index = 0; index < subjectBox
									.getItemCount(); index++) {
								subjectBoxContents.add((String) subjectBox
										.getItemAt(index));
							}

							for (String boxContent : subjectBoxContents) {
								if (!parameterNames.contains(boxContent))
									subjectBox.removeItem(boxContent);
							}

							for (String parameterName : parameterNames) {
								if (!subjectBoxContents.contains(parameterName))
									subjectBox.addItem(parameterName);
							}

							if (codeBlock.getParameters().contains(
									previousSubject)) {
								final String subjectName;
								subjectName = previousSubject.getDisplayText();
								subjectBox.setSelectedItem(subjectName);
							} else if (!(scriptIt instanceof CauseIt)) {
								subjectBox.setSelectedItem(null);
							}

							subjectBox.revalidate();
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