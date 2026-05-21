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

out/menu.png: assets/menu.png
	mkdir -p out
	cp assets/menu.png out/menu.png

out/how-to-play.png: assets/how-to-play.png
	mkdir -p out
	cp assets/how-to-play.png out/how-to-play.png

out/meme.png: assets/meme.png
	mkdir -p out
	cp assets/meme.png out/meme.png

out: out/main.js out/index.html out/bg.png out/bulb.png out/rod.png out/menu.png out/how-to-play.png out/meme.png

build: out
	
clean:
	rm -r out
