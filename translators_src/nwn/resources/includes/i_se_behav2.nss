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

const string TASK_PREFIX = "SCEZ_TASK_";

/*
 * Function Declarations
 */

int SCEZ_Behav_IsIdle(object actor);

string SCEZ_Behav_GetIndependentProactiveQueueName(object actor);
string SCEZ_Behav_GetLatentQueueListName(object actor);
string SCEZ_Behav_GetCollaborativeQueueListName(object actor);

// Proactive Queue
string SCEZ_Behav_PeekIndependentProactiveQueue(object actor);
void SCEZ_Behav_AddToIndependentProactiveQueue(object actor, string name);
string SCEZ_Behav_PopIndependentProactiveQueue(object actor);
int SCEZ_Behav_IndependentProactiveQueueIsEmpty(object actor);

// Sizes

int SCEZ_Behav_GetLatentQueueListSize(object actor);
int SCEZ_Behav_GetCollaborativeQueueListSize(object actor);

/*
 * Function Definitions
 */

// An actor is idle if it has no latent or collaborative queues.
int SCEZ_Behav_IsIdle(object actor) {
  return SCEZ_Behav_GetLatentQueueListSize(actor) == 0
            && SCEZ_Behav_GetCollaborativeQueueListSize(actor) == 0
            && SCEZ_Behav_IndependentProactiveQueueIsEmpty(actor) == 0;
}

string SCEZ_Behav_GetIndependentProactiveQueueName(object actor) {
    return INDEPENDENT_PROACTIVE_PREFIX + GetTag(actor);
}

string SCEZ_Behav_PeekIndependentProactiveQueue(object actor) {
    return SCEZ_Struct_QueuePeek(actor, SCEZ_Behav_GetIndependentProactiveQueueName(actor));
}

string SCEZ_Behav_GetLatentQueueListName(object actor) {
    return LATENT_LIST_PREFIX + GetTag(actor);
}
string SCEZ_Behav_GetCollaborativeQueueListName(object actor) {
   return COLLABORATIVE_LIST_PREFIX + GetTag(actor);
}

int SCEZ_Behav_IndependentProactiveQueueIsEmpty(object actor) {
   return SCEZ_Struct_QueueIsEmpty(actor, SCEZ_Behav_GetIndependentProactiveQueueName(actor));
}

int SCEZ_Behav_GetLatentQueueListSize(object actor){
   return SCEZ_Struct_ArrayGetSize(actor, SCEZ_Behav_GetLatentQueueListName(actor));
}

int SCEZ_Behav_GetCollaborativeQueueListSize(object actor) {
   return SCEZ_Struct_ArrayGetSize(actor, SCEZ_Behav_GetCollaborativeQueueListName(actor));
}

int SCEZ_Behav_IsCurrentIndependentProactive(object actor, string proactiveName) {
    return SCEZ_Behav_PeekIndependentProactiveQueue(actor) == TASK_PREFIX + proactiveName;
}

void SCEZ_Behav_AddToIndependentProactiveQueue(object actor, string proactiveName){
   SCEZ_Struct_QueueAdd(actor, SCEZ_Behav_GetIndependentProactiveQueueName(actor), TASK_PREFIX + proactiveName);
}

string SCEZ_Behav_PopIndependentProactiveQueue(object actor){
   return SCEZ_Struct_QueuePop(actor, SCEZ_Behav_GetIndependentProactiveQueueName(actor));
}