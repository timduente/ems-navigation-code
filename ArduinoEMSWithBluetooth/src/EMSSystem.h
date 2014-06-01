/*
 * EMSSystem.h
 *
 *  Created on: 26.05.2014
 *      Author: Tim Dünte
 */

#ifndef EMSSYSTEM_H_
#define EMSSYSTEM_H_

#include "EMSChannel.h"

class EMSSystem {
public:
	EMSSystem(int channels);
	virtual ~EMSSystem();

	virtual void addChannelToSystem(EMSChannel *emsChannel);
	virtual void doCommand(String *command);
	virtual void check();


	static void start();
	static void end();

protected:
	virtual void doActionCommand(String *command);
	virtual void setOption(String *option);

private:
	EMSChannel **emsChannels;
	int channelCount;
	int size;
};

#endif /* EMSSYSTEM_H_ */
