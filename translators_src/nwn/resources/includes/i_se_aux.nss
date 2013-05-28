// ScriptEase general auxiliary include file

// Gets the nearest object to target, including target itself.
object SE_AUX_GetNearestObjectByTagIncludeSelf(string tag, object target=OBJECT_SELF){
  object oFound = OBJECT_INVALID;

  if(GetTag(GetModule()) == tag) {
  	oFound = GetModule();
  }
  else if(GetTag(target) == tag){
      oFound = target;
  }
  else {
    oFound = GetNearestObjectByTag(tag, target);

    if(oFound == OBJECT_INVALID){
      oFound = GetObjectByTag(tag);
    }
  }

  return oFound;
}

/*  All this "specific" stuff is now on hold.  Kept for possible future use
// Gets a random object of the resref list, including self
object SE_AUX_GetRandomSpecificObjectWithSelf(string tag, string resref){
  int count = 0;
  int i = 0;
  int randomNumber = 0;
  object obj = GetObjectByTag(tag, i++);
  while (obj != OBJECT_INVALID) {
    if (GetResRef(obj) == resref)
      count++;
    obj = GetObjectByTag(tag, i++);
  }
  if (count == 0)
    return OBJECT_INVALID;
  randomNumber = Random(count);
  i = 0;
  count = 0;
  obj = GetObjectByTag(tag, i++);
  while (obj != OBJECT_INVALID) {
    if (GetResRef(obj) == resref) {
      if(count == randomNumber)
        break;
      count++;
    }
    obj = GetObjectByTag(tag, i++);
  }
  return obj;
}

// Gets the nearest object of the resref, excluding self
object SE_AUX_GetFirstSpecificObjectWithoutSelf(string tag, string resref){
  int i = 0;
  object obj;
  while (GetResRef(obj) != resref || obj == OBJECT_SELF){
    obj = GetObjectByTag(tag, i++);
    if (obj == OBJECT_INVALID) return OBJECT_INVALID;
  }
  return obj;
}

// Gets a random object of the resref list, excluding self
object SE_AUX_GetRandomSpecificObjectWithoutSelf(string tag, string resref){
  int count = 0;
  int i = 0;
  int randomNumber = 0;
  object obj = GetObjectByTag(tag, i++);
  while (obj != OBJECT_INVALID) {
    if (GetResRef(obj) == resref && obj != OBJECT_SELF)
      count++;
    obj = GetObjectByTag(tag, i++);
  }
  if (count == 0)
      return OBJECT_INVALID;
  randomNumber = Random(count);
  i = 0;
  count = 0;
  obj = GetObjectByTag(tag, i++);
  while (obj != OBJECT_INVALID) {
    if (GetResRef(obj) == resref && obj != OBJECT_SELF){
      if (count == randomNumber)
        break;
      count++;
    }
    obj = GetObjectByTag(tag, i++);
  }
  return obj;
}
*/

// Gets the nearest object from tag list, excluding self
object SE_AUX_GetNearestObjectWithoutSelf(string tag) {
  object obj = GetNearestObjectByTag(tag);
  if (obj == OBJECT_SELF)
    obj = GetNearestObjectByTag(tag, OBJECT_SELF, 1);
  return obj;
}

// Gets the Nth nearest object from tag list, excluding self
object SE_AUX_GetNthNearestObjectWithoutSelf(string tag, int nth) {
 return GetNearestObjectByTag(tag, OBJECT_SELF, nth);
}

// Gets a random object from tag list, including self
object SE_AUX_GetRandomObjectWithSelf(string tag) {
  int count = 0;
  int i = 0;
  int randomNumber = 0;
  object obj = GetObjectByTag(tag, i++);
  while (obj != OBJECT_INVALID) {
    count++;
    obj = GetObjectByTag(tag, i++);
  }
  if (count == 0)
    return OBJECT_INVALID;
  randomNumber = Random(count);
  i = 0;
  count = 0;
  obj = GetObjectByTag(tag, i++);
  while (obj != OBJECT_INVALID) {
    if (count == randomNumber)
      break;
    count++;
    obj = GetObjectByTag(tag, i++);
  }
  return obj;
}

