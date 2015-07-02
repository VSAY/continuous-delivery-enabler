package com.liquidhub.framework.providers.jenkins

import static com.liquidhub.framework.ci.view.ViewElementTypes.BOOLEAN_CHOICE
import static com.liquidhub.framework.ci.view.ViewElementTypes.READ_ONLY_BOOLEAN_CHOICE
import static com.liquidhub.framework.ci.view.ViewElementTypes.READ_ONLY_TEXT
import static com.liquidhub.framework.ci.view.ViewElementTypes.SINGLE_SELECT_CHOICES
import static com.liquidhub.framework.ci.view.ViewElementTypes.TEXT
import static java.util.UUID.randomUUID

import com.liquidhub.framework.ci.logger.Logger
import com.liquidhub.framework.ci.model.JobParameter
import com.liquidhub.framework.ci.model.ParameterListingScript
import com.liquidhub.framework.ci.view.JobViewSupport
import jenkins.model.Jenkins //TODO This is an ugly jenkins dependency, figure out how to remove it, may be use scripts?

class JenkinsJobViewSupport implements JobViewSupport{

	private static Logger logger

	public def findPreConfiguredRepositoryNamesInProjectView(projectName){


		//In the current jenkins instance if you find a view which matches the current project name, return that view. That is our project view
		def projectView = Jenkins.instance.views.findResult{it.name =~ /${projectName}.*/ ? it : null }

		//Let us find the existing repositories included in that project view we found, default to an empty array
		def repositoryViews = projectView && projectView.views ? projectView.views.findResults{
			/*
			 * We have various kinds of views in a project view. Typically, one view per repository listing its jobs.Some additional views per repository
			 * showing test results, build monitors, delivery pipelines, etc.
			 * All, we need to do is figure out the repositories.Once we can figure out the repositores, we can derive everything else since the views
			 *  associated with a repository are rules embedded into code.
			 *
			 *  By convention, we ALWAYS make repository listing of jobs as Sectioned views (look at createView API), so that's why we are interested in list views
			 *  We cannot do an instanceof test because that would require us to import and resolve a jenkins class which is undesirable
			 *
			 */
			'hudson.plugins.sectioned_view.SectionedView'.equals(it.class.name) ? it.name : null
		} : []


		repositoryViews as Set
	}

	def defineParameter(parameter){

		def name = parameter.name
		def description = parameter?.description
	
		def parameterClosure = {}

		switch(parameter.elementType){

			case SINGLE_SELECT_CHOICES:
				parameterClosure = createChoiceOptionsView(name, description,parameter.valueListingScript, parameter.labelListingScript)
				break

			case READ_ONLY_TEXT:
				def valueListingScript = parameter.valueListingScript?: new ParameterListingScript(text:parameter.defaultValue)
				parameterClosure = createSimpleTextBox(name, description,valueListingScript, false) //false represents not editable
				break

			case BOOLEAN_CHOICE:
				parameterClosure = createStaticBooleanChoice(name, description, parameter?.defaultValue)
				break

			case READ_ONLY_BOOLEAN_CHOICE:
				def valueListingScript = new ParameterListingScript(text:parameter.defaultValue)
				parameterClosure = createDynamicBooleanChoice(name, description, valueListingScript, false)//false represents not editable
				break

			case TEXT:
				def valueListingScript = parameter.valueListingScript?: new ParameterListingScript(text:parameter.defaultValue)
				parameterClosure =  createSimpleTextBox(name, description, valueListingScript, true)//true represents editable
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
