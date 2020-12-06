import static com.jayway.jsonpath.JsonPath.parse
timeout = 4000;
interval = 400;
count = (timeout / interval).intValue();
rangeRetry = 1..count;

def response;
def jsonObject;

responseHistory = "";
controllerMap = [
    "power": [
        ['$.event.header.name': "ChangeReport", '$.event.payload.change.properties[0].namespace': "Alexa.PowerController"],
        ['$.event.payload.change.properties[0].name': "powerState"],
        2
    ],
    "add": [
        ['$.event.header.name': "ChangeReport", '$.event.payload.change.properties[0].namespace': ""],
        [],
        2
    ],
    "update": [
        ['$.event.header.name': "ChangeReport", '$.event.payload.change.properties[0].namespace': ""],
        [],
        2
    ],
]

deviceAssertions = [
    "Core400S": [
        '$.a.b.c': "e"
    ]
]

/*
** Returns true if give json string met all conditions in checkPoints map.
*/
def hasJsonStringMetConditions(String jsonString, Map checkPoints){
    //轮询停止的条件是：用户传的checkPoints里面的值都有，或者是10s超时，checkPoints里面的值都不包含，则终止
    try {
	    jsonObject = parse(jsonString);
	} catch(e) {
	    responseHistory += "The string cannot be parsed as json objcet.\n";
	    return false
	}

    if (checkPoints.every { key, value ->
	    try {
	        valueOfResponse = jsonObject.read(key);
	    } catch(e) {
	        log.info("Json path ${key} does not exist in json string ${jsonString}")
	        responseHistory += "Json path does not exist in json string: ${key}\n"
	        return false
	    }

	    if (valueOfResponse != value) {
	        log.info("Value to json path ${key} is not correct, value expected: ${value}, value got: ${valueOfResponse}")
	        responseHistory += "Value to json path ${key} is not correct, value expected: ${value}, value got: ${valueOfResponse}"
	        return false
	    }
	    log.info("Json path ${key} exists, and value ${value} is right.")
	    responseHistory += "Json path ${key} exists, and value ${value} is right."
	    return true
    }) {
        responseHistory += "All these json path exist, and values are correct: ${checkPoints}\n"
        return true
    }

    return false   //这个false为啥要return
}

/*
** Find json string corresponding to specific report type from response.
*/
def findJsonStringFromResponse(String responseAsString, Map checkPoints) {
	def expectedJsonString = null;
	// Get all json string from response, saved to list
	jsonStringList = responseAsString.findAll(/body: (.*?)\. response:/) {match, body -> body}
	log.info("Json string list extracted : ${jsonStringList}")   //取出的re是一个[{}]或者[{},{}]的格式数据
	responseHistory += "Json string list extracted : ${jsonStringList}\n"

	expectedJsonString = jsonStringList.find { String jsonString ->
		if (hasJsonStringMetConditions(jsonString, checkPoints)) {
			return true
		}
		return false
	}
	log.info("Json string met conditions: ${expectedJsonString}")
	responseHistory += "Json string met conditions: ${expectedJsonString}\n"

	return expectedJsonString;
}

/*
** Returns json string corresponding to specific report type via polling.
*/
def pollAndReturnJsonString(Map checkPoints) {
    // Retry n times, if any time the response was expected, this function will return as true.
    def expectedJsonString = null;
    rangeRetry.any { int i ->
        if (i == 1) {
            response = prev.getResponseDataAsString();
        } else {
            sleep(interval)
            responseHistory += "Sleeped ${interval} milliseconds.\n"
            response = sampler.sample().getResponseDataAsString();
        }
        response = response.replace("\\", "");
        responseHistory += "${i}, response: ${response}\n"
        log.info("Tried ${i} times, Response: ${response}");
        expectedJsonString = findJsonStringFromResponse(response, checkPoints)

        if (expectedJsonString) {
        	responseHistory += "Json string met conditions found: ${expectedJsonString}\n"
            return true
        }
        return false
    }

    log.info("Extracted json string after polling: ${expectedJsonString}")
    return expectedJsonString
}

/*
** Add additional assertions to 'controllerMap' based on report type.
*/
def evaluateControllerMap() {
    def controllers = ["power", "mode", "range", "toggle"]
    def addOrUpdate = ["add", "update"]
    def type = args[0]
    // common assertions preset
    def assertionsMap = controllerMap[type][1]
    log.info("Assertions preset: ${assertionsMap}")
    responseHistory += "Assertions preset: ${assertionsMap}\n"
    if (controllers.contains(type)) {
        assertionsMap.put('$.event.payload.change.properties[0].value', args[1])
    } else if (addOrUpdate.contains(type)) {
        configModel = args[1]
        assert deviceAssertions.contains(configModel) : "Config model provided not in ${deviceAssertions.keySet().join(', ')}"
        def additionAssertionMap = deviceAssertions[configModel]
        assertionsMap.putAll(additionAssertionMap)
        if (type == "update") {
            assertionsMap.put('$.event.payload.endpoints[0].friendlyName', args[2])
        }
    }
    // complete assertions
    log.info("Complete assertions: ${assertionsMap}")
    responseHistory += "Complete assertions: ${assertionsMap}\n"
    controllerMap[type][1] = assertionsMap
    log.info("Complete map: ${controllerMap}")
    responseHistory += "Complete map: ${controllerMap}\n"
}

/*
** Check parameters got from JMeter JSR223.
*/
def checkParameters() {
    // length of parameters should not be smaller than 1
	if (args.size() < 1) {
		responseHistory += "Parameters error, provide 1 parameter at least .\n"
        AssertionResult.setFailureMessage("${responseHistory}");
        return false
	}

    // first parameter should be one of given types
	def type = args[0]
	if (!controllerMap.containsKey(type)) {
		responseHistory += "First parameter can only be one of ${controllerMap.keySet().join(', ')}\n"
        AssertionResult.setFailureMessage("${responseHistory}");
       	return false
	}

    // length of parameters should be equal to value pre-set
	def expectedParamsLength = controllerMap[type][2]
	if (args.size() != expectedParamsLength) {
		responseHistory += "Parameters error. Parameters length for ${args[0]} are supposed to be ${expectedParamsLength}, but got ${args.size()}\n"
        AssertionResult.setFailureMessage("${responseHistory}");
        return false
    }

	return true
}

def main() {
	if (checkParameters()) {
        evaluateControllerMap()
		def quitPollCheckPoints = controllerMap[args[0]][0]
		def jsonCheckPoints = controllerMap[args[0]][1]
		def expectedJsonString = pollAndReturnJsonString(quitPollCheckPoints);
		if (expectedJsonString && hasJsonStringMetConditions(expectedJsonString, jsonCheckPoints)) {
			responseHistory += "Passed.\n"
			AssertionResult.setFailure(false);
		}
	}

    AssertionResult.setFailureMessage("${responseHistory}");
	AssertionResult.setFailure(true);
}

main()
