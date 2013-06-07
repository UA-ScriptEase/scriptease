package scriptease.gui.SEGraph.renderers;

import java.awt.Dimension;
import java.awt.Font;

import javax.swing.GroupLayout;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import scriptease.gui.SEGraph.SEGraph;
import scriptease.gui.component.ScriptWidgetFactory;
import scriptease.model.atomic.KnowIt;
import scriptease.model.semodel.SEModel;
import scriptease.model.semodel.SEModelManager;
import scriptease.model.semodel.StoryModel;
import scriptease.model.semodel.dialogue.DialogueLine;
import scriptease.translator.io.model.GameModule;
import scriptease.translator.io.model.Resource;
import scriptease.util.StringOp;

/**
 * Draws an editor inside the node to edit {@link DialogueLine}s
 * 
 * @author kschenk
 * 
 */
public class DialogueLineRenderer extends SEGraphNodeRenderer<DialogueLine> {
	final SEGraph<DialogueLine> graph;

	// TODO Everything needs to listen to changes and update the dialogue line
	// appropriately.

	public DialogueLineRenderer(SEGraph<DialogueLine> graph) {
		super(graph);
		this.graph = graph;
	}

	@Override
	protected void configureInternalComponents(JComponent component,
			DialogueLine node) {
		if (node == this.graph.getStartNode()) {
			// TODO
			component.add(new JTextField(node.getDialogue()));
		} else {
			this.renderChildNode(component, node);
		}
	}

	private void renderChildNode(JComponent component, DialogueLine node) {
		final SEModel model = SEModelManager.getInstance().getActiveModel();

		if (!(model instanceof StoryModel))
			throw new IllegalArgumentException(
					"Why are we editing dialogue when a story model is "
							+ "not active!? The active model is " + model);

		final JTextArea dialogueArea;
		final JScrollPane dialogueScrollPane;
		final StoryModel story;
		final GameModule module;

		final JComponent audioPanel;
		final JComponent imagePanel;

		final JCheckBox enabledBox;

		final GroupLayout layout;

		dialogueArea = new JTextArea(node.getDialogue());
		dialogueScrollPane = new JScrollPane(dialogueArea);
		story = (StoryModel) model;
		module = story.getModule();

		imagePanel = this.createSlotForType(module.getImageType(), node);
		audioPanel = this.createSlotForType(module.getAudioType(), node);

		enabledBox = new JCheckBox("Enabled", node.isEnabled());

		layout = new GroupLayout(component);

		component.setLayout(layout);

		enabledBox.setOpaque(false);
		dialogueArea.setLineWrap(true);
		dialogueArea.setWrapStyleWord(true);
		dialogueArea.setFont(new Font("SansSerif", Font.PLAIN, 12));
		dialogueScrollPane.setPreferredSize(new Dimension(200, 75));

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
	}

	private JComponent createSlotForType(String type, DialogueLine node) {
		final JComponent audioPanel;

		if (StringOp.exists(type)) {
			final KnowIt audioKnowIt;
			final Resource audio;

			audioKnowIt = new KnowIt(StringOp.toProperCase(type), type);
			audioPanel = ScriptWidgetFactory.buildSlotPanel(audioKnowIt, false);
			audio = node.getAudio();

			if (audio != null)
				audioKnowIt.setBinding(audio);
		} else
			audioPanel = new JPanel();

		return audioPanel;
	}
}
