package com.liquidhub.framework.ci.model

import static com.liquidhub.framework.ci.view.ViewElementTypes.TEXT

import com.liquidhub.framework.ci.view.ViewElementTypes

/**
 * Collection of deployment job parameters
 * 
 * @author Rahul Mishra,LiquidHub
 *
 */
enum DeploymentJobParameters {

	GROUP_ID(new JobParameter(name: 'groupId',description: 'The group id of the artifact which needs to be deployed', elementType : ViewElementTypes.READ_ONLY_TEXT)),
	ARTIFACT_ID(new JobParameter(name: 'artifactId',description: 'The unique identifier of the artifact which needs to be deployed.',elementType: ViewElementTypes.READ_ONLY_TEXT)),
	PACKAGING(new JobParameter(name:'packaging',description: 'The packaging of the artifact to be deployed.',elementType: ViewElementTypes.READ_ONLY_TEXT)),
	ARTIFACT_VERSION(new JobParameter(name: 'artifactVersion',description: 'The version of the artifact which needs to be deployed',elementType: ViewElementTypes.SINGLE_SELECT_CHOICES)),
	TARGET_CLUSTER_NAME(new JobParameter(name: 'targetClusterName', description: 'The Cluster/JVM Name for which the deployment is targeted.You do not have to change this value',elementType: ViewElementTypes.TEXT)),
	TARGET_CELL_NAME(new JobParameter(name:'targetCellName',description: 'The cell for which the deployment is targeted.You do not have to change this value', elementType: ViewElementTypes.TEXT)),
	APP_CONTEXT_ROOT(new JobParameter(name:'applicationContextRoot',description: 'The Web Application context root', elementType: ViewElementTypes.TEXT)),
	REPLACEMENT_VERSION(new JobParameter(name:'replacementVersion',description: 'Sepcify the artifact version which this version should replace(if any)', elementType: ViewElementTypes.TEXT))
	

	public DeploymentJobParameters(parameter){
		this.properties = parameter.properties
	}

	def properties
	
	
	public static void main(String[] args){
		println GROUP_ID.properties
	}
}
