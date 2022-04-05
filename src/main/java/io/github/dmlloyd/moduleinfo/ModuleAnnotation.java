package io.github.dmlloyd.moduleinfo;

import java.util.Map;

/**
 *
 */
public class ModuleAnnotation {
    private String type;
    private boolean visible = true;
    private Map<String, Object> values;

    public String getType() {
        return type;
    }

    public void setType(final String type) {
        this.type = type;
    }

    public boolean isVisible() {
        return visible;
    }

    public void setVisible(final boolean visible) {
        this.visible = visible;
    }

    public Map<String, Object> getValues() {
        return values;
    }

    public void setValues(final Map<String, Object> values) {
        this.values = values;
    }
}
