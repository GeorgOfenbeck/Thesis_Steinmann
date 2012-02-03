#include "sharedDOM/MultiLanguageSerializationService.h"
#include "sharedDOM/MultiLanguageTestClass.h"
#include "sharedDOM/MeasurementDescription.h"
#include "sharedDOM/MeasurementCommand.h"
#include "sharedDOM/MeasurementRunOutputCollection.h"
#include "sharedDOM/MeasurementRunOutput.h"
#include "typeRegistry/TypeRegistry.h"
#include "typeRegistry/TypeRegistryEntry.h"
#include "baseClasses/KernelBase.h"
#include "baseClasses/MeasurerBase.h"
#include "baseClasses/MeasurementSchemeBase.h"
#include "baseClasses/ConfiguratorBase.h"
#include "utils.h"
#include "kernels/MemoryKernel.h"
#include "baseClasses/SystemInitializer.h"

#include <iostream>
#include <pthread.h>
#include <vector>
#include <sched.h>
#include <cstdio>
#include <cstdlib>
#include <string.h>
#include <csignal>
#include <fstream>
#include <typeinfo>
#include <sys/ptrace.h>
#include <sys/reg.h>
#include <sys/wait.h>
#include <unistd.h>

#define THREADCOUNT 200

using namespace std;

/*
 void *ThreadStart(void *arg){
 // abort all threads on the first cpu
 // this will cause threads from the other cpu to be put on the first cpu
 if (sched_getcpu()==0)
 return NULL;

 // do some work
 long sum=0;
 for (int i=0; i<100000; i++)
 for (int j=0; j<10000; j++)
 sum++;

 printf("%li; cpu: %i\n",sum,sched_getcpu());

 return NULL;
 }

 int main() {

 coreSwitchTest();

 return 0;

 vector<pthread_t> threads;
 pthread_attr_t attr;
 pthread_attr_init(&attr);
 pthread_attr_setdetachstate(&attr, PTHREAD_CREATE_JOINABLE);

 for (int i=0; i<THREADCOUNT; i++){
 pthread_t thread;
 pthread_create(&thread,&attr,ThreadStart,NULL);
 threads.push_back(thread);
 }
 pthread_attr_destroy(&attr);

 // join all threads
 for (int i=0; i<THREADCOUNT; i++){
 void *ret=NULL;
 pthread_join(threads[i],&ret);
 }


 cout << "!!!Hello World!!!" << endl; // prints !!!Hello World!!!
 return 0;
 }
 */

#include "sharedDOM/MemoryKernelDescription.h"

/*
 * Print a progress bar. Completed is the amount of work which is completed already,
 * total is the total amount of work (100%).
 *
 * If called multiple time, the existing progress bar is overwritten.
 */
static void printProgress(int completed, int total) {
	const int totalElements = 40;

	int printedChars = 0;

	// abort if there is no work to be done
	if (total == 0) {
		return;
	}

	// print the progress bar
	printedChars += printf("|");
	int i;
	for (i = 0; i < completed * totalElements / total && i < totalElements;
			i++) {
		printedChars += printf("=");
	}

	// fill remaining space
	for (; i < totalElements; i++) {
		printedChars += printf(" ");
	}

	// output the final bars and the precent number
	printedChars += printf("| %i%%", completed * 100 / total);

	// move the cursor back to the beginning of the line
	//for (i=0; i<printedChars; i++) printf("\b");
	printf("\r");
	fflush(stdout);
}

/*
 * If set to true, the measurement loop will quit in the next iteration.
 * Used by the handler of CTRL+C to abort the measurement
 */
static bool abortMeasurement;

