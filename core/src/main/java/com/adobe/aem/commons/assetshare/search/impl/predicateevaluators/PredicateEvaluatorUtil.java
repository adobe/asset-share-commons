package com.adobe.aem.commons.assetshare.search.impl.predicateevaluators;

import com.day.cq.search.Predicate;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class PredicateEvaluatorUtil {

    public static List<String> getValues(final Predicate predicate, final String parameterName) {
        return predicate.getParameters().entrySet().stream().filter(entry -> {
            final Pattern pattern = Pattern.compile("(\\d+_)?" + parameterName);
            final Matcher matcher = pattern.matcher(entry.getKey());

            return matcher.matches();
        }).map(entry -> entry.getValue()).collect(Collectors.toList());x
    }
}
