{ pkgs ? import <nixpkgs> {} } :
with pkgs;
stdenv.mkDerivation rec {
  name = "boost";
  buildInputs = [ ];
  nativeBuildInputs = [ leiningen postgresql foreman ];
}