definition(
	name: "Asker",
	namespace: "Tek",
	author: "me",
	description: "Turns on and off a collection of lights from Alexa.",
	category: "Convenience",
    iconUrl: "https://raw.githubusercontent.com/n8xd/AskHome/master/askhome108.png",
    iconX2Url: "https://raw.githubusercontent.com/n8xd/AskHome/master/askhome512.png"
)

preferences {
	page(name: "connectDevPage")
}

/////////////////////////////////////////////////////////////////////////////////////////////////////////

// Use inputs to attach smartthings devices to this app
def connectDevPage() {
   dynamicPage(name: "connectDevPage", title:"Connect Devices", input: true, uninstall: true ) {
      section(title: "Select Devices") {
        input "brlight", "capability.switch", title: "Select the Bedroom Light", required: true, multiple:true
      }
      if (!state.tok) { try { state.tok = createAccessToken()} catch (error) {state.tok = null }}
      section(title: "Show the OAUTH ID/Token Pair") {
        paragraph "   var STappID = '${app.id}';\n   var STtoken = '${state.tok}';\n"
      }
      section([mobileOnly:true]) {
		label title: "Assign a name", required: false
      }
   }
}

/////////////////////////////////////////////////////////////////////////////////////////////////////////
mappings { path("/:noun/:operator/:inquiz"){ action: [GET: "centralCommand"] } }
/////////////////////////////////////////////////////////////////////////////////////////////////////////

def centralCommand() {
        log.debug params

		def noun = params.noun
        def op  = params.operator   
        def inq = params.inquiz    
        
        log.debug "Central Command  ${noun} ${op} ${inq}"
        
        state.talk2me = ""    
       
        if (op == "none") { op = "status" }                      //if there is no op, status request
        if (["done","finished"].contains(op)) { op = "status" }  //with or without inquisitor these are status
        if (inq != "none") {                                     //with an inquisitor these are status
            if (["on","off","open","close"].contains(op)) { op = "status" }
        }

        switch (noun) {
            case "light"       :  switch(op) {       // simple on and off
                                            case "on"        :
                                            	log.debug "On"
                                                switchResponse(brlight, noun,op)
                                                break
                                            case "off"       : 
                                            	log.debug "Off"
                                                switchResponse(brlight, noun,op)
                                                break
                                            case "open"		 :
                                            	switchResponse(brlight, noun,op)
                                            	break
                                            case "close"	 :
                                            	switchResponse(brlight, noun,op)
                                            	break
                                            case "status"    :
                                            	switchResponse(brlight, noun,op)
                                            	break
                                            default          : defaultResponseUnkOp(noun,op)
                                            				   break
                                          }
                                          break
                                          
            case "none"                :  defaultResponseWhat()
                                          break
                                          
            default                    :  defaultResponseUnkNoun(noun,op)
            							  break
      }
      
      return ["talk2me" : state.talk2me]
}



//////////////////////////////////////////////////////////////////////////////////////////////////////////
//capability responses - DO and Report
//
//////////////////////////////////////////////////////////////////////////////////////////////////////////

def defaultResponseWhat()
{
      state.talk2me = state.talk2me + "Ask me about something, or to do something with something.   " 
}

// defaultResponse Unknown Device
def defaultResponseUnkNoun(noun, op)
{
      state.talk2me = state.talk2me + "I can't find any thing called, ${noun} in the smart app.  " 
}


// defaultResponse Unknown Operator for device
def defaultResponseUnkOp(noun, op)
{
      state.talk2me = state.talk2me + "I haven't been told how to do ${op} with ${noun} yet.  "
}

def switchResponse(handle, noun, op)
{
      def arg = handle.currentValue("switch")                        						//value before change 
          if ((op == "on") || (op == "open")) {
          	onMethod()
            arg = "turning " + op;
          }     	//switch flips slow in state, so tell them we did Op
          else if ((op == "off") || (op = "close")) { 
          	offMethod()
            arg = "turning " + op; 
          } 		// ...or it will report what it was, not what we want
          else if (op == "status") { 
          			//uses default arg value (the status)
          }         // dont report Op, report the real currentState
      state.talk2me = state.talk2me + "The ${noun} is ${op}.  "      						// talk2me : switch is on (or off) 
}

////////////////////////////////////////////////

def onMethod(){
	brlight.on()
}
def offMethod(){
	brlight.off()
}
