#!/bin/bash/groovy

/*
** 将接口通用参数组装成字符串，并保存到特定的 property（property 名字和指定的用户有关）中。
** 
** 1. 用户登录成功后，获取用户的 accountID 和 token
** 2. 根据第一个参数的值，决定是为哪个用户组装
**      - setOps：运营管理平台用户
**      - setOpsMall：商城管理用户
**      - setVeSync：VeSync 用户
**      - setVeSyncMall：VeSync 商城用户
** 3. 第二个参数为用户的代号，最终的 property 名字和此代号有关
** 4. 如果第一个参数是 setVeSync 或者 setVeSyncMall，需要指定第三个参数，取属性名的前缀
**    如：若属性名为 mall.client.account01.email，第三个参数赋值为 mall.client.
*/

import groovy.json.JsonOutput
import static com.jayway.jsonpath.JsonPath.parse

/*
** Save common parameters for operation management system login session to specific property after login successfully.
*/
def setOpsLoginSessionCommonParams(propertyName) {
	String commonParams = """\
        "accountID": "${accountID}",
        "token": "${token}",
        "timeZone": "America/New_York",
        "acceptLanguage": "en",
        "osInfo": "osInfo",
        "clientInfo": "clientInfo",
        "clientType": "clientType",
        "clientVersion": "clientVersion",
        "terminalId": "terminalId",
        "debugMode": true,
	""".stripIndent();
	
	props.put(propertyName, commonParams);
	log.info("name: ${propertyName}, value: ${commonParams}")
}

/*
** Save common parameters for operation mall management system login session to specific property after login successfully.
*/
def setOpsMallLoginSessionCommonParams(propertyName, propertyPrefix) {
	String timeZonePropName = "${propertyPrefix}timeZone";
	String acceptLanguagePropName = "${propertyPrefix}acceptLanguage";
	String osInfoPropName = "${propertyPrefix}osInfo"
	String clientInfoPropName = "${propertyPrefix}clientInfo"
	String clientTypePropName = "${propertyPrefix}clientType"
	String clientVersionPropName = "${propertyPrefix}clientVersion"
	
	String timeZone = props.get(timeZonePropName);
	String acceptLanguage = props.get(acceptLanguagePropName);
	String osInfo = props.get(osInfoPropName);
	String clientInfo = props.get(clientInfoPropName);
	String clientType = props.get(clientTypePropName);
	String clientVersion = props.get(clientVersionPropName);
	
	String terminalId = vars.get("terminalId")

	String commonParams = """\
        "accountID": "${accountID}",
        "token": "${token}",
        "timeZone": "${timeZone}",
        "acceptLanguage": "${acceptLanguage}",
        "osInfo": "${osInfo}",
        "clientInfo": "${clientInfo}",
        "clientType": "${clientType}",
        "clientVersion": "${clientVersion}",
        "terminalId": "${terminalId}",
        "debugMode": true,
	""".stripIndent();
	
	props.put(propertyName, commonParams);
	log.info("name: ${propertyName}, value: ${commonParams}")
}

/*
** Save common parameters for vesync login session to specific property after login successfully.
** 
** Note:
**   Variables appVersion, phoneOS, timeZone must be set in the scope.
*/
def setVeSyncLoginSessionCommonParams(propertyName, id, propertyPrefix) {
	String appVersionPropName = "${propertyPrefix}${id}.appVersion";
	String phoneOSPropName = "${propertyPrefix}${id}.phoneOS";
	String timeZonePropName = "${propertyPrefix}${id}.timeZone"
	
	String appVersion = props.get(appVersionPropName);
	String phoneOS = props.get(phoneOSPropName);
	String timeZone = props.get(timeZonePropName);
	
	String commonParams = """\
		"acceptLanguage": "en",
		"appVersion": "${appVersion}",
		"phoneBrand": "TestBrand",
		"phoneOS": "${phoneOS}",
		"timeZone": "${timeZone}",
		"token": "${token}",
		"accountID": "${accountID}",
		"debugMode": true,
	""".stripIndent();
	
	props.put(propertyName, commonParams);
	log.info("name: ${propertyName}, value: ${commonParams}")
}

/*
** Save common parameters for vesync mall login session to specific property after login successfully.
** 
** Note:
**   Variables appVersion, phoneOS, timeZone must be set in the scope.
*/
def setVeSyncMallLoginSessionCommonParams(propertyName, id, propertyPrefix) {
	String appVersionPropName = "${propertyPrefix}${id}.appVersion";
	String phoneOSPropName = "${propertyPrefix}${id}.phoneOS";
	String timeZonePropName = "${propertyPrefix}${id}.timeZone"
	
	String appVersion = props.get(appVersionPropName);
	String phoneOS = props.get(phoneOSPropName);
	String timeZone = props.get(timeZonePropName);
	String uuid = UUID.randomUUID().toString()
	
	String commonParams = """\
		"acceptLanguage": "en",
		"clientVersion": "${appVersion}",
		"clientType": "vesyncApp",
		"clientInfo": "${phoneOS}",
		"osInfo": "${phoneOS}",
		"timeZone": "${timeZone}",
		"token": "${token}",
		"accountID": "${accountID}",
		"debugMode": true,
		"terminalId": "${uuid}",
	""".stripIndent();
	
	props.put(propertyName, commonParams);
	log.info("name: ${propertyName}, value: ${commonParams}")
}

String accountIDJSONPath = '$.result.accountID';
String tokenJSONPath = '$.result.token';

// Get login response
String responseText = prev.getResponseDataAsString();
log.info("Response: " + responseText)

// Get accountID and token
jsonObject = parse(responseText);
accountID = jsonObject.read(accountIDJSONPath);
token = jsonObject.read(tokenJSONPath);
log.info("accountID: ${accountID}, token: ${token}");

accountCode = args[1];
log.info("account code (second parameter assigned): ${accountCode}");

if (args[0] == "setOps") {
	String propertyName = "ops.account.${accountCode}.login-session.common-parameters";
	setOpsLoginSessionCommonParams(propertyName);
} else if (args[0] == "setOpsMall") {
	String propertyPrefix = "mall.management.common-params."
	if (args.size() == 3) {
		propertyPrefix = args[2]
	}
	
	String propertyName = "ops.mall.account.${accountCode}.login-session.common-parameters";
	setOpsMallLoginSessionCommonParams(propertyName, propertyPrefix);
} else if (args[0] == "setVeSync") {
	String propertyPrefix = "vesync."
	if (args.size() == 3) {
		propertyPrefix = args[2]
	}
	
	String propertyName = "vesync.${accountCode}.login-session.common-parameters";
	setVeSyncLoginSessionCommonParams(propertyName, accountCode, propertyPrefix);
} else if (args[0] == "setVeSyncMall") {
	String propertyPrefix = "vesync."
	if (args.size() == 3) {
		propertyPrefix = args[2]
	}
	
	String propertyName = "vesync.mall.${accountCode}.login-session.common-parameters";
	setVeSyncMallLoginSessionCommonParams(propertyName, accountCode, propertyPrefix);
}

String accountIDPropName = "${accountCode}.accountID"
String tokenPropName = "${accountCode}.token"
props.put(accountIDPropName, accountID)
props.put(tokenPropName, token)
log.info("accountID property, name: ${accountIDPropName}, value: ${accountID}")
log.info("token property, name: ${tokenPropName}, value: ${token}")