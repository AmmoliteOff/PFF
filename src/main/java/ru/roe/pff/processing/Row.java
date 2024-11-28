package ru.roe.pff.processing;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

@Data
public class Row {
    private int index;
    private List<Object> elements = new ArrayList<>();
    private List<Class<?>> types = new ArrayList<>();

    public void setElements(List<Object> elements, List<Class<?>> types) {
        this.types = types;
        for (int i = 0; i < elements.size(); i++) {
            this.elements.add(cast(types.get(i), elements.get(i)));
        }
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


    private <T> T cast(Class<T> expectedType, Object element) {
        if (expectedType == String.class) {
            return expectedType.cast(element); // Строка не требует преобразования
        }

        if (expectedType == Double.class && element instanceof String) {
            try {
                return expectedType.cast(Double.parseDouble((String) element));
            } catch (NumberFormatException e) {
                return null; // TODO техническая ошибка - значение не совпадает с ожидаемым типом
            }
        }

        if (expectedType == Integer.class && element instanceof String) {
            try {
                return expectedType.cast(Integer.parseInt((String) element));
            } catch (NumberFormatException e) {
                return null; // TODO техническая ошибка - значение не совпадает с ожидаемым типом
            }
        }

        if (expectedType == Boolean.class && element instanceof String) {
            return expectedType.cast(Boolean.parseBoolean((String) element));
        }

        if (expectedType == LocalDate.class && element instanceof String) {
            try {
                return expectedType.cast(LocalDate.parse((String) element));
            } catch (DateTimeParseException e) {
                return null; // TODO техническая ошибка - значение не совпадает с ожидаемым типом
            }
        }

        if (expectedType == LocalTime.class && element instanceof String) {
            try {
                return expectedType.cast(LocalTime.parse((String) element));
            } catch (DateTimeParseException e) {
                return null; // TODO техническая ошибка - значение не совпадает с ожидаемым типом
            }
        }

        if (expectedType == LocalDateTime.class && element instanceof String) {
            try {
                return expectedType.cast(LocalDateTime.parse((String) element));
            } catch (DateTimeParseException e) {
                return null; // TODO техническая ошибка - значение не совпадает с ожидаемым типом
            }
        }

        // Для других типов, если они совпадают с типом элемента, возвращаем его
        return expectedType.cast(element);
    }
}
