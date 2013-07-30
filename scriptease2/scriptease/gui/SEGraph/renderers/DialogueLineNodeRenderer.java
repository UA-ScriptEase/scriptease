package scriptease.gui.SEGraph.renderers;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Collection;

import javax.swing.GroupLayout;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import scriptease.controller.observer.storycomponent.StoryComponentEvent;
import scriptease.controller.observer.storycomponent.StoryComponentEvent.StoryComponentChangeEnum;
import scriptease.controller.observer.storycomponent.StoryComponentObserver;
import scriptease.gui.WidgetDecorator;
import scriptease.gui.SEGraph.SEGraph;
import scriptease.gui.SEGraph.observers.SEGraphAdapter;
import scriptease.gui.SEGraph.observers.SEGraphObserver;
import scriptease.gui.component.ScriptWidgetFactory;
import scriptease.model.atomic.KnowIt;
import scriptease.model.atomic.knowitbindings.KnowItBinding;
import scriptease.model.atomic.knowitbindings.KnowItBindingResource;
import scriptease.model.semodel.SEModel;
import scriptease.model.semodel.SEModelManager;
import scriptease.model.semodel.StoryModel;
import scriptease.model.semodel.dialogue.DialogueLine;
import scriptease.model.semodel.dialogue.DialogueLine.Speaker;
import scriptease.translator.io.model.GameModule;
import scriptease.translator.io.model.Resource;
import scriptease.util.StringOp;

/**
 * Draws an editor inside the node to edit {@link DialogueLine}s
 * 
 * @author kschenk
 * 
 */
public class DialogueLineNodeRenderer extends SEGraphNodeRenderer<DialogueLine> {
	final SEGraph<DialogueLine> graph;

	private static enum SlotType {
		IMAGE, AUDIO;
	}

	public DialogueLineNodeRenderer(SEGraph<DialogueLine> graph) {
		super(graph);
		this.graph = graph;
	}

	@Override
	protected void configureInternalComponents(JComponent component,
			final DialogueLine node) {
		if (node == this.graph.getStartNode()) {
			this.renderStartNode(component, node);
		} else {
			this.renderChildNode(component, node);
		}
	}

	/**
	 * Renders the starting node of a conversation graph.
	 * 
	 * @param component
	 * @param node
	 */
	private void renderStartNode(JComponent component, final DialogueLine node) {
		final JTextField dialogueField = new JTextField(node.getName());

		WidgetDecorator.decorateJTextFieldForFocusEvents(dialogueField,
				new Runnable() {
					@Override
					public void run() {
						node.setName(dialogueField.getText());

						graph.revalidate();
						graph.repaint();
					}
				}, true, Color.WHITE);

		component.add(dialogueField);
	}

	/**
	 * Renders the node for a node that is not the first node in the graph.
	 * 
	 * @param component
	 * @param node
	 */
	private void renderChildNode(final JComponent component,
			final DialogueLine node) {
		final SEModel model = SEModelManager.getInstance().getActiveModel();

		if (!(model instanceof StoryModel))
			throw new IllegalArgumentException(
					"Why are we editing dialogue when a story model is "
							+ "not active!? The active model is " + model);

		final JTextArea dialogueArea;
		final JScrollPane dialogueScrollPane;
		final GameModule module;

		final Speaker speaker;

		final JComponent audioPanel;
		final JComponent imagePanel;

		final GroupLayout layout;
		final JCheckBox enabledBox;
		final JLabel speakerLabel;

		final SEGraphObserver<DialogueLine> selectionObserver;

		dialogueArea = new JTextArea(node.getName());
		dialogueScrollPane = new JScrollPane(dialogueArea);

		speaker = node.getSpeaker();
		module = ((StoryModel) model).getModule();

		speakerLabel = new JLabel();

		audioPanel = this.createSlot(module, node, SlotType.AUDIO);
		imagePanel = this.createSlot(module, node, SlotType.IMAGE);

		enabledBox = new JCheckBox("Enabled", node.isEnabled());

		layout = new GroupLayout(component);

		selectionObserver = new SEGraphAdapter<DialogueLine>() {

			@Override
			public void nodesSelected(Collection<DialogueLine> nodes) {
				if (nodes.contains(node))
					speakerLabel.setForeground(Color.WHITE);
				else
					speakerLabel.setForeground(Color.GRAY);
			}

			@Override
			public void nodeRemoved(DialogueLine removedNode) {
				if (removedNode == node) {
					final SEGraphObserver<DialogueLine> observer = this;
					// Must invoke later to avoid concurrent modifications
					SwingUtilities.invokeLater(new Runnable() {
						@Override
						public void run() {
							graph.removeSEGraphObserver(observer);
						}
					});
				}
			}

		};

		component.setLayout(layout);

		enabledBox.setOpaque(false);

		dialogueArea.setLineWrap(true);
		dialogueArea.setWrapStyleWord(true);
		dialogueArea.setFont(new Font("SansSerif", Font.PLAIN, 12));

		dialogueScrollPane.setPreferredSize(new Dimension(230, 75));

		layout.setAutoCreateContainerGaps(true);
		layout.setAutoCreateGaps(true);

		speakerLabel.setFont(new Font("SansSerif", Font.PLAIN, 18));
		speakerLabel.setText(speaker.toString());
		if (this.graph.getSelectedNodes().contains(node))
			speakerLabel.setForeground(Color.WHITE);
		else
			speakerLabel.setForeground(Color.GRAY);

		this.graph.addSEGraphObserver(selectionObserver);

		dialogueArea.getDocument().addDocumentListener(new DocumentListener() {

			@Override
			public void insertUpdate(DocumentEvent e) {
				node.setName(dialogueArea.getText());
			}

			@Override
			public void removeUpdate(DocumentEvent e) {
				this.insertUpdate(e);
			}

			@Override
			public void changedUpdate(DocumentEvent e) {
				this.insertUpdate(e);
			}
		});

		enabledBox.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				node.setEnabled(enabledBox.isSelected());
			}
		});

		layout.setHorizontalGroup(layout
				.createParallelGroup()
				.addComponent(speakerLabel)
				.addComponent(dialogueScrollPane)
				.addGroup(
						layout.createSequentialGroup().addComponent(enabledBox)
								.addComponent(audioPanel)
								.addComponent(imagePanel)));

		layout.setVerticalGroup(layout
				.createSequentialGroup()
				.addComponent(speakerLabel)
				.addComponent(dialogueScrollPane)
				.addGroup(
						layout.createParallelGroup().addComponent(enabledBox)
								.addComponent(audioPanel)
								.addComponent(imagePanel)));
	}

	/**
	 * Creates a slot that depends on the {@link SlotType} whether it is
	 * {@link SlotType#AUDIO} or {@link SlotType#IMAGE}.
	 * 
	 * @param module
	 * @param line
	 * @param slotType
	 * @return
	 */
	private JComponent createSlot(GameModule module, final DialogueLine line,
			final SlotType slotType) {
		final JComponent slotPanel;
		final String type;
		final KnowIt knowIt;

		if (slotType == SlotType.AUDIO) {
			type = module.getAudioType();
			knowIt = line.getAudio();
		} else if (slotType == SlotType.IMAGE) {
			type = module.getImageType();
			knowIt = line.getImage();
		} else {
			type = "";
			// We never reach a point where the knowIt is used at this point.
			knowIt = null;
		}

		if (StringOp.exists(type)) {
			slotPanel = ScriptWidgetFactory.buildSlotPanel(knowIt, false);
		} else
			slotPanel = new JPanel();

		return slotPanel;
	}
}
