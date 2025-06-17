package com.final_app.tools;

import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class ListFilter {
    /**
     * Generic method to filter a list based on multiple predicates.
     *
     * @param list The list to filter.
     * @param predicates Varargs of predicates. Null predicates in this array are ignored.
     * @param <T> The type of elements in the list.
     * @return A new list containing only the elements that satisfy all non-null predicates.
     */
    @SafeVarargs // Suppresses warnings for generic varargs if predicates are not misused
    public static <T> List<T> filterList(List<T> list, Predicate<T>... predicates) {
        Predicate<T> combinedPredicate = item -> true; // Start with a predicate that always passes

        if (predicates != null) {
            for (Predicate<T> predicate : predicates) {
                if (predicate != null) { // Only consider non-null predicates
                    combinedPredicate = combinedPredicate.and(predicate);
                }
            }
        }

        return list.stream()
                .filter(combinedPredicate)
                .collect(Collectors.toList());
    }
}
