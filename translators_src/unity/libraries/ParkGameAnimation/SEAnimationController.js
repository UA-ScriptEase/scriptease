#pragma strict

/**
 * This class defines an SEAnimationController. It is specific to animations 
 * created in the "Park Game".  
 *
 * The SEPAnimationController is part of the optional Park Game Animation Library.
 *
 *@author ScriptEase II Team
 */

/**
 * Returns the SEAnimationController attached to the object. Attaches the SEAnimationController.js
 * script if none are attached.
 */
static function GetAnimationController(object:GameObject):SEAnimationController {
	var seAnimationController:SEAnimationController = object.GetComponent(SEAnimationController);
		
	if(seAnimationController == null) {
		object.AddComponent(SEAnimationController);
		seAnimationController = object.GetComponent(SEAnimationController);
	}
	
	return seAnimationController;
}

internal var shoulderL : Transform;
internal var shoulderR : Transform;
internal var thighL : Transform;
internal var thighR : Transform;
internal var handL : Transform;
internal var handR : Transform;
internal var forearmL : Transform;
internal var forearmR : Transform;
internal var spine : Transform;
internal var root : Transform;
internal var pelvis : Transform;
internal var neck : Transform;
internal var head : Transform;

shoulderL = transform.Find("SK Root/SK Pelvis/SK Spine/SK Spine1/SK Spine2/SK Spine3/SK Neck/SK L Clavicle/SK L UpperArm").transform;
shoulderR = transform.Find("SK Root/SK Pelvis/SK Spine/SK Spine1/SK Spine2/SK Spine3/SK Neck/SK R Clavicle/SK R UpperArm").transform;
thighL = transform.Find("SK Root/SK Pelvis/SK L Thigh").transform;
thighR = transform.Find("SK Root/SK Pelvis/SK R Thigh").transform;
handL = transform.Find("SK Root/SK Pelvis/SK Spine/SK Spine1/SK Spine2/SK Spine3/SK Neck/SK L Clavicle/SK L UpperArm/SK L Forearm/SK L Hand").transform;
handR = transform.Find("SK Root/SK Pelvis/SK Spine/SK Spine1/SK Spine2/SK Spine3/SK Neck/SK R Clavicle/SK R UpperArm/SK R Forearm/SK R Hand").transform;
forearmL = transform.Find("SK Root/SK Pelvis/SK Spine/SK Spine1/SK Spine2/SK Spine3/SK Neck/SK L Clavicle/SK L UpperArm/SK L Forearm").transform;
forearmR = transform.Find("SK Root/SK Pelvis/SK Spine/SK Spine1/SK Spine2/SK Spine3/SK Neck/SK R Clavicle/SK R UpperArm/SK R Forearm").transform;
spine = transform.Find("SK Root/SK Pelvis/SK Spine").transform;
neck = transform.Find("SK Root/SK Pelvis/SK Spine/SK Spine1/SK Spine2/SK Spine3/SK Neck").transform;
head = transform.Find("SK Root/SK Pelvis/SK Spine/SK Spine1/SK Spine2/SK Spine3/SK Neck/SK Head").transform;
pelvis = transform.Find("SK Root/SK Pelvis").transform;
root = transform.Find("SK Root").transform;

internal var kickAnimName : String; kickAnimName = "kick_angry";
internal var kick : AnimationState;
kick = animation[kickAnimName];
internal var kick2AnimName : String; kick2AnimName = "kick_Normal";
internal var kick2 : AnimationState;
kick2 = animation[kick2AnimName];


internal var kickAnimName_left : String; kickAnimName_left = "kick_angry_left";
internal var kick_left : AnimationState;
kick_left = animation[kickAnimName_left];
internal var kick2AnimName_left : String; kick2AnimName_left = "kick_Normal_left";
internal var kick2_left : AnimationState;
kick2_left = animation[kick2AnimName_left];

internal var sitAnimName : String; sitAnimName = "turnsit";
internal var sitAnim : AnimationState;
sitAnim = animation[sitAnimName];

internal var sittingAnimName : String; sittingAnimName = "turn_sitting";
internal var sittingAnim : AnimationState;
sittingAnim = animation[sittingAnimName];

internal var neutralAnimName : String; neutralAnimName = "NeutralIdle"; // NeutralIdle
internal var neutral : AnimationState ;
neutral = animation[neutralAnimName];

internal var standIdleAnimName : String; standIdleAnimName = "stand_Idle";
internal var standIdle : AnimationState;
standIdle = animation[standIdleAnimName];

internal var walkAnimName : String; walkAnimName = "walkCycle";
internal var walk : AnimationState;
walk = animation[walkAnimName];
internal var walkArmAnimName : String; walkArmAnimName = "walkCycleArm";
internal var walkArm : AnimationState;
walkArm = animation[walkArmAnimName];


internal var runAnimName : String; runAnimName = "runCycle";
internal var run : AnimationState;
run = animation[runAnimName];
internal var runArmAnimName : String; runArmAnimName = "runCycleArm";
internal var runArm : AnimationState;
runArm = animation[runArmAnimName];

internal var sadAnimName : String; sadAnimName = "SadIdle";
internal var sad : AnimationState;
sad = animation[sadAnimName];
internal var sadArmAnimName : String; sadArmAnimName = "SadIdleArm";
internal var sadArm : AnimationState;
sadArm = animation[sadArmAnimName];


internal var angryAnimName : String; angryAnimName = "AngryIdle";
internal var angry : AnimationState;
angry = animation[angryAnimName];
internal var angryArmAnimName : String; angryArmAnimName = "AngryIdleArm";
internal var angryArm : AnimationState;
angryArm = animation[angryArmAnimName];


internal var happyAnimName : String; happyAnimName = "JoyIdle";
internal var happy : AnimationState;
happy = animation[happyAnimName];
internal var happyArmAnimName : String; happyArmAnimName = "JoyIdleArm";
internal var happyArm : AnimationState;
happyArm = animation[happyArmAnimName];


