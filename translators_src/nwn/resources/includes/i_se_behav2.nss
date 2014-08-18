#include "nw_i0_generic"
#include "i_se_structs"
#include "i_se_aux"

/*
 * Constants
 */

const string INDEPENDENT_PROACTIVE_PREFIX = "SCEZ_Behav_IndependentProactiveQueue_";
const string LATENT_PREFIX = "SCEZ_Behav_LatentQueue_";
const string COLLABORATIVE_PREFIX = "SCEZ_Behav_CollaborativeQueue_";
const string LATENT_LIST_PREFIX = "SCEZ_Behav_LatentQueueList_";
const string COLLABORATIVE_LIST_PREFIX = "SCEZ_Behav_CollaborativeQueueList_";
const string PRIORITY_PREFIX = "SCEZ_Behav_Priority_";
const string TASK_PREFIX = "SCEZ_TASK_";

/*
 * Function Declarations
 */

int SCEZ_Behav_IsIdle(object actor);

// Name Getters
string SCEZ_Behav_GetTaskName(string name);
string SCEZ_Behav_GetIndependentProactiveQueueName(object actor);
string SCEZ_Behav_GetLatentBehaviourName(string name);
string SCEZ_Behav_GetLatentBehaviourListName(object actor);
string SCEZ_Behav_GetCollaborativeQueueListName(object actor);

// Proactive Queue
string SCEZ_Behav_PeekIndependentProactiveQueue(object actor);
void SCEZ_Behav_AddToIndependentProactiveQueue(object actor, string name);
string SCEZ_Behav_PopIndependentProactiveQueue(object actor);
int SCEZ_Behav_IndependentProactiveQueueIsEmpty(object actor);

// Latent Queues
int SCEZ_Behav_GetLatentBehaviourListSize(object actor);

void SCEZ_Behav_AddLatentBehaviour(object actor, string name, float priority);

void SCEZ_Behav_AddToLatentBehaviourQueue(object actor, string name, string proactiveName);
string SCEZ_Behav_PeekLatentBehaviourQueue(object actor, string name);
string SCEZ_Behav_PopLatentBehaviourQueue(object actor, string name);

int  SCEZ_Behav_IsCurrentLatentBehaviour(object actor, string name);
int SCEZ_Behav_LatentBehaviourQueueIsEmpty(object actor, string name);


// Collaborative Queues
int SCEZ_Behav_GetCollaborativeQueueListSize(object actor);

/*
 * Function Definitions
 */

// An actor is idle if it has no latent or collaborative queues.
int SCEZ_Behav_IsIdle(object actor) {
  return SCEZ_Behav_GetLatentBehaviourListSize(actor) == 0
            && SCEZ_Behav_GetCollaborativeQueueListSize(actor) == 0
            && SCEZ_Behav_IndependentProactiveQueueIsEmpty(actor) == 1;
}


/**
 * Name Getters
 */
string SCEZ_Behav_GetIndependentProactiveQueueName(object actor) {
    return INDEPENDENT_PROACTIVE_PREFIX + GetTag(actor);
}

string SCEZ_Behav_GetLatentBehaviourName(string name) {
    return LATENT_PREFIX + name;
}

string SCEZ_Behav_GetLatentBehaviourListName(object actor) {
    return LATENT_LIST_PREFIX + GetTag(actor);
}
string SCEZ_Behav_GetCollaborativeQueueListName(object actor) {
   return COLLABORATIVE_LIST_PREFIX + GetTag(actor);
}

string SCEZ_Behav_GetTaskName(string name) {
   return TASK_PREFIX + name;
}

string SCEZ_Behav_GetPriorityName(string name) {
  return PRIORITY_PREFIX + name;
}



/**
 * Proactive Queue
 */
string SCEZ_Behav_PeekIndependentProactiveQueue(object actor) {
    return SCEZ_Struct_QueuePeek(actor, SCEZ_Behav_GetIndependentProactiveQueueName(actor));
}

int SCEZ_Behav_IndependentProactiveQueueIsEmpty(object actor) {
   return SCEZ_Struct_QueueIsEmpty(actor, SCEZ_Behav_GetIndependentProactiveQueueName(actor));
}

int SCEZ_Behav_IsCurrentIndependentProactive(object actor, string proactiveName) {
    return SCEZ_Behav_PeekIndependentProactiveQueue(actor) == SCEZ_Behav_GetTaskName(proactiveName);
}