// Gets a random object from tag list, excluding self
object SE_AUX_GetRandomObjectWithoutSelf(string tag) {
  int count = 0;
  int i = 0;
  int randomNumber = 0;
  int selfRef = -1;
  object obj = GetObjectByTag(tag, i++);
  while (obj != OBJECT_INVALID) {
      if (obj == OBJECT_SELF)
        selfRef = i;
      count++;
      obj = GetObjectByTag(tag, i++);
  }
  if (count == 0)
    return OBJECT_INVALID;
  randomNumber = Random(count);
  while (randomNumber == selfRef) //Make sure we don't pick self
    randomNumber = Random(count);
  i = 0;
  count = 0;
  obj = GetObjectByTag(tag, i++);
  while (obj != OBJECT_INVALID) {
    if (count == randomNumber)
      break;
    count++;
    obj = GetObjectByTag(tag, i++);
  }
  return obj;
}

// Derivative of CreateItemOnObject that can be placed on an action queue.
void SE_AUX_CreateItemOnObject(string blueprint, object target)
{
  CreateItemOnObject(blueprint, target);
}

// Function that determines if a one-time situation should run.
int SE_AUX_Once(string var, int nEntities, int shouldDelete)
{
  string instvar = var + "_ninst";
  int nInstances = GetLocalInt(GetModule(), instvar );
  int bFound = FALSE;
   /* linear search expecting this not to happen too often for any
    * one var.
    */
  int i,j;
  for( i = 0; i < nInstances; ++i ) {
    int b_iMatches = TRUE;
    for( j = 0; j < nEntities; ++j ) {
      string vij = var + "_" + IntToString(i) + "_" + IntToString(j);
      string vej = var + "_" + IntToString(j);

      if( GetLocalObject(GetModule(), vij) !=
          GetLocalObject(OBJECT_SELF, vej) ) {
        b_iMatches = FALSE;
        break;
      }
    } // for j
    if( b_iMatches == TRUE ) {
      bFound = TRUE;
      break;
    }
  } // for i

  if( bFound != TRUE ) {
    // record this set of entities.
    for( j = 0; j < nEntities; ++j ) {
      string vij = var + "_" + IntToString(nInstances) + "_" + IntToString(j);
      string vej = var + "_" + IntToString(j);

      SetLocalObject( GetModule(), vij, GetLocalObject(OBJECT_SELF, vej) );
    }
    // Increment count of previous occurrence set.
    SetLocalInt( GetModule(), instvar, nInstances + 1 );
  }
  /* regardless of whether this set of entities already existed, delete
   * the input array.
   */
  if( shouldDelete ) {
    for( j = 0; j < nEntities; ++j ) {
      string vej = var + "_" + IntToString(j);
      DeleteLocalObject( OBJECT_SELF, vej );
    }
  }

  // May proceed if no previous sets were found.
  return ! bFound;
}

// Return the resref based on the template ID
string SE_AUX_GetResRef(string templateID) 
{
	int dotLoc = FindSubString(templateID, ".");
	return GetStringLeft(templateID, dotLoc);
}

// Return the type of the object based on the template ID
int SE_AUX_GetType(string templateID) 
{
	int type;

	int dotLoc = FindSubString(templateID, ".");
	string typeExt = GetStringRight(templateID, GetStringLength(templateID) - (dotLoc + 1));
	
	   	// Cleverly determine that pesky constant from the template id (resref) extension.
    if(typeExt == "utc" || typeExt == "UTC"){
        // this little piggy was a piggy
        type = OBJECT_TYPE_CREATURE;
    } else if(typeExt == "uti" || typeExt == "UTI"){
        // this little piggy was a meal
        type = OBJECT_TYPE_ITEM;
    } else if(typeExt == "utp" || typeExt == "UTP"){
        // this little piggy was a statue
        type = OBJECT_TYPE_PLACEABLE;
    } else if(typeExt == "uts" || typeExt == "UTS"){
        // this little piggy was a butcher's shop
        type = OBJECT_TYPE_STORE;
    } else if(typeExt == "utw" || typeExt == "UTW"){
        // and this little piggy went wee wee wee, to the waypoint home.
        type = OBJECT_TYPE_WAYPOINT;
    }
    
    return type;
}

