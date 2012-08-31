// Quest pattern include file
#include "i_se_journal"

const int SCEZ_QUEST_POINT_ENABLED = 1;
const int SCEZ_QUEST_POINT_REACHING = 2;
const int SCEZ_QUEST_POINT_FAILING = 3;
const int SCEZ_QUEST_POINT_REACHED = 4;
const int SCEZ_QUEST_POINT_FAILED = 5;
const int SCEZ_QUEST_COMPLETED = 6;
const int SCEZ_QUEST_FAILED = 7;

const int SCEZ_QUEST_POINT_TYPE_NORMAL = 0;
const int SCEZ_QUEST_POINT_TYPE_COMMITTED = 1;
const int SCEZ_QUEST_POINT_TYPE_CLOSE = 2;

int SCEZ_Quest_Event_InitializeQuest(string quest, string description, int numClosePoints, string failureEntry, string parentQuest);
int SCEZ_Quest_Event_QuestCompleted(string quest);
int SCEZ_Quest_Event_QuestFailed(string quest);
int SCEZ_Quest_Event_PointIsEnabled(string quest, string point);
int SCEZ_Quest_Event_PointIsReached(string quest, string point);
int SCEZ_Quest_Event_PointIsFailed(string quest, string point); // Provide implementation
int SCEZ_Quest_Event_PointBecomesReached(string quest, string point, int type, string entry, int xp, int gold);
int SCEZ_Quest_Event_PointBecomesFailed(string quest, string point, int type, string entry, string enablers, int enablersRequired);
int SCEZ_Quest_Event_PointEnabledBy(string quest, string point, string enablers, int enablersRequired);
int SCEZ_Quest_Event_PointFailedBy(string quest, string point, string enablers);

void SCEZ_Quest_FireEvent(string quest, string point, int type);
void SCEZ_Quest_SetEventPoint(string quest, string point);
string SCEZ_Quest_GetEventPoint(string quest);
void SCEZ_Quest_SetEventType(string quest, int type);
int SCEZ_Quest_GetEventType(string quest);
void SCEZ_Quest_SetPointEncounterOccurred(string quest, string point, int occurred);
int SCEZ_Quest_GetPointEncounterOccurred(string quest, string point);
void SCEZ_Quest_SetPointFailureEncounterOccurred(string quest, string point, int occurred);
int SCEZ_Quest_GetPointFailureEncounterOccurred(string quest, string point);
void SCEZ_Quest_SetPointReached(string quest, string point, int reached);
int SCEZ_Quest_GetPointReached(string quest, string point);
void SCEZ_Quest_SetPointFailed(string quest, string point, int reached);
int SCEZ_Quest_GetPointFailed(string quest, string point);
void SCEZ_Quest_SetEnablersReached(string quest, string point, int times);
int SCEZ_Quest_GetEnablersReached(string quest, string point);
void SCEZ_Quest_SetEnablersFailed(string quest, string point, int times);
int SCEZ_Quest_GetEnablersFailed(string quest, string point);
void SCEZ_Quest_SetPointEnabled(string quest, string point, int enabled);
int SCEZ_Quest_GetPointEnabled(string quest, string point);
void SCEZ_Quest_SetQuestTimeStamp(string quest, int timestamp);
int SCEZ_Quest_GetQuestTimeStamp(string quest);
void SCEZ_Quest_SetPointTimeStamp(string quest, string point, int timestamp);
int SCEZ_Quest_GetPointTimeStamp(string quest, string point);
void SCEZ_Quest_SetQuestEnabled(string quest, int enabled);
int SCEZ_Quest_GetQuestEnabled(string quest);
void SCEZ_Quest_SetQuestCompleted(string quest, int completed);
int SCEZ_Quest_GetQuestCompleted(string quest);
void SCEZ_Quest_SetQuestFailed(string quest, int completed);
int SCEZ_Quest_GetQuestFailed(string quest);
void SCEZ_Quest_SetNumClosePoints(string quest, int closePoints);
int SCEZ_Quest_GetNumClosePoints(string quest);
void SCEZ_Quest_SetNumClosePointsFailed(string quest, int closePoints);
int SCEZ_Quest_GetNumClosePointsFailed(string quest);
void SCEZ_Quest_SetQuestFailureEntry(string quest, string entry);
string SCEZ_Quest_GetQuestFailureEntry(string quest);
int SCEZ_Quest_CountEnablers(string enablers);
void SCEZ_Quest_SetParentQuest(string quest, string parentQuest);
string SCEZ_Quest_GetParentQuest(string quest);
string SCEZ_Quest_GetBaseQuest(string quest);
void SCEZ_Quest_SetQuestAbandoned(string quest, string journalEntry);

