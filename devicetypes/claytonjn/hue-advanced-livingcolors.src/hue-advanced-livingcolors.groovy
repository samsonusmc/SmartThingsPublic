/**
 *  Hue Advanced -CD- LivingColors
 *
 *  Philips Hue Type "Color Light"
 *
 *  Author: claytonjn
 */

// for the UI
metadata {
	// Automatically generated. Make future change here.
	definition (name: "Hue Advanced -CD- LivingColors", namespace: "claytonjn", author: "claytonjn") {
		capability "Switch Level"
		capability "Actuator"
		capability "Color Control"
		capability "Switch"
		capability "Refresh"
		capability "Sensor"

		command "setAdjustedColor"
        command "reset"
        command "refresh"
		command "setTransitionTime"
		command "alert"
		command "colorloopOn"
		command "colorloopOff"
        command "setColorLoopOn"
        command "setColorLoopOff"
		command "bri_inc"
		command "sat_inc"
		command "hue_inc"
		command "ct_inc"
		command "xy_inc"
		command "enableCDBrightness"
        command "disableCDBrightness"
		command "enableCDColor"
		command "disableCDColor"
		command "tileSetLevel"
		command "tileSetAdjustedColor"
        command "tileReset"

		attribute "transitionTime", "NUMBER"
		attribute "xy", "json_object"
		attribute "effect", "enum", ["none", "colorloop"]
		attribute "colormode", "enum", ["hs", "xy"]
		attribute "reachable", "enum", ["true", "false"]
		attribute "cdBrightness", "enum", ["true", "false"]
		attribute "cdColor", "enum", ["true", "false"]
	}

	simulator {
		// TODO: define status and reply messages here
	}

	tiles (scale: 2){
		multiAttributeTile(name:"rich-control", type: "lighting", width: 6, height: 4, canChangeIcon: true){
			tileAttribute ("device.switch", key: "PRIMARY_CONTROL") {
				attributeState "on", label:'${name}', action:"switch.off", icon:"st.lights.philips.hue-single", backgroundColor:"#79b821", nextState:"turningOff"
				attributeState "off", label:'${name}', action:"switch.on", icon:"st.lights.philips.hue-single", backgroundColor:"#ffffff", nextState:"turningOn"
				attributeState "turningOn", label:'${name}', action:"switch.off", icon:"st.lights.philips.hue-single", backgroundColor:"#79b821", nextState:"turningOff"
				attributeState "turningOff", label:'${name}', action:"switch.on", icon:"st.lights.philips.hue-single", backgroundColor:"#ffffff", nextState:"turningOn"
			}
			tileAttribute ("device.level", key: "SLIDER_CONTROL") {
				attributeState "level", action:"tileSetLevel", range:"(0..100)"
            }
			tileAttribute ("device.color", key: "COLOR_CONTROL") {
				attributeState "color", action:"tileSetAdjustedColor"
			}
		}

		standardTile("reset", "device.reset", height: 2, width: 2, inactiveLabel: false, decoration: "flat") {
			state "default", label:"Reset", action:"tileReset", icon:"st.lights.philips.hue-single"
		}

		standardTile("refresh", "device.refresh", height: 2, width: 2, inactiveLabel: false, decoration: "flat") {
			state "default", label:"", action:"refresh.refresh", icon:"st.secondary.refresh"
		}

		controlTile("transitionTimeSliderControl", "device.transitionTime", "slider", width: 4, height: 1, inactiveLabel: false, range:"(0..10)") {
			state "setTransitionTime", action: "setTransitionTime"
		}
		valueTile("transTime", "device.transitionTime", inactiveLabel: false, decoration: "flat", width: 2, height: 1) {
			state "transitionTime", label: 'Transition:              ${currentValue} s'
		}

		standardTile("effectControl", "device.effect", height: 2, width: 2, inactiveLabel: false, decoration: "flat") {
			state "none", label:"Colorloop Off", action:"setColorLoopOn", nextState: "updating", icon:"https://raw.githubusercontent.com/claytonjn/SmartThingsPublic/Hue-Advanced-Development/smartapp-icons/hue-advanced/png/colorloop-off.png"
			state "colorloop", label:"Colorloop On", action:"setColorLoopOff", nextState: "updating", icon:"https://raw.githubusercontent.com/claytonjn/SmartThingsPublic/Hue-Advanced-Development/smartapp-icons/hue-advanced/png/colorloop-on.png"
			state "updating", label:"Working", icon: "st.secondary.secondary"
		}

		valueTile("colormode", "device.colormode", inactiveLabel: false, decoration: "flat", width: 4, height: 1) {
			state "default", label: 'Colormode: ${currentValue}'
		}

		valueTile("reachable", "device.reachable", inactiveLabel: false, decoration: "flat", width: 4, height: 1) {
			state "true", label: 'Reachable'
			state "false", label: 'Not Reachable!'
		}

		valueTile("cdBrightnessControl", "device.cdBrightness", inactiveLabel: false, decoration: "flat", width: 3, height: 1) {
			state "true", label: "Circadian Brightness On", action: "disableCDBrightness", nextState: "updating"
			state "false", label: "Circadian Brightness Off", action: "enableCDBrightness", nextState: "updating"
			state "updating", label: "Working"
		}

		valueTile("cdColorControl", "device.cdColor", inactiveLabel: false, decoration: "flat", width: 3, height: 1) {
			state "true", label: "Circadian Color On", action: "disableCDColor", nextState: "updating"
			state "false", label: "Circadian Color Off", action: "enableCDColor", nextState: "updating"
			state "updating", label: "Working"
		}

		main(["rich-control"])
		details(["rich-control", "transitionTimeSliderControl", "transTime", "cdBrightnessControl", "cdColorControl", "effectControl", "colormode", "reachable", "reset", "refresh"])
	}
}

