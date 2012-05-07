// ScriptEase2 Quest pattern include file @author mfchurch
// appends the given element to the end of the given list
string SE2_AppendListElement(string list, string element);
// Gets the first element from the given array (array stays unmodified)
string SE2_GetFirstArrayElement(string array);
// removes first element from the given string array and returns the new array
string SE2_RemoveFirstArrayElement(string array);
// returns the number of elements in the given array
int SE2_ArraySize(string array);
// checks if the questpoint with the given name can be activated. Checks if the fan in condition has been satisfied
int SE2_Quest_CanActivateQuestPoint(string name);
// sets the questpoint and ancestor nodes to inactive, sets the questpoint to succeeded and activates the children points if they are able to be activated.
void SE2_Quest_SucceedQuestPoint(string name);
// sets the questpoint and ancestor nodes to inactive. sets the questpoint to failed. checks if the Quest can now not be succeeded.
void SE2_Quest_FailQuestPoint(string name);
// deactivates all of the ancestors of the given questpoint
void SE2_Quest_DeactivateAncestors(string name);
// checks if the questpoint with the given name is currently active
int SE2_Quest_isActiveQuestPoint(string name);
// checks if the questpoint with the given name has succeeded
int SE2_Quest_hasSuceededQuestPoint(string name);
// checks if the questpoint with the given name has failed
int SE2_Quest_hasFailedQuestPoint(string name);
// registers the given questpoint in the quest system
void SE2_Quest_RegisterQuestPoint(string name, int commiting, int fanIn, object player);
// registers the given parent for the given questpoint
void SE2_Quest_RegisterQuestPointParent(string name, string parent, object player);
// registers the given child for the given questpoint
void SE2_Quest_RegisterQuestPointChild(string name, string child, object player);
// registers the given quest in the quest system
void SE2_Quest_RegisterQuest(string name, string start, string end);
// validates the quest system by checking if the quest cannot be succeeded, this should be called after every questpoint failure
void SE2_Quest_ValidateQuest(string name);
// disables all points in the quest
void SE2_Quest_FailQuest(string name);
// activates the quest’s start point
void SE2_Quest_StartQuest(string name);
// returns a string containing all of the ancestors of the given quest point
string SE2_Quest_GetAllAncestors(string name);

// Quest constants
const string SEPARATOR = "_";
// Track if a QuestPoint is currently active
const string QUEST_POINT_ACTIVE = "_QuestPointActive";
// Track if a QuestPoint is committing
const string QUEST_POINT_COMMITING = "_QuestPointCommiting";
// Track if a QuestPoint has succeeded
const string QUEST_POINT_SUCCEEDED = "_QuestPointSucceeded";
// Track if a QuestPoint has failed
const string QUEST_POINT_FAILED =  "_QuestPointFailed";
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

// appends the given element to the end of the given list and returns the list
string SE2_AppendListElement(string list, string element) {
    if (list == "") {
        list = element;
    } else {
        list += SEPARATOR + element;
    }
    return list;
}

// Gets the first element from the given array (array stays unmodified)
string SE2_GetFirstArrayElement(string array) {
    string first = array;
    int index = FindSubString(array, SEPARATOR);
    if (index != -1) {
        // set left to be the left of the delimiter in string
        first = GetStringLeft(array, index);
    }
    // return the left of the delimiter
    //SendMessageToPC(GetFirstPC(), "element: " + first + " from array: " + array);
    return first;
}

// returns the number of elements in the given array
int SE2_ArraySize(string array) {
    if (array == "")
        return 0;

    int count = 1;
    int index = 0;
    while ((index = FindSubString(array, SEPARATOR, index)) != -1)
        count++;
    return count;
}

// removes first element from the given string array and returns the new array
string SE2_RemoveFirstArrayElement(string array) {
    string newString = "";
    int arrayLength = GetStringLength(array);
    int separatorLength = GetStringLength(SEPARATOR);
    int index = FindSubString(array, SEPARATOR);
    if (index != -1) {
        newString = GetStringRight(array, arrayLength - (index + separatorLength));
    }
    //SendMessageToPC(GetFirstPC(), "Before: " + array + " After: " + newString);
    return newString;
}

