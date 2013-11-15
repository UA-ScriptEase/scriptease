package scriptease.gui.libraryeditor;

import java.awt.FlowLayout;
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

import scriptease.controller.StoryAdapter;
import scriptease.controller.StoryVisitor;
import scriptease.controller.observer.StoryComponentPanelJListObserver;
import scriptease.controller.observer.storycomponent.StoryComponentEvent;
import scriptease.controller.observer.storycomponent.StoryComponentObserver;
import scriptease.controller.undo.UndoManager;
import scriptease.gui.WidgetDecorator;
import scriptease.gui.action.typemenus.TypeAction;
import scriptease.gui.libraryeditor.codeblocks.CodeBlockPanel;
import scriptease.gui.pane.LibraryPanel;
import scriptease.model.CodeBlock;
import scriptease.model.CodeBlockSource;
import scriptease.model.StoryComponent;
import scriptease.model.atomic.KnowIt;
import scriptease.model.atomic.describeits.DescribeIt;
import scriptease.model.complex.AskIt;
import scriptease.model.complex.CauseIt;
import scriptease.model.complex.ScriptIt;
import scriptease.model.complex.behaviours.Behaviour;
import scriptease.model.semodel.SEModelManager;
import scriptease.model.semodel.librarymodel.LibraryModel;

/**
 * LibraryEditorPanel is dependent on the component being edited in the Library
 * editor. A specific panel is created for each type of component.
 * 
 * @author mfchurch
 * @author jyuen
 */
@SuppressWarnings("serial")
public class LibraryEditorPanel extends JPanel implements
		StoryComponentPanelJListObserver {
	private final StoryVisitor panelBuilder;

	public LibraryEditorPanel() {
		this.setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
		LibraryPanel.getInstance().addStoryComponentPanelJListObserver(this);

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

				codeBlockEditingPanel = new JPanel();

				codeBlockEditingPanel.setLayout(new BoxLayout(
						codeBlockEditingPanel, BoxLayout.PAGE_AXIS));

				if (!(scriptIt instanceof CauseIt)) {
					final JPanel scriptItControlPanel;
					final JButton addCodeBlockButton;

					scriptItControlPanel = new JPanel();
					addCodeBlockButton = new JButton("Add CodeBlock");

					scriptItControlPanel.setLayout(new FlowLayout(
							FlowLayout.LEADING));

					scriptItControlPanel.setBorder(BorderFactory
							.createTitledBorder("Effect Control"));

					addCodeBlockButton.addActionListener(new ActionListener() {
						@Override
						public void actionPerformed(ActionEvent e) {
							if (!UndoManager.getInstance()
									.hasOpenUndoableAction())
								UndoManager.getInstance().startUndoableAction(
										"Adding CodeBlock to "
												+ scriptIt.getDisplayText());

							final CodeBlock codeBlock;

							codeBlock = new CodeBlockSource(
									((LibraryModel) SEModelManager
											.getInstance().getActiveModel())
											.getNextCodeBlockID());

							scriptIt.addCodeBlock(codeBlock);

							UndoManager.getInstance().endUndoableAction();
						}
					});
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
			public void processBehaviour(Behaviour behaviour) {
				this.defaultProcess(behaviour);

				this.pane.add(LibraryEditorPanelFactory.getInstance()
						.buildBehaviourEditingPanel(behaviour));

				this.pane.revalidate();
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

				final DescribeIt describeIt;

				knowItPanel = new JPanel();

				knowItPanelLayout = new GroupLayout(knowItPanel);
				typeAction = new TypeAction();
				typesButton = new JButton(typeAction);

				describeIt = knowIt.getLibrary().getDescribeIt(knowIt);

				nameField = new JTextField(describeIt.getName());

				nameLabel = new JLabel("Name: ");
				typesLabel = new JLabel("Types: ");

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

				typesLabel.setFont(LibraryEditorPanelFactory.labelFont);
				nameLabel.setFont(LibraryEditorPanelFactory.labelFont);

				typeAction.getTypeSelectionDialogBuilder().deselectAll();
				typeAction.getTypeSelectionDialogBuilder()
						.selectTypesByKeyword(knowIt.getTypes(), true);

				WidgetDecorator.decorateJTextFieldForFocusEvents(nameField,
						commitText, false);

				nameField.setHorizontalAlignment(JTextField.LEADING);

				knowItPanel.setBorder(BorderFactory
						.createTitledBorder("DescribeIt"));

				typeAction.setAction(new Runnable() {
					@Override
					public void run() {
						final Collection<String> types = typeAction
								.getTypeSelectionDialogBuilder()
								.getSelectedTypeKeywords();

						// Important: DescribeIt types MUST be set first because
						// KnowIts notify observers when their's are changed,
						// throwing NullPointExceptions everywhere!
						describeIt.setTypes(types);

						knowIt.setTypes(types);
					}
				});

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

				knowItPanelLayout.setVerticalGroup(knowItPanelLayout
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
				this.pane.removeAll();

				this.pane.add(LibraryEditorPanelFactory.getInstance()
						.buildDescriptorPanel(component));

				this.pane.revalidate();
			}
		};
	}

	@Override
	public void componentSelected(StoryComponent component) {
		if (component == null) {
			this.removeAll();
			this.revalidate();
		} else {
			component.process(this.panelBuilder);
		}
	}
}
