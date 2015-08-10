package com.liquidhub.framework.ci.model

import groovy.transform.Immutable
import groovy.transform.ToString

@ToString(includeNames=true)
class EmailNotificationContext {
	
	String recipientList, subjectTemplate,contentTemplate
	
	def emailTriggers = [:]
		
	public void addEmailForTrigger(String triggerName, Email email){		
		emailTriggers[triggerName] = email		
	}
		

}
