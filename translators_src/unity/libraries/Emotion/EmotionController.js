#pragma strict

/**
 * This class defines functions used by ScriptEase II to implement emotions.
 * EmotionControllers should be attached to GameObjects that require an
 * emotional state to be stored.
 * 
 * @author ScriptEase II Team 
 */

enum Emotion {
 NONE,
 HAPPY,
 SAD,
 ANGRY,
 AFRAID
}

var currentEmotion:Emotion = Emotion.NONE;

function SetEmotion(emotion:Emotion) {
	this.currentEmotion = emotion;
}

function HasEmotion():boolean {
	return this.currentEmotion != Emotion.NONE;
}

function IsHappy():boolean {
	return this.currentEmotion == Emotion.HAPPY;
}

function IsSad():boolean {
	return this.currentEmotion == Emotion.SAD;
}

function IsAngry():boolean {
	return this.currentEmotion == Emotion.ANGRY;
}

function IsAfraid():boolean {
	return this.currentEmotion == Emotion.AFRAID;
}
