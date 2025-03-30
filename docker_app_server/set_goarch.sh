#!/usr/bin/env sh

# Detect architecture and set GOARCH
ARCH=$(uname -m)
case $ARCH in
    x86_64)
        GOARCH=amd64
        ;;
    aarch64|arm64)
        GOARCH=arm64
        ;;
    armv7*)
        GOARCH=arm
        ;;
    i386|i686)
        GOARCH=386
        ;;
    *)
        echo "Unsupported architecture: $ARCH"
        exit 1
        ;;
esac

echo "Using GOARCH=$GOARCH"
export GOARCH