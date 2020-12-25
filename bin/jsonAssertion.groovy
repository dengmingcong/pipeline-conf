#!/bin/bash/groovy

import groovy.json.JsonOutput
import static com.jayway.jsonpath.JsonPath.parse

// Get response
String responseText = prev.getResponseDataAsString();
log.info("Response: " + responseText)

// Get JSON path and expected string from parameters
log.info("Parameters: " + Parameters)
def jsonPath = args[0];
log.info("JSON Path: ${jsonPath}")
def expectedJsonString = Parameters.replace(jsonPath, "");
log.info("Expected JSON string: " + expectedJsonString);

// Get actual string by JSON path
def jsonObject = parse(responseText);
def actualJsonString = jsonObject.read(jsonPath);
actualJsonString = JsonOutput.toJson(actualJsonString);  
log.info("Actual JSON string: " + actualJsonString);

responseHistory = """

* JSON Path
${jsonPath}

* Expected
${expectedJsonString}

* Got
${actualJsonString}

"""

org.skyscreamer.jsonassert.JSONAssert.assertEquals(responseHistory, expectedJsonString, actualJsonString, false);