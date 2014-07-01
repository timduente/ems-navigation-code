/*
 * EMSChannel.cpp
 *
 *  Created on: 06.05.2014
 *      Author: Tim Dünte
 */
#include "EMSChannel.h"

//---------- constructor ----------------------------------------------------

EMSChannel::EMSChannel(uint8_t channel_to_Pads, uint8_t channel_to_Resistor,
		uint8_t poti_slave_select_pin) {
	intensity = 0;
	increaseDecrease = false;
	activated = false;

	maxIntensity = MAX_INTENSITY;
	minIntensity = MIN_INTENSITY;

	this->channel_to_Pads = channel_to_Pads;
	this->channel_to_Resistor = channel_to_Resistor;
	this->poti_slave_select_pin = poti_slave_select_pin;
	increaseDecreaseTime = 0;
	endTime = 0;

	digitalPoti = new McpDigitalPot(poti_slave_select_pin, 5000);

	pinMode(channel_to_Pads, OUTPUT);
	pinMode(channel_to_Resistor, OUTPUT);

	digitalWrite(channel_to_Resistor, HIGH);
	digitalWrite(channel_to_Pads, LOW);
}

EMSChannel::~EMSChannel() {
	delete (digitalPoti);
}

//---------- public ----------------------------------------------------
/*
 * Startet die Kommunikation mit dem digitalen Potentiometer. Muss auf jeden fall aufgerufen werden am besten in der setup().
 */
void EMSChannel::start() {
	//SPI.setClockDivider(SPI_CLOCK_DIV128);
	SPI.begin();
}

/*
 * Beendet die Kommunikation mit dem digitalen Potentiometer.
 */
void EMSChannel::end() {
	SPI.end();
}

/*
 * Schaltet das EMS-Signal auf die Pads. Vorher wird die Intensität auf 0 gefahren, damit man sich nicht erschreckt.
 */
void EMSChannel::activate() {
	//int oldIntensity = this->intensity;
	//setIntensity(0);

	digitalWrite(channel_to_Pads, HIGH);
	delay(MAX_ON_TIME_PHOTOMOS);
	digitalWrite(channel_to_Resistor, LOW);
	activated = true;
}
/*
 * Schaltet das EMS-Signal auf den Widerstand
 */
void EMSChannel::deactivate() {
	digitalWrite(channel_to_Resistor, HIGH);
	delay(MAX_ON_TIME_PHOTOMOS);
	digitalWrite(channel_to_Pads, LOW);
	activated = false;
}

/*
 * Fragt ab, ob das Signal über die Pads geht.
 */
bool EMSChannel::isActivated() {
	return activated;
}

/* Setzt die Intensität auf einer Skala von 0-100

 */
void EMSChannel::setIntensity(int intensity) {
	int resistorLevel = 127;
	if (intensity > 127) {
		this->intensity = 127;
	} else if (intensity < 0) {
		this->intensity = 0;
	} else {
		this->intensity = intensity;

	}
	resistorLevel = 127 - this->intensity;
	if (increaseDecrease) {
		int actPotiPosition = digitalPoti->getPosition(whiperIndex);
//		Serial.println("Position des Potis: ");
//		Serial.print(actPotiPosition);
//		Serial.println("ResistorLevel: ");
//		Serial.print(resistorLevel);
//		Serial.println();
		if (actPotiPosition >= resistorLevel) {
			digitalPoti->decrement(actPotiPosition - resistorLevel,
					increaseDecreaseTime);
//			for (int i = actPotiPosition; i > resistorLevel; i--) {
//				digitalPoti->decrement();
//				delay(increaseDecreaseTime);
//			}
		} else {
			digitalPoti->increment(resistorLevel - actPotiPosition,
					increaseDecreaseTime);
//			for (int i = actPotiPosition; i < resistorLevel; i++) {
//				digitalPoti->increment();
//				delay(increaseDecreaseTime);
//			}
		}
	} else {
		digitalPoti->setPosition(whiperIndex, intensity);
	}

//	Serial.print("Position des dig. Potis: ");
//	Serial.println(digitalPoti->getPosition(whiperIndex));

}