internal var afraidAnimName : String; afraidAnimName = "SurprisedIdle";
internal var afraid : AnimationState;
afraid = animation[afraidAnimName];
internal var afraidArmAnimName : String; afraidArmAnimName = "SurprisedIdleArm";
internal var afraidArm : AnimationState;
afraidArm = animation[afraidArmAnimName];
internal var afraidHeadAnimName : String; afraidHeadAnimName = "idleFear";
internal var afraidHead : AnimationState;
afraidHead = animation[afraidHeadAnimName];


internal var waveAnimName : String; waveAnimName = "waveBoth";
internal var wave : AnimationState;
wave = animation[waveAnimName];
internal var kneelAnimName : String; kneelAnimName = "kneelIdle";
internal var kneel : AnimationState;
kneel = animation[kneelAnimName];

//internal var ArmOffsetAnimName : String; ArmOffsetAnimName = "ArmOffset";
//internal var ArmOffset : AnimationState;
//ArmOffset = animation[ArmOffsetAnimName];


internal var startleAnimName : String; startleAnimName = "startled_mild";
internal var startle : AnimationState;
startle = animation[startleAnimName];


internal var drinkAnimName : String; drinkAnimName = "drinkFromCup";
internal var drink : AnimationState;
drink = animation[drinkAnimName];
internal var holdIdleAnimName : String; holdIdleAnimName = "holdIdle";
internal var holdIdle : AnimationState;
holdIdle = animation[holdIdleAnimName];
internal var pickTableAnimName : String; pickTableAnimName = "pickFromTable";
internal var pickTable : AnimationState;
pickTable = animation[pickTableAnimName];
internal var lookAtAnimName : String; lookAtAnimName = "lookAtHoldingItem";
internal var lookAt : AnimationState;
lookAt = animation[lookAtAnimName];
internal var playIPadAnimName : String; playIPadAnimName = "playIpad";
internal var playIPad : AnimationState;
playIPad = animation[playIPadAnimName];
internal var sitDownAnimName : String; sitDownAnimName = "sitDown";
internal var sitDown : AnimationState;
sitDown = animation[sitDownAnimName];
internal var sitIdleAnimName : String; sitIdleAnimName = "sitIdle";
internal var sitIdle : AnimationState;
sitIdle = animation[sitIdleAnimName];
internal var sitNervousAnimName : String; sitNervousAnimName = "sitNervous";
internal var sitNervous : AnimationState;
sitNervous = animation[sitNervousAnimName];
internal var sitTalkLeftAnimName : String; sitTalkLeftAnimName = "sitTalkToLeft";
internal var sitTalkLeft : AnimationState;
sitTalkLeft = animation[sitTalkLeftAnimName];
internal var sitTalkRightAnimName : String; sitTalkRightAnimName = "sitTalkToRight";
internal var sitTalkRight : AnimationState;
sitTalkRight = animation[sitTalkRightAnimName];
internal var standUpAnimName : String; standUpAnimName = "standUp";
internal var standUp : AnimationState;
standUp = animation[standUpAnimName];

// SECOND SET FROM WEI
internal var checkOverAnimName : String; checkOverAnimName = "checkOverShoulder";
internal var checkOver : AnimationState;
checkOver = animation[checkOverAnimName];
internal var idleTalkAnimName : String; idleTalkAnimName = "idleTalk";
internal var idleTalk : AnimationState;
idleTalk = animation[idleTalkAnimName];
internal var lookAtIdleAnimName : String; lookAtIdleAnimName = "lookAtHoldingItemIdle";
internal var lookAtIdle : AnimationState;
lookAtIdle = animation[lookAtIdleAnimName];
internal var nodHeadAnimName : String; nodHeadAnimName = "nodHead";
internal var nodHead : AnimationState;
nodHead = animation[nodHeadAnimName];
internal var passDownAnimName : String; passDownAnimName = "passItemDown";
internal var passDown : AnimationState;
passDown = animation[passDownAnimName];
internal var passUpAnimName : String; passUpAnimName = "passItemUp";
internal var passUp : AnimationState;
passUp = animation[passUpAnimName];
internal var pickGroundAnimName : String; pickGroundAnimName = "pickFromGround";
internal var pickGround : AnimationState;
pickGround = animation[pickGroundAnimName];
internal var pointAtAnimName : String; pointAtAnimName = "pointAtItem";
internal var pointAt : AnimationState;
pointAt = animation[pointAtAnimName];
internal var shakeHeadAnimName : String; shakeHeadAnimName = "shakeHead";
internal var shakeHead : AnimationState;
shakeHead = animation[shakeHeadAnimName];
internal var throwItemAnimName : String; throwItemAnimName = "throwItem";
internal var throwItem : AnimationState;
throwItem = animation[throwItemAnimName];


var playDrink : boolean = false;
var playHoldIdle : boolean = false;
var playPickOffTable : boolean = false;
var playLookAtItem : boolean = false;
var playWithIPad : boolean = false;


/****************************************************************************/
//neutral.layer = 1;
walk.layer = 1;
run.layer = 1;
sitAnim.layer = 3;
sittingAnim.layer = 1;
sitDown.layer = 4;
standUp.layer = 4;
sitNervous.layer = 3;
sitIdle.layer = 3;
sitTalkRight.layer = 3;
sitTalkLeft.layer = 3;

standIdle.layer = 1;

happy.layer = 2;
happy.AddMixingTransform(spine);
//happyArm.layer = 2;

sad.layer = 2;
sad.AddMixingTransform(spine);
//sadArm.layer = 2;

angry.layer = 2;
angry.AddMixingTransform(spine);
//angryArm.layer = 2;

afraid.layer = 2;
afraid.AddMixingTransform(spine);
//afraidArm.layer = 2;
afraidHead.layer = 6;
afraidHead.weight = 1;
afraidHead.AddMixingTransform(neck);


walkArm.layer = 3;
walkArm.AddMixingTransform(shoulderL);
walkArm.AddMixingTransform(shoulderR);
runArm.layer = 3;
runArm.AddMixingTransform(shoulderL);
runArm.AddMixingTransform(shoulderR);
//walkArm.enabled = false;
//runArm.enabled = false;

