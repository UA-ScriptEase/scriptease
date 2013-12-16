// Behaviour pattern include file
#include "nw_i0_generic"
#include "i_se_structs"
#include "i_se_aux"

// ------------ CONSTANTS ----------------
const int EVENT_BEHAVIOUR_LOOP = 31415;
const int EVENT_BEHAVIOUR_LATENT = 31417;

const string SCEZ_LOOP_RUNNING_FLAG = "SCEZ_LoopIsRunning";

const string QUEUE_BEFORE_SYNC_STRING = "beforeSyncValue";
const string QUEUE_AFTER_SYNC_STRING = "afterSyncValue";
const string QUEUE_COLLABORATOR_STRING = "collab";
const string QUEUE_TIMER_STRING = "scez_timer";
const string QUEUE_PRIORITY_STRING = "scez_priority";

const string BEHAVIOUR_PRIORITY_QUEUE_STRING = "priorityQueue";
const string BEHAVIOUR_GARBAGE_BIN_STRING = "garbageBin";


const string BEHAVIOUR_STACK_STRING = "behaviour";
const int BEHAVIOUR_STACK_BOTTOM = 1;
const string BEHAVIOUR_SUSPENDED_PRIORITYQUEUE_STRING = "behaviourSuspended";
const string BEHAVIOUR_PRIORITY_STACK_STRING = "priority";

const string BEHAVIOUR_LATENT_MAILBOX_STRING = "newLatents";

const string BEHAVIOUR_REACTIVE_MAILBOX_STRING = "requestedReactions";

const string BEHAVIOUR_PENDING_PHASE_ARRAY_STRING = "pendingPhases";
const string BEHAVIOUR_CURRENT_UNIQUE_ID = "behaviourUniqueID";

const int SCEZ_MED_TIMEOUT_CYCLES = 200;
const int SCEZ_LONG_TIMEOUT_CYCLES = 400;
const int SCEZ_NOTIMEOUT = 0;
const int SCEZ_MEDTIMEOUT = 1;
const int SCEZ_LONGTIMEOUT = 2;


// ------------ DECLARATIONS -------------
void SCEZ_Behaviour_IncrementAllTimers(object actor);
void SCEZ_Behaviour_IncrementTimer(object actor, string queue);
//int SCEZ_Behaviour_AreCollaborators(object actor1, object actor2);
int SCEZ_Behaviour_IncrementQueueSync(string queue);
void SCEZ_Behaviour_FillPriorityStack(object actor, string behaviourMailbox);
void SCEZ_Behaviour_Loop();
void SCEZ_Behaviour_QueuePriorityBehaviours();
int SCEZ_Behaviour_QueueHasPriority(object actor, string queue);
int SCEZ_Behaviour_GetQueuePriority( object actor, string queueName );
string SCEZ_Behaviour_GetStack (object actor, string stackName);
object SCEZ_Behaviour_GetCollaboratorForQueue(object actor, string queue);
int SCEZ_Behaviour_PriorityQueue_BinarySearch(object actor, string storageArray, int priority, int low, int high);
void SCEZ_Behaviour_DestroyQueueAt(object actor, string arrayName, int index);
void SCEZ_Behaviour_DestroyQueue(object actor, string queueName);
string SCEZ_Behaviour_GetExecutionStack(object actor);
void SCEZ_Behaviour_PriorityQueueAdd(object actor, string priorityQueueName, string newQueue, int priority=-1);
string SCEZ_Behaviour_PriorityQueue_PopFirstReady(object actor, string arrayName);
string SCEZ_Behaviour_PriorityQueue_PeekFirstReady(object actor, string arrayName);
int SCEZ_Behaviour_PriorityQueue_FindFirstReady(object actor, string arrayName);
void SCEZ_Behaviour_Signal(object actor);
void SCEZ_Behaviour_SetQueuePriority( object actor, string queueName, int priority );
string SCEZ_Behaviour_BuildUniqueQueueName(string behaviourName, object owner, object collaborator = OBJECT_INVALID);
int SCEZ_Behaviour_IsMyTurn (object actor, string queueName );
void SCEZ_Behaviour_InterruptWith (object actor, string queueName);
int SCEZ_Behaviour_QueueHasInterruptPriority(object actor, string queueName);
string SCEZ_Behaviour_PriorityQueueRemoveElement( object owner, string priorityQueueName, string element );
void SCEZ_Behaviour_PriorityQueue_Destroy(object actor, string queueName);
int SCEZ_Behaviour_GetAfterSyncForQueue(object actor, string queueName);

object SCEZ_Behaviour_GetCollaboratorForQueue(object actor, string queue){
  return GetLocalObject(actor, queue+QUEUE_COLLABORATOR_STRING);
}

void SCEZ_Behaviour_SetCollaboratorForQueue(object actor, string queue, object newCollaborator){
  SetLocalObject(actor, queue+QUEUE_COLLABORATOR_STRING, newCollaborator);
}

// behaviourName is the name given to the behaviour from the ScriptEase GUI.
// Collaborator defaults to OBJECT_INVALID for convenience
// If collaborator is OBJECT_INVALID, then the generated queueName will represent an independent behaviour
string SCEZ_Behaviour_BuildQueueName(string behaviourName, object owner, object collaborator = OBJECT_INVALID){
  string newQueue = behaviourName;

  if(owner == OBJECT_INVALID){
    return "BAD QUEUENAME BUILD CALL";
  }

  newQueue += "," + ObjectToString(owner);

  newQueue += "," + ObjectToString(collaborator);

  return newQueue;
}

