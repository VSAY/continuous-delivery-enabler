package com.liquidhub.framework.ci.model

import groovy.transform.Immutable
import groovy.transform.ToString

@ToString(includeNames=true)
class Email {
	
	String sendTo, escalateTo, subject,body
	
	

}
