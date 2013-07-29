using UnityEngine;
using System.Collections;

/**
 * This class defines functions used by ScriptEase II to get components from
 * Game Objects. If they are not found, ScriptEase II automatically adds the
 * component to the game object.
 *
 * @author ScriptEase II Team 
 */
public class SEGameObject : MonoBehaviour {
	
	/**
	 * Gets the passed in component of the same type. If it does not exist,
	 * this function adds the component and then returns the new one.
	 */
	public static T Get<T>(GameObject go) where T : Component
	{
		T component = go.GetComponent<T>();
    	
		if(component == null) {
			go.AddComponent<T>();
			component = go.GetComponent<T>();
		}
		
		return component;
	}
}
