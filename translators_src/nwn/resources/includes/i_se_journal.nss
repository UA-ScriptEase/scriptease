/*
 * Journal entry interface include file. Intended for use with ScriptEase Quests.
 * Provides handy dandy functions for modifying the stored journal entries.
 * Journal entries and their history are stored as local strings, which are
 * then applied to a custom token which the only contents of a journal entry text.
 *
 * Author: ScriptEase team, University of Alberta
 *         http://www.cs.ualberta.ca/~script
 */

// Pushes the locally stored quest description with full history to
// the custom token to be displayed in the journal entry.
//   quest: The quest ID string for the quest to update
//   target: The target who should own the quest history.
void SCEZ_Journal_UpdateQuest(string quest, object target);

//
void SCEZ_Journal_SetQuestFinished(string quest, object target, int finished);
void SCEZ_Journal_AddToQuestHistory(string quest, object target, string entry);
void SCEZ_Journal_ClearQuestHistory(string quest, object target);
void SCEZ_Journal_SetQuestDescription(string quest, object target, string description);
int SCEZ_Journal_GetQuestHistoryEmpty(string quest, object target);
string SCEZ_Journal_ReplaceTags(string str, object target);
string SCEZ_Journal_ReplaceTag(string str, object target);

void SCEZ_Journal_UpdateQuest(string quest, object target) {
  string questDescription = GetLocalString(target, quest + "_JournalQuestDescription");
  string questHistory = GetLocalString(target, quest + "_JournalQuestHistory");

  int questFinished = GetLocalInt(target, quest + "_JournalQuestFinished");
  int questToken = StringToInt(GetSubString(quest, 5, GetStringLength(quest) - 1));

  int questOffset = GetLocalInt(target, quest + "_JournalQuestOffset");
  SetLocalInt(target, quest + "_JournalQuestOffset", !questOffset);

  if(questDescription == "")
  {
    SetCustomToken(questToken, questHistory);
  }
  else if(questHistory == "")
  {
    SetCustomToken(questToken, questDescription);
  }
  else
  {
    SetCustomToken(questToken, questDescription + "\n\n" + questHistory);
  }

  AddJournalQuestEntry(quest, 2 * questFinished + questOffset + 1, target, TRUE, FALSE, TRUE);
}


void SCEZ_Journal_SetQuestFinished(string quest, object target, int finished)
{
  SetLocalInt(target, quest + "_JournalQuestFinished", finished);
}

void SCEZ_Journal_AddToQuestHistory(string quest, object target, string entry)
{
  if(entry != "")
  {
    string history = GetLocalString(target, quest + "_JournalQuestHistory");

    if(history != "")
    {
      history = history + "\n\n";
    }

    entry = SCEZ_Journal_ReplaceTags(entry, target);

    history = history + "- " + entry;

    SetLocalString(target, quest + "_JournalQuestHistory", history);
  }
}

void SCEZ_Journal_ClearQuestHistory(string quest, object target)
{
  SetLocalString(target, quest + "_JournalQuestHistory", "");
}

void SCEZ_Journal_SetQuestDescription(string quest, object target, string description)
{
  description = SCEZ_Journal_ReplaceTags(description, target);

  SetLocalString(target, quest + "_JournalQuestDescription", description);
}

int SCEZ_Journal_GetQuestHistoryEmpty(string quest, object target)
{
    string history = GetLocalString(target, quest + "_JournalQuestHistory");
    int strlen = GetStringLength(history);
    if(strlen == 0)
    {
        return TRUE;
    }
    else
    {
        return FALSE;
    }
}

string SCEZ_Journal_ReplaceTags(string str, object target)
{
  string tempstr = "";
  string substr = "";

  int i;
  int strlen = GetStringLength(str);
  int tagstart = 0;

  for(i = 0; i < strlen; i ++)
  {
    substr = GetSubString(str, i, 1);

    if(substr == "<")
    {
      tagstart = i;
    }
    else if(substr == ">")
    {
      tempstr = tempstr + GetSubString(str, 0, tagstart);

      substr = GetSubString(str, tagstart, i - tagstart + 1);

      substr = SCEZ_Journal_ReplaceTag(substr, target);

      tempstr = tempstr + substr;

      str = GetSubString(str, i + 1, strlen - i);
      i = -1;
      strlen = GetStringLength(str);
    }
  }

  return tempstr + str;
}

