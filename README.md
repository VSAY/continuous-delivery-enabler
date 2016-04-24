Based on Job DSL with custom extensions.

**Starting Point**: continuous-delivery-enabler/dsl/jenkins_job_generation_dsl.groovy

The above file path is configured in Jenkins. 

**Base configuration file**: continuous-delivery-enabler/resources/core/default-project-settings.yml

**Major Abstractions**:
<ol>
<li>Configuration Manager: Allows us to load configuration files found in the Jenkins Job Workspace. The current implementation reads YAML files. 

<li>JobSectionConfigurer: A Jenkins job configuration has logically distinct sections(for the most part), each implementation of the configurer must provide an XML snippet for such a section. 

<li>StashConfigurationManager: Manages and abstracts the stash repository configuration which is controlled by Jenkins when the project is onboarded

</ol>

More documentation to follow


