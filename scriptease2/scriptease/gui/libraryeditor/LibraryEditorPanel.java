package scriptease.gui.libraryeditor;

import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Collection;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import scriptease.ScriptEase;
import scriptease.controller.StoryAdapter;
import scriptease.controller.StoryVisitor;
import scriptease.controller.observer.StoryComponentPanelJListObserver;
import scriptease.controller.observer.storycomponent.StoryComponentEvent;
import scriptease.controller.observer.storycomponent.StoryComponentObserver;
import scriptease.controller.undo.UndoManager;
import scriptease.gui.WidgetDecorator;
import scriptease.gui.action.typemenus.TypeAction;
import scriptease.gui.component.ComponentFactory;
import scriptease.gui.libraryeditor.codeblocks.CodeBlockPanel;
import scriptease.gui.pane.LibraryPanel;
import scriptease.gui.ui.ScriptEaseUI;
import scriptease.model.CodeBlock;
import scriptease.model.CodeBlockSource;
import scriptease.model.StoryComponent;
import scriptease.model.atomic.KnowIt;
import scriptease.model.atomic.describeits.DescribeIt;
import scriptease.model.complex.ActivityIt;
import scriptease.model.complex.AskIt;
import scriptease.model.complex.CauseIt;
import scriptease.model.complex.ScriptIt;
import scriptease.model.complex.behaviours.Behaviour;
import scriptease.model.semodel.librarymodel.LibraryModel;
import scriptease.translator.io.model.GameType;

/**
 * Creates a Panel dependent on the component being edited in the Library
 * editor.
 * 
 * @author mfchurch
 * @author jyuen
 */
@SuppressWarnings("serial")
public class LibraryEditorPanel extends JPanel {
	private final StoryVisitor panelBuilder;

