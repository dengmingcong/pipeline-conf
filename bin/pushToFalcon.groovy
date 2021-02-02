#!/bin/bash/groovy

// Copied from https://www.blazemeter.com/blog/sending-http-and-https-requests-using-groovy-in-jmeter

import org.apache.jmeter.samplers.SampleResult;
import org.apache.http.HttpHeaders;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.apache.http.entity.StringEntity;
import com.google.gson.Gson;


/*
** Send HTTP and HTTPS POST requests.
** 
** @url String, the path by which to send the request.
** @body Map<String,Object>, body of POST request. 
*/
List<String> sendPost(String url, List body) {

	// Create an object that allows to configure settings for the HTTP / HTTPS request
    RequestConfig requestConfig = RequestConfig.custom()
		.setConnectTimeout(2000)
		.setSocketTimeout(3000)
		.build();

	// Creating a StringEntity object that will contain the body of our request.
    StringEntity entity = new StringEntity(new Gson().toJson(body), "UTF-8");

	// Creating an object that will contain all the necessary parameters for sending the request.
    HttpUriRequest request = RequestBuilder.create("POST")
		.setConfig(requestConfig)
		.setUri(url)
		.setHeader(HttpHeaders.CONTENT_TYPE, "application/json;charset=UTF-8")
		.setEntity(entity)
		.build();

	// Forming the variable "req" with the data type String containing information about the request that is sent
    String req = "REQUEST:" + "\n" + request.getRequestLine() + "\n" + "Headers: " +
		request.getAllHeaders() + "\n" + EntityUtils.toString(entity) + "\n";

	// Creating an httpClient object that will send the request; the formation of a response; displaying the request and the response to the cmd.
    HttpClientBuilder.create().build().withCloseable {httpClient ->
        httpClient.execute(request).withCloseable {response ->
            String res = "RESPONSE:" + "\n" + response.getStatusLine() + "\n" + "Headers: " +
                    response.getAllHeaders() + "\n" +
                    (response.getEntity() != null ? EntityUtils.toString(response.getEntity()) : "") + "\n";
            System.out.println(req + "\n"  + res );
            return Arrays.asList(req, res);
        }
    }
}


/*
** Send HTTP and HTTPS GET requests.
** 
** @url String, the path by which to send the request.
** @body Map<String,Object>, all elements will be converted to query string.
*/
List<String> sendGet(String url, Map<String,String> body) {

	RequestConfig requestConfig = RequestConfig.custom()
		.setConnectTimeout(2000)
		.setSocketTimeout(3000)
		.build();

	RequestBuilder requestBuilder = RequestBuilder.get()
			.setConfig(requestConfig)
			.setUri(url)
			.setHeader(HttpHeaders.CONTENT_TYPE, "application/json;charset=UTF-8");

	body.forEach({key, value -> requestBuilder.addParameter(key, value)});
	
	HttpUriRequest request = requestBuilder.build();

	String req = "REQUEST:" + "\n" + request.getRequestLine() + "\n" + "Headers: " +
			request.getAllHeaders() + "\n";

	HttpClientBuilder.create().build().withCloseable {httpClient ->
		httpClient.execute(request).withCloseable {response ->
			String res = "RESPONSE:" + "\n" + response.getStatusLine() + "\n" + "Headers: " +
				response.getAllHeaders() + "\n" +
				(response.getEntity() != null ? EntityUtils.toString(response.getEntity()) : "") + "\n";
			System.out.println(req + "\n"  + res );
			return Arrays.asList(req, res);
		}
	}
}


/*
** Push HTTP request load time to open-falcon.
*/
def pushToFalcon(String url) {
	// skip if transaction sample
	if (sampleEvent.isTransactionSampleEvent()) { return}
	
	def deviceType = vars.get("deviceType");
	def deviceRegion = vars.get("deviceRegion");

	String endpoint = "smokeTest-${deviceType}_${deviceRegion}";
	def metric = prev.getUrlAsString();
	def timestamp = System.currentTimeMillis();
	def step = 120;
	// response time, elapsed time in milliseconds
	def value = -1;
	def isSuccess = prev.isSuccessful();
	if(isSuccess){
	value = prev.getTime();
	}
	def counterType = "GAUGE"

	Map<String,Object> map = new LinkedHashMap<>();
	map.put("endpoint", endpoint);
	map.put("metric", metric);
	map.put("timestamp", timestamp);
	map.put("step", step);
	map.put("value", value);
	map.put("counterType", counterType);
	
	List data = [map];

	List responseList = sendPost(url, data);
	log.info(Arrays.toString(responseList));
}


def push = props.get("open-falcon.push.enabled", false)

if (push) {
    def url = props.get("open-falcon.push.url", "http://127.0.0.1:1988/v1/push")
	pushToFalcon(url)
}