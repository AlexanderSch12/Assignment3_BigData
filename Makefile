##
## Makefile
##
## Copyright (c) DTAI - KU Leuven – All rights reserved. Proprietary, do not
## copy or distribute without permission. Written by Pieter Robberechts, 2023
## 

.PHONY: clean bf_small lsh_small lsh_full

# Experiment parameters ######################################################

# Dataset directory
DATAFOLDER=/cw/bdap/assignment3/tweets.tsv

# Ouptut directory
OUTPUT=output.tsv

# Experiment parameters
THRESHOLD=0.9
# NB_TWEETS=8870959
NB_TWEETS=5000000
SHINGLE_LENGTH=3
NB_SHINGLES=1000
NB_HASHES=20
NB_BANDS=5
NB_BUCKETS=10000

# Compilation  ###############################################################

## Locate directories
class_d=bin
source_d=src

# Compilation stuff
JAVAC=javac
JFLAGS=-g -d $(class_d) -cp $(class_d) -Xlint:all

all: $(class_d)/Runner.class

clean:
	rm -rf $(class_d)/*

$(class_d)/MurmurHash.class: $(source_d)/MurmurHash.java
	@$(JAVAC) $(JFLAGS) $<

$(class_d)/Primes.class: $(source_d)/Primes.java
	@$(JAVAC) $(JFLAGS) $<

$(class_d)/SimilarPair.class: $(source_d)/SimilarPair.java
	@$(JAVAC) $(JFLAGS) $<

$(class_d)/Shingler.class: $(source_d)/Shingler.java $(class_d)/MurmurHash.class
	@$(JAVAC) $(JFLAGS) $<

$(class_d)/Reader.class: $(source_d)/Reader.java $(class_d)/Shingler.class
	@$(JAVAC) $(JFLAGS) $<

$(class_d)/TwitterReader.class: $(source_d)/TwitterReader.java $(class_d)/Reader.class
	@$(JAVAC) $(JFLAGS) $<

$(class_d)/SimilaritySearcher.class: $(source_d)/SimilaritySearcher.java $(class_d)/Reader.class $(class_d)/SimilarPair.class
	@$(JAVAC) $(JFLAGS) $<

$(class_d)/BruteForceSearch.class: $(source_d)/BruteForceSearch.java $(class_d)/SimilaritySearcher.class
	@$(JAVAC) $(JFLAGS) $<

$(class_d)/Minhash.class: $(source_d)/Minhash.java
	@$(JAVAC) $(JFLAGS) $<

$(class_d)/LSH.class: $(source_d)/LSH.java $(class_d)/SimilaritySearcher.class $(class_d)/Primes.class $(class_d)/Minhash.class
	@$(JAVAC) $(JFLAGS) $<

$(class_d)/Runner.class: $(source_d)/Runner.java $(class_d)/TwitterReader.class $(class_d)/BruteForceSearch.class $(class_d)/LSH.class
	@$(JAVAC) $(JFLAGS) $<

# Experiments ################################################################

bf_small: $(class_d)/Runner.class
	@echo "Testing BF on subset of data"
	time java -cp .:$(class_d) -Xmx2g Runner \
		-method bf \
		-maxTweets 5000 \
		-dataFile ${DATAFOLDER} \
		-outputFile ${OUTPUT} \
		-threshold 0.9 \
		-shingleLength ${SHINGLE_LENGTH} \
		-numShingles ${NB_SHINGLES}

lsh_small: $(class_d)/Runner.class
	@echo "Testing LSH on subset of data"
	time java -cp .:$(class_d) -Xmx2g Runner \
		-method lsh \
		-maxTweets 5000 \
		-dataFile ${DATAFOLDER} \
		-outputFile ${OUTPUT} \
		-threshold 0.9 \
		-shingleLength ${SHINGLE_LENGTH} \
		-numShingles ${NB_SHINGLES} \
		-numHashes ${NB_HASHES} \
		-numBands ${NB_BANDS} \
		-numBuckets ${NB_BUCKETS}

lsh_full: $(class_d)/Runner.class
	@echo "Running LSH on full dataset"
	time java -cp .:$(class_d) -Xmx2g Runner \
		-method lsh \
		-maxTweets ${NB_TWEETS} \
		-dataFile ${DATAFOLDER} \
		-outputFile ${OUTPUT} \
		-threshold ${THRESHOLD} \
		-shingleLength ${SHINGLE_LENGTH} \
		-numShingles ${NB_SHINGLES} \
		-numHashes ${NB_HASHES} \
		-numBands ${NB_BANDS} \
		-numBuckets ${NB_BUCKETS}

lsh_test:
	for NB_SHINGLES in 100 200 300 400 500 600 700 800 900 1000 1200 1500 ; do \
		for NB_HASHES in 5 10 20 30 35 40 45 50 55 65 70 80 100 ; do \
			for NB_BANDS in 5 7 10 12 15 20 ; do \
				for NB_BUCKETS in 200 400 600 700 800 900 1000 1200 ; do \
					$(class_d)/Runner.class ;  \
					echo "-------- SHINGLES: $$NB_SHINGLES HASHES: $$NB_HASHES BANDS: $$NB_BANDS BUCKETS: $$NB_BUCKETS --------" >> out.txt ; \
					time java -cp .:$(class_d) -Xmx2g Runner \
						-method lsh \
						-maxTweets 5000 \
						-dataFile ${DATAFOLDER} \
						-outputFile ${OUTPUT} \
						-threshold 0.9 \
						-shingleLength ${SHINGLE_LENGTH} \
						-numShingles $$NB_SHINGLES \
						-numHashes $$NB_HASHES \
						-numBands $$NB_BANDS \
						-numBuckets $$NB_BUCKETS >> out.txt ; \
					done ; \
				done ; \
			done ; \
		done


