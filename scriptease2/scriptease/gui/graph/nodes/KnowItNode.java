package scriptease.gui.graph.nodes;

import java.awt.FlowLayout;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import scriptease.controller.AbstractNoOpStoryVisitor;
import scriptease.controller.GraphNodeVisitor;
import scriptease.controller.observer.StoryComponentEvent;
import scriptease.controller.observer.StoryComponentObserver;
import scriptease.gui.SETree.cell.ScriptWidgetFactory;
import scriptease.gui.SETree.cell.TypeWidget;
import scriptease.gui.SETree.ui.ScriptEaseUI;
import scriptease.model.StoryComponent;
import scriptease.model.atomic.KnowIt;

public class KnowItNode extends GraphNode implements StoryComponentObserver {
	private KnowIt knowIt;

	public KnowItNode(KnowIt knowIt) {
		super();
		setKnowIt(knowIt);
	}

	public void setKnowIt(KnowIt knowIt) {
		if (this.knowIt != null)
			this.knowIt.removeStoryComponentObserver(this);
		this.knowIt = knowIt;
		this.knowIt.addStoryComponentObserver(this);
	}

	public KnowIt getKnowIt() {
		return this.knowIt;
	}

	@Override
	public KnowItNode clone() {
		KnowItNode clone = (KnowItNode) super.clone();
		clone.setKnowIt(this.knowIt);

		return clone;
	}

	@Override
	public int hashCode() {
		int hash = super.hashCode();
		final KnowIt knowIt = this.getKnowIt();
		for (Character character : knowIt.getDisplayText().toCharArray()) {
			hash += character;
		}
		return hash;
	}

	@Override
	public String toString() {
		return "KnowItNode [" + this.knowIt + "]";
	}

	@Override
	public void process(GraphNodeVisitor processController) {
		processController.processKnowItNode(this);
	}

	@SuppressWarnings("serial")
	private class KnowItNodePanel extends JPanel {
		public KnowItNodePanel() {
			super(new FlowLayout(FlowLayout.CENTER, 2, 0));
			this.setOpaque(true);
			this.setBackground(backgroundColour);
			buildPanel();
		}

		private void buildPanel() {
			if (knowIt != null) {
				JPanel typePanel = new JPanel(new FlowLayout(FlowLayout.CENTER,
						0, 0));
				typePanel.setOpaque(false);
				
				for (String type : knowIt.getAcceptableTypes()) {
					TypeWidget typeWidget = ScriptWidgetFactory
							.buildTypeWidget(type);
					typeWidget.setSelected(true);
					typeWidget.setBackground(ScriptEaseUI.COLOUR_BOUND);
					typePanel.add(typeWidget);
				}

				this.add(typePanel);
				this.add(new JLabel(knowIt.getDisplayText()));
			}
			SwingUtilities.invokeLater(new Runnable() {
				@Override
				public void run() {
					validate();
				}
			});
		}
	}

	@Override
	public void componentChanged(StoryComponentEvent event) {
		StoryComponent source = event.getSource();
		source.process(new AbstractNoOpStoryVisitor() {
			@Override
			public void processKnowIt(KnowIt knowIt) {
				setKnowIt(knowIt);
			}
		});
	}

	@Override
	public boolean represents(Object object) {
		return object == this.knowIt;
	}
}