int SCEZ_Quest_Event_UserPointIsEnabled(string pointpair);
int SCEZ_Quest_Event_UserPointIsReached(string pointpair);
int SCEZ_Quest_Event_UserPointBecomesReached(string pointpair, int type, string entry, int xp, int gold);
int SCEZ_Quest_Event_UserPointBecomesFailed(string pointpair, int type, string entry, string enablers, int enablersRequired);
int SCEZ_Quest_Event_UserPointEnabledBy(string pointpair, string enablers, int enablersRequired);
int SCEZ_Quest_Event_UserPointFailedBy(string pointpair, string enablers);
int SCEZ_Quest_UserGetPointEnabled(string pointpair);
int SCEZ_Quest_UserGetPointFailed(string pointpair);
int SCEZ_Quest_UserGetPointReached(string pointpair);

string SCEZ_Quest_extractStringHead(string source, string separator);
string SCEZ_Quest_extractStringTail(string source, string separator);
string SCEZ_Quest_extractQuestPoint(string source);
string SCEZ_Quest_extractQuest(string source);

int SCEZ_Quest_Event_InitializeQuest(string quest, string description, int numClosePoints, string failureEntry, string parentQuest)
{
  SCEZ_Quest_SetQuestTimeStamp(quest, 1);

  SCEZ_Journal_SetQuestDescription(quest, GetFirstPC(), description);

  SCEZ_Quest_SetNumClosePoints(quest, numClosePoints);

  SCEZ_Quest_SetQuestFailureEntry(quest, failureEntry);

  SCEZ_Quest_SetParentQuest(quest, parentQuest);

  return TRUE;
}

int SCEZ_Quest_Event_QuestCompleted(string quest)
{
  return SCEZ_Quest_GetEventType(quest) == SCEZ_QUEST_COMPLETED;
}

int SCEZ_Quest_Event_QuestFailed(string quest)
{
  return SCEZ_Quest_GetEventType(quest) == SCEZ_QUEST_FAILED;
}

int SCEZ_Quest_Event_PointIsReached(string quest, string point)
{
  if(SCEZ_Quest_GetEventType(quest) != SCEZ_QUEST_POINT_REACHED)
  {
    return FALSE;
  }

  if(SCEZ_Quest_GetEventPoint(quest) != point)
  {
    return FALSE;
  }

  if(SCEZ_Quest_GetQuestCompleted(quest) == TRUE)
  {
    return FALSE;
  }

  return TRUE;
}

