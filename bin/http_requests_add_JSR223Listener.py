import sys
from lxml import etree as LET

src = sys.argv[1]
dst = sys.argv[2]
jenkins_job_name = sys.argv[3]


def http_request_add_JSR223Listener():
    ltree = LET.parse(src)
    jmeter_test_plan = ltree.getroot()
    
    if (jmeter_test_plan.get("monitored")):
        return
    else:
        jmeter_test_plan.set("monitored", "true") 
        
    hash_trees = ltree.xpath(".//HTTPSamplerProxy/following-sibling::hashTree[1]")
    
    for hash_tree in hash_trees:
        # fist http requests is preheat interface, no need to upload monitoring
        if (hash_trees.index(hash_tree) == 0):
            continue
            
        # add one sub-elements
        jsr223_tree = LET.SubElement(hash_tree, "JSR223Listener")
        
        # set attributes "guiclass", "testclass", "testname", "enabled".
        jsr223_tree.set("guiclass", "TestBeanGUI")
        jsr223_tree.set("testclass", "JSR223Listener")
        jsr223_tree.set("testname", "JSR223 Listener")
        jsr223_tree.set("enabled", "true")

        # add three sub-elements
        script_language = LET.SubElement(jsr223_tree, "stringProp", attrib={"name": "scriptLanguage"})
        script_language.text = "groovy"
        
        filename = LET.SubElement(jsr223_tree, "stringProp", attrib={"name": "filename"})
        filename.text = "pushToFalcon.groovy"
        
        parameters = LET.SubElement(jsr223_tree, "stringProp", attrib={"name": "parameters"})
        parameters.text = jenkins_job_name
        
        cache_key = LET.SubElement(jsr223_tree, "stringProp", attrib={"name": "cacheKey"})
        cache_key.text = "true"

    ltree.write(dst,encoding='utf-8', xml_declaration=True, pretty_print=True)
    

if __name__ == "__main__":
    http_request_add_JSR223Listener()
