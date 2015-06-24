package com.liquidhub.framework.providers.jenkins

import static com.liquidhub.framework.ci.view.ViewElementTypes.BOOLEAN_CHOICE
import static com.liquidhub.framework.ci.view.ViewElementTypes.SINGLE_SELECT_CHOICES
import static com.liquidhub.framework.ci.view.ViewElementTypes.TEXT_FIELD
import static java.util.UUID.randomUUID

import com.liquidhub.framework.ci.logger.Logger
import com.liquidhub.framework.ci.model.GitflowJobParameter
import com.liquidhub.framework.ci.model.ParameterListingScript
import com.liquidhub.framework.ci.view.JobViewSupport

class JenkinsJobViewSupport implements JobViewSupport{
  
	private static Logger logger

	def defineParameter(GitflowJobParameter parameter){

		def name = parameter.name
		def description = parameter?.description
		boolean editable = parameter?.editable
		
		def parameterClosure = {}

		switch(parameter.elementType){

			case SINGLE_SELECT_CHOICES:
				parameterClosure = createChoiceOptionsView(name, description,parameter.valueListingScript, parameter.labelListingScript)
				break

			case BOOLEAN_CHOICE:
				parameterClosure = parameter?.valueListingScript||!editable ? createDynamicBooleanChoice(name, description, parameter.valueListingScript, editable) : createStaticBooleanChoice(name, description, parameter?.defaultValue)
				break

			case TEXT_FIELD:
				parameterClosure =  createSimpleTextBox(name, description, parameter.valueListingScript, editable)
				break
		}
		
		return parameterClosure
	}


	protected def createChoiceOptionsView(String name,String parameterDescription, ParameterListingScript valueListingScript, ParameterListingScript labelListingScript) {
    	def choicesGroovyScriptText = valueListingScript?.text
		def choicesDescriptionGroovyScriptText = labelListingScript?.text
		def bindings = valueListingScript.bindings

		def param = createExtendedParameter(name, parameterDescription, choicesGroovyScriptText, choicesDescriptionGroovyScriptText, bindings, 'PT_RADIO')

		return { 'com.cwctravel.hudson.plugins.extended__choice__parameter.ExtendedChoiceParameterDefinition'(plugin:'extended-choice-parameter@0.36', param) }
	}

	protected def createSimpleTextBox(String parameter,String parameterDescription, ParameterListingScript valueListingScript ,editable) {
		def scriptText = valueListingScript?.text
		boolean readOnly = !editable

		def param = createDynamicParameter(parameter, parameterDescription, scriptText, readOnly)

		return {
			'com.seitenbau.jenkins.plugins.dynamicparameter.StringParameterDefinition'(plugin:'dynamicparameter@0.2.0',param)
		}
	}

	protected Object createStaticBooleanChoice(String parameter, String parameterDescription, def checkedByDefault) {
		return {

			'hudson.model.BooleanParameterDefinition'{
				delegate.createNode('name', parameter)
				description(parameterDescription)
				defaultValue(checkedByDefault.toBoolean())
			}
		}
	}

	protected Object createDynamicBooleanChoice(String parameter, String parameterDescription,ParameterListingScript listingScript,editable) {

		def scriptText = listingScript?.text
		def readOnly = !editable

		def param = createDynamicParameter(parameter, parameterDescription, scriptText,readOnly)

		return {
			'com.seitenbau.jenkins.plugins.dynamicparameter.StringParameterDefinition'(plugin:'dynamicparameter@0.2.0', param)
		}
	}


	protected  def createExtendedParameter(parameterName, parameterDescription, choicesGroovyScriptText, choicesDescriptionGroovyScriptText, bindingProperties, buttonType){
		return {
			delegate.createNode('name', parameterName)
			description(parameterDescription)
			quoteValue(false)
			visibleItemCount 15
			type(buttonType)
			groovyScript(choicesGroovyScriptText)
			bindings(bindingProperties.collect{key,value -> key+'='+value}.join(' \n ')) //convert the bindings map to a newline separated string
			groovyClasspath()
			multiSelectDelimiter(',')
			if(choicesDescriptionGroovyScriptText){
				descriptionGroovyScript(choicesDescriptionGroovyScriptText)
				descriptionBindings()
				descriptionGroovyClasspath()
			}
		}
	}


	/**
	 * Creates a dynamic parameter based on the specification of the dynamic parameter plugin
	 *
	 * @param delegate
	 * @param parameterName
	 * @param descriptionText
	 * @param scriptText
	 * @return
	 */
	protected def createDynamicParameter(parameterName, descriptionText, scriptText, readOnly=true){
		return {
			delegate.createNode('name', parameterName)
			description(descriptionText)
			__uuid(UUID.randomUUID())
			__remote false
			__script scriptText
			readonlyInputField(readOnly)
			__localBaseDirectory(serialization:'custom') {
				'hudson.FilePath' {
					'default'{ remote('/dynamicParameterClasspath') }
					delegate.createNode('boolean',true)
				}
			}
			__remoteBaseDirectory('dynamic_parameter_classpath')
			__classPath()

		}
	}


}
