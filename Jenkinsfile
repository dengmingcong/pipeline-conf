#!/bin/bash/groovy
import groovy.json.JsonSlurper

properties([buildDiscarder(logRotator(artifactDaysToKeepStr: '', artifactNumToKeepStr: '', daysToKeepStr: '3', numToKeepStr: '5')), disableConcurrentBuilds()])

def ENV_MAP = [
	ci: [label: "slave", businessRepoUrl: "http://fangcun.vesync.com:8081/testTeam/Automation_testonline_CN.git"],
	testonline: [label: "slave", businessRepoUrl: "http://fangcun.vesync.com:8081/testTeam/Automation_testonline_CN.git"], 
	predeploy: [label: "slave", businessRepoUrl: "http://fangcun.vesync.com:8081/testTeam/Automation_predeploy_CN.git"]
]
def jobNameLowerCase = env.JOB_NAME.toLowerCase()
def envFromJobName = ""
if (jobNameLowerCase.endsWith("ci")) {
	envFromJobName = "ci"
} else if (jobNameLowerCase.endsWith("testonline")) {
	envFromJobName = "testonline"
} else if (jobNameLowerCase.endsWith("predeploy")) {
	envFromJobName = "predeploy"
} else {
	echo "Your job name ${env.JOB_NAME} is supposed to end with either one of words 'ci', 'testonline', or 'predeploy' (case insensitive)."
	currentBuild.result = 'FAILURE'
}
def AGENT_LABEL = ENV_MAP[envFromJobName]['label']
echo "Stages next would be executed on agents with label: ${AGENT_LABEL}."

node(AGENT_LABEL) {
	def JENKINS_CONF_REPO_URL = "git@fangcun.vesync.com:testTeam/pipeline-conf.git"
	def JENKINS_CONF_REPO_BRANCHE = "cn"
	def JENKINS_CONF_DIR = "jobs/cn"
	def JENKINS_EXTRAS_DIR = "extras"
	def JENKINS_CONF_CONTENT = ""
	def BUSINESS_REPO_URL = ""
	def BUSINESS_REPO_BRANCH = ""
	
	def JMETER_HOME = "/data/jenkins_data/jmeter40"
	def PROJECT_ROOT_DIR = env.WORKSPACE
	def CUSTOMIZE_BUILD_XML_PY = "bin/customize_build_xml.py"
	def SAMPLE_BUILD_XML = "resources/build_template.xml"
	def OUTPUT_BUILD_XML = "build.xml"
	def JMX = ""
	def PROPERTY_FILES = ""
	def TEST_NAME = env.JOB_NAME
	
	tool name: 'ant-1.10.8', type: 'ant'
	
	stage("Get Jenkins Conf File") {
		echo "Try to get configuration file from repository ${JENKINS_CONF_REPO_URL}"
		try {
			sh "git archive --format=tar --remote=${JENKINS_CONF_REPO_URL} ${JENKINS_CONF_REPO_BRANCHE} ${JENKINS_CONF_DIR}/${env.JOB_NAME}.json | (tar xf - && mv ${JENKINS_CONF_DIR}/${JOB_NAME}.json . && rm -rf ${JENKINS_CONF_DIR})"
		} catch (Exception e) {
			echo "Configuration file ${JENKINS_CONF_DIR}/${env.JOB_NAME}.json does not exist in: repository ${JENKINS_CONF_REPO_URL}, branch ${JENKINS_CONF_REPO_BRANCHE}"
  	  		sh "exit 1"
		}
		echo "Got configuration file ${JENKINS_CONF_DIR}/${env.JOB_NAME}.json"
		JENKINS_CONF_CONTENT = readFile(encoding: 'utf-8', file: "${env.JOB_NAME}.json")
		echo JENKINS_CONF_CONTENT
	}
	
	stage("Set Variables") {
	    def jsonSlurper = new JsonSlurper()
		def jenkinsConf = jsonSlurper.parseText(JENKINS_CONF_CONTENT)
		
		if (jenkinsConf.git.containsKey("url") && jenkinsConf.git.url) {
			BUSINESS_REPO_URL = jenkinsConf.git.url
		} else {
			BUSINESS_REPO_URL = ENV_MAP[envFromJobName]['businessRepoUrl']
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
		checkout([$class: 'GitSCM', branches: [[name: "*/${BUSINESS_REPO_BRANCH}"]], doGenerateSubmoduleConfigurations: false, extensions: [[$class: 'CloneOption', depth: 1, noTags: true, reference: '', shallow: true]], submoduleCfg: [], userRemoteConfigs: [[credentialsId: 'deploy', url: "${BUSINESS_REPO_URL}"]]])
		
		echo "Downloading Python scripts, sample build.xml, xslt, and other dependencies..."
		sh "git archive --format=tar --remote=${JENKINS_CONF_REPO_URL} ${JENKINS_CONF_REPO_BRANCHE} ${JENKINS_EXTRAS_DIR} | (tar xf - && cp -r ${JENKINS_EXTRAS_DIR}/* . && rm -rf ${JENKINS_EXTRAS_DIR})"
		
		echo "Converting simple controller to transaction controller..."
		sh "[[ -d ${ANT_HOME} ]] && [[ -d ${JMETER_HOME} ]]"
		sh "cd ${PROJECT_ROOT_DIR}; [[ -d reports ]] || mkdir reports; python3 bin/simple_controller_to_transaction_controller.py ${JMX} ${JMX}; cp resources/img/* reports"
		
		echo "Generating customized build.xml.."
		sh "python3 ${CUSTOMIZE_BUILD_XML_PY} ${SAMPLE_BUILD_XML} ${OUTPUT_BUILD_XML} ${PROJECT_ROOT_DIR} ${JMETER_HOME} ${JMX} ${TEST_NAME} -p ${PROPERTY_FILES}"
	}
	
	stage("Ant Build") {
		sh "ant -file ${OUTPUT_BUILD_XML}"
	}
	
	stage("Publish HTML Reports") {
		publishHTML([allowMissing: true, alwaysLinkToLastBuild: false, keepAll: true, reportDir: 'reports', reportFiles: "${TEST_NAME}_summary.html", reportName: "${TEST_NAME}_summary", reportTitles: "${TEST_NAME}_summary"])
		publishHTML([allowMissing: true, alwaysLinkToLastBuild: false, keepAll: true, reportDir: 'reports', reportFiles: "${TEST_NAME}_detail.html", reportName: "${TEST_NAME}_detail", reportTitles: "${TEST_NAME}_detail"])
	}
	
	stage("Mark Unstable") {
		sh "[[ `grep -c '<failure>true</failure>' ${PROJECT_ROOT_DIR}/${TEST_NAME}.jtl` == 0 ]]"
		sh "[[ `grep -c 's=\"false\"' ${PROJECT_ROOT_DIR}/${TEST_NAME}.jtl` == 0 ]]"
	}
}