void SCEZ_Behav_AddToIndependentProactiveQueue(object actor, string proactiveName){
   SCEZ_Struct_QueueAdd(actor, SCEZ_Behav_GetIndependentProactiveQueueName(actor), SCEZ_Behav_GetTaskName(proactiveName));
}

string SCEZ_Behav_PopIndependentProactiveQueue(object actor){
   return SCEZ_Struct_QueuePop(actor, SCEZ_Behav_GetIndependentProactiveQueueName(actor));
}

/**
 * Latent Queues
 */
int SCEZ_Behav_GetLatentBehaviourListSize(object actor){
   return SCEZ_Struct_ArrayGetSize(actor, SCEZ_Behav_GetLatentBehaviourListName(actor));
}

int  SCEZ_Behav_IsCurrentLatentBehaviour(object actor, string name) {
   string SE_BEHAVIOUR_LIST_NAME = SCEZ_Behav_GetLatentBehaviourListName(actor);
   string SE_ThisName = SCEZ_Behav_GetLatentBehaviourName(name);
   return SCEZ_Struct_ArrayFindElement(actor, SE_BEHAVIOUR_LIST_NAME, SE_ThisName) > -1;
}

// This adds the behaviour to a list of latent behaviours ordered by priority
void SCEZ_Behav_AddLatentBehaviour(object actor, string name, float priority) {
   string SE_BehavName = SCEZ_Behav_GetLatentBehaviourName(name);
   string SE_BehavListName =  SCEZ_Behav_GetLatentBehaviourListName(actor);

   int index = 0;
   int SE_SIZE = SCEZ_Struct_ArrayGetSize(actor, SE_BehavListName);

   // Save the priority so we can access it when we add another behaviour.
   SetLocalFloat(actor, SCEZ_Behav_GetPriorityName(SE_BehavName), priority);

   if(SE_SIZE == 0) {
      SCEZ_Struct_ArrayAppendElement(actor, SE_BehavListName, SE_BehavName);
   } else {
       for(index; index < SE_SIZE ; index++) {
          // This is the latent name...
          string stringAtIndex = SCEZ_Struct_ArrayGetElementAtIndex(actor, SE_BehavListName, index) ;
          float priorityAtIndex = GetLocalFloat(actor, SCEZ_Behav_GetPriorityName(stringAtIndex));

          if(priority <= priorityAtIndex) {
              // This should insert the behaviour before the current element.
              SCEZ_Struct_ArrayInsertElement(actor, SE_BehavListName, SE_BehavName, index);
              break;
          }

          if(index == SE_SIZE - 1) {
              // We add the behaviour to the end if it has the highest priority
              SCEZ_Struct_ArrayAppendElement(actor, SE_BehavListName, SE_BehavName);
          }
       }
   }
}

string SCEZ_Behav_PeekLatentBehaviourQueue(object actor, string name) {
    return SCEZ_Struct_QueuePeek(actor, SCEZ_Behav_GetLatentBehaviourName(name));
}

string SCEZ_Behav_PopLatentBehaviourQueue(object actor, string name) {
    string SE_BehavName = SCEZ_Behav_GetLatentBehaviourName(name);
    string SE_BehavTaskName = SCEZ_Struct_QueuePop(actor, SE_BehavName);

    if(SCEZ_Behav_LatentBehaviourQueueIsEmpty(actor, SE_BehavName)) {
        SCEZ_Struct_ArrayRemoveElement(actor, SCEZ_Behav_GetLatentBehaviourListName(actor), SE_BehavName);
    }

    return SE_BehavTaskName;
}

int SCEZ_Behav_LatentBehaviourQueueIsEmpty(object actor, string name){
    return SCEZ_Struct_QueueIsEmpty(actor, SCEZ_Behav_GetLatentBehaviourName(name));
}

void SCEZ_Behav_AddToLatentBehaviourQueue(object actor, string name, string proactiveName) {
    SCEZ_Struct_QueueAdd(actor, SCEZ_Behav_GetLatentBehaviourName(name), SCEZ_Behav_GetTaskName(proactiveName));
}

int SCEZ_Behav_IsCurrentLatentBehaviourTask(object actor, string name, string proactiveName) {
    return SCEZ_Behav_PeekLatentBehaviourQueue(actor, name) == SCEZ_Behav_GetTaskName(proactiveName);
}

/**
 * Collaborative queues. TODO Eventually
 */
int SCEZ_Behav_GetCollaborativeQueueListSize(object actor) {
   return SCEZ_Struct_ArrayGetSize(actor, SCEZ_Behav_GetCollaborativeQueueListName(actor));
}