	public LibraryEditorPanel() {
		this.setBackground(ScriptEaseUI.PRIMARY_UI);
		this.setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));

		final JLabel info;

		info = new JLabel("Select a story component from the left to edit it.");

		info.setFont(new Font("SansSerif", Font.PLAIN, 16));

		this.add(info);

		LibraryPanel.getMainLibraryPanel().addStoryComponentPanelJListObserver(
				new StoryComponentPanelJListObserver() {
					@Override
					public void componentSelected(StoryComponent component) {
						if (component == null) {
							LibraryEditorPanel.this.removeAll();
							LibraryEditorPanel.this.revalidate();
						} else {
							component.process(panelBuilder);
							component
									.addStoryComponentObserver(new StoryComponentObserver() {

										@Override
										public void componentChanged(
												StoryComponentEvent event) {
											if (event.getType() == StoryComponentEvent.StoryComponentChangeEnum.CHANGE_REMOVED) {
												LibraryEditorPanel.this
														.removeAll();
											}
										}
									});
						}
					}
				});

		/*
		 * Create a Story Adapter which calls an update on the editorPanel. This
		 * is used as a sort of Command Pattern with UIListenerFactory.
		 */
		this.panelBuilder = new StoryAdapter() {
			private final LibraryEditorPanel pane = LibraryEditorPanel.this;

			private void setUpCodeBlockPanels(final ScriptIt scriptIt,
					final JPanel editingPanel) {
				editingPanel.removeAll();

				for (CodeBlock codeBlock : scriptIt.getCodeBlocks()) {
					editingPanel.add(new CodeBlockPanel(codeBlock, scriptIt));
				}

				editingPanel.revalidate();
			}

			@Override
			public void processScriptIt(final ScriptIt scriptIt) {
				// Causes and effects are processed as ScriptIts
				this.defaultProcess(scriptIt);

				final JPanel codeBlockEditingPanel;

				final boolean isEditable;

				codeBlockEditingPanel = new JPanel();

				codeBlockEditingPanel.setLayout(new BoxLayout(
						codeBlockEditingPanel, BoxLayout.PAGE_AXIS));

				isEditable = ScriptEase.DEBUG_MODE
						|| !scriptIt.getLibrary().isReadOnly();

				if (!(scriptIt instanceof CauseIt)) {
					final JPanel scriptItControlPanel;
					final JButton addCodeBlockButton;

					scriptItControlPanel = new JPanel();
					addCodeBlockButton = ComponentFactory.buildFlatButton(
							ScriptEaseUI.SE_BLUE, "Add CodeBlock");

					scriptItControlPanel.setLayout(new FlowLayout(
							FlowLayout.LEADING));
					scriptItControlPanel.setOpaque(false);

					addCodeBlockButton.addActionListener(new ActionListener() {
						@Override
						public void actionPerformed(ActionEvent e) {
							if (!UndoManager.getInstance()
									.hasOpenUndoableAction())
								UndoManager.getInstance().startUndoableAction(
										"Adding CodeBlock to "
												+ scriptIt.getDisplayText());

							final LibraryModel library = scriptIt.getLibrary();
							final CodeBlock codeBlock = new CodeBlockSource(
									library);

							scriptIt.addCodeBlock(codeBlock);

							UndoManager.getInstance().endUndoableAction();
						}
					});
					if (!isEditable) {
						addCodeBlockButton.setEnabled(false);
					}

					scriptItControlPanel.add(addCodeBlockButton);
					this.pane.add(scriptItControlPanel);
				}

				this.pane.add(codeBlockEditingPanel);

				this.setUpCodeBlockPanels(scriptIt, codeBlockEditingPanel);

				scriptIt.addStoryComponentObserver(this,
						new StoryComponentObserver() {
							@Override
							public void componentChanged(
									StoryComponentEvent event) {
								switch (event.getType()) {
								case CHANGE_CODEBLOCK_ADDED:
								case CHANGE_CODEBLOCK_REMOVED:
									setUpCodeBlockPanels(scriptIt,
											codeBlockEditingPanel);
								default:
									break;
								}
							}
						});

				this.pane.revalidate();
			}

			@Override
			public void processActivityIt(ActivityIt activityIt) {
				final JPanel panel;

				panel = LibraryEditorPanelFactory.getInstance()
						.buildActivityItEditingPanel(activityIt);

				this.defaultProcess(activityIt);
				this.pane.add(panel);

				this.pane.repaint();
				this.pane.revalidate();
			}

			@Override
			public void processBehaviour(Behaviour behaviour) {
				final JPanel panel;
				panel = LibraryEditorPanelFactory.getInstance()
						.buildBehaviourEditingPanel(behaviour);
				
				this.pane.removeAll();
				this.pane.add(panel);

				this.pane.repaint();
				this.pane.revalidate();
				// Should we add default process? I'm not realy sure how
				// behaviours are generated.
			}

			/**
			 * @param knowIt
			 */
			@Override
			public void processKnowIt(final KnowIt knowIt) {
				this.pane.removeAll();

				final JPanel knowItPanel;
				final JComponent describeItEditingPanel;

				final GroupLayout knowItPanelLayout;
				final TypeAction typeAction;
				final Runnable commitText;

				final JButton typesButton;
				final JTextField nameField;

				final JLabel nameLabel;
				final JLabel typesLabel;
				final JLabel readOnlyLabel;

				final DescribeIt describeIt;

				final boolean isEditable;

				knowItPanel = new JPanel();

				knowItPanelLayout = new GroupLayout(knowItPanel);
				typeAction = new TypeAction();
				typesButton = new JButton(typeAction);

				describeIt = knowIt.getLibrary().getDescribeIt(knowIt);

				isEditable = ScriptEase.DEBUG_MODE
						|| !knowIt.getLibrary().isReadOnly();

				nameField = new JTextField(describeIt.getName());

				nameLabel = new JLabel("Name: ");
				typesLabel = new JLabel("Types: ");
				readOnlyLabel = new JLabel(
						"This element is from a read-only library and cannot be edited.");
				commitText = new Runnable() {
					@Override
					public void run() {
						describeIt.setName(nameField.getText());
						knowIt.setDisplayText(nameField.getText());
					}
				};

				describeItEditingPanel = LibraryEditorPanelFactory
						.getInstance().buildDescribeItEditingPanel(describeIt,
								knowIt);

				knowItPanel.setLayout(knowItPanelLayout);
				knowItPanel.setOpaque(false);

				typesLabel.setFont(LibraryEditorPanelFactory.labelFont);
				nameLabel.setFont(LibraryEditorPanelFactory.labelFont);
				readOnlyLabel.setFont(LibraryEditorPanelFactory.labelFont);
				readOnlyLabel.setForeground(ScriptEaseUI.SE_BLUE);

				typeAction.deselectAll();
				typeAction.selectTypesByKeyword(knowIt.getTypes(), true);

				WidgetDecorator.decorateJTextFieldForFocusEvents(nameField,
						commitText, false);

				nameField.setHorizontalAlignment(JTextField.LEADING);

				knowItPanel.setBorder(BorderFactory
						.createTitledBorder("DescribeIt"));

				typeAction.setAction(new Runnable() {
					@Override
					public void run() {
						final Collection<GameType> types = typeAction
								.getSelectedTypes();

						// Important: DescribeIt types MUST be set first because
						// KnowIts notify observers when their's are changed,
						// throwing NullPointExceptions everywhere!
						describeIt.setTypes(types);

						knowIt.setTypes(types);
					}
				});

				if (!isEditable) {
					typesButton.setEnabled(isEditable);
					nameField.setEnabled(isEditable);

					knowItPanelLayout
							.setHorizontalGroup(knowItPanelLayout
									.createParallelGroup()
									.addGroup(
											knowItPanelLayout
													.createSequentialGroup()
													.addGroup(
															knowItPanelLayout
																	.createParallelGroup()
																	.addComponent(
																			nameLabel)
																	.addComponent(
																			typesLabel))
													.addGroup(
															knowItPanelLayout
																	.createParallelGroup()
																	.addComponent(
																			readOnlyLabel)
																	.addComponent(
																			nameField)
																	.addComponent(
																			typesButton))));

					knowItPanelLayout
							.setVerticalGroup(knowItPanelLayout
									.createSequentialGroup()
									.addGroup(
											knowItPanelLayout
													.createParallelGroup(
															GroupLayout.Alignment.BASELINE)
													.addComponent(readOnlyLabel))
									.addGroup(
											knowItPanelLayout
													.createParallelGroup(
															GroupLayout.Alignment.BASELINE)
													.addComponent(nameLabel)
													.addComponent(nameField))
									.addGroup(
											knowItPanelLayout
													.createParallelGroup(
															GroupLayout.Alignment.BASELINE)
													.addComponent(typesLabel)
													.addComponent(typesButton)));
				} else {
					knowItPanelLayout
							.setHorizontalGroup(knowItPanelLayout
									.createParallelGroup()
									.addGroup(
											knowItPanelLayout
													.createSequentialGroup()
													.addGroup(
															knowItPanelLayout
																	.createParallelGroup()
																	.addComponent(
																			nameLabel)
																	.addComponent(
																			typesLabel))
													.addGroup(
															knowItPanelLayout
																	.createParallelGroup()
																	.addComponent(
																			nameField)
																	.addComponent(
																			typesButton))));

					knowItPanelLayout
							.setVerticalGroup(knowItPanelLayout
									.createSequentialGroup()
									.addGroup(
											knowItPanelLayout
													.createParallelGroup(
															GroupLayout.Alignment.BASELINE)
													.addComponent(nameLabel)
													.addComponent(nameField))
									.addGroup(
											knowItPanelLayout
													.createParallelGroup(
															GroupLayout.Alignment.BASELINE)
													.addComponent(typesLabel)
													.addComponent(typesButton)));
				}

				this.pane.add(knowItPanel);
				this.pane.add(describeItEditingPanel);
				this.pane.revalidate();
			}

			// We may want to implement these later, so their default methods
			// are here in case.
			@Override
			public void processAskIt(AskIt questionIt) {
				this.defaultProcess(questionIt);
			}

			@Override
			public void defaultProcess(StoryComponent component) {
				final JPanel descriptorPanel;

				descriptorPanel = LibraryEditorPanelFactory.getInstance()
						.buildDescriptorPanel(component);

				this.pane.removeAll();
				this.pane.add(descriptorPanel);
				this.pane.revalidate();
			}
		};
	}
}
