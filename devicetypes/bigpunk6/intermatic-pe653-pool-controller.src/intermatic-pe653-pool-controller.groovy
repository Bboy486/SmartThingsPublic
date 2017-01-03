/**
 *
 * This is a custom Device Type for the Intermatic PE653 Wireless 5-Circuit Pool/Spa Control System.
 *
 * Installation
 *
 * Create a new device type (https://graph.api.smartthings.com/ide/devices)
 *    Capabilities:
 *        Configuration
 *        Refresh
 *        Polling
 *        Switch
 *    Custom Attribute
 *        switch1
 *        switch2
 *        switch3
 *        switch4
 *        switch5
 *    Custom Command
 *        on1
 *        off1
 *        on2
 *        off2
 *        on3
 *        off3
 *        on4
 *        off4
 *        on5
 *        off5
 */


preferences {
    input "operationMode1", "enum", title: "Booster Pump",
        metadata: [values: ["No",
                            "Uses Circuit-1",
                            "Variable Speed pump Speed-1",
                            "Variable Speed pump Speed-2",
                            "Variable Speed pump Speed-3",
                            "Variable Speed pump Speed-4"]]
    input "operationMode2", "enum", title: "Pump Type", metadata: [values: ["1 Speed Pump","2 Speed Pump"]]
}

metadata {
	definition (name: "Intermatic PE653 Pool Controller", author: "bigpunk6") {
		capability "Switch"
		capability "Polling"
		capability "Configuration"
		capability "Refresh"

		attribute "switch1", "string"
		attribute "switch2", "string"
		attribute "switch3", "string"
		attribute "switch4", "string"
		attribute "switch5", "string"

		command "on1"
		command "off1"
		command "on2"
		command "off2"
		command "on3"
		command "off3"
		command "on4"
		command "off4"
		command "on5"
		command "off5"
	}

	// tile definitions
	tiles {
		standardTile("switch1", "device.switch1",canChangeIcon: true) {
			state "on", label: "switch1", action: "off1", icon: "st.switches.switch.on", backgroundColor: "#79b821"
			state "off", label: "switch1", action: "on1", icon: "st.switches.switch.off", backgroundColor: "#ffffff"
		}
        standardTile("switch2", "device.switch2",canChangeIcon: true) {
			state "on", label: "switch2", action: "off2", icon: "st.switches.switch.on", backgroundColor: "#79b821"
			state "off", label: "switch2", action: "on2", icon: "st.switches.switch.off", backgroundColor: "#ffffff"
		}
        standardTile("switch3", "device.switch3",canChangeIcon: true) {
			state "on", label: "switch3", action: "off3", icon: "st.switches.switch.on", backgroundColor: "#79b821"
			state "off", label: "switch3", action:"on3", icon: "st.switches.switch.off", backgroundColor: "#ffffff"
		}
        standardTile("switch4", "device.switch4",canChangeIcon: true) {
			state "on", label: "switch4", action: "off4", icon: "st.switches.switch.on", backgroundColor: "#79b821"
			state "off", label: "switch4", action:"on4", icon: "st.switches.switch.off", backgroundColor: "#ffffff"
		}
        standardTile("switch5", "device.switch5",canChangeIcon: true) {
			state "on", label: "switch5", action: "off5", icon: "st.switches.switch.on", backgroundColor: "#79b821"
			state "off", label: "switch5", action:"on5", icon: "st.switches.switch.off", backgroundColor: "#ffffff"
		}
		standardTile("refresh", "device.switch", inactiveLabel: false, decoration: "flat") {
			state "default", label:'', action:"refresh.refresh", icon:"st.secondary.refresh"
		}

		main "switch1"
		details(["switch1","switch2","switch3","switch4","switch5","refresh"])
	}
}

import physicalgraph.zwave.commands.*

//Parse
def parse(String description) {
	def result = null
	def cmd = zwave.parse(description, [0x20: 1, 0x70: 1, 0x86: 1, 0x60:3, 0x31:1, 0x25:1, 0x81:1])
	if (cmd) {
        if( cmd.CMD == "6006" ) {
            def map = [ name: "switch$cmd.instance" ]
            if (cmd.commandClass == 37){
                if (cmd.parameter == [0]) {
                    map.value = "off"
                }
                if (cmd.parameter == [255]) {
                    map.value = "on"
                }
            }
            result = createEvent(map)
        } else {
		result = createEvent(zwaveEvent(cmd))
        }
	}
    log.debug "Parse cmd $cmd"
	log.debug "Parse returned ${result?.descriptionText}"
    log.debug "Parse \"$description\" parsed to ${result.inspect()}"
	return result
}

//Reports

def zwaveEvent(basicv1.BasicReport cmd) {
	[name: "switch", value: cmd.value ? "on" : "off", type: "physical"]
}

def zwaveEvent(switchbinaryv1.SwitchBinaryReport cmd) {
	[name: "switch", value: cmd.value ? "on" : "off", type: "digital"]
}

def zwaveEvent(sensormultilevelv1.SensorMultilevelReport cmd) {
    log.debug "$cmd"
}

def zwaveEvent(multichannelv3.MultiInstanceReport cmd) {
    log.debug "$cmd"
}

