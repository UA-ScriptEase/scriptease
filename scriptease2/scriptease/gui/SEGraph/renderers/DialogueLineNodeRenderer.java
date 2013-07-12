package scriptease.gui.SEGraph.renderers;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.GroupLayout;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import scriptease.controller.observer.storycomponent.StoryComponentEvent;
import scriptease.controller.observer.storycomponent.StoryComponentEvent.StoryComponentChangeEnum;
import scriptease.controller.observer.storycomponent.StoryComponentObserver;
import scriptease.gui.WidgetDecorator;
import scriptease.gui.SEGraph.SEGraph;
import scriptease.gui.component.ScriptWidgetFactory;
import scriptease.model.atomic.KnowIt;
import scriptease.model.atomic.knowitbindings.KnowItBinding;
import scriptease.model.atomic.knowitbindings.KnowItBindingResource;
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
public class DialogueLineNodeRenderer extends SEGraphNodeRenderer<DialogueLine> {
	final SEGraph<DialogueLine> graph;

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

	private void renderStartNode(JComponent component, final DialogueLine node) {
		final JTextField dialogueField;
		final Runnable commitText;

		dialogueField = new JTextField(node.getName());
		commitText = new Runnable() {
			@Override
			public void run() {
				node.setName(dialogueField.getText());

				graph.revalidate();
				graph.repaint();
			}
		};

		WidgetDecorator.decorateJTextFieldForFocusEvents(dialogueField,
				commitText, true, Color.WHITE);

		component.add(dialogueField);
	}

	private void renderChildNode(JComponent component, final DialogueLine node) {
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

		dialogueArea = new JTextArea(node.getName()) {
			@Override
			protected void paintComponent(Graphics g) {
				super.paintComponent(g);

				g.setColor(Color.LIGHT_GRAY);
				g.drawString(node.getSpeaker().name(), this.getWidth() / 2,
						this.getHeight() / 2);
			}
		};
		dialogueScrollPane = new JScrollPane(dialogueArea);
		story = (StoryModel) model;
		module = story.getModule();

		audioPanel = this.createSlot(module.getAudioType(), node.getAudio(),
				new StoryComponentObserver() {
					@Override
					public void componentChanged(StoryComponentEvent event) {
						if (event.getType() == StoryComponentChangeEnum.CHANGE_KNOW_IT_BOUND) {
							node.setAudio(getResourceFromKnowIt((KnowIt) event
									.getSource()));
						}
					}
				});

		imagePanel = this.createSlot(module.getImageType(), node.getImage(),
				new StoryComponentObserver() {
					@Override
					public void componentChanged(StoryComponentEvent event) {
						if (event.getType() == StoryComponentChangeEnum.CHANGE_KNOW_IT_BOUND) {
							final Resource image;

							image = getResourceFromKnowIt((KnowIt) event
									.getSource());
							node.setImage(image);
						}
					}
				});

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

	private Resource getResourceFromKnowIt(KnowIt knowIt) {
		final KnowItBinding binding = knowIt.getBinding();
		final Resource resource;

		if (binding instanceof KnowItBindingResource) {
			resource = ((KnowItBindingResource) binding).getValue();
		} else
			resource = null;

		return resource;
	}

	private JComponent createSlot(String type, Resource resource,
			StoryComponentObserver observer) {
		final JComponent slotPanel;

		if (StringOp.exists(type)) {
			final KnowIt knowIt;

			knowIt = new KnowIt(StringOp.toProperCase(type), type);
			slotPanel = ScriptWidgetFactory.buildSlotPanel(knowIt, false);

			if (resource != null)
				knowIt.setBinding(resource);

			knowIt.addStoryComponentObserver(observer);
		} else
			slotPanel = new JPanel();

		return slotPanel;
	}
}
