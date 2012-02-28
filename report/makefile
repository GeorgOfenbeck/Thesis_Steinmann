UMLET_JAR=~/Umlet/umlet.jar

sourcesToImages=$(addprefix out/, $(addsuffix .pdf, $(basename $(1))))

DIAGRAM_SOURCES=$(shell find -L -name "*.uxf" \( -path "./diagrams/*" \) -print)
DIAGRAM_IMAGES=$(call sourcesToImages, $(DIAGRAM_SOURCES))

LATEX=cd out && pdflatex
FLAGS=-interaction=nonstopmode

.PHONY: report clean quick
report: report.pdf

clean:
	rm -rf out

quick: 
	$(LATEX) $(FLAGS) ../report.tex
	cp out/report.pdf report.pdf

out:
	mkdir out
	mkdir out/diagrams

report.pdf: out/report.pdf
	cp out/report.pdf report.pdf

out/report.pdf: report.tex report.bib out $(DIAGRAM_IMAGES)
	$(LATEX) $(FLAGS) ../report.tex
	cd out && bibtex report 
	$(LATEX) $(FLAGS) ../report.tex
	$(LATEX) $(FLAGS) ../report.tex
	
out/%.pdf: %.uxf
	mkdir mkdir -p $(dir $@)
	java -jar $(UMLET_JAR) -action=convert -format=pdf -filename='$<' -output='$@'
	pdfcrop $@ $@

	
	