int SCEZ_Quest_Event_PointBecomesReached(string quest, string point, int type, string entry, int xp, int gold)
{
  if(SCEZ_Quest_GetEventType(quest) != SCEZ_QUEST_POINT_REACHING)
  {
    return FALSE;
  }

  if(SCEZ_Quest_GetEventPoint(quest) != point)
  {
    return FALSE;
  }

  if(SCEZ_Quest_GetPointReached(quest, point) != FALSE)
  {
    return FALSE;
  }

  if(SCEZ_Quest_GetPointFailed(quest, point) != FALSE)
  {
    return FALSE;
  }

  if(SCEZ_Quest_GetPointEnabled(quest, point) != TRUE)
  {
    return FALSE;
  }

  if(SCEZ_Quest_GetPointEncounterOccurred(quest, point) != TRUE)
  {
    if(SCEZ_Quest_GetPointFailureEncounterOccurred(quest, point) == TRUE)
    {
      SCEZ_Quest_FireEvent(quest, point, SCEZ_QUEST_POINT_FAILING);
    }

    return FALSE;
  }

  if(SCEZ_Quest_GetQuestCompleted(quest) != FALSE)
  {
    return FALSE;
  }

  if(SCEZ_Quest_GetQuestFailed(quest) != FALSE)
  {
    return FALSE;
  }

  string baseQuest = SCEZ_Quest_GetBaseQuest(quest);

  if(SCEZ_Quest_GetQuestCompleted(baseQuest) != FALSE)
  {
    return FALSE;
  }

  if(SCEZ_Quest_GetQuestFailed(baseQuest) != FALSE)
  {
    return FALSE;
  }

  object player = GetFirstPC();

  if(entry != "")
  {
    SCEZ_Journal_AddToQuestHistory(baseQuest, player, entry);
  }

  if(type != SCEZ_QUEST_POINT_TYPE_NORMAL)
  {
    SCEZ_Quest_SetQuestTimeStamp(quest, SCEZ_Quest_GetQuestTimeStamp(quest) + 1);
  }

  SCEZ_Quest_SetPointEnabled(quest, point, FALSE);
  SCEZ_Quest_SetPointReached(quest, point, TRUE);

  if(type == SCEZ_QUEST_POINT_TYPE_CLOSE)
  {
    SCEZ_Quest_SetQuestCompleted(quest, TRUE);
    SCEZ_Journal_SetQuestFinished(quest, player, TRUE);
  }

  if(entry != "" || type == SCEZ_QUEST_POINT_TYPE_CLOSE)
  {
    SCEZ_Journal_UpdateQuest(baseQuest, player);
  }

  GiveXPToCreature(player, xp);
  GiveGoldToCreature(player, gold);

  return TRUE;
}

int SCEZ_Quest_Event_PointBecomesFailed(string quest, string point, int type, string entry, string enablers, int enablersRequired)
{
  if(SCEZ_Quest_GetEventType(quest) != SCEZ_QUEST_POINT_FAILING)
  {
    return FALSE;
  }

  if(SCEZ_Quest_GetEventPoint(quest) != point)
  {
    return FALSE;
  }

  if(SCEZ_Quest_GetPointReached(quest, point) != FALSE)
  {
    return FALSE;
  }

  if(SCEZ_Quest_GetPointFailed(quest, point) != FALSE)
  {
    return FALSE;
  }

  if(SCEZ_Quest_GetQuestCompleted(quest) != FALSE)
  {
    return FALSE;
  }

  if(SCEZ_Quest_GetQuestFailed(quest) != FALSE)
  {
    return FALSE;
  }

  string baseQuest = SCEZ_Quest_GetBaseQuest(quest);

  if(SCEZ_Quest_GetQuestCompleted(baseQuest) != FALSE)
  {
    return FALSE;
  }

  if(SCEZ_Quest_GetQuestFailed(baseQuest) != FALSE)
  {
    return FALSE;
  }

  int rVal = FALSE;
  object player = GetFirstPC();

  if((SCEZ_Quest_GetPointEnabled(quest, point) == TRUE) && (SCEZ_Quest_GetPointFailureEncounterOccurred(quest, point) == TRUE))
  {
    if(entry != "")
    {
      SCEZ_Journal_AddToQuestHistory(baseQuest, player, entry);
      SCEZ_Journal_UpdateQuest(baseQuest, player);
    }

    SCEZ_Quest_SetPointEnabled(quest, point, FALSE);
    SCEZ_Quest_SetPointFailed(quest, point, TRUE);

    rVal = TRUE;
  }

  int numEnablers = SCEZ_Quest_CountEnablers(enablers);
  int enablersFailed = SCEZ_Quest_GetEnablersFailed(quest, point);
  if(enablersFailed >= (numEnablers - enablersRequired + 1))
  {
    SCEZ_Quest_SetPointFailed(quest, point, TRUE);
  }

  if(SCEZ_Quest_GetPointFailed(quest, point) == TRUE)
  {
    if(type == SCEZ_QUEST_POINT_TYPE_CLOSE)
    {
      int pointsFailed = SCEZ_Quest_GetNumClosePointsFailed(quest) + 1;
      SCEZ_Quest_SetNumClosePointsFailed(quest, pointsFailed);

      if(pointsFailed >= SCEZ_Quest_GetNumClosePoints(quest))
      {
        SCEZ_Quest_SetQuestFailed(quest, TRUE);

        entry = SCEZ_Quest_GetQuestFailureEntry(quest);
        if(entry != "")
        {
          SCEZ_Journal_AddToQuestHistory(baseQuest, player, entry);
          SCEZ_Journal_SetQuestFinished(quest, player, TRUE);
          SCEZ_Journal_UpdateQuest(baseQuest, player);
        }
        else{
          SCEZ_Journal_SetQuestFinished(quest, player, TRUE);
          SCEZ_Journal_UpdateQuest(baseQuest, player);
        }
      }
    }
  }

  return rVal;
}

