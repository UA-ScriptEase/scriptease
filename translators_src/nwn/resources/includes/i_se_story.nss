#include "i_se_structs"

// ============================ Story constants ============================
// ScriptEase2 Story pattern include file
//
// Author: ScriptEase Team
// =========================================================================


// ============================ Function Declarations ============================

// Registers the Story Point with the given name in the story system.
//   oPlayer:   The owner of the story point
//   name:      The name of the story point.
//   fanIn:     The number of preceding story points that must be completed prior.
void SE2_Story_RegisterStoryPoint(object player, string name, int fanIn);

// registers the given parent for the given storypoint
void SE2_Story_RegisterStoryPointParent(string name, string parent, object player);

// registers the given child for the given storypoint
void SE2_Story_RegisterStoryPointChild(string name, string child, object player);

// Determines if the storypoint with the given name has had its
// fan-in count condition satisfied
//   oPlayer:   The owner of the story point
//   name: The name of the story point to test.
int SE2_Story_FanInAchieved(object oPlayer, string name);

// Gets the given story point's activation status as one of
// STORY_POINT_STATE_*.
//   oPlayer:   The owner of the story point
//   name:      The name of the story point to get active status for
int SE2_Story_GetState(object oPlayer, string name);

// Sets the given story point's activation status as a boolean
//   oPlayer:   The owner of the story point
//   name:      The name of the story point to set active status for
//   state:     The new status as one of STORY_POINT_STATE_*.
int SE2_Story_SetState(object oPlayer, string name, int state);

// Sets the state of all story point relatives for the story point with
// the given name. NOTE: This does NOT set the state of the given root
// node.
//   name:   The name of the story point to start from.
//   state:  The new state for the relatives. One of STORY_POINT_STATE_*.
//   list:   The list to search through. One of STORY_POINT_CHILDREN or
//           STORY_POINT_PARENTS
void SE2_Story_SetStateForRelatives(string name, int state, string list);

// Populates an array with the names of all of the story points that lead to or follow
// from the story point with the given name. Use STORY_POINT_CHILDREN for all following,
// and STORY_POINT_PARENTS for all preceding.
//   name:  The name of the story point whose precursors are to be found
//   relativeList:  One of STORY_POINT_PARENTS or STORY_POINT_CHILDREN
//   tempList: the name of the temporary array to collect the relatives in.
void SE2_Story_GetAllRelatives(string name, string relativeList, string tempList);

// Resets the story point with the given name to enabled and
// all following story points to disabled.
//   oPlayer:   The owner of the story point
//   name: The name of the story point to reset from.
void SE2_Story_ResetAtStoryPoint(object oPlayer, string name);

// Resets the story point with the given name to enabled and
// all following story points to disabled.
//   oPlayer:   The owner of the story point
//   name: The name of the story point to reset from.
void SE2_Story_ValidateStoryPoint(object oPlayer, string name);

// ============================ Story constants ============================
// The event number for stories.
const int SE2_STORY_ENABLED_EVENT = 2012;

// State for a disabled Story Point
const int STORY_POINT_STATE_NONE = 0;
// State for an enabled Story Point
const int STORY_POINT_STATE_ENABLED = 1;
// State for a succeeded Story Point
const int STORY_POINT_STATE_SUCCESS = 2;
// State for a failed Story Point
const int STORY_POINT_STATE_FAIL = 3;

// suffix for variable to track story point's current state.
const string STORY_POINT_STATE = "_StoryPointState";
// Track the fanIn for a StoryPoint
const string STORY_POINT_FANIN = "_StoryPointFanIn";
// Track the children for a StoryPoint
const string STORY_POINT_CHILDREN = "_StoryPointChildren";
// Track the parents for a StoryPoint
const string STORY_POINT_PARENTS = "_StoryPointParents";

const string STORY_POINTS_ENABLED_LIST = "StoryPointsRecentlyEnabled";


// ============================ Function Definitions ============================

// Determines if the storypoint with the given name has had its
// fan-in count condition satisfied
//   name: The name of the story point to test.
int SE2_Story_FanInAchieved(object oPlayer, string name) {
    //debug("Checking if " + name + " can be activated");
    int fanIn = GetLocalInt(oPlayer, name + STORY_POINT_FANIN);
    int i;
    string parentsArray = name + STORY_POINT_PARENTS;

    //debug("Checking fan-in of " + name);

    // count the number of successful parents.
    for(i = 0; i < SCEZ_Struct_ArrayGetSize(oPlayer, parentsArray); i++){
        string parent = SCEZ_Struct_ArrayGetElementAtIndex(oPlayer, parentsArray, i);

        if (SE2_Story_GetState(oPlayer, parent) == STORY_POINT_STATE_SUCCESS) {
            fanIn--;
        }
    }

    // return whether the fanIn condition has been satisfied
    return fanIn <= 0;
}

