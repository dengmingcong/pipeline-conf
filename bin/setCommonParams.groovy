#!/bin/bash/groovy

/*
** 从登录接口的请求和响应中提取参数，并以此作为后续接口的公共参数。
 */

import groovy.json.JsonSlurper

def jsonSlurper = new JsonSlurper()

def setLegacyCommonParameters(token, accountID, timeZone, acceptLanguage, phoneOS, phoneBrand, appVersion) {
	String commonParams = """\
		"token": "${token}",
		"accountID": "${accountID}",
		"timeZone": "${timeZone}",
		"acceptLanguage": "${acceptLanguage}",
		"phoneOS": "${phoneOS}",
		"phoneBrand": "${phoneBrand}",
		"appVersion": "${appVersion}",
		"debugMode": true,
	""".stripIndent();

	props.put(outputPropName, commonParams);
	log.info("name: ${outputPropName}, value: ${commonParams}")
}

def setLatestCommonParameters(token, accountID, timeZone, acceptLanguage, osInfo, clientInfo, clientVersion, clientType) {
	String uuid = UUID.randomUUID().toString()
	String commonParams = """\
		"token": "${token}",
		"accountID": "${accountID}",
		"timeZone": "${timeZone}",
		"acceptLanguage": "${acceptLanguage}",
		"osInfo": "${osInfo}",
		"clientInfo": "${clientInfo}",
		"clientVersion": "${clientVersion}",
		"clientType": "${clientType}",
		"debugMode": true,
		"terminalId": "${uuid}",
	""".stripIndent();

	props.put(outputPropName, commonParams);
	log.info("name: ${outputPropName}, value: ${commonParams}")
}

def setClientTypeOnPhoneOS(String phoneOS) {
	phoneOS = phoneOS.toLowerCase()
	if (phoneOS.contains("ios") || phoneOS.contains("android")) {
		clientType = "vesyncApp"
	} else if (phoneOS.contains("win")) {
		clientType = "web"
	} else {
		clientType = "M"
	}
    return clientType
}

outputPropName = args[0]
String outputType = args[1]
// can only be one of "LEGACY, LATEST"
OUTPUT_TYPES = ["LEGACY", "LATEST"]
assert OUTPUT_TYPES.contains(outputType), "output type (the second parameter) can only be one of ${OUTPUT_TYPES}"

String requestText = sampler.getArguments().getArgument(0).getValue();
String responseText = prev.getResponseDataAsString();

String tokenKeyName = 'token'
String accountIDKeyName = 'accountID'
if (args.size() == 3) {
	accountIDKeyName = args[2]
}

requestAsMap = jsonSlurper.parseText(requestText)
responseAsMap = jsonSlurper.parseText(responseText);
token = responseAsMap['result'][tokenKeyName]
accountID = responseAsMap['result'][accountIDKeyName]
log.info("accountID: ${accountID}, token: ${token}");

isLegacy = true

if ((requestAsMap.containsKey("context") && requestAsMap.context.containsKey("osInfo")) ||
		requestAsMap.containsKey("osInfo")) {
	isLegacy = false
}

if (isLegacy) {
	String timeZone = requestAsMap.timeZone
	String acceptLanguage = requestAsMap.acceptLanguage
	String phoneOS = requestAsMap.phoneOS
	String phoneBrand = requestAsMap.phoneBrand
	String appVersion = requestAsMap.appVersion

	if (outputType == "LEGACY") {
		setLegacyCommonParameters(token, accountID, timeZone, acceptLanguage, phoneOS, phoneBrand, appVersion)
	} else if (outputType == "LATEST") {
		String osInfo = phoneOS
		String clientInfo = phoneBrand
		String clientVersion = appVersion
		clientType = setClientTypeOnPhoneOS(phoneOS)
		setLatestCommonParameters(token, accountID, timeZone, acceptLanguage, osInfo, clientInfo, clientVersion, clientType)
	}
} else {
	requestContext = requestAsMap
	if (requestAsMap.containsKey('context')) {
		requestContext = requestAsMap.context
	}
	timeZone = requestContext.timeZone
	acceptLanguage = requestContext.acceptLanguage
	osInfo = requestContext.osInfo
	clientInfo = requestContext.clientInfo
	clientVersion = requestContext.clientVersion
	clientType = requestContext.clientType

	if (outputType == "LATEST") {
		setLatestCommonParameters(token, accountID, timeZone, acceptLanguage, osInfo, clientInfo, clientVersion, clientType)
	} else if (outputType == "LEGACY") {
		phoneOS = osInfo
		phoneBrand = clientInfo
		appVersion = clientVersion
		setLegacyCommonParameters(token, accountID, timeZone, acceptLanguage, phoneOS, phoneBrand, appVersion)
	}
}
