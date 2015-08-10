package com.liquidhub.framework.ci.model

class Email {

	def subject, body
	
	boolean sendToDevelopers, sendToRequestor, includeCulprits, sendToRecipientList
	
	String recipientList

}
