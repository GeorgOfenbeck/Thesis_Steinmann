UMLET_JAR=~/Umlet/umlet.jar

sourcesToImages=$(addprefix out/, $(addsuffix .pdf, $(basename $(1))))
DIAGRAM_SOURCES=$(shell find -L -name "*.uxf" \( -path "./diagrams/*" \) -print)
DIAGRAM_IMAGES=$(call sourcesToImages, $(DIAGRAM_SOURCES))

PLOT_SOURCES=$(filter-out ./plots/header.gnuplot, $(shell find -L -name "*.gnuplot" \( -path "./plots/*" \) -print))
PLOT_IMAGES=$(call sourcesToImages, $(PLOT_SOURCES))

GRAPHS=$(shell find -L -name "*.pdf" \( -path "./graphs/*" \) -print)

BASE_DIR=$(shell pwd)

LATEX=pdflatex
FLAGS=-interaction=nonstopmode

.PHONY: report clean quick graphs plots
report: report.pdf

clean:
	rm -rf out

quick: 
	$(LATEX) $(FLAGS) report.tex
	cp out/report.pdf report.pdf

out:
	mkdir out
	mkdir out/diagrams

plots:  $(PLOT_IMAGES)

graphs:
	cd graphs && rot measure -checkCore report

report.pdf: report.tex report.bib out $(DIAGRAM_IMAGES) $(GRAPHS) $(PLOT_IMAGES)
	(($(LATEX) $(FLAGS) report.tex \
	&& bibtex report.aux \
	&& $(LATEX) $(FLAGS) report.tex \
	&& $(LATEX) $(FLAGS) report.tex) || rm report.pdf)
	
$(DIAGRAM_IMAGES): out/%.pdf: %.uxf
	mkdir -p $(dir $@)
	java -jar $(UMLET_JAR) -action=convert -format=pdf -filename='$<' -output='$@'
	pdfcrop $@ $@

$(PLOT_IMAGES): out/%.pdf: %.gnuplot
	mkdir -p $(dir $@)
	cd $(dir $<) && gnuplot $(notdir $<) > $(BASE_DIR)/$@
	
	