void SE2_Story_RegisterStoryPoint(object oPlayer, string name, int fanIn) {
    SE2_Story_SetState(oPlayer, name, STORY_POINT_STATE_NONE);
    SetLocalInt(oPlayer, name + STORY_POINT_FANIN, fanIn);

}

// registers the given parent for the given storypoint
void SE2_Story_RegisterStoryPointParent(string name, string parent, object player) {
    SCEZ_Struct_ArrayAppendElement(player, name + STORY_POINT_PARENTS, parent);
}

// registers the given child for the given storypoint
void SE2_Story_RegisterStoryPointChild(string name, string child, object player) {
    SCEZ_Struct_ArrayAppendElement(player, name + STORY_POINT_CHILDREN, child);
}

void SE2_Story_ValidateStoryPoint(object oPlayer, string name) {
    int curState = SE2_Story_GetState(oPlayer, name);
    string childArray = name + STORY_POINT_CHILDREN;
    int i;

    //debug("Validating " + name);

    if (SE2_Story_FanInAchieved(oPlayer, name)){
        // don't downgrade from success or failure to enabled.
        if(curState != STORY_POINT_STATE_SUCCESS && curState != STORY_POINT_STATE_FAIL ){
            //debug("Validation is enabling " + name);
            SE2_Story_SetState(oPlayer, name, STORY_POINT_STATE_ENABLED);
        }
    } else {
        //debug("Validation is disabling " + name);
        // Remember: setting state will also cause the rest of the graph
        // to validate.
        SE2_Story_SetState(oPlayer, name, STORY_POINT_STATE_NONE);
    }
}

void SE2_Story_ResetAtStoryPoint(object oPlayer, string name) {
    //debug("Resetting at " + name);

    SE2_Story_SetState(oPlayer, name, STORY_POINT_STATE_ENABLED);
}

void SE2_Story_SetStateForRelatives(string name, int state, string relativeList) {
    object player = GetFirstPC();
    string tempArray = "tempStoryRelativesArray";
    string relative;
    int i;

    SE2_Story_GetAllRelatives(name, relativeList, tempArray);

    //debug("Setting relatives of " + name + " to " + IntToString(state));

    // for each relative
    for(i = 0; i < SCEZ_Struct_ArrayGetSize(player, tempArray); i++) {
        relative = SCEZ_Struct_ArrayGetElementAtIndex(player, tempArray, i);

        SE2_Story_SetState(player, relative, state);
    }

    // garbage collection: remove the temp array.
    SCEZ_Struct_ArrayClear(player, tempArray);
}

void SE2_Story_GetAllRelatives(string name, string relativeList, string tempArray) {
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
        SE2_Story_GetAllRelatives(relative, relativeList, tempArray);
    }
}

int SE2_Story_GetState(object oPlayer, string name) {
    return GetLocalInt(oPlayer, name + STORY_POINT_STATE);
}

void SE2_Story_SetState(object oPlayer, string name, int state) {
    object module = GetModule();
    int curState = SE2_Story_GetState(oPlayer, name);

    // don't bother setting the state to what it is already,
    // since we'll just end up throwing spurious events.
    if(curState == state){
        return;
    }

    //debug("setting " + name + "'s state to " + IntToString(state));

    SetLocalInt(oPlayer, name + STORY_POINT_STATE, state);

    // validate all of the node's children since this one
    // changed its status.
    string childArray = name + STORY_POINT_CHILDREN;
    int i;
    string child;

    // validate the rest of the graph, now that this one changed.
    for(i = 0; i < SCEZ_Struct_ArrayGetSize(oPlayer, childArray); i++){
        child = SCEZ_Struct_ArrayGetElementAtIndex(oPlayer, childArray, i);
        SE2_Story_ValidateStoryPoint(oPlayer, child);
    }

    if(state == STORY_POINT_STATE_ENABLED){
        SCEZ_Struct_ArrayAppendElement(module, STORY_POINTS_ENABLED_LIST, name);
        SignalEvent(module, EventUserDefined(SE2_STORY_ENABLED_EVENT));
    } else{
        SCEZ_Struct_ArrayRemoveElement(module, STORY_POINTS_ENABLED_LIST, name);
    }
}
