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

//	Serial.println("Action Kommand");
//	Serial.flush();

	int seperatorChannel = 0;
	//int seperatorStepTime = 0;
	int seperatorSignalLength = 0;
	char c;
	// I ist an 0ter Stelle
	unsigned int indexOfLastSeperator = 0;

	for (unsigned int i = 1; i < command->length(); i++) {
		c = command->charAt(i);
		if (c == 'T') {
			if (seperatorSignalLength || (indexOfLastSeperator + 1 == i)) {
				//	Kommando fehlerhaft
				return;
			}
			seperatorSignalLength = i;
			indexOfLastSeperator = i;
//		} else if (c == 'S') {
//			if (seperatorStepTime || (indexOfLastSeperator + 1 == i)) {
//				//	Kommando fehlerhaft
//				return;
//			}
//			seperatorStepTime = i;
//			indexOfLastSeperator = i;
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

	if (seperatorChannel /*&& seperatorStepTime*/ && seperatorSignalLength) {
		//	Kommando syntaktisch korrekt
//		Serial.println("Kommando korrekt");
//		Serial.flush();

		String intensity_s = command->substring(1, seperatorChannel);

		int intensity = intensity_s.toInt();
//		Serial.print("Intensity:");
//		Serial.println(intensity);
//		Serial.flush();
		String channel_s = command->substring(seperatorChannel + 1,
				/*seperatorStepTime*/ seperatorSignalLength);

		int channel = channel_s.toInt();
//		Serial.print("channel: ");
//		Serial.println(channel);
//		Serial.flush();
//		String stepTime_s = command->substring(seperatorStepTime + 1,
//				seperatorSignalLength);
//		int stepTime = stepTime_s.toInt();

		String signalLength_s = command->substring(seperatorSignalLength + 1,
				command->length());
		int signalLength = signalLength_s.toInt();
//		Serial.print("Signallaenge:");
//		Serial.println(signalLength);
//		Serial.flush();

//		emsChannels[channel]->setIncreaseDecreaseTime(stepTime);

//		Serial.print("Steptime:");
//		Serial.println(stepTime);
//		Serial.flush();

		if (channel < size) {
			emsChannels[channel]->activate();
			emsChannels[channel]->setIntensity(intensity, false, signalLength);
		}
	}
}

void EMSSystem::setOption(String *option) {
	char secChar = option->charAt(2);
	int channel = -1;
	int value = -1;
	switch (option->charAt(1)){
	case 'C':
		if(secChar == 'T' && getChannelAndValue(option, &channel, &value)){
			//set changeTime
			emsChannels[channel]->setIncreaseDecreaseTime(value);
		}
		break;
	case 'M':
		if(secChar == 'A' && getChannelAndValue(option, &channel, &value)){
			//Kalibrierung Maximalwert
			emsChannels[channel]->setMaxIntensity(value);
		}else if (secChar == 'I' && getChannelAndValue(option, &channel, &value)){
			//Kalibrierung Minimalwert
			emsChannels[channel]->setMinIntensity(value);
		}
		break;

	default: break;
	}

}

bool EMSSystem::getChannelAndValue(String *option, int *channel, int *value){
	int left = option->indexOf('[');
	int right = option->lastIndexOf(']');
	int seperator = option->indexOf(',', left + 1);

	if(left < seperator && seperator < right && left!= -1 && right != -1 && seperator !=-1){
		String help = option->substring(left + 1 ,seperator);
		(*channel) = help.toInt() ;
		help = option->substring(seperator + 1, right);
		(*value) = help.toInt();

		//Parsen war erfolgreich
		//Überprüfen ob es diesen Kanal ueberhaupt gibt.
		return isInRange((*channel));
	}
	//Parsen war nicht erfolgreich
	return false;
}

bool EMSSystem::isInRange(int channel){
	return (channel >= 0 && channel < size);
}

int EMSSystem::check() {
	int stopCount = 0;
	for (int i = 0; i < size; i++) {
		stopCount = stopCount + emsChannels[i]->check();
	}
	return stopCount;
}

void EMSSystem::doCommand(String *command) {
	if (command->length() > 0) {
		if (command->charAt(0) == ACTION) {
			doActionCommand(command);
		} else if (command->charAt(0) == OPTION) {
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

