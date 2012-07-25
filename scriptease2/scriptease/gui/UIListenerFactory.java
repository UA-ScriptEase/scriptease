package scriptease.gui;

import java.util.ArrayList;
import java.util.Collection;

import javax.swing.JPanel;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;

import scriptease.controller.StoryVisitor;
import scriptease.gui.storycomponentpanel.StoryComponentPanel;
import scriptease.model.CodeBlock;
import scriptease.model.StoryComponent;
import scriptease.model.atomic.KnowIt;

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
					
					if (componentPanel.getSelectionManager().getSelectedPanels().size() < 2)
						component.process(storyVisitor);
				} else {
					throw new ClassCastException(
							"An Object not of type StoryComponentPanel was found in the tree.");
				}
			}
		};

		return libraryListener;
	}

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
}