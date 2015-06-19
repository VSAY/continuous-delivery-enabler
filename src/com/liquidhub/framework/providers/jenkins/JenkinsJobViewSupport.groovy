package com.liquidhub.framework.providers.jenkins

import static java.util.UUID.randomUUID

import com.liquidhub.framework.ci.job.JobViewSupport
import com.liquidhub.framework.ci.model.GeneratedJobParameters


class JenkinsJobViewSupport implements JobViewSupport{

	@Override
	public def createChoiceOptionsView(GeneratedJobParameters parameter, parameterDescription, choicesGroovyScriptText, choicesDescriptionGroovyScriptText, bindings) {

		def param = createExtendedParameter(parameter.parameterName, parameterDescription, choicesGroovyScriptText, choicesDescriptionGroovyScriptText, bindings, 'PT_RADIO')

		return { 'com.cwctravel.hudson.plugins.extended__choice__parameter.ExtendedChoiceParameterDefinition'(plugin:'extended-choice-parameter@0.36', param) }
	}

	@Override
	public def createSimpleTextBox(GeneratedJobParameters parameter, parameterDescription, scriptText,readOnly) {

		def param = createDynamicParameter(parameter.parameterName, parameterDescription, scriptText, readOnly)

		return {
			'com.seitenbau.jenkins.plugins.dynamicparameter.StringParameterDefinition'(plugin:'dynamicparameter@0.2.0',param)
		}
	}

	@Override
	public Object createSimpleBooleanChoice(GeneratedJobParameters parameter, parameterDescription, boolean checkedByDefault) {

		return {

			'hudson.model.BooleanParameterDefinition'{
				delegate.createNode('name', parameter.parameterName)
				description(parameterDescription)
				defaultValue(checkedByDefault)
			}
		}
	}

	@Override
	public Object createSimpleCheckBox(GeneratedJobParameters parameter, parameterDescription, scriptText,readOnly) {

		def param = createDynamicParameter(parameter.parameterName, parameterDescription, scriptText,readOnly)

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
