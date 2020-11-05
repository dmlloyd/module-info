package io.github.dmlloyd.moduleinfo;

import java.util.jar.Attributes;
import java.util.jar.Manifest;

/**
 */
public class DetectVersionClassPathVisitor extends ClassPathVisitor {
    private final Item<String> versionItem;

    public DetectVersionClassPathVisitor(final ClassPathVisitor cpv, final Item<String> versionItem) {
        super(cpv);
        this.versionItem = versionItem;
    }

    public DetectVersionClassPathVisitor(final Item<String> versionItem) {
        this(null, versionItem);
    }

    public ManifestVisitor visitManifest() {
        final ManifestVisitor mv = super.visitManifest();
        // check the manifest
        return new ManifestVisitor(mv) {
            public void visit(final Manifest manifest) {
                final Object version = manifest.getMainAttributes().get(Attributes.Name.IMPLEMENTATION_VERSION);
                if (version != null) {
                    versionItem.set(version.toString());
                }
                super.visit(manifest);
            }
        };
    }
}
