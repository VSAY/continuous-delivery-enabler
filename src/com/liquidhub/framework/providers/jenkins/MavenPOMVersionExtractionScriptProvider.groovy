package com.liquidhub.framework.providers.jenkins

import com.liquidhub.framework.ci.EmbeddedScriptProvider


class MavenPOMVersionExtractionScriptProvider implements EmbeddedScriptProvider{

	/**
	 * Provides a handle on a script snippet which is capable of extracting the project version from a maven pom
	 *
	 * @return the extraction script
	 */
 String getScript(Map bindings){

		'''
		|import jenkins.util.*
		|import hudson.model.StringParameterValue
        |import hudson.model.ParametersAction
		|
		|def currentThread = Thread.currentThread()
		|def currentBuild = currentThread?.executable
		|def workspace = currentBuild.getModuleRoot().absolutize().toString()
		|
		|def project = new XmlSlurper().parse(new File("$workspace/pom.xml"))
		|
		|def param = new hudson.model.StringParameterValue("PROJECT_VERSION", project.version.toString())
		|currentBuild.addAction(new ParametersAction(param))
		'''.stripMargin()
	}
}
