import groovy.json.JsonSlurper
import groovy.json.JsonOutput


String response = prev.getResponseDataAsString();
log.info("==============:" + response)
//String response_new = response.replace("\"{", "{");//替换为标准的json格式
//response_new = response_new.replace("}\"", "}");//替换为标准的json格式
//log.info("======response_new========:" + response_new)


def slurper = new groovy.json.JsonSlurper();  
def jsonobj = slurper.parseText(response);  //将json string 转成json 对象

result = jsonobj.result.data.event.payload; //取出想要的值，取出来是个json对象
restoJsonString = JsonOutput.toJson(result)    //将json对象转成json字符串

log.info("======restoJsonString========:" + restoJsonString)

//expect = args[0]
//log.info("=======expect=======:" + expect)
log.info ("======expect=========:" + Parameters )
org.skyscreamer.jsonassert.JSONAssert.assertEquals(Parameters, restoJsonString, true)
