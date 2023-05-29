/**
 * Copyright (c) DTAI - KU Leuven – All rights reserved. Proprietary, do not
 * copy or distribute without permission. Written by Pieter Robberechts, 2023
 */

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Set;

/**
 * Class for computing MinHash signatures.
 */
public final class Minhash {


    private Minhash() {

    }

    /**
     * Construct the table of hash values needed to construct the signature matrix.
     * Position (i,j) contains the result of applying function j to row number i.
     *
     * @param numHashes number of hashes that will be used in the signature matrix
     * @param numValues number of unique values that occur in the object set representations (i.e. number of rows of the characteristic matrix)
     * @param seed      should be used to generate any random numbers needed
     * @return the (numValues x numHashes) matrix of hash values
     */
    public static int[][] constructHashTable(int numHashes, int numValues, int seed)
    {
        int[][] hashes = new int[numValues][numHashes];

        Random rn = new Random(seed);
        int a, b;
        int prime = Primes.findLeastPrimeNumber(numHashes);

        for (int i = 0; i < numHashes; i++) {
            // h(x) = ((a.x + b) mod p) mod #rows
            a = Math.abs(rn.nextInt(prime));
            b = Math.abs(rn.nextInt(prime));
            for (int j = 0; j < numValues; j++) {
                hashes[j][i] = ((a*j + b) % prime) % numValues;
            }    
        }
        return hashes;
    }

    /**
     * Construct the signature matrix.
     *
     * @param reader     iterator returning the set represenation of objects for which the signature matrix should be constructed
     * @param hashValues (numValues x numHashes) matrix of hash values
     * @return signatureMatrix      the signature matrix (numHashes x numObjects) 
     */
    public static int[][] constructSignatureMatrix(Reader reader, int[][] hashValues)
    {
        int numHashes = hashValues[0].length;
        int numDocs = reader.maxDocs;
        int[][] signatureMatrix = new int[numHashes][numDocs];

        // Initialize signature matrix with infinity
        for (int i = 0; i < numHashes; i++) {
            for (int j = 0; j < numDocs; j++) signatureMatrix[i][j] = Integer.MAX_VALUE;
        }

        /* One Pass Implementation */
        // Loop trough documents first
        while (reader.hasNext()) {
            Set<Integer> shinglesIndex = reader.next();
            // Loop trough rows of the document
            for (int row: shinglesIndex) {
                // If row index is in set, use hash-value for signature
                for (int h = 0; h < numHashes; h++) {
                    // If the hash-value is smaller than the current hash-value
                    if (hashValues[row][h] < signatureMatrix[h][reader.curDoc]) {
                        signatureMatrix[h][reader.curDoc] = hashValues[row][h];
                    }
                }
            }
        }
        reader.reset();
        return signatureMatrix;
    }
}
