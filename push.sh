#!/bin/sh

for remote in $(git remote | grep -v origin)
do
    echo "Pushing to $remote";
    git push "$remote" --all;
    git push "$remote" --tags;
done