int SCEZ_Behaviour_QueueBelongsTo(object actor, string queueName) {
  int charsFromRHS = GetStringLength(queueName) - (FindSubString(queueName, ",")+1);
  string actorIDs = GetStringRight( queueName, charsFromRHS );
  string queueOwnerID = GetStringLeft(actorIDs, FindSubString( actorIDs, "," ));
  //debug("The queue: "+ queueName +" belongs to "+ queueOwnerID +", derived from "+ actorIDs +" and my ID is "+ ObjectToString(actor));
  if( queueOwnerID == ObjectToString(actor) ) {
    return 1;
  }
  return 0;
}

/*
  There are circumstances in which one collaborator can finish and attempt to re-initiate a collaborative before the other
  collaborator has finished the first collaboration. Because the actors and behaviour name are the same, the queueName
  generated by buildQueueName is identical in both runs. This confuses the lagging actor because some of the queue-specific
  variables are overwritten. Thus, we've created this function to provide a unique ID for each queue.
*/
string SCEZ_Behaviour_BuildUniqueQueueName(string behaviourName, object owner, object collaborator = OBJECT_INVALID) {
  int uniqueID = GetLocalInt(GetModule(), BEHAVIOUR_CURRENT_UNIQUE_ID);
  string uniqueQueueName = SCEZ_Behaviour_BuildQueueName(behaviourName +":"+ IntToString(uniqueID), owner, collaborator);

  ++uniqueID;

  // reset the unique ID every once in a while to avoid integer overflows.
  if(uniqueID > 10000){ // this number is arbitrary - remiller
    uniqueID = 0;
  }

  SetLocalInt(GetModule(), BEHAVIOUR_CURRENT_UNIQUE_ID, uniqueID);
  return uniqueQueueName;

}

void SCEZ_Behaviour_MailReactive(object recipient, string proactiveQueueName){
  //debug("Mail Reactive to "+ GetName(recipient) +" for queue: "+ proactiveQueueName );
  int priority = SCEZ_Behaviour_GetQueuePriority(OBJECT_SELF, proactiveQueueName);

  //debug("Setting "+ GetName(recipient) +"'s queue: "+ proactiveQueueName +" priority to: "+ IntToString(priority));

  SCEZ_Behaviour_SetQueuePriority(recipient, proactiveQueueName, priority);
  SCEZ_Behaviour_SetCollaboratorForQueue(recipient, proactiveQueueName, OBJECT_SELF);
  SCEZ_Behaviour_PriorityQueueAdd(recipient, BEHAVIOUR_REACTIVE_MAILBOX_STRING, proactiveQueueName, priority); // + "," + IntToString(index), priority);
 //debug("SIGNAL from MailReactive");
 // SCEZ_Behaviour_Signal(recipient);
}

void SCEZ_Behaviour_UnsetSignal(object actor) {
 //debug(GetName(actor) + " has been un-signalled. ");
  SetLocalInt(actor, "sczm__signal", FALSE);
}

int SCEZ_Behaviour_IsSignaled(object actor) {
  return GetLocalInt(actor, "sczm__signal") == TRUE;
}

int SCEZ_Behaviour_IsDialogueEvent() {
  return GetUserDefinedEventNumber() == EVENT_DIALOGUE;
}

int SCEZ_Behaviour_GetBeforeSyncForQueue(object actor, string queueName){
  return GetLocalInt(actor, queueName+QUEUE_BEFORE_SYNC_STRING);
}

int SCEZ_Behaviour_GetAfterSyncForQueue(object actor, string queueName){
  return GetLocalInt(actor, queueName+QUEUE_AFTER_SYNC_STRING);
}

int SCEZ_Behaviour_IsMyTurn (object actor, string queueName ){
  object collaborator = SCEZ_Behaviour_GetCollaboratorForQueue(actor, queueName);

  // If there is no collaborator, it is always the actor's turn.
  if(collaborator == OBJECT_INVALID || collaborator == actor) {
    //debug("It's my turn" + ((collaborator == OBJECT_INVALID)? " because my collaborator doesn't exist for " + queueName  + "!": "!"));
    return TRUE;
  }
  // If there IS a collaborator, and actor is ahead in the collaboration, it is NOT the actor's turn.
  else if( SCEZ_Behaviour_GetBeforeSyncForQueue(actor, queueName) > SCEZ_Behaviour_GetAfterSyncForQueue(collaborator, queueName) ) {
    //debug("It is not my turn yet. My sync: " + IntToString(SCEZ_Behaviour_GetBeforeSyncForQueue(actor, queueName)) + ", their sync: " + IntToString(SCEZ_Behaviour_GetAfterSyncForQueue(collaborator, queueName)));
    return FALSE;
  }

  // Since the actor has a collaborator, and is not ahead of him/her in the collaboration, it is the actor's turn
  return TRUE;
}

