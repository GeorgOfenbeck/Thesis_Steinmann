report: report.dvi

clean:
	rm -rf out

out:
	mkdir out

report.dvi: report.tex report.bib out
	cd out &&\
	pdflatex ../report.tex &&\
	bibtex report &&\
	pdflatex ../report.tex &&\
	pdflatex ../report.tex