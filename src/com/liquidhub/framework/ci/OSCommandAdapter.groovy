package com.liquidhub.framework.ci

interface OSCommandAdapter {

	def adapt(cmd, parameters)
	
	def adapt(cmd)

}