void SCEZ_Behaviour_IncrementSyncForQueue(object actor, string queueName){
  int newSyncValue = GetLocalInt(actor, queueName+QUEUE_BEFORE_SYNC_STRING) + 1 ;
  //debug("Incrementing my sync value on "+ queueName +" to "+ IntToString(newSyncValue) );
  SetLocalInt(actor, queueName+QUEUE_BEFORE_SYNC_STRING, newSyncValue);

//  debug("=======================: My public sync: " + IntToString(SCEZ_Behaviour_GetAfterSyncForQueue(OBJECT_SELF, queueName)) + " private sync:  " + IntToString(SCEZ_Behaviour_GetBeforeSyncForQueue(OBJECT_SELF, queueName)));
//  debug("=======================: Their public sync: " + IntToString(SCEZ_Behaviour_GetAfterSyncForQueue(SCEZ_Behaviour_GetCollaboratorForQueue(actor, queueName), queueName)) + " private sync: " + IntToString(SCEZ_Behaviour_GetBeforeSyncForQueue(SCEZ_Behaviour_GetCollaboratorForQueue(actor, queueName), queueName)));

}

void SCEZ_Behaviour_IncrementSyncForCollaborativeQueue(object actor, string queueName){
  int newSyncValue = GetLocalInt(actor, queueName+QUEUE_AFTER_SYNC_STRING) + 1 ;
 //debug("Incrementing my sync value on "+ queueName +" to "+ IntToString(newSyncValue) );
  SetLocalInt(actor, queueName+QUEUE_AFTER_SYNC_STRING, newSyncValue);

//  debug("=======================: My public sync: " + IntToString(SCEZ_Behaviour_GetAfterSyncForQueue(OBJECT_SELF, queueName)) + " private sync:  " + IntToString(SCEZ_Behaviour_GetBeforeSyncForQueue(OBJECT_SELF, queueName)));
//  debug("=======================: Their public sync: " + IntToString(SCEZ_Behaviour_GetAfterSyncForQueue(SCEZ_Behaviour_GetCollaboratorForQueue(actor, queueName), queueName)) + " private sync: " + IntToString(SCEZ_Behaviour_GetBeforeSyncForQueue(SCEZ_Behaviour_GetCollaboratorForQueue(actor, queueName), queueName)));
}


// behaviour include file
void SCEZ_Behaviour_SendSpin(object o) {
  //debug("Send SPIN...");
  DelayCommand(0.2f, AssignCommand(o, SignalEvent(o, EventUserDefined(EVENT_BEHAVIOUR_LOOP))));
}

int SCEZ_Behaviour_IsSpinEvent() {
  return GetUserDefinedEventNumber() == EVENT_BEHAVIOUR_LOOP;
}

// on a delay to prevent concurrency issues with latents. Recall that DelayCommand is run after the currently active script is finished.
void SCEZ_Behaviour_SendLatent(object o) {
//  debug("Send LATENT...");
  DelayCommand(0.2f, AssignCommand(o, SignalEvent(o, EventUserDefined(EVENT_BEHAVIOUR_LATENT) )));
}

int SCEZ_Behaviour_IsLatentEvent() {
  return GetUserDefinedEventNumber() == EVENT_BEHAVIOUR_LATENT;
}

void SCEZ_Behaviour_Spawn() {
  // spawn condition NW_FLAG_ON_DIALOGUE_EVENT causes the default conversation script to fire a user defined event with 1004 as the event number.
  // We catch that so they don't perform while in convo, and return to performing after convo
  SetSpawnInCondition(NW_FLAG_ON_DIALOGUE_EVENT);

  SetLocalFloat(OBJECT_SELF, "SE_AUX_ORIGINAL_FACING", GetFacing(OBJECT_SELF));
  SetLocalLocation(OBJECT_SELF, "SE_AUX_ORIGINAL_LOCATION", GetLocation(OBJECT_SELF));
  // SCEZ_Behaviour_ResetLatentPriority(OBJECT_SELF);
  SCEZ_Behaviour_Signal (OBJECT_SELF);
  SCEZ_Behaviour_SendSpin(OBJECT_SELF);//SignalEvent(OBJECT_SELF, EventUserDefined(EVENT_BEHAVIOUR_LOOP));
}

void SCEZ_Behaviour_IncrementAllTimers(object actor) {
  int to = GetLocalInt(actor, QUEUE_TIMER_STRING);
  int i;
  string behaviourStack = SCEZ_Behaviour_GetExecutionStack(actor);
  string suspendedBehaviourArray = BEHAVIOUR_SUSPENDED_PRIORITYQUEUE_STRING;
  string arrayElement = "";
  string stackElement = "";

  for(i = BEHAVIOUR_STACK_BOTTOM; i <= SCEZ_Struct_StackSize(actor, behaviourStack) ; i++){
    //behaviourStack+IntToString(i) will give the i-th element in the behaviour stack
    stackElement = GetLocalString(actor, behaviourStack+IntToString(i));

    SCEZ_Behaviour_IncrementTimer(actor, stackElement);
  }

  arrayElement = "";

  for(i = 0; i < SCEZ_Struct_ArrayGetSize(actor, suspendedBehaviourArray) ; i++){
    //suspendedBehaviourArray+IntToString(i) will give the i-th element in the pending behaviour array
    arrayElement = GetLocalString(actor, suspendedBehaviourArray+IntToString(i));

    SCEZ_Behaviour_IncrementTimer(actor, arrayElement);
  }
}

void SCEZ_Behaviour_ResetTimer(object actor, string queue) {
  //debug("Resetting timer on "+ queue);
  SetLocalInt(actor, queue+QUEUE_TIMER_STRING, 1);
}

