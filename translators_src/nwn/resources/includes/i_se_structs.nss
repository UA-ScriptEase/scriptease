string SIZE_STRING = "Size";
string HEAD_STRING = "head";
string TAIL_STRING = "tail";
string TOP_INDEX_LABEL  = "top";

void debug (string msg);

//\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\
//////////// STRING ARRAY ///////////////////
//\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\
// The name says it all
int SCEZ_Struct_ArrayGetSize(object owner, string arrayName) {
  return GetLocalInt( owner, arrayName+SIZE_STRING );
}

// Completely removes the array from the object
void SCEZ_Struct_ArrayClear(object owner, string arrayName) {
  int n = SCEZ_Struct_ArrayGetSize( owner, arrayName ); 
  int i = 0;
  while(i < n) {
    DeleteLocalString(owner, arrayName+IntToString(i));
    i = i + 1;
  }
  //Remove array size var
  DeleteLocalInt(owner, arrayName+SIZE_STRING);
}

// Writes 'element' into the specified array at 'index'. Returns FALSE on failure.
int SCEZ_Struct_ArraySetElementAtIndex (object owner, string arrayName, int index, string element) {
  int arraySize = SCEZ_Struct_ArrayGetSize( owner, arrayName );
  if( index >= arraySize ) {
    // index out of bounds!!
    return FALSE;
  }
  
  SetLocalString( owner, arrayName+IntToString(index), element );
  return TRUE;
}

string SCEZ_Struct_ArrayGetElementAtIndex (object owner, string arrayName, int index) {
  return GetLocalString( owner, arrayName+IntToString( index ) );
}

string SCEZ_Struct_ArrayRemoveElementAtIndex (object owner, string arrayName, int index) {
  if(index == SCEZ_Struct_ArrayGetSize(owner, arrayName) - 1 ){
    //debug("Decreasing array size on "+arrayName+" from "+IntToString(GetLocalInt(owner, arrayName+SIZE_STRING))+" to one less");
    SetLocalInt(owner, arrayName+SIZE_STRING, GetLocalInt(owner, arrayName+SIZE_STRING)-1 );
  }

  string removedString = GetLocalString( owner, arrayName+IntToString(index) );
  DeleteLocalString( owner, arrayName+IntToString(index) );
  return removedString;
}

// Locates the given element in the array with the given name. Returns the index of that element, or -1 if it is not found.
//   owner:     The owner of the array.
//   arrayName: The name of the array to search.
//   element:   The element to search for.
int SCEZ_Struct_ArrayFindElement(object owner, string arrayName, string element) {
  int i;
  for(i = 0; i < SCEZ_Struct_ArrayGetSize(owner, arrayName); i++) {
    if(SCEZ_Struct_ArrayGetElementAtIndex(owner, arrayName, i) == element) {
      return i;
    }
  }
  // No Such Element
  return -1;
}

string SCEZ_Struct_ArrayRemoveElement(object owner, string arrayName, string element) {
  int i = SCEZ_Struct_ArrayFindElement(owner, arrayName, element);
  
  if(i < 0) {
	return "";
  } else {
    return SCEZ_Struct_ArrayRemoveElementAtIndex(owner, arrayName, i);
  }
}

int SCEZ_Struct_ArrayAppendElement(object owner, string arrayName, string element) { 
  int arraySize = SCEZ_Struct_ArrayGetSize( owner, arrayName ); //GetLocalInt(OBJECT_SELF, "scez_can");
  SetLocalInt( owner, arrayName+SIZE_STRING, arraySize+1 );
  SCEZ_Struct_ArraySetElementAtIndex(owner, arrayName, arraySize, element);
  
//  debug("Array "+ arrayName +" now contains "+ SCEZ_Struct_ArrayGetElementAtIndex(owner, arrayName, arraySize) +" at index "+ IntToString(arraySize));
  
  return arraySize;
}