/***SUPERHEARTBEAT STUFF***/
/*
    This is an interface for user defined heartbeat events. This allows
    the user to define a heartbeat event with a much shorter duration than
    the standard six second heartbeat event.

    (THE FOLLOWING RANGE COMPONENT HAS BEEN REMOVED FOR THE TIME BEING)

    The heartbeat event also has a range component to it as well, so if the
    object with a heartbeat is not within range of a source then the heartbeat
    will not fire. This allows for a heartbeat event that is not CPU extensive
    if placed on a large amount of objects.
*/

/*
    Sets the super heartbeat event. When a super heartbeat occurs a userdefined
    event is fired on the target object.

    Multiple targets can be assigned to one activator and vice versa.

    target - This is the object that the super heartbeat is attached to. When
    a super heartbeat occurs a user defined event will be fired on the target
    object, with a specific event number.

    activator - This is the object that the target object must be within range
    of in order to have a super heartbeat occur. The range is 15.0 metres.
*/
void SE_AUX_SetSuperHeartBeat(object target, object activator);

/*
    Removes the super heart beat between the target and the activator.
*/
void SE_AUX_RemoveSuperHeartBeat(object target, object activator);

/*
    This function is called internally by SetSuperHeartBeat, there is no need
    for a user to call this function.
*/
void SE_AUX_PerformSuperHeartBeat(object activator);

/*
    Returns TRUE if the target object has just fired a super heartbeat given
    the user defined event number.

    Returns FALSE otherwise.
*/
int SE_AUX_IsSuperHeartBeat(object target, int event_num);

void SE_AUX_SetSuperHeartBeat(object target, object activator)
{
    // See if there is a super heartbeat already.
    int activator_id = GetLocalInt(activator, "SE_AUX_SHB_ACTIVATOR_ID");
    int num_targets = GetLocalInt(activator, "SE_AUX_SHB_NUM_TARGETS");

    // If this activator has no ID assigned to them...
    if(activator_id == 0)
    {
        // Assign a new ID.

        activator_id = 1 + GetLocalInt(GetModule(), "SE_AUX_SHB_LAST_KNOWN_ACTIVATOR_ID");
        SetLocalInt(GetModule(), "SE_AUX_SHB_LAST_KNOWN_ACTIVATOR_ID", activator_id);
        SetLocalInt(activator, "SE_AUX_SHB_ACTIVATOR_ID", activator_id);
    }

    string id_string = "SE_AUX_SHB_ACTIVATOR_ID_" + IntToString(activator_id);

    // If the activator hasn't registered the target then do so.
    if(GetLocalInt(target, id_string) == FALSE)
    {
        SetLocalInt(target, id_string, TRUE);
        SetLocalInt(activator, "SE_AUX_SHB_NUM_TARGETS", num_targets + 1);
    }

    // If the activator has just added its first target then we need to start
    // performing the super heart beat.
    if(num_targets == 0)
    {
        // This doesn't actually get called until we exit the current function.
        DelayCommand(0.0, SE_AUX_PerformSuperHeartBeat(activator));
    }
}

void SE_AUX_RemoveSuperHeartBeat(object target, object activator)
{
    int activator_id = GetLocalInt(activator, "SE_AUX_SHB_ACTIVATOR_ID");
    string id_string = "SE_AUX_SHB_ACTIVATOR_ID_" + IntToString(activator_id);

    int num_targets;

    // If the activator is registered with the target...
    if(GetLocalInt(target, id_string) == TRUE)
    {
        // Unregister the activator with the target.
        DeleteLocalInt(target, id_string);

        num_targets = GetLocalInt(activator, "SE_AUX_SHB_NUM_TARGETS");
        SetLocalInt(activator, "SE_AUX_SHB_NUM_TARGETS", num_targets - 1);
    }
}