void SCEZ_Behaviour_IncrementTimer(object actor, string queue) {
  // Sanity check
  if( queue == \"\" || actor == OBJECT_INVALID ) {
    return;
  }
//  debug("Incrementing queue timer: "+ queue+QUEUE_TIMER_STRING +"::"+ IntToString(GetLocalInt(actor, queue+QUEUE_TIMER_STRING)));

  // If queue has expired, just drop it.
  int incrementedValue = GetLocalInt(actor, queue+QUEUE_TIMER_STRING)+1;
  if( incrementedValue > SCEZ_LONG_TIMEOUT_CYCLES ) {
    SCEZ_Behaviour_PriorityQueueRemoveElement(actor, BEHAVIOUR_SUSPENDED_PRIORITYQUEUE_STRING, queue);
//    SCEZ_Struct_ArrayRemoveElement (actor, BEHAVIOUR_SUSPENDED_PRIORITYQUEUE_STRING, queue);
    SCEZ_Behaviour_DestroyQueue( actor, queue );

    return;
  }

  SetLocalInt(actor, queue+QUEUE_TIMER_STRING, incrementedValue);
}

// Determines and returns the current timeout value, 0,1 or 2, of the timer variable passed in.
// The value corresponds to the number of times we've moved up the timeout ladder (none, med, long).
int SCEZ_Behaviour_GetTimeout( object actor, string queue ) {
  int timeout = GetLocalInt( actor, queue+QUEUE_TIMER_STRING);
  if (timeout < SCEZ_MED_TIMEOUT_CYCLES) {
    return SCEZ_NOTIMEOUT;
  }else if( timeout > SCEZ_LONG_TIMEOUT_CYCLES ){
    return SCEZ_LONGTIMEOUT;
  }else {
    return SCEZ_MEDTIMEOUT;
  }
}

int SCEZ_Behaviour_GetMotivation(object actor, string motivation) {
  return GetLocalInt(actor, "sczm_"+motivation);
}

void SCEZ_Behaviour_SetMotivation(object actor, string motivation, int val) {
  if (val < 0) {
    val = 0;
  }
  if (val > 100) {
    val = 100;
  }
  SetLocalInt(actor, "sczm_"+motivation, val);
}

void SCEZ_Behaviour_IncreaseMotivation(object actor, string motivation, int delta) {
  int val;
  val = SCEZ_Behaviour_GetMotivation(actor, motivation);
  val = val + delta;
  SCEZ_Behaviour_SetMotivation(actor, motivation, val);
}

void SCEZ_Behaviour_DecreaseMotivation(object actor, string motivation, int delta) {
  int val;
  val = SCEZ_Behaviour_GetMotivation(actor, motivation);
  val = val - delta;
  SCEZ_Behaviour_SetMotivation(actor, motivation, val);
}

string SCEZ_Behaviour_GetPhase(object actor) {
  //debug("Getting phase from :" + GetName(actor));

  return GetLocalString(actor, "sczm_phase");
}

void SCEZ_Behaviour_SetPhase(object actor, string phase) {
  SetLocalString(actor, "sczm_phase", phase);
}

void SCEZ_Behaviour_ResetPhase(object actor) {
  SetLocalString(actor, "sczm_phase", "");
}

string SCEZ_Behaviour_GetTransientPhase(object actor) {
  return GetLocalString(actor, "sczm_tphase");
}

void SCEZ_Behaviour_SetTransientPhase(object actor, string phase) {
  SetLocalString(actor, "sczm_tphase", phase);
}

void SCEZ_Behaviour_ResetTransientPhase(object actor) {
  SetLocalString(actor, "sczm_tphase", "");
}

void SCEZ_Behaviour_Signal(object actor) {
 //debug("Signalled " + GetName(actor) +  ".");
  SetLocalInt(actor, "sczm__signal", TRUE);
}

void SCEZ_Behaviour_CollaboratorArrayClear() {
  int n = GetLocalInt(OBJECT_SELF, "scez_can");
  int i = 0;
  while(i < n) {
    DeleteLocalObject(OBJECT_SELF, "scez_ca"+IntToString(i));
    i = i + 1;
  }
  DeleteLocalInt(OBJECT_SELF, "scez_can");
}

void SCEZ_Behaviour_CollaboratorArrayAdd(object c, int number) {
  int n = GetLocalInt(OBJECT_SELF, "scez_can");
  SetLocalObject(OBJECT_SELF, "scez_ca"+IntToString(n), c);
  SetLocalInt(OBJECT_SELF, "scez_can"+IntToString(n), number);
  SetLocalInt(OBJECT_SELF, "scez_can", n+1);
}

object SCEZ_Behaviour_CollaboratorArrayRandom() {
  int n = GetLocalInt(OBJECT_SELF, "scez_can");
  if (n == 0) {
    return OBJECT_INVALID;
  } else {
    int i = Random(n);
    int number = GetLocalInt(OBJECT_SELF, "scez_can"+IntToString(i));
    SetLocalInt(OBJECT_SELF, "scez_can", number);
    return GetLocalObject(OBJECT_SELF, "scez_ca"+IntToString(i));
  }
}

