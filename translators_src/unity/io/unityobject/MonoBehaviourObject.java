package io.unityobject;

public class MonoBehaviourObject extends UnityResource {

	private final UnityResource parent;
	private final String scriptGUID;

	public MonoBehaviourObject(int uniqueID, String tag, UnityResource parent,
			String scriptGUID) {
		super(uniqueID, tag);

		this.parent = parent;
		this.scriptGUID = scriptGUID;
	}
}
