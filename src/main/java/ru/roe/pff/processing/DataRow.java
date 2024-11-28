package ru.roe.pff.processing;

import lombok.Data;

import java.util.List;

@Data
public class DataRow {
    private final List<String> data;
    private final int index;
}