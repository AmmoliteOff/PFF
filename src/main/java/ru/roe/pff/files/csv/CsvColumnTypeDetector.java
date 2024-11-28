package ru.roe.pff.files.csv;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

public class CsvColumnTypeDetector {

    public static List<Class<?>> getColumnTypes(List<String> elements) {
        List<Class<?>> columnTypes = new ArrayList<>();

        // Для каждой колонки (элемента строки) определяем её тип
        for (Object element : elements) {
            columnTypes.add(determineElementType(element));
        }

        return columnTypes;
    }

    // Определение типа элемента
    private static Class<?> determineElementType(Object element) {
        if (element == null) {
            return String.class;  // Если элемент null, выбираем String
        }

        // Попытка преобразовать элемент в тип Double
        if (element instanceof String value) {
            if (isValidNumber(value)) {
                return Double.class;
            } else if (isValidDate(value)) {
                return LocalDate.class;
            } else {
                return String.class;
            }
        }

        // Если элемент уже является числом или датой, возвращаем его тип
        if (element instanceof Double) {
            return Double.class;
        }
        if (element instanceof Integer) {
            return Integer.class;
        }
        if (element instanceof LocalDate) {
            return LocalDate.class;
        }

        // Если тип не определён, возвращаем String
        return String.class;
    }

    // Проверка на число
    private static boolean isValidNumber(String value) {
        try {
            Double.parseDouble(value);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    // Проверка на дату (формат: yyyy-MM-dd)
    private static boolean isValidDate(String value) {
        try {
            LocalDate.parse(value);
            return true;
        } catch (DateTimeParseException e) {
            return false;
        }
    }
}

