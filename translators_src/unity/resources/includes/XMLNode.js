class XMLNode extends Boo.Lang.Hash{
	function GetNodeList(path:String){
		return GetObject(path) as XMLNodeList;
	}
	
	function GetNode(path:String){
		return GetObject(path) as XMLNode;
	}
	
	function GetValue(path:String){
		return GetObject(path) as String;
	}
	
	private function GetObject(path:String){
		var bits:String[]=path.Split(">"[0]);
		var currentNode:XMLNode=this;
		var currentNodeList:XMLNodeList;
		var listMode:boolean=false;
		var ob:Object;
		for(var i:int=0;i<bits.length;i++){
			 if(listMode){
				ob=currentNode=currentNodeList[parseInt(bits[i])];
				listMode=false;
			 }else{
				ob=currentNode[bits[i]];
				if(ob instanceof Array){
					currentNodeList=ob as Array;
					listMode=true;
				}else{
					// reached a leaf node/attribute
					if(i!=(bits.length-1)){
						// unexpected leaf node
						var actualPath:String="";
						for(var j:int;j<=i;j++){
							actualPath=actualPath+">"+bits[j];
						}
						Debug.Log("xml path search truncated. Wanted: "+path+" got: "+actualPath);
					}
					return ob;
				}
			 }
		}
		if(listMode) return currentNodeList;
		else return currentNode;
	}
}