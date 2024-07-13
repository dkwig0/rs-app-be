package com.myorg.core.converter;

import java.util.List;

/**
 * @author Aliaksei Tsvirko
 */
public abstract class AbstractConverter<O, I> {

    public abstract O convert(I item);

    public List<O> convertAll(List<I> items) {
        if (items == null) {
            throw new RuntimeException("Provided list to convert is null.");
        }

        return items.stream().map(this::convert).toList();
    }

}
