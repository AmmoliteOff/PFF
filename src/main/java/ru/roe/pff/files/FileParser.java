package ru.roe.pff.files;

import ru.roe.pff.processing.DataRow;

import java.io.InputStream;
import java.util.List;
import java.util.UUID;

public abstract class FileParser {

    public abstract List<DataRow> parse(UUID fileId, InputStream input);

    public abstract List<DataRow> parseFrom(int begin, int end, InputStream input);
}