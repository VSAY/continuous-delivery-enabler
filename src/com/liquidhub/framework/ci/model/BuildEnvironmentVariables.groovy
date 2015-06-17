package com.liquidhub.framework.ci.model

/**
 * Helps bind tool specific build parameters to logical concepts thus providing an easier upgrade/transition path and abstracting client code
 * from tool specific nuances
 * 
 * @author Rahul Mishra,LiquidHub
 *
 */
enum BuildEnvironmentVariables {

	BUILD_STATUS('$BUILD_STATUS'),
	PROJECT_NAME('$PROJECT_NAME'),
	BUILD_NUMBER( '$BUILD_NUMBER'),


	public def paramValue

	BuildEnvironmentVariables(paramValue){
		this.paramValue = paramValue
	}
}
