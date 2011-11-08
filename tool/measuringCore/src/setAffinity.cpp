#include <sched.h>
#include <cstdio>
#include <unistd.h>
int main(){
	int cpu=sched_getcpu();
	printf("hello World, running on cpu %i\n",cpu);
	
	// switching to other cpu
	cpu_set_t mask;
	CPU_ZERO(&mask);
 	if (cpu==0)
		CPU_SET(1,&mask);
	else
		CPU_SET(0,&mask);

	sched_setaffinity(0,sizeof(mask),&mask);
	usleep(100000);
	
	printf("running on cpu %i\n",sched_getcpu());
	
	return 0;
}