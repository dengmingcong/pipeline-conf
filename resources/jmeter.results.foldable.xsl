<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="2.0">
    <xsl:output method="html" indent="no" encoding="UTF-8" doctype-public="-//W3C//DTD HTML 4.01 Transitional//EN" doctype-system="http://www.w3.org/TR/html4/loose.dtd"/>
    <xsl:strip-space elements="*"/>
    <xsl:template match="/testResults">
        <html lang="en">
        <head>
            <meta name="Author" content="shanhe.me"/>
            <title>JMeter Test Results</title>
            <style type="text/css"><![CDATA[
            
                html, body { width: 100%; height: 100%; font-size: 12px; overflow: hidden; }
                .hidden {display: none}
                
                #left-panel { position: absolute; left: 0; top: 0; bottom: 0; width: 500px; overflow: auto; border-right: 1px solid #bbbbc3; }
                #left-panel ul { padding-left: 20px; line-height: 20px; list-style-type: none; white-space: nowrap; }
                #left-panel .folder-icon, #left-panel .sampler-icon { height: 1.2em; width: 1.2em; padding-right: 0.2em; display: inline-block; position: relative; top: 0.2em; }
                #left-panel .collapsed .elbow-end-minus { background: transparent url('arrows.gif') no-repeat 0 0; }
                #left-panel .collapsed .elbow-end-minus:hover { background: transparent url('arrows.gif') no-repeat -32px 0; }
                #left-panel .elbow-end-minus { height: 1.2em; width: 1.2em; padding-right: 0.2em; vertical-align: top; background: transparent url('arrows.gif') no-repeat -16px 0; margin-left: -1.4em; }
                #left-panel .elbow-end-minus:hover { height: 1.2em; width: 1.2em; padding-right: 0.2em; vertical-align: top; background: transparent url('arrows.gif') no-repeat -48px 0; }
                #left-panel div.title.selected, #left-panel div.title.selected:hover { margin-left: -9999rem; padding-left: 9999rem; background-color: #d9e8fb; }
                #left-panel div.title { cursor: pointer; }
                #left-panel div.title span { display: inline-block; }
                #left-panel div.title:hover { margin-left: -9999rem; padding-left: 9999rem; background-color: #e8e8e8; }
                #left-panel .expand-collpase-buttons { padding-left: 20px; padding-top: 10px; }
                #left-panel .expand-tree, .collapse-tree { font-size: 0.8em; margin-right: 5px; }
                
                #right-panel { position: absolute; left: 500px; right: 0; top: 0; bottom: 0; overflow: auto; background: white }
                #right-panel .group { font-size: 14px; font-weight: bold; line-height: 20px; counter-reset: assertion; background-color: #3c4ca970; color: #2b4dad; }
                #right-panel table { table-layout: fixed; width: 100%; border: none; border-collapse: collapse; white-space: break-spaces; }
                #right-panel tbody.failure { color: red }
                #right-panel tbody pre { margin: 0; }
                //#right-panel tbody tr { padding-left: 18px; },如果使用了display: block，table-layout: fixed 就不生效，暂不知原因
                #right-panel tbody tr:nth-child(odd) { background-color:  #e6e9f359; background-clip: padding-box; }
                #right-panel tbody tr:nth-child(even) { background-color:  #1331d214; background-clip: padding-box; }
                #right-panel tbody tr td { vertical-align: baseline; font-size: 13px; overflow: auto; text-overflow: break-spaces; }
                #right-panel tbody tr td.data { line-height: 19px; }
                #right-panel thead tr th { text-align: left; }
                #right-panel tbody tr td.key, #right-panel thead tr th.key { width: 10%; }
                #right-panel tbody tr td.delimiter, #right-panel thead tr th.delimiter { width: 1%; }
                #right-panel tbody tr td.value, #right-panel thead tr th.value { width: 89%; font-family: monospace; }
                // #right-panel tbody tr th.assertion:before { counter-increment: assertion; content: counter(assertion) ". " }
                // #right-panel tbody tr th.assertion { color: black }
                #right-panel .trail { border-top: 1px solid #b4b4b4 }
                
                .all-passed { color: green; font-size: 2rem; padding: 2em;}
				.aborted-failure { color: red; font-size: 2rem; padding: 2em;}
                
            ]]></style>
            <script type="text/javascript"><![CDATA[
                
                var last_selected = null;
                var patch_timestamp = function() {
                    var spans = document.getElementsByTagName("span");
                    var len = spans.length;
                    for( var i = 0; i < len; ++i ) {
                        var span = spans[i];
                        if( "patch_timestamp" == span.className )
                            span.innerHTML = new Date( parseInt( span.innerHTML ) );
                    }
                };
                
                const setClassHidden = function addClassHiddenToSiblingUl() {
                    let collapsedElements = document.querySelectorAll(".collapsed");
                    collapsedElements.forEach(function(item, index, array) {
                        item.parentNode.querySelector("ul").classList.add("hidden");
                    });
                };
                
                const toggleElbow = function handlerWhenElbowElementWasClicked(event) {
                    if (event.target.classList.contains("elbow-end-minus")) {
                        event.target.parentNode.classList.toggle("collapsed");
                        event.target.parentNode.parentNode.querySelector("ul").classList.toggle("hidden");
                    }
                };
                
                const selectOnLoad = function decideWhichElementShouldBeSelectedWhenDocumentLoaded() {
                    let firstFailureHttpSample = document.querySelector(".http-sample > .failure");
					let firstAbortedHttpSample = document.querySelector(".aborted");
					if(firstAbortedHttpSample){
						document.getElementById("right-panel").innerHTML = document.getElementById("aborted-failure").innerHTML;
						document.querySelector("#right-panel > div").classList.add("aborted-failure");
					} else {
						if (firstFailureHttpSample) {
							last_selected = firstFailureHttpSample;
							last_selected.classList.add("selected");
							document.getElementById("right-panel").innerHTML = firstFailureHttpSample.nextSibling.innerHTML;
						} else {
							document.getElementById("right-panel").innerHTML = document.getElementById("all-passed").innerHTML;
							document.querySelector("#right-panel > div").classList.add("all-passed");
						}
					}
                }
                
                const titleSelected = function handlerWhenTitleWasClicked(event) {
                    let titleElement = null;
                    if (event.target.classList.contains("title")) {
                        titleElement = event.target;
                    }
                    if (event.target.parentNode.classList.contains("title") && !event.target.classList.contains("elbow-end-minus")) {
                        titleElement = event.target.parentNode;
                    }
                    if (titleElement) {
                        if (last_selected === titleElement) {
                            return;
                        }
                        if (last_selected) {
                            last_selected.classList.remove("selected");
                        }
                        last_selected = titleElement;
                        last_selected.classList.add("selected");
                        document.getElementById("right-panel").innerHTML = last_selected.nextSibling.innerHTML;
                    }
                }
                
                const expandTree = function handlerWhenButtonExpandTreeWasClicked(event) {
                    if (event.target.classList.contains("expand-tree")) {
                        let expandableElements = document.querySelectorAll(".expandable");
                        expandableElements.forEach(function(item, index, array) {
                            if (item.classList.contains("collapsed")) {
                                item.classList.remove("collapsed");
                                item.parentNode.querySelector("ul").classList.remove("hidden");
                            }
                        });
                    }
                }
                
                const collapseTree = function handlerWhenButtonCollapseTreeWasClicked(event) {
                    if (event.target.classList.contains("collapse-tree")) {
                        let expandableElements = document.querySelectorAll(".expandable");
                        expandableElements.forEach(function(item, index, array) {
                            if (!item.classList.contains("collapsed")) {
                                item.classList.add("collapsed");
                                item.parentNode.querySelector("ul").classList.add("hidden");
                            }
                        });
                    }
                }
                
                const isJsonString = function returnTrueIfStringParsedAsJson(str) {
                    try {
                        JSON.parse(str);
                    } catch (e) {
                        return false;
                    }
                    return true;
                }
                
                const formatJSON = function formatDataValueWhenParsedAsJSON() {
                    let tableDatas = document.querySelectorAll("td.data.value");
                    let jsonObject = null;
                    tableDatas.forEach(function(item, index, array) {
                        if (isJsonString(item.innerText)) {
                            jsonObject = JSON.parse(item.innerText);
                            item.innerText = JSON.stringify(jsonObject, null, 4);
                        }
                    });
                }
        
                window.onload = function() { 
                    patch_timestamp();
                    var o = document.getElementById("result-list");
                    var leftPanel = document.getElementById("left-panel");
                    selectOnLoad();
                    o.addEventListener('click', toggleElbow, false);
                    o.addEventListener('click', titleSelected, false);
                    leftPanel.addEventListener('click', expandTree, false);
                    leftPanel.addEventListener('click', collapseTree, false);
                    formatJSON();
                };
        
            ]]></script>
        </head>
        <body>
            <div id="left-panel">
                <div class="expand-collpase-buttons">
                    <button class="expand-tree">Expand Tree</button>
                    <button class="collapse-tree">Collapse Tree</button>
                </div>
                <ul id="result-list">
                <xsl:for-each-group select="sample | httpSample" group-by="@tn">
                    <xsl:sort select="replace(@tn, '.* (\d+)-\d+$', '$1')"/>
                    <xsl:sort select="replace(@tn, '.* \d+-(\d+)$', '$1')"/>
                    <li class="thread-group">
                        <div>
                            <xsl:choose>
                                <xsl:when test="current-group()[@s = 'false']">
                                    <xsl:attribute name="class">title thread-group-name expandable failure</xsl:attribute>
                                    <img src="s.gif" class="elbow-end-minus"></img>
                                    <img src="folder-fail.png" class="folder-icon"></img>
                                </xsl:when>
                                <xsl:otherwise>
                                    <xsl:attribute name="class">title thread-group-name expandable collapsed success</xsl:attribute>
                                    <img src="s.gif" class="elbow-end-minus"></img>
                                    <img src="folder-success.png" class="folder-icon"></img>
                                </xsl:otherwise>
                            </xsl:choose>
                            <span>
                                <xsl:value-of select="current-grouping-key()"/>
                            </span>
                        </div>
                        <div class="detail hidden">
                            <div class="group">Thread Group</div>
                            <div class="zebra">
                                <table>
                                    <tr><td class="data key">Thread Group Name</td><td class="data delimiter">:</td><td class="data value"><xsl:value-of select="current-grouping-key()"/></td></tr>
                                </table>
                            </div>
                        </div>
                        <ul>
                            <xsl:choose>
                                <xsl:when test="current-group()[@s = 'false']">
                                    <xsl:attribute name="class"></xsl:attribute>
                                </xsl:when>
                                <xsl:otherwise>
                                    <xsl:attribute name="class">hidden</xsl:attribute>
                                </xsl:otherwise>
                            </xsl:choose>
                            <xsl:apply-templates select="current-group()">
                            </xsl:apply-templates>
                        </ul>
                    </li>
                </xsl:for-each-group>
                </ul>
            </div>
            <div id="right-panel"></div>
            <div id="all-passed" class="hidden">
                <div>All testcases passed.</div>
            </div>
			
			<div id="aborted-failure" class="hidden">
				<xsl:choose>
					<xsl:when test="@aborted = 'true'">
						<xsl:attribute name="class">hidden aborted</xsl:attribute>
					</xsl:when>
					<xsl:otherwise>
						<xsl:attribute name="class">hidden</xsl:attribute>
					</xsl:otherwise>
				</xsl:choose>
                <div>此次测试异常中断（原因可能是超时或手动停止），报告内容不完整，只包含中断前的执行内容</div>
            </div>
        </body>
        </html>
    </xsl:template>
    
    <xsl:template match="httpSample">
        <li class="http-sample">
            <div>
                <xsl:choose>
                    <xsl:when test="@s = 'true'">
                        <xsl:attribute name="class">title success</xsl:attribute>
                        <img src="sampler-success.png" class="sampler-icon"></img>
                    </xsl:when>
                    <xsl:otherwise>
                        <xsl:attribute name="class">title failure</xsl:attribute>
                        <img src="sampler-fail.png" class="sampler-icon"></img>
                    </xsl:otherwise>
                </xsl:choose>
                <span>
                    <xsl:value-of select="@lb"/>
                </span>
            </div>
            <div class="detail hidden">
                <div class="group">Sampler</div>
                <div class="zebra">
                    <table>
                        <tr><td class="data key">Thread Name</td><td class="data delimiter">:</td><td class="data value"><xsl:value-of select="@tn"/></td></tr>
                        <tr><td class="data key">Timestamp</td><td class="data delimiter">:</td><td class="data value"><span class="patch_timestamp"><xsl:value-of select="@ts"/></span></td></tr>
                        <tr><td class="data key">Time</td><td class="data delimiter">:</td><td class="data value"><xsl:value-of select="@t"/> ms</td></tr>
                        <tr><td class="data key">Latency</td><td class="data delimiter">:</td><td class="data value"><xsl:value-of select="@lt"/> ms</td></tr>
                        <tr><td class="data key">Bytes</td><td class="data delimiter">:</td><td class="data value"><xsl:value-of select="@by"/></td></tr>
                        <tr><td class="data key">Sample Count</td><td class="data delimiter">:</td><td class="data value"><xsl:value-of select="@sc"/></td></tr>
                        <tr><td class="data key">Error Count</td><td class="data delimiter">:</td><td class="data value"><xsl:value-of select="@ec"/></td></tr>
                        <tr><td class="data key">Response Code</td><td class="data delimiter">:</td><td class="data value"><xsl:value-of select="@rc"/></td></tr>
                        <tr><td class="data key">Response Message</td><td class="data delimiter">:</td><td class="data value"><xsl:value-of select="@rm"/></td></tr>
                    </table>
                </div>
                <div class="trail"></div>
                <xsl:if test="count(assertionResult) &gt; 0">
                    <div class="group">Assertion</div>
                    <div class="zebra">
                        <table>
                            <xsl:for-each select="assertionResult">
                                <thead>
                                    <tr>
                                        <th class="data assertion key" scope="col"><xsl:value-of select="name"/></th>
                                        <th class="data assertion delimiter" scope="col"></th>
                                        <th class="data assertion value" scope="col"></th>
                                    </tr>
                                </thead>
                                <tbody>
                                    <xsl:attribute name="class">
                                        <xsl:choose>
                                            <xsl:when test="failure = 'true'">failure</xsl:when>
                                            <xsl:when test="error = 'true'">failure</xsl:when>
                                        </xsl:choose>
                                    </xsl:attribute>
                                    <tr><td class="data key">Failure</td><td class="data delimiter">:</td><td class="data value"><xsl:value-of select="failure"/></td></tr>
                                    <tr><td class="data key">Error</td><td class="data delimiter">:</td><td class="data value"><xsl:value-of select="error"/></td></tr>
                                    <tr><td class="data key">Failure Message</td><td class="data delimiter">:</td><td class="data value"><xsl:value-of select="failureMessage"/></td></tr>
                                </tbody>
                            </xsl:for-each>
                        </table>
                    </div>
                    <div class="trail"></div>
                </xsl:if>
                <div class="group">Request</div>
                <div class="zebra">
                    <table>
                        <tr><td class="data key">Method/Url</td><td class="data delimiter">:</td><td class="data value"><xsl:value-of select="method"/><xsl:text> </xsl:text><xsl:value-of select="java.net.URL"/></td></tr>
                        <tr><td class="data key">Query String</td><td class="data delimiter">:</td><td class="data value"><xsl:value-of select="queryString"/></td></tr>
                        <tr><td class="data key">Cookies</td><td class="data delimiter">:</td><td class="data value"><xsl:value-of select="cookies"/></td></tr>
                        <tr><td class="data key">Request Headers</td><td class="data delimiter">:</td><td class="data value"><xsl:value-of select="requestHeader"/></td></tr>
                    </table>
                </div>
                <div class="trail"></div>
                <div class="group">Response</div>
                <div class="zebra">
                    <table>
                        <tr><td class="data key">Response Headers</td><td class="data delimiter">:</td><td class="data value"><xsl:value-of select="responseHeader"/></td></tr>
                        <tr><td class="data key">Response Data</td><td class="data delimiter">:</td><td class="data value"><xsl:value-of select="responseData"/></td></tr>
                        <tr><td class="data key">Response File</td><td class="data delimiter">:</td><td class="data value"><xsl:value-of select="responseFile"/></td></tr>
                    </table>
                </div>
                <div class="trail"></div>
            </div>
        </li>
    </xsl:template>
    
    <xsl:template match="sample">
        <li class="transaction-controller">
            <div>
                <xsl:choose>
                    <xsl:when test="@s = 'true'">
                        <xsl:attribute name="class">title expandable collapsed success</xsl:attribute>
                        <img src="s.gif" class="elbow-end-minus"></img>
                        <img src="folder-success.png" class="folder-icon"></img>
                    </xsl:when>
                    <xsl:otherwise>
                        <xsl:attribute name="class">title expandable failure</xsl:attribute>
                        <img src="s.gif" class="elbow-end-minus"></img>
                        <img src="folder-fail.png" class="folder-icon"></img>
                    </xsl:otherwise>
                </xsl:choose>
                <span>
                    <xsl:value-of select="@lb"/>
                </span>
            </div>
            <div class="detail hidden">
                <div class="group">Sampler</div>
                <div class="zebra">
                    <table>
                        <tr><td class="data key">Thread Name</td><td class="data delimiter">:</td><td class="data value"><xsl:value-of select="@tn"/></td></tr>
                        <tr><td class="data key">Timestamp</td><td class="data delimiter">:</td><td class="data value"><span class="patch_timestamp"><xsl:value-of select="@ts"/></span></td></tr>
                        <tr><td class="data key">Time</td><td class="data delimiter">:</td><td class="data value"><xsl:value-of select="@t"/> ms</td></tr>
                        <tr><td class="data key">Latency</td><td class="data delimiter">:</td><td class="data value"><xsl:value-of select="@lt"/> ms</td></tr>
                        <tr><td class="data key">Bytes</td><td class="data delimiter">:</td><td class="data value"><xsl:value-of select="@by"/></td></tr>
                        <tr><td class="data key">Sample Count</td><td class="data delimiter">:</td><td class="data value"><xsl:value-of select="@sc"/></td></tr>
                        <tr><td class="data key">Error Count</td><td class="data delimiter">:</td><td class="data value"><xsl:value-of select="@ec"/></td></tr>
                        <tr><td class="data key">Response Code</td><td class="data delimiter">:</td><td class="data value"><xsl:value-of select="@rc"/></td></tr>
                        <tr><td class="data key">Response Message</td><td class="data delimiter">:</td><td class="data value"><xsl:value-of select="@rm"/></td></tr>
                    </table>
                </div>
                <div class="trail"></div>
                <xsl:if test="count(assertionResult) &gt; 0">
                    <div class="group">Assertion</div>
                    <div class="zebra">
                        <table>
                            <xsl:for-each select="assertionResult">
                                <thead>
                                    <tr>
                                        <th class="data assertion" scope="col"><xsl:value-of select="name"/></th>
                                    </tr>
                                </thead>
                                <tbody>
                                    <xsl:attribute name="class">
                                        <xsl:choose>
                                            <xsl:when test="failure = 'true'">failure</xsl:when>
                                            <xsl:when test="error = 'true'">failure</xsl:when>
                                        </xsl:choose>
                                    </xsl:attribute>
                                    <tr><td class="data key">Failure</td><td class="data delimiter">:</td><td class="data value"><xsl:value-of select="failure"/></td></tr>
                                    <tr><td class="data key">Error</td><td class="data delimiter">:</td><td class="data value"><xsl:value-of select="error"/></td></tr>
                                    <tr><td class="data key">Failure Message</td><td class="data delimiter">:</td><td class="data value"><xsl:value-of select="failureMessage"/></td></tr>
                                </tbody>
                            </xsl:for-each>
                        </table>
                    </div>
                    <div class="trail"></div>
                </xsl:if>
                <div class="group">Request</div>
                <div class="zebra">
                    <table>
                        <tr><td class="data key">Method/Url</td><td class="data delimiter">:</td><td class="data value"><xsl:value-of select="method"/><xsl:text> </xsl:text><xsl:value-of select="java.net.URL"/></td></tr>
                        <tr><td class="data key">Query String</td><td class="data delimiter">:</td><td class="data value"><xsl:value-of select="queryString"/></td></tr>
                        <tr><td class="data key">Cookies</td><td class="data delimiter">:</td><td class="data value"><xsl:value-of select="cookies"/></td></tr>
                        <tr><td class="data key">Request Headers</td><td class="data delimiter">:</td><td class="data value"><xsl:value-of select="requestHeader"/></td></tr>
                    </table>
                </div>
                <div class="trail"></div>
                <div class="group">Response</div>
                <div class="zebra">
                    <table>
                        <tr><td class="data key">Response Headers</td><td class="data delimiter">:</td><td class="data value"><xsl:value-of select="responseHeader"/></td></tr>
                        <tr><td class="data key">Response Data</td><td class="data delimiter">:</td><td class="data value"><xsl:value-of select="responseData"/></td></tr>
                        <tr><td class="data key">Response File</td><td class="data delimiter">:</td><td class="data value"><xsl:value-of select="responseFile"/></td></tr>
                    </table>
                </div>
                <div class="trail"></div>
            </div>
            <ul>
                <xsl:choose>
                    <xsl:when test="@s = 'true'">
                        <xsl:attribute name="class">hidden</xsl:attribute>
                    </xsl:when>
                </xsl:choose>
                <xsl:apply-templates select="sample | httpSample"/>
            </ul>
        </li>
    </xsl:template>
</xsl:stylesheet>