package io.genericfileformat;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import scriptease.model.complex.ScriptIt;

/**
 * We never want more than one Journal GFF in our erf file. This is its own
 * class due to specialist methods that we do NOT want inside of a regular GFF.
 * 
 * @author kschenk
 * 
 */
public class GeneratedJournalGFF extends GenericFileFormat {
	// ResRef must be module for journal.
	private static final String RESREF = "module";

	private final List<JournalCategory> categories;

	/**
	 * Creates a new GeneratedJournalGFF. Since a journal requires at least one
	 * category, we need to pass in a ScriptIt to create the first category.
	 * 
	 * @param scriptIt
	 *            The initial ScriptIt to create a Journal GFF for. This cannot
	 *            be null.
	 */
	public GeneratedJournalGFF(ScriptIt scriptIt) {
		super(RESREF, TYPE_JOURNAL_BP + " ");

		if (scriptIt == null) {
			throw new NullPointerException("Cannot create journal GFF with"
					+ "null ScriptIt.");
		}

		this.categories = new ArrayList<JournalCategory>();

		// Add labels
		this.labelArray.add("Categories");
		this.labelArray.add("Name");
		this.labelArray.add("XP");
		this.labelArray.add("Priority");
		this.labelArray.add("Picture");
		this.labelArray.add("Comment");
		this.labelArray.add("Tag");
		this.labelArray.add("EntryList");
		this.labelArray.add("ID");
		this.labelArray.add("End");
		this.labelArray.add("Text");

		this.addCategory(scriptIt);
	}

	/**
	 * Set the name of the Journal Category with the specified ScriptIt.
	 * 
	 * @param name
	 *            The new name for the Category.
	 * @param scriptIt
	 *            The ScriptIt used to find the Category.
	 * @return Returns whether the assignment was successful. This will only
	 *         fail if we do not find the tag.
	 */
	public boolean setName(String name, ScriptIt scriptIt) {
		boolean success = false;

		for (JournalCategory category : this.categories) {
			if (category.getScriptIt().equals(scriptIt)) {
				category.setName(name);
				success = true;
				break;
			}
		}

		if (success)
			this.updateFieldsAndOffsets();

		return success;
	}

	/**
	 * Set the tag of the Journal Category with the specified ScriptIt.
	 * 
	 * @param newTag
	 *            The new tag for the Category.
	 * @param scriptIt
	 *            The ScriptIt used to find the Category.
	 * @return Returns true if assignment was successful. Unsuccessful
	 *         assignment can mean that a category with the tag was not found,
	 *         or a category with the new tag already existed.
	 */
	public boolean setTag(String newTag, ScriptIt scriptIt) {
		JournalCategory foundCategory = null;

		for (JournalCategory category : this.categories) {
			// Return false if we find that the tag already exists.
			if (category.getTag().equals(newTag))
				return false;

			if (category.getScriptIt().equals(scriptIt))
				foundCategory = category;
		}

		if (foundCategory != null) {
			foundCategory.setTag(newTag);

			this.updateFieldsAndOffsets();

			return true;
		}

		return false;
	}

	/**
	 * Adds a placeholder category to the journal with name "Placeholder", tag
	 * "se_placeholder+number_of_categories" and entry text "&lt;PLCEHLDR&gt;"
	 */
	public void addCategory(ScriptIt scriptIt) {
		this.categories.add(new JournalCategory(scriptIt, "Placeholder",
				"se_placeholder" + this.categories.size(), "<PLCEHLDR>"));

		Collections.sort(this.categories);

		this.updateFieldsAndOffsets();
	}

	/**
	 * Removes a category with the specified ScriptIt. Returns true if
	 * successful.
	 * 
	 * @param scriptIt
	 *            The scriptIt used to find the JournalCategory.
	 */
	public boolean removeCategory(ScriptIt scriptIt) {
		JournalCategory toBeRemoved = null;

		for (JournalCategory category : this.categories) {
			if (category.getScriptIt().equals(scriptIt)) {
				toBeRemoved = category;
				break;
			}
		}

		if (toBeRemoved != null) {
			this.categories.remove(toBeRemoved);
			this.updateFieldsAndOffsets();
		}

		return toBeRemoved != null;
	}

