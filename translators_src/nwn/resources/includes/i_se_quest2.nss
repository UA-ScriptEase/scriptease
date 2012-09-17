#include "i_se_structs"

// ============================ Quest constants ============================
// ScriptEase2 Quest pattern include file
//
// Author: ScriptEase Team
// =========================================================================


// ============================ Function Declarations ============================

// Registers the Quest Point with the given name in the quest system.
//   oPlayer:   The owner of the quest point
//   name:      The name of the quest point.
//   fanIn:     The number of preceding quest points that must be completed prior.
void SE2_Quest_RegisterQuestPoint(object player, string name, int fanIn);

// registers the given parent for the given questpoint
void SE2_Quest_RegisterQuestPointParent(string name, string parent, object player);

// registers the given child for the given questpoint
void SE2_Quest_RegisterQuestPointChild(string name, string child, object player);

// Determines if the questpoint with the given name has had its
// fan-in count condition satisfied
//   oPlayer:   The owner of the quest point
//   name: The name of the quest point to test.
int SE2_Quest_FanInAchieved(object oPlayer, string name);

// Gets the given quest point's activation status as one of
// QUEST_POINT_STATE_*.
//   oPlayer:   The owner of the quest point
//   name:      The name of the quest point to get active status for
int SE2_Quest_GetState(object oPlayer, string name);

// Sets the given quest point's activation status as a boolean
//   oPlayer:   The owner of the quest point
//   name:      The name of the quest point to set active status for
//   state:     The new status as one of QUEST_POINT_STATE_*.
int SE2_Quest_SetState(object oPlayer, string name, int state);

// Sets the state of all quest point relatives for the quest point with
// the given name. NOTE: This does NOT set the state of the given root
// node.
//   name:   The name of the quest point to start from.
//   state:  The new state for the relatives. One of QUEST_POINT_STATE_*.
//   list:   The list to search through. One of QUEST_POINT_CHILDREN or
//           QUEST_POINT_PARENTS
void SE2_Quest_SetStateForRelatives(string name, int state, string list);

// Populates an array with the names of all of the quest points that lead to or follow
// from the quest point with the given name. Use QUEST_POINT_CHILDREN for all following,
// and QUEST_POINT_PARENTS for all preceding.
//   name:  The name of the quest point whose precursors are to be found
//   relativeList:  One of QUEST_POINT_PARENTS or QUEST_POINT_CHILDREN
//   tempList: the name of the temporary array to collect the relatives in.
void SE2_Quest_GetAllRelatives(string name, string relativeList, string tempList);

// Resets the quest point with the given name to enabled and
// all following quest points to disabled.
//   oPlayer:   The owner of the quest point
//   name: The name of the quest point to reset from.
void SE2_Quest_ResetAtQuestPoint(object oPlayer, string name);

// Resets the quest point with the given name to enabled and
// all following quest points to disabled.
//   oPlayer:   The owner of the quest point
//   name: The name of the quest point to reset from.
void SE2_Quest_ValidateQuestPoint(object oPlayer, string name);

// ============================ Quest constants ============================
// The event number for quests.
const int SE2_QUEST_ENABLED_EVENT = 2012;

// State for a disabled Quest Point
const int QUEST_POINT_STATE_NONE = 0;
// State for an enabled Quest Point
const int QUEST_POINT_STATE_ENABLED = 1;
// State for a succeeded Quest Point
const int QUEST_POINT_STATE_SUCCESS = 2;
// State for a failed Quest Point
const int QUEST_POINT_STATE_FAIL = 3;

// suffix for variable to track quest point's current state.
const string QUEST_POINT_STATE = "_QuestPointState";
// Track the fanIn for a QuestPoint
const string QUEST_POINT_FANIN = "_QuestPointFanIn";
// Track the children for a QuestPoint
const string QUEST_POINT_CHILDREN = "_QuestPointChildren";
// Track the parents for a QuestPoint
const string QUEST_POINT_PARENTS = "_QuestPointParents";

const string QUEST_POINTS_ENABLED_LIST = "QuestPointsRecentlyEnabled";


// ============================ Function Definitions ============================

// Determines if the questpoint with the given name has had its
// fan-in count condition satisfied
//   name: The name of the quest point to test.
int SE2_Quest_FanInAchieved(object oPlayer, string name) {
    //debug("Checking if " + name + " can be activated");
    int fanIn = GetLocalInt(oPlayer, name + QUEST_POINT_FANIN);
    int i;
    string parentsArray = name + QUEST_POINT_PARENTS;

    //debug("Checking fan-in of " + name);

    // count the number of successful parents.
    for(i = 0; i < SCEZ_Struct_ArrayGetSize(oPlayer, parentsArray); i++){
        string parent = SCEZ_Struct_ArrayGetElementAtIndex(oPlayer, parentsArray, i);

        if (SE2_Quest_GetState(oPlayer, parent) == QUEST_POINT_STATE_SUCCESS) {
            fanIn--;
        }
    }

    // return whether the fanIn condition has been satisfied
    return fanIn <= 0;
}

