package com.amazon.green.book.service.webapp.utils;

import static com.amazon.green.book.service.webapp.context.SearchStoreRequestContext.SPACE;
import static java.util.Arrays.asList;
import static java.util.Optional.ofNullable;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.startsWithIgnoreCase;

import com.amazon.green.book.service.model.StoreInformation;
import com.google.common.collect.ImmutableList;
import java.util.List;
import lombok.experimental.UtilityClass;

@UtilityClass
public class StoresFilter {

    /**
     * Filters a list of StoreInformation based on alpha characters and postalCode from the search query.
     *
     * @param storesToFilter the list of StoreInformation to filter
     * @param postalCode the postalCode field that filtering should match
     * @param alphaChars the alpha chars in the raw search query
     * @return filtered list of StoreInformation
     */
    public static List<StoreInformation> filterStoresByQuery(final List<StoreInformation> storesToFilter,
                                                             final String postalCode,
                                                             final String alphaChars) {
        return ofNullable(storesToFilter)
                .orElse(ImmutableList.of())
                .stream()
                .filter(store -> matchCityStatePostalCode(store, alphaChars, postalCode))
                .collect(ImmutableList.toImmutableList());
    }


    private static boolean matchCityStatePostalCode(final StoreInformation store,
                                                    final String alphaChars,
                                                    final String postalCode) {

        if (isBlank(alphaChars)) {
            return matchPostalCode(store, postalCode);
        }

        // These lines are needed so that city with multiple words get broken down into multiple words properly
        // For example "Salt Lake City UT" -> List<String> storeCityStateWords = ["Salt", "Lake", "City", "UT"]
        final String storeCityState = store.getCity() + SPACE + store.getState();
        final List<String> storeCityStateWords = asList(storeCityState.split(SPACE));

        final List<String> alphaCharsWords = asList(alphaChars.split(SPACE));

        return matchPostalCode(store, postalCode) && alphaCharsWords.stream().allMatch(alphaCharsWord ->
                storeCityStateWords.stream().anyMatch(storeCityStateWord ->
                        startsWithIgnoreCase(storeCityStateWord, alphaCharsWord)));
    }

    private static boolean matchPostalCode(final StoreInformation store, final String postalCode) {
        // if postalCode is null don't check on postalCode field
        return postalCode == null || startsWithIgnoreCase(store.getPostalCode(), postalCode);
    }
}
