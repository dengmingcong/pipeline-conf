#!/bin/bash/groovy

properties([buildDiscarder(logRotator(artifactDaysToKeepStr: '', artifactNumToKeepStr: '', daysToKeepStr: '3', numToKeepStr: '5')), disableConcurrentBuilds(), gitLabConnection('')])

ENV_MAP = [
	ci: [label: "fullTest", businessRepoName: "Automation_CI", businessRepoUrl: "git@local-git.vesync.com:testTeam/Automation_CI.git", businessRepoBranches: ["Raigor", "Regression_Raigor"]],
	testonline: [label: "Predeploy-smokeTest", businessRepoName: "Automation_testonline", businessRepoUrl: "git@fangcun.vesync.com:testTeam/Automation_testonline.git", businessRepoBranches: ["master", "Regression_master"]], 
	predeploy: [label: "Predeploy-smokeTest", businessRepoName: "Automation_predeploy", businessRepoUrl: "git@fangcun.vesync.com:testTeam/Automation_predeploy.git", businessRepoBranches: ["master", "Regression_master"]]
]

ANT_HOME = "/data/apache-ant-1.9.14"
JMETER_HOME = ""
QA_HOME = ""
PIPELINE_CONF_BRANCH = "master"
PIPELINE_CONF_DIR = ""
STAGE = ""
AGENT_LABEL = ""

/*
** 1. determine test stage based on Jenkins job name.
** 2. set QA_HOME to "/data/qa/debug" if string "debug" occurs in job name, else to "/data/qa/stable".
** 3. set JMETER_HOME to "${QA_HOME}/jmeter40" if string "debug" occurs in job name, else to "/usr/local/jmeter40".
*/
def parseJobName() {
    def jobNameLowerCase = env.JOB_NAME.toLowerCase()

    if (jobNameLowerCase.endsWith("ci")) {
    	STAGE = "ci"
    } else if (jobNameLowerCase.endsWith("testonline")) {
    	STAGE = "testonline"
    } else if (jobNameLowerCase.endsWith("predeploy")) {
    	STAGE = "predeploy"
    } else {
    	error "Your job name ${env.JOB_NAME} is supposed to end with either one of words 'ci', 'testonline', or 'predeploy' (case insensitive)."
    }
	
	if (jobNameLowerCase.startsWith("debug")) {
		QA_HOME = "/data/qa/debug"
		JMETER_HOME = "${QA_HOME}/jmeter40"
	} else {
		QA_HOME = "/data/qa/stable"
		JMETER_HOME = "/usr/local/jmeter40"
	}
}

stage("Set Envionment Variables") {
    parseJobName()
	
	PIPELINE_CONF_DIR = "${QA_HOME}/pipeline-conf/${PIPELINE_CONF_BRANCH}"
	
	AGENT_LABEL = ENV_MAP[STAGE]['label']
	echo "Stages next would be executed on agents with label: ${AGENT_LABEL}."
}

