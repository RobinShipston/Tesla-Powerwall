/**
 *  Tesla Powerwall II
 *
 *  Copyright 2018 Robin Shipston
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
 
 /* IMPORTANT
 *  Look for http://MYPOWERWALL.MYDOMAIN.COM:MYPORTNUMBER in two places below
 *  Unfortunately this device handler only works with FQDNs - the method used will not work with an IP address due to SmartThings restrictions
 *  There is a way to use an IP address by writing a device handler SmartApp, but I lost the will to live trying to understand the awful SmartThings documentation
 *  So you'll need to setup a FQDN, and port forwarding on your router, along with a firewall rule to restrict traffic to local only
 */
 
 
metadata {
	definition (name: "Tesla Powerwall II", namespace: "RobinShipston", author: "Robin Shipston") {
		capability "Battery"
		capability "Energy Meter"
        capability "Power Meter"
        capability "Power Source"
	}

	preferences {
        
    }


	simulator {
		// TODO: define status and reply messages here
	}

	tiles(scale: 2) {
		// TODO: define your main and details tiles here

         valueTile("solar", "device.solar", width: 2, height: 2) {
			state("solar", label:'${currentValue}kW', unit:"kW",
				backgroundColors:[
					[value: 0.1, color: "#aaaaaa"],
					[value: 1, color: "#ffcc00"],
					[value: 3, color: "#ff9900"],
                    [value: 4, color: "#ff0000"]
				]
			)
		}       
         standardTile("blank", "device.blank", width: 2, height: 2) {
    		state ("blank", label: "", backgroundColor:"#ffffff")
		}
       
        //level of charge
        valueTile("battery", "device.battery", width: 2, height: 2) {
			state("battery", label:' Charge  ${currentValue}%', unit:"%",
				backgroundColors:[
					[value: 12.5, color: "#153591"],
					[value: 25, color: "#1e9cbb"],
					[value: 37.5, color: "#90d2a7"],
					[value: 50, color: "#44b621"],
					[value: 62.5, color: "#f1d801"],
					[value: 75, color: "#d04e00"],
					[value: 87.5, color: "#bc2323"]
				]
			)
		}

        valueTile("load", "device.load", width: 2, height: 2) {
			state("load", label:'${currentValue}kW', unit:"kW",
				backgroundColors:[
					[value: 0.3, color: "#90d2a7"],
					[value: 1, color: "#44b621"],
					[value: 2, color: "#f1d801"],
					[value: 3, color: "#d04e00"],
					[value: 4, color: "#bc2323"]
				]
			)
		}       
        valueTile("grid", "device.grid", width: 2, height: 2) {
			state("grid", label:'${currentValue}kW', unit:"kW",
				backgroundColors:[
                	[value: 0, color: "#FF0000"],
					[value: 0.250, color: "#aaaaaa"],
					[value: 0.500, color: "#1e9cbb"],
					[value: 0.750, color: "#90d2a7"],
					[value: 1, color: "#44b621"],
					[value: 2, color: "#f1d801"],
					[value: 3, color: "#d04e00"],
					[value: 4, color: "#bc2323"]
				]
			)
		}       
        valueTile("powerwall", "device.powerwall", width: 2, height: 2) {
			state("powerwall", label:'${currentValue}kW', unit:"kW",
				backgroundColors:[
					[value: 0.3, color: "#90d2a7"],
					[value: 1, color: "#44b621"],
					[value: 2, color: "#f1d801"],
					[value: 3, color: "#d04e00"],
					[value: 4, color: "#bc2323"]
				]
			)
		}              
        
        
        main(["battery", "grid"])
        details(["blank", "solar", "blank", "grid", "battery", "load", "blank", "powerwall", "blank"])       
	}
}

def installed() {
    log.debug "Installed with settings: ${settings}"
    initialize()
}


def updated() {
    log.debug "Updated with settings: ${settings}"
    initialize()
}


def initialize() {
    
    updateCharge()
    runEvery1Minute(updateCharge)
    
}

// parse events into attributes
def parse(String description) {

}


def updateCharge() {

	def name
	def params = [
        uri:  'http://MYPOWERWALL.MYDOMAIN.COM:MYPORTNUMBER',
        path: '/api/system_status/soe',
        contentType: 'application/json'      
    ]
    try {
        httpGet(params) {resp ->
        	
            log.debug "resp data: ${resp.data}"
            log.debug "battery charge: ${resp.data.percentage}"
            sendEvent(name: "battery", value: "${resp.data.percentage}")
          
        }
    } catch (e) {
        log.error "error: $e"
    }
 /// Get the meter values

 	params = [
        uri:  'http://MYPOWERWALL.MYDOMAIN.COM:MYPORTNUMBER',
        path: '/api/meters/aggregates',
        contentType: 'application/json'   
    ]
    try {
        httpGet(params) {resp ->
            //log.debug "resp data: ${resp.data}"
            log.debug "load: ${Math.round(resp.data.load.instant_power/100)/10}"
            log.debug "site: ${Math.round(resp.data.site.instant_power/100)/10}"
            log.debug "battery: ${Math.round(resp.data.battery.instant_power/100)/10}"
            log.debug "solar: ${Math.round(resp.data.solar.instant_power/100)/10}"
            sendEvent(name: "solar", value: "${Math.round(resp.data.solar.instant_power/100)/10}")
            sendEvent(name: "load", value: "${Math.round(resp.data.load.instant_power/100)/10}")
            sendEvent(name: "grid", value: "${Math.round(resp.data.site.instant_power/100)/10}")
            sendEvent(name: "powerwall", value: "${Math.round(resp.data.battery.instant_power/100)/10}")  
        }
    } catch (e) {
        log.error "error: $e"
    }   
}




