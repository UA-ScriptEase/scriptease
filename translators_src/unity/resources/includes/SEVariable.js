#pragma strict

/**
 * This class is used by some ScriptEase II effects to let the user store
 * values. All Put methods use "dictionary[key] = value" instead of
 * Dictionary.Add(key, value) because Add will not allow the value to be
 * overwritten.
 *
 * @author ScriptEase II Team
 */
 
private static var stringMap : Dictionary.<String, String> = new Dictionary.<String, String>();
private static var floatMap : Dictionary.<String, float> = new Dictionary.<String, float>();
private static var gameObjectMap : Dictionary.<String, GameObject> = new Dictionary.<String, GameObject>();
private static var booleanMap : Dictionary.<String, boolean> = new Dictionary.<String, boolean>();

static function PutString(key:String, stored:String) {
	stringMap[key] = stored;
}

static function PutFloat(key:String, stored:float) {
	floatMap[key] = stored;
}

static function PutGameObject(key:String, stored:GameObject) {
	gameObjectMap[key] = stored;
}

static function PutBoolean(key:String, stored:boolean) {
	booleanMap[key] = stored;
}

static function GetString(key:String) : String {
	if(stringMap.ContainsKey(key)) {
		return stringMap[key];
	} else {
		return "";
	}
}

static function GetFloat(key:String) : float {
	if(floatMap.ContainsKey(key)) {
		return floatMap[key];
	} else {
		return 0;
	}
}

static function GetGameObject(key:String) : GameObject {
	if(gameObjectMap.ContainsKey(key)) {
		return gameObjectMap[key];
	} else {
		// Create a new object and immediately destroy it. We can't just return null
		// or other things will break.
		var object = new GameObject();
		GameObject.Destroy(object);
		return object;
	}
}

static function GetBoolean(key:String) : boolean {
	return booleanMap[key];
}

/**
 * Returns the SEPath attached to the object. Attaches an SEPath if none are
 * attached. This allows the experienced game developer to add an SEPath in the
 * editor but still make use of ScriptEase II.
 */
static function GetPath(object:GameObject):SEPath {
	var sePath:SEPath = object.GetComponent(SEPath);
		
	if(sePath == null) {
		object.AddComponent(SEPath);
		sePath = object.GetComponent(SEPath);
	}
	
	return sePath;
}