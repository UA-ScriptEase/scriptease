package io.unityobject;

import java.util.ArrayList;
import java.util.Collection;

public class UnityGameObject extends UnityResource {
	private final Collection<UnityResource> children;

	public UnityGameObject(int uniqueID, String tag) {
		super(uniqueID, tag);

		this.children = new ArrayList<UnityResource>();
	}

}
