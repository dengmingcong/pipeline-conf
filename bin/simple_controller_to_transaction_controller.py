import sys

import xml.etree.ElementTree as ET

src = sys.argv[1]
dst = sys.argv[2]


def simple_controller_to_transaction_controller():
    tree = ET.parse(src)
    simple_controllers = tree.findall(".//GenericController")
    for simple_controller in simple_controllers:
        # change tag
        simple_controller.tag = "TransactionController"
        # set attributes "guiclass", "testclass".
        simple_controller.set("guiclass", "TransactionControllerGui")
        simple_controller.set("testclass", "TransactionController")
        # add two sub-elements
        property_timer = ET.SubElement(simple_controller, "boolProp", attrib={"name": "TransactionController.includeTimers"})
        property_timer.text = "false"
        property_parent = ET.SubElement(simple_controller, "boolProp", attrib={"name": "TransactionController.parent"})
        property_parent.text = "true"

    # set all transaction controllers generating parent sample.
    transaction_controllers = tree.findall(".//TransactionController")
    for transaction_controller in transaction_controllers:
        property_parent = transaction_controller.find("boolProp[@name='TransactionController.parent']")
        property_parent.text = "true"

    tree.write(dst, encoding="utf-8")


if __name__ == "__main__":
    simple_controller_to_transaction_controller()
