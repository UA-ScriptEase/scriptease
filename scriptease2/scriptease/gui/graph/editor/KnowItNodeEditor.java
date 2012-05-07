package scriptease.gui.graph.editor;

import java.awt.Container;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.Collection;

import javax.swing.JButton;
import javax.swing.JTextField;

import scriptease.gui.action.view.ShowFilterMenuAction;
import scriptease.gui.graph.nodes.KnowItNode;
import scriptease.model.atomic.KnowIt;

/**
 * JPanel made for editing the fields of a KnowItNode. Used by DescribeIts.
 * @author mfchurch
 *
 */
@SuppressWarnings("serial")
public class KnowItNodeEditor extends GraphNodeEditor {

	public KnowItNodeEditor(KnowItNode knowItNode) {
		super(knowItNode);
		final ShowFilterMenuAction typeFilter = new ShowFilterMenuAction();
		final KnowIt knowIt = ((KnowItNode) node).getKnowIt();
		typeFilter.selectTypes(knowIt.getTypes(), true);
		typeFilter.setSelectionChangedAction(new Runnable() {
			@Override
			public void run() {
				updateKnowItTypes(typeFilter.getAcceptedTypes());
			}
		});
		this.add(buildField("Types", new JButton(typeFilter)));
	}

	@Override
	protected JTextField getNameField() {
		final JTextField nameField = new JTextField(FIELD_SIZE);
		nameField.setText(((KnowItNode) node).getKnowIt().getDisplayText());
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
				updateKnowItName(nameField.getText());
			}
		});
		return nameField;
	}

	private void updateKnowItName(String newText) {
		KnowItNode knowItNode = (KnowItNode) node;
		KnowIt knowIt = knowItNode.getKnowIt();
		if (!newText.equals(knowIt.getDisplayText())) {
			// TODO: Undo/Redo
			knowIt.setDisplayText(newText);
		}
	};

	private void updateKnowItTypes(Collection<String> types) {
		KnowItNode knowItNode = (KnowItNode) node;
		KnowIt knowIt = knowItNode.getKnowIt();
		if (!knowIt.getTypes().equals(types)) {
			// TODO: Undo/Redo
			knowIt.setTypes(types);
		}
	}
}
