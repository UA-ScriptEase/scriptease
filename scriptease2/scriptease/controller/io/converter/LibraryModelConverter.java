package scriptease.controller.io.converter;

import java.util.ArrayList;
import java.util.Collection;

import scriptease.controller.io.FileIO;
import scriptease.gui.SETree.filters.CategoryFilter.Category;
import scriptease.model.LibraryModel;
import scriptease.model.StoryComponent;

import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;

public class LibraryModelConverter implements Converter {
	private static final String TAG_TITLE = "Title";
	private static final String TAG_AUTHOR = "Author";

	@SuppressWarnings("rawtypes")
	@Override
	public boolean canConvert(Class type) {
		return type.equals(LibraryModel.class);
	}

	@Override
	public void marshal(Object source, HierarchicalStreamWriter writer,
			MarshallingContext context) {
		final LibraryModel model = (LibraryModel) source;

		writer.startNode(TAG_TITLE);
		writer.setValue(model.getTitle());
		writer.endNode();

		writer.startNode(TAG_AUTHOR);
		writer.setValue(model.getAuthor());
		writer.endNode();

		// write out the libary's patterns by category

		// effects
		writer.startNode(Category.EFFECTS.toString());
		context.convertAnother(model.getEffectsCategory().getChildren());
		writer.endNode();

		// causes
		writer.startNode(Category.CAUSES.toString());
		context.convertAnother(model.getCausesCategory().getChildren());
		writer.endNode();

		// effects
		writer.startNode(Category.DESCRIPTIONS.toString());
		context.convertAnother(model.getDescriptionsCategory()
				.getChildren());
		writer.endNode();

		// folders
		writer.startNode(Category.FOLDERS.toString());
		context.convertAnother(model.getFoldersCategory().getChildren());
		writer.endNode();
	}

	@Override
	public Object unmarshal(HierarchicalStreamReader reader,
			UnmarshallingContext context) {
		LibraryModel model = null;
		final String title;
		final String author;

		title = FileIO.readValue(reader, TAG_TITLE);
		author = FileIO.readValue(reader, TAG_AUTHOR);

		model = new LibraryModel(title, author);
		model.setAutosaving(true);

		this.unmarshallLibraryContents(reader, context, model);

		return model;
	}
	
	/**
	 * Populates the library from its recorded contents on disk.
	 * 
	 * @param reader
	 *            the source
	 * @param context
	 *            the reading context
	 * @param library
	 *            the library to populate
	 */
	@SuppressWarnings("unchecked")
	private void unmarshallLibraryContents(HierarchicalStreamReader reader,
			UnmarshallingContext context, LibraryModel library) {
		final Collection<StoryComponent> contents;
		final boolean wasSaving;

		// disable autosaving while constructing the library
		wasSaving = library.isAutosaving();
		library.setAutosaving(false);

		// read in the libary's patterns by category
		contents = new ArrayList<StoryComponent>();

		// actions
		reader.moveDown();
		reader.getNodeName();
		if (reader.hasMoreChildren())
			contents.addAll((Collection<? extends StoryComponent>) context
					.convertAnother(library, ArrayList.class));
		reader.moveUp();

		// causes
		reader.moveDown();
		if (reader.hasMoreChildren())
			contents.addAll((Collection<? extends StoryComponent>) context
					.convertAnother(library, ArrayList.class));
		reader.moveUp();

		// effects
		reader.moveDown();
		if (reader.hasMoreChildren())
			contents.addAll((Collection<? extends StoryComponent>) context
					.convertAnother(library, ArrayList.class));
		reader.moveUp();

		// folders
		reader.moveDown();
		if (reader.hasMoreChildren())
			contents.addAll((Collection<? extends StoryComponent>) context
					.convertAnother(library, ArrayList.class));
		reader.moveUp();

		// now add all of them to the library and let it sort it out.
		for (StoryComponent comp : contents) {
			library.add(comp);
		}

		library.setAutosaving(wasSaving);
	}
}