	/**
	 * Adds a new field into the field array with the given type, label index,
	 * and data or data offset. Initializes CEXOLOCSTRING types to blanks.
	 * 
	 * I abstracted this out to make the {@link #updateFieldsAndOffsets()}
	 * method easier to read.
	 * 
	 * @param typeNumber
	 * @param labelIndex
	 * @param dataOrDataOffset
	 * 
	 * @return The created field.
	 */
	private GffField addField(long typeNumber, long labelIndex,
			long dataOrDataOffset) {
		final GffField newField;

		newField = new GffField(typeNumber, labelIndex, dataOrDataOffset);

		if (typeNumber == GffField.TYPE_CEXOLOCSTRING)
			newField.setBlankCExoLocString();

		this.fieldArray.add(newField);

		return newField;
	}

	/**
	 * Writes the journals categories to the model with appropriate offsets.
	 */
	private void updateFieldsAndOffsets() {
		this.structArray.clear();
		this.fieldArray.clear();

		this.repopulateIndexArrays();

		final String commentText;

		commentText = "Journal generated by ScriptEase 2. Do not touch if you"
				+ " don't want to cause major issues.";

		// Top Level Struct
		this.structArray.add(new GffStruct(-1, 0, 1));

		// Categories Field: DataOrDataOffset is constant
		if (this.categories.size() == 2) {
			// Special case for 2 categories again. Not sure what it is about
			// these, but they like being special...
			this.addField(GffField.TYPE_LIST, 0, 16);
		} else
			this.addField(GffField.TYPE_LIST, 0, 0);

		int previousOffset = 0;
		for (JournalCategory category : this.categories) {
			final int index = this.categories.indexOf(category);

			final int COMMENT_OFFSET;
			final int TAG_OFFSET;
			final int TEXT_OFFSET;
			final int ENTRY_LIST_OFFSET;

			COMMENT_OFFSET = previousOffset + category.getName().length() + 20;
			TAG_OFFSET = COMMENT_OFFSET + commentText.length() + 20;
			TEXT_OFFSET = TAG_OFFSET + category.getTag().length() + 4;

			if (this.categories.size() == 2) {
				if (index == 0) {
					ENTRY_LIST_OFFSET = 8;
				} else {
					ENTRY_LIST_OFFSET = 28;
				}
			} else {
				ENTRY_LIST_OFFSET = 4 * (this.categories.size() + 1) + 8
						* index;
			}

			final GffField nameField;
			final GffField commentField;
			final GffField tagField;
			final GffField textField;

			// JournalCategory struct
			this.structArray.add(new GffStruct(index, index * 40, 7));
			// JournalEntry struct
			this.structArray.add(new GffStruct(0, index * 40 + 28, 3));

			nameField = this.addField(GffField.TYPE_CEXOLOCSTRING, 1,
					previousOffset);

			this.addField(GffField.TYPE_DWORD, 2, 0); // XP
			this.addField(GffField.TYPE_DWORD, 3, 4); // Priority
			this.addField(GffField.TYPE_WORD, 4, 65535); // Picture

			commentField = this.addField(GffField.TYPE_CEXOSTRING, 5,
					COMMENT_OFFSET);
			tagField = this.addField(GffField.TYPE_CEXOSTRING, 6, TAG_OFFSET);

			this.addField(GffField.TYPE_LIST, 7, ENTRY_LIST_OFFSET); // EntryList

			/*
			 * Note: we only have one entry per category since we're using
			 * custom tags to update entries instead of the built-in journal
			 * system.
			 */

			this.addField(GffField.TYPE_DWORD, 8, 1); // ID
			this.addField(GffField.TYPE_WORD, 9, 0); // End

			// Text field:
			textField = this.addField(GffField.TYPE_CEXOLOCSTRING, 10,
					TEXT_OFFSET);

			textField.setData(category.getEntryText());
			commentField.setData(commentText);
			tagField.setData(category.getTag());
			nameField.setData(category.getName());

			previousOffset = TEXT_OFFSET + category.getEntryText().length()
					+ 20;
		}
	}

