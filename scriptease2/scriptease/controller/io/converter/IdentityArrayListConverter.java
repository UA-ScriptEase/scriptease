package scriptease.controller.io.converter;

import sun.awt.util.IdentityArrayList;

import com.thoughtworks.xstream.converters.collections.CollectionConverter;
import com.thoughtworks.xstream.mapper.Mapper;

/**
 * The reason we need this class is because the regular ol' CollectionConverter
 * can't deal with IdentityArrayLists.
 * 
 * @author kschenk
 * 
 */
public class IdentityArrayListConverter extends CollectionConverter {

	public IdentityArrayListConverter(Mapper mapper) {
		super(mapper);
	}

	@SuppressWarnings("rawtypes")
	@Override
	public boolean canConvert(Class type) {
		return type.equals(IdentityArrayList.class);
	}
}
