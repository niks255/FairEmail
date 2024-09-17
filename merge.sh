#!/bin/sh

git reset --hard && \
git fetch upstream && \
git merge $(git describe --tags $(git rev-list --tags --max-count=1))
$SHELL