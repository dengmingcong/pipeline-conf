import groovy.json.JsonOutput
import static com.jayway.jsonpath.JsonPath.parse

String accountIDJSONPath = '$.result.accountID';
String tokenJSONPath = '$.result.token';
String accoutIDPropertyName = "account.id";
String tokenPropertyName = "account.token";
String accountIDVarName = "accountID";
String tokenVarName = "token";

if (Parameters == "get") {
	// Get response
	String responseText = prev.getResponseDataAsString();
	log.info("Response: " + responseText)

	// Get accountID and token
	def jsonObject = parse(responseText);
	def accountID = jsonObject.read(accountIDJSONPath);
	def token = jsonObject.read(tokenJSONPath);
	log.info("accountID: ${accountID}, token: ${token}")

	// Set properties
	props.put(accoutIDPropertyName, accountID);
	props.put(tokenPropertyName, token);
} else if (Parameters == "set") {
	def accountID = props.get(accoutIDPropertyName);
	def token = props.get(tokenPropertyName);
	
	vars.put(accountIDVarName, accountID)
	vars.put(tokenVarName, token)
}