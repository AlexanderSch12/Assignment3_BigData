/**
 * Copyright (c) DTAI - KU Leuven – All rights reserved. Proprietary, do not
 * copy or distribute without permission. Written by Pieter Robberechts, 2023
 */

import java.util.*;
import java.io.*;

/**
 * Implementation of minhash and locality sensitive hashing (LSH) to find
 * similar objects.
 * <p>
 * The LSH should first construct a signature matrix. Based on this, LSH is
 * performed resulting in a mapping of band ids to hash tables (stored in
 * bandToBuckets). From this bandsToBuckets mapping, the most similar items
 * should then be retrieved.
 */
public class LSH extends SimilaritySearcher {

    int numHashes;
    int numBands;
    int numBuckets;
    int numShingles;
    int numdocs;
    int seed;
    int[][] signatureMatrix;
    List<Set<Integer>> documents;

    /**
     * Construct an LSH similarity searcher.
     *
     * @param reader     the document reader
     * @param numHashes  number of hashes to use to construct the signature matrix
     * @param numBands   number of bands to use during locality sensitive hashing
     * @param numBuckets number of buckets to use during locality sensitive hashing
     * @param seed       should be used to generate any random numbers needed
     */
    public LSH(Reader reader, int numHashes, int numBands, int numBuckets, int seed) {
        super(reader);

        this.numHashes = numHashes;
        this.numBands = numBands;
        this.numBuckets = numBuckets;
        this.numShingles = reader.getNumShingles();
        this.numdocs = reader.getMaxDocs();
        this.seed = seed;
        this.documents = reader.readAll();
        int[][] hashValues = Minhash.constructHashTable(numHashes, numShingles, seed);
        this.signatureMatrix = Minhash.constructSignatureMatrix(reader, hashValues);

        getSimilarPairsAboveThreshold(0.8);
    }

    /**
     *
     *
     * @param signatureMatrix   (numHashes x numObjects) signature matrix
     * @param seed              seed used for MurmurHash to hash keys
     * @return bandBuckets      buckets of every band with the candidate pairs
     */
    public List<List<List<Integer>>> lsh(int[][] signatureMatrix, int seed)
    {
        List<List<List<Integer>>> bandBuckets = new ArrayList<>(numBands);
        int rows = numHashes / numBands;
        byte[] docKey = new byte[rows];

        for (int b = 0; b < numBands; b++) {
            // List of Set<Integer> where the index is the hashed doc key in the current band and the Set are the doc
            // id's of the candidate pairs
            ArrayList<List<Integer>> buckets = new ArrayList<>(numBuckets);
            for (int d = 0; d < numdocs; d++) {
                // Construct key of current doc in current band
                for (int r = 0; r < rows; r++) {
                    docKey[r] = (byte) signatureMatrix[r][d];
                }
                // Hash key using MurmurHash
                int index = MurmurHash.hash32(docKey, rows, seed) % numBuckets;
                System.out.println("Index: " + index);
                buckets.get(index).add(d);
            }
            bandBuckets.add(b, buckets);
        }
        return bandBuckets;
    }


    /**
     * Returns the pairs with similarity above threshold (approximate).
     */
    @Override
    public Set<SimilarPair> getSimilarPairsAboveThreshold(double threshold) {
        Set<SimilarPair> similarPairsAboveThreshold = new HashSet<SimilarPair>();
        List<List<List<Integer>>> bandBuckets = lsh(signatureMatrix, seed);

        for(int b = 0 ; b < numBands ; b++)
        {
            List<List<Integer>> buckets = bandBuckets.get(b);
            for(int bucket = 0 ; bucket < buckets.size() ; bucket++)
            {
                List<Integer> candidates = buckets.get(bucket);
                for(int i = 0 ; i < candidates.size() ; i++)
                {
                    for(int j = i + 1 ; j < candidates.size() ; j++)
                    {
                        double sim = jaccardSimilarity(documents.get(i),documents.get(j));
                        if(sim > threshold)
                        {
                            similarPairsAboveThreshold.add(new SimilarPair(i,j,sim));
                        }
                    }
                }
            }
        }
        return similarPairsAboveThreshold;
    }

}
