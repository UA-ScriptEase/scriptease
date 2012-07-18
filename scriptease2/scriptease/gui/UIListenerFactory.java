package scriptease.gui;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;

import javax.swing.JPanel;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;

import scriptease.controller.StoryVisitor;
import scriptease.controller.io.FileIO;
import scriptease.gui.storycomponentpanel.StoryComponentPanel;
import scriptease.model.CodeBlock;
import scriptease.model.StoryComponent;
import scriptease.model.atomic.KnowIt;
import scriptease.model.complex.ScriptIt;
import scriptease.translator.Translator;
import scriptease.translator.TranslatorManager;

public class UIListenerFactory {

	private static UIListenerFactory instance = new UIListenerFactory();

	/**
	 * Returns the sole instance of the UIListenerFactory.
	 * 
	 * @return
	 */
	public static UIListenerFactory getInstance() {
		return instance;
	}

	/*
	 * This method was in StoryComponentSplitPane on the save button. It called
	 * the specific "savecomponent" method, which depended on if we were editing
	 * a scriptit or a knowit.
	 * 
	 * 
	 * private void saveAction(JButton button) {
	 * 
	 * if (saveComponent()) {
	 * 
	 * // TODO add functionality? Maybe? Or check for exceptions at least?
	 * 
	 * } else WindowManager.getInstance().showWarningDialog(SCB_WARNING,
	 * "Unable to save");
	 * 
	 * }
	 */

	/**
	 * Builds a tree selection listener for the StoryComponentLibrary. It may be
	 * possible to use this in the future to make a general
	 * TreeSelectionListener for more LibraryPanes.
	 * 
	 * @parma storyVisitor The StoryVisitor that determines action when a
	 *        specific story component is selected.
	 * @return
	 */
	public TreeSelectionListener buildStoryComponentLibraryListener(
			final StoryVisitor storyVisitor) {
		TreeSelectionListener libraryListener = new TreeSelectionListener() {

			@Override
			public void valueChanged(TreeSelectionEvent e) {
				if (e.getSource() instanceof StoryComponentPanel) {
					final StoryComponentPanel componentPanel;
					final StoryComponent component;

					componentPanel = (StoryComponentPanel) e.getSource();
					component = componentPanel.getStoryComponent();

					component.process(storyVisitor);
				} else {
					throw new ClassCastException(
							"An Object not of type StoryComponentPanel was found in the tree.");
				}
			}
		};

		return libraryListener;
	}

	/*
	 * This method was in the StoryComponentDescriptorTemplate class. It saved a
	 * component that was passed in the constructor of the class.
	 * 
	 * 
	 * protected void saveComponentToAPI() { Translator activeTranslator =
	 * TranslatorManager.getInstance() .getActiveTranslator();
	 * activeTranslator.getApiDictionary().getLibrary().add(component); File
	 * filePath = activeTranslator
	 * .getPathProperty(Translator.DescriptionKeys.API_DICTIONARY_PATH
	 * .toString()); FileIO.getInstance().writeAPIDictionary(
	 * activeTranslator.getApiDictionary(), filePath); }
	 */

	// EffectPanel Listeners

	// TODO Add this as a listener on the parameter list.
	public void updateCodeBlockParameterList(ArrayList<String> newKnowItNames,
			CodeBlock codeBlock, Collection<JPanel> codeBlockEditors) {
		Collection<KnowIt> knowIts = new ArrayList<KnowIt>();
		for (int i = 0; i < newKnowItNames.size(); i++) {
			knowIts.add(new KnowIt(newKnowItNames.get(i)));
		}

		codeBlock.setParameters(knowIts);

		// Find and update the appropriate CodeBlockEditor (TODO replace with
		// observers on parameters)
		/*
		 * Figure this out,too.
		 * 
		 * for (JPanel editor : codeBlockEditors) { CodeBlock editorBlock =
		 * editor.getCodeBlock(); if (editorBlock == codeBlock)
		 * editor.updateDisplay(); }
		 */
	}

	public boolean saveComponent(ScriptIt scriptIt,
			Collection<JPanel> codeBlockEditors) {
		boolean componentValueCheck = scriptIt.getTypes().size() > 0
				&& scriptIt.getDisplayText().length() > 0
				&& scriptIt.getLabels().size() > 0;

		if (!componentValueCheck) {
			WindowManager.getInstance().showWarningDialog(
					"Story Component Builder Error",
					"One or more of the component parts are missing.");
			return false;
		}
		// Useless options, help force people to make a choice
		final Translator activeTranslator = TranslatorManager.getInstance()
				.getActiveTranslator();
		if (scriptIt != null && activeTranslator != null) {

			/*
			 * Figure this out.
			 * 
			 * 
			 * for (JPanel codeBlockEditor : codeBlockEditors) { final CodeBlock
			 * codeBlock = codeBlockEditor.getCodeBlock(); final
			 * Collection<FormatFragment> codeFragments = codeBlockEditor
			 * .getCodeFragments();
			 * 
			 * codeBlock.setCode(codeFragments); }
			 */
			return true;
		}
		return false;
	}

	// DescribeItPanel Listeners:

	public boolean saveComponent(KnowIt knowIt) {
		Translator activeTranslator = TranslatorManager.getInstance()
				.getActiveTranslator();
		if (knowIt != null && activeTranslator != null) {
			activeTranslator.getApiDictionary().getLibrary().add(knowIt);
			File filePath = activeTranslator
					.getPathProperty(Translator.DescriptionKeys.API_DICTIONARY_PATH
							.toString());

			FileIO.getInstance().writeAPIDictionary(
					activeTranslator.getApiDictionary(), filePath);

			return true;
		}
		return false;
	}

}
