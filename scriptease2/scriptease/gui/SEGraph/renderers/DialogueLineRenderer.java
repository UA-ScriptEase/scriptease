package scriptease.gui.SEGraph.renderers;

import java.awt.Dimension;
import java.awt.Font;

import javax.swing.GroupLayout;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import scriptease.gui.SEGraph.SEGraph;
import scriptease.gui.component.SlotPanel;
import scriptease.model.atomic.KnowIt;
import scriptease.model.semodel.dialogue.DialogueLine;

public class DialogueLineRenderer extends SEGraphNodeRenderer<DialogueLine> {

	public DialogueLineRenderer(SEGraph<DialogueLine> graph) {
		super(graph);
	}

	@Override
	protected void configureInternalComponents(JComponent component,
			DialogueLine node) {
		final JTextArea dialogueArea;
		final JScrollPane dialogueScrollPane;

		final SlotPanel audioPanel;
		final SlotPanel imagePanel;

		final JCheckBox enabledBox;

		final GroupLayout layout;

		dialogueArea = new JTextArea(node.getDialogue());
		dialogueScrollPane = new JScrollPane(dialogueArea);

		audioPanel = new SlotPanel(new KnowIt("Audio"), false);
		imagePanel = new SlotPanel(new KnowIt("Image"), false);

		enabledBox = new JCheckBox("Enabled", node.isEnabled());

		layout = new GroupLayout(component);

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

		enabledBox.setOpaque(false);
		dialogueArea.setLineWrap(true);
		dialogueArea.setWrapStyleWord(true);
		dialogueArea.setFont(new Font("SansSerif", Font.PLAIN, 12));
		dialogueScrollPane.setPreferredSize(new Dimension(200, 75));
	}
}
