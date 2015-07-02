package com.liquidhub.framework.ci


/**
 * A factory method to generate job configurations. Implementations MUST bind to a specific CI tool and use the closure configuration
 * to generate the job configuration. Implementations MUST not throw an error if they recieve configuration elements that are not understood.
 * They MUST throw an error when they do not recieve an element which is mandatory for the underlying provider
 * 
 * 
 * @author Rahul Mishra, LiquidHub
 *
 */
interface JobFactory {
	
	def job(name, type, Closure jobConfig)
	
	def job(name, Closure jobConfig)
	
	def getImpl()
	
	

}
