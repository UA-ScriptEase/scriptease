/*
 * UnityScript Lightweight XML Parser
 * by Fraser McCormick (unityscripts@roguishness.com)
 * http://twitter.com/flimgoblin
 * http://www.roguishness.com/unity/
 *
 * You may use this script under the terms of either the MIT License 
 * or the Gnu Lesser General Public License (LGPL) Version 3. 
 * See:
 * http://www.roguishness.com/unity/lgpl-3.0-standalone.html
 * http://www.roguishness.com/unity/gpl-3.0-standalone.html
 * or
 * http://www.roguishness.com/unity/MIT-license.txt
 */
  
 
 

/* Usage:
 * parser=new XMLParser();
 * var node=parser.Parse("<example><value type=\"String\">Foobar</value><value type=\"Int\">3</value></example>");
 * 
 * Nodes are Boo.Lang.Hash values with text content in "_text" field, other attributes
 * in "@attribute" and any child nodes listed in an array of their nodename.
 * 
 * any XML meta tags <? .. ?> are ignored as are comments <!-- ... -->
 * any CDATA is bundled into the "_text" attribute of its containing node.
 *
 * e.g. the above XML is parsed to:
 * node={ "example": 
 *			[ 
 *			   { "_text":"", 
 *				  "value": [ { "_text":"Foobar", "@type":"String"}, {"_text":"3", "@type":"Int"}]
 *			   } 
 *			],
 *		  "_text":""
 *     }
 *		  
 */
class XMLParser{
	
	private var LT:char="<"[0];
	private var GT=">"[0];
	private var SPACE=" "[0];
	private var QUOTE="\""[0];
	private var SLASH="/"[0];
	private var QMARK="?"[0];
	private var EQUALS="="[0];
	private var EXCLAMATION="!"[0];
	private var DASH="-"[0];
	//private var SQL="["[0];
	private var SQR="]"[0];
	
	function Parse(content:String){

		var rootNode:XMLNode=new XMLNode();
		rootNode["_text"]="";

		var nodeContents:String="";
		
		var inElement:boolean=false;
		var collectNodeName:boolean=false;
		var collectAttributeName:boolean=false;
		var collectAttributeValue:boolean=false;
		var quoted:boolean=false;
		var attName:String="";
		var attValue:String="";
		var nodeName:String="";
		var textValue:String="";
		
		var inMetaTag:boolean=false;
		var inComment:boolean=false;
		var inDoctype:boolean=false;
		var inCDATA:boolean=false;
		
		var parents:XMLNodeList=new XMLNodeList();
		
		var currentNode:XMLNode=rootNode;
		for(var i=0;i<content.length;i++){
			
			var c:char=content[i];
			var cn:char;
			var cnn:char;
			var cp:char;
			if((i+1)<content.length) cn=content[i+1]; 
			if((i+2)<content.length) cnn=content[i+2]; 
			if(i>0)cp=content[i-1];
					
			if(inMetaTag){
				if(c==QMARK && cn==GT){
					inMetaTag=false;
					i++;
				}
				continue;
			}else{
				if(!quoted && c==LT && cn==QMARK){
					inMetaTag=true;
					continue;	
				}	
			}
			
			if(inDoctype){				
				if(cn==GT){
					inDoctype=false;
					i++;
				}
				continue;
			}else if(inComment){
				if(cp==DASH && c==DASH && cn==GT){
					inComment=false;
					i++;
				}
				continue;	
			}else{
				if(!quoted && c==LT && cn==EXCLAMATION){					
					if(content.length>i+9 && content.Substring(i,9)=="<![CDATA["){
						inCDATA=true;
						i+=8;
					}else if(content.length > i+9 && content.Substring(i,9)=="<!DOCTYPE"){
						inDoctype=true;						
						i+=8;					
					}else if(content.length > i+2 && content.Substring(i,4)=="<!--"){					
						inComment=true;
						i+=3;
					}
					continue;	
				}
			}
			
			if(inCDATA){
				if(c==SQR && cn==SQR && cnn==GT){
					inCDATA=false;
					i+=2;
					continue;
				}
				textValue+=c;
				continue;	
			}
			
			
			if(inElement){
				if(collectNodeName){
					if(c==SPACE){
						collectNodeName=false;
					}else if(c==GT){
						collectNodeName=false;
						inElement=false;
					}
					
			
		
					if(!collectNodeName && nodeName.length>0){
						if(nodeName[0]==SLASH){
							// close tag
							if(textValue.length>0){
								currentNode["_text"]+=textValue;
							}
					
							textValue="";
							nodeName="";
							currentNode=parents.Pop();
						}else{

							if(textValue.length>0){
								currentNode["_text"]+=textValue;
							}
							textValue="";	
							var newNode:XMLNode=new XMLNode();
							newNode["_text"]="";
							newNode["_name"]=nodeName;
							
							if(!currentNode[nodeName]){
								currentNode[nodeName]=new XMLNodeList();	
							}
							var a:Array=currentNode[nodeName];
							a.Push(newNode);	
							parents.Push(currentNode);
							currentNode=newNode;
							nodeName="";
						}
					}else{
						nodeName+=c;	
					}
				}else{
					
					if(!quoted && c==SLASH && cn==GT){
						inElement=false;
						collectAttributeName=false;
						collectAttributeValue=false;	
						if(attName){
							if(attValue){
								currentNode["@"+attName]=attValue;								
							}else{
								currentNode["@"+attName]=true;								
							}
						}
						
						i++;
						currentNode=parents.Pop();
						attName="";
						attValue="";		
					}
					else if(!quoted && c==GT){
						inElement=false;
						collectAttributeName=false;
						collectAttributeValue=false;	
						if(attName){
							currentNode["@"+attName]=attValue;							
						}
						
						attName="";
						attValue="";	
					}else{
						if(collectAttributeName){
							if(c==SPACE || c==EQUALS){
								collectAttributeName=false;	
								collectAttributeValue=true;
							}else{
								attName+=c;
							}
						}else if(collectAttributeValue){
							if(c==QUOTE){
								if(quoted){
									collectAttributeValue=false;
									currentNode["@"+attName]=attValue;								
									attValue="";
									attName="";
									quoted=false;
								}else{
									quoted=true;	
								}
							}else{
								if(quoted){
									attValue+=c;	
								}else{
									if(c==SPACE){
										collectAttributeValue=false;	
										currentNode["@"+attName]=attValue;								
										attValue="";
										attName="";
									}	
								}
							}
						}else if(c==SPACE){
						
						}else{
							collectAttributeName=true;							
							attName=""+c;
							attValue="";
							quoted=false;		
						}	
					}
				}
			}else{
				if(c==LT){
					inElement=true;
					collectNodeName=true;	
				}else{
					textValue+=c;	
				}	
				
			}
				
		}
	
		return rootNode;
	}

}