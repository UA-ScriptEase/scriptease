package scriptease.model.semodel;

import java.io.File;
import java.util.Collection;
import java.util.Map;

import scriptease.controller.ModelVisitor;
import scriptease.gui.component.TypeWidget;
import scriptease.translator.Translator;
import scriptease.translator.io.model.GameType.GUIType;
import scriptease.util.FileOp;

/**
 * Abstract tree model of related patterns, either for a Story or a Library. A
 * Pattern Model has a <i>name</i>, an <i>author</i>, and a <i>root/i> node for
 * the tree of patterns that it contains.
 * 
 * @author remiller
 * @author kschenk
 */
public abstract class SEModel {
	private String name;
	private String author;

	public SEModel() {
		this("", "");
	}

	public SEModel(String title) {
		this(title, "");
	}

	public SEModel(String title, String author) {
		this.name = title;
		this.author = author;
	}

	/**
	 * Get's the PatternModel Name from the file with the extension removed
	 * 
	 * @return
	 */
	public final String getName() {
		return FileOp.removeExtension(new File(this.name)).getName();
	}

	/**
	 * @param name
	 *            The StoryModel's new title. Cannot be null.
	 * @throws IllegalArgumentException
	 *             if <code>author</code> is null.
	 */
	public final void setTitle(String title) {
		if (this.name == null)
			throw new IllegalArgumentException(
					"Cannot give a StoryModel a null name.");

		this.name = title;
	}

	/**
	 * @return The author of the StoryModel
	 */
	public final String getTitle() {
		return this.name;
	}

	/**
	 * @param author
	 *            The StoryModel's new author. Cannot be null.
	 * @throws IllegalArgumentException
	 *             if <code>author</code> is null.
	 */
	public final void setAuthor(String author) {
		if (this.author == null)
			throw new IllegalArgumentException(
					"Cannot give a PatternModel a null author.");

		this.author = author;
	}

	/**
	 * @return The author of the StoryModel
	 */
	public final String getAuthor() {
		return this.author;
	}

	/**
	 * Returns all of the type keywords known by the model.
	 * 
	 * @return
	 */
	public abstract Collection<String> getTypeKeywords();

	/**
	 * Returns the regex of the type known by the model.
	 * 
	 * @param keyword
	 * @return
	 */
	public abstract String getTypeRegex(String keyword);

	/**
	 * Returns the enumerated values of a type known by the model. <br>
	 * <br>
	 * An example of these would be if a translator gives special names to
	 * booleans to make more sense to the user. So the enumerated map would look
	 * like <code>{ (Active, true), (Inactive, false) }</code>
	 * 
	 * @param keyword
	 * @return
	 */
	public abstract Map<String, String> getTypeEnumeratedValues(String keyword);

	/**
	 * Returns the display text of a type known by the model.
	 * 
	 * @param keyword
	 * @return
	 */
	public abstract String getTypeDisplayText(String keyword);

	/**
	 * Returns the slots associated with a type known by the model.
	 * 
	 * @param keyword
	 * @return
	 */
	public abstract Collection<String> getTypeSlots(String keyword);

	/**
	 * Returns the code symbol associated with a type known by the model.
	 * 
	 * @param keyword
	 * @return
	 */
	public abstract String getTypeCodeSymbol(String keyword);

	/**
	 * Returns a map of characters to escape and the values to replace them with
	 * from a type known by the model.
	 * 
	 * @param keyword
	 * @return
	 */
	public abstract Map<String, String> getTypeEscapes(String keyword);

	/**
	 * Returns the GUI widget that is to edit a type known by the model.
	 * 
	 * @param keyword
	 *            the type whose GUI is to be determined.
	 * @return the GUI widget that will edit a component of this type, or
	 *         <code>null</code> if there is no widget specified.
	 */
	public abstract GUIType getTypeGUI(String keyword);

	/**
	 * Returns the name of the widget that should be displayed in the
	 * {@link TypeWidget} associated with a type known by the model.
	 * 
	 * @param keyword
	 * @return
	 */
	public abstract String getTypeWidgetName(String keyword);

	/**
	 * Gets the translator to be used to translate this pattern model.
	 * 
	 * @return the translator
	 */
	public abstract Translator getTranslator();

	/**
	 * This is a double-dispatch hook for the
	 * {@link scriptease.controller.ModelVisitor} family of controllers.
	 * <code>visitor</code> implements each of: process[X] where [X] is each of
	 * the leaf members of the <code>PatternModel</code> family. <BR>
	 * <BR>
	 * To Use: Pass in a valid ModelVisitor to this method. The implementing
	 * atom of this method will dispatch the appropriate
	 * <code>ModelVisitor</code> method for the atom's type. Voila! Double
	 * dispatch! :-)
	 * 
	 * @param visitor
	 *            The <code>ModelVisitor</code> that will process this
	 *            PatternModel.
	 */
	public abstract void process(ModelVisitor visitor);

	@Override
	public String toString() {
		return this.getName();
	}

}
