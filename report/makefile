UMLET_JAR=~/Umlet/umlet.jar

sourcesToImages=$(addprefix out/, $(addsuffix .pdf, $(basename $(1))))

DIAGRAM_SOURCES=$(shell find -L -name "*.uxf" \( -path "./diagrams/*" \) -print)
DIAGRAM_IMAGES=$(call sourcesToImages, $(DIAGRAM_SOURCES))

GRAPHS=$(shell find -L -name "*.pdf" \( -path "./graphs/*" \) -print)

LATEX=pdflatex
FLAGS=-interaction=nonstopmode

.PHONY: report clean quick
report: report.pdf

clean:
	rm -rf out

quick: 
	$(LATEX) $(FLAGS) report.tex
	cp out/report.pdf report.pdf

out:
	mkdir out
	mkdir out/diagrams

report.pdf: report.tex report.bib out $(DIAGRAM_IMAGES) $(GRAPHS)
	rot measure -checkCore report
	(($(LATEX) $(FLAGS) report.tex \
	&& bibtex report.aux \
	&& $(LATEX) $(FLAGS) report.tex \
	&& $(LATEX) $(FLAGS) report.tex) || rm report.pdf)
	
out/%.pdf: %.uxf
	mkdir mkdir -p $(dir $@)
	java -jar $(UMLET_JAR) -action=convert -format=pdf -filename='$<' -output='$@'
	pdfcrop $@ $@

	
	