package ru.roe.pff.files;

import ru.roe.pff.processing.DataRow;
import ru.roe.pff.processing.DataRowValidator;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

public abstract class FileParser {

    public abstract Integer parse(DataRowValidator validator, InputStream input) throws IOException;

    public abstract List<DataRow> parseFrom(int begin, int end, InputStream input) throws IOException;

}