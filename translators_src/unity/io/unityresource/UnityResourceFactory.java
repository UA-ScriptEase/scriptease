package io.unityresource;

import io.Scene;
import io.UnityProject;
import io.unityconstants.UnityField;
import io.unityconstants.UnityType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.yaml.snakeyaml.events.Event;
import org.yaml.snakeyaml.events.MappingStartEvent;
import org.yaml.snakeyaml.events.ScalarEvent;

/**
 * Contains methods to build {@link UnityResource}s.
 * 
 * @author kschenk
 * 
 */
@SuppressWarnings("serial")
public class UnityResourceFactory {
	private static final UnityResourceFactory instance = new UnityResourceFactory();

	// Just a couple of values that make our code cleaner and use less memory.
	private static final PropertyValue ZERO_VALUE = new PropertyValue(
			String.valueOf(0));
	private static final PropertyValue ONE_VALUE = new PropertyValue(1);
	private static final String FILEID = UnityField.FILEID.getName();
	private static final PropertyValue EMPTY_FILEID = new PropertyValue(
			new HashMap<String, PropertyValue>() {
				{
					this.put(FILEID, ZERO_VALUE);
				}
			});

	/**
	 * Gets the sole instance of the UnityResourceFactory.
	 * 
	 * @return
	 */
	public static UnityResourceFactory getInstance() {
		return instance;
	}

	private UnityResourceFactory() {
	};

	/**
	 * Builds an empty, invisible game object. Note that each game object
	 * requires an attached transform object, which can be created with
	 * {@link #buildTransformObject(Scene, int, int)}.
	 * 
	 * @param childTransformID
	 *            This is the id number of the child Transform. Note that this
	 *            method will NOT check if this is a valid ID number, since we
	 *            usually have to create both objects at the same time.
	 * @param name
	 *            The name of the Game Object.
	 * @param idNumber
	 *            The unique ID number of the Game Object.
	 * @return
	 */
	public UnityResource buildEmptyGameObject(final int childTransformID,
			final String name, int idNumber) {
		final PropertyValue transformID;
		final PropertyValue transformMap;
		final PropertyValue mComponentList;
		final PropertyValue propertiesMap;

		final Map<String, PropertyValue> objectMap;

		transformID = new PropertyValue(new HashMap<String, PropertyValue>() {
			{
				this.put(FILEID,
						new PropertyValue(String.valueOf(childTransformID)));
			}
		});

		transformMap = new PropertyValue(new HashMap<String, PropertyValue>() {
			{
				this.put("4", transformID);
			}
		});

		mComponentList = new PropertyValue(new ArrayList<PropertyValue>() {
			{
				this.add(transformMap);
			}
		});

		propertiesMap = new PropertyValue(new HashMap<String, PropertyValue>() {
			{
				this.put(UnityField.M_OBJECTHIDEFLAGS.getName(), ZERO_VALUE);
				this.put(UnityField.M_PREFABPARENTOBJECT.getName(),
						EMPTY_FILEID);
				this.put(UnityField.M_PREFABINTERNAL.getName(), EMPTY_FILEID);
				this.put(UnityField.SERIALIZEDVERSION.getName(),
						new PropertyValue(4));
				this.put(UnityField.M_COMPONENT.getName(), mComponentList);
				this.put(UnityField.M_LAYER.getName(), ZERO_VALUE);
				this.put(UnityField.M_NAME.getName(), new PropertyValue(name));
				this.put(UnityField.M_TAGSTRING.getName(), new PropertyValue(
						"Untagged"));
				this.put(UnityField.M_ICON.getName(), EMPTY_FILEID);
				this.put(UnityField.M_NAVMESHLAYER.getName(), ZERO_VALUE);
				this.put(UnityField.M_STATICEDITORFLAGS.getName(), ZERO_VALUE);
				this.put(UnityField.M_ISACTIVE.getName(), ONE_VALUE);
			}
		});

		objectMap = new HashMap<String, PropertyValue>() {
			{
				this.put(UnityType.GAMEOBJECT.getName(), propertiesMap);
			}
		};

		return new UnityResource(idNumber, UnityProject.UNITY_TAG
				+ UnityType.GAMEOBJECT.getID(), objectMap);
	}

