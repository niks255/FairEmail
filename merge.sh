#!/bin/sh

git reset --hard && \
git fetch origin && \
git merge $(git describe --tags $(git rev-list --tags --max-count=1))
$SHELL