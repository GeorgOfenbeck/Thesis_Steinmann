# makefile for the measuring core

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

# source files to be ignored. separated by space
SOURCE_IGNORE_PATTERNS=./src/coreSwitchTest.cpp ./src/setAffinity.cpp

# search for the source files in the ./src directory. Filter out the ignored source files
SOURCES=$(filter-out $(SOURCE_IGNORE_PATTERNS),$(shell find -L -name "*.cpp" -path "./src/*"))

# search for .d files in the build directories
DEP_FILES=$(shell find -L -name "*.d" -path "./$(BUILD_DIR)/*")

# create the file names of the .o files from the source files
OBJECTS=$(addprefix $(BUILD_DIR)/, $(addsuffix .o, $(basename $(SOURCES))))

.PHONY: rot clean

# rule for the executable
rot: $(BUILD_DIR)/rot

# rule for cleaning. just delete the build dir
clean:
	rm -rf $(BUILD_DIR)/

# creates the executable from the .o files
$(BUILD_DIR)/rot: $(OBJECTS)
	$(GCC) -pthread -rdynamic -o $@ $(OBJECTS) $(LIBS)

#include all dependency files
include $(DEP_FILES)

# special rules for certain files ....
$(BUILD_DIR)/./src/baseClasses/MeasurementSchemeBase.o: src/baseClasses/MeasurementSchemeBase.cpp
	mkdir -p $(BUILD_DIR)/$(dir $<)
	$(GCC) $(CFLAGS) $(DEP_FLAGS) $(INCLUDE_DIRS) -ftemplate-depth=4096 -c -o $@ $<
# add more rules here when needed	

# the default rule
$(BUILD_DIR)/%.o: %.cpp
	mkdir -p $(BUILD_DIR)/$(dir $<)
	$(GCC) $(CFLAGS) $(DEP_FLAGS) $(INCLUDE_DIRS) -c -o $@ $<
	
