package com.liquidhub.framework.ci.model

import static com.liquidhub.framework.ci.view.ViewElementTypes.TEXT

import com.liquidhub.framework.ci.view.ViewElementTypes

/**
 * Collection of deployment job parameters
 * 
 * @author Rahul Mishra,LiquidHub
 *
 */
enum WebsphereDeploymentJobParameters {

	GROUP_ID(new JobParameter(name: 'groupId',description: 'The group id of the artifact which needs to be deployed', elementType : ViewElementTypes.READ_ONLY_TEXT)),
	ARTIFACT_ID(new JobParameter(name: 'artifactId',description: 'The unique identifier of the artifact which needs to be deployed.',elementType: ViewElementTypes.READ_ONLY_TEXT)),
	PACKAGING(new JobParameter(name:'packaging',description: 'The packaging of the artifact to be deployed.',elementType: ViewElementTypes.READ_ONLY_TEXT)),
	ARTIFACT_VERSION(new JobParameter(name: 'version',description: 'The version of the artifact which needs to be deployed. If a version you have been requested to deploy does not show here, please contact jenkinssysadmin@ibx.com',elementType: ViewElementTypes.SINGLE_SELECT_CHOICES)),
	DEPLOYMENT_MANAGER(new JobParameter(name: 'deploymentManager',description: 'The Websphere deployment manager instance',elementType: ViewElementTypes.READ_ONLY_TEXT)),
	TARGET_JVM_NAME(new JobParameter(name: 'targetJVMName', description: 'The Cluster/JVM Name for which the deployment is targeted. The name for the regular stack is shown. For secondary stacks, please edit the value accordingly')),
	TARGET_CELL_NAME(new JobParameter(name:'cellName',description: 'The cell for which the deployment is targeted.You do not have to change this value', elementType: ViewElementTypes.READ_ONLY_TEXT)),
	APP_CONTEXT_ROOT(new JobParameter(name:'ctxroot',description: 'The Web Application context root', elementType: ViewElementTypes.TEXT)),
	RESTART(new JobParameter(name: 'restart', description:'Select this flag if you want the server to be restarted after deployment', elementType:ViewElementTypes.BOOLEAN_CHOICE)),
	REPLACEMENT_VERSION(new JobParameter(name:'replacementVersion',description: 'Sepcify the artifact version which this version should replace(if any)', elementType: ViewElementTypes.TEXT)),
	MULTIPLE(new JobParameter(name:'multiple',description: 'Select this flag to support multiple deployments on the same JVM. If additional WAR files are deployed on this JVM and you intend to retain those deployments, select this flag', elementType: ViewElementTypes.BOOLEAN_CHOICE))
	

	public WebsphereDeploymentJobParameters(parameter){
		this.properties = parameter.properties
	}

	def properties
	
	
	public static void main(String[] args){
		println GROUP_ID.properties
	}
}
