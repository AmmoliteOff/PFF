package ru.roe.pff.files;

import ru.roe.pff.processing.DataRowValidator;

import java.io.IOException;
import java.io.InputStream;

public abstract class FileParser {

    public abstract void parse(DataRowValidator validator, InputStream input) throws IOException;

}