	/**
	 * Return the number of categories in the journal.
	 * 
	 * @return
	 */
	public int getNumberOfCategories() {
		return this.categories.size();
	}

	/**
	 * Repopulates index arrays based on number of categories.
	 */
	private void repopulateIndexArrays() {
		final int SIZE_CATEGORIES = this.categories.size();
		this.fieldIndicesArray.clear();
		this.listIndicesArray.clear();

		for (long i = 1; i <= SIZE_CATEGORIES * 10; i++)
			this.fieldIndicesArray.add(i);

		/*
		 * Not sure if there's a method to the madness, but this is what is
		 * generated by NWN. Beware he who dares to look upon this abomination.
		 */
		if (SIZE_CATEGORIES != 2)
			for (long i = 0; i <= SIZE_CATEGORIES; i++) {
				final List<Long> longList;

				longList = new ArrayList<Long>();

				if (i == 0) {
					for (long j = 0; j < SIZE_CATEGORIES; j++) {
						longList.add(j * 2 + 1);
					}
				} else
					longList.add(i * 2);

				this.listIndicesArray.add(longList);
			}
		else {
			// Special case for a size of 2. Why? Why not!
			for (long i = 0; i < SIZE_CATEGORIES * 2; i++) {
				final List<Long> longList;

				longList = new ArrayList<Long>();

				if (i == 2) {
					longList.add(i - 1);
				}

				longList.add(i + 1);

				this.listIndicesArray.add(longList);
			}
		}
	}

	private class JournalCategory implements Comparable<JournalCategory> {
		private final ScriptIt scriptIt;

		private String name;
		private String tag;
		private String entryText;

		private JournalCategory(ScriptIt scriptIt, String name, String tag,
				String entryText) {
			this.scriptIt = scriptIt;

			this.setName(name);
			this.setTag(tag);
			this.setEntryText(entryText);
		}

		/**
		 * Returns the ScriptIt associated with the JournalCategory.
		 * 
		 * @return
		 */
		private ScriptIt getScriptIt() {
			return this.scriptIt;
		}

		/**
		 * Set the name of the Journal category.
		 * 
		 * @param name
		 */
		private void setName(String name) {
			this.name = name;
		}

		/**
		 * Returns the name stored by the Journal Category. Note that this
		 * returns what is stored in the class field, not in the GFFfield,
		 * although these two should be the same.
		 * 
		 * @return
		 */
		private String getName() {
			return this.name;
		}

		/**
		 * Set the tag of the journal category.
		 * 
		 * @param tag
		 */
		private void setTag(String tag) {
			this.tag = tag;
		}

		/**
		 * Set the entry text.
		 * 
		 * @param entryText
		 */
		private void setEntryText(String entryText) {
			this.entryText = entryText;
		}

		/**
		 * Returns the tag stored by the Journal Category. Note that this
		 * returns what is stored in the class field, not in the GFFfield,
		 * although these two should be the same.
		 * 
		 * @return
		 */
		private String getTag() {
			return this.tag;
		}

		/**
		 * Returns the entry text stored by the Journal Category. Note that this
		 * returns what is stored in the class field, not in the GFFfield,
		 * although these two should be the same.
		 * 
		 * @return
		 */
		private String getEntryText() {
			return this.entryText;
		}

		@Override
		public int compareTo(JournalCategory o) {
			int comparison;
			// Upper case has a different ascii value. This can be important for
			// comparisons to underscores, etc.

			comparison = this.getName().toUpperCase()
					.compareTo(o.getName().toUpperCase());

			return comparison;
		}
	}
}