void SE_AUX_PerformSuperHeartBeat(object activator)
{
    location activator_loc = GetLocation(activator);

    int obj_index = 0;
    object next_closest; // = GetNearestObject(OBJECT_TYPE_ALL, activator, obj_index);
    //float obj_distance = GetDistanceBetweenLocations(activator_loc, GetLocation(next_closest));

    int activator_id = GetLocalInt(activator, "SE_AUX_SHB_ACTIVATOR_ID");
    string id_string = "SE_AUX_SHB_ACTIVATOR_ID_" + IntToString(activator_id);

    string producer_string;
    object current_object;
    int producer_index;
    int buffer_size;

    // If the activator has 0 targets then we can stop performing the
    // super heart beat
    if(GetLocalInt(activator, "SE_AUX_SHB_NUM_TARGETS") == 0)
    {
        return;
    }

    // First check to see if the area the activator is in is registered for a
    // super heart beat with the area
    next_closest = GetArea(activator);

    // Loop through all objects within 15.0 metres of the activator
    while(GetIsObjectValid(next_closest)) // && obj_distance < 15.0)
    {
        // If the object has a super heart beat registered with the activator...
        if(GetLocalInt(next_closest, id_string) == TRUE)
        {
            // The activator has a bounded buffer of the activators that have
            // activated the super heart beat, add this activator to it.

            buffer_size = 50; // This is the buffer size.

            // The producer_index should be at the next unallocated space in
            // the buffer.
            producer_index = GetLocalInt(next_closest, "SE_AUX_SHB_PRODUCER_INDEX");

            producer_string = "SE_AUX_SHB_ACTIVATOR_" + IntToString(producer_index);
            current_object = GetLocalObject(next_closest, producer_string);

            // We can only add to the buffer if the current_object is invalid
            // otherwise the buffer is full.
            if(!GetIsObjectValid(current_object))
            {
                // Add the activator to the buffer.
                SetLocalObject(next_closest, producer_string, activator);

                // Increment the producer index
                producer_index ++;

                // Wrap around the buffer if needed.
                if(producer_index == buffer_size)
                {
                    producer_index = 0;
                }

                // Set the producer index.
                SetLocalInt(next_closest, "SE_AUX_SHB_PRODUCER_INDEX", producer_index);
            }

            // Finally signal the event.
            SignalEvent(next_closest, EventUserDefined(42));
        }

        obj_index ++;
        next_closest = GetNearestObject(OBJECT_TYPE_ALL, activator, obj_index);
        //obj_distance = GetDistanceBetweenLocations(activator_loc, GetLocation(next_closest));
    }

    DelayCommand(0.333333333, SE_AUX_PerformSuperHeartBeat(activator));
}

int SE_AUX_IsSuperHeartBeat(object target, int event_num)
{
    int producer_index;
    int consumer_index;

    object activator;

    string activator_string;

    int buffer_size = 50;

    // If the event number is not that of the super heart beat...
    if(event_num != 42)
    {
        return FALSE;
    }

    // Get the consumer index.
    consumer_index = GetLocalInt(target, "SE_AUX_SHB_CONSUMER_INDEX");
    // Get the producer index.
    producer_index = GetLocalInt(target, "SE_AUX_SHB_PRODUCER_INDEX");

    // If there are no objects currently in the buffer...
    if(consumer_index == producer_index)
    {
        // Just return FALSE
        return FALSE;
    }

    activator_string = "SE_AUX_SHB_ACTIVATOR_" + IntToString(consumer_index);

    // Get the next consumer.
    activator = GetLocalObject(target, activator_string);
    DeleteLocalObject(target, activator_string);

    // Increment to the next consumer index
    consumer_index ++;
    // Account for wrap around
    if(consumer_index == buffer_size)
    {
        consumer_index = 0;
    }
    // Set the new consumer index
    SetLocalInt(target, "SE_AUX_SHB_CONSUMER_INDEX", consumer_index);

    int return_status = FALSE;

    if(GetIsObjectValid(activator))
    {
        // Sets the current activator. (This applies to the script that this function
        // is called from.
        SetLocalObject(target, "SE_AUX_SHB_ACTIVATOR", activator);

        return_status = TRUE;
    }

    return return_status;
}

/**
 * Return 1 with probability numerator / denominator.
 * Results undefined if denominator <= 0.
 */
int SE_AUX_RandomGroupChance( int numerator, int denominator ) {
  return (Random(denominator) < numerator);
}

/**
 * Use with ActionDoCommand to queue up a Special Conversation. This has to
 * exist so that the local variable set on the owner isn't overwritten by
 * multiple queued one-liner actions.
 * @param topickey The string key to set on the owner that must be matched
 * against the argument in the <matchvar> tag in the Comment field of the header
 * node in the topic group in the dialog file.
 */
