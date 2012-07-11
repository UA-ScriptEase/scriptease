package scriptease.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Vector;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JToolBar;
import javax.swing.SpringLayout;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.TreeSelectionListener;

import scriptease.ScriptEase;
import scriptease.controller.AbstractNoOpStoryVisitor;
import scriptease.controller.StoryVisitor;
import scriptease.controller.VisibilityManager;
import scriptease.gui.action.ToolBarButtonAction;
import scriptease.gui.action.ToolBarButtonAction.ToolBarButtonMode;
import scriptease.gui.graph.GraphPanel;
import scriptease.gui.graph.nodes.GraphNode;
import scriptease.gui.pane.GameObjectPane;
import scriptease.gui.pane.LibraryPane;
import scriptease.gui.storycomponentbuilder.CodeInputTextPane;
import scriptease.gui.storycomponentbuilder.StoryComponentBindingList;
import scriptease.gui.storycomponentbuilder.StoryComponentBindingList.BindingContext;
import scriptease.gui.storycomponentbuilder.StoryComponentMultiSelector;
import scriptease.model.CodeBlock;
import scriptease.model.StoryComponent;
import scriptease.model.StoryModel;
import scriptease.model.atomic.DescribeIt;
import scriptease.model.atomic.KnowIt;
import scriptease.model.complex.AskIt;
import scriptease.model.complex.ScriptIt;
import scriptease.model.complex.StoryComponentContainer;
import scriptease.translator.Translator;
import scriptease.translator.TranslatorManager;
import scriptease.translator.codegenerator.GameObjectPicker;
import scriptease.util.SpringUtilities;

/**
 * A class for creating different panels. Right now, the commonality between
 * these panels is that they have a ToolBar and a GraphPanel.
 * 
 * @author kschenk
 * 
 */
public class PanelFactory {
	private static PanelFactory instance = new PanelFactory();

	public static PanelFactory getInstance() {
		return instance;
	}

	/**
	 * Creates a panel for editing Quests.
	 * 
	 * @param start
	 *            Start Point of the graph.
	 * @return
	 */
	public JPanel buildQuestPanel(final GraphNode start) {
		final JPanel questPanel = new JPanel(new BorderLayout(), true);
		final GraphPanel graphPanel = new GraphPanel(start);

		ToolBarButtonAction.addJComponent(graphPanel);

		final JToolBar graphToolBar = ToolBarFactory
				.buildGraphEditorToolBar(graphPanel);
		final JToolBar questToolBar = ToolBarFactory
				.buildQuestEditorToolBar(graphPanel);

		questPanel.add(graphToolBar.add(questToolBar), BorderLayout.PAGE_START);

		ToolBarButtonAction.setMode(ToolBarButtonMode.SELECT_GRAPH_NODE);

		questPanel.add(new JScrollPane(graphPanel), BorderLayout.CENTER);

		return questPanel;
	}

	/**
	 * Creates a panel for editing DescribeIts.
	 * 
	 * @param start
	 *            Start Point of the graph
	 * @return
	 */
	public JPanel buildDescribeItPanel(final GraphNode start,
			final DescribeIt describeIt) {
		final JPanel describeItPanel = new JPanel(new BorderLayout(), true);
		final GraphPanel graphPanel = new GraphPanel(start);

		DescribeIt editedDescribeIt = describeIt.clone();
		editedDescribeIt.clearSelection();

		graphPanel.setHeadNode(editedDescribeIt.getHeadNode());

		ToolBarButtonAction.addJComponent(graphPanel);

		final JToolBar graphToolBar = ToolBarFactory
				.buildGraphEditorToolBar(graphPanel);

		final JToolBar describeItToolBar = ToolBarFactory
				.buildDescribeItToolBar(editedDescribeIt, graphPanel);

		describeItPanel.add(graphToolBar.add(describeItToolBar),
				BorderLayout.PAGE_START);

		ToolBarButtonAction.setMode(ToolBarButtonMode.SELECT_GRAPH_NODE);

		describeItPanel.add(new JScrollPane(graphPanel), BorderLayout.CENTER);

		return describeItPanel;
	}

	/**
	 * Builds a pane containing all game objects in the active module, organized
	 * by category, allowing the user to drag them onto bindings in a Story.
	 * 
	 * @return A JPanel GameObject picker.
	 * @author graves
	 * @author mfchurch
	 */
	public JPanel buildGameObjectPane(StoryModel model) {
		GameObjectPicker picker;

		if (model != null) {
			Translator translator = model.getTranslator();
			if (translator != null) {
				// Get the picker
				if ((picker = translator.getCustomGameObjectPicker()) == null) {
					picker = new GameObjectPane();
				}
				return picker.getPickerPanel();
			}
		}
		// otherwise return an empty hidden JPanel
		JPanel jPanel = new JPanel();
		jPanel.setVisible(false);
		return jPanel;
	}

