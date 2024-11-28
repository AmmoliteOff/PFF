package ru.roe.pff.processing;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class Row {
    private int index;
    private List<Object> elements = new ArrayList<>();
    private List<Class<?>> types = new ArrayList<>();

    public void setElements(List<Object> elements, List<Class<?>> types) {
        this.types = types;
        this.elements = elements;
    }

    public <T> T get(int index, Class<T> expectedType) {
        if (index < 0 || index >= elements.size()) {
            throw new IndexOutOfBoundsException("Invalid index: " + index);
        }

        if (elements.get(index) == null) {
            return null;
        }

        // Проверяем, что тип ожидаемого значения совпадает с типом в types
        Class<?> actualType = types.get(index);
        if (!expectedType.isAssignableFrom(actualType)) {
            throw new ClassCastException("Cannot cast element of type " + actualType.getName() +
                " to expected type " + expectedType.getName());
        }

        // Возвращаем элемент, приведенный к нужному типу
        return expectedType.cast(elements.get(index));
    }
}