// parse events into attributes
def parse(description) {
	log.debug "parse() - $description"
	def results = []

	def map = description
	if (description instanceof String)  {
		log.debug "Hue Advanced -CD- LivingColors stringToMap - ${map}"
		map = stringToMap(description)
	}

	if (map?.name && map?.value) {
		results << createEvent(name: "${map?.name}", value: "${map?.value}")
	}
	results
}

// handle commands
void setTransitionTime(transitionTime) {
	log.trace "Transition time set to ${transitionTime}"
	sendEvent(name: "transitionTime", value: transitionTime)
}

void on(transitionTime = device.currentValue("transitionTime")) {
	if(transitionTime == null) { transitionTime = device.currentValue("transitionTime") ?: parent.getSelectedTransition() ?: 1 }

	
	log.trace parent.on(this, transitionTime, deviceType)
	sendEvent(name: "switch", value: "on")
    if(state.colorLoopState == "on"){colorloopOn()}
    if(state.colorLoopState == "off"){colorloopOff()}
}

void off(transitionTime = device.currentValue("transitionTime")) {
	if(transitionTime == null) { transitionTime = device.currentValue("transitionTime") ?: parent.getSelectedTransition() ?: 1 }

	colorloopOff()
	log.trace parent.off(this, transitionTime, deviceType)
	sendEvent(name: "switch", value: "off")
}

void nextLevel(transitionTime = device.currentValue("transitionTime"), disableCDB = false) {
	if(transitionTime == null) { transitionTime = device.currentValue("transitionTime") ?: parent.getSelectedTransition() ?: 1 }

	def level = device.latestValue("level") as Integer ?: 0
	if (level <= 100) {
		level = Math.min(25 * (Math.round(level / 25) + 1), 100) as Integer
	}
	else {
		level = 25
	}
	setLevel(level, transitionTime, disableCDB)
}

void tileSetLevel(percent) {
	setLevel(percent, null, true)
}