	/**
	 * Creates a new JPanel with nothing inside except a JLabel of the passed
	 * text.
	 * 
	 * @param text
	 * @return
	 */
	private JPanel buildEmptyPanelWithText(String text) {
		final JPanel nullPanel;
		final JLabel nullLabel;

		nullPanel = new JPanel();
		nullLabel = new JLabel(text);

		nullPanel.add(nullLabel);

		return nullPanel;
	}

	/**
	 * Builds an empty StoryComponentEditorPanel.
	 * 
	 * @return
	 */
	public JPanel buildStoryComponentEditorPanel() {
		final JPanel editorPanel;

		editorPanel = new JPanel();

		editorPanel.setLayout(new BorderLayout());
		editorPanel.add(buildStoryComponentEditorPanelContents(null),
				BorderLayout.CENTER);

		return editorPanel;
	}

	/**
	 * Creates a JPanel with fields for Name, Types, Labels, and a check box for
	 * Visibility. This JPanel is common to all story component editor panes.
	 * 
	 * @return
	 */
	private JPanel buildStoryComponentEditorPanelContents(
			StoryComponent storyComponent) {

		// TODO Add in the Save and Cancel buttons here, too.
		// I'm not sure what SpringLayout looks like, so I'll hold off
		// on those buttons until I get SE running again.

		if (storyComponent == null) {
			return buildEmptyPanelWithText("Select a Story Component to begin editing it.");
		}

		final JPanel editorPanel;
		final JPanel descriptorPanel;

		final JLabel nameLabel;
		final JLabel typeLabel;
		final JLabel labelLabel;
		final JLabel visibleLabel;

		final JTextField nameField;
		// TODO Refactor the StoryComponentMultiSelector
		final StoryComponentMultiSelector typeSelector;
		final JButton typeButton;
		final JTextField labelField;
		final JCheckBox visibleBox;

		final Font labelFont;

		editorPanel = new JPanel();
		descriptorPanel = new JPanel();

		nameLabel = new JLabel("Name: ");
		typeLabel = new JLabel("Types: ");
		labelLabel = new JLabel("Labels: ");
		visibleLabel = new JLabel("Visible: ");

		nameField = new JTextField();
		typeSelector = new StoryComponentMultiSelector();
		typeButton = typeSelector.getRootButton();
		labelField = new JTextField();
		visibleBox = new JCheckBox();

		labelFont = new Font("Helvetica", Font.BOLD,
				Integer.parseInt(ScriptEase.getInstance().getPreference(
						ScriptEase.FONT_SIZE_KEY)));

		editorPanel.setLayout(new BoxLayout(editorPanel, BoxLayout.PAGE_AXIS));

		// Set up the descriptorPanel

		// TODO Change to GroupLayout instead of SpringLayout!
		
		descriptorPanel.setLayout(new SpringLayout());
		descriptorPanel.setBorder(new LineBorder(Color.DARK_GRAY));
		descriptorPanel.setBorder(new TitledBorder("Component Descriptors"));

		// Set up the labels

		nameLabel.setFont(labelFont);
		nameLabel.setLabelFor(nameField);

		typeLabel.setFont(labelFont);
		typeLabel.setLabelFor(typeButton);

		labelLabel.setFont(labelFont);
		labelLabel.setLabelFor(labelField);

		visibleLabel.setFont(labelFont);
		visibleLabel.setLabelFor(visibleBox);

		// Set up the default field values

		nameField.setText(storyComponent.getDisplayText());

		String label = "";
		for (String storyComponentLabel : storyComponent.getLabels()) {
			label += storyComponentLabel + ", ";
		}
		int labelLength = label.length();
		if (labelLength > 0) {
			String shortenedLabel = label.substring(0, labelLength - 2);
			labelField.setText(shortenedLabel);
		}
		
		visibleBox.setSelected(VisibilityManager.getInstance().isVisible(
				storyComponent));

		// Set up the field listeners

		// Add JComponents to DescriptorPanel

		descriptorPanel.add(visibleLabel);
		descriptorPanel.add(visibleBox);

		descriptorPanel.add(nameLabel);
		descriptorPanel.add(nameField);

		descriptorPanel.add(labelLabel);
		descriptorPanel.add(labelField);

		descriptorPanel.add(typeLabel);
		descriptorPanel.add(typeButton);

		descriptorPanel.setMaximumSize(new Dimension(2000, 150));

		SpringUtilities.makeCompactGrid(descriptorPanel, 4, 2, 6, 6, 6, 6);

		editorPanel.add(descriptorPanel);
		editorPanel.add(Box.createHorizontalGlue());

		StoryVisitor storyComponentVisitor = new AbstractNoOpStoryVisitor() {

			@Override
			public void processScriptIt(ScriptIt scriptIt) {
				ArrayList<String> types = new ArrayList<String>();
				types.addAll(scriptIt.getTypes());
				typeSelector.setData(types);

				editorPanel.add(PanelFactory.getInstance()
						.buildEffectEditorPanel(scriptIt));
			}

			@Override
			public void processKnowIt(KnowIt knowIt) {
				ArrayList<String> types = new ArrayList<String>();
				types.addAll(knowIt.getTypes());
				typeSelector.setData(types);
				
				editorPanel.add(PanelFactory.getInstance()
						.buildDescriptionEditorPanel(knowIt));
			}

			@Override
			public void defaultProcess(StoryComponent component) {
				editorPanel.removeAll();
				editorPanel
						.add(buildEmptyPanelWithText("Select a Story Component to begin editing it."));
			}

			// We may want to implement these later, so their default methods
			// are here in case.
			@Override
			public void processAskIt(AskIt questionIt) {
				// Unimplemented
				this.defaultProcess(questionIt);
			}

			@Override
			public void processStoryComponentContainer(
					StoryComponentContainer container) {
				// Unimplemented
				this.defaultProcess(container);
			}
		};

		storyComponent.process(storyComponentVisitor);

		// TODO Add save/cancel buttons down here, centered.

		return editorPanel;

	}

