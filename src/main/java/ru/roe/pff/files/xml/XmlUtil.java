package ru.roe.pff.files.xml;

import lombok.experimental.UtilityClass;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@UtilityClass
public final class XmlUtil {

    public static final String[] FIELD_ORDER = {
            "id",
            "available",
            "price",
            "currencyId",
            "categoryId",
            "picture",
            "name",
            "vendor",
            "description",
            "barcode",
            "param_Артикул",
            "param_Рейтинг",
            "param_Количество отзывов",
            "param_Скидка",
            "param_Новинка"
    };

    public static List<String> getColumnNames() {
        return Collections.unmodifiableList(Arrays.asList(FIELD_ORDER));
    }

}