node(AGENT_LABEL) {
    def JENKINS_JOB_WORKSPACE = env.WORKSPACE
    def CUSTOMIZE_BUILD_XML_PY = "${PIPELINE_CONF_DIR}/bin/customize_build_xml.py"
    def SAMPLE_BUILD_XML = "${PIPELINE_CONF_DIR}/resources/build_template.xml"
    def OUTPUT_BUILD_XML = "${JENKINS_JOB_WORKSPACE}/build.xml"
    def JMX = ""
    def PROPERTY_FILES = ""
    def TEST_NAME = env.JOB_NAME
    def BUSINESS_REPO_DIR = ""
    def BUSINESS_REPO_NAME = ""
    def BUSINESS_REPO_BRANCH = ""

	stage("Check Agent Health") {
		echo "Execute command 'hostname' to check agent health (one minute once)."
		retry(5) {
            try {
                timeout(1) {
                    sh "hostname"
                }
            } catch (Exception e) {
                error "Error. Cannot execute command now."
            }
        }
	}

	stage("Assign JMeter Test Plan") {
        def jobConfFile = "${PIPELINE_CONF_DIR}/jobs/${env.JOB_NAME}.json"
        echo "Jenkins job configuration file: ${jobConfFile}"
        
        if (!fileExists(jobConfFile)) {
            error "Configuration file ${jobConfFile} does not exist."
        }

        echo readFile(encoding: 'utf-8', file: jobConfFile)
        def jenkinsConf = readJSON file: jobConfFile
		
		BUSINESS_REPO_NAME = ENV_MAP[STAGE]['businessRepoName']
		BUSINESS_REPO_BRANCH = jenkinsConf.git.branch
		echo "Git repository name: ${BUSINESS_REPO_NAME}"
		echo "Git repostiory branch: ${BUSINESS_REPO_BRANCH}"
		
        BUSINESS_REPO_DIR = "${QA_HOME}/${BUSINESS_REPO_NAME}/${BUSINESS_REPO_BRANCH}"
		JMX = "${BUSINESS_REPO_DIR}/${jenkinsConf.jmeter.jmx}"
		if (jenkinsConf.jmeter.containsKey("properties") && jenkinsConf.jmeter.properties) {
			PROPERTY_FILES = "${PIPELINE_CONF_DIR}/resources/common.properties," + "${BUSINESS_REPO_DIR}/${jenkinsConf.jmeter.properties}"
		} else {
			PROPERTY_FILES = "${PIPELINE_CONF_DIR}/resources/common.properties"
		}
		echo "JMeter's additional property files: ${PROPERTY_FILES}"
	}
	
	stage("Pre-Build") {
        echo "Converting simple controller to transaction controller..."
        sh "[[ -d ${ANT_HOME} ]] && [[ -d ${JMETER_HOME} ]]"
        sh "cd ${JENKINS_JOB_WORKSPACE}; [[ -d reports ]] || mkdir reports; python3 ${PIPELINE_CONF_DIR}/bin/simple_controller_to_transaction_controller.py ${JMX} ${JMX}; cp ${PIPELINE_CONF_DIR}/resources/img/* reports"
        
        echo "Generating customized build.xml.."
        sh "python3 ${CUSTOMIZE_BUILD_XML_PY} ${SAMPLE_BUILD_XML} ${OUTPUT_BUILD_XML} ${JENKINS_JOB_WORKSPACE} ${JMETER_HOME} ${JMX} ${TEST_NAME} -p ${PROPERTY_FILES}"
	}
	
	stage("Ant Build") {
		timeout(15) {
			sh  "${ANT_HOME}/bin/ant -file ${OUTPUT_BUILD_XML} run"
        }
		sh "if ! grep '</testResults>' ${JENKINS_JOB_WORKSPACE}/${TEST_NAME}.jtl ;\
				then echo 'Error. Ant building was interrupted (caused by timeout or manual aborted), try to generate report for tests already runned.';\
				sed -i 's/<testResults/& aborted=\"true\"/' ${JENKINS_JOB_WORKSPACE}/${TEST_NAME}.jtl;\
				echo '<failure>true</failure>' >> ${JENKINS_JOB_WORKSPACE}/${TEST_NAME}.jtl;\
				echo '</testResults>' >> ${JENKINS_JOB_WORKSPACE}/${TEST_NAME}.jtl;\
			fi"
		sh "${ANT_HOME}/bin/ant -file ${OUTPUT_BUILD_XML} report"
	}
	
	stage("Publish HTML Reports") {
		publishHTML([allowMissing: true, alwaysLinkToLastBuild: false, keepAll: true, reportDir: 'reports', reportFiles: "${TEST_NAME}_summary.html", reportName: "${TEST_NAME}_summary", reportTitles: "${TEST_NAME}_summary"])
		publishHTML([allowMissing: true, alwaysLinkToLastBuild: false, keepAll: true, reportDir: 'reports', reportFiles: "${TEST_NAME}_detail.html", reportName: "${TEST_NAME}_detail", reportTitles: "${TEST_NAME}_detail"])
	}
	
	stage("Mark Unstable") {
		sh "[[ `grep -c '<failure>true</failure>' ${JENKINS_JOB_WORKSPACE}/${TEST_NAME}.jtl` == 0 ]]"
		sh "[[ `grep -c 's=\"false\"' ${JENKINS_JOB_WORKSPACE}/${TEST_NAME}.jtl` == 0 ]]"
	}
}