void SCEZ_Behaviour_QueuePriorityBehaviours(){
  string behaviourStack = SCEZ_Behaviour_GetExecutionStack(OBJECT_SELF);
  string topQueue = SCEZ_Behaviour_PriorityQueue_PeekFirstReady(OBJECT_SELF, BEHAVIOUR_PRIORITY_QUEUE_STRING);

  if( topQueue == \"\" ) {
    SCEZ_Behaviour_PriorityQueue_Destroy(OBJECT_SELF, BEHAVIOUR_PRIORITY_QUEUE_STRING);
    return;
  }
  else {
    object collaborator = SCEZ_Behaviour_GetCollaboratorForQueue(OBJECT_SELF, topQueue);
    // If the new behaviour is collaborative, and we are the initiator of that collaboration, we must notify our collaborator
    if( collaborator != OBJECT_INVALID && SCEZ_Behaviour_QueueBelongsTo(OBJECT_SELF, topQueue) ) {
      SCEZ_Behaviour_MailReactive(collaborator, topQueue);
    }
    //debug("  Queueing priority behaviours");

    SCEZ_Behaviour_InterruptWith(OBJECT_SELF, SCEZ_Behaviour_PriorityQueue_PopFirstReady(OBJECT_SELF, BEHAVIOUR_PRIORITY_QUEUE_STRING));
    SCEZ_Behaviour_PriorityQueue_Destroy(OBJECT_SELF, BEHAVIOUR_PRIORITY_QUEUE_STRING);
  }
}

void SCEZ_Behaviour_QueueResumableBehaviours() {
  int suspendedArraySize = SCEZ_Struct_ArrayGetSize(OBJECT_SELF, BEHAVIOUR_SUSPENDED_PRIORITYQUEUE_STRING);
  if( suspendedArraySize > 0 ) {
//    debug("Resumables found...");
    int i;
    for(i=0; i<suspendedArraySize; i++) {
      string suspendedQueue = SCEZ_Struct_ArrayGetElementAtIndex (OBJECT_SELF, BEHAVIOUR_SUSPENDED_PRIORITYQUEUE_STRING, i);
      if( SCEZ_Behaviour_QueueHasInterruptPriority(OBJECT_SELF, suspendedQueue) && SCEZ_Behaviour_IsMyTurn(OBJECT_SELF, suspendedQueue) ){
        //debug("Resuming "+ suspendedQueue);
        SCEZ_Behaviour_PriorityQueueRemoveElement (OBJECT_SELF, BEHAVIOUR_SUSPENDED_PRIORITYQUEUE_STRING, suspendedQueue);
//        SCEZ_Struct_ArrayRemoveElement (OBJECT_SELF, BEHAVIOUR_SUSPENDED_PRIORITYQUEUE_STRING, suspendedQueue);
        SCEZ_Behaviour_ResetTimer(OBJECT_SELF, suspendedQueue);
        SCEZ_Behaviour_InterruptWith(OBJECT_SELF, suspendedQueue);
        return;
      }
    }
  }
}

string SCEZ_Behaviour_GetStack (object actor, string stackName) {
  return GetLocalString( actor, stackName+"stack" );
}

string SCEZ_Behaviour_GetExecutionStack(object actor){
//  string stackName = SCEZ_Behaviour_GetStack(actor, BEHAVIOUR_STACK_STRING);
  // If the execution stack has not yet been created, do it now.
//  if( stackName == \"\" ) {
//   SetLocalString(actor, BEHAVIOUR_STACK_STRING+"stack",
//  }
  return BEHAVIOUR_STACK_STRING+"stack";
}

int SCEZ_Behaviour_GetQueuePriority( object actor, string queueName ){
  return GetLocalInt( actor, queueName+QUEUE_PRIORITY_STRING );
}

void SCEZ_Behaviour_SetQueuePriority( object actor, string queueName, int priority ){
  SetLocalInt( actor, queueName+QUEUE_PRIORITY_STRING, priority );
}

void SCEZ_Behaviour_ResetQueuePriority(object actor, string queue) {
  SCEZ_Behaviour_SetQueuePriority(actor, queue, -1);
}

/*
  Returns TRUE iff the queue 'queueName' has strictly HIGHER priority than the currently executing behaviour.
*/
int SCEZ_Behaviour_QueueHasPriority(object actor, string queueName) {
  int newPriority = SCEZ_Behaviour_GetQueuePriority(OBJECT_SELF, queueName);
  string topQueue = SCEZ_Struct_StackPeek(actor, SCEZ_Behaviour_GetExecutionStack(actor) );

  return SCEZ_Behaviour_GetQueuePriority(actor, topQueue) < newPriority;

}

/*
  Returns TRUE iff the queue 'queueName' has EQUAL or HIGHER priority than the currently executing behaviour.
*/
int SCEZ_Behaviour_QueueHasInterruptPriority(object actor, string queueName) {
  int newPriority = SCEZ_Behaviour_GetQueuePriority(OBJECT_SELF, queueName);
  string topQueue = SCEZ_Struct_StackPeek(actor, SCEZ_Behaviour_GetExecutionStack(actor) );

  return SCEZ_Behaviour_GetQueuePriority(actor, topQueue) <= newPriority;

}