int SCEZ_Quest_Event_PointIsEnabled(string quest, string point)
{
  if(SCEZ_Quest_GetEventType(quest) != SCEZ_QUEST_POINT_ENABLED)
  {
    return FALSE;
  }

  if(SCEZ_Quest_GetEventPoint(quest) != point)
  {
    return FALSE;
  }

  if(SCEZ_Quest_GetQuestCompleted(quest) == TRUE)
  {
    return FALSE;
  }

  return TRUE;
}

int SCEZ_Quest_Event_PointEnabledBy(string quest, string point, string enabledBy, int enablersRequired)
{
  if(SCEZ_Quest_GetEventType(quest) != SCEZ_QUEST_POINT_REACHED)
  {
    return FALSE;
  }

  if(SCEZ_Quest_GetPointReached(quest, point) == TRUE)
  {
    return FALSE;
  }

  if(SCEZ_Quest_GetPointFailed(quest, point) == TRUE)
  {
    return FALSE;
  }

  if(SCEZ_Quest_GetQuestCompleted(quest) == TRUE)
  {
    return FALSE;
  }

  if(SCEZ_Quest_GetQuestFailed(quest) == TRUE)
  {
    return FALSE;
  }
  string pointReached = SCEZ_Quest_GetEventPoint(quest);
  int pointIndex = FindSubString(enabledBy, pointReached);
  if(pointIndex == -1)
  {
    return FALSE;
  }

  int strlen = GetStringLength(pointReached);
  string charsAfter = GetSubString(enabledBy, pointIndex + strlen, 2);

  if(GetStringLength(enabledBy) != (pointIndex + strlen) && charsAfter != "||")
  {
    return FALSE;
  }

  int enablersReached = SCEZ_Quest_GetEnablersReached(quest, point) + 1;
  SCEZ_Quest_SetEnablersReached(quest, point, enablersReached);

  if(enablersReached >= enablersRequired)
  {
    SCEZ_Quest_FireEvent(quest, point, SCEZ_QUEST_POINT_ENABLED);
    SCEZ_Quest_SetPointEnabled(quest, point, TRUE);
  }

  return TRUE;
}