sadArm.layer = 3;
sadArm.AddMixingTransform(shoulderR);
sadArm.AddMixingTransform(shoulderL);

happyArm.layer = 3;
happyArm.AddMixingTransform(handR);
happyArm.AddMixingTransform(handL);

angryArm.layer = 3;
angryArm.AddMixingTransform(handR);
angryArm.AddMixingTransform(handL);

afraidArm.layer = 3;
afraidArm.AddMixingTransform(forearmR);
afraidArm.AddMixingTransform(forearmL);

wave.layer = 6;
wave.AddMixingTransform(shoulderL);
wave.AddMixingTransform(shoulderR);
kneel.layer = 4;
kneel.AddMixingTransform(thighL);
kneel.AddMixingTransform(thighR);

holdIdle.layer = 5;
holdIdle.AddMixingTransform(shoulderR);
drink.layer = 5;
drink.AddMixingTransform(spine);
playIPad.layer = 5;
playIPad.AddMixingTransform(spine);
lookAt.layer = 5;
lookAt.AddMixingTransform(spine);
lookAtIdle.layer = 5;
lookAtIdle.AddMixingTransform(spine);
pickTable.layer = 5;
pickTable.AddMixingTransform(spine);
pickTable.AddMixingTransform(root);
pickGround.layer = 5;
passUp.layer = 5;
passUp.AddMixingTransform(spine);
passUp.AddMixingTransform(shoulderR);
passDown.layer = 5;
passDown.AddMixingTransform(spine);
passDown.AddMixingTransform(shoulderR);

throwItem.layer = 5;

nodHead.layer = 6;
nodHead.AddMixingTransform(head);
shakeHead.layer = 6;
shakeHead.AddMixingTransform(head);

checkOver.layer = 4;
checkOver.AddMixingTransform(spine);

pointAt.layer = 4;
pointAt.AddMixingTransform(neck);

idleTalk.layer = 4;
//idleTalk.AddMixingTransform(neck);

internal var kneelOffset : Vector3;
internal var waistHeight : float;
waistHeight = root.localPosition.y;

//ArmOffset.layer = 5;
//ArmOffset.AddMixingTransform(shoulderL);
//ArmOffset.AddMixingTransform(shoulderR);

kick.layer = 6;
kick.enabled = false;
kick.speed = 1.5;
kick2.layer = 6;
kick2.enabled = false;

kick_left.layer = 6;
kick_left.enabled = false;
kick_left.speed = 1.5;
kick2_left.layer = 6;
kick2_left.enabled = false;
//kick2.speed = 1.5;

startle.layer = 7;
startle.enabled = false;


/************************************/

var sadWeight : float;
var happyWeight : float;
var angryWeight : float;
var afraidWeight : float;
var weightKneel : float;
var weightwaveBoth : float = 1;
var weightwaveSpeed : float = 1;
//var weightArmOffset : float;


var weightStartle : float = 1;
internal var weightSadCur : float;
internal var weightFearCur : float;
internal var weightAngerCur : float;
internal var weightJoyCur : float;
internal var FearSpeed : float;
internal var SadSpeed : float;
internal var AngerSpeed : float;
internal var JoySpeed : float;
internal var walkSpeedDefault : float;
walkSpeedDefault = 1.041087 * transform.localScale.x;
internal var runSpeedDefault : float;
runSpeedDefault = 4.537676 * transform.localScale.x;

var CharSpeed : float = 0;

var staySitting : boolean = false;
//var holding : boolean = false;

var changed : boolean = false;
var angryKick : boolean = false;
var leftFoot : boolean = false;
var leftHand : boolean = false;
var waitForAnimDone : boolean = false;
var waitingAnim : String = "";
var doRun : boolean = false;
var doSit : boolean = false;
var doWave : boolean = false;
var doHold : boolean = false;
var areSitting : boolean = false;
var doKick : boolean = false;
var doStartle : boolean = false;
var doPickUp : boolean = false;
var areHolding : boolean = false;
var doShake : boolean = false;
var doNod : boolean = false;
var doCheck : boolean = false;
var showWave : boolean = false;
var notDone : boolean = true;
var doTalk : boolean = false;

var itemHeld : Transform = null;
var setParent : Transform = null;

private var motor : CharacterMotor;
	
/*
* Find out if this character has a character motor
*/
function Awake () {
	motor = GetComponent(CharacterMotor);
}
	
function Start() {
	//itemHeld = null;
	//itemHeld = "paper cup";
	showWave = true;
	doPass = false;
	onGround = false;
	throwOut = false;
	areSitting = false;
	//areHolding = false;
	doRun = false;
	//CharSpeed = 0;
	doCheck = false;
	//showTalk = true;
	weightStartle = 0.5;
	//toggleTalk();
	
	
	
}


