#!/bin/bash/groovy

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.jayway.jsonpath.JsonPath

String delimiter = "__@@__"

// Get response
String responseText = prev.getResponseDataAsString();
log.info("raw response: ${responseText}")

// remove redundant quotes to make string valid JSON
responseText = responseText.replace("\"{", "{").replace("}\"", "}")
log.info("response parsed: ${responseText}")

// Get JSON path and expected string from parameters
log.info("Parameters: " + Parameters)
String jsonPath = ""
String expectedJsonString = ""

if (Parameters.contains(delimiter)) {
	splited = Parameters.split(delimiter)
	jsonPath = splited[0]
	expectedJsonString = splited[1]
} else {
	jsonPath = args[0];
	expectedJsonString = Parameters.replace(jsonPath, "");
}

log.info("JSON Path: ${jsonPath}")
log.info("Expected JSON string: " + expectedJsonString);

// Get response string by JSON path
Gson gson = new GsonBuilder().serializeNulls().disableHtmlEscaping().create();
def responseJson = JsonPath.read(responseText, jsonPath);
responseJson = gson.toJson(responseJson); 
log.info("Actual JSON string: ${responseJson}");

responseHistory = """

* JSON Path
${jsonPath}

* Expected
${expectedJsonString}

* Got
${responseJson}

"""

org.skyscreamer.jsonassert.JSONAssert.assertEquals(responseHistory, expectedJsonString, responseJson, false);