	private JPanel buildEffectEditorPanel(ScriptIt scriptIt) {
		final JPanel effectEditorPanel;
		final Collection<CodeBlock> codeBlocks;
		final Collection<JPanel> codeBlockEditors;

		effectEditorPanel = new JPanel();
		codeBlocks = scriptIt.getCodeBlocks();

		codeBlockEditors = new ArrayList<JPanel>(codeBlocks.size());

		for (CodeBlock codeBlock : codeBlocks) {
			JPanel codeBlockEditor = buildCodeBlockPanel(codeBlock);
			codeBlockEditors.add(codeBlockEditor);
			effectEditorPanel.add(codeBlockEditor);
		}

		// TODO Set the text, type, and labels like in DescriptionEditor.

		// Also make code block editors:
		// for (JPanel codeBlockEditor : codeBlockEditors) {
		// codeBlockEditor.updateDisplay();
		// }
		effectEditorPanel.setLayout(new BoxLayout(effectEditorPanel,
				BoxLayout.PAGE_AXIS));
		effectEditorPanel.add(Box.createVerticalGlue());

		return effectEditorPanel;
	}

	/*
	 * TODO Will need to implement DescribeItGraphPanel here or just combine
	 * them
	 */
	public JPanel buildDescriptionEditorPanel(KnowIt knowIt) {
		final JPanel descriptionEditorPanel;
		final StoryComponentBindingList bindingList;

		descriptionEditorPanel = new JPanel();
		bindingList = new StoryComponentBindingList(BindingContext.BINDING);

		descriptionEditorPanel.setLayout(new BoxLayout(descriptionEditorPanel,
				BoxLayout.PAGE_AXIS));
		descriptionEditorPanel.add(bindingList);
		descriptionEditorPanel.add(Box.createVerticalGlue());

		// Set text of NameField to knowIt.getDisplayText();
		String labelList = "";
		for (String label : knowIt.getLabels())
			label += label + ", ";
		// Set the Label list to labelList
		// Set the types to knowIt.getTypes()

		return descriptionEditorPanel;
	}

	private JPanel buildCodeBlockPanel(CodeBlock codeBlock) {
		final String CODE_BLOCK = "CodeBlock";
		final String CODE = "Code";
		final JPanel codePanel;
		final JPanel codeBlockPanel;
		final CodeInputTextPane codePane;
		final StoryComponentBindingList parameterList;
		final JScrollPane codeScrollPane;
		final JLabel label;
		final Font labelFont;

		codePane = new CodeInputTextPane();
		parameterList = new StoryComponentBindingList(
				(BindingContext.PARAMETER));
		codeBlockPanel = new JPanel();
		codePanel = new JPanel();
		codeScrollPane = new JScrollPane(codePane.getTextPane());
		label = new JLabel(CODE);
		labelFont = new Font("Helvetica", Font.BOLD,
				Integer.parseInt(ScriptEase.getInstance().getPreference(
						ScriptEase.FONT_SIZE_KEY)));

		codeScrollPane
				.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);

