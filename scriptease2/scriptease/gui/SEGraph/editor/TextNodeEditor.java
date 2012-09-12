package scriptease.gui.SEGraph.editor;

import javax.swing.JTextField;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;

import scriptease.gui.SEGraph.nodes.TextNode;

/**
 * JPanel made for editing the fields of a TextNode. Used by DescribeIts.
 * 
 * @author mfchurch
 * @author kschenk
 * 
 */
@SuppressWarnings("serial")
public class TextNodeEditor extends GraphNodeEditor {

	/**
	 * Constructor. Creates a new Text Node Editor that calls
	 * <code>super()</code> to GraphNodeEditor.
	 */
	public TextNodeEditor() {
		super();
	}

	/**
	 * Overriden method that checks if the node is null before creating the
	 * JTextField. If it is null, an invisible JTextField is created.
	 */
	@Override
	protected JTextField getNameField() {
		final JTextField nullField = new JTextField("");
		nullField.setVisible(false);
		if (this.node == null) {
			return nullField;
		}

		final JTextField nameField = new JTextField(FIELD_SIZE);
		nameField.setText(((TextNode) this.node).getText());
		nameField.addCaretListener(new CaretListener() {

			@Override
			public void caretUpdate(CaretEvent arg0) {
				updateText(nameField.getText());
			}
		});
		return nameField;
	}

	/**
	 * Updates the TextNode's text if it differs from the current text
	 * 
	 * @param newText
	 */
	private void updateText(String newText) {
		TextNode textNode = (TextNode) this.node;
		if (!newText.equals(textNode.getText())) {
			// TODO Undo/Redo capture
			textNode.setText(newText);
		}
	}
}
