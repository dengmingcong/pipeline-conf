#!/bin/bash/groovy
import groovy.json.JsonSlurper

def AGENT_LABEL = ""
def JENKINS_CONF_REPO_URL = "git@fangcun.vesync.com:testTeam/pipeline-conf.git"
def JENKINS_CONF_REPO_BRANCHE = "dev"
def JENKINS_CONF_DIR = "jobs"
def JENKINS_EXTRAS_DIR = "extras"
def JENKINS_CONF_CONTENT = ""
def ENV_MAP = [
	ci: [label: "fullTest", businessRepoUrl: "git@local-git.vesync.com:testTeam/Automation_CI.git"],
	testonline: [label: "Predeploy-smokeTest", businessRepoUrl: "git@fangcun.vesync.com:testTeam/Automation_testonline.git"], 
	predeploy: [lebel: "Predeploy-smokeTest", businessRepoUrl: "git@fangcun.vesync.com:testTeam/Automation_predeploy.git"]
]


node {
	stage("Get Jenkins Conf File") {
		echo "Try to get configuration file from repository ${JENKINS_CONF_REPO_URL}"
		try {
			sh "git archive --format=tar --remote=${JENKINS_CONF_REPO_URL} ${JENKINS_CONF_REPO_BRANCHE} ${JENKINS_CONF_DIR}/${env.JOB_NAME}.json | (tar xf - && mv ${JENKINS_CONF_DIR}/${JOB_NAME}.json . && rm -rf ${JENKINS_CONF_DIR})"
		} catch (Exception e) {
			echo "Configuration file ${JENKINS_CONF_DIR}/${env.JOB_NAME}.json does not exist in: repository ${JENKINS_CONF_REPO_URL}, branch ${JENKINS_CONF_REPO_BRANCHE}"
  	  		sh "exit 1"
		}
		echo "Got configuration file ${JENKINS_CONF_DIR}/${env.JOB_NAME}.json:"
		JENKINS_CONF_CONTENT = readFile(encoding: 'utf-8', file: "${env.JOB_NAME}.json")
		echo JENKINS_CONF_CONTENT
	}
	
	stage("Allocate Agent") {
	    def jsonSlurper = new JsonSlurper()
		def jenkinsConf = jsonSlurper.parseText(JENKINS_CONF_CONTENT)
		AGENT_LABEL = ENV_MAP[jenkinsConf.env]['label']
		echo "Stages next would be executed on agents with label: ${AGENT_LABEL}."
	}
}

node(AGENT_LABEL) {
	def BUSINESS_REPO_URL = ""
	def BUSINESS_REPO_BRANCH = ""
	
	def ANT_HOME = "/data/apache-ant-1.9.14"
	def JMETER_HOME = "/usr/local/jmeter40"
	def PROJECT_ROOT_DIR = env.WORKSPACE
	def CUSTOMIZE_BUILD_XML_PY = "bin/customize_build_xml.py"
	def SAMPLE_BUILD_XML = "resources/build_template.xml"
	def OUTPUT_BUILD_XML = "build.xml"
	def JMX = ""
	def PROPERTY_FILES = ""
	def TEST_NAME = env.JOB_NAME
	
	stage("Set Variables") {
	    def jsonSlurper = new JsonSlurper()
		def jenkinsConf = jsonSlurper.parseText(JENKINS_CONF_CONTENT)
		
		if (jenkinsConf.git.containsKey("url") && jenkinsConf.git.url) {
			BUSINESS_REPO_URL = jenkinsConf.git.url
		} else {
			BUSINESS_REPO_URL = ENV_MAP[jenkinsConf.env]['businessRepoUrl']
		}
		BUSINESS_REPO_BRANCH = jenkinsConf.git.branch
		echo "Git repository URL: ${BUSINESS_REPO_URL}"
		echo "Git repostiory BRANCH: ${BUSINESS_REPO_BRANCH}"
		
		JMX = jenkinsConf.jmeter.jmx
		if (jenkinsConf.jmeter.containsKey("properties") && jenkinsConf.jmeter.properties) {
			PROPERTY_FILES = "resources/common.properties," + jenkinsConf.jmeter.properties
		} else {
			PROPERTY_FILES = "resources/common.properties"
		}
		echo "JMeter's additional property files: ${PROPERTY_FILES}"
	}
	
	stage("Pre-Build") {
		echo "Getting codes (jmx, csv and so on) ..."
		checkout([$class: 'GitSCM', branches: [[name: "*/${BUSINESS_REPO_BRANCH}"]], doGenerateSubmoduleConfigurations: false, extensions: [], submoduleCfg: [], userRemoteConfigs: [[url: "${BUSINESS_REPO_URL}"]]])
		
		echo "Downloading Python scripts, sample build.xml, xslt, and other dependencies..."
		sh "git archive --format=tar --remote=${JENKINS_CONF_REPO_URL} ${JENKINS_CONF_REPO_BRANCHE} ${JENKINS_EXTRAS_DIR} | (tar xf - && cp -r ${JENKINS_EXTRAS_DIR}/* . && rm -rf ${JENKINS_EXTRAS_DIR})"
		
		echo "Converting simple controller to transaction controller..."
		sh "[[ -d ${ANT_HOME} ]] && [[ -d ${JMETER_HOME} ]]"
		sh "cd ${PROJECT_ROOT_DIR}; [[ -d reports ]] || mkdir reports; python3 bin/simple_controller_to_transaction_controller.py ${JMX} ${JMX}; cp resources/img/* reports"
		
		echo "Generating customized build.xml.."
		sh "python3 ${CUSTOMIZE_BUILD_XML_PY} ${SAMPLE_BUILD_XML} ${OUTPUT_BUILD_XML} ${PROJECT_ROOT_DIR} ${JMETER_HOME} ${JMX} ${TEST_NAME} -p ${ADDITIONAL_PROPERTIES}"
	}
	
	stage("Ant Build") {
		sh "${ANT_HOME}/bin/ant -file ${OUTPUT_BUILD_XML}"
	}
	
	stage("Publish HTML Reports") {
		publishHTML([allowMissing: true, alwaysLinkToLastBuild: true, keepAll: true, reportDir: '', reportFiles: "${TEST_NAME}_summary.html", reportName: "${TEST_NAME}_summary", reportTitles: "${TEST_NAME}_summary"])
		publishHTML([allowMissing: true, alwaysLinkToLastBuild: true, keepAll: true, reportDir: 'reports', reportFiles: "${TEST_NAME}_detail.html", reportName: "${TEST_NAME}_detail", reportTitles: "${TEST_NAME}_detail"])
	}
	
	stage("Mark Unstable") {
		sh "[[ `grep -c '<failure>true</failure>' ${TEST_PATH}/${TEST_NAME}.jtl` == 0 ]]"
	}
}