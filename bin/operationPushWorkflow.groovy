#!/bin/bash/groovy
responseBusinessIds = vars.get("businessId_ALL")

allCreatorDataMap = [
	"msgIdD01": vars.get("msgIdD01"),
	"msgIdD02": vars.get("msgIdD02"),
	"msgIdD03": vars.get("msgIdD03"),
	"msgIdD04": vars.get("msgIdD04"),
	"msgIdD05": vars.get("msgIdD05"),
	"msgIdD06": vars.get("msgIdD06"),
	"msgIdD07": vars.get("msgIdD07"),
	"msgIdD08": vars.get("msgIdD08"),
	"msgIdD09": vars.get("msgIdD09"),
	"msgIdD10": vars.get("msgIdD10"),
	"msgIdD11": vars.get("msgIdD11"),
	"msgIdD12": vars.get("msgIdD12"),
	"msgIdD13": vars.get("msgIdD13"),
	"msgIdD14": vars.get("msgIdD14"),
	"msgIdD15": vars.get("msgIdD15"),
	"msgIdD16": vars.get("msgIdD16"),
	"msgIdD17": vars.get("msgIdD17"),
	"msgIdD18": vars.get("msgIdD18"),
	"msgIdD19": vars.get("msgIdD19"),
	"msgIdD20": vars.get("msgIdD20"),
	"msgIdD21": vars.get("msgIdD21"),
	"msgIdD22": vars.get("msgIdD22"),
	"msgIdD23": vars.get("msgIdD23"),
	"msgIdD24": vars.get("msgIdD24"),
	"msgIdD25": vars.get("msgIdD25"),
	"msgIdD26": vars.get("msgIdD26"),
	"msgIdD27": vars.get("msgIdD27"),
	"msgIdD28": vars.get("msgIdD28"),
	"msgIdD29": vars.get("msgIdD29"),
	"msgIdD30": vars.get("msgIdD30"),
	"msgIdD31": vars.get("msgIdD31"),
	"msgIdD32": vars.get("msgIdD32"),
	"msgIdD33": vars.get("msgIdD33"),
	"msgIdD34": vars.get("msgIdD34"),
	"msgIdD35": vars.get("msgIdD35"),
	"msgIdD36": vars.get("msgIdD36"),
	"msgIdD37": vars.get("msgIdD37"),
	"msgIdD38": vars.get("msgIdD38"),
	"msgIdD39": vars.get("msgIdD39"),
	"msgIdD40": vars.get("msgIdD40"),
	"msgIdD41": vars.get("msgIdD41"),
	"msgIdD42": vars.get("msgIdD42"),
	"msgIdD43": vars.get("msgIdD43"),
	"msgIdD44": vars.get("msgIdD44"),
	"msgIdD45": vars.get("msgIdD45"),
	"msgIdD46": vars.get("msgIdD46")
]

allApproverDataMap = [
	"msgIdA01": vars.get("msgIdA01"),
	"msgIdA02": vars.get("msgIdA02"),
	"msgIdA03": vars.get("msgIdA03"),
	"msgIdA04": vars.get("msgIdA04"),
	"msgIdA05": vars.get("msgIdA05"),
	"msgIdA06": vars.get("msgIdA06"),
	"msgIdA07": vars.get("msgIdA07"),
	"msgIdA08": vars.get("msgIdA08")
]

// variable like "msgIdA01" will be treated as approver data, "msgIdD01" will be treated as creator data
allDataMap = allCreatorDataMap
if (Parameters.contains("A")) {
	allDataMap = allApproverDataMap
}

expectIncludeMsgIdList = Eval.me(Parameters)

log.info("list got after evaluating parameters: ${expectIncludeMsgIdList}")

expectIncludeMsgIdMap = [:]
expectIncludeMsgIdList.each{ i -> 
	evaledValue = vars.get(i)
	log.info("value of ${i}: ${evaledValue}")
	expectIncludeMsgIdMap[i] = evaledValue
}

expectNotIncludeMsgIdMap = allDataMap - expectIncludeMsgIdMap

responseBusinessIdList = responseBusinessIds.split(",")

log.info("response business id list: ${responseBusinessIdList}")
log.info("these business id should be in response: ${expectIncludeMsgIdMap}")
log.info("these business id should not be in response: ${expectNotIncludeMsgIdMap}")

expectIncludeMsgIdMap.each{ k, v -> assert responseBusinessIdList.contains(v), "返回的列表中应该包含（但未包含） businessId 为 ${v} (${k}) 的数据"}
expectNotIncludeMsgIdMap.each{ k, v -> assert !responseBusinessIdList.contains(v), "返回的列表中不应该包含（但已包含） businessId 为 ${v} (${k}) 的数据"}