void SCEZ_Struct_ArrayInsertElement(object owner, string arrayName, string element, int index) {
  string leftNeighbour = "";
  int size = SCEZ_Struct_ArrayGetSize(owner, arrayName);
  int i = size;
  
  SetLocalInt( owner, arrayName+SIZE_STRING, size+1 );
  
  while(i > index){
    leftNeighbour = SCEZ_Struct_ArrayGetElementAtIndex(owner, arrayName, i-1);
    
 //   debug("moving '" + leftNeighbour + "' from index " + IntToString(i-1) + " to " +IntToString(i) );
  
    SCEZ_Struct_ArraySetElementAtIndex(owner, arrayName, i, leftNeighbour);
    
    i--;
  }
  
  SCEZ_Struct_ArraySetElementAtIndex(owner, arrayName, index, element);
}

string SCEZ_Struct_ArrayGetRandomElement(object owner, string arrayName) {
  int arraySize = GetLocalInt(owner, arrayName+SIZE_STRING);
  if (arraySize == 0) {
    return "";
  } else {
    int i = Random(arraySize);
    return GetLocalString(owner, arrayName+IntToString(i));
  }
}

// swaps the element at index A with element at index B in the given array
void SCEZ_Struct_ArraySwap(object actor, string arrayName, int a, int b){
  string temp;
  
  temp = SCEZ_Struct_ArrayGetElementAtIndex(actor, arrayName, a);
  
  SCEZ_Struct_ArraySetElementAtIndex(actor, arrayName, a, SCEZ_Struct_ArrayGetElementAtIndex(actor, arrayName, b));
  
  SCEZ_Struct_ArraySetElementAtIndex(actor, arrayName, b, temp);
}

void SCEZ_Struct_ArrayShuffle(object actor, string arrayName, int seed) {
  int j;
  
  for (j = 2; j < SCEZ_Struct_ArrayGetSize(actor, arrayName); j++) {
    SCEZ_Struct_ArraySwap(actor, arrayName, (seed % j), j);
    seed = seed / j;        // integer division cuts off the remainder
  }
}


//\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\
//////////// OBJECT ARRAY ///////////////////
//\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\

// Completely removes the array from the object
void SCEZ_Struct_ObjectArrayClear(object owner, string arrayName) {
  int n = SCEZ_Struct_ArrayGetSize( owner, arrayName ); //GetLocalInt(OBJECT_SELF, "scez_can");
  int i = 0;
  while(i < n) {
    DeleteLocalObject(owner, arrayName+IntToString(i));
    i = i + 1;
  }
  //Remove array size var
  DeleteLocalInt(owner, arrayName+SIZE_STRING);
}

// Writes 'element' into the specified array at 'index'. Returns FALSE on failure.
int SCEZ_Struct_ObjectArraySetElementAtIndex (object owner, string arrayName, int index, object element) {
  int arraySize = SCEZ_Struct_ArrayGetSize( owner, arrayName );
  if( index >= arraySize ) {
    // index out of bounds!!
    return FALSE;
  }
  
  SetLocalObject( owner, arrayName+IntToString(index), element );
  
  return TRUE;
}

object SCEZ_Struct_ObjectArrayGetElementAtIndex (object owner, string arrayName, int index) {
  return GetLocalObject( owner, arrayName+IntToString( index ) );
}

object SCEZ_Struct_ObjectArrayRemoveElementAtIndex (object owner, string arrayName, int index) {
  if(index == SCEZ_Struct_ArrayGetSize(owner, arrayName) - 1 ){
    SetLocalInt(owner, arrayName+SIZE_STRING, GetLocalInt(owner, arrayName+SIZE_STRING)-1 );
  }

  object removedObject = GetLocalObject( owner, arrayName+IntToString(index) );
  DeleteLocalObject( owner, arrayName+IntToString(index) );
  return removedObject;
}

int SCEZ_Struct_ObjectArrayAppendElement(object owner, string arrayName, object element) {
  int arraySize = SCEZ_Struct_ArrayGetSize( owner, arrayName ); //GetLocalInt(OBJECT_SELF, "scez_can");
  SetLocalInt( owner, arrayName+SIZE_STRING, arraySize+1 );
  SCEZ_Struct_ObjectArraySetElementAtIndex(owner, arrayName, arraySize, element);
  
  return arraySize;
}

