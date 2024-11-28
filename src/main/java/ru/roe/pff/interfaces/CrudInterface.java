package ru.roe.pff.interfaces;

import java.util.List;

public interface CrudInterface<IN, OUT, ID> {
    OUT get(ID id);

    List<OUT> getAll();

    OUT create(IN object);

    OUT update(ID id, IN object);

    void delete(ID id);
}