string SCEZ_Journal_ReplaceTag(string str, object target)
{
  if(str == "<Boy/Girl>")
    if(GetGender(target) == GENDER_MALE)
      str = "Boy";
    else
      str = "Girl";
  else if(str == "<boy/girl>")
    if(GetGender(target) == GENDER_MALE)
      str = "boy";
    else
      str = "girl";
  else if(str == "<Brother/Sister>")
    if(GetGender(target) == GENDER_MALE)
      str = "Brother";
    else
      str = "Sister";
  else if(str == "<brother/sister>")
    if(GetGender(target) == GENDER_MALE)
      str = "brother";
    else
      str = "sister";
  else if(str == "<He/She>")
    if(GetGender(target) == GENDER_MALE)
      str = "He";
    else
      str = "She";
  else if(str == "<he/she>")
    if(GetGender(target) == GENDER_MALE)
      str = "he";
    else
      str = "she";
  else if(str == "<Him/Her>")
    if(GetGender(target) == GENDER_MALE)
      str = "Him";
    else
      str = "Her";
  else if(str == "<him/her>")
    if(GetGender(target) == GENDER_MALE)
      str = "him";
    else
      str = "her";
  else if(str == "<His/Her>")
    if(GetGender(target) == GENDER_MALE)
       str = "His";
    else
      str = "Her";
  else if(str == "<his/her>")
    if(GetGender(target) == GENDER_MALE)
      str = "his";
    else
      str = "her";
  else if(str == "<His/Hers>")
    if(GetGender(target) == GENDER_MALE)
      str = "His";
    else
      str = "Hers";
  else if(str == "<his/hers>")
    if(GetGender(target) == GENDER_MALE)
      str = "his";
    else
      str = "hers";
  else if(str == "<Lad/Lass>")
    if(GetGender(target) == GENDER_MALE)
      str = "Lad";
    else
      str = "Lass";
  else if(str == "<lad/lass>")
    if(GetGender(target) == GENDER_MALE)
      str = "lad";
    else
      str = "lass";
  else if(str == "<Lord/Lady>")
    if(GetGender(target) == GENDER_MALE)
      str = "Lord";
    else
      str = "Lady";
  else if(str == "<lord/lady>")
    if(GetGender(target) == GENDER_MALE)
      str = "lord";
    else
      str = "lady";
  else if(str == "<Male/Female>")
    if(GetGender(target) == GENDER_MALE)
      str = "Male";
    else
      str = "Female";
  else if(str == "<male/female>")
    if(GetGender(target) == GENDER_MALE)
        str = "male";
      else
        str = "female";
  else if(str == "<Man/Woman>")
    if(GetGender(target) == GENDER_MALE)
      str = "Man";
    else
      str = "Woman";
  else if(str == "<man/woman>")
    if(GetGender(target) == GENDER_MALE)
      str = "man";
    else
      str = "woman";
  else if(str == "<Master/Mistress>")
    if(GetGender(target) == GENDER_MALE)
      str = "Master";
    else
      str = "Mistress";
  else if(str == "<master/mistress>")
    if(GetGender(target) == GENDER_MALE)
      str = "master";
    else
      str = "mistress";
  else if(str == "<Mister/Missus>")
    if(GetGender(target) == GENDER_MALE)
      str = "Mister";
    else
      str = "Missus";
  else if(str == "<mister/missus>")
    if(GetGender(target) == GENDER_MALE)
      str = "mister";
    else
      str = "missus";
  else if(str == "<Sir/Madam>")
    if(GetGender(target) == GENDER_MALE)
      str = "Sir";
    else
      str = "Madam";
  else if(str == "<sir/madam>")
    if(GetGender(target) == GENDER_MALE)
      str = "sir";
    else
      str = "madam";
  else if(str == "<Bitch/Bastard>")
    if(GetGender(target) == GENDER_MALE)
      str = "Bastard";
    else
      str = "Bitch";
  else if(str == "<bitch/bastard>")
    if(GetGender(target) == GENDER_MALE)
       str = "bastard";
    else
       str = "bitch";
  else
    str = "UNRECOGNIZED_TOKEN";

  return str;
}
