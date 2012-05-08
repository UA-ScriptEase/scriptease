package scriptease.gui.control;

import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.Document;

/**
 * This is a text field with some extra properties as well as some abstract
 * methods to enforce those properties:
 * <ol>
 * <li>It determines when the text field should be editable. The method hook for
 * this is {@link #updateEnabledState()}.</li>
 * <li>It supports Autoflushing.</li>
 * </ol>
 * <strong>Warning:&nbsp;</strong> Autoflush can cause infinite loops. See
 * {@link Autoflushable} documentation for details. <br>
 * <br>
 * This class is based off of the ScriptEase 1 classes
 * <code>ca.ualberta.cs.games.scriptease.gui.FlushableTextField</code> and
 * <code>ca.ualberta.cs.games.scriptease.gui.SeTextField</code> by mattm, hence
 * his place in the author list.
 * 
 * @author mattm
 * @author remiller
 */
@SuppressWarnings("serial")
public abstract class FlushableTextField extends JTextField implements
		Autoflushable {
	private boolean autoflushEnabled = true;

	/*
	 * Java is stupid and forces constructors to be explicitly defined. These
	 * constructors are just calls to super with an added call to init(). It
	 * would be nice if they could be inherited, or if super(...) calls would
	 * just go up the chain until they hit one that matches. - remiller
	 */

	public FlushableTextField() {
		super();
		this.init();
	}

	public FlushableTextField(String text) {
		super(text);
		this.init();
	}

	public FlushableTextField(int columns) {
		super(columns);
		this.init();
	}

	public FlushableTextField(String text, int columns) {
		super(text, columns);
		this.init();
	}

	public FlushableTextField(Document doc, String text, int columns) {
		super(doc, text, columns);
		this.init();
	}

	/**
	 * Initialises the FlushableTextField. Java is stupid and forces
	 * constructors to be explicitly defined, so the common code is here.
	 */
	private void init() {
		// document observer that causes the autoflush to activate
		DocumentListener flusher = new DocumentListener() {
			public void changedUpdate(DocumentEvent e) {
			}

			public void insertUpdate(DocumentEvent e) {
				FlushableTextField.this.tryFlush();
			}

			public void removeUpdate(DocumentEvent e) {
				FlushableTextField.this.tryFlush();
			}
		};

		this.getDocument().addDocumentListener(flusher);
		this.updateEnabledState();
	}

	/**
	 * Gets the text from the model object state that this text field edits.
	 * 
	 * @return The text from the model object edited by this Text Field.
	 */
	protected abstract String getModelText();

	/**
	 * Sets the model object text state for the model object that this text
	 * field edits.
	 * 
	 * @param newValue
	 *            The new content of the model property that this field edits.
	 */
	protected abstract void updateKnowItBinding(String newValue);

	/**
	 * Sets this Text Field to enabled or disabled depending on the context of
	 * the situation. <br>
	 * <br>
	 * For example, a text field for editing the name of a parameter may be
	 * disabled if the StoryComponent parameter owner has no parameters.
	 */
	protected void updateEnabledState() {
		this.setEnabled(this.isLegal());
	}

	/**
	 * Override this method to extend the definition of whether this text field
	 * should be enabled or not. Include a call to <code>super.isLegal()</code>
	 * to ensure all qualities are checked.
	 * 
	 * @return True if the FlushableTextField should be enabled
	 */
	protected boolean isLegal() {
		return true;
	}

	/**
	 * Updates this text field to display the text from the model. <br>
	 * <br>
	 * Call this method as part of the response to model updates.
	 */
	protected void updateDisplay() {
		final String modelText = this.getModelText();
		final String viewText = this.getText();
		boolean auto = this.isAutoflushEnabled();

		this.setAutoflushEnabled(false);
		if (!modelText.equals(viewText))
			this.setText(modelText);
		this.setAutoflushEnabled(auto);
	}

	@Override
	public void setAutoflushEnabled(boolean enabled) {
		this.autoflushEnabled = enabled;
	}

	@Override
	public boolean isAutoflushEnabled() {
		return this.autoflushEnabled;
	}

	@Override
	public void tryFlush() {
		if (FlushableTextField.this.isAutoflushEnabled())
			this.updateKnowItBinding(this.getText());
	}
}
