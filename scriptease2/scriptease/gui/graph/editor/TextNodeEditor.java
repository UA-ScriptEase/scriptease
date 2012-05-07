package scriptease.gui.graph.editor;

import java.awt.Container;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

import javax.swing.JTextField;

import scriptease.gui.graph.nodes.TextNode;

/**
 * JPanel made for editing the fields of a TextNode. Used by DescribeIts.
 * @author mfchurch
 *
 */
@SuppressWarnings("serial")
public class TextNodeEditor extends GraphNodeEditor {

	public TextNodeEditor(TextNode node) {
		super(node);
	}

	@Override
	protected JTextField getNameField() {
		final JTextField nameField = new JTextField(FIELD_SIZE);
		nameField.setText(((TextNode) node).getText());
		nameField.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				int key = e.getKeyCode();
				if (key == KeyEvent.VK_ENTER) {
					// Change focus
					Container parent = getParent();
					if (parent != null)
						parent.requestFocusInWindow();
				}
			}
		});
		nameField.addFocusListener(new FocusListener() {
			@Override
			public void focusGained(FocusEvent e) {
			}

			@Override
			public void focusLost(FocusEvent e) {
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
		TextNode textNode = (TextNode) node;
		if (!newText.equals(textNode.getText())) {
			// TODO Undo/Redo capture
			textNode.setText(newText);
		}
	}
}