int SCEZ_Quest_Event_PointFailedBy(string quest, string point, string enabledBy)
{
  int eventType = SCEZ_Quest_GetEventType(quest);
  if(eventType != SCEZ_QUEST_POINT_REACHED && eventType != SCEZ_QUEST_POINT_FAILED)
  {
    return FALSE;
  }

  if(SCEZ_Quest_GetPointReached(quest, point) == TRUE)
  {
    return FALSE;
  }

  if(SCEZ_Quest_GetPointFailed(quest, point) == TRUE)
  {
    return FALSE;
  }

  if(SCEZ_Quest_GetQuestCompleted(quest) == TRUE)
  {
    return FALSE;
  }

  if(SCEZ_Quest_GetQuestFailed(quest) == TRUE)
  {
    return FALSE;
  }

  if(eventType == SCEZ_QUEST_POINT_REACHED)
  {
    int questTimeStamp = SCEZ_Quest_GetQuestTimeStamp(quest);
    int pointTimeStamp = SCEZ_Quest_GetPointTimeStamp(quest, point);

    if((0 < pointTimeStamp) && (pointTimeStamp < questTimeStamp))
    {
      SCEZ_Quest_SetPointTimeStamp(quest, point, 0);
      SCEZ_Quest_FireEvent(quest, point, SCEZ_QUEST_POINT_FAILING);
      SCEZ_Quest_SetEnablersFailed(quest, point, SCEZ_Quest_GetEnablersFailed(quest, point) + 1);

      return TRUE;
    }
    else
    {
      return FALSE;
    }
  }

  string pointFailed = SCEZ_Quest_GetEventPoint(quest);
  int pointIndex = FindSubString(enabledBy, pointFailed);
  if(pointIndex == -1)
  {
    return FALSE;
  }

  int strlen = GetStringLength(pointFailed);
  string charsAfter = GetSubString(enabledBy, pointIndex + strlen, 2);

  if(GetStringLength(enabledBy) != (pointIndex + strlen) && charsAfter != "||")
  {
    return FALSE;
  }

  SCEZ_Quest_FireEvent(quest, point, SCEZ_QUEST_POINT_FAILING);
  SCEZ_Quest_SetEnablersFailed(quest, point, SCEZ_Quest_GetEnablersFailed(quest, point) + 1);

  return TRUE;
}

void SCEZ_Quest_FireEvent(string quest, string point, int type)
{
  string oldEventPoint = SCEZ_Quest_GetEventPoint(quest);
  int oldEventType = SCEZ_Quest_GetEventType(quest);

  DelayCommand(0.0f, SCEZ_Quest_SetEventPoint(quest, point));
  DelayCommand(0.0f, SCEZ_Quest_SetEventType(quest, type));
  DelayCommand(0.0f, ExecuteScript("Q_" + quest, GetModule()));
  DelayCommand(0.0f, SCEZ_Quest_SetEventPoint(quest, oldEventPoint));
  DelayCommand(0.0f, SCEZ_Quest_SetEventType(quest, oldEventType));
}

void SCEZ_Quest_SetEventPoint(string quest, string point)
{
  SetLocalString(GetModule(), quest + "_EventPoint", point);
}

string SCEZ_Quest_GetEventPoint(string quest)
{
  return GetLocalString(GetModule(), quest + "_EventPoint");
}

void SCEZ_Quest_SetEventType(string quest, int type)
{
  SetLocalInt(GetModule(), quest + "_EventType", type);
}

int SCEZ_Quest_GetEventType(string quest)
{
  return GetLocalInt(GetModule(), quest + "_EventType");
}

void SCEZ_Quest_SetPointEncounterOccurred(string quest, string point, int occurred)
{
  if(occurred == TRUE)
  {
    SCEZ_Quest_FireEvent(quest, point, SCEZ_QUEST_POINT_REACHING);
  }

  SetLocalInt(GetModule(), quest + "_" + point + "_PointEncounterOccurred", occurred);
}

int SCEZ_Quest_GetPointEncounterOccurred(string quest, string point)
{
  return GetLocalInt(GetModule(), quest + "_" + point + "_PointEncounterOccurred");
}

void SCEZ_Quest_SetPointFailureEncounterOccurred(string quest, string point, int occurred)
{
  if(occurred == TRUE)
  {
    SCEZ_Quest_FireEvent(quest, point, SCEZ_QUEST_POINT_FAILING);
  }

  SetLocalInt(GetModule(), quest + "_" + point + "_PointFailureEncounterOccurred", occurred);
}