void SCEZ_Struct_ObjectArrayInsertElement(object owner, string arrayName, object element, int index) {
  object temp;
  int size = SCEZ_Struct_ArrayGetSize(owner, arrayName);
  int i = size;
  
  SetLocalInt( owner, arrayName+SIZE_STRING, size+1 );
  
  while(i > index){
    SCEZ_Struct_ObjectArraySetElementAtIndex(owner, arrayName, i, SCEZ_Struct_ObjectArrayGetElementAtIndex(owner, arrayName, i-1) );
    
    i--;
  }
  
  SCEZ_Struct_ObjectArraySetElementAtIndex(owner, arrayName, index, element);
}

object SCEZ_Struct_ObjectArrayGetRandomElement(object owner, string arrayName) {
  int arraySize = GetLocalInt(owner, arrayName+SIZE_STRING);
  
  if (arraySize == 0) {
    return OBJECT_INVALID;
  } else {
    int i = Random(arraySize);
    return GetLocalObject(owner, arrayName+IntToString(i));
  }
}

// swaps the element at index A with element at index B in the given array
void SCEZ_Struct_ObjectArraySwap(object actor, string arrayName, int a, int b){
  object temp;
  
  temp = SCEZ_Struct_ObjectArrayGetElementAtIndex(actor, arrayName, a);
  
  SCEZ_Struct_ObjectArraySetElementAtIndex(actor, arrayName, a, SCEZ_Struct_ObjectArrayGetElementAtIndex(actor, arrayName, b));
  
  SCEZ_Struct_ObjectArraySetElementAtIndex(actor, arrayName, b, temp);
}


void SCEZ_Struct_ObjectArrayShuffle(object actor, string arrayName, int seed) {
  int j;
  
  for (j = 2; j < SCEZ_Struct_ArrayGetSize(actor, arrayName); j++) {
    SCEZ_Struct_ObjectArraySwap(actor, arrayName, (seed % j), j);
    seed = seed / j;        // integer division cuts off the remainder
  }
}


//\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\
////////////// QUEUE ////////////////////////
//\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\
int SCEZ_Struct_QueueIsEmpty(object owner, string queueName) {
//  debug("Checking bounds for: "+ queueName +" HEAD: "+ IntToString(GetLocalInt(owner, queueName+HEAD_STRING)) +" TAIL: "+ IntToString(GetLocalInt(owner, queueName+TAIL_STRING)));
  return GetLocalInt(owner, queueName+HEAD_STRING) >= GetLocalInt(owner, queueName+TAIL_STRING);
}

// Look but don't touch
string SCEZ_Struct_QueuePeek(object owner, string queueName) {
  int current = GetLocalInt(owner, queueName+HEAD_STRING);
//  debug("Peeking at HEAD: "+ IntToString(current));
  return GetLocalString(owner, queueName+IntToString(current));
}

// Pop the next element in the queue
string SCEZ_Struct_QueuePop(object owner, string queueName) {
  //debug("QueuePopping off of: "+ queueName);
  // dequeue current element
  int current = GetLocalInt(owner, queueName+HEAD_STRING);
  string next = GetLocalString( owner, queueName+IntToString(current) );
  DeleteLocalString(owner, queueName+IntToString(current));
  
  // Increment the head
  //debug("Pre-increment "+queueName+HEAD_STRING+": "+ IntToString(GetLocalInt(owner, queueName+HEAD_STRING) ) );
  SetLocalInt(owner, queueName+HEAD_STRING, ++current);
  //debug("Post-increment "+queueName+HEAD_STRING+": "+ IntToString(GetLocalInt(owner, queueName+HEAD_STRING) ) );
  return next;    
}

void SCEZ_Struct_QueueAdd(object actor, string queue, string element) {
  // queue element at tail
  int tail = GetLocalInt(actor, queue+TAIL_STRING);
  SetLocalString(actor, queue+IntToString(tail), element);
  tail = tail + 1;  
  SetLocalInt(actor, queue+TAIL_STRING, tail);
}

