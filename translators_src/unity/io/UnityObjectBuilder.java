package io;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.yaml.snakeyaml.events.Event;
import org.yaml.snakeyaml.events.MappingStartEvent;
import org.yaml.snakeyaml.events.ScalarEvent;

/**
 * Builds all of the Unity Objects for one scene using the stream events.
 * 
 * @author kschenk
 * 
 */
public class UnityObjectBuilder {
	private final List<UnityObject> unityObjects;

	/**
	 * Builds {@link UnityObject}s for a scene based on the passed in event
	 * iterator. If this event iterator contains start events without matching
	 * end events, this translator will throw exceptions like [insert baseball
	 * simile here].
	 * 
	 * @param eventIterator
	 */
	public UnityObjectBuilder(Iterator<Event> eventIterator) {
		this.unityObjects = new ArrayList<UnityObject>();

		// Go through each event.
		while (eventIterator.hasNext()) {
			final Event event = eventIterator.next();
			if (event.is(Event.ID.DocumentStart)) {
				final UnityObject object;

				object = this.buildObject(eventIterator);

				if (object != null)
					this.unityObjects.add(object);
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
	}

	private UnityObject buildObject(Iterator<Event> eventIterator) {
		UnityObject object = null;

		if (eventIterator.hasNext()) {
			final Event event = eventIterator.next();

			if (event.is(Event.ID.MappingStart)) {
				final MappingStartEvent mapEvent = (MappingStartEvent) event;

				object = new UnityObject(
						Integer.parseInt(mapEvent.getAnchor()),
						mapEvent.getTag());

				object.setProperties(this.buildMap(eventIterator));
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
	private Map<String, Object> buildMap(Iterator<Event> eventIterator) {
		final Map<String, Object> map = new LinkedHashMap<String, Object>();

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
					map.put(currentKey, value);
					currentKey = null;
				}
			} else if (event.is(Event.ID.MappingStart)) {
				if (currentKey != null) {
					map.put(currentKey, this.buildMap(eventIterator));
					currentKey = null;
				} else
					throw new NullPointerException(
							"Attempted to add an entry to map [" + map
									+ "] with a null key.");
			} else if (event.is(Event.ID.SequenceStart)) {
				map.put(currentKey, this.buildSequence(eventIterator));
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

	public List<Object> buildSequence(Iterator<Event> eventIterator) {
		final List<Object> sequence = new ArrayList<Object>();

		while (eventIterator.hasNext()) {
			final Event event = eventIterator.next();

			if (event.is(Event.ID.Scalar)) {
				sequence.add(((ScalarEvent) event).getValue());
			} else if (event.is(Event.ID.MappingStart)) {
				sequence.add(this.buildMap(eventIterator));
			} else if (event.is(Event.ID.SequenceStart)) {
				sequence.add(this.buildSequence(eventIterator));
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

	/**
	 * Returns a list of {@link UnityObject}s created by the builder.
	 * 
	 * @return
	 */
	public List<UnityObject> getObjects() {
		return this.unityObjects;
	}
}