var stopPassUp = false;
var incidentWait : boolean = false;
var doingPickUp = false;
function Update () {


	// there's an animation in progress we need to let finish before starting a new one
	// this applies to sitting down, standing up, picking up, putting down, startle, kick
	// but not holding or waving, or once actually sitting
	if (waitForAnimDone) { 
		
		var putDownTable : boolean = false;
		if (!doingPickUp) 
			putDownTable  = waitingAnim.Contains("pickFromTable") && animation.IsPlaying(waitingAnim) && notDone && Mathf.Abs(pickTable.time - 0.5) < 0.3;
		var putDownPass : boolean = waitingAnim.Contains("passItemDown") && animation.IsPlaying(waitingAnim) && notDone && passDown.time < 0.05;
		var putdownGarb : boolean = waitingAnim.Contains("throwItem") && animation.IsPlaying(waitingAnim) && notDone && throwItem.time > (throwItem.length / 2);
		
		
		var pickUpGround : boolean = waitingAnim.Contains("pickFromGround") && animation.IsPlaying(waitingAnim) && notDone && pickGround.time > 1.50;
		var pickFromTable : boolean = waitingAnim.Contains("pickFromTable") && animation.IsPlaying(waitingAnim) && notDone && pickTable.time > 0.60;
	
	
		if (pickUpGround || pickFromTable) {
			notDone = false;
		
			performPickUp();
			
		}
		
		if (putDownTable || putDownPass || putdownGarb) {
			// need to DROP item we're holding
			
			// make sure we're actually holding something first
			if (itemHeld != null) {
				
				// give item a new parent
				itemHeld.parent = setParent;
				
				// if we're doing passItemDown, then we should pass to 'friend'
				if (putDownPass && !setParent.name.Contains("Props")) {
					var rHand = setParent.Find("SK Root/SK Pelvis/SK Spine/SK Spine1/SK Spine2/SK Spine3/SK Neck/SK R Clavicle/SK R UpperArm/SK R Forearm/SK R Hand").transform;
					itemHeld.parent = rHand;
					itemHeld.rigidbody.collider.enabled = false;
				
					if (itemHeld.name.Contains("newspaper")) {
						itemHeld.transform.localPosition = Vector3(-4,2,0);
						itemHeld.localEulerAngles = Vector3(355,200,180);	
					} else if (itemHeld.name.Contains("papercup")) {
						itemHeld.localPosition = Vector3(-4,2.3,0.5);
						itemHeld.localEulerAngles = Vector3(355,0,180);
					} else if (itemHeld.name.Contains("book")) {
						itemHeld.localPosition = Vector3(-4,4,3);
						itemHeld.localEulerAngles = Vector3(0,0,270);
					} else {
						itemHeld.localPosition = Vector3(-5,4,5); 
						itemHeld.localEulerAngles = Vector3(23, 6, 103);
					}
				} else {
					itemHeld.eulerAngles = Vector3(270,0,0);
					itemHeld.rigidbody.detectCollisions = true;
					itemHeld.rigidbody.isKinematic = false;
					itemHeld.rigidbody.collider.enabled = true;
				}
				
				
				setParent = null;
		
				itemHeld = null;
			}
			notDone = false;
			return;
		}
		
		if (waitingAnim.Contains("passItemUp") && (passUp.time > (passUp.length * 2)) && notDone) {
		
			passUp.enabled = false;
			notDone = false;
			stopPassUp = true;
			return;
		}
		
		if (waitingAnim.Contains("passItemDown") && animation.IsPlaying("passItemUp")) {
			//Debug.Log("Special case, where we are trying to pass an item over, ignore.");
			return;
		}
		
		if (waitingAnim.Contains("standUp") && !animation.IsPlaying(waitingAnim)) {
			// we're done standing up
		
			// should toggle reposition
			transform.position = Vector3(transform.position.x + (transform.forward.x * 0.77), transform.position.y, transform.position.z + (transform.forward.z * 0.77));
			
			waitForAnimDone = false;
			
			return;
		}
		
		if (waitingAnim.Contains("sitDown")) {
			waitForAnimDone = false;
			areSitting = true;
		}
		
		if (!animation.IsPlaying(waitingAnim) || (waitingAnim.Contains("standUp") && standUp.time > standUp.length) || stopPassUp) {
			if (waitingAnim.Contains("pickFromTable") && !areHolding) {
				// now that we've put the item down, we should crossfade with neutral arms
				animation.CrossFade(neutralAnimName);
			}
			waitForAnimDone = false;
			incidentWait = false;
			stopPassUp = false;
			doingPickUp = false;
			
			if (areHolding && (waitingAnim.Contains("pickFromTable") || waitingAnim.Contains("pickFromGround") || waitingAnim.Contains("passItemUp"))) {
			
				pickGround.enabled = false;
				pickTable.enabled = false;
				passUp.enabled = false;
				hold();
			}
			
		
		} else {
			return;
		}
	} 
	
	if (!waitForAnimDone) {
		waitingAnim = "";
		if (itemHeld != null && doPickUp != true) {
			areHolding = true;
		}
		
		
		// assume we want to reset the main unless we specify not to...
		changed = true;
		
		// check if we're idle (ie charspeed == 0)
		if (CharSpeed == 0) {
			// since we're idle, we can do things like sit down, 
			// stand up, stay sitting, startle, or kick.
			
			// Determine if any of these need to be done. 
			if (doSit) {
				//Debug.Log("Should toggle sitting");
				// move from sitting to standing and vice versa 
				toggleSit();
			} else if (areSitting) {
				//Debug.Log("Should play sitting");
				// can perform sitting animations (talk left/right, idle, nervous)
				sit();
			} else if (doStartle) {
				Debug.Log("Should startle");
				// run Startle Animation
				startStartle();
				changed = false;
			} else if (doKick) {
				//Debug.Log("Should kick");
				// run kick Animation
				startKick();
			} 
		}
		
		if (doTalk) {
//			Debug.Log("Should toggle idleTalk");
			// do idle talk
			toggleTalk();
		}
		
		if (doCheck) {
			//Debug.Log("Should check over shoulder");
			// do shoulder check animation
			doShoulderCheck();
		}
		
		if (doPickUp) {
			//Debug.Log("Should toggle Pick Up");
			// pick up or put down object
			if (!areHolding) 
				changed = false;
			toggleHold();
		} else if (areHolding) {
		
			// make sure we're still *holding* something first
			
			if (itemHeld != null && transform.FindChild("SK Root/SK Pelvis/SK Spine/SK Spine1/SK Spine2/SK Spine3/SK Neck/SK R Clavicle/SK R UpperArm/SK R Forearm/SK R Hand/" + itemHeld.name)) {
				hold();
				//changed = false;
				//Debug.Log(transform.name + " is HOLDING a " + itemHeld);
			} else {
			//	Debug.Log(transform.name + " thought it was holding something but IT IS NOT!!!!!");
				areHolding = false;
				itemHeld = null;
				holdIdle.enabled = false;
			}
			//Debug.Log("Should play holding");
			// perform various holding actions (play with, look at, drink)
		}
		

		
		if (doWave) {
			//Debug.Log("Should Toggle Wave");
			// start or stop waving
			toggleWave();
		}
		
		if (doNod) {
			//Debug.Log("Should Toggle Nod");
			toggleNod();
		} else if (doShake) {
			//Debug.Log("Should Toggle Shake");
			toggleShake();	
		}
		
		// should we reset the walk/run/idle animation or emotion?
		if (changed) {
			
			//Debug.Log("We need to reset the main animation");
			//changed = false;
			setMainAnimation();
		}
	} 
	
}

