package io.unityobject;

public class MonoBehaviourObject extends UnityObject {

	private final UnityObject parent;
	private final String scriptGUID;

	public MonoBehaviourObject(int uniqueID, String tag, UnityObject parent,
			String scriptGUID) {
		super(uniqueID, tag);

		this.parent = parent;
		this.scriptGUID = scriptGUID;
	}
}
