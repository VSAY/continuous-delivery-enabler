package com.liquidhub.framework.ci

import groovy.text.SimpleTemplateEngine
import groovy.text.TemplateEngine

import com.liquidhub.framework.ci.model.JobGenerationContext
import com.liquidhub.framework.config.JobGenerationWorkspaceUtils;

/**
 * A build environment aware template engine. Wraps around groovy's template engine and can perform variable substitution from the build environment 
 * parameters automatically
 * 
 * @author Rahul Mishra,LiquidHub
 *
 */
class BuildEnvironmentAwareTemplateEngine{

	private JobGenerationWorkspaceUtils workspaceUtils

	private TemplateEngine  engine = new SimpleTemplateEngine()

	private def buildEnvVars

	BuildEnvironmentAwareTemplateEngine(JobGenerationContext context){
		this.workspaceUtils = context.workspaceUtils
		this.buildEnvVars = context.configuration.buildEnvProperties
	}


	/**
	 * Load the template content from the specified file path
	 * 
	 * @param templatePath   The path from which the file has to be loaded, the path provided must be relative to the job generation workspace     
	 * @param templateParameters The variable parameters which need to be substituted in the template
	 * 
	 * @return the templated content
	 */
	def withContentFromTemplate(templatePath, templateParameters){

		def templateContent = withTemplate(templatePath){templateContent ->

			if(!templateContent){
				throw new IllegalArgumentException('Failed to load template content. No template was found at the specified path '+templatePath)
			}

			def params = [:] << buildEnvVars << templateParameters  //Always override provided arguments with generic environment variables 
			engine.createTemplate(templateContent).make(params).toString()
		}
	}



	//TODO See why Memoization doesn't work
	protected def withTemplate(templatePath, templateCompiler){
		templateCompiler(workspaceUtils.fileReader(templatePath))
	}
}