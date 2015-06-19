package com.liquidhub.framework.config.model

import groovy.transform.ToString

import com.liquidhub.framework.model.SCM
import com.liquidhub.framework.model.Technology
import com.liquidhub.framework.model.Tool

@ToString(includeNames=true)
class BuildConfig {

	Tool maven
	Technology languageRuntime
	SCM scm
}
