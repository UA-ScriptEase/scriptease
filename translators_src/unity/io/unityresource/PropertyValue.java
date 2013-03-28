package io.unityresource;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 * A wrapper class for values that can be added to a Unity Object's properties.
 * Using propert values makes our other code a lot cleaner since we don't have
 * to use multiple instanceof checks every time we parse a collection of them.
 * 
 * We also don't just have maps of objects, which means we won't unknowingly
 * change the type of a value.
 * 
 * @author kschenk
 * 
 */
public class PropertyValue {

	/**
	 * The types that we can store in a property value. Each of these should
	 * have an associated constructor.
	 * 
	 * @author kschenk
	 * 
	 */
	private static enum Type {
		STRING, INTEGER, FLOAT, LIST, MAP
	}

	private final Type type;

	private final Object value;

	/**
	 * Creates a property value for a {@link String}.
	 * 
	 * @param string
	 */
	public PropertyValue(String string) {
		this.value = string;
		this.type = Type.STRING;
	}

	/**
	 * Creates a property value for a {@link Map}.
	 * 
	 * @param map
	 */
	public PropertyValue(Map<String, PropertyValue> map) {
		this.value = map;
		this.type = Type.MAP;
	}

	/**
	 * Creates a property value for a {@link List}.
	 * 
	 * @param list
	 */
	public PropertyValue(List<PropertyValue> list) {
		this.value = list;
		this.type = Type.LIST;
	}

	/**
	 * Creates a property value for a {@link Float}.
	 * 
	 * @param value
	 */
	public PropertyValue(Float value) {
		this.value = value;
		this.type = Type.FLOAT;
	}

	/**
	 * Creates a property value for an {@link Integer}.
	 * 
	 * @param integer
	 */
	public PropertyValue(Integer integer) {
		this.value = integer;
		this.type = Type.INTEGER;
	}

	/**
	 * Builds a property map value based on the object passed in. If this is not
	 * a valid type for a property map value, an illegal argument exception is
	 * thrown.
	 * 
	 * @deprecated It is almost always safer to use explicit constructors
	 *             instead of this builder method, but there are rare cases
	 *             where we do not know what kind of object we are using.
	 * 
	 * @param object
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static PropertyValue buildValue(Object object) {
		if (object instanceof String)
			return new PropertyValue((String) object);
		else if (object instanceof Integer)
			return new PropertyValue((Integer) object);
		else if (object instanceof Float)
			return new PropertyValue((Float) object);
		else if (object instanceof Map<?, ?>)
			return new PropertyValue((Map<String, PropertyValue>) object);
		else if (object instanceof List<?>)
			return new PropertyValue((List<PropertyValue>) object);
		else
			throw new IllegalArgumentException("Object " + object
					+ " is not a recognized type for a map value.");
	}

	/**
	 * Converts a map of {@link PropertyValue}s to a map of objects that they
	 * contain. We need to use this for codegen.
	 * 
	 * @param propertyValueMap
	 * @return
	 */
	public static Map<String, Object> convertToValueMap(
			Map<String, PropertyValue> propertyValueMap) {
		final Map<String, Object> map = new HashMap<String, Object>();

		for (Entry<String, PropertyValue> entry : propertyValueMap.entrySet()) {
			final PropertyValue value = entry.getValue();
			final String key = entry.getKey();

			if (value.isMap())
				map.put(key, PropertyValue.convertToValueMap(value.getMap()));
			else if (value.isList())
				map.put(key, PropertyValue.convertToValueList(value.getList()));
			else
				map.put(key, entry.getValue().getValue());
		}

		return map;
	}

	/**
	 * Converts a list of {@link PropertyValue}s to a map of objects that they
	 * contain. We need to use this for codegen.
	 * 
	 * @param list
	 * @return
	 */
	public static List<Object> convertToValueList(List<PropertyValue> list) {
		final List<Object> newList = new ArrayList<Object>();

		for (PropertyValue value : list) {
			if (value.isMap())
				newList.add(PropertyValue.convertToValueMap(value.getMap()));
			else if (value.isList())
				newList.add(PropertyValue.convertToValueList(value.getList()));
			else
				newList.add(value.getValue());
		}
		return newList;
	}

	/**
	 * Checks if the {@link PropertyValue} is a {@link String}.
	 * 
	 * @return
	 */
	public boolean isString() {
		return this.type == Type.STRING;
	}

	/**
	 * Checks if the {@link PropertyValue} is a {@link Map}.
	 * 
	 * @return
	 */
	public boolean isMap() {
		return this.type == Type.MAP;
	}

	/**
	 * Checks if the {@link PropertyValue} is a {@link List}.
	 * 
	 * @return
	 */
	public boolean isList() {
		return this.type == Type.LIST;
	}

	/**
	 * Returns the value held by this object.
	 * 
	 * @deprecated You should usually use the specific values for the type, such
	 *             as {@link #getString()}, to make sure you aren't breaking
	 *             anything elsewhere.
	 * @return
	 */
	public Object getValue() {
		return this.value;
	}

	/**
	 * Returns the string held by a {@link PropertyValue} if it holds one.
	 * Otherwise, returns null.
	 * 
	 * @return
	 */
	public String getString() {
		if (this.type == Type.STRING)
			return (String) this.value;
		else
			return null;
	}

	/**
	 * Returns the integer held by the {@link PropertyValue} if it holds one.
	 * Otherwise, returns null.
	 * 
	 * @return
	 */
	public Integer getInteger() {
		if (this.type == Type.INTEGER)
			return (Integer) this.value;
		else
			return null;
	}

	/**
	 * Returns the list of {@link PropertyValue}s held by a
	 * {@link PropertyValue} if it holds one. Otherwise, returns null.
	 * 
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public List<PropertyValue> getList() {
		if (this.type == Type.LIST)
			return (List<PropertyValue>) this.value;
		else
			return null;
	}

	/**
	 * Returns the Map of Strings to {@link PropertyValue}s held by a
	 * {@link PropertyValue} if it holds one. Otherwise, returns null.
	 * 
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public Map<String, PropertyValue> getMap() {
		if (this.type == Type.MAP)
			return (Map<String, PropertyValue>) this.value;
		else
			return null;
	}

	/**
	 * Property values are equal if their values are equal. Property Valuse can
	 * also be compared to objects, which are equal if the property value's
	 * contained value is equal to the object.
	 * 
	 * @param obj
	 * @return
	 */
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof PropertyValue) {
			return this.value.equals(((PropertyValue) obj).getValue());
		} else
			return this.value.equals(obj);
	}

	@Override
	public String toString() {
		return "PropertyValue: [" + this.type.name() + ": " + this.value + "]";
	}
}
