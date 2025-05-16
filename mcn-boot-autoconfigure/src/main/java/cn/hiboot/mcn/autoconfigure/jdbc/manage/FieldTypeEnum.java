package cn.hiboot.mcn.autoconfigure.jdbc.manage;


/**
 * FieldTypeEnum
 *
 * @author DingHao
 * @since 2025/5/15 15:01
 */
enum FieldTypeEnum {

    LONG("整数", 1),

    FLOAT("浮点", 2),

    DATA_TIME("日期时间", 4),

    DATE("日期", 41),

    SHORT_TEXT("短文本", 5),

    LONG_TEXT("文本型", 10);


    private final String field;
    private final int id;

    FieldTypeEnum(String field, int id) {
        this.field = field;
        this.id = id;
    }

    public String getField() {
        return field;
    }

    public int getId() {
        return id;
    }

    public static FieldTypeEnum of(int id) {
        for (FieldTypeEnum fieldTypeEnum : FieldTypeEnum.values()) {
            if (fieldTypeEnum.getId() == id) {
                return fieldTypeEnum;
            }
        }
        return null;
    }

}