void EMSChannel::setIntensity(int intensity, bool increaseDecrease) {
	int resistorLevel = 127;
	if (intensity > 127) {
		this->intensity = 127;
	} else if (intensity < 0) {
		this->intensity = 0;
	} else {
		this->intensity = intensity;
	}
	resistorLevel = 127 - this->intensity;
	if (increaseDecrease) {
		int actPotiPosition = digitalPoti->getPosition(whiperIndex);
		if (actPotiPosition >= resistorLevel) {
			digitalPoti->decrement(actPotiPosition - resistorLevel,
					increaseDecreaseTime);
		} else {
			digitalPoti->increment(resistorLevel - actPotiPosition,
					increaseDecreaseTime);
		}
	} else {
		digitalPoti->setPosition(whiperIndex, intensity);
	}
}

void EMSChannel::setIntensity(int intensity, bool increaseDecrease,
		unsigned int time) {

	//Es wird noch auf einen Befehl gewartet, der noch nicht zu Ende ist. Der kommende Befehl wird NICHT ausgeführt.
	if (endTime) {
		return;
	}

	intensity = int ((maxIntensity - minIntensity) * intensity / 100.0f + 0.5f) + minIntensity;

	int resistorLevel = MAX_INTENSITY;
	if (intensity > MAX_INTENSITY) {
		this->intensity = MAX_INTENSITY;
	} else if (intensity < 0) {
		this->intensity = 0;
	} else {
		this->intensity = intensity;
	}
	resistorLevel = MAX_INTENSITY - this->intensity;
	if (increaseDecrease) {
		int actPotiPosition = digitalPoti->getPosition(whiperIndex);
		if (actPotiPosition >= resistorLevel) {
			digitalPoti->decrement(actPotiPosition - resistorLevel,
					increaseDecreaseTime);
		} else {
			digitalPoti->increment(resistorLevel - actPotiPosition,
					increaseDecreaseTime);
		}
	} else {
		digitalPoti->setPosition(whiperIndex, intensity);
	}
//	Serial.print("Stufe des dig. Potis: ");
//	Serial.println(digitalPoti->getPosition(whiperIndex));
//	Serial.flush();
	endTime = millis() + time;
}

/* Gibt die Intensität zurück 0-100

 */
int EMSChannel::getIntensity() {
	Serial.println("debugInfo: stufe: ");
	Serial.println(digitalPoti->getPosition(whiperIndex));
	return intensity;
}

/* Gibt zurück ob das Signal langsam ansteigt und abfällt.

 */
void EMSChannel::setSignalIncreaseDecrease(bool increaseDecreaseOn) {
	increaseDecrease = increaseDecreaseOn;
}

int EMSChannel::check() {
	if (endTime && endTime <= millis()) {
		setIntensity(0);
		deactivate();
		endTime = 0;
		return 1;
	}
	return 0;
}

/* Gibt zurück, ob das Signal anteigt und abfällt oder ob es schlagartig geschaltet wird. An und Absteigend ist default und wesentlich angenehmer.

 */
bool EMSChannel::isSignalIncreaseDecrease() {
	return increaseDecrease;
}

int EMSChannel::getIncreaseDecreaseTime() {
	return increaseDecreaseTime;
}

void EMSChannel::setIncreaseDecreaseTime(int increaseDecreaseTime) {
	this->increaseDecreaseTime = increaseDecreaseTime;
}

//maxIntensity in Prozent
void EMSChannel::setMaxIntensity(int maxIntensity){
	this->maxIntensity = int (MAX_INTENSITY * maxIntensity / 100.0f + 0.5f);
}

//minIntensity in Prozent
void EMSChannel::setMinIntensity(int minIntensity){
	this->minIntensity = int (MAX_INTENSITY * minIntensity / 100.0f + 0.5f);
}

//---------- private ----------------------------------------------------
