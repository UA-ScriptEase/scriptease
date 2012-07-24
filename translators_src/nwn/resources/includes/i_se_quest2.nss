#include "i_se_structs"

// ============================ Quest constants ============================
// ScriptEase2 Quest pattern include file
//
// Author: ScriptEase Team
// =========================================================================


// ============================ Function Declarations ============================

// Registers the Quest Point with the given name in the quest system.
//   name:      The name of the quest point.
//   fanIn:     The number of preceding quest points that must be completed prior.
//   player:    The player who owns the quest.
void SE2_Quest_RegisterQuestPoint(string name, int fanIn, object player);

// registers the given parent for the given questpoint
void SE2_Quest_RegisterQuestPointParent(string name, string parent, object player);

// registers the given child for the given questpoint
void SE2_Quest_RegisterQuestPointChild(string name, string child, object player);

// Determines if the questpoint with the given name has had its
// fan-in count condition satisfied
//   name: The name of the quest point to test.
int SE2_Quest_FanInAchieved(string name);

// sets the questpoint and ancestor nodes to inactive, sets the questpoint to succeeded and activates the children points if they are able to be activated.
void SE2_Quest_SucceedQuestPoint(string name);

// sets the questpoint and ancestor nodes to inactive. sets the questpoint to failed. checks if the Quest can now not be succeeded.
void SE2_Quest_FailQuestPoint(string name);

// Gets the given quest point's activation status as one of
// QUEST_POINT_STATE_*.
//   oPlayer:   The owner of the quest point
//   name:      The name of the quest point to get active status for
int SE2_Quest_getState(object oPlayer, string name);

// Sets the given quest point's activation status as a boolean
//   oPlayer:   The owner of the quest point
//   name:      The name of the quest point to set active status for
//   state:     The new status as one of QUEST_POINT_STATE_*.
int SE2_Quest_setState(object oPlayer, string name, int state);

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
//   name: The name of the quest point to reset from.
void SE2_Quest_ResetAtQuestPoint(string name);

// registers the given quest in the quest system
//void SE2_Quest_RegisterQuest(string name, string start, string end);

// Validates the quest system by checking if the quest cannot be succeeded
// This should be called after every Quest Point failure
//void SE2_Quest_ValidateQuest(string name);

// Disables all points in the quest
//void SE2_Quest_FailQuest(string name);

// Activates the quest’s start point
//void SE2_Quest_StartQuest(string name);

// ============================ Quest constants ============================
// The event number for quests.
const int SE2_QUEST_ENABLED_EVENT = 2012;

// State for a disabled Quest Point
const int QUEST_POINT_STATE_DISABLED = 0;
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
// Track the containing Quest for a QuestPoint
const string QUEST_POINT_CONTAINER = "_QuestPointContainer";
// Track the start of a Quest
const string QUEST_START = "_QuestStart";
// Track the end of a Quest
const string QUEST_END = "_QuestEnd";
// Track if a Quest is currently active
const string QUEST_ACTIVE = "_QuestActive";

const string QUEST_POINTS_ENABLED_LIST = "QuestPointsRecentlyEnabled";


// ============================ Function Definitions ============================

// Determines if the questpoint with the given name has had its
// fan-in count condition satisfied
//   name: The name of the quest point to test.
int SE2_Quest_FanInAchieved(string name) {
    //SendMessageToPC(GetFirstPC(), "Checking if " + name + " can be activated");
    object player = GetFirstPC();
    int fanIn = GetLocalInt(player, name + QUEST_POINT_FANIN);
    int i;
    string parentsArray = name + QUEST_POINT_PARENTS;

    // for each parent
    for(i = 0; i < SCEZ_Struct_ArrayGetSize(player, parentsArray); i++){
        string parent = SCEZ_Struct_ArrayGetElementAtIndex(player, parentsArray, i);

        // has the parent succeeded?
        if (SE2_Quest_getState(player, parent) == QUEST_POINT_STATE_SUCCESS) {
            fanIn--;
        }
    }

    // return whether the fanIn condition has been satisfied
    return fanIn <= 0;
}

void SE2_Quest_RegisterQuestPoint(string name, int fanIn, object player) {
    SE2_Quest_setState(player, name, QUEST_POINT_STATE_DISABLED);
    SetLocalInt(player, name + QUEST_POINT_FANIN, fanIn);

    // set up QUEST_POINT_CONTAINER
    //setLocalString(player, name + QUEST_POINT_CONTAINER, containerQuest);
}

// registers the given parent for the given questpoint
void SE2_Quest_RegisterQuestPointParent(string name, string parent, object player) {
    SCEZ_Struct_ArrayAppendElement(player, name + QUEST_POINT_PARENTS, parent);
}

