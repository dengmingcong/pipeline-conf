String priorityPropName = args[0]
String operator = args[1].toString()

def priority = props.get(priorityPropName).toInteger()
log.info("priority: ${priority}")
log.info("operator: ${operator}")
def new_priority = priority

if (operator == "+") {
	new_priority = priority + 1
} else if (operator == "-") {
	new_priority = priority - 1
}

log.info("new priority: ${new_priority}")
props.put(priorityPropName, new_priority.toString())