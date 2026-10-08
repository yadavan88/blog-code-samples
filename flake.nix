{
  description = "dev shell with Claude Code sandboxed by nono";
  inputs.nixpkgs.url = "github:NixOS/nixpkgs/nixpkgs-unstable";
  outputs = { nixpkgs, ... }:
    let
      systems = [ "aarch64-darwin" "x86_64-darwin" "x86_64-linux" "aarch64-linux" ];
      forAll = f: nixpkgs.lib.genAttrs systems (system:
        f (import nixpkgs { inherit system; config.allowUnfree = true; }));
    in {
      devShells = forAll (pkgs:
        let
          realClaude = "${pkgs.claude-code}/bin/claude";
          claude = pkgs.writeShellScriptBin "claude" ''
            export DISABLE_AUTOUPDATER=1
            export ANTHROPIC_MODEL="''${ANTHROPIC_MODEL:-sonnet}"
            if [ -n "''${NONO_CAP_FILE:-}" ]; then
              exec ${realClaude} "$@"
            fi
            cd "$(git rev-parse --show-toplevel 2>/dev/null || pwd)"
            exec ${pkgs.nono}/bin/nono run --profile strict --allow-cwd -- ${realClaude} "$@"
          '';
        in {
          default = pkgs.mkShell {
            packages = [ pkgs.git pkgs.nono claude ];
          };
        });
    };
}