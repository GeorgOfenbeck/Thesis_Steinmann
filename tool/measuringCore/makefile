# makefile for the measuring core

# build directory
BUILD_DIR=build

# libraries for linking
LIBS=-lpfm -lstdc++ #-lpapi

# command for the compiler
GCC=ccache gcc

# compilation flags
CFLAGS=-std=c++0x -Wall -ggdb
OPTIMIZATION_FLAGS=-O0

# source files to be ignored. separated by space
SOURCE_IGNORE_PATTERNS=./src/coreSwitchTest.cpp ./src/setAffinity.cpp

# additional directories to search for includes
INCLUDE_DIRS=-I"src" -I"generated"

# flags for generating the .d files
DEP_FLAGS=-MMD -MP -MF"$(@:%.o=%.d)" -MT"$(@:%.o=%.d)"

# setup the kernel used
KERNEL_NAME=Dummy
include kernelName.mk

# setup the kernel optimization flags
KERNEL_OPTIMIZATION_FLAGS=-O0
include kernelOptimization.mk

# directory for the kenrels
KERNEL_DIR=./src/kernels

# Measurment Scheme Registration file
MEASUREMENT_SCHEME_REGISTRATION_FILE=./generated/MeasurementSchemeRegistration.cpp

# pattern for all kernel sources
ALL_KERNEL_SOURCES_PATTERN=$(KERNEL_DIR)/% $(MEASUREMENT_SCHEME_REGISTRATION_FILE)

sourcesToObjects=$(addprefix $(BUILD_DIR)/, $(addsuffix .o, $(basename $(1))))
sourcesToDeps=$(addprefix $(BUILD_DIR)/, $(addsuffix .d, $(basename $(1))))

# search for the source files in the ./src directory. Filter out the ignored source files
ALL_SOURCES=$(filter-out $(SOURCE_IGNORE_PATTERNS),$(shell find -L -name "*.cpp" \( -path "./src/*" -o -path "./generated/*" \) -print))

# remove the kernels from the normal sources 
SOURCES=$(filter-out $(ALL_KERNEL_SOURCES_PATTERN), $(ALL_SOURCES))
# create the file names of the .o files from the source files

OBJECTS=$(call sourcesToObjects, $(SOURCES))

# extract all source files within the kernel dir 
ALL_KERNEL_SOURCES=$(filter $(ALL_KERNEL_SOURCES_PATTERN), $(ALL_SOURCES))

# extract the source files for the current kernel, create object file names
KERNEL_SOURCES=$(filter $(MEASUREMENT_SCHEME_REGISTRATION_FILE) $(KERNEL_DIR)/$(KERNEL_NAME)Kernel.cpp $(KERNEL_DIR)/$(KERNEL_NAME)/%, $(ALL_KERNEL_SOURCES))
KERNEL_OBJECTS=$(call sourcesToObjects, $(KERNEL_SOURCES))

# search for .d files in the build directories
PRESENT_DEP_FILES=$(patsubst ./build/%, build/./%, $(shell find -L -name "*.d" -path "./$(BUILD_DIR)/*"))

# generate .d file names from the source file names
GENERATED_DEP_FILES=$(call sourcesToDeps, $(SOURCES) $(KERNEL_SOURCES))

# keep only the .d files present which are required
DEP_FILES=$(filter $(GENERATED_DEP_FILES), $(PRESENT_DEP_FILES))

.PHONY: all clean clean_kernels

# rule for the executable
all: $(BUILD_DIR)/measuringCore

# rule for cleaning. just delete the build dir
clean:
	rm -rf $(BUILD_DIR)/

clean_kernels:
	rm -rf $(BUILD_DIR)/$(KERNEL_DIR)
	
# creates the executable from the .o files
$(BUILD_DIR)/measuringCore: $(OBJECTS) $(KERNEL_OBJECTS)
	$(GCC) -pthread -rdynamic -o $@ $(OBJECTS) $(KERNEL_OBJECTS) $(LIBS)

#include all dependency files
include $(DEP_FILES)


# special rules for certain files ....

# compile without optimization, to ease generation of code for flushing L1 caches
$(BUILD_DIR)/./src/baseClasses/MeasurementSchemeBase.o: src/baseClasses/MeasurementSchemeBase.cpp
	mkdir -p $(BUILD_DIR)/$(dir $<)
	$(GCC) $(CFLAGS) -O0 $(DEP_FLAGS) $(INCLUDE_DIRS) -ftemplate-depth=4096 -c -o $@ $<
# add more rules here when needed	

# make the kernels depend on the kernel optimization flags
$(KERNEL_OBJECTS): kernelOptimization.mk

# rule for the kernels
$(KERNEL_OBJECTS): $(BUILD_DIR)/%.o: %.cpp
	mkdir -p $(BUILD_DIR)/$(dir $<)
	$(GCC) $(CFLAGS) $(KERNEL_OPTIMIZATION_FLAGS) -Wa,-ahl=$(patsubst %.o,%.s,$@) $(DEP_FLAGS) $(INCLUDE_DIRS) -c -o $@ $<
	
 
# the default rule
$(BUILD_DIR)/%.o: %.cpp
	mkdir -p $(BUILD_DIR)/$(dir $<)
	$(GCC) $(CFLAGS) $(OPTIMIZATION_FLAGS) $(DEP_FLAGS) $(INCLUDE_DIRS) -c -o $@ $<
	
