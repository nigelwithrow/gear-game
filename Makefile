SRC := $(wildcard src/*)

out/main.js: $(SRC) shadow-cljs.edn
	mkdir -p out
	shadow-cljs compile game

watch: $(SRC) shadow-cljs.edn
	shadow-cljs watch game

out/index.html: index.html
	mkdir -p out
	cp index.html out/index.html

out/bg.png: assets/bg.png
	mkdir -p out
	cp assets/bg.png out/bg.png

out/bulb.png: assets/bulb.png
	mkdir -p out
	cp assets/bulb.png out/bulb.png

out/rod.png: assets/rod.png
	mkdir -p out
	cp assets/rod.png out/rod.png

out: out/main.js out/index.html out/bg.png out/bulb.png out/rod.png

build: out
	
clean:
	rm -r out
