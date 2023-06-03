#!/bin/bash

java Runner \
    -method lsh \
    -maxTweets 5000000 \
    -dataFile /cw/bdap/assignment3/tweets.tsv \
    -outputFile output.tsv \
    -threshold 0.9 \
    -shingleLength 3 \
    -numShingles 1000 \
    -numHashes 84 \
    -numBands 4 \
    -numBuckets 7000000