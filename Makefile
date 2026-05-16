SRC := $(wildcard src/*)

out/main.js: $(SRC) shadow-cljs.edn
	mkdir -p out
	shadow-cljs compile game

watch: $(SRC) shadow-cljs.edn
	shadow-cljs watch game

out/index.html: index.html
	mkdir -p out
	cp index.html out/index.html

out: out/main.js out/index.html

build: out
	
clean:
	rm -r out