static void sigint_handler(int arg) {
	abortMeasurement = true;
}
int doIt(int argc, char *argv[]) {
	MultiLanguageSerializationService serializationService;

	printf("Initializing system\n");
	SystemInitializer::initialize();

	printf("Reading input\n");
	ifstream input("config");
	if (!input.is_open()) {
		printf("could not read measurement configuratio file <config>");
		exit(1);
	}
	MeasurementCommand *command =
			(MeasurementCommand *) serializationService.DeSerialize(input);
	input.close();

	MeasurementDescription *description = command->getMeasurement();

	printf("Setting up the measurement\n");

	printf("registered kernels:\n");
	TypeRegistry<KernelBase>::print();

	printf("registered measurers:\n");
	TypeRegistry<MeasurerBase>::print();

	printf("registered measurement schemes:\n");
	TypeRegistry<MeasurementSchemeBase>::print();

	// create kernel
	KernelBase *kernel = TypeRegistry<KernelBase>::createObject(
			description->getKernel());
	if (kernel == NULL) {
		printf("kernel for %s not found\n",
				typeid(*description->getKernel()).name());
		exit(1);
	}

	// create measurer
	MeasurerBase *mainMeasurer = TypeRegistry<MeasurerBase>::createObject(
			description->getMeasurer());
	if (mainMeasurer == NULL) {
		printf("measurer for %s not found\n",
				typeid(*description->getMeasurer()).name());
		exit(1);
	}

	// create validation measurers
	vector<MeasurerBase*> *validationMeasurers = new vector<MeasurerBase*>();
	foreach(MeasurerDescriptionBase* measurerDescription,description->getValidationMeasurers())
			{
				MeasurerBase *measurer =
						TypeRegistry<MeasurerBase>::createObject(
								measurerDescription);
				if (measurer == NULL) {
					printf("measurer for %s not found\n",
							typeid(*measurerDescription).name());
					exit(1);
				}
				validationMeasurers->push_back(measurer);
			}

	// create additional measurers
	vector<MeasurerBase*> *additionalMeasurers = new vector<MeasurerBase*>();
	foreach(MeasurerDescriptionBase* measurerDescription,description->getAdditionalMeasurers())
			{
				MeasurerBase *measurer =
						TypeRegistry<MeasurerBase>::createObject(
								measurerDescription);
				if (measurer == NULL) {
					printf("measurer for %s not found\n",
							typeid(*measurerDescription).name());
					exit(1);
				}
				additionalMeasurers->push_back(measurer);
			}

	// create configurators
	vector<ConfiguratorBase*> *configurators=new vector<ConfiguratorBase*>();
	foreach(ConfiguratorDescriptionBase* desc,description->getConfigurators())
	{
		ConfiguratorBase *configurator =
				TypeRegistry<ConfiguratorBase>::createObject(
						desc);
		if (configurator == NULL) {
			printf("configurator for %s not found\n",
					typeid(*desc).name());
			exit(1);
		}
		configurators->push_back(configurator);
	}
	// create measurement scheme
	MeasurementSchemeBase *scheme =
			TypeRegistry<MeasurementSchemeBase>::createObject(
					description->getScheme(), kernel, mainMeasurer);
	if (scheme == NULL) {
		printf("measurement scheme for %s not found\n",
				typeid(*description->getScheme()).name());
		exit(1);
	}

	// add additional measurers
	scheme->setValidationMeasurers(validationMeasurers);
	scheme->setAdditionalMeasurers(additionalMeasurers);
	scheme->setConfigurators(configurators);

	// notify the configurators
	foreach(ConfiguratorBase *configurator, *configurators){
		configurator->beforeMeasurement();
	}

	// initialize the schemes, which will initialize the kernel and the measurers as well
	scheme->initialize();

	MeasurementRunOutputCollection outputCollection;

	printf("Performing measurement\n");

	for (int i = 0; !abortMeasurement && i < command->getNumberOfMeasurements();
			i++) {
		printProgress(i, command->getNumberOfMeasurements());

		MeasurementRunOutput *measurerOutput = scheme->measure();
		if (measurerOutput != NULL) {
			outputCollection.getOutputs().push_back(measurerOutput);
		}
	}
	// print final progress bar
	printProgress(100, 100);
	printf("\n");

	printf("tearing down\n");
	// notify configurators

	reverse_foreach(ConfiguratorBase *configurator, *configurators){
		configurator->afterMeasurement();
	}

	delete (kernel);
	delete (mainMeasurer);
	foreach (MeasurerBase *measurer, *validationMeasurers){
		delete(measurer);
	}
	delete(validationMeasurers);
	foreach (MeasurerBase *measurer, *additionalMeasurers){
		delete(measurer);
	}
	delete(additionalMeasurers);
	foreach(ConfiguratorBase *configurator, *configurators){
		delete(configurator);
	}
	delete(configurators);
	delete (scheme);

	printf("writing output\n");
	ofstream output("output");
	serializationService.Serialize(&outputCollection, output);
	output.close();

	printf("Shutting down system\n");
	SystemInitializer::shutdown();

	return 0;
}

void handleSerializationTest() {
	MultiLanguageSerializationService serializationService;

	// run the serialization test
	printf("Running Serialization Test\n");

	// load input
	printf("Loading input\n");
	ifstream input("serializationTestInput");
	MultiLanguageTestClass *testObject;
	testObject = (MultiLanguageTestClass *) serializationService.DeSerialize(
			input);
	input.close();

	// write output
	printf("writing output\n");
	ofstream output("serializationTestOutput");
	serializationService.Serialize(testObject, output);
	output.close();

}

/**
 * main method of the child process
 */
void childMain(){
	// wait till the parent traces the child
	ptrace(PTRACE_TRACEME);
	raise(SIGCHLD);

	printf("parent is tracing");
}

pid_t startChildProcess(){
	pid_t childPid=fork();

	if (childPid==0){
		// we are in the child process
		childMain();
	}

	if (childPid==-1){
		perror("fork failed");
		exit(1);
	}

	return childPid;
}

int main(int argc, char *argv[]) {
	// check if doing a serialization test is requested
	if (argc == 2 && strcmp(argv[1], "serializationTest") == 0) {
		handleSerializationTest();

		// end program
		return 0;
	}

	pid_t childPid=startChildProcess();

	printf("wait for child");
	//wait for the child
	waitpid(childPid,NULL,0);

	printf("child stopped");

	exit(0);

	// setup handler for SIGINT (Ctrl+C)
	abortMeasurement = false;
	signal(SIGINT, sigint_handler);

	// perform the measurement
	try {
		return doIt(argc, argv);
	} catch (char const *msg) {
		printf("Exception: %s\n", msg);
	} catch (string s) {
		cout << "Exception: " << s << "\n";
	} catch (Exception exception) {
		cout << "Exception: " << exception.get_message() << "\n";
		exception.print(0);
	} catch (...) {
		throw;
	}
}
