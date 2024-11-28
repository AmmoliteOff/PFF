package ru.roe.pff.files;

import ru.roe.pff.processing.DataRow;
import ru.roe.pff.processing.DataRowValidator;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

public abstract class FileParser {

    public abstract List<DataRow> parse(DataRowValidator validator, InputStream input) throws IOException;

}