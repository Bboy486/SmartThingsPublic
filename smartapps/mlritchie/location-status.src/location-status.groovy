/**
 *  Location Status
 *
 *  Copyright 2015 Michael Ritchie
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License. You may obtain a copy of the License at:
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 *  on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License
 *  for the specific language governing permissions and limitations under the License.
 *
 */
definition(
    name: "Location Status",
    namespace: "mlritchie",
    author: "Michael Ritchie",
    description: "Manage virtual switch if designated lights are on, locks are unlocked, and doors open",
    category: "Convenience",
    iconUrl: "http://cdn.device-icons.smartthings.com/Home/home5-icn.png",
    iconX2Url: "http://cdn.device-icons.smartthings.com/Home/home5-icn@2x.png")


preferences {
		section("Monitor these switches...") {
            input "switches", "capability.switch", title: "Monitor these switches", multiple: true, required: false
		}
		section("Monitor this lock") {
			input "lock1", "capability.lock", multiple: false, required: false
		}
		section("Monitor these doors") {
			input "contacts", "capability.contactSensor", multiple: true, title: "Which Contacts?", required: false
            input "garageswitch", "capability.switch", title: "Garage Switch?", required: false
		}
    section("Manage this alarm") {
			input "alarm1", "capability.alarm", multiple: false, required: false
		}
    section("Set this simulated switch") {
			input "houseSwitch", "capability.switch", multiple: false, required: true
		}
}

def installed() {
	initialize()
}

def updated() {
	unsubscribe()
	initialize()
}

def initialize() {
	subscribe(switches, "switch", evtHandler)
    subscribe(lock1, "lock", evtHandler)
    subscribe(contacts, "contact", evtHandler)
    subscribe(alarm1, "system", evtHandler)
    subscribe(houseSwitch, "switch", houseHandler)
}

def evtHandler(evt) {
    def onSwitches = switches.findAll { it?.latestValue("switch") == "on" }
	def anyOn = onSwitches.size() > 0 ? true : false
    if (anyOn) {
    	houseSwitch.setMsg("light", "on")
        houseSwitch.setMsg("onMsg", "Switches On: ${onSwitches}")
    } else {
    	houseSwitch.setMsg("light", "off")
        houseSwitch.setMsg("onMsg", "All Switches Off")
    }
    
    def openLock = lock1.latestValue("lock") == "unlocked"
    if (openLock) {
    	houseSwitch.setMsg("lock", "unlocked")
        houseSwitch.setMsg("lockMsg", "Kitchen Door Unlocked")
    } else {
    	houseSwitch.setMsg("lock", "locked")
        houseSwitch.setMsg("lockMsg", "Kitchen Door Locked")
    }
    
    def anyOpen = contacts.findAll { it?.latestValue("contact") == "open" }
    if (anyOpen) {
    	houseSwitch.setMsg("contact", "open")
        houseSwitch.setMsg("doorMsg", "Contacts Open: ${anyOpen}")
    } else {
    	houseSwitch.setMsg("contact", "closed")
        houseSwitch.setMsg("doorMsg", "All Contacts Closed")
    }
    
    def alarmStatus = alarm1.latestValue("system")
    alarmStatus = alarmStatus.replace("arming", "armed")
    def alarmDisarmed = alarmStatus == "disarmed"
    houseSwitch.setMsg("alarm", "${alarmStatus}")
    houseSwitch.setMsg("alarmMsg", "Alarm: ${alarmStatus}")
    
    def houseStatus = houseSwitch.currentValue("switch")
    
    if (anyOn || openLock || anyOpen || alarmDisarmed) {
    	if (houseStatus == "off") {
        	houseSwitch.on()
        }
    } else {
    	if (houseStatus == "on") {
        	houseSwitch.off()
        }
    }
}

def houseHandler(evt) {
	def deviceName = evt.name
    def deviceValue = evt.value
    //log.debug "deviceName: ${deviceName}, deviceValue: ${deviceValue}"

	if (deviceValue == "shuttingdown") {
        if (lock1) {
            checkLock()
        }
        if (garageswitch) {
            checkDoor()
        }
        if (alarm1) {
            checkAlarm()
        }
        turnOffSwitches(switches)
        houseSwitch.off()
	}
    
}

private checkLock() {   
	def anyLocked = lock1.currentValue("lock") == 'locked'
	if (!anyLocked) {
		lock1.lock()
	} else {
		log.debug "Door already locked."
	}
}

private checkDoor() {
    contacts.each {
        //log.debug "${it.name}, ${it.label}: ${it.latestValue("contact")}"
        if (it.label == "Garage Door") {
        	def isOpen = it.latestValue("contact") == "open"
            if (isOpen) {
                garageswitch.on()
            } else {
                log.debug "Door already closed."
            }
        }
    }
}

private checkAlarm() {
    def alarmArmed = alarm1.latestValue("system")
	if (alarmArmed != "armedStay") {
		alarm1.on()
	} else {
		log.debug "Alarm already armed."
	}
}

def turnOffSwitches(devices) {
	devices.each {
        if(it.currentValue("switch") == "on")
        	it.off()
    }
}