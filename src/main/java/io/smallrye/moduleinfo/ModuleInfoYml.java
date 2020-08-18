package io.smallrye.moduleinfo;

import java.util.List;
import java.util.Map;

/**
 *
 */
public class ModuleInfoYml {
    private String name;
    private String version;
    private boolean open;
    private boolean synthetic;
    private boolean mandated;
    private String sourceFile;
    private String mainClass;
    private List<String> packages;
    private List<Require> requires;
    private List<Export> exports;
    private List<Export> opens;
    private List<String> uses;
    private List<Provide> provides;
    private List<Annotation> annotations;

    public ModuleInfoYml() {
    }

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(final String version) {
        this.version = version;
    }

    public boolean isOpen() {
        return open;
    }

    public void setOpen(final boolean open) {
        this.open = open;
    }

    public boolean isSynthetic() {
        return synthetic;
    }

    public void setSynthetic(final boolean synthetic) {
        this.synthetic = synthetic;
    }

    public boolean isMandated() {
        return mandated;
    }

    public void setMandated(final boolean mandated) {
        this.mandated = mandated;
    }

    public String getSourceFile() {
        return sourceFile;
    }

    public void setSourceFile(final String sourceFile) {
        this.sourceFile = sourceFile;
    }

    public String getMainClass() {
        return mainClass;
    }

    public void setMainClass(final String mainClass) {
        this.mainClass = mainClass;
    }

    public List<String> getPackages() {
        return packages;
    }

    public void setPackages(final List<String> packages) {
        this.packages = packages;
    }

    public List<Require> getRequires() {
        return requires;
    }

    public void setRequires(final List<Require> requires) {
        this.requires = requires;
    }

    public List<Export> getExports() {
        return exports;
    }

    public void setExports(final List<Export> exports) {
        this.exports = exports;
    }

    public List<Export> getOpens() {
        return opens;
    }

    public void setOpens(final List<Export> opens) {
        this.opens = opens;
    }

    public List<String> getUses() {
        return uses;
    }

    public void setUses(final List<String> uses) {
        this.uses = uses;
    }

    public List<Provide> getProvides() {
        return provides;
    }

    public void setProvides(final List<Provide> provides) {
        this.provides = provides;
    }

    public List<Annotation> getAnnotations() {
        return annotations;
    }

    public void setAnnotations(final List<Annotation> annotations) {
        this.annotations = annotations;
    }

    public static class Require {
        private String module;
        private String version;
        private boolean static_;
        private boolean synthetic;
        private boolean mandated;
        private boolean transitive;

        public String getModule() {
            return module;
        }

        public void setModule(final String module) {
            this.module = module;
        }

        public String getVersion() {
            return version;
        }

        public void setVersion(final String version) {
            this.version = version;
        }

        public boolean isStatic() {
            return static_;
        }

        public void setStatic(final boolean static_) {
            this.static_ = static_;
        }

        public boolean isSynthetic() {
            return synthetic;
        }

        public void setSynthetic(final boolean synthetic) {
            this.synthetic = synthetic;
        }

        public boolean isMandated() {
            return mandated;
        }

        public void setMandated(final boolean mandated) {
            this.mandated = mandated;
        }

        public boolean isTransitive() {
            return transitive;
        }

        public void setTransitive(final boolean transitive) {
            this.transitive = transitive;
        }
    }

    public static class Export {
        private String package_;
        private List<String> to;
        private boolean synthetic;
        private boolean mandated;

        public String getPackage() {
            return package_;
        }

        public void setPackage(final String package_) {
            this.package_ = package_;
        }

        public List<String> getTo() {
            return to;
        }

        public void setTo(final List<String> to) {
            this.to = to;
        }

        public boolean isSynthetic() {
            return synthetic;
        }

        public void setSynthetic(final boolean synthetic) {
            this.synthetic = synthetic;
        }

        public boolean isMandated() {
            return mandated;
        }

        public void setMandated(final boolean mandated) {
            this.mandated = mandated;
        }
    }

    public static class Provide {
        private String serviceType;
        private List<String> with;

        public String getServiceType() {
            return serviceType;
        }

        public void setServiceType(final String serviceType) {
            this.serviceType = serviceType;
        }

        public List<String> getWith() {
            return with;
        }

        public void setWith(final List<String> with) {
            this.with = with;
        }
    }

    public static class Annotation {
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
}
