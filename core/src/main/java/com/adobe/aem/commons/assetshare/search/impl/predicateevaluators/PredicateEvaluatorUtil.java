package com.adobe.aem.commons.assetshare.search.impl.predicateevaluators;

import com.day.cq.search.Predicate;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PredicateEvaluatorUtil {

    public static List<String> getValues(final Predicate predicate, final String parameterName) {
        return getValues(predicate, parameterName, false);
    }

    public static List<String> getValues(final Predicate predicate, final String parameterName, final boolean nullify) {
        final List<String> values = new ArrayList<>();

        predicate.getParameters().entrySet().stream().forEach(entry -> {
            final Pattern pattern = Pattern.compile("^(\\d+_)?" + parameterName + "$");
            final Matcher matcher = pattern.matcher(entry.getKey());

            if (matcher.matches()) {
                values.add(entry.getValue());

                if (nullify) {
                    predicate.set(entry.getKey(), null);
                }
            }
        });

        return values;
    }
}
