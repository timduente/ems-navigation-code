/*
 Zwei LED koennen ueber eine serielle Bluetoothverbindung an und ausgeschaltet werden.
 */
#include <Arduino.h>
#include "Bluetooth.h"
#include "EMSChannel.h"
#include "EMSSystem.h"


//Bietet die Moeglichkeit einen weiteren seriellen Anschluss zu erstellen
#include "SoftwareSerial/SoftwareSerial.h"

// Pin 13 ist mit dem zweiten Relais verbunden. Pin 12 mit dem ersten.
const int opto1 = 6;
const int opto2 = 7;
const int opto3 = 4;
const int opto4 = 5;

//Pins fuer die serielle Verbindung zum Bluetoothchip
const int rxPin = 2; //Receive
const int txPin = 3; //Transfer

//Pin für den ChipSelect Port des Dig.Potis
const int _csPoti = 10;
const int _csPoti2 = 9;

SoftwareSerial bluetooth(rxPin, txPin); //Neuer Serieller Port
EMSChannel emsChannel1(opto1, opto2, _csPoti);
EMSChannel emsChannel2(opto3, opto4, _csPoti2);
EMSSystem emsSystem(2);

// the setup routine runs once when you press reset:
void setup() {
	//Initialierung einer seriellen Verbindung zum PC
	Serial.begin(9600);
	//Serial.println("Serial bereit");
	delay(550);
	initBluetoothChip();

	emsSystem.addChannelToSystem(&emsChannel1);
	emsSystem.addChannelToSystem(&emsChannel2);
	EMSSystem::start();
}

// the loop routine runs over and over again forever:
void loop() {
	if (bluetooth.available() > 3) {
		String command = bluetooth.readStringUntil(';');

		Serial.println(command);
		Serial.flush();


		emsSystem.doCommand(&command);

		//Serial.print("Kommando: ");


		//char c = (char) bluetooth.read();
		//Alle Daten die der Bluetoothchip sendet werden an die serielle Verbindung zum PC gesendet

		//bluetooth.write(c);
	}

	//Überprüft ob irgendwo ein Signal beendet werden muss.
	emsSystem.check();

	if (Serial.available()) {
		char c = (char) Serial.read();
		doCommand(c);
		//Alle Daten die ich in der seriellen Konsole eingebe werden an den Bluetoothchip gesendet.
		//bluetooth.write(c);
	}
}

void initBluetoothChip() {
	//Dieser komplette Block ist im Moment noch notwendig um eine serielle Verbindung zum Chip aufbauen zu kÃ¶nnen.
	bluetooth.begin(115200);        // The Bluetooth Mate defaults to 115200bps
	delay(320);                     // IMPORTANT DELAY! (Minimum ~276ms)
	bluetooth.print("$$$");         // Enter command mode
	delay(15);                      // IMPORTANT DELAY! (Minimum ~10ms)
	bluetooth.println("U,9600,N"); // Temporarily Change the baudrate to 9600, no parity
	bluetooth.println("---");
	bluetooth.begin(9600);          // Start bluetooth serial at 9600
	delay(320);
	//bluetooth.println("Bluetooth bereit");
}

void doCommand(char c) {
	if (c == '1') {
		emsChannel1.setIntensity(0);
		emsChannel2.setIntensity(0);
	} else if (c == '2') {
		emsChannel1.setIntensity(25);
		emsChannel2.setIntensity(25);
	} else if (c == '3') {
		emsChannel1.setIntensity(50);
		emsChannel2.setIntensity(50);
	} else if (c == '4') {
		emsChannel1.setIntensity(75);
		emsChannel2.setIntensity(75);
	} else if (c == '5') {
		emsChannel1.setIntensity(100);
		emsChannel2.setIntensity(100);
	} else if (c == '6') {
		emsChannel1.setIntensity(127);
		emsChannel2.setIntensity(127);
	} else if (c == '9') {
		if (emsChannel1.isActivated()) {
			emsChannel1.deactivate();
			Serial.println("Channel 1 inactive");
		} else {
			emsChannel1.activate();
			Serial.println("Channel 1 active");
		}
	} else if (c == '0') {
		if (emsChannel2.isActivated()) {
			emsChannel2.deactivate();
			Serial.println("Channel 2 inactive");
		} else {
			emsChannel2.activate();
			Serial.println("Channel 2 active");
		}
	}
}