void setLevel(percent, transitionTime = device.currentValue("transitionTime"), disableCDB = false) {
	if(transitionTime == null) { transitionTime = device.currentValue("transitionTime") ?: parent.getSelectedTransition() ?: 1 }

    log.debug "Executing 'setLevel'"
    if (verifyPercent(percent)) {
		if (percent == 0) {
			off()
		} else {
	        parent.setLevel(this, percent, transitionTime, deviceType)
			if(disableCDB == true) { disableCDBrightness() }
	        sendEvent(name: "level", value: percent, descriptionText: "Level has changed to ${percent}%")
	        sendEvent(name: "switch", value: "on")
		}
    }
}

void setSaturation(percent, transitionTime = device.currentValue("transitionTime"), disableCDC = false) {
	if(transitionTime == null) { transitionTime = device.currentValue("transitionTime") ?: parent.getSelectedTransition() ?: 1 }

	colorloopOff()
    log.debug "Executing 'setSaturation'"
    if (verifyPercent(percent)) {
        parent.setSaturation(this, percent, transitionTime, deviceType)
		if(disableCDC == true) { disableCDColor() }
        sendEvent(name: "saturation", value: percent)
		sendEvent(name: "colormode", value: "hs")
    }
}

void setHue(percent, transitionTime = device.currentValue("transitionTime"), disableCDC = false) {
	if(transitionTime == null) { transitionTime = device.currentValue("transitionTime") ?: parent.getSelectedTransition() ?: 1 }

	colorloopOff()
    log.debug "Executing 'setHue'"
    if (verifyPercent(percent)) {
        parent.setHue(this, percent, transitionTime, deviceType)
		if(disableCDC == true) { disableCDColor() }
        sendEvent(name: "hue", value: percent)
		sendEvent(name: "colormode", value: "hs")
    }
}

