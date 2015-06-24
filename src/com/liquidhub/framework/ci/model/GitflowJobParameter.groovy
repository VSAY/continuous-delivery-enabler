package com.liquidhub.framework.ci.model

import com.liquidhub.framework.ci.view.ViewElementTypes


class GitflowJobParameter{

	GitflowJobParameterNames name

	ViewElementTypes viewType

	String description

	def defaultValue=''
	
	ViewElementTypes elementType

	ParameterListingScript valueListingScript

	ParameterListingScript labelListingScript

	boolean editable=true


	def getDescription(){
		this?.description ?: this.name.description
	}

	public def getName() {
		return name.parameterName;
	}
}



