/*
 * ArithmeticKernel.cpp
 *
 *  Created on: Dec 20, 2011
 *      Author: ruedi
 */

#include "ArithmeticKernel.h"

#include "typeRegistry/TypeRegisterer.h"
#include <cmath>
static TypeRegisterer<ArithmeticKernel> dummy;

double ArithmeticKernel::getBase(double exponent, double result){
	return exp(log(result)/exponent);
}
