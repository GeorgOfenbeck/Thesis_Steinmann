#ifndef YetAnotherKernel_H_
#define YetAnotherKernel_H_

#include "sharedEntities/kernels/YetAnotherKernelData.h"

class YetAnotherKernel : public YetAnotherKernelData {
protected:
	double *a,*b,*c;
	std::vector<std::pair<void*,long> > getBuffers();

public:

	void initialize();
	void run();
	void dispose();
};

#endif /* YetAnotherKernel_H_ */
