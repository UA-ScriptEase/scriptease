package scriptease.gui.control.editor;

import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

import javax.swing.JPanel;
import javax.swing.JTextField;

import scriptease.controller.undo.UndoManager;
import scriptease.model.StoryComponent;
import scriptease.model.StoryModelPool;

/**
 * NameEditor is a modified textfield that is attached to a KnowIt and updates
 * it's display text when it loses focus. It will also resize itself and the 4
 * levels of Containers above it to match the new size of the text. Meant to be
 * used in StoryComponentPanel's (specifically for KnowIt naming)
 * 
 * @author mfchurch
 * 
 */
@SuppressWarnings("serial")
public class NameEditor extends JTextField {
	protected StoryComponent storyComponent;

	public NameEditor(final StoryComponent component) {
		super();
		this.storyComponent = component;
		initialize();
		setupTextField();
	}
	
	protected StoryComponent getComponent() {
		return this.storyComponent;
	}

	protected void setupTextField() {
		this.setText(this.storyComponent.getDisplayText());
	}

	private void initialize() {
		this.setBackground(Color.white);
		this.resizeForText();

		// this.setText(component.getDisplayText());
		this.setHorizontalAlignment(JTextField.CENTER);
		this.addFocusListener(new FocusListener() {
			@Override
			public void focusLost(FocusEvent e) {
				NameEditor.this.updateText();
			}

			@Override
			public void focusGained(FocusEvent e) {
			}
		});

		this.addKeyListener(new KeyAdapter() {
			@Override
			public void keyTyped(KeyEvent e) {
				// update the text
				super.keyTyped(e);
				// resize the field
				resizeForText();
			}
		});
	}

	private void resizeForText() {
		final Dimension oldSize = this.getSize();

		// get metrics from the graphics
		FontMetrics metrics = this.getFontMetrics(this.getFont());
		// get the height of a line of text in this font and render context
		int hgt = metrics.getHeight();
		// get the advance of my text in this font and render context
		int adv = metrics.stringWidth(this.getText());
		// calculate the size of a box to hold the text with some padding.
		Dimension newSize = new Dimension(adv + 26, hgt + 6);
		// resize
		this.setSize(newSize);

		// Get the difference
		int xDifference = newSize.width - oldSize.width;
		int yDifference = newSize.height - oldSize.height;
		// Resize the next 5 levels above us. Hardcoded to work specifically for
		// use in StoryComponentPanels.
		Container parent = this.getParent();
		while (parent != null && parent instanceof JPanel) {
			final Dimension oldParentSize = parent.getSize();
			// Calculate the new parent size
			Dimension newParentSize = new Dimension(oldParentSize.width
					+ xDifference, oldParentSize.height + yDifference);
			// resize
			parent.setSize(newParentSize);
			parent.doLayout();
			parent = parent.getParent();
		}
	}

	/**
	 * Updates the text for either a KnowIt's displayText
	 */
	protected void updateText() {
		final String newValue = this.getText();
		if (StoryModelPool.getInstance().hasActiveModel()) {
			final String oldValue = this.storyComponent.getDisplayText();
			if (!oldValue.equals(newValue)) {
				if (!UndoManager.getInstance().hasOpenUndoableAction()) {
					UndoManager.getInstance().startUndoableAction(
							"Change " + oldValue + " to " + newValue);
					this.storyComponent.setDisplayText(newValue);
					UndoManager.getInstance().endUndoableAction();
				}
			}
		}
	}

	@Override
	public void setEnabled(boolean enabled) {
		this.setEditable(enabled);
	}
}