/*
  Places the queue 'queueName' directly onto the execution stack of 'actor' and then immediately fires the behaviour.
*/
void SCEZ_Behaviour_InterruptWith (object actor, string newQueueName) {
  string behaviourStack = SCEZ_Behaviour_GetExecutionStack(actor);
  string oldQueueName = SCEZ_Struct_StackPeek(actor, behaviourStack);

  //debug("Interrupting " + oldQueueName + " with "+ newQueueName);

  SCEZ_Struct_StackPush(actor, behaviourStack, newQueueName);
  ClearAllActions();

  // Increment the collaborativeSync iff we've just cleared the increment off of the action queue.
  if( SCEZ_Behaviour_GetBeforeSyncForQueue(actor, oldQueueName) != SCEZ_Behaviour_GetAfterSyncForQueue(actor, oldQueueName) ) {
    // The collaborative sync has not been incremented, do it now.
    SCEZ_Behaviour_IncrementSyncForCollaborativeQueue(actor, oldQueueName);
    //debug("I am incrementing my public sync cause I've been interrupted");
  }
  // In case this interruption has just cleared the garbage collection of a behaviour queue, garbage.dispose
  int i;
  for( i=0; i<SCEZ_Struct_ArrayGetSize(OBJECT_SELF, BEHAVIOUR_GARBAGE_BIN_STRING); i++ ) {
    SCEZ_Behaviour_DestroyQueue(OBJECT_SELF, SCEZ_Struct_ArrayRemoveElementAtIndex(OBJECT_SELF, BEHAVIOUR_GARBAGE_BIN_STRING, i));
  }
  SCEZ_Struct_ArrayClear(actor, BEHAVIOUR_GARBAGE_BIN_STRING);

  //debug("SIGNAL from InterruptWith");
  SCEZ_Behaviour_Signal(actor);
}

// returning -1 means that the collaborator doesn't exist or some other major busti[fi]*cation,
// and therefore the queue should be killed behind the barn. *- JD
int SCEZ_Behaviour_IsCollaborativeReady(object actor, string queue){
  object collab = SCEZ_Behaviour_GetCollaboratorForQueue(actor, queue);
  string topCollabQueue;

  if( collab == OBJECT_INVALID ){
      return -1;
  }

  topCollabQueue = SCEZ_Struct_StackPeek(collab, SCEZ_Behaviour_GetExecutionStack(collab));

  if( (SCEZ_Behaviour_GetCollaboratorForQueue( collab, topCollabQueue) != actor) ||
      (topCollabQueue != queue) ){
    return FALSE;
  }

  return TRUE;
}

string SCEZ_Behaviour_ParseBehaviourNameFrom(string queueName){
  string behaviourName;

  behaviourName = GetStringLeft(queueName, FindSubString(queueName, ","));

  //debug("'s behaviour name is: " + behaviourName);

  return behaviourName;
}

string SCEZ_Behaviour_ParseNonUniqueBehaviourNameFrom(string queueName){
  string behaviourName = SCEZ_Behaviour_ParseBehaviourNameFrom(queueName);
  if( FindSubString(behaviourName, ":") > -1 ) {
    behaviourName = GetStringLeft(queueName, FindSubString(queueName, ":"));
  }

  return behaviourName;
}

void SCEZ_UpdatePhase() {
  string newPhase = "";//"PerformanceStartCue";

  if( SCEZ_Struct_ArrayGetSize(OBJECT_SELF, BEHAVIOUR_PENDING_PHASE_ARRAY_STRING) > 0 ) {
    newPhase = SCEZ_Struct_ArrayGetElementAtIndex(OBJECT_SELF, BEHAVIOUR_PENDING_PHASE_ARRAY_STRING, SCEZ_Struct_ArrayGetSize(OBJECT_SELF, BEHAVIOUR_PENDING_PHASE_ARRAY_STRING)-1);
    SCEZ_Struct_ArrayClear(OBJECT_SELF, BEHAVIOUR_PENDING_PHASE_ARRAY_STRING);
  }


  if(newPhase != ""){
      //debug("Setting phase to :'" + newPhase + "'");
    SCEZ_Behaviour_SetPhase(OBJECT_SELF, newPhase);
    // This method is used to signal the start cue of a role in a performance
    SCEZ_Behaviour_SignalStartCue(OBJECT_SELF);
  }
}

void SCEZ_Behaviour_EndTaskOnQueue(string queue){
  SCEZ_Behaviour_IncrementSyncForCollaborativeQueue(OBJECT_SELF, queue);
  SCEZ_Struct_QueuePop(OBJECT_SELF, queue);
 // debug("SIGNAL from EndTaskOnQueue");
  SCEZ_Behaviour_Signal(OBJECT_SELF);
}

// destroys the given queue in the array given and deletes all of its related variables.
void SCEZ_Behaviour_DestroyQueueAt(object actor, string arrayName, int index){
  string destroyed = SCEZ_Struct_ArrayRemoveElementAtIndex(actor, arrayName, index);

  SCEZ_Behaviour_DestroyQueue(actor, destroyed);
}

