# makefile for the measuring core

# build directory
BUILD_DIR=build

EXECUTABLE=$(BUILD_DIR)/systemConfigurator

# libraries for linking
LIBS=-lpfm -lstdc++ #-lpapi

# command for the compiler
GCC=ccache gcc

# compilation flags
CFLAGS=-std=c++0x -Wall -ggdb
OPTIMIZATION_FLAGS=-O3

# source files to be ignored. separated by space
SOURCE_IGNORE_PATTERNS=

# additional directories to search for includes
INCLUDE_DIRS=

# flags for generating the .d files
DEP_FLAGS=-MMD -MP -MF"$(@:%.o=%.d)" -MT"$(@:%.o=%.d)"

sourcesToObjects=$(addprefix $(BUILD_DIR)/, $(addsuffix .o, $(basename $(1))))
sourcesToDeps=$(addprefix $(BUILD_DIR)/, $(addsuffix .d, $(basename $(1))))

# search for the source files in the ./src directory. Filter out the ignored source files
SOURCES=$(filter-out $(SOURCE_IGNORE_PATTERNS),$(shell find -L -name "*.cpp" \( -path "./src/*" \) -print))

# create the file names of the .o files from the source files
OBJECTS=$(call sourcesToObjects, $(SOURCES))

# search for .d files in the build directories
PRESENT_DEP_FILES=$(patsubst ./build/%, build/./%, $(shell find -L -name "*.d" -path "./$(BUILD_DIR)/*"))

# generate .d file names from the source file names
GENERATED_DEP_FILES=$(call sourcesToDeps, $(SOURCES) $(KERNEL_SOURCES))

# keep only the .d files present which are required
DEP_FILES=$(filter $(GENERATED_DEP_FILES), $(PRESENT_DEP_FILES))

.PHONY: all clean super

# rule for the executable
all: $(EXECUTABLE)

# rule for cleaning. just delete the build dir
clean:
	rm -rf $(BUILD_DIR)/

super: $(EXECUTABLE)
	chown root:root $(EXECUTABLE)
	chmod a=xs $(EXECUTABLE)
	
# creates the executable from the .o files
$(EXECUTABLE): $(OBJECTS) $(KERNEL_OBJECTS)
	$(GCC) -pthread -rdynamic -o $@ $(OBJECTS) $(KERNEL_OBJECTS) $(LIBS)

#include all dependency files
include $(DEP_FILES)


# special rules for certain files ....

# add more rules here when needed	

# the default rule
$(BUILD_DIR)/%.o: %.cpp
	mkdir -p $(BUILD_DIR)/$(dir $<)
	$(GCC) $(CFLAGS) $(OPTIMIZATION_FLAGS) $(DEP_FLAGS) $(INCLUDE_DIRS) -c -o $@ $<
	
