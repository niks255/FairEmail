#!/bin/sh

for remote in $(git remote | grep -v origin)
do
    echo "Pushing to $remote";
    git push "$remote" --all $@;
done
