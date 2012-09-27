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

import scriptease.controller.observer.storycomponent.StoryComponentEvent;
import scriptease.controller.observer.storycomponent.StoryComponentObserver;
import scriptease.controller.observer.storycomponent.StoryComponentEvent.StoryComponentChangeEnum;
import scriptease.controller.undo.UndoManager;
import scriptease.model.PatternModelManager;
import scriptease.model.StoryComponent;

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
	protected final StoryComponent storyComponent;
	private final StoryComponentObserver observer;

	public NameEditor(final StoryComponent component) {
		super();
		this.storyComponent = component;
		this.observer = new StoryComponentObserver() {
			@Override
			public void componentChanged(StoryComponentEvent event) {
				if (event.getType() == StoryComponentChangeEnum.CHANGE_TEXT_NAME)
					NameEditor.this.setupTextField();
			}
		};

		component.addStoryComponentObserver(this.observer);

		this.setBackground(Color.white);

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

		this.setupTextField();
		this.resizeForText();
	}

	protected StoryComponent getComponent() {
		return this.storyComponent;
	}

	protected void setupTextField() {
		this.setText(this.storyComponent.getDisplayText());
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
		final Dimension newSize = new Dimension(adv + 26, hgt + 6);
		// resize
		this.setSize(newSize);
		this.setPreferredSize(newSize);

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
		if (PatternModelManager.getInstance().hasActiveModel()) {
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
	public void setText(String t) {
		super.setText(t);
		this.resizeForText();
	}

	@Override
	public void setEnabled(boolean enabled) {
		this.setEditable(enabled);
	}
}