void SE_AUX_SpeakSpecialOneLiner( object speaker, string topickey,
                                  string topic, string dlgfile ) {
  SetLocalString(OBJECT_SELF, topickey, topic);
  AssignCommand(speaker, SpeakOneLinerConversation(dlgfile));
}



/**
 * Get the nearest creature
 */

object SE_AUX_GetNearestCreature() {

  object creature = GetNearestCreature(
                      CREATURE_TYPE_PLAYER_CHAR,
                      PLAYER_CHAR_NOT_PC);

  return creature;

}

/**
 * Get a random creature
 */

object SE_AUX_GetRandomCreature() {

  int i = 1;
  object creature = GetNearestCreature(
                      CREATURE_TYPE_PLAYER_CHAR,
                      PLAYER_CHAR_NOT_PC);

  while(creature != OBJECT_INVALID) {
    i++;
    creature = GetNearestCreature(
                 CREATURE_TYPE_PLAYER_CHAR,
                 PLAYER_CHAR_NOT_PC,
                 OBJECT_SELF,
                 i);
  }

  int rand = Random(i-1) + 1;

  creature = GetNearestCreature(
               CREATURE_TYPE_PLAYER_CHAR,
               PLAYER_CHAR_NOT_PC,
               OBJECT_SELF,
               rand);

  return creature;

}

/**
 * Get the nearest PC
 */

object SE_AUX_GetNearestPC() {

  /*object creature = GetNearestCreature(
                      CREATURE_TYPE_PLAYER_CHAR,
                      PLAYER_CHAR_IS_PC);

  return creature;*/

  return GetFirstPC();

}

/**
 * Get a random PC
 */

object SE_AUX_GetRandomPC() {

  /*int i = 1;
  object creature = GetNearestCreature(
                      CREATURE_TYPE_PLAYER_CHAR,
                      PLAYER_CHAR_IS_PC);

  while(creature != OBJECT_INVALID) {
    i++;
    creature = GetNearestCreature(
                 CREATURE_TYPE_PLAYER_CHAR,
                 PLAYER_CHAR_IS_PC,
                 OBJECT_SELF,
                 i);
  }

  int rand = Random(i-1) + 1;

  creature = GetNearestCreature(
               CREATURE_TYPE_PLAYER_CHAR,
               PLAYER_CHAR_IS_PC,
               OBJECT_SELF,
               rand);

  return creature;*/

  return GetFirstPC();
}

/* // obsolete
void SE_AUX_PerformRangeEvent(object actor){
   // Signal the range event.
   SignalEvent(actor, EventUserDefined(3700));
   DelayCommand(0.333333333, SE_AUX_PerformRangeEvent(actor));
}

void SE_AUX_SetRangeEvent(object actor)
{
   DelayCommand(0.0, SE_AUX_PerformRangeEvent(actor));
}

void SE_AUX_PerformTimerEvent(object actor){
   // Signal the timer event.
   SignalEvent(actor, EventUserDefined(3600));
   DelayCommand(0.333333333, SE_AUX_PerformTimerEvent(actor));
}

void SE_AUX_SetTimerEvent(object actor)
{
   DelayCommand(0.0, SE_AUX_PerformTimerEvent(actor));
}*/


