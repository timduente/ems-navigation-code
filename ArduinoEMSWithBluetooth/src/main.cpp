/*
 * main.cpp
 *
 *  Created on: 22.04.2014
 *      Author: Tim Dünte
 */

#include <Arduino.h>
#include "Bluetooth.h"
//#include "old.h"

int main(void) {
  /* Must call init for arduino to work properly */

  init();
  setup();

  for (;;) {
	  loop();
  } // end for
} // end main
