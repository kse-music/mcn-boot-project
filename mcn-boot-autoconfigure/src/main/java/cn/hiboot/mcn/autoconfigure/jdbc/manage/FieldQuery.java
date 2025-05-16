package cn.hiboot.mcn.autoconfigure.jdbc.manage;


/**
 * FieldQuery
 *
 * @author DingHao
 * @since 2025/5/15 15:01
 */
public class FieldQuery {

    private String name;

    private Operator operator;

    private Object value;

    public FieldQuery() {
    }

    public FieldQuery(String name, Operator operator, Object value) {
        this.name = name;
        this.operator = operator;
        this.value = value;
    }

    public enum Operator {

        eq("="),
        gt(">"),
        gte(">="),
        lt("<"),
        lte("<="),
        ne("!="),
        like("like"),
        in("in"),
        ;

        private final String operator;

        Operator(String operator) {
            this.operator = operator;
        }

        public String valueString() {
            return operator;
        }

    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Operator getOperator() {
        return operator;
    }

    public void setOperator(Operator operator) {
        this.operator = operator;
    }

    public Object getValue() {
        return value;
    }

    public void setValue(Object value) {
        this.value = value;
    }
}
