package scriptease.gui.SEGraph.nodes;

import java.awt.Font;

import scriptease.controller.GraphNodeVisitor;

/**
 * @deprecated This needs to be removed. We have SEGraph now, which is more
 *             coder-friendly and does more things.
 */
public class TextNode extends GraphNode {
	protected String text;
	protected int bolded;

	public TextNode(String text) {
		super();
		this.text = text;
	}

	public void setText(String text) {
		this.text = text;
	}

	public String getText() {
		return this.text;
	}

	/**
	 * Bolds the label if given true, otherwise makes it plain
	 * 
	 * @param shouldBold
	 */
	public void setBoldStatus(boolean shouldBold) {
		if (shouldBold)
			this.bolded = Font.BOLD;
		else
			this.bolded = Font.PLAIN;
	}

	public int getBoldStatus() {
		return this.bolded;
	}

	@Override
	public TextNode clone() {
		TextNode clone = (TextNode) super.clone();
		clone.setText(this.text);
		return clone;
	}

	@Override
	public int hashCode() {
		int hash = super.hashCode();
		for (Character character : this.getText().toCharArray())
			hash += character;
		return hash;
	}

	@Override
	public String toString() {
		String toString = "TextNode [" + this.text + "]";
		return toString;
	}

	@Override
	public void process(GraphNodeVisitor processController) {
		processController.processTextNode(this);
	}

}
