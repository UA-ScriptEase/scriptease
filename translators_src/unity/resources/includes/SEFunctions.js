/**
 * This file provides various functions we use in different places. It saves us
 * from rewriting code all the time.
 *
 * @author ScriptEase Team
 */

/** 
 * We need this because the default PlayClipAtPoint doesn't let us
 * stop the clip from playing! 
 */
static function PlayClip(clip: AudioClip, pos: Vector3) : GameObject {
  var tempGO : GameObject;
  var aSource : AudioSource;
  
  tempGO = GameObject("TemporaryDialogueAudio");
  aSource = tempGO.AddComponent(AudioSource);
  
  tempGO.transform.position = pos;
  aSource.clip = clip;

  aSource.Play();
  // destroy object after clip duration
  Destroy(tempGO, clip.length); 
  
  return tempGO;
}