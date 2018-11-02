{ pkgs ? import <nixpkgs> {} } :
with pkgs;
stdenv.mkDerivation rec {
  name = "boost";
  buildInputs = [ ];
  nativeBuildInputs = [ leiningen postgresql foreman ];
  shellHook = ''
    echo "(setq cider-lein-command \"${pkgs.leiningen}/bin/lein\")" > lein.el
  '';
}