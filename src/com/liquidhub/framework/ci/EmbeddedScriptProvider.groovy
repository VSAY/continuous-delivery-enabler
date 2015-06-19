package com.liquidhub.framework.ci

interface EmbeddedScriptProvider {
	
	String getScript(Map bindings)

}
