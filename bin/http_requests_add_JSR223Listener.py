import sys
from lxml import etree as LET

src = sys.argv[1]
dst = sys.argv[2]


def http_request_add_JSR223Listener():
    ltree = LET.parse(src)

    hash_trees = ltree.xpath(".//HTTPSamplerProxy/following-sibling::hashTree[1]")
    for hash_tree in hash_trees:
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
        
        cache_key = LET.SubElement(jsr223_tree, "stringProp", attrib={"name": "cacheKey"})
        cache_key.text = "true"
        
    ltree.write(dst,encoding='utf-8', xml_declaration=True, pretty_print=True)

if __name__ == "__main__":
    http_request_add_JSR223Listener()
