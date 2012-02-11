/*
 * ArithmeticKernel.h
 *
 *  Created on: Dec 20, 2011
 *      Author: ruedi
 */

#ifndef ARITHMETICKERNEL_H_
#define ARITHMETICKERNEL_H_

#include "sharedDOM/ArithmeticKernelData.h"

class ArithmeticKernel: public ArithmeticKernelData {
public:
	enum ArithmeticOperation {
		ArithmeticOperation_ADD,
		ArithmeticOperation_MUL,
		ArithmeticOperation_MULADD,
	};

	enum InstructionSet {
		SSE,
		SSEScalar,
		x87,
	};

	// solves base**exponent=result, with b unknown
	static double getBase(double exponent, double result);

private:

public:
	double result;

	void initialize() {
	}

	void run();

	void dispose() {
	}

};
#endif /* ARITHMETICKERNEL_H_ */
