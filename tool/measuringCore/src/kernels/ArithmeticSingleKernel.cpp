/*
 * ArithmeticKernel.cpp
 *
 *  Created on: Dec 20, 2011
 *      Author: ruedi
 */

#include "ArithmeticSingleKernel.h"

#include "typeRegistry/TypeRegisterer.h"
#include <cmath>
static TypeRegisterer<ArithmeticSingleKernel> dummy;

double ArithmeticSingleKernel::getBase(double exponent, double result){
	return exp(log(result)/exponent);
}