//This method is the core of the timer used in stories. It periodicly
//creates interval events and only once creates an expiration event
//for each timer. This design relies using user defined events use
//numbers starting at 1002. Each timer uses 2 user defined event numbers.
//Realisticly the event numbers higher than 1100 should be free for
//other uses.
void SE_AUX_RunTimer(string timer_name, int interval, int countdown_threshold)
{
    object oModule = GetModule();

    //Check whether the timer has aborted.
    //If it has then do not generate any events, do any calculations,
    //or continue the recursive method calls. The timer has
    //forever stopped.
    if(GetLocalInt(oModule, timer_name + "IsActive") != 1)
    {
        //Calculate the remaining seconds before the expiration event.
        //Check whether the timer has expired.
        int iCurrentSecond = GetTimeSecond();
         int iCurrentMinute = GetTimeMinute();
        int iCurrentHour = GetTimeHour();
        int iCurrentDay = GetCalendarDay();
        int iCurrentMonth = GetCalendarMonth();
        int iCurrentYear = GetCalendarYear();

        int iExpirationYear = GetLocalInt(oModule, timer_name +
            "ExpirationYear");
        int iExpirationMonth = GetLocalInt(oModule, timer_name +
            "ExpirationMonth");
        int iExpirationDay = GetLocalInt(oModule, timer_name +
            "ExpirationDay");
        int iExpirationHour = GetLocalInt(oModule, timer_name +
            "ExpirationHour");
        int iExpirationMinute = GetLocalInt(oModule, timer_name +
            "ExpirationMinute");
        int iExpirationSecond = GetLocalInt(oModule, timer_name +
            "ExpirationSecond");

        //Calculate the remaining time in seconds and store it. This time is
        //not used in this event but could be used in another encounter, for
        //example one that displays the remaining time above the PC in periodic
        //fashion.
        int iRemainingYears = iExpirationYear - iCurrentYear;
        int iRemainingMonths = iExpirationMonth - iCurrentMonth;
        int iRemainingDays = iExpirationDay - iCurrentDay;
        int iRemainingHours = iExpirationHour - iCurrentHour;
        int iRemainingMinutes = iExpirationMinute - iCurrentMinute;
        int iRemainingSeconds = iExpirationSecond - iCurrentSecond +
        iRemainingMinutes*60 + iRemainingHours*60*60 +
        iRemainingDays*60*60*24 + iRemainingMonths*60*60*24*28 +
        iRemainingYears*60*60*24*28*12;
        SetLocalInt(oModule, timer_name + "RemainingSeconds",
            iRemainingSeconds);

        //Calculate the normal remaining seconds before the next interval event.
        int iNextIntervalSeconds = interval;

        //Calculate the real remaining seconds before the next interval
        //event taking into account the final 10 seconds are always counted
        //down second by second regardless of the stated interval.
        if(iRemainingSeconds <= countdown_threshold)
        {
            iNextIntervalSeconds = 1;
        }
        else if(iRemainingSeconds <= countdown_threshold + interval)
        {
            iNextIntervalSeconds = iRemainingSeconds - countdown_threshold;
        }

        //Since the remaining time could be so huge as to exceed the maximum
        //size of an integer it is safer to check whether the time has
        //elapsed unit by unit.
        int iTimeExpired = 0;

        if(iCurrentYear > iExpirationYear) {
            iTimeExpired = 1;
        }
        else if (iCurrentYear == iExpirationYear) {
            if(iCurrentMonth > iExpirationMonth) {
                  iTimeExpired = 1;
            }
            else if(iCurrentMonth == iExpirationMonth) {
                  if(iCurrentDay > iExpirationDay) {
                    iTimeExpired = 1;
                  }
                  else if(iCurrentDay == iExpirationDay) {
                    if(iCurrentHour > iExpirationHour) {
                          iTimeExpired = 1;
                    }
                       else if(iCurrentHour == iExpirationHour) {
                          if(iCurrentMinute > iExpirationMinute) {
                            iTimeExpired = 1;
                          }
                          else if(iCurrentMinute == iExpirationMinute) {
                            if(iCurrentSecond >= iExpirationSecond) {
                                  iTimeExpired = 1;
                            }
                          }
                    }
                  }
            }
        }

        //Calculate the user defined event number for this timer.
        int iTimerNumber = GetLocalInt(oModule, timer_name + "TimerNumber");
        int iEventNumber;

        //If there is time remaining ...
        if(iTimeExpired == 0)
        {
            //Fire an interval event and call this method recusively,
            //delaying until the next interval event.
            iEventNumber = iTimerNumber * 2 + 1000;
            SignalEvent(oModule, EventUserDefined(iEventNumber));

            //Recursively call the method.
            DelayCommand(IntToFloat(iNextIntervalSeconds),
                SE_AUX_RunTimer(timer_name, interval, countdown_threshold));
        }
        else
        {
            //Otherwise fire an expiration event.
            iEventNumber = iTimerNumber * 2 + 1001;
            SignalEvent(oModule, EventUserDefined(iEventNumber));
        }
    }
}

// This method is used to signal the start cue of a role in a performance
// The start cue is executed once and only once when a role is switched
void SCEZ_Behaviour_SignalStartCue(object actor) {
  // signal the start cue
  SignalEvent(actor, EventUserDefined(31416));
}

