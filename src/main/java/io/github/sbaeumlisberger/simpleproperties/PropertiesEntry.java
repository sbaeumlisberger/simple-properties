package io.github.sbaeumlisberger.simpleproperties;

public class PropertiesEntry {

    private String comment;
    private String key;
    private String value;

    public PropertiesEntry() {
        // entry of blank line
    }

    public PropertiesEntry(String comment) {
        this.comment = comment;
    }

    public PropertiesEntry(String key, String value) {
        this.key = key;
        this.value = value;
    }

    public boolean isBlank() {
        return key == null && comment == null;
    }

    public boolean isComment() {
        return comment != null;
    }

    public boolean isProperty() {
        return key != null;
    }

    public String getComment() {
        return comment;
    }

    void setComment(String comment) {
        this.comment = comment;
    }

    public String getKey() {
        return key;
    }

    void setKey(String key) {
        this.key = key;
    }

    public String getValue() {
        return value;
    }

    void setValue(String value) {
        this.value = value;
    }
}