internal var blendWalk : float = 1;
/*
* This function controls the main animation state for the character, including the emotional influence for that state. These
* states include standing idle, walking and running. 
*/
function setMainAnimation() {
	if (motor != null) {
		if ((motor.inputMoveDirection.x < 0.1 && motor.inputMoveDirection.z < 0.1) && (motor.inputMoveDirection.x > -0.1 && motor.inputMoveDirection.z >- 0.1)) {
			//gameObject.animation.CrossFade("stand_Idle");
			CharSpeed = 0;
			//standIdle.enabled = true;
			animation.CrossFade(standIdleAnimName);
		
			//walk.enabled = false;
			//walkArm.enabled = false;
		} else {
			//gameObject.animation.CrossFade("walkNeutral");
			walk.speed = (1 / walkSpeedDefault) * CharSpeed;
			CharSpeed = 1.5;
			//walk.enabled = true;
			animation.CrossFade(walkAnimName);

		}
	
	} else {
		
		if (waitingAnim.Contains("standUp") || areSitting) {
			// we're sitting, so don't worry about any of the below
			//Debug.Log("we're suppose to be standing");
			
			// we should probably turn off all, so that sitting overrides...
			happy.enabled = false;
			happyArm.enabled = false;
			sad.enabled = false;
			sadArm.enabled = false;
			angry.enabled = false;
			angryArm.enabled = false;
			afraid.enabled = false;
			afraidArm.enabled = false;
			afraidHead.enabled = false;
			
			return;
		}
		
		if (happyWeight != 0){
			happy.enabled = true;
			happyArm.enabled = true;
			happy.weight = happyWeight;
			happyArm.weight = happyWeight;
			animation.CrossFade(happyAnimName, 0.5, PlayMode.StopSameLayer);
			animation.CrossFade(happyArmAnimName, 0.5, PlayMode.StopSameLayer);
		} else {
			happy.weight = 0;
		//	happy.enabled = false;
			happyArm.enabled = false;
		}
		
		if (sadWeight != 0){
			sad.enabled = true;
		//	sadArm.enabled = true;
			sad.weight = sadWeight;
		//	sadArm.weight = Mathf.Clamp(0,sadWeight, 0.6);
			
			//if (sadArm.weight > 0.6){
			//	sadArm.weight = 0.6;
			//}
			
			animation.CrossFade(sadAnimName, 0.5, PlayMode.StopSameLayer);
			animation.CrossFade(sadArmAnimName, 0.5, PlayMode.StopSameLayer);
		} else {
			sad.weight = 0;
		//	sad.enabled = false;
			sadArm.enabled = false;
		}
		
		if (angryWeight != 0){
			angry.enabled = true;
		//	angryArm.enabled = true;
			angry.weight = angryWeight;
		//	angryArm.weight = angryWeight;
			animation.CrossFade(angryAnimName, 0.5, PlayMode.StopSameLayer);
			animation.CrossFade(angryArmAnimName, 0.5, PlayMode.StopSameLayer);
		} else {
			angry.weight = 0;
		//	angry.enabled = false;
			angryArm.enabled = false;
		}
		
		if (afraidWeight != 0){
			//afraid.enabled = true;
			afraidArm.enabled = true;
			afraidHead.enabled = true;
			
			//afraid.weight = afraidWeight;
			afraidArm.weight = afraidWeight-0.3;
			
			// need to blend in a different face (with no emotions) to cover afraid
			
			//animation.Blend(afraidAnimName);
			animation.CrossFade(afraidArmAnimName, 0.5, PlayMode.StopSameLayer);
			//afraidHead.wrapMode = WrapMode.Loop;
			animation.CrossFade(afraidHeadAnimName, 0.5, PlayMode.StopSameLayer);
			//checkAnimationsPlaying();
		} else {
			afraid.weight = 0;
	//		afraid.enabled = false;
			afraidArm.enabled = false;
			afraidHead.enabled = false;
		}
		
		
	//	if (angryWeight == 0 && sadWeight == 0 && happyWeight == 0 && afraidWeight == 0) {
			// no emotions, so do neutral walk and run	
	//	}
		
		if (doRun && CharSpeed > 0) {
			CharSpeed = runSpeedDefault;
			run.speed = (1 / runSpeedDefault) * CharSpeed;
			run.weight = 0.8;
			run.enabled = true;
			animation.CrossFade(runAnimName);
			runArm.enabled = true;
			runArm.weight = 0.5;
			animation.CrossFade(runArmAnimName);
		
			walk.enabled = false;
			walkArm.enabled = false;
			standIdle.enabled = false;
			
		} else if (CharSpeed > 0) {
			//walk.speed = blendWalk * CharSpeed;
			walk.speed = (1 / walkSpeedDefault) * CharSpeed;
			walk.enabled = true;
			animation.Blend(walkAnimName);
			walkArm.enabled = true;
			animation.Blend(walkArmAnimName); 
			
			run.enabled = false;
			runArm.enabled = false;
			standIdle.enabled = false;
		} else {
		//	Debug.Log("Play neutral");
			if (areSitting) {
				sit();
			} else {
		
				
				standIdle.enabled = true;
				animation.Blend(standIdleAnimName);
			
				walk.enabled = false;
				walkArm.enabled = false;
				run.enabled = false;
				runArm.enabled = false;
			}
		}		
	}
}