void setColor(value) {
    log.debug "setColor: ${value}, $this"
	colorloopOff()
    def events = []
    def validValues = [:]

	if (value.transitionTime) { validValues.transitionTime = value.transitionTime }
	else {
		def transitionTime = (device.currentValue("transitionTime")) ?: parent.getSelectedTransition() ?: 3
		validValues.transitionTime = transitionTime
	}
    if (verifyPercent(value.hue)) {
        events << createEvent(name: "hue", value: value.hue)
		events << createEvent(name: "colormode", value: "hs")
        validValues.hue = value.hue
    }
    if (verifyPercent(value.saturation)) {
        events << createEvent(name: "saturation", value: value.saturation)
		events << createEvent(name: "colormode", value: "hs")
        validValues.saturation = value.saturation
    }
	if (value.xy != null) {
		if (value.xy[0] < 0 || value.xy[0] > 1 || value.xy[1] < 0 || value.xy[1] > 1) {
			log.warn "$value.xy is not a valid color"
		} else if (verifyPercent(value.level)) {
			value.xy[0] = value.xy[0].round(4)
			value.xy[1] = value.xy[1].round(4)
			events << createEvent(name: "xy", value: groovy.json.JsonOutput.toJson([value.xy[0], value.xy[1]]))
			events << createEvent(name: "color", value: parent.getXYtoHex(value.xy, value.level))
			events << createEvent(name: "colormode", value: "xy")
			validValues.xy = value.xy
		}
	}
    if (value.hex != null) {
        if (value.hex ==~ /^\#([A-Fa-f0-9]){6}$/) {
            events << createEvent(name: "color", value: value.hex)
			def xy = parent.getHextoXY(value.hex)
			events << createEvent(name: "xy", value: groovy.json.JsonOutput.toJson([xy[0], xy[1]]))
			events << createEvent(name: "colormode", value: "xy")
            validValues.hex = value.hex
        } else {
            log.warn "$value.hex is not a valid color"
        }
    }
    if (verifyPercent(value.level) && value.level > 0) {
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
	if (value.disableCDBrightness == true) { disableCDBrightness() }
	if (value.disableCDColor == true) { disableCDColor() }
    if (!events.isEmpty()) {
        parent.setColor(this, validValues, deviceType)
    }
    events.each {
        sendEvent(it)
    }
}

private tileReset() {
    reset(null, true)
}

void reset(transitionTime = device.currentValue("transitionTime"), disableCDB = false, disableCDC = false) {
    if(transitionTime == null) { transitionTime = device.currentValue("transitionTime") ?: parent.getSelectedTransition() ?: 1 }

    log.debug "Executing 'reset'"
    def value = [level:100, saturation:18, hue:8, transitiontime:transitionTime, disableCDBrightness:disableCDB, disableCDColor:disableCDC]
	colorloopOff()
    setAdjustedColor(value)
    parent.poll()
}

void tileSetAdjustedColor(value) {
	value.disableCDColor = true
	setAdjustedColor(value)
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

void refresh() {
    log.debug "Executing 'refresh'"
    parent.manualRefresh()
}

void alert(alert) {
	log.debug "Executing 'alert'"
	parent.setAlert(this, alert, deviceType)
}

void setColorLoopOn(){
	state.colorLoopState = "on"
    colorloopOn()
}

void setColorLoopOff(){
	state.colorLoopState = "off"
    colorloopOff()
}


void colorloopOn() {
	log.debug "Executing 'colorloopOn'"
	if(device.latestValue("switch") != "on") { on() }
	parent.setEffect(this, "colorloop", deviceType)
	sendEvent(name: "effect", value: "colorloop", descriptionText: "Colorloop has been turned on")
}

void colorloopOff() {
	log.debug "Executing 'colorloopOff'"
	parent.setEffect(this, "none", deviceType)
	sendEvent(name: "effect", value: "none", descriptionText: "Colorloop has been turned off")
}

void bri_inc(value) {
	log.debug "Executing 'bri_inc'"
	parent.bri_inc(this, value, deviceType)
}

void sat_inc(value) {
	log.debug "Executing 'sat_inc'"
	parent.sat_inc(this, value, deviceType)
}

void hue_inc(value) {
	log.debug "Executing 'hue_inc'"
	parent.hue_inc(this, value, deviceType)
}

void ct_inc(value) {
	log.debug "Executing 'ct_inc'"
	parent.ct_inc(this, value, deviceType)
}

void xy_inc(x, y) {
	x = x.round(4)
	y = y.round(4)
	log.debug "Executing 'xy_inc'"
	parent.xy_inc(this, [x, y], deviceType)
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
	setTransitionTime(parent.getSelectedTransition())
	sendEvent(name: "cdBrightness", value: "true", displayed: false)
	sendEvent(name: "cdColor", value: "true", displayed: false)
}

def getDeviceType() { return "lights" }

void setHADeviceHandler(circadianDaylightIntegration) {
	if (circadianDaylightIntegration == true) {
		setDeviceType("Hue Advanced -CD- LivingColors")
	} else {
		setDeviceType("Hue Advanced LivingColors")
	}
}

void enableCDBrightness() {
	log.debug "Executing 'enableCDBrightness'"
	sendEvent(name: "cdBrightness", value: "true", descriptionText: "Circadian Brightness has been enabled")
}

void disableCDBrightness() {
	log.debug "Executing 'disableCDBrightness'"
	sendEvent(name: "cdBrightness", value: "false", descriptionText: "Circadian Brightness has been disabled")
}

void enableCDColor() {
	log.debug "Executing 'enableCDColor'"
	sendEvent(name: "cdColor", value: "true", descriptionText: "Circadian Color has been enabled")
}

void disableCDColor() {
	log.debug "Executing 'disableCDColor'"
	sendEvent(name: "cdColor", value: "false", descriptionText: "Circadian Color has been disabled")
}
