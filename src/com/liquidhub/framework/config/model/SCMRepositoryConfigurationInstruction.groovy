package com.liquidhub.framework.config.model

import groovy.transform.ToString

@ToString(includeNames=true)
/**
 * Represents a singular independent instruction set meant to apply against a SCM Repository. The data in the instruction set is leveraged to make a REST API invocation.
 * 
 * @author Rahul Mishra,LiquidHub
 *
 */
class SCMRepositoryConfigurationInstruction {

	def apiName, httpMethod, uri, payload,queryParams
	
	boolean autoInvoke 
}