int SCEZ_Quest_GetPointFailureEncounterOccurred(string quest, string point)
{
  return GetLocalInt(GetModule(), quest + "_" + point + "_PointFailureEncounterOccurred");
}

void SCEZ_Quest_SetPointReached(string quest, string point, int reached)
{
  if(reached == TRUE)
  {
    if(SCEZ_Quest_GetPointFailed(quest, point) == TRUE)
    {
      return;
    }

    SCEZ_Quest_FireEvent(quest, point, SCEZ_QUEST_POINT_REACHED);
  }

  SetLocalInt(GetModule(), quest + "_" + point + "_PointReached", reached);
}

int SCEZ_Quest_GetPointReached(string quest, string point)
{
  return GetLocalInt(GetModule(), quest + "_" + point + "_PointReached");
}

void SCEZ_Quest_SetPointFailed(string quest, string point, int failed)
{
  if(failed == TRUE)
  {
    if(SCEZ_Quest_GetPointReached(quest, point) == TRUE)
    {
      return;
    }

    SCEZ_Quest_FireEvent(quest, point, SCEZ_QUEST_POINT_FAILED);
  }

  SetLocalInt(GetModule(), quest + "_" + point + "_PointFailed", failed);
}

int SCEZ_Quest_GetPointFailed(string quest, string point)
{
  return GetLocalInt(GetModule(), quest + "_" + point + "_PointFailed");
}

void SCEZ_Quest_SetEnablersReached(string quest, string point, int reached)
{
  SetLocalInt(GetModule(), quest + "_" + point + "_EnablersReached", reached);
}

int SCEZ_Quest_GetEnablersReached(string quest, string point)
{
  return GetLocalInt(GetModule(), quest + "_" + point + "_EnablersReached");
}

void SCEZ_Quest_SetEnablersFailed(string quest, string point, int failed)
{
  SetLocalInt(GetModule(), quest + "_" + point + "_EnablersFailed", failed);
}

int SCEZ_Quest_GetEnablersFailed(string quest, string point)
{
  return GetLocalInt(GetModule(), quest + "_" + point + "_EnablersFailed");
}

void SCEZ_Quest_SetPointEnabled(string quest, string point, int enabled)
{
  if(enabled)
  {
    enabled = SCEZ_Quest_GetQuestTimeStamp(quest);

    SCEZ_Quest_FireEvent(quest, point, SCEZ_QUEST_POINT_REACHING);
  }

  SCEZ_Quest_SetPointTimeStamp(quest, point, enabled);
}

int SCEZ_Quest_GetPointEnabled(string quest, string point)
{
  return SCEZ_Quest_GetQuestTimeStamp(quest) == SCEZ_Quest_GetPointTimeStamp(quest, point);
}

void SCEZ_Quest_SetQuestTimeStamp(string quest, int timestamp)
{
  SetLocalInt(GetModule(), quest + "_TimeStamp", timestamp);
}

int SCEZ_Quest_GetQuestTimeStamp(string quest)
{
  return GetLocalInt(GetModule(), quest + "_TimeStamp");
}

void SCEZ_Quest_SetPointTimeStamp(string quest, string point, int timestamp)
{
  SetLocalInt(GetModule(), quest + "_" + point + "_TimeStamp", timestamp);
}

int SCEZ_Quest_GetPointTimeStamp(string quest, string point)
{
  return GetLocalInt(GetModule(), quest + "_" + point + "_TimeStamp");
}

void SCEZ_Quest_SetQuestEnabled(string quest, int enabled)
{
  if(enabled)
  {
    if(SCEZ_Quest_GetPointReached(quest, "Start") == FALSE)
    {
      SCEZ_Quest_SetPointReached(quest, "Start", TRUE);

    }
  }

  SetLocalInt(GetModule(), quest + "_QuestEnabled", enabled);
}

int SCEZ_Quest_GetQuestEnabled(string quest)
{
  return GetLocalInt(GetModule(), quest + "_QuestEnabled");
}

