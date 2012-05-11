package scriptease.translator.codegenerator.code.fragments;

import java.util.regex.Pattern;

import scriptease.translator.codegenerator.code.contexts.Context;

/**
 * MapRefFragment is used to implement arbitrary maps in the LanguageDictionary.
 * The Fragments are resolved to a value that exists in the maps, specified by
 * the data and ref. The ref specifies the specific map to search, where the
 * data specified which field to use as a key. For example: <mapRef
 * ref="eventInstaller" data="subject" /> would use the subject (based on
 * context) as the key in the map eventInstaller, and return the value.
 * 
 * @author mfchurch
 * 
 */
public class MapRefFragment extends FormatFragment {
	private String ref = "";

	public MapRefFragment(String data, String ref) {
		super(data);
		this.ref = ref;
	}

	public String getRef() {
		return this.ref;
	}

	@Override
	public String resolve(Context context) {
		super.resolve(context);
		final String key;
		final SimpleFragment simpleFrag = new SimpleFragment(
				this.getDirectiveText(), Pattern.compile(""));

		key = simpleFrag.resolve(context);

		String code = context.getTranslator().getLanguageDictionary()
				.getGameMap(this.ref).resolveEntry(key);

		if (code != null)
			return code;
		else
			return "<MapRef was unable to be resolved for key: " + key + " >";
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof MapRefFragment) {
			return this.hashCode() == obj.hashCode();
		}
		return false;
	}

	@Override
	public int hashCode() {
		return super.hashCode() + this.ref.hashCode();
	}
}