var wasDrinking : boolean = false;
var lookIdle : boolean = false;
var waveDone : boolean = false;
/*
* Used to control various animations when holding item
*/
function hold() {
	if (playLookAtItem && !lookIdle) {
		//lookAt.time = 5;
		//Debug.Log("look at item");
		animation.CrossFade(lookAtAnimName);
		lookIdle = true;
		//animation.PlayQueued(lookAtIdleAnimName, QueueMode.CompleteOthers);
	} else if (playDrink) {
		//Debug.Log("Play Drink anim");
		wasDrinking = true;
		animation.CrossFade(drinkAnimName);
		
	} else if (playWithIPad) {
		//wasDrinking = true;
		//("Play Ipad Anim");
		animation.CrossFade(playIPadAnimName);
	} else {
		if (waveDone) {
		//	Debug.Log("BlendArm");
			animation.CrossFade(neutralAnimName);
			//wasDrinking = false;
			waveDone = false;
		}
		if (wasDrinking) {
		//	Debug.Log("WAS DRINKING blEND STUFF");
			animation.CrossFade(neutralAnimName);
			neutral.AddMixingTransform(spine); 
			animation.CrossFade(neutralAnimName);	
			neutral.AddMixingTransform(root);		
			wasDrinking = false;
		}
		if (lookIdle && !playLookAtItem) {
		//	Debug.Log("Look Idle and !Play");
			animation.CrossFade(neutralAnimName);
			neutral.AddMixingTransform(spine); 
			animation.CrossFade(neutralAnimName);	
			neutral.AddMixingTransform(root);		
			lookIdle = false;
			
		} 
		
		if (lookIdle && playLookAtItem) {
			animation.CrossFade(lookAtIdleAnimName);
		} else {
			lookIdle = false;
			animation.CrossFade(holdIdleAnimName);
		}
	}
}

var doPick : boolean = false;
var onGround : boolean = false;
var throwOut : boolean = false;
var doPass : boolean = false;
/*
* Used to run the pick up or put down animation
*/
function toggleHold() {
	doPickUp = false;
	pickTable.AddMixingTransform(spine);
	pickTable.AddMixingTransform(root);
	if (areHolding) {
		//("Put Down Item");
		//holdIdle.enabled = true;
		pickTable.speed = -1;
		pickTable.time = pickTable.length;
		notDone = true;
		
		if(areSitting) {
			//Debug.Log("Need to PUT DOWN while sitting");
			pickTable.RemoveMixingTransform(root);
			//pickTable.AddMixingTransform(spine);
		} else {
			pickTable.RemoveMixingTransform(spine);
		}
		
		// should we be throwing the item out?? If set and sitting, ignore
		if (throwOut && !areSitting) {
			animation.Play(throwItemAnimName);
			waitingAnim = "throwItem";
			throwOut = false;
		} else if (doPass && !areSitting) {
			// we're getting rid of item, so put arm 'down' after
			//Debug.Log("TIME TO DO PASS ITEM DOWN");
			passUp.wrapMode = WrapMode.Once;
			animation.Play(passUpAnimName);
			
			passDown.speed = 0.5;
			animation.PlayQueued(passDownAnimName, QueueMode.CompleteOthers); 
			//animation.Play(passDownAnimName);
			waitingAnim = "passItemDown";	 
			doPass = false;
		} else {
			animation.Play(pickTableAnimName);
			waitingAnim = "pickFromTable";
		}
		areHolding = false;
	} else {
		//Debug.Log("Pick Up Item");
		//doPick = true;
		areHolding = true;
		neutral.enabled = false;
		notDone = false;
		
		if(areSitting) {
			//Debug.Log("Need to PICK UP while sitting");
			pickTable.RemoveMixingTransform(root);
			//pickTable.AddMixingTransform(spine);
		} else {
			pickTable.RemoveMixingTransform(spine);
			//pickTable.AddMixingTransform(root);
		}
		
		// should we be pick off the ground? If set and sitting, ignore
		if (onGround && !areSitting) {
			animation.Play(pickGroundAnimName);
			waitingAnim = "pickFromGround";
			onGround = false;
		} else if (doPass) {
			//passDown.speed = -1;
			//passDown.time = passDown.length;
			// we are receiving item from someone doing passDown - should reach over, take, and put arm down
			// so should be a single pingpong of passUp
			passUp.wrapMode = WrapMode.PingPong;
			animation.Play(passUpAnimName);
			waitingAnim = "passItemUp";
			notDone = true;
			doPass = false;
		} else {
			pickTable.speed = 1;
			pickTable.time = 0;
			animation.Play(pickTableAnimName);
			waitingAnim = "pickFromTable";
		}
		notDone = true;
		doingPickUp = true;
		//changed = false;
	}
	
	waitForAnimDone = true;
}


/*
* Used to start/stop nod head animation
*/
function toggleNod() {
	doNod = false;
		
	nodHead.enabled = true;
	animation.CrossFade(nodHeadAnimName);

}

var showTalk : boolean = false;
/*
* Decide whether to show or stop showing idle talk
*/
function toggleTalk() {

	doTalk = false;
	//idleTalk.AddMixingTransform(spine);
	idleTalk.AddMixingTransform(head);
	
	if (showTalk) {
		WaitForSeconds(Random.Range(0,1.0));
		//Debug.Log("SHOW TALK");
		/*if (areSitting || areHolding || CharSpeed > 0) {
			// don't mix arms (head)
			idleTalk.RemoveMixingTransform(spine);
		} else {
			idleTalk.RemoveMixingTransform(head);
		}*/
		idleTalk.enabled = true;
		animation.CrossFade(idleTalkAnimName);
		showTalk = false;
	} else {
		idleTalk.enabled = false;
		//if (areSitting) {
			
		//	sit();
		//} else {
			//setMainAnimation();
		//}
		showTalk = true;
	}
}

/*
* Used to start/stop shake head animation
*/
function toggleShake() {
	doShake = false;

	shakeHead.enabled = true;
	animation.CrossFade(shakeHeadAnimName);
}