void SCEZ_Quest_SetQuestCompleted(string quest, int completed)
{
  if(completed)
  {
    if(SCEZ_Quest_GetQuestFailed(quest) == TRUE)
    {
      return;
    }

    SCEZ_Quest_FireEvent(quest, "", SCEZ_QUEST_COMPLETED);
  }

  SetLocalInt(GetModule(), quest + "_QuestCompleted", completed);
}

int SCEZ_Quest_GetQuestCompleted(string quest)
{
  return GetLocalInt(GetModule(), quest + "_QuestCompleted");
}

void SCEZ_Quest_SetQuestFailed(string quest, int failed)
{
  if(failed)
  {
    if(SCEZ_Quest_GetQuestCompleted(quest) == TRUE)
    {
      return;
    }

    SCEZ_Quest_FireEvent(quest, "", SCEZ_QUEST_FAILED);
  }

  SetLocalInt(GetModule(), quest + "_QuestFailed", failed);
}

int SCEZ_Quest_GetQuestFailed(string quest)
{
  return GetLocalInt(GetModule(), quest + "_QuestFailed");
}

void SCEZ_Quest_SetNumClosePoints(string quest, int closePoints)
{
  SetLocalInt(GetModule(), quest + "_QuestNumClosePoints", closePoints);
}

int SCEZ_Quest_GetNumClosePoints(string quest)
{
  return GetLocalInt(GetModule(), quest + "_QuestNumClosePoints");
}

void SCEZ_Quest_SetNumClosePointsFailed(string quest, int closePoints)
{
  SetLocalInt(GetModule(), quest + "_QuestNumClosePointsFailed", closePoints);
}

int SCEZ_Quest_GetNumClosePointsFailed(string quest)
{
  return GetLocalInt(GetModule(), quest + "_QuestNumClosePointsFailed");
}

void SCEZ_Quest_SetQuestFailureEntry(string quest, string entry)
{
  SetLocalString(GetModule(), quest + "_QuestFailureEntry", entry);
}

string SCEZ_Quest_GetQuestFailureEntry(string quest)
{
  return GetLocalString(GetModule(), quest + "_QuestFailureEntry");
}

int SCEZ_Quest_CountEnablers(string enablers)
{
  if(enablers == "")
  {
    return 0;
  }

  int i;
  int numEnablers = 1;
  int strlen = GetStringLength(enablers);

  for(i = 0; i < (strlen - 1); i ++)
  {
    if(GetSubString(enablers, i, 2) == "||")
    {
      numEnablers ++;
    }
  }

  return numEnablers;
}

void SCEZ_Quest_SetParentQuest(string quest, string parentQuest)
{
  SetLocalString(GetModule(), quest + "_ParentQuest", parentQuest);
}

string SCEZ_Quest_GetParentQuest(string quest)
{
  return GetLocalString(GetModule(), quest + "_ParentQuest");
}

string SCEZ_Quest_GetBaseQuest(string quest)
{
  string questA = "";
  string questB = quest;

  while(questA != questB)
  {
    questA = questB;
    questB = SCEZ_Quest_GetParentQuest(questA);
  }

  return questA;
}

void SCEZ_Quest_SetQuestAbandoned(string quest, string journalEntry)
{
    object player = GetFirstPC();
    string baseQuest = SCEZ_Quest_GetBaseQuest(quest);

    if(!SCEZ_Quest_GetQuestCompleted(quest) && !SCEZ_Quest_GetQuestFailed(quest) ){
        if(!SCEZ_Journal_GetQuestHistoryEmpty(quest, player))
         //&& GetStringLength(journalEntry) != 0
        {
            SCEZ_Journal_AddToQuestHistory(quest, player, journalEntry);
            SCEZ_Journal_SetQuestFinished(quest, player, TRUE);
            SCEZ_Quest_SetQuestFailed(quest, TRUE);
            SCEZ_Journal_UpdateQuest(baseQuest, player);
        }
        else{
            SCEZ_Quest_SetQuestFailed(quest, TRUE);
        }
    }

}

