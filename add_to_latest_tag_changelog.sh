#!/bin/bash

git log --oneline $(git tag | tail -n2 |xargs | sed -e 's# #...#') | grep -v "\[maven-release-plugin\]" > target/changelog.txt

git tag -a $(git tag | tail -n1) -f -F target/changelog.txt