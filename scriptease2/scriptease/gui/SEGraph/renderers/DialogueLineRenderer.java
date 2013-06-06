package scriptease.gui.SEGraph.renderers;

import java.awt.Dimension;
import java.awt.Font;

import javax.swing.GroupLayout;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import scriptease.gui.SEGraph.SEGraph;
import scriptease.gui.component.ScriptWidgetFactory;
import scriptease.model.atomic.KnowIt;
import scriptease.model.semodel.SEModelManager;
import scriptease.model.semodel.dialogue.DialogueLine;
import scriptease.model.semodel.librarymodel.LibraryModel;
import scriptease.translator.TranslatorManager;
import scriptease.translator.io.model.Resource;

/**
 * Draws an editor inside the node to edit {@link DialogueLine}s
 * 
 * @author kschenk
 * 
 */
public class DialogueLineRenderer extends SEGraphNodeRenderer<DialogueLine> {

	public DialogueLineRenderer(SEGraph<DialogueLine> graph) {
		super(graph);
	}

	@Override
	protected void configureInternalComponents(JComponent component,
			DialogueLine node) {
		final JTextArea dialogueArea;
		final JScrollPane dialogueScrollPane;

		final KnowIt audioKnowIt;
		final KnowIt imageKnowIt;
		final JComponent audioPanel;
		final JComponent imagePanel;
		final LibraryModel library;

		final JCheckBox enabledBox;

		final GroupLayout layout;

		dialogueArea = new JTextArea(node.getDialogue());
		dialogueScrollPane = new JScrollPane(dialogueArea);

		audioKnowIt = new KnowIt("Audio", node.getAudioTypes());
		imageKnowIt = new KnowIt("Image", node.getImageTypes());

		audioPanel = ScriptWidgetFactory.buildSlotPanel(audioKnowIt, false);
		imagePanel = ScriptWidgetFactory.buildSlotPanel(imageKnowIt, false);

		library = TranslatorManager.getInstance().getActiveDefaultLibrary();
		
		enabledBox = new JCheckBox("Enabled", node.isEnabled());

		layout = new GroupLayout(component);

		//audioKnowIt.setLibrary(library);
		//imageKnowIt.setLibrary(library);
		
		component.setLayout(layout);

		layout.setAutoCreateContainerGaps(true);
		layout.setAutoCreateGaps(true);

		layout.setHorizontalGroup(layout
				.createParallelGroup()
				.addComponent(dialogueScrollPane)
				.addGroup(
						layout.createSequentialGroup().addComponent(enabledBox)
								.addComponent(audioPanel)
								.addComponent(imagePanel)));

		// vertical perspective
		layout.setVerticalGroup(layout
				.createSequentialGroup()
				.addComponent(dialogueScrollPane)
				.addGroup(
						layout.createParallelGroup().addComponent(enabledBox)
								.addComponent(audioPanel)
								.addComponent(imagePanel)));

		final Resource audio;
		final Resource image;

		audio = node.getAudio();
		image = node.getImage();

		if (audio != null)
			audioKnowIt.setBinding(audio);

		if (image != null)
			imageKnowIt.setBinding(image);

		enabledBox.setOpaque(false);
		dialogueArea.setLineWrap(true);
		dialogueArea.setWrapStyleWord(true);
		dialogueArea.setFont(new Font("SansSerif", Font.PLAIN, 12));
		dialogueScrollPane.setPreferredSize(new Dimension(200, 75));
	}
}
