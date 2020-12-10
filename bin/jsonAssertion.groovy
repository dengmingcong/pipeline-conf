import groovy.json.JsonOutput
import static com.jayway.jsonpath.JsonPath.parse

String responseText = prev.getResponseDataAsString();
log.info("Response text: " + responseText)

def jsonPath = args[0];
def expectedJsonString = Parameters.replace(jsonPath, "");
log.info("Expected JSON string: " + expectedJsonString);

def jsonObject = parse(responseText);
def actualJsonString = jsonObject.read(jsonPath);
actualJsonString = JsonOutput.toJson(actualJsonString)  
log.info("Actual JSON string: " + actualJsonString);

org.skyscreamer.jsonassert.JSONAssert.assertEquals(expectedJsonString, actualJsonString, false)