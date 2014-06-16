/*
 * EMSSystem.h
 *
 *  Created on: 26.05.2014
 *      Author: Tim Dünte
 */

#ifndef EMSSYSTEM_H_
#define EMSSYSTEM_H_

#include "EMSChannel.h"

#define ACTION 'I'
#define OPTION '_'

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
	virtual bool getChannelAndValue(String *option, int *channel, int *value);

private:
	EMSChannel **emsChannels;
	int channelCount;
	int size;
	bool isInRange(int channel);
};

#endif /* EMSSYSTEM_H_ */
