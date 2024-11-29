package ru.roe.pff.processing;

import lombok.Data;

import java.util.List;
import java.util.Objects;

@Data
public class DataRow {
    private final List<String> data;
    private final int index;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DataRow dataRow = (DataRow) o;
        return Objects.equals(data, dataRow.data);
    }

    @Override
    public int hashCode() {
        return Objects.hash(data);
    }
}