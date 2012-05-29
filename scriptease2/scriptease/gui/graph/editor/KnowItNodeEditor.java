package scriptease.gui.graph.editor;

import java.util.Collection;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JTextField;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;

import scriptease.gui.action.view.ShowFilterMenuAction;
import scriptease.gui.graph.nodes.GraphNode;
import scriptease.gui.graph.nodes.KnowItNode;
import scriptease.model.atomic.KnowIt;

/**
 * JPanel made for editing the fields of a KnowItNode. Used by DescribeIts.
 * Edits the name, and the types associated with the KnowItNode.
 * 
 * @author mfchurch
 * 
 */
@SuppressWarnings("serial")
public class KnowItNodeEditor extends GraphNodeEditor {

	private JComponent typeField;

	/**
	 * Creates the new KnowItNodeEditor with a blank JButton.
	 */
	public KnowItNodeEditor() {
		super();
		this.typeField = new JButton();
		this.add(typeField);
	}

	/**
	 * Overriden setNode method, which sets up the type filter for the type
	 * field button, which is the button that allows the user to select which
	 * types of story components to allow.
	 */
	@Override
	public void setNode(GraphNode node) {
		super.setNode(node);
		final ShowFilterMenuAction typeFilter = new ShowFilterMenuAction();
		final KnowIt knowIt = ((KnowItNode) node).getKnowIt();
		typeFilter.selectTypes(knowIt.getTypes(), true);
		typeFilter.setSelectionChangedAction(new Runnable() {
			@Override
			public void run() {
				updateKnowItTypes(typeFilter.getAcceptedTypes());
			}
		});
		this.remove(typeField);
		this.typeField = buildField("Types", new JButton(typeFilter));
		this.add(typeField);
	}

	/**
	 * Overriden getNameField method, which checks if the node is null. If it
	 * is, an invisible placeholder JTextField is created until setNode is
	 * called with a proper and well cultured node.
	 */
	@Override
	protected JTextField getNameField() {
		JTextField nullField = new JTextField("");
		nullField.setVisible(false);
		if (node == null) {
			System.out.println("Node is null");

			return nullField;
		}
		final JTextField nameField = new JTextField(FIELD_SIZE);
		nameField.setText(((KnowItNode) node).getKnowIt().getDisplayText());
		nameField.addCaretListener(new CaretListener() {

			@Override
			public void caretUpdate(CaretEvent arg0) {
				updateKnowItName(nameField.getText());
			}
		});
		return nameField;
	}

	/**
	 * Updates the name of the KnowIt to the string passed.
	 * 
	 * @param newText
	 */
	private void updateKnowItName(String newText) {
		KnowItNode knowItNode = (KnowItNode) node;
		KnowIt knowIt = knowItNode.getKnowIt();
		if (!newText.equals(knowIt.getDisplayText())) {
			// TODO: Undo/Redo
			knowIt.setDisplayText(newText);
		}
	};

	/**
	 * Updates the types of the KnowIt to the collection of types passed.
	 * 
	 * @param types
	 */
	private void updateKnowItTypes(Collection<String> types) {
		KnowItNode knowItNode = (KnowItNode) node;
		KnowIt knowIt = knowItNode.getKnowIt();
		if (!knowIt.getTypes().equals(types)) {
			// TODO: Undo/Redo
			knowIt.setTypes(types);
		}
	}
}
