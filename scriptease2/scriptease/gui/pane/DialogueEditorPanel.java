package scriptease.gui.pane;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JToolBar;
import javax.swing.border.EtchedBorder;

import scriptease.gui.SEGraph.SEGraph;
import scriptease.gui.SEGraph.SEGraphFactory;
import scriptease.gui.component.ComponentFactory;
import scriptease.model.semodel.StoryModel;
import scriptease.model.semodel.dialogue.DialogueLine;

/**
 * This panel contains a Dialogue Line Graph.
 * 
 * @author kschenk
 * 
 */
@SuppressWarnings("serial")
public class DialogueEditorPanel extends JPanel {
	private final StoryModel model;
	private DialogueLine dialogueLine;
	private final JButton backToStory;

	/**
	 * Creates a new editor panel for dialogues. To update the dialogue line
	 * it's editing, call {@link #setDialogueLine(DialogueLine)}.
	 * 
	 * @param model
	 * @param backToStory
	 */
	public DialogueEditorPanel(StoryModel model, JButton backToStory) {
		this.model = model;
		this.dialogueLine = null;
		this.backToStory = backToStory;

		this.setLayout(new BorderLayout());
		this.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED));

		if (this.backToStory != null)
			this.add(this.backToStory, BorderLayout.EAST);

	}

	/**
	 * Sets the dialogue line to be edited. A button used to go back to the
	 * story must be passed in.
	 * 
	 * @param dialogueLine
	 * @param backToStory
	 */
	public void setDialogueLine(DialogueLine dialogueLine) {
		final BorderLayout layout = (BorderLayout) this.getLayout();

		final Component center = layout.getLayoutComponent(BorderLayout.CENTER);
		final Component west = layout.getLayoutComponent(BorderLayout.WEST);

		if (center != null)
			this.remove(center);
		if (west != null)
			this.remove(west);

		this.dialogueLine = dialogueLine;

		if (dialogueLine == null)
			return;

		final SEGraph<DialogueLine> graph;
		final JToolBar graphToolBar;
		final JScrollPane graphScrollPane;

		graph = SEGraphFactory.buildDialogueLineGraph(this.model, dialogueLine);
		graphToolBar = ComponentFactory.buildGraphEditorToolBar();
		graphScrollPane = new JScrollPane(graph);

		graphToolBar.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1,
				Color.LIGHT_GRAY));
		graphScrollPane.setBorder(BorderFactory.createEmptyBorder());

		this.add(graphToolBar, BorderLayout.WEST);
		this.add(graphScrollPane, BorderLayout.CENTER);

		this.repaint();
		this.revalidate();
	}

	/**
	 * Returns the current dialogue line getting edited.
	 * 
	 * @return
	 */
	public DialogueLine getDialogueLine() {
		return this.dialogueLine;
	}
}
