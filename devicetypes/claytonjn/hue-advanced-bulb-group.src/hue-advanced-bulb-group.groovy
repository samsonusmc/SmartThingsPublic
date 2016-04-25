/**
 *  Hue Advanced Bulb/Group
 *
 *  Philips Hue Type "Extended Color Light"
 *  Philips Hue Type "LightGroup"
 *  Philips Hue Type "Room"
 *
 *  Author: claytonjn
 */

// for the UI
metadata {
	// Automatically generated. Make future change here.
	definition (name: "Hue Advanced Bulb/Group", namespace: "claytonjn", author: "claytonjn") {
		capability "Switch Level"
		capability "Actuator"
		capability "Color Control"
		capability "Color Temperature"
		capability "Switch"
		capability "Refresh"
		capability "Sensor"

		command "setAdjustedColor"
        command "reset"
        command "refresh"

		attribute "deviceSwitch", "enum", ["lightsOn", "lightsOff", "lightsTurningOn", "lightsTurningOff", "groupsOn", "groupsOff", "groupsTurningOn", "groupsTurningOff"]
	}

	simulator {
		// TODO: define status and reply messages here
	}

	tiles (scale: 2){
		multiAttributeTile(name:"rich-control", type: "lighting", width: 6, height: 4, canChangeIcon: true){
			tileAttribute ("deviceSwitch", key: "PRIMARY_CONTROL") {
				attributeState "lightsOn", label:'ON', action:"switch.off", icon:"st.lights.philips.hue-single", backgroundColor:"#79b821", nextState:"lightsTurningOff"
				attributeState "lightsOff", label:'OFF', action:"switch.on", icon:"st.lights.philips.hue-single", backgroundColor:"#ffffff", nextState:"lightsTurningOn"
				attributeState "lightsTurningOn", label:'TURNING ON', action:"switch.off", icon:"st.lights.philips.hue-single", backgroundColor:"#79b821", nextState:"lightsTurningOff"
				attributeState "lightsTurningOff", label:'TURNING OFF', action:"switch.on", icon:"st.lights.philips.hue-single", backgroundColor:"#ffffff", nextState:"lightsTurningOn"
				attributeState "groupsOn", label:'ON', action:"switch.off", icon:"st.lights.philips.hue-multi", backgroundColor:"#79b821", nextState:"groupsTurningOff"
				attributeState "groupsOff", label:'OFF', action:"switch.on", icon:"st.lights.philips.hue-multi", backgroundColor:"#ffffff", nextState:"groupsTurningOn"
				attributeState "groupsTurningOn", label:'TURNING ON', action:"switch.off", icon:"st.lights.philips.hue-multi", backgroundColor:"#79b821", nextState:"groupsTurningOff"
				attributeState "groupsTurningOff", label:'TURNING OFF', action:"switch.on", icon:"st.lights.philips.hue-multi", backgroundColor:"#ffffff", nextState:"groupsTurningOn"
			}
			tileAttribute ("device.level", key: "SLIDER_CONTROL") {
				attributeState "level", action:"switch level.setLevel", range:"(0..100)"
            }
            tileAttribute ("device.level", key: "SECONDARY_CONTROL") {
	            attributeState "level", label: 'Level ${currentValue}%'
			}
			tileAttribute ("device.color", key: "COLOR_CONTROL") {
				attributeState "color", action:"setAdjustedColor"
			}
		}

        controlTile("colorTempSliderControl", "device.colorTemperature", "slider", width: 4, height: 2, inactiveLabel: false, range:"(2000..6500)") {
            state "colorTemperature", action:"color temperature.setColorTemperature"
        }

        valueTile("colorTemp", "device.colorTemperature", inactiveLabel: false, decoration: "flat", width: 2, height: 2) {
            state "colorTemperature", label: '${currentValue} K'
        }

		standardTile("reset", "device.reset", height: 2, width: 2, inactiveLabel: false, decoration: "flat") {
			state "default", label:"Reset Color", action:"reset", icon:"st.lights.philips.hue-single"
		}

		standardTile("refresh", "device.refresh", height: 2, width: 2, inactiveLabel: false, decoration: "flat") {
			state "default", label:"", action:"refresh.refresh", icon:"st.secondary.refresh"
		}

		main(["rich-control"])
		details(["rich-control", "colorTempSliderControl", "colorTemp", "reset", "refresh"])
	}
}

// parse events into attributes
def parse(description) {
	log.debug "parse() - $description"
	def results = []

	def map = description
	if (description instanceof String)  {
		log.debug "Hue Advanced Bulb/Group stringToMap - ${map}"
		map = stringToMap(description)
	}

	if (map?.name && map?.value) {
		results << createEvent(name: "${map?.name}", value: "${map?.value}")
	}
	results
}

