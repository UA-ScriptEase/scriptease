package io.genericfileformat;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * We never want more than one Journal GFF in our erf file, so this is a
 * singleton class. This is its own class due to specialist methods that we do
 * NOT want inside of a regular GFF.
 * 
 * @author kschenk
 * 
 */
public class GeneratedJournalGFF extends GenericFileFormat {
	private static final String RESREF = "se_genjournal";

	private static final GeneratedJournalGFF instance = new GeneratedJournalGFF();

	private final Collection<JournalCategory> categories;

	/**
	 * Returns the sole instance of GeneratedJournalGFF.
	 * 
	 * @return
	 */
	public static GeneratedJournalGFF getInstance() {
		return instance;
	}

	public void addCategory() {
		this.categories.add(new JournalCategory());
	}

	private GeneratedJournalGFF() {
		super(RESREF, TYPE_JOURNAL_BP + " ");

		this.categories = new ArrayList<JournalCategory>();

		// Add labels
		this.labelArray.add("Categories");

		// Top Level Struct
		this.structArray.add(new GffStruct(-1, 0, 1));

		// TODO Anything other than "categories" should be separated, so we can
		// create individual ones. We can have many categories and many entries
		// per category...
	}

	/**
	 * Repopulates index arrays based on number of categories.
	 */
	private void repopulateIndexArrays() {
		this.fieldIndicesArray.clear();
		this.listIndicesArray.clear();

		for (long i = 0; i < this.categories.size() * 10; i++)
			this.fieldIndicesArray.add(i);

		// TODO This is probably wrong.
		// output for an old test file showed these to be:

		/*
		 * LISTINDICES@0: 1
		 * 
		 * LISTINDICES@1: 2
		 * 
		 * LISTINDICES@2: 1
		 * 
		 * LISTINDICES@2: 3
		 * 
		 * LISTINDICES@3: 4
		 */
		for (long i = 0; i < this.categories.size() * 2; i++) {
			final List<Long> longList;

			longList = new ArrayList<Long>();

			longList.add(i + 1);

			this.listIndicesArray.add(longList);
		}
	}

	private class JournalCategory {
		private final GffField nameField;
		private final GffField tagField;

		public JournalCategory() {
			// TODO: Tag based on # of categories
			// TODO: entry data based on # of categories
			// TODO: name based on user input

			final GeneratedJournalGFF gff;

			gff = GeneratedJournalGFF.this;

			gff.labelArray.add("Name");
			gff.labelArray.add("XP");
			gff.labelArray.add("Priority");
			gff.labelArray.add("Picture");
			gff.labelArray.add("Comment");
			gff.labelArray.add("Tag");
			gff.labelArray.add("EntryList");
			gff.labelArray.add("ID");
			gff.labelArray.add("End");
			gff.labelArray.add("Text");

			// JournalCategory struct
			gff.structArray.add(new GffStruct(0, 0, 7));
			// JournalEntry struct
			gff.structArray.add(new GffStruct(0, 28, 3));

			// Categories Field: TODO DODO (!?)
			gff.fieldArray.add(new GffField(15, 0, 0));

			// Name Field: TODO DODO
			this.nameField = new GffField(12, 1, 0);
			this.nameField.setBlankCExoLocString();
			gff.fieldArray.add(this.nameField);

			// XP Field: DataOrDataOffset is constant.
			gff.fieldArray.add(new GffField(4, 2, 0));

			// Priority Field: DataOrDataOffset is constant.
			gff.fieldArray.add(new GffField(4, 3, 4));

			// Picture Field: DataOrDataOffset is constant.
			gff.fieldArray.add(new GffField(2, 4, 65535));

			// Comment Field: TODO DODO
			final GffField commentField;
			commentField = new GffField(10, 5, 20);
			gff.fieldArray.add(commentField);

			// Tag Field: TODO DODO
			this.tagField = new GffField(10, 6, 24);
			gff.fieldArray.add(tagField);

			// EntryList field: TODO DODO
			gff.fieldArray.add(new GffField(15, 7, 8));

			/*
			 * Entries
			 * 
			 * Note: we only have one entry per category since we're using
			 * custom tags to update entries instead of the built-in journal
			 * system.
			 */

			// ID Field: DataOrDataOffset is constant.
			gff.fieldArray.add(new GffField(4, 8, 1));

			// End field: DataOrDataOffset is constant.
			gff.fieldArray.add(new GffField(2, 9, 0));

			// Text field:
			final GffField textField;

			// This is a CExoLoc field. TODO DODO
			textField = new GffField(12, 10, 28);
			textField.setBlankCExoLocString();

			gff.fieldArray.add(textField);

			// TODO When we move out categories, we need to set data to
			// "<CUSTOM" + categoryCount + ">" instead
			// TODO Will have to change Tag data to category

			// There is nothing wrong with regenerating these btw

			textField.setData("<CUSTOM10>");
			commentField.setData("Journal generated by ScriptEase 2. Do not "
					+ "touch if you don't want to cause major issues.");

			this.setTag("se_category1");
			this.setName("Category Test");

			gff.repopulateIndexArrays();
		}

		/**
		 * Set the name of the Journal category.
		 * 
		 * @param name
		 */
		public void setName(String name) {
			this.nameField.setData(name);
		}

		/**
		 * Set the tag of the journal category.
		 * 
		 * @param tag
		 */
		public void setTag(String tag) {
			this.tagField.setData(tag);
		}
	}
}
