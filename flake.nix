{
  outputs =
    { self, nixpkgs, ... }@inputs:
    {
      formatter.x86_64-linux = nixpkgs.legacyPackages.x86_64-linux.nixfmt-rfc-style;
      devShells.x86_64-linux.default = nixpkgs.legacyPackages.x86_64-linux.mkShell {
        packages = with nixpkgs.legacyPackages.x86_64-linux; [
          # love
          clojure # for development - im still learning clojure after all
          clojure-lsp
          jdk21_headless # requried by shadow-cljs
        ];
        shellHook = ''
          echo "Gear game dev shell"
        '';
      };

    };
}
