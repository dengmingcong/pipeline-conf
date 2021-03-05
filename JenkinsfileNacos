#!/bin/bash/groovy

properties([buildDiscarder(logRotator(artifactDaysToKeepStr: '', artifactNumToKeepStr: '', daysToKeepStr: '3', numToKeepStr: '5')), disableConcurrentBuilds(), gitLabConnection('')])

ENV_MAP = [
	ci: [label: "fullTest", businessRepoName: "cloud-api-test", businessRepoBranches: ["master"]],
	testonline: [label: "Predeploy-smokeTest", businessRepoName: "cloud-api-test", businessRepoBranches: ["master"]], 
	predeploy: [label: "Predeploy-smokeTest", businessRepoName: "cloud-api-test", businessRepoBranches: ["master"]]
]

ANT_HOME = "/data/apache-ant-1.9.14"
JMETER_HOME = "/usr/local/jmeter40"
QA_HOME = "/data/qa"
PIPELINE_CONF_BRANCH = "deploy"
PIPELINE_CONF_DIR = "${QA_HOME}/pipeline-conf/${PIPELINE_CONF_BRANCH}"
STAGE = ""
AGENT_LABEL = ""

stage("Set Agent Label") {
    node {
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
        AGENT_LABEL = ENV_MAP[STAGE]['label']
        echo "Stages next would be executed on agents with label: ${AGENT_LABEL}."
    }
}

node(AGENT_LABEL) {
    def JENKINS_JOB_WORKSPACE = env.WORKSPACE
	def NACOS_JMETER = "${QA_HOME}/nacos-jmeter"
    def CUSTOMIZE_BUILD_XML_PY = "${PIPELINE_CONF_DIR}/bin/customize_build_xml.py"
    def SAMPLE_BUILD_XML = "${PIPELINE_CONF_DIR}/resources/build_template.xml"
    def OUTPUT_BUILD_XML = "${JENKINS_JOB_WORKSPACE}/build.xml"
    def TEST_NAME = env.JOB_NAME
    
    def BUSINESS_REPO_NAME = ENV_MAP[STAGE]['businessRepoName']
    def BUSINESS_REPO_BRANCH = "master"
	def BUSINESS_REPO_DIR = "${QA_HOME}/${BUSINESS_REPO_NAME}/${BUSINESS_REPO_BRANCH}"
	
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
	
	stage("Pre-Build") {
        sh "[[ -d ${ANT_HOME} ]] && [[ -d ${JMETER_HOME} ]]"
        sh "cd ${JENKINS_JOB_WORKSPACE}; [[ -d reports ]] || mkdir reports; cp ${PIPELINE_CONF_DIR}/resources/img/* reports"
		sh "cd ${NACOS_JMETER}/bin; python3 init_jenkins_build.py ${env.JOB_NAME} ${JENKINS_JOB_WORKSPACE} ${JMETER_HOME} ${TEST_NAME} ${BUSINESS_REPO_DIR} ${OUTPUT_BUILD_XML}"
	}
	
	stage("Ant Build") {
		timeout(15) {
			sh "${ANT_HOME}/bin/ant -file ${OUTPUT_BUILD_XML}"
		}
	}
	
	stage("Publish HTML Reports") {
		def reportFiles = []
        jmx_file_names = readJSON file: "jmx.json"
        jmx_file_names.each {
			jtlContent = readFile "${it}.jtl"
			if (jtlContent.contains('s="false"')) {
				newReportName = "[FAIL]${it}.html"
				sh "mv reports/${it}.html reports/${newReportName}"
				reportFiles.add(newReportName)
			} else {
				newReportName = "[PASS]${it}.html"
				sh "mv reports/${it}.html reports/[PASS]${it}.html"
				reportFiles.add(newReportName)
			}
        }
		reportFilesString = reportFiles.join(",")
		publishHTML([allowMissing: true, alwaysLinkToLastBuild: false, keepAll: true, reportDir: 'reports', reportFiles: reportFilesString, reportName: "${TEST_NAME}", reportTitles: ""])
	}
	
	stage("Mark Unstable") {
        jmx_file_names = readJSON file: "jmx.json"
        jmx_file_names.each {
			sh "[[ `grep -c '<failure>true</failure>' ${it}.jtl` == 0 ]]"
			sh "[[ `grep -c 's=\"false\"' ${it}.jtl` == 0 ]]"
        }
	}
}