/*
* Used to start/stop wave animation
*/
function toggleWave() {
	//weightwaveBoth = val;
	doWave = false;
	
	if(showWave){
		wave.AddMixingTransform(shoulderR);
		wave.AddMixingTransform(shoulderL);
		
		// should wave with right hand, unless holding object, then wave left (or specified left)
		if (areHolding || leftHand) {
			//Debug.Log("Should Wave Left");
			wave.RemoveMixingTransform(shoulderR);
		} else {
			wave.RemoveMixingTransform(shoulderL);
		}
		
		wave.enabled = true;
		wave.speed = weightwaveSpeed;	
		wave.weight = weightwaveBoth;
		//animation.Blend(waveAnimName);
		animation.CrossFade(waveAnimName);
		showWave = false;
		
	}else{
		wave.enabled = false;
		animation.CrossFade(neutralAnimName);
		showWave = true;
		if (areHolding)
			waveDone = true;
	}
}

function startKick() {
	//doKick = false;
	//angryKick = true;
	waitingAnim = "kick";
	waitForAnimDone = true;
	doKick = false;
	//Debug.Log("Time to KICK " + transform.name);
	if (angryKick) {
		
		//Debug.Log("Angry Kick");
		if(leftFoot){
			kick.enabled = true;
			animation.CrossFade(kickAnimName);
		}else{
			kick_left.enabled = true;
			animation.CrossFade(kickAnimName_left);
		}
	
	} else {
		
		//Debug.Log("Normal Kick");
		if(leftFoot){
			kick2.enabled = true;
			//kick2.speed = 3.0;
			animation.CrossFade(kick2AnimName);
		}else{
			//kick2_left.speed = 5.0;
			kick2_left.enabled = true;
			//print("kick left is enabled"); 
			animation.CrossFade(kick2AnimName_left);
		}
	}
	
	changed = true;
	//setMainAnimation();
}

function doShoulderCheck() {
	doCheck = false;
	if (doRun && CharSpeed > 0) {
		// speed up animation
		checkOver.speed = 3;
	} else {
		checkOver.speed = 3;
	}
	// make sure we're not shaking or nodding
	shakeHead.enabled = false;
	nodHead.enabled = false;
	// check if we are talking
	if (animation.IsPlaying("idleTalk")) {
		// need to resume talk after shoulder check
		showTalk = true;
		doTalk = true;
	}
	animation.Play(checkOverAnimName);
	waitingAnim = "checkOverShoulder";
	waitForAnimDone = true;
}

//var startled : boolean = true;
function startStartle() {
	print("ANIMATION: Show startle");
	
	doStartle = false;
	startle.speed = weightStartle;
	startle.weight = weightStartle;
	//startle.time = startle.length;
	animation.Play(startleAnimName);
	
	while (Mathf.Abs(animation[startleAnimName].time) < startle.length * 2) {
		//Debug.Log("Startle noramlized time is " + animation[startleAnimName].time);
		yield;
	}
	
	animation.Stop(startleAnimName);
}


var playTalkLeft : boolean = false;
var playTalkRight : boolean = false;
var playSitIdle : boolean = false;
var playSitNervous : boolean = false;
/*
* Deals with various sit animations
*/
function sit() {
	
	if ((animation.IsPlaying("sitDown") && sitDown.time < (sitDown.length / 2)) || waitingAnim.Contains("standUp")) {
		return;
	}
	//Debug.Log("Should choose new animation to play while sitting");
	if (playTalkLeft) {
		//Debug.Log("should play talk to left");
		animation.CrossFade(sitTalkLeftAnimName);
	} else if (playTalkRight) {
		animation.CrossFade(sitTalkRightAnimName);
	} else {
		if (areHolding) {
			hold();
		}
		
		// play nervous if we specify it or the person is suppose to be afraid
		if (playSitNervous || emotion == 4) {
			sitNervous.speed = 0.75;
			animation.CrossFade(sitNervousAnimName);
		} else {
			animation.CrossFade(sitIdleAnimName);
		}
	}	
		
}

function toggleSit() {
//	Debug.Log("Toggle SIT animation");
	
	doSit = false;
	
	if (areSitting) {
		//Debug.Log("Should stand up");
		
		
		standUp.speed = 1;
		// stop sitting animations first??? 
		animation.Stop();
		animation.Play(standUpAnimName);

		areSitting = false;
		notDone = true;
		waitingAnim = "standUp";
		//checkAnimationsPlaying();
		//waitForAnimDone = true;
		
		//changed = true;
	} else {
		//Debug.Log("Should sit down + AND MOVE OVER");
		
		//sitDown.speed = 0.2;
		neutral.enabled = false;
		
		animation.Play(sitDownAnimName);
				//Debug.Log(transform.name + " character is starting sit at " + transform.position);
		var forVec : Vector3 = transform.forward;
		transform.position = Vector3(transform.position.x + (forVec.x * 0.77), transform.position.y, transform.position.z + (forVec.z * 0.77));
		//transform.position = Vector3(transform.position.x - (forVec.x * 0.77), transform.position.y, transform.position.z - (forVec.z * 0.77));
		//Debug.Log(transform.name + " should have now moved position to " + transform.position + " using forward vector " + forVec);
		transform.Rotate (0,180,0);		
		//Debug.Log(transform.name + " should have rotated as well.");

		//animation.PlayQueued("sitIdle", QueueMode.CompleteOthers);
		areSitting = true;
		waitingAnim = "sitDown";
	}
	
	//if (animation.IsPlaying("sitDown")) 
	//	Debug.Log("WE ARE SITTING DOWN");
	
	waitForAnimDone = true;

}

function getEmotion() : int {
	return emotion;
}

var emotion : int = -1;

function setSpeed() {
//	Debug.Log("Setting speed for " + transform.name);
	if (emotion == 1) {
		CharSpeed = 2;
	} else if (emotion == 2) {
		CharSpeed = 1;
	} else if (emotion == 3) {
		CharSpeed = 2.25;
	} else if (emotion == 4) {
		CharSpeed = 1.25;
	} else {
		CharSpeed = 2;
	}
	changed = true;
}

