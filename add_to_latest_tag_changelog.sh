#!/bin/bash

git log --oneline $(git tag | tail -n2 |xargs | sed -e 's# #...#') | grep -v "\[maven-release-plugin\]" > target/changelog.txt

latestTag=$(git tag | tail -n1)
git tag -a $latestTag -f -F target/changelog.txt

git push --force origin refs/tags/$latestTag