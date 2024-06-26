#!/bin/sh

set -e

if ! which wasm-pack > /dev/null; then
    cargo install wasm-pack
fi

if ! which wasm-snip > /dev/null; then
    cargo install wasm-snip
fi

wasm-pack build --release --target web

mkdir -p out

wasm-snip \
    --snip-rust-panicking-code \
    --snip-rust-fmt-code \
    -o out/wasm_css_analyzer.wasm \
    pkg/wasm_css_analyzer_bg.wasm

# NOTE: wasm-pack uses ES module, but we want a classic script.
cat pkg/wasm_css_analyzer.js | \
    sed -e 's/export function/function/' | \
    sed -e 's/import.meta.url/""/' | \
    grep -v '^export ' > out/wasm_css_analyzer.js
