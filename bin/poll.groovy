import static com.jayway.jsonpath.JsonPath.parse

def response;
def jsonObject;

responseHistory = "";


/*
** Returns true if all json path exist, and values are the same as expected.
**
** We will try to convert the response string to json object as first step.
*/
def isExpectedResponseJson(String responseAsString, Map checkPoints) {
    try {
        jsonObject = parse(responseAsString);
    } catch(e) {
        responseHistory += """\
		* Error message
		The response string cannot be parsed as json objcet.\n\n""".stripIndent();
        return false
    }

    // If any json path does not exist, or its value is not expected, this function will return as false.
    if (checkPoints.any { key, value ->
        try {
            valueOfResponse = jsonObject.read(key);
        } catch(e) {
            responseHistory += """\
			* Error message
			Json path does not exist in response: ${key}\n\n""".stripIndent()
            return true
        }
        
        if (valueOfResponse != value) {
            responseHistory += """\
			* Error message
			Value to this json path is incorrect. json path: ${key}, expected value: ${value}, got value: ${valueOfResponse}\n\n""".stripIndent()
            return true
        }
    }) {
        return false
    }

    responseHistory += """\
	* Success message
	All these json path exist, and values are correct: ${checkPoints}\n\n""".stripIndent()
    return true
}


/*
** Returns true if any time retried the response was as expected.
*/
def isExpectedResponseJsonAfterPoll(Map checkPoints) {
    // Retry n times, if any time the response was as expected, this function will return as true.
    if (rangeRetry.any { int i ->
        if (i == 1) {
            response = prev.getResponseDataAsString();
        } else {
            sleep(interval)
            responseHistory += """\
			* Delay
			Sleeped ${interval} milliseconds.\n\n""".stripIndent()
            response = sampler.sample().getResponseDataAsString();
        }
        response = response.replaceAll("\\\\", "");
        responseHistory += """\
# ${i} / ${count}
		
* Response
${response}\n\n""".stripIndent()
        log.info("Tried ${i} times, Response: ${response}");
        
        if (isExpectedResponseJson(response, checkPoints)) {
            return true   
        }
        return false
    }) { 
        return true
    } else {
        return false
    }
}


/*
** Return true if all check points are sub string of the response string.
*/
def isExpectedResponseString(String responseAsString, List checkPoints) {
    // Find check point (as a string) which was not contained in response string.
    if (checkPoints.any { item ->
        if (!responseAsString.contains(item)) {
            responseHistory += """\
			* Error message
			The response string does not contain sub string: ${item}\n\n""".stripIndent()
            return true
        }
    }) {
        return false
    }

    responseHistory += """\
	* Success message
	All check points ${checkPoints.join(", ")} exist in the response string\n\n""".stripIndent()
    return true
}


/*
** Returns true if any time retried the response was as expected.
*/
def isExpectedResponseStringAfterPoll(List checkPoints) {
    // Retry n times, if any time the response was expected, this function will return as true.
    if (rangeRetry.any { int i ->
        if (i == 1) {
            response = prev.getResponseDataAsString();
        } else {
            sleep(interval)
            responseHistory += """\
			* Delay
			Sleeped ${interval} milliseconds.\n\n""".stripIndent()
            response = sampler.sample().getResponseDataAsString();
        }
        response = response.replace("\\", "");
        responseHistory += """\
# ${i} / ${count}
		
* Response
${response}\n\n""".stripIndent()
        log.info("Tried ${i} times, Response: ${response}");
        
        if (isExpectedResponseString(response, checkPoints)) {
            return true   
        }
        return false
    }) { 
        return true
    } else {
        return false
    }
}


/*
** Poll in specific timeout until response was as expected.
**
** 1. Evaluate string Parameters via methond 'Eval.me()'.
** 2. If object after evaluated is instance of 'Map', the response would be parsed to json object first, and check the json object.
** 3. If object after evaluated is instance of 'List', check if every string in list are sub string of response.
** 4. If neither, mark assertion as failure.
*/
def poll() {
	timeout = 10000;
	interval = 500;

	// Set timeout equal to first argument if 'Parameters' does not start with '['
	if (!Parameters.startsWith('[')) {
		try {
			timeout = args[0].toInteger()
		} catch(e) {
			responseHistory += """\
			
			* Error message
			The first argument can only be numbers if 'Parameters' does not start with '['.\n\n""".stripIndent();
			AssertionResult.setFailureMessage("${responseHistory}");
            AssertionResult.setFailure(true);
			return
		}
		Parameters = Parameters.replace(args[0], "")
	}
	
	count = (timeout / interval).intValue();
	rangeRetry = 1..count;
	
	responseHistory += """Timeout set to expire in ${timeout} milliseconds.\n""";
	
    def checkPoints = Eval.me(Parameters)
    switch (checkPoints) {
        case Map:
			responseHistory += "Parameters are evaluated as Map, response would be treated as JSON.\n\n"
            if (isExpectedResponseJsonAfterPoll(checkPoints)) {
                AssertionResult.setFailureMessage("${responseHistory}");
                AssertionResult.setFailure(false);
            } else {
                responseHistory += "Retried ${count} times, all failed."
                AssertionResult.setFailureMessage("${responseHistory}");
                AssertionResult.setFailure(true);
            }
            break        

        case List:
			responseHistory += "Parameters are evaluated as List, response would be treated as literal string.\n\n"
            if (isExpectedResponseStringAfterPoll(checkPoints)) {
                AssertionResult.setFailureMessage("${responseHistory}");
                AssertionResult.setFailure(false);
            } else {
                responseHistory += "Retried ${count} times, all failed."
                AssertionResult.setFailureMessage("${responseHistory}");
                AssertionResult.setFailure(true);
            }
            break 

        default:
            responseHistory += "Error! Parameter assigned can only be instance of List or Map."
            AssertionResult.setFailureMessage("${responseHistory}");
            AssertionResult.setFailure(true);
    }
}

poll()
sleep(interval)
log.info("Sleeped ${interval} milliseconds before sending new request.")
