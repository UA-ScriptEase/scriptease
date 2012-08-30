package scriptease.controller.io.converter;

import sun.awt.util.IdentityArrayList;

import com.thoughtworks.xstream.converters.collections.CollectionConverter;
import com.thoughtworks.xstream.mapper.Mapper;

public class IdentityArrayListConverter extends CollectionConverter{

	public IdentityArrayListConverter(Mapper mapper) {
		super(mapper);
	}
	
	@SuppressWarnings("rawtypes")
	@Override
	public boolean canConvert(Class type) {
		return type.equals(IdentityArrayList.class);
	}
}
