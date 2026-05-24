SRC := $(wildcard src/*)

out/main.js: $(SRC) shadow-cljs.edn
	mkdir -p out
	shadow-cljs compile game

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

out/menu.png: assets/menu2.png
	mkdir -p out
	cp assets/menu2.png out/menu.png

out/how-to-play.png: assets/how-to-play2.png
	mkdir -p out
	cp assets/how-to-play2.png out/how-to-play.png

out/next-level.png: assets/next-level.png
	mkdir -p out
	cp assets/next-level.png out/next-level.png

out/win.png: assets/win.png
	mkdir -p out
	cp assets/win.png out/win.png

out/user-score-bg.png: assets/user-score-bg.png
	mkdir -p out
	cp assets/user-score-bg.png out/user-score-bg.png

out/meme.png: assets/meme.png
	mkdir -p out
	cp assets/meme.png out/meme.png

out/parens.png: assets/parens.png
	mkdir -p out
	cp assets/parens.png out/parens.png

out/gear.png: assets/gear.png
	mkdir -p out
	cp assets/gear.png out/gear.png

watch: $(SRC) \
	shadow-cljs.edn \
	out/index.html out/bg.png out/bulb.png out/rod.png out/menu.png out/how-to-play.png \
	out/next-level.png out/meme.png out/parens.png out/gear.png out/user-score-bg.png out/win.png
	shadow-cljs watch game

out: out/main.js \
	out/index.html out/bg.png out/bulb.png out/rod.png out/menu.png out/how-to-play.png \
	out/next-level.png out/meme.png out/parens.png out/gear.png out/user-score-bg.png out/win.png

build: out
	
clean:
	rm -r out
