package com.myorg.core.converter;

import java.util.List;

/**
 * @author Aliaksei Tsvirko
 */
public abstract class AbstractConverter<I, O> {

    public abstract I convert(O item);

    public List<I> convertAll(List<O> items) {
        if (items == null) {
            throw new RuntimeException("Provided list to convert is null.");
        }

        return items.stream().map(this::convert).toList();
    }

}