// checks if the questpoint with the given name can be activated. Checks if the fan in condition has been satisfied
int SE2_Quest_CanActivateQuestPoint(string name) {
    //SendMessageToPC(GetFirstPC(), "Checking if " + name + " can be activated");
    // set up data on the player
    object player = GetFirstPC();
    // get the fan in
    int fanIn = GetLocalInt(player, name + QUEST_POINT_FANIN);
    // get parents
    string parents = GetLocalString(player, name + QUEST_POINT_PARENTS);

    // for each parent
    while (SE2_ArraySize(parents) > 0) {
        string parent = SE2_GetFirstArrayElement(parents);
        parents = SE2_RemoveFirstArrayElement(parents);
        // has the parent succeeded?
        if (GetLocalInt(player, parent + QUEST_POINT_SUCCEEDED)) {
            // subtract from the remaining fanIn
            fanIn--;
        }
    }

    // return whether the fanIn condition has been satisfied
    return fanIn <= 0;
}

// registers the given questpoint in the quest system
void SE2_Quest_RegisterQuestPoint(string name, int commiting, int fanIn, object player) {
    // set up QUEST_POINT_ACTIVE default to false
    SetLocalInt(player, name + QUEST_POINT_ACTIVE, 0);
    // set up QUEST_POINT_COMMITING
    SetLocalInt(player, name + QUEST_POINT_COMMITING, commiting);
    // set up QUEST_POINT_SUCCEEDED default to false
    SetLocalInt(player, name + QUEST_POINT_SUCCEEDED, 0);
    // set up QUEST_POINT_FAILED default to false
    SetLocalInt(player, name + QUEST_POINT_FAILED, 0);
    // set up QUEST_POINT_FANIN
    SetLocalInt(player, name + QUEST_POINT_FANIN, fanIn);
    // set up QUEST_POINT_CONTAINER
    //setLocalString(player, name + QUEST_POINT_CONTAINER, containerQuest);
}

// registers the given parent for the given questpoint
void SE2_Quest_RegisterQuestPointParent(string name, string parent, object player) {
    // get the current list of parents
    string parents = GetLocalString(player, name + QUEST_POINT_PARENTS);
    // append the parent to it
    parents = SE2_AppendListElement(parents, parent);
    // set up QUEST_POINT_PARENTS
    SetLocalString(player, name + QUEST_POINT_PARENTS, parents);
}

// registers the given child for the given questpoint
void SE2_Quest_RegisterQuestPointChild(string name, string child, object player) {
    // get the current list of children
    string children = GetLocalString(player, name + QUEST_POINT_CHILDREN);
    // append the child to it
    children = SE2_AppendListElement(children, child);
    // set up QUEST_POINT_CHILDREN
    SetLocalString(player, name + QUEST_POINT_CHILDREN, children);
}

//sets the questpoint and ancestor nodes to inactive, sets the questpoint to succeeded and activates the children points if they are able to be activated.
void SE2_Quest_SucceedQuestPoint(string name) {
    // set up data on the player
    object player = GetFirstPC();
    // set the questpoint to succeeded
    SetLocalInt(player, name + QUEST_POINT_SUCCEEDED, 1);
    // set up questpoint to not failed
    SetLocalInt(player, name + QUEST_POINT_FAILED, 0);
    // set the quest point to deactivated
    SetLocalInt(player, name + QUEST_POINT_ACTIVE, 0);

    // get children and activate them
    string children = GetLocalString(player, name + QUEST_POINT_CHILDREN);
    //SendMessageToPC(GetFirstPC(), "Activating children: " + children);

    // for each child
    while (SE2_ArraySize(children) > 0) {
        string child = SE2_GetFirstArrayElement(children);
        //SendMessageToPC(GetFirstPC(), "Child: " + child);
        children = SE2_RemoveFirstArrayElement(children);
        //SendMessageToPC(GetFirstPC(), "Children: " + children);
        //SendMessageToPC(GetFirstPC(), "Trying to Activate : " + child);
        // if the children are eligible to be activated
        if (SE2_Quest_CanActivateQuestPoint(child)) {
            //SendMessageToPC(GetFirstPC(), "Child Activated");
            // set the child to active
            SetLocalInt(player, child + QUEST_POINT_ACTIVE, 1);
        }
    }

    // deactivate all of the ancestors
    SE2_Quest_DeactivateAncestors(name);
}
// deactivates all of the ancestors of the given questpoint
void SE2_Quest_DeactivateAncestors(string name) {
    // set up data on the player
    object player = GetFirstPC();
    // get ancestors and deactivate them
    string ancestors = SE2_Quest_GetAllAncestors(name);
    string ancestor;
    // for each ancestor
    while (SE2_ArraySize(ancestors) > 0) {
        string ancestor = SE2_GetFirstArrayElement(ancestors);
        ancestors = SE2_RemoveFirstArrayElement(ancestors);
        // set the ancestor to deactivated
        SetLocalInt(player, ancestor + QUEST_POINT_ACTIVE, 0);
    }
}

