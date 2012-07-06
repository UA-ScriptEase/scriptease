package scriptease.gui.storycomponentbuilder;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Observable;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.border.TitledBorder;

import scriptease.gui.storycomponentbuilder.StoryComponentBindingList.BindingContext;
import scriptease.gui.storycomponentbuilder.StoryComponentMultiSelector.MultiSelectorContext;
import scriptease.model.CodeBlock;
import scriptease.model.atomic.KnowIt;
import scriptease.model.complex.ScriptIt;
import scriptease.translator.Translator;
import scriptease.translator.TranslatorManager;
import scriptease.translator.codegenerator.code.fragments.FormatFragment;

@SuppressWarnings("serial")
public class StoryComponentScriptItEditor extends
		StoryComponentDescriptorTemplate implements ActionListener {
	private Collection<CodeBlockEditor> codeBlockEditors;

	public StoryComponentScriptItEditor(ScriptIt scriptIt,
			ComponentContext contextOfComp) {
		super(scriptIt, contextOfComp);

		// build codeBlock editors for each CodeBlock in the ScriptIt
		final Collection<CodeBlock> codeBlocks = scriptIt.getCodeBlocks();
		this.codeBlockEditors = new ArrayList<CodeBlockEditor>(
				codeBlocks.size());
		for (CodeBlock codeBlock : codeBlocks) {
			CodeBlockEditor codeBlockEditor = new CodeBlockEditor(codeBlock);
			codeBlockEditors.add(codeBlockEditor);
			add(codeBlockEditor);
		}

		add(Box.createVerticalGlue());
		add(savecancelPanel);
	}

	@Override
	public void checkIfUpdatable() {
		if (component.getDisplayText() != "")
			setUpdatableStoryComponent();
	}

	@Override
	public void setActionButtonForParameters(ActionListener e) {
	}

	@Override
	public ScriptIt getStoryComponent() {
		return (ScriptIt) this.component;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
	}

	public void updateCodeBlockParameterList(ArrayList<String> rg,
			CodeBlock codeBlock) {
		Collection<KnowIt> knowIts = new ArrayList<KnowIt>();
		for (int i = 0; i < rg.size(); i++) {
			knowIts.add(new KnowIt(rg.get(i)));
		}

		codeBlock.setParameters(knowIts);

		// Find and update the appropriate CodeBlockEditor (TODO replace with
		// observers on parameters)
		for (CodeBlockEditor editor : this.codeBlockEditors) {
			CodeBlock editorBlock = editor.getCodeBlock();
			if (editorBlock == codeBlock)
				editor.updateDisplay();
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public void update(Observable o, Object arg) {
		final ScriptIt scriptIt = getStoryComponent();

		if (o instanceof StoryComponentTextField) {
			if (arg == null) {
				scriptIt.setDisplayText(((StoryComponentTextField) o)
						.getNameComp());
			} else {
				// Default to the main code block, as it will be the
				// common case?
				final CodeBlock mainCodeBlock = scriptIt.getMainCodeBlock();
				this.updateCodeBlockParameterList((ArrayList<String>) arg,
						mainCodeBlock);
				scriptIt.setDisplayText(((StoryComponentTextField) o)
						.getNameComp());
			}
		}

		if (o instanceof StoryComponentMultiSelector) {
			ArrayList<String> addMeToStuff = (ArrayList<String>) arg;
			if (((StoryComponentMultiSelector) o).getSelectorContext() == MultiSelectorContext.TYPES) {
				this.getStoryComponent().getMainCodeBlock()
						.setTypes(addMeToStuff);

			}
		}

		if (o instanceof LabelField) {
			if (scriptIt.getLabels().size() > 0)
				scriptIt.removeLabel(((ArrayList<String>) component.getLabels())
						.get(0));
			scriptIt.addLabel(((LabelField) o).getLabelText());

		}

	}

	private boolean componentValueCheck() {
		if ((this.getStoryComponent().getTypes().size() > 0)
				&& (component.getDisplayText().length() > 0)
				&& (component.getLabels().size() > 0))
			return true;
		return false;
	}

	@Override
	protected void setUpdatableStoryComponent() {
		final ScriptIt scriptIt = this.getStoryComponent();

		nameField.setText(scriptIt.getDisplayText());

		for (String label : scriptIt.getLabels()) {
			setLabelField(label);
		}

		getTypeSelector().setData((ArrayList<String>) scriptIt.getTypes());

		// Update the display of the CodeBlockEditors to match the current state
		for (CodeBlockEditor codeBlockEditor : codeBlockEditors) {
			codeBlockEditor.updateDisplay();
		}
	}

	private void addCodeToComponent() {
		// Add all of the CodeBlocks' code
		for (CodeBlockEditor codeBlockEditor : codeBlockEditors) {
			final CodeBlock codeBlock = codeBlockEditor.getCodeBlock();
			final Collection<FormatFragment> codeFragments = codeBlockEditor
					.getCodeFragments();

			final Translator activeTranslator = TranslatorManager.getInstance()
					.getActiveTranslator();

			activeTranslator.setCode(codeBlock, codeFragments);
		}
	}

	@Override
	public boolean saveComponent() {
		if (!componentValueCheck()) {
			JOptionPane.showMessageDialog(this,
					"One or more of the component parts are missing",
					"Missing Component Parameters", JOptionPane.ERROR_MESSAGE);
			return false;
		}
		// Useless options, help force people to make a choice
		final Translator activeTranslator = TranslatorManager.getInstance()
				.getActiveTranslator();
		if (component != null && activeTranslator != null) {
			if (isAnUpdate) {
				addCodeToComponent();
				saveComponentToAPI();
				return true;
			} else {
				addCodeToComponent();
				saveComponentToAPI();
				return true;
			}
		}
		return false;
	}

	@Override
	public void updateComponent() {
		addCodeToComponent();
		// Well lets update components
	}

	@Override
	public void deleteComponent() {
		// TODO Auto-generated method stub

	}

	/**
	 * CodeBlockEditor provides an interface for modifying the provided
	 * codeBlock
	 * 
	 * @author mfchurch
	 * 
	 */
	private class CodeBlockEditor extends JPanel {
		private final String CODE_BLOCK = "CodeBlock";
		private final String CODE = "Code";

		private final CodeInputTextPane codePane;
		private final CodeBlock codeBlock;
		private final StoryComponentBindingList parameterList;

		public CodeBlockEditor(CodeBlock codeBlock) {
			this.codeBlock = codeBlock;
			this.codePane = new CodeInputTextPane();
			this.parameterList = new StoryComponentBindingList(
					(BindingContext.PARAMETER));

			// Content
			this.add(parameterList);
			final JPanel buildCodePanel = this.buildCodePanel();
			this.add(buildCodePanel);

			// Appearance
			this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
			this.setBorder(new TitledBorder(CODE_BLOCK));
			int preferredWidth = Math.max(
					parameterList.getPreferredSize().width,
					buildCodePanel.getPreferredSize().width);
			int preferredHeight = Math.max(
					parameterList.getPreferredSize().height,
					buildCodePanel.getPreferredSize().height);
			this.setPreferredSize(new Dimension(preferredWidth, preferredHeight));
		}

		/**
		 * Gets the codeBlock being edited
		 * 
		 * @return
		 */
		public CodeBlock getCodeBlock() {
			return this.codeBlock;
		}

		/**
		 * Updates the display of the editor to match the current state of the
		 * codeBlock
		 */
		public void updateDisplay() {
			final Translator activeTranslator = TranslatorManager.getInstance()
					.getActiveTranslator();
			final Collection<FormatFragment> codeFragments = activeTranslator
					.getCode(codeBlock);
			if (codeFragments.size() > 0)
				codePane.setCodeFragments(codeFragments);
			parameterList.updateBindingList(codeBlock.getParameters());
		}

		/**
		 * Helper method for building the CodePanel
		 * 
		 * @return
		 */
		private JPanel buildCodePanel() {
			final JPanel codePanel = new JPanel();
			final JLabel label = new JLabel(CODE);

			label.setFont(labelFont);
			JScrollPane codeScrollPane = new JScrollPane(codePane);
			codeScrollPane
					.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
			codePanel.add(label);
			label.setLabelFor(codeScrollPane);
			codePanel.add(codeScrollPane);
			return codePanel;
		}

		/**
		 * Gets the codeFragments from the codePane
		 * 
		 * @return
		 */
		public Collection<FormatFragment> getCodeFragments() {
			return codePane.getCodeFragments();
		}
	}
}