		label.setFont(labelFont);
		label.setLabelFor(codeScrollPane);

		codePanel.add(label);
		codePanel.add(codeScrollPane);

		codeBlockPanel
				.setLayout(new BoxLayout(codeBlockPanel, BoxLayout.Y_AXIS));
		codeBlockPanel.setBorder(new TitledBorder(CODE_BLOCK));
		int preferredWidth = Math.max(parameterList.getPreferredSize().width,
				codePanel.getPreferredSize().width);
		int preferredHeight = Math.max(parameterList.getPreferredSize().height,
				codePanel.getPreferredSize().height);
		codeBlockPanel.setPreferredSize(new Dimension(preferredWidth,
				preferredHeight));

		codeBlockPanel.add(parameterList);
		codeBlockPanel.add(codePanel);

		// TODO Add a listener that updates this panel.
		/*
		 * Old Code: final Collection<FormatFragment> codeFragments =
		 * codeBlock.getCode(); if (codeFragments.size() > 0)
		 * codePane.setCodeFragments(codeFragments);
		 * parameterList.updateBindingList(codeBlock.getParameters());
		 */
		return codeBlockPanel;
	}

	/**
	 * Builds the library pane for the Story Component Editor. This pane allows
	 * loading of different translators through a ComboBox.
	 * 
	 * @param editorPanel
	 * @return
	 */
	public JPanel buildStoryComponentLibraryPanel(JPanel editorPanel) {
		final List<Translator> translators;
		final JComboBox<Translator> libSelector;
		final JPanel libraryPanel;
		final JPanel translatorPanel;
		final LibraryPane libraryPane;
		final Translator activeTranslator;
		final TreeSelectionListener librarySelectionListener;
		final StoryVisitor storyVisitor;

		libraryPanel = new JPanel();
		translatorPanel = new JPanel();
		translators = new ArrayList<Translator>();
		libraryPane = new LibraryPane();
		activeTranslator = TranslatorManager.getInstance()
				.getActiveTranslator();
		storyVisitor = editorPanelUpdater(editorPanel);

		libraryPanel.setLayout(new BoxLayout(libraryPanel, BoxLayout.Y_AXIS));

		translators.add(null);
		translators.addAll(TranslatorManager.getInstance().getTranslators());

		libSelector = new JComboBox<Translator>(new Vector<Translator>(
				translators));
		libSelector.setSelectedItem(activeTranslator);

		// TODO This should update the editorpanel, too!
		libSelector.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				setTranslator((Translator) libSelector.getSelectedItem());
			}
		});

		translatorPanel.add(new JLabel("Currently loaded translator: "));
		translatorPanel.add(libSelector);

		libraryPanel.add(translatorPanel);

		librarySelectionListener = UIListenerFactory.getInstance()
				.buildStoryComponentLibraryListener(storyVisitor);

		// Add the tree listener
		libraryPane.addTreeSelectionListener(librarySelectionListener);

		libraryPanel.add(libraryPane);

		return libraryPanel;
	}

	/**
	 * Creates an AbstractNoOpStoryVisitor which calls an update on the
	 * editorPanel. This is used as a sort of Command Pattern with
	 * UIListenerFactory.
	 * 
	 * @param editorPanel
	 *            The editorPanel to update.
	 * 
	 * @return
	 */
	public StoryVisitor editorPanelUpdater(final JPanel editorPanel) {
		StoryVisitor storyVisitor = new AbstractNoOpStoryVisitor() {
			@Override
			public void defaultProcess(StoryComponent component) {
				editorPanel.removeAll();
				editorPanel.add(
						buildStoryComponentEditorPanelContents(component),
						BorderLayout.CENTER);

				editorPanel.repaint();
				editorPanel.revalidate();
			}
		};
		return storyVisitor;
	}

	/**
	 * Method to set the translator. This is separate so that Progress.aj can
	 * show the loading bar when a new Translator is loading.
	 * 
	 * @author remiller
	 * 
	 * @param t
	 *            The Translator to load
	 */
	private void setTranslator(Translator t) {
		TranslatorManager.getInstance().setActiveTranslator(t);
	}
}