// destroys the given queue by deleting all array associated variables.
// Any queue associated variable added to the system should be destroyed here.
void SCEZ_Behaviour_DestroyQueue(object actor, string queueName){
//  debug("Destroying '" + queueName + "' on " + GetName(actor) );

  SCEZ_Struct_QueueClear(actor, queueName);

  //debug("Deleting local integer: " + queueName+QUEUE_TIMER_STRING);
  DeleteLocalInt(actor, queueName+QUEUE_TIMER_STRING);
  //debug("double checked value: " + IntToString( GetLocalInt(actor, queueName+QUEUE_TIMER_STRING) ) );

  DeleteLocalInt(actor, queueName+QUEUE_PRIORITY_STRING);
  DeleteLocalObject(actor, queueName+QUEUE_COLLABORATOR_STRING);

  //debug("Deleting local integer: " + queueName+QUEUE_BEFORE_SYNC_STRING);
  DeleteLocalInt(actor, queueName+QUEUE_BEFORE_SYNC_STRING);
  //debug("double checked value: " + IntToString( GetLocalInt(actor, queueName+QUEUE_BEFORE_SYNC_STRING)) + "===================================================="  );
  DeleteLocalInt(actor, queueName+QUEUE_AFTER_SYNC_STRING);
  SCEZ_Struct_ArrayRemoveElement(actor, BEHAVIOUR_GARBAGE_BIN_STRING, queueName);

}

//\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\
////////////// PRIORITY QUEUE ///////////////
//\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\
void SCEZ_Behaviour_PriorityQueueAdd(object actor, string priorityQueueName, string newQueue, int priority=-1){
  if ( priority == -1 ) {
    //debug("No Priority rating given, retrieving... ");
    priority = SCEZ_Behaviour_GetQueuePriority(actor, newQueue);
    //debug("Retrieved priority rating: "+ IntToString(priority) +" for queue: "+ newQueue);
  }

  int testi = 0;
  int size = SCEZ_Struct_ArrayGetSize(actor, priorityQueueName);
  while(testi < size ){
    //debug("CONTENTS: Element "+ IntToString(testi) +" in the queue "+ priorityQueueName +" is :'" + SCEZ_Struct_ArrayGetElementAtIndex(actor, priorityQueueName, testi) + "'");
    testi++;
  }

  int index = SCEZ_Behaviour_PriorityQueue_BinarySearch(actor, priorityQueueName, priority, 0, SCEZ_Struct_ArrayGetSize(actor, priorityQueueName) );
  //debug(priorityQueueName + "'s size is: " + IntToString(size) );
  //debug("Adding to "+ priorityQueueName +" the queue "+ newQueue +" with priority "+ IntToString(priority) +" at index "+ IntToString(index));

  SCEZ_Struct_ArrayInsertElement(actor, priorityQueueName, newQueue, index);
}

// DEPRECATED
string SCEZ_Behaviour_PriorityQueueRemove(object actor, string queueName, int index){
  return SCEZ_Behaviour_PriorityQueueRemoveElement( actor, queueName, GetLocalString(actor, queueName+IntToString(index)) );
}

// Pops the highest priority (rightmost) element, regardless of whether the collaborator is ready or not.
string SCEZ_Behaviour_PriorityQueue_Pop(object actor, string queueName){
  int size = SCEZ_Struct_ArrayGetSize(actor, queueName);
  int index = size-1;
  string popped = "";

  //debug("PriorityQueuePop - popping from " + queueName + ", which has a size " + IntToString(size) );

  while (index > 0){
    if(SCEZ_Struct_ArrayGetElementAtIndex(actor, queueName, index) != ""){
      break;
    }

      index--;
  }

  if( (index < 0) || (index >= size) ){
    //debug("Your index: "+ IntToString(index) +" is bigger than your size "+ IntToString(size));
    return "Stereoisomerism";
  }else{
    popped = SCEZ_Behaviour_PriorityQueueRemove(actor, queueName, index);

    //debug("PriorityQueuePopping " + popped + " from " + queueName + ", Batman!");

    return popped;
  }
}

string SCEZ_Behaviour_PriorityQueue_Peek(object actor, string queueName){
  int index = 0;
  int size = SCEZ_Struct_ArrayGetSize(actor, queueName);

  while (index < size){
    if(SCEZ_Struct_ArrayGetElementAtIndex(actor, queueName, index) != ""){
      break;
    }

      index++;
  }

  if( ! (index < size) ){
    return "";
  }else{
    return SCEZ_Struct_ArrayGetElementAtIndex(actor, queueName, index);
  }
}


void SCEZ_Behaviour_PriorityQueue_Clear(object actor, string queueName){
  int arraySize = SCEZ_Struct_ArrayGetSize( actor, queueName );
  while( arraySize > 0 ){
    //debug("PriorityQueueClear... Deleting from: "+ queueName +", "+ IntToString(arraySize) +" elements left");

    SCEZ_Behaviour_DestroyQueue(actor, SCEZ_Behaviour_PriorityQueue_Pop( actor, queueName ) );
    arraySize = SCEZ_Struct_ArrayGetSize( actor, queueName );
  }
}

void SCEZ_Behaviour_PriorityQueue_Destroy(object actor, string queueName){
  //debug("PriorityQueueDestroy - destroying '" + queueName + "'");
  SCEZ_Behaviour_PriorityQueue_Clear(actor, queueName);

  SCEZ_Struct_ArrayClear(actor, queueName);
}

// So far this only handles collaboratives.
string SCEZ_Behaviour_PriorityQueue_PopFirstReady(object actor, string queueName){
  int index = SCEZ_Behaviour_PriorityQueue_FindFirstReady(actor, queueName);
  string popped = "";

  if(index < 0)
    return "";
  else{
    popped = SCEZ_Behaviour_PriorityQueueRemove(actor, queueName, index);

    return popped;
  }
}