def zwaveEvent(multichannelv3.MultiChannelCapabilityReport cmd) {
    log.debug "$cmd"
}

def zwaveEvent(multichannelv3.MultiChannelEndPointReport cmd) {
    log.debug "$cmd"
}

def zwaveEvent(multichannelv3.MultiInstanceCmdEncap cmd) {
    log.debug "$cmd"
    def map = [ name: "switch$cmd.instance" ]
        if (cmd.commandClass == 37){
            if (cmd.parameter == [0]) {
                map.value = "off"
            }
            if (cmd.parameter == [255]) {
                map.value = "on"
            }
        }
    createEvent(map)
}

def zwaveEvent(multichannelv3.MultiChannelCmdEncap cmd) {
    log.debug "$cmd"
    def map = [ name: "switch$cmd.destinationEndPoint" ]
        if (cmd.commandClass == 37){
            if (cmd.parameter == [0]) {
                map.value = "off"
            }
            if (cmd.parameter == [255]) {
                map.value = "on"
            }
        }
    createEvent(map)
}

def zwaveEvent(cmd) {
	log.warn "Captured zwave command $cmd"
}

//Commands

//test
def test() {
	def cmds = []
        cmds << zwave.multiChannelV3.multiChannelCapabilityGet(endPoint:1).format()
        cmds << zwave.multiChannelV3.multiChannelCapabilityGet(endPoint:2).format()
        cmds << zwave.multiChannelV3.multiChannelCapabilityGet(endPoint:3).format()
        cmds << zwave.multiChannelV3.multiChannelCapabilityGet(endPoint:4).format()
        cmds << zwave.multiChannelV3.multiChannelCapabilityGet(endPoint:5).format()
		cmds << zwave.multiChannelV3.multiChannelEndPointGet().format()
        cmds << zwave.multiChannelV3.multiInstanceGet(commandClass:37).format()
	log.debug "Sending ${cmds.inspect()}"
	delayBetween(cmds, 2300)
}

//test2
def test2() {
    log.debug "HubID: $zwaveHubNodeId"
    log.debug "Device name: $device.displayName"
    log.debug "Device: $device.id"
    log.debug "Device: $device.name"
    log.debug "Device: $device.label"
    log.debug "$device.data"
    log.debug "$device.rawDescription"
}

//switch instance
def on(value) {
log.debug "value $value"
	delayBetween([
		zwave.multiChannelV3.multiChannelCmdEncap(sourceEndPoint: value, destinationEndPoint: value, commandClass:37, command:1, parameter:[255]).format(),
		zwave.multiChannelV3.multiChannelCmdEncap(sourceEndPoint: value, destinationEndPoint: value, commandClass:37, command:2).format()
	], 2300)
}

def off(value) {
log.debug "value $value"
	delayBetween([
		zwave.multiChannelV3.multiChannelCmdEncap(sourceEndPoint: value, destinationEndPoint: value, commandClass:37, command:1, parameter:[0]).format(),
		zwave.multiChannelV3.multiChannelCmdEncap(sourceEndPoint: value, destinationEndPoint: value, commandClass:37, command:2).format()
	], 2300)
}

//switch1
def on1() {
	on(1)
}

def off1() {
	off(1)
}

//switch2
def on2() {
	on(2)
}

def off2() {
	off(2)
}

//switch3
def on3() {
	on(3)
}

def off3() {
	off(3)
}

//switch4
def on4() {
	on(4)
}

def off4() {
	off(4)
}

//switch5
def on5() {
	on(5)
}

def off5() {
	off(5)
}

def poll() {
    refresh()
}

def refresh() {
	delayBetween([
    zwave.multiChannelV3.multiChannelCmdEncap(sourceEndPoint:1, destinationEndPoint:1, commandClass:37, command:2).format(),
    zwave.multiChannelV3.multiChannelCmdEncap(sourceEndPoint:2, destinationEndPoint:2, commandClass:37, command:2).format(),
    zwave.multiChannelV3.multiChannelCmdEncap(sourceEndPoint:3, destinationEndPoint:3, commandClass:37, command:2).format(),
    zwave.multiChannelV3.multiChannelCmdEncap(sourceEndPoint:4, destinationEndPoint:4, commandClass:37, command:2).format(),
    zwave.multiChannelV3.multiChannelCmdEncap(sourceEndPoint:5, destinationEndPoint:5, commandClass:37, command:2).format()
    ], 2300)
}
                         
def configure() {
    def operationMode = [
        value1: 7,
        value2: "two"
    ]

    // Set Operation Mode variables based on the user preferences
    if (operationMode1 == "No") {
        switch ( operationMode2 ) {
            case "1 Speed Pump":
                operationMode['value1'] = 0x00
                break
            case "2 Speed Pump":
                operationMode['value1'] = 0x02
                break
         }
    } else {
        switch ( operationMode2 ) {
            case "1 Speed Pump":
                operationMode['value1'] = 0x01
                break
            case "2 Speed Pump":
                operationMode['value1'] = 0x03
                break
        }
    }
log.debug operationMode
log.debug "$operationMode.value1"
}