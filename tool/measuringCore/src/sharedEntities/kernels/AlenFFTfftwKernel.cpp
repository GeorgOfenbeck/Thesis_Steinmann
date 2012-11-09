/*
 * AlenFFTfftwKernel.cpp
 *
 *  Created on: Nov 1, 2012
 *      Author: max
 */

#include "AlenFFTfftwKernel.h"
#include "AlenFFTfftw/ffts.h"

AlenFFTfftwKernel::~AlenFFTfftwKernel() {
	// TODO Auto-generated destructor stub
}

void AlenFFTfftwKernel::initialize() {

	input = new double[this->getBufferSize()*2];

	// dumb mode on
	fp[3] = &fft3; fp[4] = &fft4; fp[5] = &fft5; fp[6] = &fft6;
	fp[7] = &fft7; fp[8] = &fft8; fp[9] = &fft9;

	fp[10] = &fft10; fp[11] = &fft11; fp[12] = &fft12; fp[13] = &fft13; fp[14] = &fft14;
	fp[15] = &fft15; fp[16] = &fft16; fp[17] = &fft17; fp[18] = &fft18; fp[19] = &fft19;

	fp[20] = &fft20; fp[21] = &fft21; fp[22] = &fft22; fp[23] = &fft23; fp[24] = &fft24;
	fp[25] = &fft25; fp[26] = &fft26; fp[27] = &fft27; fp[28] = &fft28; fp[29] = &fft29;

	fp[30] = &fft30; fp[31] = &fft31; fp[32] = &fft32; fp[33] = &fft33; fp[34] = &fft34;
	fp[35] = &fft35; fp[36] = &fft36; fp[37] = &fft37; fp[38] = &fft38; fp[39] = &fft39;

	fp[40] = &fft40; fp[41] = &fft41; fp[42] = &fft42; fp[43] = &fft43; fp[44] = &fft44;
	fp[45] = &fft45; fp[46] = &fft46; fp[47] = &fft47; fp[48] = &fft48; fp[49] = &fft49;

	fp[50] = &fft50; fp[51] = &fft51; fp[52] = &fft52; fp[53] = &fft53; fp[54] = &fft54;
	fp[55] = &fft55; fp[56] = &fft56; fp[57] = &fft57; fp[58] = &fft58; fp[59] = &fft59;

	fp[60] = &fft60; fp[61] = &fft61; fp[62] = &fft62; fp[63] = &fft63; fp[64] = &fft64;

	fp[65] = &fft65;
	// dumb mode off

	for (int i = 0; i < getBufferSize() * 2; i++) {
			input[i] = drand48();
	}
}

void AlenFFTfftwKernel::run() {
	output = (*fp[this->getBufferSize()])(input);
}

std::vector<std::pair<void*, long> > AlenFFTfftwKernel::getBuffers() {
	std::vector<std::pair<void*, long> > result;
	result.push_back(
			std::make_pair((void*) input,
					getBufferSize() * sizeof(double) * 2));
	return result;
}

void AlenFFTfftwKernel::dispose() {
	delete[] input;
}
