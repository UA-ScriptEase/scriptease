package scriptease.gui.libraryeditor;

import java.awt.Color;
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
import scriptease.controller.undo.UndoManager;
import scriptease.gui.WidgetDecorator;
import scriptease.gui.action.typemenus.TypeAction;
import scriptease.gui.pane.LibraryPanel;
import scriptease.model.CodeBlock;
import scriptease.model.CodeBlockSource;
import scriptease.model.StoryComponent;
import scriptease.model.atomic.KnowIt;
import scriptease.model.atomic.describeits.DescribeIt;
import scriptease.model.complex.AskIt;
import scriptease.model.complex.CauseIt;
import scriptease.model.complex.FunctionIt;
import scriptease.model.complex.ScriptIt;
import scriptease.model.complex.behaviours.Behaviour;
import scriptease.model.semodel.SEModelManager;
import scriptease.model.semodel.librarymodel.LibraryModel;

/**
 * Creates a Panel dependent on the component being edited in the Library
 * editor.
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
		 * Create an AbstractNoOpStoryVisitor which calls an update on the
		 * editorPanel. This is used as a sort of Command Pattern with
		 * UIListenerFactory.
		 */
		panelBuilder = new StoryAdapter() {

			private Runnable setUpCodeBlockPanels(final ScriptIt scriptIt,
					final JPanel editingPanel) {
				return new Runnable() {
					@Override
					public void run() {
						final Collection<CodeBlock> codeBlocks;
						codeBlocks = scriptIt.getCodeBlocks();

						editingPanel.removeAll();
						FormatFragmentSelectionManager.getInstance()
								.setFormatFragment(null, null);

						for (CodeBlock codeBlock : codeBlocks) {
							editingPanel.add(LibraryEditorPanelFactory
									.getInstance().buildCodeBlockPanel(
											codeBlock, scriptIt));
						}

						editingPanel.revalidate();
					}
				};
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
					LibraryEditorPanel.this.add(scriptItControlPanel);
				}

				LibraryEditorPanel.this.add(codeBlockEditingPanel);

				this.setUpCodeBlockPanels(scriptIt, codeBlockEditingPanel)
						.run();

				scriptIt.addStoryComponentObserver(LibraryEditorListenerFactory
						.getInstance().buildScriptItEditorObserver(
								setUpCodeBlockPanels(scriptIt,
										codeBlockEditingPanel)));

				LibraryEditorPanel.this.revalidate();
			}

			@Override
			public void processFunctionIt(FunctionIt functionIt) {
				this.defaultProcess(functionIt);

				LibraryEditorPanel.this.add(LibraryEditorPanelFactory
						.getInstance().buildFunctionItEditingPanel(functionIt));

				LibraryEditorPanel.this.repaint();
				LibraryEditorPanel.this.revalidate();
			}

			@Override
			public void processBehaviour(Behaviour behaviour) {
				LibraryEditorPanel.this.removeAll();

				LibraryEditorListenerFactory.getInstance()
						.refreshCodeBlockComponentObserverList();
				FormatFragmentSelectionManager.getInstance().setFormatFragment(
						null, null);

				LibraryEditorPanel.this.add(LibraryEditorPanelFactory
						.getInstance().buildBehaviourEditingPanel(behaviour));

				LibraryEditorPanel.this.repaint();
				LibraryEditorPanel.this.revalidate();
			}

			/**
			 * @param knowIt
			 */
			@Override
			public void processKnowIt(final KnowIt knowIt) {
				LibraryEditorPanel.this.removeAll();
				LibraryEditorPanel.this.revalidate();
				LibraryEditorPanel.this.repaint();

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
						commitText, false, Color.white);

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

				LibraryEditorPanel.this.add(knowItPanel);
				LibraryEditorPanel.this.add(describeItEditingPanel);
			}

			// We may want to implement these later, so their default methods
			// are here in case.
			@Override
			public void processAskIt(AskIt questionIt) {
				this.defaultProcess(questionIt);
			}

			@Override
			public void defaultProcess(StoryComponent component) {
				/*
				 * defaultProcess adds a name, type, and visibility fields to
				 * the specified component panel.
				 */
				LibraryEditorListenerFactory.getInstance()
						.refreshCodeBlockComponentObserverList();
				FormatFragmentSelectionManager.getInstance().setFormatFragment(
						null, null);

				LibraryEditorPanel.this.removeAll();

				LibraryEditorPanel.this.add(LibraryEditorPanelFactory
						.getInstance().buildDescriptorPanel(component));

				LibraryEditorPanel.this.revalidate();
				LibraryEditorPanel.this.repaint();
			}
		};
	}

	@Override
	public void componentSelected(StoryComponent component) {
		if (component == null) {
			this.removeAll();
			this.revalidate();
			this.repaint();
		} else {
			component.process(this.panelBuilder);
		}
	}
}