// registers the given child for the given questpoint
void SE2_Quest_RegisterQuestPointChild(string name, string child, object player) {
    SCEZ_Struct_ArrayAppendElement(player, name + QUEST_POINT_CHILDREN, child);
}

//sets the questpoint and ancestor nodes to inactive, sets the questpoint to succeeded and activates the children points if they are able to be activated.
void SE2_Quest_SucceedQuestPoint(string name) {
    object player = GetFirstPC();
    string childListName = name + QUEST_POINT_CHILDREN;
    int i;

    SE2_Quest_setState(player, name, QUEST_POINT_STATE_SUCCESS);

    //debug("Child list size for " + name + ":" + IntToString(SCEZ_Struct_ArrayGetSize(player, childListName)));

    // get children and activate them
    // for each child
    for(i = 0; i < SCEZ_Struct_ArrayGetSize(player, childListName); i++){
        string child = SCEZ_Struct_ArrayGetElementAtIndex(player, childListName, i);

        //debug("Trying to Activate : " + child);

        if (SE2_Quest_FanInAchieved(child)) {
            //debug("Enabling " + child);
            SE2_Quest_setState(player, child, QUEST_POINT_STATE_ENABLED);
        }
    }
}

void SE2_Quest_ResetAtQuestPoint(string name) {
    debug("Resetting at " + name);

    object player = GetFirstPC();
    SE2_Quest_setState(player, name, QUEST_POINT_STATE_ENABLED);
    SE2_Quest_SetStateForRelatives(name, QUEST_POINT_STATE_DISABLED, QUEST_POINT_CHILDREN);
}

void SE2_Quest_SetStateForRelatives(string name, int state, string relativeList) {
    object player = GetFirstPC();
    string tempArray = "tempQuestRelativesArray";
    string relative;
    int i;

    SE2_Quest_GetAllRelatives(name, relativeList, tempArray);
    
    debug("Setting relatives of " + name + " to " + IntToString(state));

    // for each relative
    for(i = 0; i < SCEZ_Struct_ArrayGetSize(player, tempArray); i++) {
        relative = SCEZ_Struct_ArrayGetElementAtIndex(player, tempArray, i);

        SE2_Quest_setState(player, relative, state);
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

// sets the questpoint and ancestor nodes to inactive. sets the questpoint to failed. checks if the Quest can now not be succeeded.
void SE2_Quest_FailQuestPoint(string name) {
    object player = GetFirstPC();

    SE2_Quest_setState(player, name, QUEST_POINT_STATE_FAIL);

    // validate the quest
    //SE2_Quest_ValidateQuest();
}

int SE2_Quest_getState(object oPlayer, string name) {
    return GetLocalInt(oPlayer, name + QUEST_POINT_STATE);
}

void SE2_Quest_setState(object oPlayer, string name, int state) {
    object module = GetModule();
    int curState = SE2_Quest_getState(oPlayer, name);
    
	// don't bother setting the state to what it is already, 
	// since we'll just end up throwing spurious events.
    if(curState == state){
        return;
    }

    debug("setting " + name + "'s state to " + IntToString(state));

    SetLocalInt(oPlayer, name + QUEST_POINT_STATE, state);

    if(state == QUEST_POINT_STATE_ENABLED){
        SCEZ_Struct_ArrayAppendElement(module, QUEST_POINTS_ENABLED_LIST, name);
        SignalEvent(module, EventUserDefined(SE2_QUEST_ENABLED_EVENT));
    } else{
        SCEZ_Struct_ArrayRemoveElement(module, QUEST_POINTS_ENABLED_LIST, name);
    }
}

// registers the given quest in the quest system
//void SE2_Quest_RegisterQuest(string name, string start, string end) {
    // set up data on the player
    //object player = GetFirstPC();
    // set up QUEST_START
    //SetLocalString(player, name + QUEST_START, start);
    // set up QUEST_END
    //SetLocalString(player, name + QUEST_END, end);
    // set up QUEST_ACTIVE default to false
    //SetLocalInt(player, name + QUEST_ACTIVE, 0);
    // set up QUEST_START
    //SetLocalString(player, name + QUEST_CONTAINER, containerQuest);
//}

// activates the quest’s start point
//void SE2_Quest_StartQuest(string name) {
    // set up data on the player
    //object player = GetFirstPC();
    // get the start of the quest
    //string start = getLocalString(player, name + QUEST_START);
    // activate start
    //setLocalInt(player, start + QUEST_POINT_ACTIVE, 1);
//}

//disables all points in the quest
//void SE2_Quest_FailQuest(string name) {
//TODO
//}

// validates the quest system by checking if the quest cannot be succeeded,
// this should be called after every questpoint failure
//void SE2_Quest_ValidateQuest(string name) {
    //object player = GetFirstPC();
    //string start = getLocalString(player, name + QUEST_START);

//}