void SE2_Quest_RegisterQuestPoint(object oPlayer, string name, int fanIn) {
    SE2_Quest_SetState(oPlayer, name, QUEST_POINT_STATE_NONE);
    SetLocalInt(oPlayer, name + QUEST_POINT_FANIN, fanIn);

}

// registers the given parent for the given questpoint
void SE2_Quest_RegisterQuestPointParent(string name, string parent, object player) {
    SCEZ_Struct_ArrayAppendElement(player, name + QUEST_POINT_PARENTS, parent);
}

// registers the given child for the given questpoint
void SE2_Quest_RegisterQuestPointChild(string name, string child, object player) {
    SCEZ_Struct_ArrayAppendElement(player, name + QUEST_POINT_CHILDREN, child);
}

void SE2_Quest_ValidateQuestPoint(object oPlayer, string name) {
    int curState = SE2_Quest_GetState(oPlayer, name);
    string childArray = name + QUEST_POINT_CHILDREN;
    int i;

    //debug("Validating " + name);

    if (SE2_Quest_FanInAchieved(oPlayer, name)){
        // don't downgrade from success or failure to enabled.
        if(curState != QUEST_POINT_STATE_SUCCESS && curState != QUEST_POINT_STATE_FAIL ){
            //debug("Validation is enabling " + name);
            SE2_Quest_SetState(oPlayer, name, QUEST_POINT_STATE_ENABLED);
        }
    } else {
        //debug("Validation is disabling " + name);
        // Remember: setting state will also cause the rest of the graph
        // to validate.
        SE2_Quest_SetState(oPlayer, name, QUEST_POINT_STATE_NONE);
    }
}

void SE2_Quest_ResetAtQuestPoint(object oPlayer, string name) {
    //debug("Resetting at " + name);

    SE2_Quest_SetState(oPlayer, name, QUEST_POINT_STATE_ENABLED);
}

void SE2_Quest_SetStateForRelatives(string name, int state, string relativeList) {
    object player = GetFirstPC();
    string tempArray = "tempQuestRelativesArray";
    string relative;
    int i;

    SE2_Quest_GetAllRelatives(name, relativeList, tempArray);

    //debug("Setting relatives of " + name + " to " + IntToString(state));

    // for each relative
    for(i = 0; i < SCEZ_Struct_ArrayGetSize(player, tempArray); i++) {
        relative = SCEZ_Struct_ArrayGetElementAtIndex(player, tempArray, i);

        SE2_Quest_SetState(player, relative, state);
    }

    // garbage collection: remove the temp array.
    SCEZ_Struct_ArrayClear(player, tempArray);
}

void SE2_Quest_GetAllRelatives(string name, string relativeList, string tempArray) {
    object player = GetFirstPC();
    string relativesListName = name + relativeList;
    int i;
    string relative;

    // for each relative
    for(i = 0; i < SCEZ_Struct_ArrayGetSize(player, relativesListName); i++) {
        relative = SCEZ_Struct_ArrayGetElementAtIndex(player, relativesListName, i);

        // append the relative
        SCEZ_Struct_ArrayAppendElement(player, tempArray, relative);

        // check the relative for more relatives
        SE2_Quest_GetAllRelatives(relative, relativeList, tempArray);
    }
}

int SE2_Quest_GetState(object oPlayer, string name) {
    return GetLocalInt(oPlayer, name + QUEST_POINT_STATE);
}

void SE2_Quest_SetState(object oPlayer, string name, int state) {
    object module = GetModule();
    int curState = SE2_Quest_GetState(oPlayer, name);

    // don't bother setting the state to what it is already,
    // since we'll just end up throwing spurious events.
    if(curState == state){
        return;
    }

    //debug("setting " + name + "'s state to " + IntToString(state));

    SetLocalInt(oPlayer, name + QUEST_POINT_STATE, state);

    // validate all of the node's children since this one
    // changed its status.
    string childArray = name + QUEST_POINT_CHILDREN;
    int i;
    string child;

    // validate the rest of the graph, now that this one changed.
    for(i = 0; i < SCEZ_Struct_ArrayGetSize(oPlayer, childArray); i++){
        child = SCEZ_Struct_ArrayGetElementAtIndex(oPlayer, childArray, i);
        SE2_Quest_ValidateQuestPoint(oPlayer, child);
    }

    if(state == QUEST_POINT_STATE_ENABLED){
        SCEZ_Struct_ArrayAppendElement(module, QUEST_POINTS_ENABLED_LIST, name);
        SignalEvent(module, EventUserDefined(SE2_QUEST_ENABLED_EVENT));
    } else{
        SCEZ_Struct_ArrayRemoveElement(module, QUEST_POINTS_ENABLED_LIST, name);
    }
}
