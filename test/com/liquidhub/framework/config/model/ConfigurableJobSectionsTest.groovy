package com.liquidhub.framework.config.model;

import static org.junit.Assert.*;

import org.junit.Test;

class ConfigurableJobSectionsTest {

	@Test
	public void test() {
		def sections = [scm: 'GENERIC_SCM_SECTION_CONFIGURER',
			trigger: 'GENERIC_SCM_TRIGGER_SECTION_CONFIGURER',
			email: 'EXTENDED_EMAIL_SECTION_CONFIGURER']
		ConfigurableJobSections jobSections = new ConfigurableJobSections(sections)
		jobSections.provider('scm')
	}
}
