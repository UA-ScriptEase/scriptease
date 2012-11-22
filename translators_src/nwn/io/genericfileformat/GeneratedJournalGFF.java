package io.genericfileformat;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import scriptease.model.complex.ScriptIt;
import scriptease.model.complex.StoryPoint;

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

	// TODO Not sure if there's an easier way to reference these effects.
	// I almost feel like we should have tags in our APIDictionary, especially
	// since these should be implemented in all translators ever.
	public static final String EFFECT_CREATE_JOURNAL_TEXT = "Add Journal Record for <Story Point>";
	public static final String PARAMETER_STORY_POINT_TEXT = "Story Point";

	private static final String PLACEHOLDER_ENTRY_TEXT = "<PLACEHOLDER>";

	private final List<JournalCategory> categories;

	// This goes up and up for generating default category tags.
	private int categoryTagCount;

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
		this.categoryTagCount = 0;

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
	 * Set the tag of the Journal Category with the specified ScriptIt. We pass
	 * in a story point, whose unique 32 character name is used to generate a
	 * tag.<br>
	 * <br>
	 * This method also creates an entry text for the passed in story point.<br>
	 * <br>
	 * This method deals with null values appropriately by creating a
	 * placeholder tag and entry text.
	 * 
	 * @param storyPoint
	 *            The story point that we are assigning
	 * @param scriptIt
	 *            The ScriptIt used to find the Category.
	 * @return Returns true if assignment was successful. Unsuccessful
	 *         assignment can mean that a category with the tag was not found,
	 *         or a category with the new tag already existed.
	 */
	public boolean setStoryPoint(StoryPoint storyPoint, ScriptIt scriptIt) {
		final int MAX_TAG_LEN = 32;
		final String tag;

		if (storyPoint != null)
			tag = storyPoint.getUnique32CharName();
		else
			tag = this.generateDefaultTag();

		if (tag.length() > MAX_TAG_LEN)
			throw new IllegalArgumentException("Tag \"" + tag
					+ "\" is greater than " + MAX_TAG_LEN);

		JournalCategory foundCategory = null;

		for (JournalCategory category : this.categories) {
			if (foundCategory == category)
				throw new IllegalArgumentException(
						"Found more than one category for the tag " + tag
								+ ". Only one category may exist per" + "tag.");

			// Return false if we find that the tag already exists.
			if (category.getTag().equals(tag))
				return false;

			if (category.getScriptIt().equals(scriptIt))
				foundCategory = category;
		}

		if (foundCategory != null) {

			foundCategory.setTag(tag);

			if (storyPoint != null) {
				final String name;
				final int tagNumber;

				name = storyPoint.getDisplayText();
				tagNumber = 10 + storyPoint.getUniqueID();

				if (!name.isEmpty())
					foundCategory.setName(name);
				else
					foundCategory.setName(" ");

				foundCategory.setEntryText("<CUSTOM" + tagNumber + ">");
			} else
				foundCategory.setEntryText(PLACEHOLDER_ENTRY_TEXT);

			this.updateFieldsAndOffsets();

			return true;
		}

		return false;
	}

	/**
	 * Generates a default tag that equals "se_placeholder" plus the current
	 * category tag count.
	 * 
	 * @return
	 */
	public String generateDefaultTag() {
		return "se_placeholder" + this.categoryTagCount++;
	}

	/**
	 * Adds a placeholder category to the journal with name "Placeholder", a
	 * default tag and entry text "&lt;PLCEHLDR&gt;"
	 */
	public void addCategory(ScriptIt scriptIt) {
		this.categories.add(new JournalCategory(scriptIt, "Placeholder", this
				.generateDefaultTag(), PLACEHOLDER_ENTRY_TEXT));

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
			this.addField(GffField.TYPE_LIST, 0, 20);
		} else
			this.addField(GffField.TYPE_LIST, 0, 0);

		int previousOffset = 0;
		for (JournalCategory category : this.categories) {
			final int index = this.categories.indexOf(category);

			final int COMMENT_OFFSET;
			final int TAG_OFFSET;
			final int TEXT_OFFSET_1;
			final int TEXT_OFFSET_2;
			final int ENTRY_LIST_OFFSET;

			COMMENT_OFFSET = previousOffset + category.getName().length() + 20;
			TAG_OFFSET = COMMENT_OFFSET + commentText.length() + 20;
			TEXT_OFFSET_1 = TAG_OFFSET + category.getTag().length() + 4;
			TEXT_OFFSET_2 = TEXT_OFFSET_1 + category.getEntryText().length()
					+ 20;

			if (this.categories.size() == 2) {
				if (index == 0) {
					ENTRY_LIST_OFFSET = 8;
				} else {
					ENTRY_LIST_OFFSET = 32;
				}
			} else {
				ENTRY_LIST_OFFSET = 4 * (this.categories.size() + 1) + 12
						* index;
			}

			final GffField nameField;
			final GffField commentField;
			final GffField tagField;
			final GffField textField1;
			final GffField textField2;

			// JournalCategory struct
			this.structArray.add(new GffStruct(index, index * 52, 7));
			// JournalEntry struct 1
			this.structArray.add(new GffStruct(0, index * 52 + 28, 3));
			// JournalEntry struct 2
			this.structArray.add(new GffStruct(1, index * 52 + 40, 3));

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

			// Entry 1
			this.addField(GffField.TYPE_DWORD, 8, 1); // ID 1
			this.addField(GffField.TYPE_WORD, 9, 0); // End

			// Text field:
			textField1 = this.addField(GffField.TYPE_CEXOLOCSTRING, 10,
					TEXT_OFFSET_1);

			// Entry 2 (Finishing Entry)
			this.addField(GffField.TYPE_DWORD, 8, 2); // ID 2
			this.addField(GffField.TYPE_WORD, 9, 1);

			textField2 = this.addField(GffField.TYPE_CEXOLOCSTRING, 10,
					TEXT_OFFSET_2);

			textField2.setData(category.getEntryText());
			textField1.setData(category.getEntryText());
			commentField.setData(commentText);
			tagField.setData(category.getTag());
			nameField.setData(category.getName());

			previousOffset = TEXT_OFFSET_2 + category.getEntryText().length()
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

		for (long i = 1; i <= SIZE_CATEGORIES * 13; i++)
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
						longList.add(j * 3 + 1);
					}
				} else {
					longList.add(i * 3 - 1);
					longList.add(i * 3);
				}

				this.listIndicesArray.add(longList);
			}
		else {
			/*
			 * Special case for a size of 2. Why? Why not! This is awful, but
			 * writing out a for loop that does this takes up almost as much
			 * space and is even more confusing to look at since each index
			 * would have a special case...
			 */
			final List<Long> longList1;
			final List<Long> longList2;
			final List<Long> longList3;
			final List<Long> longList4;

			longList1 = new ArrayList<Long>();
			longList2 = new ArrayList<Long>();
			longList3 = new ArrayList<Long>();
			longList4 = new ArrayList<Long>();

			longList1.add(new Long(1));

			longList2.add(new Long(2));
			longList2.add(new Long(3));

			longList3.add(new Long(1));
			longList3.add(new Long(4));

			longList4.add(new Long(5));
			longList4.add(new Long(6));

			this.listIndicesArray.add(longList1);
			this.listIndicesArray.add(longList2);
			this.listIndicesArray.add(longList3);
			this.listIndicesArray.add(longList4);
		}
	}

	/**
	 * A JournalCategory represents a category in NWN journals. These categories
	 * have names, tags, and entry text. There are other fields and variables
	 * associated with journals, such as Quest Experience and if reaching them
	 * finishes the category. However, we are not interested in those right now.
	 * If we ever are, this would be the place to store them. <br>
	 * <br>
	 * The JournalCategory object also stores a ScriptIt, which facilitates
	 * deleting categories by deleting ScriptIts from the model. <br>
	 * <br>
	 * JournalCategory implements Comparable, allowing it to be sorted by name.
	 * 
	 * @author kschenk
	 * 
	 */
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
