#pragma strict

import System.Collections.Generic;

/**
 * This class defines functions used by ScriptEase II to implement the dialogue
 * systen. They probably shouldn't be used in your own scripts if you are
 * developing primarily with ScriptEase, as it may break the dialogue system.
 *
 * @author ScriptEase II Team 
 */

static var DialogueRoots:List.<DialogueLine> = new List.<DialogueLine>();
static var DialoguesInitialized:boolean = false;

/**
 * Registers a dialogue root.
 */
static function RegisterRoot(text:String, id:int, speaker:int, enabled:boolean, image:Texture2D, audio:AudioSource) {
	var root:DialogueLine;
	
	root = new DialogueLine(text, id, speaker, enabled, image, audio);
	
	DialogueRoots.Add(root);
}

/**
 * Registers a child node to the parent.
 */
static function RegisterChild(parentID:int, text:String, id:int, speaker:int, enabled:boolean, image:Texture2D, audio:AudioSource) {
	var parent:DialogueLine = FindDialogueLine(parentID);
	
	if(parent != null) {
		var child:DialogueLine = FindDialogueLine(id);
		
		if(child != null) {
			parent.AddChild(child);
		} else 
			parent.AddChild(new DialogueLine(text, id, speaker, enabled, image, audio));
	} else {
		Debug.Log("SEDialogue Warning: Could not find parent with id "
			+ parentID);
	}
}

static function IsEnabled(id:int):boolean  {
	var line:DialogueLine = FindDialogueLine(id);
	
	if(line != null)
		return line.Enabled;
	else {
		Debug.Log("SEDialogue Warning: Attempted to find enabled state of " +
			"nonexistant DialogueLine " + id);
		return false;
	}
}

/**
 * Finds the Dialouge Line that matches the unique ID
 */
private static function FindDialogueLine(id:int) : DialogueLine {
	for(var root:DialogueLine in SEDialogue.DialogueRoots) {
		var dialogueLine = FindDialogueLineInDescendants(root, id);
		
		if(dialogueLine != null)
			return dialogueLine;
	}
	
	return null;
}

/**
 * Recursively searches for a Dialogue Line in the descendants of the passed in line.
 * This should only be called by FindDialogueLine(String).
 */
private static function FindDialogueLineInDescendants(parent:DialogueLine, id:int) : DialogueLine {
	if(parent.ID == id) 
		return parent;
	else {
		for(child in parent.Children) {
			var foundLine:DialogueLine = FindDialogueLineInDescendants(child, id);
				
			if(foundLine != null)
				return foundLine;
		}
	}
	
	return null;

}

private class DialogueLine {
	var Text:String;
	var ID:int;
	var Speaker:int;
	
	var Enabled:boolean;
	
	var Children:List.<DialogueLine>;
	var Parents:List.<DialogueLine>;
	
	var Audio:AudioSource;
	var Image:Texture2D;
	
	function DialogueLine(text:String, id:int, speaker:int, enabled:boolean, image:Texture2D, audio:AudioSource) {
		this.Text = text;
		this.ID = id;
		this.Speaker = speaker;
		this.Enabled = enabled;
		this.Image = image;
		this.Audio = audio;
		
		this.Children = new List.<DialogueLine>();
		this.Parents = new List.<DialogueLine>();
	}
	
	/*
     * Add a child to a Dialogue Line, automatically adding the Dialogue Line to
     * the child's parents.
     */
	function AddChild(child:DialogueLine) {
		this.Children.Add(child);
		child.Parents.Add(this);
	}
}