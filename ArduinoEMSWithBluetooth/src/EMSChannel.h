/*
 * EMSChannel.h
 *
 *  Created on: 06.05.2014
 *      Author: Tim D�nte
 */

#ifndef EMSCHANNEL_H_
#define EMSCHANNEL_H_

#include <Arduino.h>

//Bindet die SPI Bibliothek ein. �ber SPI l�uft die Kommunikation mit dem digitalen Potentiometer
#include "SPI/SPI.h"
//Eine Klasse f�r digitale Potentiometer vom MCP
#include "McpDigitalPot.h"
#define MAX_ON_TIME_PHOTOMOS 2

class EMSChannel {

public:
	EMSChannel(uint8_t channel_to_Pads, uint8_t channel_to_Resistor, uint8_t poti_slave_select_pin);
	virtual ~EMSChannel();

	static void start();
	static void end();

	virtual void activate();
	virtual void deactivate();
	virtual bool isActivated();

	virtual void setIntensity(int intensity);
	virtual void setIntensity(int intensity, bool increaseDecrease);
	virtual void setIntensity(int intensity, bool increaseDecrease, unsigned int time);
	virtual int getIntensity();

	virtual void setSignalIncreaseDecrease(bool increaseDecreaseOn);
	virtual bool isSignalIncreaseDecrease();

	virtual int getIncreaseDecreaseTime();
	virtual void setIncreaseDecreaseTime(int increaseDecreaseTime);

	virtual void check();


private:
	//interne Variablen
	bool increaseDecrease;
	bool activated;
	int intensity;
	int increaseDecreaseTime;
	unsigned long int endTime;


	//Internes Objekt um das digitale Potentiometer anzusteuern
	McpDigitalPot* digitalPoti;
	static const int whiperIndex = 0;

	//Anschluesse:
	uint8_t channel_to_Pads;
	uint8_t channel_to_Resistor;
	uint8_t poti_slave_select_pin;

};

#endif /* EMSCHANNEL_H_ */