// So far this only handles collaboratives.
string SCEZ_Behaviour_PriorityQueue_PeekFirstReady(object actor, string queueName){
  int index = SCEZ_Behaviour_PriorityQueue_FindFirstReady(actor, queueName);

  //debug("PriorityQueue_PeekFirstReady... found index " + IntToString(index) );

  if(index < 0)
    return "";
  else
    return SCEZ_Struct_ArrayGetElementAtIndex(actor, queueName, index);
}

// returns the index of the first ready element in the priority queue. -1 on fail
// Searches from the highest priority to lowest, IE rightmost to leftmost.
int SCEZ_Behaviour_PriorityQueue_FindFirstReady(object actor, string queueName){
  //debug( "Finding first ready for "+ queueName +" on "+ GetName(actor) );
  int size = SCEZ_Struct_ArrayGetSize(actor, queueName);
  string topQueue = SCEZ_Struct_StackPeek(actor, SCEZ_Behaviour_GetExecutionStack(actor) );
  int currentPriority = -1;
  int i;

  string queue;

  //debug("SIZE of " + queueName + " = " + IntToString(size));

  for(i = size-1; i >= 0; i--){
    queue = SCEZ_Struct_ArrayGetElementAtIndex(actor, queueName, i);
    //debug(queueName +"["+ IntToString(i) +"] => "+ queue);

    // If the system gets spammed with latent event calls, 'queue' is sometimes empty. We handle this gracefully like this...
    if(queue == \"\") {
      continue;
    }
    int potentialPriority = SCEZ_Behaviour_GetQueuePriority(actor, queue);
    if( topQueue != "" ) {
      currentPriority = SCEZ_Behaviour_GetQueuePriority(actor, topQueue);
    }
    //debug("Comparing potential Priority of: "+ queue +"::"+ IntToString(potentialPriority) +" with current priority from: "+ topQueue +"::"+ IntToString(currentPriority));
    if( potentialPriority <= currentPriority ){
      return -1; // The priority queue is ordered, so if we've failed here, we'll fail on all further entries, so just give up.
    }
    // This queue has the necessary priority, but it's no good to us if it's waiting on a collaborator to act.
    else if( SCEZ_Behaviour_IsMyTurn(actor, queueName) ){
      return i;
    }
  }

  return -1;
}

/*
  Returns the index of the place where the given priority would belong.
*/
int SCEZ_Behaviour_PriorityQueue_BinarySearch(object actor, string storageArray, int priority, int low, int high){
  int mid;
  int midPriority;
  //debug("Binary Search:: low="+IntToString(low) + " high="+ IntToString(high) +" priority="+ IntToString(priority));

  if(high < low){
    // If we get so far as to not be able to find a place for the new priority,
    //  then it must be less than every other element, and therefore should be placed at the low end, IE leftmost.
    //debug("Binary Search failed!! high::"+ IntToString(high) +" < low::"+ IntToString(low));
    //debug("Smaller than every other value in the array. Must go leftmost.");
    return 0;
  }

  mid = low + ((high - low)/2);

  midPriority = SCEZ_Behaviour_GetQueuePriority(actor, SCEZ_Struct_ArrayGetElementAtIndex(actor, storageArray, mid));

  //debug(storageArray + "[" + IntToString(mid) + "] is " + SCEZ_Struct_ArrayGetElementAtIndex(actor, storageArray, mid));

  //debug("BinSearch midPriority: " + IntToString(midPriority));
  //debug("BinSearch priority: " + IntToString(priority));


  if( midPriority > priority){
    // lower priorities are to the right.
    return SCEZ_Behaviour_PriorityQueue_BinarySearch(actor, storageArray, priority, low, mid-1);
  }
  else if (midPriority < priority){
    if( high-low < 2 ){
      // No sense doing another search, we're done.
      return low;
    }
    return SCEZ_Behaviour_PriorityQueue_BinarySearch(actor, storageArray, priority, mid+1, high);
  }
  else{
    return mid;
  }
}

string SCEZ_Behaviour_PriorityQueueRemoveElement( object owner, string priorityQueueName, string element ) {
  int i;
  int size = SCEZ_Struct_ArrayGetSize(owner, priorityQueueName);
  //debug("So... the size of "+ priorityQueueName +" is "+ IntToString(size) +" and we are looking for element "+ element);
  for( i=0; i<size; i++ ) {
    string currentElement = GetLocalString(owner, priorityQueueName+IntToString(i));
    //debug("In PriorityQueueRemove, Element "+IntToString(i)+" is: "+ currentElement);
    if( currentElement == element ) {
      //debug("Removing element number " + IntToString(i) + ": "+ currentElement +" from priority queue: "+ priorityQueueName);
      string removedElement = SCEZ_Struct_ArrayRemoveElementAtIndex( owner, priorityQueueName, i );

      // Clean up empty slot.
      int j = i;
      while(j < size){
        //debug("Moving " + IntToString(j+1) + "th element '" + SCEZ_Struct_ArrayGetElementAtIndex(owner, priorityQueueName, j+1) + "' left " );
        SCEZ_Struct_ArraySetElementAtIndex(owner, priorityQueueName, j, SCEZ_Struct_ArrayGetElementAtIndex(owner, priorityQueueName, j+1) );
        j++;
      }
      // Last element is empty, lose it and decrement the array size.
      SCEZ_Struct_ArrayRemoveElementAtIndex( owner, priorityQueueName, size-1 );
      return removedElement;
    }
  }
  // No Such Element
  return "";
}
