import static com.jayway.jsonpath.JsonPath.parse

timeout = 10000;
interval = 400;
count = (timeout / interval).intValue();
rangeRetry = 1..count;

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
        responseHistory += "The response string cannot be parsed as json objcet.\n";
        return false
    }

    // If any json path does not exist, or its value is not expected, this function will return as false.
    if (checkPoints.any { key, value ->
        try {
            valueOfResponse = jsonObject.read(key);
        } catch(e) {
            responseHistory += "Json path does not exist in response: ${key}\n"
            return true
        }
        
        if (valueOfResponse != value) {
            responseHistory += "Value to this json path is incorrect. json path: ${key}, expected value: ${value}, got value: ${valueOfResponse}\n"
            return true
        }
    }) {
        return false
    }

    responseHistory += "All these json path exist, and values are correct: ${checkPoints}\n"
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
            responseHistory += "Sleeped ${interval} milliseconds.\n"
            response = sampler.sample().getResponseDataAsString();
        }
        response = response.replaceAll("\\\\", "");
        responseHistory += "${i}, response: ${response}\n"
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
            responseHistory += "The response string does not contain sub string: ${item}\n"
            return true
        }
    }) {
        return false
    }

    responseHistory += "All check points ${checkPoints.join(", ")} exist in the response string\n"
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
            responseHistory += "Sleeped ${interval} milliseconds.\n"
            response = sampler.sample().getResponseDataAsString();
        }
        response = response.replace("\\", "");
        responseHistory += "${i}, response: ${response}\n"
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
    def checkPoints = Eval.me(Parameters)
    switch (checkPoints) {
        case Map:
			responseHistory += "Parameters are evaluated as Map, response would be treated as JSON \n"
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
            if (isExpectedResponseStringAfterPoll(checkPoints)) {
                AssertionResult.setFailureMessage("${responseHistory}");
                AssertionResult.setFailure(false);
            } else {
                responseHistory += "retried ${count} times, all failed."
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
