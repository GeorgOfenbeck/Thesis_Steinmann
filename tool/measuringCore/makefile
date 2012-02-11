# makefile for the measuring core

# build directory
BUILD_DIR=build

# libraries for linking
LIBS=-lpfm -lstdc++ -lblas

# command for the compiler
GCC=ccache gcc

# compilation flags
CFLAGS=-std=c++0x -Wall -ggdb
OPTIMIZATION_FLAGS=-O0

# source files to be ignored. separated by space
SOURCE_IGNORE_PATTERNS=./src/oldMain.cpp

# additional directories to search for includes
INCLUDE_DIRS=-I"src" -I"generated"

# flags for generating the .d files
DEP_FLAGS=-MMD -MP -MF"$(@:%.o=%.d)" -MT"$(@:%.o=%.d)"

# setup the kernel used
KERNEL_NAMES=Dummy
include generated/kernelNames.mk

ifneq (,$(findstring MMM, $(KERNEL_NAMES)))
	LIBS+=-lblas
endif

# setup the kernel optimization flags
$(foreach kernel,$(KERNEL_NAMES),$(eval include generated/kernelOptimization/kernelOptimization$(kernel).mk))

# the default kernel optimization flags
DEFAULT_KERNEL_OPTIMIZATION_FLAGS=-O1

# pattern for all kernel sources
ALL_KERNEL_SOURCES_PATTERN=./src/sharedDOM/%Kernel.cpp

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
ALL_KERNEL_OBJECTS=$(call sourcesToObjects, $(ALL_KERNEL_SOURCES))

# extract the source files for the current kernel, create object file names
$(foreach KERNEL_NAME,$(KERNEL_NAMES),$(eval KERNEL_SOURCES_$(KERNEL_NAME)=$(filter ./src/sharedDOM/$(KERNEL_NAME)Kernel.cpp ./src/sharedDOM/$(KERNEL_NAME)/%, $(ALL_KERNEL_SOURCES))))
$(foreach KERNEL_NAME,$(KERNEL_NAMES),$(eval KERNEL_OBJECTS_$(KERNEL_NAME)=$(call sourcesToObjects, $(KERNEL_SOURCES_$(KERNEL_NAME)))))

UNNAMED_KERNEL_OBJECTS=$(filter-out $(foreach kernel,$(KERNEL_NAMES),$(KERNEL_OBJECTS_$(kernel))), $(ALL_KERNEL_OBJECTS))

# search for .d files in the build directories
PRESENT_DEP_FILES=$(patsubst ./build/%, build/./%, $(shell find -L -name "*.d" -path "./$(BUILD_DIR)/*"))

# generate .d file names from the source file names
GENERATED_DEP_FILES=$(call sourcesToDeps, $(SOURCES) $(ALL_KERNEL_SOURCES))

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
$(BUILD_DIR)/measuringCore: $(OBJECTS) $(ALL_KERNEL_OBJECTS)
	$(GCC) -pthread -rdynamic -o $@ $^ $(LIBS)

#include all dependency files
include $(DEP_FILES)


# special rules for certain files ....

# compile without optimization, to ease generation of code for flushing L1 caches
$(BUILD_DIR)/./src/sharedDOM/WorkloadClearL1I.o: src/sharedDOM/WorkloadClearL1I.cpp
	mkdir -p $(BUILD_DIR)/$(dir $<)
	$(GCC) $(CFLAGS) -O0 $(DEP_FLAGS) $(INCLUDE_DIRS) -ftemplate-depth=4096 -c -o $@ $<
# add more rules here when needed	

# make the kernels depend on the kernel optimization flags
$(foreach kernel,$(KERNEL_NAMES),$(eval $(KERNEL_OBJECTS_$(kernel)): generated/kernelOptimization/kernelOptimization$(kernel).mk))


# rule for the named kernels
define KERNEL_RULE_template
$$(KERNEL_OBJECTS_$(1)): $(BUILD_DIR)/%.o: %.cpp
	mkdir -p $(BUILD_DIR)/$$(dir $$<)
	$(GCC) $(CFLAGS) $(KERNEL_OPTIMIZATION_FLAGS_$(1)) -Wa,-ahl=$$(patsubst %.o,%.s,$$@) $$(DEP_FLAGS) $(INCLUDE_DIRS) -c -o $$@ $$<
endef
$(foreach kernel,$(KERNEL_NAMES),$(eval $(call KERNEL_RULE_template,$(kernel))))

# rule for the remaining kernels
$(UNNAMED_KERNEL_OBJECTS): $(BUILD_DIR)/%.o: %.cpp
	mkdir -p $(BUILD_DIR)/$(dir $<)
	$(GCC) $(CFLAGS) $(DEFAULT_KERNEL_OPTIMIZATION_FLAGS) -Wa,-ahl=$(patsubst %.o,%.s,$@) $(DEP_FLAGS) $(INCLUDE_DIRS) -c -o $@ $<


# the default rule
$(BUILD_DIR)/%.o: %.cpp
	mkdir -p $(BUILD_DIR)/$(dir $<)
	$(GCC) $(CFLAGS) $(OPTIMIZATION_FLAGS) $(DEP_FLAGS) $(INCLUDE_DIRS) -c -o $@ $<
	
