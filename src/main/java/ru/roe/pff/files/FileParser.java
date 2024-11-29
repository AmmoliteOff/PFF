package ru.roe.pff.files;

import ru.roe.pff.entity.FeedFile;
import ru.roe.pff.processing.DataRow;
import ru.roe.pff.processing.DataRowValidator;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.UUID;

public abstract class FileParser {

    public abstract Integer parse(UUID fileId, InputStream input);

    public abstract List<DataRow> parseFrom(int begin, int end, InputStream input);
}