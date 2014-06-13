package scriptease.model;

import java.util.ArrayList;
import java.util.Collection;

import scriptease.controller.ModelVisitor;
import scriptease.model.semodel.SEModel;
import scriptease.model.semodel.librarymodel.LibraryModel;
import scriptease.translator.Translator;
import scriptease.translator.io.model.GameType;
import scriptease.translator.io.model.Slot;
import scriptease.util.StringOp;

public class TranslatorModel extends SEModel {
	final Translator translator;

	public TranslatorModel(Translator translator) {
		super(translator.getName(), translator.getLocation().toString(), "");

		this.translator = translator;
	}

	@Override
	public Collection<GameType> getTypes() {
		final Collection<GameType> types = new ArrayList<GameType>();

		for (LibraryModel library : this.translator.getLibraries()) {
			types.addAll(library.getTypes());
		}

		return types;
	}

	@Override
	public GameType getType(String keyword) {
		for (GameType type : this.getTypes()) {
			if (type.getName().equals(keyword))
				return type;
		}

		return null;
	}

	@Override
	public Translator getTranslator() {
		return this.translator;
	}

	@Override
	public String getSlotDefaultFormat() {
		for (LibraryModel library : this.translator.getLibraries()) {
			final String defaultFormat = library.getSlotDefaultFormat();

			if (StringOp.exists(defaultFormat))
				return defaultFormat;
		}

		return "";
	}

	private Collection<Slot> getSlots() {
		final Collection<Slot> slots = new ArrayList<Slot>();

		for (LibraryModel library : this.translator.getLibraries()) {
			slots.addAll(library.getSlots());
		}

		return slots;
	}

	@Override
	public Slot getSlot(String name) {
		for (Slot slot : this.getSlots()) {
			if (slot.getDisplayName().equals(name))
				return slot;
		}
		return null;
	}

	@Override
	public void process(ModelVisitor visitor) {
		// TODO
	}

}
