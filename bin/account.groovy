#!/bin/bash/groovy

/*
** 将接口通用参数组装成字符串，并保存到特定的 property（property 名字和指定的用户有关）中。
** 
** 1. 用户登录成功后，获取用户的 accountID 和 token
** 2. 根据第一个参数的值，决定是为运营管理平台组装（setOps），还是为 VeSync 用户组装（setVeSync）
** 3. 根据第二个参数的值，决定最终的 property 名字。注意
**      * 系统属性需要以 vesync.accountxx.appVersion 的形式命名
**      * 第二个参数（args[1]）必须是 accountxx
*/

import groovy.json.JsonOutput
import static com.jayway.jsonpath.JsonPath.parse

/*
** Save common parameters for operation management system login session to specific property after login successfully.
** 
** property value:
**   "accountID": "${accountID}",
**   "token": "${token}",
**   "timeZone": "America/New_York",
**   "acceptLanguage": "en",
**   "osInfo": "osInfo",
**   "clientInfo": "clientInfo",
**   "clientType": "clientType",
**   "clientVersion": "clientVersion",
**   "terminalId": "terminalId",
**   "debugMode": true,
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
** Save common parameters for vesync login session to specific property after login successfully.
** 
** Note:
**   Variables appVersion, phoneOS, timeZone must be set in the scope.
**
** property value:
**   "acceptLanguage": "en",
**   "appVersion": "${appVersion}",
**   "phoneBrand": "TestBrand",
**   "phoneOS": "${phoneOS}",
**   "timeZone": "${timeZone}",
**   "token": "${token}",
**   "accountID": "${accountID}",
**   "debugMode": true,
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
} else if (args[0] == "setVeSync") {
	String propertyPrefix = "vesync."
	if (args.size() == 3) {
		propertyPrefix = args[2]
	}
	
	String propertyName = "vesync.${accountCode}.login-session.common-parameters";
	setVeSyncLoginSessionCommonParams(propertyName, accountCode, propertyPrefix);
}

String accountIDPropName = "${accountCode}.accountID"
String tokenPropName = "${accountCode}.token"
props.put(accountIDPropName, accountID)
props.put(tokenPropName, token)
log.info("accountID property, name: ${accountIDPropName}, value: ${accountID}")
log.info("token property, name: ${tokenPropName}, value: ${token}")