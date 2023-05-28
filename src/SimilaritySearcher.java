/**
 * Copyright (c) DTAI - KU Leuven – All rights reserved. Proprietary, do not
 * copy or distribute without permission. Written by Pieter Robberechts, 2023
 */

import java.util.HashSet;
import java.util.Set;

import javax.lang.model.util.SimpleAnnotationValueVisitor7;

import java.util.Map;
import java.util.Arrays;


/**
 * Searching similar objects. Objects should be represented as a mapping from
 * an object identifier to a set containing the associated values.
 */
public abstract class SimilaritySearcher {

    Reader reader;

    public SimilaritySearcher(Reader reader) {
        this.reader = reader;
    }

    /**
     * Returns the pairs of the objectMapping that have a similarity coefficient exceeding threshold
     *
     * @param threshold the similarity threshold
     * @return the pairs with similarity above the threshold
     */
    abstract public Set<SimilarPair> getSimilarPairsAboveThreshold(double threshold);

    /**
     * Jaccard similarity between two sets.
     *
     * @param set1
     * @param set2
     * @return the similarity
     */
    public <T> double jaccardSimilarity(Set<T> set1, Set<T> set2) {
        double d = jaccardSimilarity1(set1,  set2);
        //double sim = jaccardSimilarity2(set1, set2);
        //if(d != sim) System.out.println(d + " " + sim);
        return d;
    }

    public <T> double jaccardSimilarity1(Set<T> set1, Set<T> set2) {
        
        Set<T> union = new HashSet<>(set1);
        union.addAll(set2);
        int unionSize = union.size();

        Set<T> intersection = new HashSet<>(set1);
        intersection.retainAll(set2);
        int intersectionSize = intersection.size();

        double d = intersectionSize != 0 ? (double) intersectionSize / unionSize : 0.0;
        return d;
    }

    public <T> double jaccardSimilarity2(Set<T> set1, Set<T> set2) {

        int size1 = set1.size();
        int size2 = set2.size();

        set1.retainAll(set2);
        int intersection = set1.size();
        
        int tt = size1 + size2 - intersection;
        double sim = intersection != 0 ? (double) intersection / tt : 0.0;

        return sim;
    }

}
