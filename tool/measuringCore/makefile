# makefile for the measuring core

# setup the default configuration
KERNEL_NAME=Dummy

#include the build configuration
include kernelName.mk

KERNEL_OPTIMIZATION_FLAGS=-O0
include kernelOptimization.mk

# build directory
BUILD_DIR=build

# additional directories to search for includes
INCLUDE_DIRS=-I"src"

# libraries for linking
LIBS=-lpfm -lstdc++ -lpapi

# command for the compiler
GCC=ccache gcc

# flags for generating the .d files
DEP_FLAGS=-MMD -MP -MF"$(@:%.o=%.d)" -MT"$(@:%.o=%.d)"

# compilation flags
CFLAGS=-std=c++0x -Wall
OPTIMIZATION_FLAGS=-O3

# source files to be ignored. separated by space
SOURCE_IGNORE_PATTERNS=./src/coreSwitchTest.cpp ./src/setAffinity.cpp

# pattern for the kernels
KERNEL_DIR=./src/kernels

sourcesToObjects=$(addprefix $(BUILD_DIR)/, $(addsuffix .o, $(basename $(1))))

# search for the source files in the ./src directory. Filter out the ignored source files
ALL_SOURCES=$(filter-out $(SOURCE_IGNORE_PATTERNS),$(shell find -L -name "*.cpp" -path "./src/*"))

# remove the kernels from the normal sources 
SOURCES=$(filter-out $(KERNEL_DIR)/%, $(ALL_SOURCES))
# create the file names of the .o files from the source files

OBJECTS=$(call sourcesToObjects, $(SOURCES))

# extract all source files within the kernel dir 
ALL_KERNEL_SOURCES=$(filter $(KERNEL_DIR)/%, $(ALL_SOURCES))

# extract the source files for the current kernel, create object file names
KERNEL_SOURCES=$(filter $(KERNEL_DIR)/$(KERNEL_NAME)Kernel.cpp $(KERNEL_DIR)/$(KERNEL_NAME)/%, $(ALL_KERNEL_SOURCES))
KERNEL_OBJECTS=$(call sourcesToObjects, $(KERNEL_SOURCES))

# search for .d files in the build directories
DEP_FILES=$(shell find -L -name "*.d" -path "./$(BUILD_DIR)/*")


.PHONY: rot clean clean_kernels

# rule for the executable
rot: $(BUILD_DIR)/rot

# rule for cleaning. just delete the build dir
clean:
	rm -rf $(BUILD_DIR)/

clean_kernels:
	rm -rf $(BUILD_DIR)/$(KERNEL_DIR)
	
# creates the executable from the .o files
$(BUILD_DIR)/rot: $(OBJECTS) $(KERNEL_OBJECTS)
	$(GCC) -pthread -rdynamic -o $@ $(OBJECTS) $(KERNEL_OBJECTS) $(LIBS)

#include all dependency files
include $(DEP_FILES)


# special rules for certain files ....
$(BUILD_DIR)/./src/baseClasses/MeasurementSchemeBase.o: src/baseClasses/MeasurementSchemeBase.cpp
	mkdir -p $(BUILD_DIR)/$(dir $<)
	$(GCC) $(CFLAGS) $(DEP_FLAGS) $(INCLUDE_DIRS) -ftemplate-depth=4096 -c -o $@ $<
# add more rules here when needed	

# make the kernels depend on the kernel optimization flags
$(KERNEL_OBJECTS): kernelOptimization.mk

# rule for the kernels
$(KERNEL_OBJECTS): $(BUILD_DIR)/%.o: %.cpp
	mkdir -p $(BUILD_DIR)/$(dir $<)
	$(GCC) $(CFLAGS) $(KERNEL_OPTIMIZATION_FLAGS) $(DEP_FLAGS) $(INCLUDE_DIRS) -c -o $@ $<
	
 
# the default rule
$(BUILD_DIR)/%.o: %.cpp
	mkdir -p $(BUILD_DIR)/$(dir $<)
	$(GCC) $(CFLAGS) $(OPTIMIZATION_FLAGS) $(DEP_FLAGS) $(INCLUDE_DIRS) -c -o $@ $<
	
