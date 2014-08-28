#include "nw_i0_generic"
#include "i_se_structs"
#include "i_se_aux"

/*
 * Constants
 */
const string BEHAVIOUR_LIST_PREFIX = "SCEZ_BehaviourList_";
const string PRIORITY_PREFIX = "SCEZ_Behav_Priority_";
const string TASK_PREFIX = "SCEZ_TASK_";
const int SIGNAL_IDLE_LOOP = 1954;

/*
 * Function Declarations
 */

int SCEZ_Behav_IsIdle(object actor);

// Name Getters
string SCEZ_Behav_GetBehaviourListName(object actor);
string SCEZ_Behav_GetPriorityName(string name);

// Behaviour List
int SCEZ_Behav_GetBehaviourListSize(object actor);
int  SCEZ_Behav_IsCurrentBehaviour(object actor, string name);

void SCEZ_Behav_AddBehaviour(object actor, string name, float priority);

// Behaviour Queues
void SCEZ_Behav_AddToBehaviourQueue(object actor, string behaviourName, string taskName);

string SCEZ_Behav_PeekBehaviourQueue(object actor, string name);
string SCEZ_Behav_PopBehaviourQueue(object actor, string name);

int SCEZ_Behav_BehaviourQueueIsEmpty(object actor, string name);
int SCEZ_Behav_IsCurrentBehaviourTask(object actor, string behaviourName, string taskName);

/*
 * Function Definitions
 */

// An actor is idle if it has no latent or collaborative queues.
int SCEZ_Behav_IsIdle(object actor) {
  return SCEZ_Behav_GetBehaviourListSize(actor) == 0;
}


/**
 * Name Getters
 */
string SCEZ_Behav_GetBehaviourListName(object actor) {
    return BEHAVIOUR_LIST_PREFIX + GetTag(actor);
}

string SCEZ_Behav_GetPriorityName(string name) {
  return PRIORITY_PREFIX + name;
}

/**
 * Behaviour List
 */
int SCEZ_Behav_GetBehaviourListSize(object actor){
   return SCEZ_Struct_ArrayGetSize(actor, SCEZ_Behav_GetBehaviourListName(actor));
}

int  SCEZ_Behav_IsCurrentBehaviour(object actor, string name) {
   return SCEZ_Struct_ArrayFindElement(actor, SCEZ_Behav_GetBehaviourListName(actor), name) == 0;
}

// This adds the behaviour to a list of latent behaviours ordered by priority
void SCEZ_Behav_AddBehaviour(object actor, string name, float priority) {
   string SE_BehavListName =  SCEZ_Behav_GetBehaviourListName(actor);

   int index = 0;
   int SE_SIZE = SCEZ_Struct_ArrayGetSize(actor, SE_BehavListName);

   // Save the priority so we can access it when we add another behaviour.

   SetLocalFloat(actor, SCEZ_Behav_GetPriorityName(name), priority);

   if(SE_SIZE == 0) {

      SCEZ_Struct_ArrayAppendElement(actor, SE_BehavListName, name);
   } else {
       for(index; index < SE_SIZE ; index++) {
          // This is the latent name...
          string stringAtIndex = SCEZ_Struct_ArrayGetElementAtIndex(actor, SE_BehavListName, index) ;
          float priorityAtIndex = GetLocalFloat(actor, SCEZ_Behav_GetPriorityName(stringAtIndex));

          if(priority <= priorityAtIndex) {
              // This should insert the behaviour before the current element.


              // TODO We may not want to insert behaviours with low priority!!


              SCEZ_Struct_ArrayInsertElement(actor, SE_BehavListName, name, index);
              break;
          }

          if(index == SE_SIZE - 1) {
              // We insert the behaviour to the start if it has the highest priority
              // which we can tell if we reach the end of the list.
              SCEZ_Struct_ArrayInsertElement(actor, SE_BehavListName, name, 0);
          }
       }
   }
}

/**
 * Behaviour queues
 */

string SCEZ_Behav_PeekBehaviourQueue(object actor, string name) {
    return SCEZ_Struct_QueuePeek(actor, name);
}

string SCEZ_Behav_PopBehaviourQueue(object actor, string name) {

    string SE_BehavTaskName = SCEZ_Struct_QueuePop(actor, name);

    if(SCEZ_Behav_BehaviourQueueIsEmpty(actor, name)) {
        SCEZ_Struct_ArrayRemoveElement(actor, SCEZ_Behav_GetBehaviourListName(actor), name);
    }

    return SE_BehavTaskName;
}

int SCEZ_Behav_BehaviourQueueIsEmpty(object actor, string name){
    return SCEZ_Struct_QueueIsEmpty(actor, name);
}

void SCEZ_Behav_AddToBehaviourQueue(object actor, string behaviourName, string taskName) {
    SCEZ_Struct_QueueAdd(actor, behaviourName, taskName);
}

int SCEZ_Behav_IsCurrentBehaviourTask(object actor, string behaviourName, string taskName) {
    return SCEZ_Behav_PeekBehaviourQueue(actor, behaviourName) == taskName;
}

string SCEZ_Behav_BehaviourListToString(object actor) {
    string SE_BehavListName =  SCEZ_Behav_GetBehaviourListName(actor);
    int index = 0;
    int SE_SIZE = SCEZ_Struct_ArrayGetSize(actor, SE_BehavListName);

    if(SE_SIZE == 0)
        return "Behaviour List is Empty";

    string SE_BehavString = "Behaviour [ ";
    for(index; index < SE_SIZE ; index++) {
          // This is the latent name...
          SE_BehavString += "[" + SCEZ_Struct_ArrayGetElementAtIndex(actor, SE_BehavListName, index)+ "]" ;
    }

    SE_BehavString += " ]";
    return SE_BehavString;
}