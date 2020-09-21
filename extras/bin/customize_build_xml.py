import argparse
import os

import xml.etree.ElementTree as ET

parser = argparse.ArgumentParser()
parser.add_argument("template", help="the template of build.xml")
parser.add_argument("out", help="new build.xml generated based on template")
parser.add_argument("project_root_dir", help="directory where test results were saved")
parser.add_argument("jmeter_home", help="JMeter home")
parser.add_argument("jmx", help="jmx file to be tested")
parser.add_argument("test_name", help="name for given test")
parser.add_argument("-p", "--properties", help="additional properties, multi-properties should be separated by ','")
args = parser.parse_args()


def customize_build_xml():

    sample_build_xml = args.template
    output_build_xml = args.out
    project_root_dir = args.project_root_dir
    jmeter_home = args.jmeter_home
    jmx = args.jmx
    test_name = args.test_name
    additional_properties = args.properties
    
    list_properties = []

    # check if files / directories exist.
    assert os.path.exists(sample_build_xml), "file or directory {} does not exist".format(sample_build_xml)
    assert os.path.exists(project_root_dir), "file or directory {} does not exist".format(project_root_dir)
    assert os.path.exists(jmeter_home), "file or directory {} does not exist".format(jmeter_home)
    assert os.path.exists(jmx), "file or directory {} does not exist".format(jmx)
    
    if additional_properties:
        if additional_properties.endswith(","):
            additional_properties = additional_properties[:-1]
        
        list_properties = additional_properties.split(",")

        for item in list_properties:
            assert os.path.exists(item.strip()), "file or directory {} does not exist.".format(item)

    tree = ET.parse(sample_build_xml)
    project_root_dir_element = tree.find("property[@name='project.root.dir']")
    jmeter_home_element = tree.find("property[@name='jmeter.home']")
    test_name_element = tree.find("property[@name='test']")
    jmeter_element = tree.find(".//jmeter")

    project_root_dir_element.set("value", project_root_dir)
    jmeter_home_element.set("value", jmeter_home)
    if jmx.startswith("/"):
        jmeter_element.set("testplan", jmx)
    else:
        jmeter_element.set("testplan", os.path.join(project_root_dir, jmx))
    test_name_element.set("value", test_name)

    for item in list_properties:
        if not item.startswith("/"):
            item = os.path.join(project_root_dir, item)        
        ET.SubElement(jmeter_element, "jmeterarg", attrib={"value": "-q{}".format(item.strip())})

    tree.write(output_build_xml)


if __name__ == "__main__":
    customize_build_xml()