// User Interface functions.  ScriptEase Atoms should call these functions, not their counterparts.



int SCEZ_Quest_Event_UserPointIsEnabled(string pointpair){
    string point = SCEZ_Quest_extractQuestPoint(pointpair);
    string quest = SCEZ_Quest_extractQuest(pointpair);

    return SCEZ_Quest_Event_PointIsEnabled(quest, point);
}

int SCEZ_Quest_Event_UserPointIsReached(string pointpair){
    string point = SCEZ_Quest_extractQuestPoint(pointpair);
    string quest = SCEZ_Quest_extractQuest(pointpair);

    return SCEZ_Quest_Event_PointIsReached(quest, point);
}

int SCEZ_Quest_Event_UserPointBecomesReached(string pointpair, int type, string entry, int xp, int gold){
    string point = SCEZ_Quest_extractQuestPoint(pointpair);
    string quest = SCEZ_Quest_extractQuest(pointpair);

    return SCEZ_Quest_Event_PointBecomesReached(quest, point, type, entry, xp, gold);
}

int SCEZ_Quest_Event_UserPointBecomesFailed(string pointpair, int type, string entry, string enablers, int enablersRequired){
    string point = SCEZ_Quest_extractQuestPoint(pointpair);
    string quest = SCEZ_Quest_extractQuest(pointpair);

    return SCEZ_Quest_Event_PointBecomesFailed(quest, point, type, entry, enablers, enablersRequired);
}

int SCEZ_Quest_Event_UserPointEnabledBy(string pointpair, string enablers, int enablersRequired){
    string point = SCEZ_Quest_extractQuestPoint(pointpair);
    string quest = SCEZ_Quest_extractQuest(pointpair);

    return SCEZ_Quest_Event_PointEnabledBy(quest, point, enablers, enablersRequired);
}

int SCEZ_Quest_Event_UserPointFailedBy(string pointpair, string enablers){
    string point = SCEZ_Quest_extractQuestPoint(pointpair);
    string quest = SCEZ_Quest_extractQuest(pointpair);

    return SCEZ_Quest_Event_PointFailedBy(quest, point, enablers);
}

int SCEZ_Quest_UserGetPointEnabled(string pointpair){
    string point = SCEZ_Quest_extractQuestPoint(pointpair);
    string quest = SCEZ_Quest_extractQuest(pointpair);

    return SCEZ_Quest_GetPointEnabled(quest, point);
}

int SCEZ_Quest_UserGetPointFailed(string pointpair){
    string point = SCEZ_Quest_extractQuestPoint(pointpair);
    string quest = SCEZ_Quest_extractQuest(pointpair);

    return SCEZ_Quest_GetPointFailed(quest, point);
}

int SCEZ_Quest_UserGetPointReached(string pointpair){
    string point = SCEZ_Quest_extractQuestPoint(pointpair);
    string quest = SCEZ_Quest_extractQuest(pointpair);

    return SCEZ_Quest_GetPointReached(quest, point);
}



// return everything before the first instance of separator, non-inclusive
string SCEZ_Quest_extractStringHead(string source, string separator){
    int sepPos = -1;

    sepPos = FindSubString(source, separator);

    if(sepPos == -1){
        return "";
    }
    else{
        return GetSubString(source, 0, sepPos);
    }
}

// Return everything after the first instance of separator, non-inclusive
string SCEZ_Quest_extractStringTail(string source, string separator){
    int sepPos = -1;

    sepPos = FindSubString(source, separator);

    if(sepPos == -1){
        return "";
    }
    else{
        return GetSubString(source, sepPos + GetStringLength(separator), GetStringLength(source)-sepPos);
    }
}

string SCEZ_Quest_extractQuestPoint(string source){
    return SCEZ_Quest_extractStringTail(source, "::");
}

string SCEZ_Quest_extractQuest(string source){
    return SCEZ_Quest_extractStringHead(source, "::");
}

/**
void main()
{
}
**/