// returns a string containing all of the ancestors of the given quest point
string SE2_Quest_GetAllAncestors(string name) {
    string ancestors = "";
    // set up data on the player
    object player = GetFirstPC();
    // get parents
    string parents = GetLocalString(player, name + QUEST_POINT_PARENTS);

    // for each parent
    while (SE2_ArraySize(parents) > 0) {
        string parent = SE2_GetFirstArrayElement(parents);
        parents = SE2_RemoveFirstArrayElement(parents);
        // append the parent as an ancestor
        ancestors = SE2_AppendListElement(ancestors, parent);
        // check the parents for ancestors
        ancestors = SE2_AppendListElement(ancestors, SE2_Quest_GetAllAncestors(parent));
    }
    return ancestors;
}

// sets the questpoint and ancestor nodes to inactive. sets the questpoint to failed. checks if the Quest can now not be succeeded.
void SE2_Quest_FailQuestPoint(string name) {
    // set up data on the player
    object player = GetFirstPC();
    // set the quest point to not succeeded
    SetLocalInt(player, name + QUEST_POINT_SUCCEEDED, 0);
    // set up quest point to failed
    SetLocalInt(player, name + QUEST_POINT_FAILED, 1);
    // set the quest point to deactivated
    SetLocalInt(player, name + QUEST_POINT_ACTIVE, 0);

    // deactivate all of the ancestors
    SE2_Quest_DeactivateAncestors(name);

    // validate the quest
    //SE2_Quest_ValidateQuest();
}

// checks if the questpoint with the given name is currently active
int SE2_Quest_isActiveQuestPoint(string name) {
    // set up data on the player
    object player = GetFirstPC();
    // return if the questpoint is active
    return GetLocalInt(player, name + QUEST_POINT_ACTIVE);
}

// checks if the questpoint with the given name has succeeded
int SE2_Quest_hasSuceededQuestPoint(string name) {
    // set up data on the player
    object player = GetFirstPC();
    // return if the questpoint is active
    return GetLocalInt(player, name + QUEST_POINT_SUCCEEDED);

}

// checks if the questpoint with the given name has failed
int SE2_Quest_hasFailedQuestPoint(string name) {
    // set up data on the player
    object player = GetFirstPC();
    // return if the questpoint is active
    return GetLocalInt(player, name + QUEST_POINT_FAILED);
}

// registers the given quest in the quest system
void SE2_Quest_RegisterQuest(string name, string start, string end) {
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
}

// activates the quest’s start point
void SE2_Quest_StartQuest(string name) {
    // set up data on the player
    //object player = GetFirstPC();
    // get the start of the quest
    //string start = getLocalString(player, name + QUEST_START);
    // activate start
    //setLocalInt(player, start + QUEST_POINT_ACTIVE, 1);
}

//disables all points in the quest
void SE2_Quest_FailQuest(string name) {
//TODO
}

// validates the quest system by checking if the quest cannot be succeeded, this should be called after every questpoint failure
void SE2_Quest_ValidateQuest(string name) {
    // set up data on the player
    //object player = GetFirstPC();
    // get the start of the quest
    //string start = getLocalString(player, name + QUEST_START);
    // check if the questpoint is active
    //int isActive = SE2_Quest_isActiveQuestPoint(start);
    //int succeeded = SE2_Quest_hasSuceededQuestPoint(start);
    //if (isActive) {
    //
    //} else {
    //  if (succeeded) {
    //      string children = getLocalString(player, start + QUEST_POINT_CHILDREN);
    //      string child;
    //      // for each child
    //      while ((child = SE2_SplitTextByDelimiter(children, SEPARATOR)) != "") {
    //      //TODO FINISH THIS METHOD
    //      }
    //  }
    //}
}