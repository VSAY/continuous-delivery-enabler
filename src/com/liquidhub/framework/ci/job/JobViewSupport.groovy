package com.liquidhub.framework.ci.job

import com.liquidhub.framework.ci.model.GeneratedJobParameters

interface JobViewSupport {
	
	def createChoiceOptionsView(GeneratedJobParameters parameterName, parameterDescription, choicesGroovyScriptText, choicesDescriptionGroovyScriptText, bindings)
	
	def createSimpleTextBox(GeneratedJobParameters parameterName, parameterDescription, scriptText,readOnly)
	
	def createSimpleBooleanChoice(GeneratedJobParameters parameterName, parameterDescription, boolean checkedByDefault)
	
	def createSimpleCheckBox(GeneratedJobParameters parameterName, parameterDescription, scriptText,readOnly)
	
	

}
