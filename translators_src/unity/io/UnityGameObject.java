package io;
import java.util.Collection;
import scriptease.translator.io.model.GameObject;

/**
 * 
 * @author lari
 */
public final class UnityGameObject implements GameObject{
	// Unity has the idea of a hierarchy of objects, enter the prefab, not needed for objects though
	private GameObject prefabParentObject;
	private int fileID;
	private String name;
	private String tag;
	//Another fileID, i wonder if its useful, just an icon that is set in the Unity editor...sounds like a Duane wishlist lol!
	private int iconID;
	private Collection<String> types;
	
	
	// wonder what an m_component does....
	private Collection<GameObject> childrenComponents;
	
	private int resolutionMethod;
	
	//The sexiness of these is that they works in C# too...
	private final String codeFindByTag = "GameObject.FindWithTag";
	private final String codeFindByName = "GameObject.Find";
	
	public void addChildComponent(GameObject childComponentObject){
		childrenComponents.add(childComponentObject);
	}
	
	public  Collection<GameObject> getChildrenComponents(){
		return this.childrenComponents;
	}
	
	@Override
	public int getObjectID() {
		// TODO Auto-generated method stub
		return fileID;
	}
	
	public GameObject getParentObject(){
		return prefabParentObject;
	}

	@Override
	public int getResolutionMethod() {
		// TODO Auto-generated method stub
		return resolutionMethod;
	}

	@Override
	public void setResolutionMethod(int methodType) {
		this.resolutionMethod = methodType;
		
	}

	@Override
	public String getTemplateID() {
		Integer id = fileID;
		return id.toString();
	}

	@Override
	public String getName() {
		return this.name;
	}

	@Override
	public String getResolutionText() {
		switch(this.resolutionMethod){
			case 0:
				return this.codeFindByName + "(\"" + this.name + "\");";
			case 1:
				return this.codeFindByTag + "(\"" + this.tag + "\");";
			default:
				return null;
		}
		
	}

	@Override
	public Collection<String> getTypes() {
		return types;
	}
	

}
