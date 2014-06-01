/*
 * EMSSystem.cpp
 *
 *  Created on: 26.05.2014
 *      Author: Tim Dünte
 */

#include "EMSSystem.h"

EMSSystem::EMSSystem(int channels) {
	emsChannels = (EMSChannel**) malloc(channels * sizeof(EMSChannel*));
	channelCount = channels;
	size = 0;
}

EMSSystem::~EMSSystem() {
	// TODO Auto-generated destructor stub
	free(emsChannels);
}

void EMSSystem::addChannelToSystem(EMSChannel *emsChannel) {
	if (size < channelCount) {
		emsChannels[size] = emsChannel;
		size++;
	}
}

void EMSSystem::doActionCommand(String *command) {
//	Kommand wird analysiert um zu testen, dass keine defekten Kommandos ausgeführt werden.
//	Es sind nur die Zeichen T, S, C und 0-9 erlaubt. Wenn das Format verletzt wird, wird das Kommando nicht ausgeführt.
//	Es muss mindestens ein Zeichen zwischen den Trennzeichen liegen.

	Serial.println("Action Kommand");
	Serial.flush();

	int seperatorChannel = 0;
	int seperatorStepTime = 0;
	int seperatorSignaleLength = 0;
	char c;
	// I ist an 0ter Stelle
	unsigned int indexOfLastSeperator = 0;

	for (unsigned int i = 1; i < command->length(); i++) {
		c = command->charAt(i);
		if (c == 'T') {
			if (seperatorSignaleLength || (indexOfLastSeperator + 1 == i)) {
				//	Kommando fehlerhaft
				return;
			}
			seperatorSignaleLength = i;
			indexOfLastSeperator = i;
		} else if (c == 'S') {
			if (seperatorStepTime || (indexOfLastSeperator + 1 == i)) {
				//	Kommando fehlerhaft
				return;
			}
			seperatorStepTime = i;
			indexOfLastSeperator = i;
		} else if (c == 'C') {
			if (seperatorChannel || (indexOfLastSeperator + 1 == i)) {
				//	Kommando fehlerhaft
				return;
			}
			seperatorChannel = i;
			indexOfLastSeperator = i;
		} else if (!(c >= '0' && c <= '9')) {

			//	Kommando fehlerhaft
			return;
		}
	}

	if (seperatorChannel && seperatorStepTime && seperatorSignaleLength) {
		//	Kommando syntaktisch korrekt
		Serial.println("Kommando korrekt");
		Serial.flush();

		String intensity_s = command->substring(1, seperatorChannel);

		int intensity = intensity_s.toInt();
		Serial.print("Intensity:");
		Serial.println(intensity);
		Serial.flush();
		String channel_s = command->substring(seperatorChannel + 1,
				seperatorStepTime);

		int channel = channel_s.toInt();
		Serial.print("channel: ");
		Serial.println(channel);
		Serial.flush();
		String stepTime_s = command->substring(seperatorStepTime + 1,
				seperatorSignaleLength);
		int stepTime = stepTime_s.toInt();

		String signalLength_s = command->substring(seperatorSignaleLength + 1,
				command->length());
		int signalLength = signalLength_s.toInt();
		Serial.print("Signallaenge:");
		Serial.println(signalLength);
		Serial.flush();

		emsChannels[channel]->setIncreaseDecreaseTime(stepTime);

		Serial.print("Steptime:");
		Serial.println(stepTime);
		Serial.flush();

		if (channel < size) {
			emsChannels[channel]->activate();
			emsChannels[channel]->setIntensity(intensity, false, signalLength);
		}
	}
}

void EMSSystem::setOption(String *option) {

}

void EMSSystem::check() {
	for (int i = 0; i < size; i++) {
		emsChannels[i]->check();
	}
}

void EMSSystem::doCommand(String *command) {
	if (command->length() > 0) {
		if (command->charAt(0) == 'I') {
			doActionCommand(command);
		} else if (command->charAt(0) == 'O') {
			setOption(command);
		} else {
			Serial.print("Unknown command: ");
			Serial.flush();
			Serial.println((*command));
			Serial.flush();
		}
	}
}

void EMSSystem::start() {
	EMSChannel::start();
}

void EMSSystem::end() {
	EMSChannel::end();
}

