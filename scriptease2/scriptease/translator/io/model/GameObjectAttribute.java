package scriptease.translator.io.model;

import java.util.Collection;

public class GameObjectAttribute implements GameConstant {
	private String name;
	private String resolutionText;
	private Collection<String> types;
	//inferred from object
	//private GameObject parent;
	
	public GameObjectAttribute(GameObject parent, String name, String resolutionText, Collection<String> types){
		//this.parent = parent;
		this.name = name;
		this.resolutionText = resolutionText;
		this.types = types;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public String getResolutionText() {
		return resolutionText;
	}

	@Override
	public Collection<String> getTypes() {
		return types;
	}



}