	/**
	 * Creates an empty transform object at position 0,0,0.
	 * 
	 * @param parentGameObjectID
	 *            This is the id number of the parent game object. Note that
	 *            this method will NOT check if this is a valid ID number, since
	 *            we usually have to create both objects at the same time.
	 * @param idNumber
	 *            The unique ID number of the Transform.
	 * @return
	 */
	public UnityResource buildTransformObject(final int parentGameObjectID,
			int idNumber) {
		final PropertyValue gameObjectID;

		final PropertyValue properties;
		final Map<String, PropertyValue> objectMap;

		final PropertyValue localRotation;
		final PropertyValue localPosition;
		final PropertyValue localScale;

		gameObjectID = new PropertyValue(new HashMap<String, PropertyValue>() {
			{
				this.put(FILEID,
						new PropertyValue(String.valueOf(parentGameObjectID)));
			}
		});

		localRotation = new PropertyValue(new HashMap<String, PropertyValue>() {
			{
				this.put(UnityField.X.getName(), ZERO_VALUE);
				this.put(UnityField.Y.getName(), ZERO_VALUE);
				this.put(UnityField.Z.getName(), ZERO_VALUE);
				this.put(UnityField.W.getName(), ONE_VALUE);
			}
		});

		localPosition = new PropertyValue(new HashMap<String, PropertyValue>() {
			{
				this.put(UnityField.X.getName(), ZERO_VALUE);
				this.put(UnityField.Y.getName(), ZERO_VALUE);
				this.put(UnityField.Z.getName(), ZERO_VALUE);
			}
		});

		localScale = new PropertyValue(new HashMap<String, PropertyValue>() {
			{
				this.put(UnityField.X.getName(), ONE_VALUE);
				this.put(UnityField.Y.getName(), ONE_VALUE);
				this.put(UnityField.Z.getName(), ONE_VALUE);

			}
		});

		properties = new PropertyValue(new HashMap<String, PropertyValue>() {
			{
				this.put(UnityField.M_OBJECTHIDEFLAGS.getName(), ZERO_VALUE);
				this.put(UnityField.M_PREFABPARENTOBJECT.getName(),
						EMPTY_FILEID);
				this.put(UnityField.M_PREFABINTERNAL.getName(), EMPTY_FILEID);
				this.put(UnityField.M_GAMEOBJECT.getName(), gameObjectID);
				this.put(UnityField.M_LOCALROTATION.getName(), localRotation);
				this.put(UnityField.M_LOCALPOSITION.getName(), localPosition);
				this.put(UnityField.M_LOCALSCALE.getName(), localScale);
				this.put(UnityField.M_CHILDREN.getName(), new PropertyValue(
						new ArrayList<PropertyValue>()));
				this.put(UnityField.M_FATHER.getName(), EMPTY_FILEID);
			}
		});

		objectMap = new HashMap<String, PropertyValue>() {
			{
				this.put(UnityType.TRANSFORM.getName(), properties);
			}
		};

		return new UnityResource(idNumber, UnityProject.UNITY_TAG
				+ UnityType.TRANSFORM.getID(), objectMap);
	}

	/**
	 * Builds a MonoBehaviourObject based on the passed in parameters.
	 * 
	 * @return
	 */
	public UnityResource buildMonoBehaviourObject(final int attachedObjectID,
			final String guid, int idNumber) {
		final PropertyValue mGameObject;
		final PropertyValue mScript;
		final PropertyValue properties;
		final Map<String, PropertyValue> objectMap;

		mGameObject = new PropertyValue(new HashMap<String, PropertyValue>() {
			{
				this.put(FILEID, new PropertyValue(attachedObjectID));
			}
		});

		mScript = new PropertyValue(new HashMap<String, PropertyValue>() {
			{
				this.put(FILEID, new PropertyValue(11500000));
				this.put(UnityField.GUID.getName(), new PropertyValue(guid));
				this.put(UnityField.TYPE.getName(), ONE_VALUE);
			}
		});

		properties = new PropertyValue(new HashMap<String, PropertyValue>() {
			{
				this.put(UnityField.M_OBJECTHIDEFLAGS.getName(), ZERO_VALUE);
				this.put(UnityField.M_PREFABPARENTOBJECT.getName(),
						EMPTY_FILEID);
				this.put(UnityField.M_PREFABINTERNAL.getName(), EMPTY_FILEID);
				this.put(UnityField.M_GAMEOBJECT.getName(), mGameObject);
				this.put(UnityField.M_ENABLED.getName(), ONE_VALUE);
				this.put(UnityField.M_EDITORHIDEFLAGS.getName(), ZERO_VALUE);
				this.put(UnityField.M_SCRIPT.getName(), mScript);
				this.put(UnityField.M_NAME.getName(), new PropertyValue(""));

			}
		});

		objectMap = new HashMap<String, PropertyValue>() {
			{
				this.put(UnityType.MONOBEHAVIOUR.getName(), properties);
			}
		};

		return new UnityResource(idNumber, UnityProject.UNITY_TAG
				+ UnityType.MONOBEHAVIOUR.getID(), objectMap);
	}

