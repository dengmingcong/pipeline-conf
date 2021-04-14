String priorityPropName = "operation.push-banner.task.priority"
def priority = props.get(priorityPropName).toInteger()
log.info("priority: ${priority}")
def new_priority = priority + 1
log.info("new priority: ${new_priority}")
props.put(priorityPropName, new_priority.toString())