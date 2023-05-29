/**
 * Copyright (c) DTAI - KU Leuven – All rights reserved. Proprietary, do not
 * copy or distribute without permission. Written by Pieter Robberechts, 2023
 */

import java.util.*;
import java.io.*;
import java.sql.Array;

/**
 * Implementation of minhash and locality sensitive hashing (LSH) to find
 * similar objects.
 * <p>
 * The LSH should first construct a signature matrix. Based on this, LSH is
 * performed resulting in a mapping of band ids to hash tables (stored in
 * bandToBuckets). From this bandsToBuckets mapping, the most similar items
 * should then be retrieved.
 */
public class LSH extends SimilaritySearcher 
{

    int numHashes;
    int numBands;
    int numBuckets;
    int numShingles;
    int numDocs;
    int seed;
    int[][] signatureMatrix;

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
        this.numDocs = reader.getMaxDocs();
        this.seed = seed;
        reader.reset();
        int[][] hashValues = Minhash.constructHashTable(numHashes, numShingles, seed);
        this.signatureMatrix = Minhash.constructSignatureMatrix(reader, hashValues);
    }


    /**
     * Returns the pairs with similarity above threshold (approximate).
     */
    @Override
    public Set<SimilarPair> getSimilarPairsAboveThreshold(double threshold) {
        Set<SimilarPair> similarPairsAboveThreshold = new HashSet<SimilarPair>();
        int rows = numHashes / numBands;
        byte[] docKey = new byte[rows];
        List<Set<Integer>> documents = reader.readAll();

        for(int b = 0 ; b < numBands ; b++)
        {
            List<List<Integer>> buckets = new ArrayList<>(numBuckets);
            for(int bucket = 0 ; bucket<numBuckets ; bucket++) buckets.add(new ArrayList<Integer>());

            for (int d = 0; d < numDocs; d++) 
            {
                // Construct key of current doc in current band
                for (int row = 0 ; row < rows ; row++) 
                {
                    docKey[row] = (byte) signatureMatrix[rows*b + row][d];
                }
                // Hash key using MurmurHash
                int index = Math.abs(MurmurHash.hash32(docKey, rows, seed)) % numBuckets;

                List<Integer> bucketList = buckets.get(index);
                for(int document : bucketList)
                {
                    double sim = jaccardSimilarity(documents.get(document),documents.get(d));
                    if(sim > threshold)
                    {
                        similarPairsAboveThreshold.add(new SimilarPair(reader.getExternalId(document),reader.getExternalId(d),sim));
                    }
                }
                bucketList.add(d);
            }
        }
        return similarPairsAboveThreshold;
    }
}