// handle commands
void on() {
	log.trace parent.on(this, state.deviceType)
	sendEvent(name: "deviceSwitch", value: "${state.deviceType}On", displayed: false)
	sendEvent(name: "switch", value: "on")
}

void off() {
	log.trace parent.off(this, state.deviceType)
	sendEvent(name: "deviceSwitch", value: "${state.deviceType}Off", displayed: false)
	sendEvent(name: "switch", value: "off")
}

void nextLevel() {
	def level = device.latestValue("level") as Integer ?: 0
	if (level <= 100) {
		level = Math.min(25 * (Math.round(level / 25) + 1), 100) as Integer
	}
	else {
		level = 25
	}
	setLevel(level)
}

void setLevel(percent) {
    log.debug "Executing 'setLevel'"
    if (verifyPercent(percent)) {
        parent.setLevel(this, percent, state.deviceType)
        sendEvent(name: "level", value: percent, descriptionText: "Level has changed to ${percent}%")
		sendEvent(name: "deviceSwitch", value: "${state.deviceType}On", displayed: false)
		sendEvent(name: "switch", value: "on")
    }
}

void setSaturation(percent) {
    log.debug "Executing 'setSaturation'"
    if (verifyPercent(percent)) {
        parent.setSaturation(this, percent, state.deviceType)
        sendEvent(name: "saturation", value: percent, displayed: false)
    }
}

void setHue(percent) {
    log.debug "Executing 'setHue'"
    if (verifyPercent(percent)) {
        parent.setHue(this, percent, state.deviceType)
        sendEvent(name: "hue", value: percent, displayed: false)
    }
}

void setColor(value) {
    log.debug "setColor: ${value}, $this"
    def events = []
    def validValues = [:]

    if (verifyPercent(value.hue)) {
        events << createEvent(name: "hue", value: value.hue, displayed: false)
        validValues.hue = value.hue
    }
    if (verifyPercent(value.saturation)) {
        events << createEvent(name: "saturation", value: value.saturation, displayed: false)
        validValues.saturation = value.saturation
    }
    if (value.hex != null) {
        if (value.hex ==~ /^\#([A-Fa-f0-9]){6}$/) {
            events << createEvent(name: "color", value: value.hex)
            validValues.hex = value.hex
        } else {
            log.warn "$value.hex is not a valid color"
        }
    }
    if (verifyPercent(value.level)) {
        events << createEvent(name: "level", value: value.level, descriptionText: "Level has changed to ${value.level}%")
        validValues.level = value.level
    }
    if (value.switch == "off" || (value.level != null && value.level <= 0)) {
        events << createEvent(name: "switch", value: "off")
        validValues.switch = "off"
    } else {
        events << createEvent(name: "switch", value: "on")
        validValues.switch = "on"
    }
    if (!events.isEmpty()) {
        parent.setColor(this, validValues, state.deviceType)
    }
    events.each {
        sendEvent(it)
    }
}

void reset() {
    log.debug "Executing 'reset'"
    def value = [level:100, saturation:56, hue:23]
    setAdjustedColor(value)
    parent.poll()
}

void setAdjustedColor(value) {
    if (value) {
        log.trace "setAdjustedColor: ${value}"
        def adjusted = value + [:]
        adjusted.hue = adjustOutgoingHue(value.hue)
        // Needed because color picker always sends 100
        adjusted.level = null
        setColor(adjusted)
    } else {
        log.warn "Invalid color input"
    }
}

void setColorTemperature(value) {
    if (value) {
        log.trace "setColorTemperature: ${value}k"
        parent.setColorTemperature(this, value, state.deviceType)
        sendEvent(name: "colorTemperature", value: value)
		sendEvent(name: "deviceSwitch", value: "${state.deviceType}On", displayed: false)
		sendEvent(name: "switch", value: "on")
    } else {
        log.warn "Invalid color temperature"
    }
}

void refresh() {
    log.debug "Executing 'refresh'"
    parent.manualRefresh()
}

def adjustOutgoingHue(percent) {
	def adjusted = percent
	if (percent > 31) {
		if (percent < 63.0) {
			adjusted = percent + (7 * (percent -30 ) / 32)
		}
		else if (percent < 73.0) {
			adjusted = 69 + (5 * (percent - 62) / 10)
		}
		else {
			adjusted = percent + (2 * (100 - percent) / 28)
		}
	}
	log.info "percent: $percent, adjusted: $adjusted"
	adjusted
}

def verifyPercent(percent) {
    if (percent == null)
        return false
    else if (percent >= 0 && percent <= 100) {
        return true
    } else {
        log.warn "$percent is not 0-100"
        return false
    }
}

void initialize(deviceType) {
	state.deviceType = deviceType
}