function checkAnimationsPlaying() {
	var play : String = "Animations that are playing for " + transform.name + ": \n";
	
	if (animation.IsPlaying("walkCycle"))
		play = play +  "walkCycle\n";
	if (animation.IsPlaying("runCycle"))
		play = play +  "runCycle\n";
	if (animation.IsPlaying("walkCycleArm"))
		play = play +  "walkCycleArm\n";
//	if (animation.IsPlaying("ArmOffset"))
//		play = play +  "ArmOffset\n";
	if (animation.IsPlaying("kick_angry"))
		play = play +  "kick_angry\n";
	if (animation.IsPlaying("kick_Normal"))
		play = play +  "kick_Normal\n";
	if (animation.IsPlaying("kick_angry_left"))
		play = play +  "kick_angry_left\n";
	if (animation.IsPlaying("kick_Normal_left"))
		play = play +  "kick_Normal_left\n";
	if (animation.IsPlaying("turnsit"))
		play = play +  "turnsit\n";
	if (animation.IsPlaying("waveBoth"))
		play = play +  "waveBoth\n";
	if (animation.IsPlaying("AngryIdle"))
		play = play +  "AngryIdle\n";
	if (animation.IsPlaying("SurprisedIdle"))
		play = play +  "SurprisedIdle\n";
	if (animation.IsPlaying("startled_mild"))
		play = play +  "startled_mild\n";
	if (animation.IsPlaying("JoyIdle"))
		play = play +  "JoyIdle\n";
	if (animation.IsPlaying("Neutral"))
		play = play +  "Neutral\n";
	if (animation.IsPlaying("SadIdle"))
		play = play +  "SadIdle\n";
	if (animation.IsPlaying("NeutralIdleArm"))
		play = play +  "NeutralIdleArm\n";
	if (animation.IsPlaying("AngryIdleArm"))
		play = play +  "AngryIdleArm\n";
	if (animation.IsPlaying("walkAnim"))
		play = play +  "walkAnim\n";
	if (animation.IsPlaying("SurprisedIdleArm"))
		play = play +  "SurprisedIdleArm\n";
	if (animation.IsPlaying("SadIdleArm"))
		play = play +  "SadIdleArm\n";
	if (animation.IsPlaying("JoyIdleArm"))
		play = play +  "JoyIdleArm\n";
	if (animation.IsPlaying("kneelIdle"))
		play = play +  "kneelIdle\n";
	if (animation.IsPlaying("drinkFromCup"))
		play = play +  "drinkFromCup\n";
	if (animation.IsPlaying("holdIdle"))
		play = play +  "holdIdle\n";
	if (animation.IsPlaying("pickFromTable"))
		play = play +  "pickFromTable\n";
	if (animation.IsPlaying("lookAtHoldingItem"))
		play = play +  "lookAtHoldingItem\n";
	if (animation.IsPlaying("playIpad"))
		play = play +  "playIpad\n";
	if (animation.IsPlaying("sitDown"))
		play = play +  "sitDown\n";
	if (animation.IsPlaying("sitIdle"))
		play = play +  "sitIdle\n";
	if (animation.IsPlaying("sitNervous"))
		play = play +  "sitNervous\n";
	if (animation.IsPlaying("sitTalkToLeft"))
		play = play +  "sitTalkToLeft\n";
	if (animation.IsPlaying("sitTalkToRight"))
		play = play +  "sitTalkToRight\n";
	if (animation.IsPlaying("standUp"))
		play = play +  "standUp\n";
	if (animation.IsPlaying("pickFromGround"))
		play = play +  "pickFromGround\n";
	if (animation.IsPlaying("passItemUp"))
		play = play +  "passItemUp\n";
	if (animation.IsPlaying("throwItem"))
		play = play +  "throwItem\n";
	if (animation.IsPlaying("nodHead"))
		play = play +  "nodHead\n";
	if (animation.IsPlaying("checkOverShoulder"))
		play = play +  "checkOverShoulder\n";
	if (animation.IsPlaying("lookAtHoldingItemIdle"))
		play = play +  "lookAtHoldingItemIdle\n";
	if (animation.IsPlaying("passItemDown"))
		play = play +  "passItemDown\n";
	if (animation.IsPlaying("idleTalk"))
		play = play +  "idleTalk\n";
	if (animation.IsPlaying("pointAtItem"))
		play = play +  "pointAtItem\n";
	if (animation.IsPlaying("shakeHead"))
		play = play +  "shakeHead\n";
	if (animation.IsPlaying("idleFear"))
		play = play + "idleFear\n";
	
	play = play + "\nSpeed is " + CharSpeed;
	Debug.Log(play);
			
}

/*
* After a pick up call is made, when the animation gets halfway through, it broadcasts a message 
* to the performPickUp functions telling them to actually do the pickup now. Because there are 
* mulitiples of these, it needs to be guarded by doPickUp (so only the correct performPickUp
* is actually activiated. (NOTE MIGHT HAVE PROBLEM IF MULTIPLE CHARACTERS ARE SUPPOSE TO DO SO
* AT SAME TIME....)
*/
function performPickUp() {
	
	var rHand : Transform = transform.Find("SK Root/SK Pelvis/SK Spine/SK Spine1/SK Spine2/SK Spine3/SK Neck/SK R Clavicle/SK R UpperArm/SK R Forearm/SK R Hand").transform;
	var root : Transform = transform.Find("SK Root").transform;

			
	itemHeld.rigidbody.isKinematic = true;
	itemHeld.rigidbody.detectCollisions = false;

	itemHeld.parent = rHand;
	
	if (itemHeld.name.Contains("newspaper")) {
		itemHeld.transform.localPosition = Vector3(-4,2,0);
		itemHeld.localEulerAngles = Vector3(355,200,180);	
	} else if (itemHeld.name.Contains("papercup")) {
		itemHeld.localPosition = Vector3(-4,2.3,0.5);
		itemHeld.localEulerAngles = Vector3(355,0,180);
	} else if (itemHeld.name.Contains("book")) {
		itemHeld.localPosition = Vector3(-4,4,3);
		itemHeld.localEulerAngles = Vector3(0,0,270);
	} else {
		itemHeld.localPosition = Vector3(-5,4,5); //-4, 0.308, 12.7);
		itemHeld.localEulerAngles = Vector3(23, 6, 103);
	}
	
}