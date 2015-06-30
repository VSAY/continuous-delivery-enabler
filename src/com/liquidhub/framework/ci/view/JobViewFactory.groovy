package com.liquidhub.framework.ci.view

import groovy.lang.Closure;

interface JobViewFactory {

	def view(name, type, Closure viewConfig)
}