	/**
	 * Builds {@link UnityResource}s for a scene based on the passed in event
	 * iterator. If this event iterator contains start events without matching
	 * end events, this translator will throw exceptions.
	 * 
	 * @param eventIterator
	 */
	public List<UnityResource> buildResources(Iterator<Event> eventIterator) {

		final List<UnityResource> unityObjects = new ArrayList<UnityResource>();

		// Go through each event.
		while (eventIterator.hasNext()) {
			final Event event = eventIterator.next();
			if (event.is(Event.ID.DocumentStart)) {
				final UnityResource object;

				object = this.buildResource(eventIterator);

				if (object != null)
					unityObjects.add(object);
			} else if (event.is(Event.ID.StreamStart)) {
				// Just continue for stream starts.
				continue;
			} else if (event.is(Event.ID.StreamEnd)) {
				if (eventIterator.hasNext()) {
					throw new IllegalStateException(
							"Stream End event encountered before all events "
									+ "were dealt with. Next event is "
									+ eventIterator.next());
				}
				break;
			} else {
				throw new IllegalArgumentException("Event [" + event
						+ "] is not the start of a document or stream.");
			}
		}

		return unityObjects;
	}

	/**
	 * Builds a new resource based on the current location of the iterator.
	 * 
	 * @param scene
	 * @param eventIterator
	 * @return
	 */
	private UnityResource buildResource(Iterator<Event> eventIterator) {
		UnityResource object = null;

		if (eventIterator.hasNext()) {
			final Event event = eventIterator.next();

			if (event.is(Event.ID.MappingStart)) {
				final MappingStartEvent mapEvent = (MappingStartEvent) event;

				object = new UnityResource(Integer.parseInt(mapEvent
						.getAnchor()), mapEvent.getTag(),
						this.buildMap(eventIterator));
			}
		}

		if (object == null)
			return null;

		while (eventIterator.hasNext()) {
			final Event event = eventIterator.next();
			if (event.is(Event.ID.MappingEnd)) {
				// We'll have a mapping end event.
				continue;
			} else if (event.is(Event.ID.DocumentEnd) && object != null) {
				return object;
			} else {
				throw new IllegalArgumentException("Invalid event [" + event
						+ "] found inside document for object " + object);
			}
		}

		return null;
	}

	/**
	 * Builds a map based on the current location of the iterator. Recursively
	 * adds any maps to itself.
	 * 
	 * @param eventIterator
	 * @return
	 */
	@SuppressWarnings("deprecation")
	private Map<String, PropertyValue> buildMap(Iterator<Event> eventIterator) {
		final Map<String, PropertyValue> map = new LinkedHashMap<String, PropertyValue>();

		String currentKey = null;
		while (eventIterator.hasNext()) {

			final Event event = eventIterator.next();

			if (event.is(Event.ID.Scalar)) {
				final Object value;

				value = ((ScalarEvent) event).getValue();
				if (currentKey == null) {
					if (value instanceof String)
						currentKey = (String) value;
					else
						throw new IllegalArgumentException(
								"Attempted to add a non "
										+ "string value as a key.");
				} else {
					map.put(currentKey, PropertyValue.buildValue(value));
					currentKey = null;
				}
			} else if (event.is(Event.ID.MappingStart)) {
				if (currentKey != null) {
					map.put(currentKey,
							new PropertyValue(this.buildMap(eventIterator)));
					currentKey = null;
				} else
					throw new NullPointerException(
							"Attempted to add an entry to map [" + map
									+ "] with a null key.");
			} else if (event.is(Event.ID.SequenceStart)) {
				map.put(currentKey,
						new PropertyValue(this.buildSequence(eventIterator)));
				currentKey = null;
			} else if (event.is(Event.ID.MappingEnd)) {
				return map;
			} else {
				throw new IllegalArgumentException("Invalid event [" + event
						+ "] found inside map.");
			}
		}

		// Throw an exception if we reach this point because we should have
		// reached a "MappingEnd" event.
		throw new IllegalStateException(
				"No closing MappingEnd event found for map: " + map);
	}

	/**
	 * Builds a sequence based on the current location of the event iterator.
	 * 
	 * @param eventIterator
	 * @return
	 */
	private List<PropertyValue> buildSequence(Iterator<Event> eventIterator) {
		final List<PropertyValue> sequence = new ArrayList<PropertyValue>();

		while (eventIterator.hasNext()) {
			final Event event = eventIterator.next();

			if (event.is(Event.ID.Scalar)) {
				sequence.add(new PropertyValue(((ScalarEvent) event).getValue()));
			} else if (event.is(Event.ID.MappingStart)) {
				sequence.add(new PropertyValue(this.buildMap(eventIterator)));
			} else if (event.is(Event.ID.SequenceStart)) {
				sequence.add(new PropertyValue(this
						.buildSequence(eventIterator)));
			} else if (event.is(Event.ID.SequenceEnd)) {
				return sequence;
			} else {
				throw new IllegalArgumentException("Invalid event [" + event
						+ "] found inside map.");
			}
		}

		throw new IllegalStateException(
				"No closing SequenceEnd event found for sequence: " + sequence);
	}
}
