call "header.gnuplot" "Hello World"

k=1024*1024*2
max(a,b)=a>b?a:b
#set log x
set log y
plot [1:2000][0.1:40] \
(2*x**3+2*x)/(3*x**2*8+max(x**2*8-k,0)) title "max", \
x/16,\
(2*x**3+2*x)/(3*x**2*8+max(x**2*8-max(k-x**2*8,0),0)) title "tripleSmall",\
(2*x**3+2*x)/((x**3+3*x**2)*8) title "tripleLarge",\
(2*x**3+2*x)/((3*x**2+(x**3)/50)*8) title "blockMiddle",\
(2*x**3+2*x)/((2*x**2+2*(x**3)/50)*8) title "blockLarge"