// Remove all variables associated with this queue
int SCEZ_Struct_QueueClear( object owner, string queueName ) {
  //int index=0;
  
//  debug("QueueClear... Deleting: "+ queueName);
  while( ! SCEZ_Struct_QueueIsEmpty( owner, queueName ) ){
    SCEZ_Struct_QueuePop( owner, queueName );
    //index++;
  }
  
  //debug("QueueClear... Deleting the head!!!! ZOMBIE!");
  DeleteLocalInt( owner, queueName+HEAD_STRING );
  DeleteLocalInt( owner, queueName+TAIL_STRING );
  
  return TRUE;
}

//\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\
////////////////// STACK ////////////////////
//\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\
// CAUTION: note that the size and indexing in this stack are the *same*! 
//    IE, the 7th stack element is stored at index 7, not the usual 6.

int SCEZ_Struct_StackIsEmpty(object owner, string stackName);
string SCEZ_Struct_StackPeek(object owner, string stackName);
int SCEZ_Struct_StackSize(object owner, string stackName);
string SCEZ_Struct_StackPop(object owner, string stackName);
int SCEZ_Struct_StackPush(object owner, string stackName, string newValue);
int SCEZ_Struct_StackClear (object owner, string stackName);


int SCEZ_Struct_StackIsEmpty(object owner, string stackName) {
  return (SCEZ_Struct_StackSize(owner, stackName) == 0);
}

string SCEZ_Struct_StackPeek(object owner, string stackName){
  return GetLocalString(owner, stackName+IntToString(GetLocalInt(owner, stackName+TOP_INDEX_LABEL )));
}

int SCEZ_Struct_StackSize(object owner, string stackName){
  return GetLocalInt( owner, stackName+TOP_INDEX_LABEL );
}

string SCEZ_Struct_StackPop(object owner, string stackName){
  int topIndex = GetLocalInt( owner, stackName+TOP_INDEX_LABEL );
  
  // Return an empty string if the stack is empty
  if( SCEZ_Struct_StackIsEmpty(owner, stackName) ) {
    return "";
  }
  
  // Do the pop
  string popped = GetLocalString( owner, stackName+IntToString(topIndex) );
  DeleteLocalString( owner, stackName+IntToString(topIndex) );
  
  // Decrement the topIndex pointer
  SetLocalInt( owner, stackName+TOP_INDEX_LABEL, --topIndex );
  
  return popped;
}

int SCEZ_Struct_StackPush(object owner, string stackName, string newValue) {
  // Don't put an empty value on the stack
  if( newValue == "" ) {
    return FALSE;
  }
  
  // Increment the topIndex pointer
  int newTopIndex = GetLocalInt( owner, stackName+TOP_INDEX_LABEL ) + 1;
  SetLocalInt( owner, stackName+TOP_INDEX_LABEL, newTopIndex );
  
  // Do the push
  SetLocalString( owner, stackName+IntToString(newTopIndex), newValue );
  
  return TRUE;
}

int SCEZ_Struct_StackClear (object owner, string stackName) {
  while( ! SCEZ_Struct_StackIsEmpty( owner, stackName ) ){
    //debug("StackClear... Deleting: "+ SCEZ_Struct_StackPeek(owner, stackName) );
    SCEZ_Struct_QueueClear( owner, SCEZ_Struct_StackPop( owner, stackName ) );
  }
  return TRUE;
}

void debug (string msg) {
  string actorName = GetName(OBJECT_SELF);
  //string topQueue = SCEZ_Struct_StackPeek(OBJECT_SELF, SCEZ_Behaviour_GetExecutionStack(OBJECT_SELF) );
  
  //if( actorName == "Server" || GetName( SCEZ_Behaviour_GetCollaboratorForQueue( OBJECT_SELF, topQueue ) ) == "Server" ) {
    //WriteTimestampedLogEntry(GetName(OBJECT_SELF) + ":: "+ msg);
    PrintString(GetName(OBJECT_SELF) + ":: "+ msg);
    SendMessageToPC(GetFirstPC(), actorName +":: "+ msg );
  //}
}