/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2018 Red Hat, Inc., and individual contributors
 * as indicated by the @author tags.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jboss.moduleinfo;

import static javax.xml.stream.XMLStreamConstants.*;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ModuleVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

/**
 * A {@code module-info.xml} parser.
 */
public class ModuleInfoXmlParser implements ClassVisitable<Exception> {
    private static final String[] NO_STRINGS = new String[0];
    private final Path moduleInfoXml;

    /**
     * Construct a new instance.
     *
     * @param moduleInfoXml the path to the {@code module-info.xml} file
     */
    public ModuleInfoXmlParser(final Path moduleInfoXml) {
        Objects.requireNonNull(moduleInfoXml, "moduleInfoXml");
        this.moduleInfoXml = moduleInfoXml;
    }

    /**
     * Read the {@code module-info.xml} file into the given class visitor.
     *
     * @param classVisitor the class visitor (must not be {@code null})
     * @throws IOException if an I/O exception occurred
     * @throws XMLStreamException if a parsing exception occurred
     */
    public void accept(final ClassVisitor classVisitor) throws IOException, XMLStreamException {
        try (InputStream inputStream = Files.newInputStream(moduleInfoXml, StandardOpenOption.READ)) {
            final XMLInputFactory xmlInputFactory = XMLInputFactory.newFactory();
            final XMLStreamReader reader = xmlInputFactory.createXMLStreamReader(inputStream);
            final int eventType = reader.nextTag();
            if (eventType != START_ELEMENT) {
                throw unknownContent(reader);
            }
            parseRootElement(reader, classVisitor);
        }
    }

    private void parseRootElement(final XMLStreamReader reader, final ClassVisitor classVisitor) throws XMLStreamException {
        if (! reader.getLocalName().equals("module-info")) {
            throw unknownContent(reader);
        }
        parseModuleInfoType(reader, classVisitor);
    }

    private void parseModuleInfoType(final XMLStreamReader reader, final ClassVisitor classVisitor) throws XMLStreamException {
        classVisitor.visit(Opcodes.V9, Opcodes.ACC_MODULE, "module-info", null, null, null);
        String name = null;
        String version = null;
        boolean open = false;
        boolean synthetic = false;
        boolean mandated = false;
        final int attrs = reader.getAttributeCount();
        for (int i = 0; i < attrs; i ++) {
            String localName = reader.getAttributeLocalName(i);
            if ("name".equals(localName)) {
                name = reader.getAttributeValue(i);
            } else if ("version".equals(localName)) {
                version = reader.getAttributeValue(i);
            } else if ("open".equals(localName)) {
                open = Boolean.parseBoolean(reader.getAttributeValue(i));
            } else if ("synthetic".equals(localName)) {
                synthetic = Boolean.parseBoolean(reader.getAttributeValue(i));
            } else if ("mandated".equals(localName)) {
                mandated = Boolean.parseBoolean(reader.getAttributeValue(i));
            } else {
                throw unknownAttribute(reader, i);
            }
        }
        if (name == null) throw missingRequiredAttribute(reader, "name");
        int eventType = reader.nextTag();
        if (eventType == START_ELEMENT && reader.getLocalName().equals("source-file")) {
            classVisitor.visitSource(parseNameType(reader), null);
            eventType = reader.nextTag();
        } else {
            classVisitor.visitSource(moduleInfoXml.getFileName().toString(), null);
        }
        int flags = 0;
        if (open) flags |= Opcodes.ACC_OPEN;
        if (synthetic) flags |= Opcodes.ACC_SYNTHETIC;
        if (mandated) flags |= Opcodes.ACC_MANDATED;
        final ModuleVisitor moduleVisitor = classVisitor.visitModule(name, flags, version);
        if (eventType == START_ELEMENT && reader.getLocalName().equals("main-class")) {
            moduleVisitor.visitMainClass(parseNameType(reader).replace('.', '/'));
            eventType = reader.nextTag();
        }
        while (eventType == START_ELEMENT) {
            if (reader.getLocalName().equals("package")) {
                moduleVisitor.visitPackage(parseNameType(reader).replace('.', '/'));
            } else if (reader.getLocalName().equals("require")) {
                parseRequireType(reader, moduleVisitor);
            } else if (reader.getLocalName().equals("export")) {
                parseExportType(reader, moduleVisitor, false);
            } else if (reader.getLocalName().equals("open")) {
                parseExportType(reader, moduleVisitor, true);
            } else if (reader.getLocalName().equals("use")) {
                moduleVisitor.visitUse(parseNameType(reader).replace('.', '/'));
            } else if (reader.getLocalName().equals("provide")) {
                parseProvideType(reader, moduleVisitor);
            } else {
                break;
            }
            eventType = reader.getEventType();
        }
        moduleVisitor.visitEnd();
        while (eventType == START_ELEMENT && reader.getLocalName().equals("annotation")) {
            parseAnnotationType(reader, classVisitor);
            eventType = reader.nextTag();
        }
        if (eventType != END_ELEMENT) {
            throw unknownContent(reader);
        }
        classVisitor.visitEnd();
    }

    private String parseNameType(final XMLStreamReader reader) throws XMLStreamException {
        String name = null;
        final int attrs = reader.getAttributeCount();
        for (int i = 0; i < attrs; i ++) {
            String localName = reader.getAttributeLocalName(i);
            if ("name".equals(localName)) {
                name = reader.getAttributeValue(i);
            } else {
                throw unknownAttribute(reader, i);
            }
        }
        if (name == null) throw missingRequiredAttribute(reader, "name");
        if (reader.nextTag() != END_ELEMENT) throw unknownContent(reader);
        return name;
    }

    private void parseRequireType(final XMLStreamReader reader, final ModuleVisitor moduleVisitor) throws XMLStreamException {
        String moduleName = null;
        boolean transitive = false;
        boolean _static = false;
        boolean synthetic = false;
        boolean mandated = false;
        String version = null;
        final int attrs = reader.getAttributeCount();
        for (int i = 0; i < attrs; i ++) {
            String localName = reader.getAttributeLocalName(i);
            if ("module-name".equals(localName)) {
                moduleName = reader.getAttributeValue(i);
            } else if ("transitive".equals(localName)) {
                transitive = Boolean.parseBoolean(reader.getAttributeValue(i));
            } else if ("static".equals(localName)) {
                _static = Boolean.parseBoolean(reader.getAttributeValue(i));
            } else if ("synthetic".equals(localName)) {
                synthetic = Boolean.parseBoolean(reader.getAttributeValue(i));
            } else if ("mandated".equals(localName)) {
                mandated = Boolean.parseBoolean(reader.getAttributeValue(i));
            } else if ("version".equals(localName)) {
                version = reader.getAttributeValue(i);
            } else {
                throw unknownAttribute(reader, i);
            }
        }
        if (moduleName == null) throw missingRequiredAttribute(reader, "module-name");
        int flags = 0;
        if (transitive) flags |= Opcodes.ACC_TRANSITIVE;
        if (_static) flags |= Opcodes.ACC_STATIC_PHASE;
        if (synthetic) flags |= Opcodes.ACC_SYNTHETIC;
        if (mandated) flags |= Opcodes.ACC_MANDATED;
        moduleVisitor.visitRequire(moduleName, flags, version);
        if (reader.nextTag() != END_ELEMENT) throw unknownContent(reader);
    }

    private void parseExportType(final XMLStreamReader reader, final ModuleVisitor moduleVisitor, final boolean open) throws XMLStreamException {
        String packageName = null;
        boolean synthetic = false;
        boolean mandated = false;
        final int attrs = reader.getAttributeCount();
        for (int i = 0; i < attrs; i ++) {
            String localName = reader.getAttributeLocalName(i);
            if ("package-name".equals(localName)) {
                packageName = reader.getAttributeValue(i).replace('/', '.');
            } else if ("synthetic".equals(localName)) {
                synthetic = Boolean.parseBoolean(reader.getAttributeValue(i));
            } else if ("mandated".equals(localName)) {
                mandated = Boolean.parseBoolean(reader.getAttributeValue(i));
            } else {
                throw unknownAttribute(reader, i);
            }
        }
        if (packageName == null) throw missingRequiredAttribute(reader, "package-name");
        int flags = 0;
        if (synthetic) flags |= Opcodes.ACC_SYNTHETIC;
        if (mandated) flags |= Opcodes.ACC_MANDATED;
        final String[] modules;
        if (reader.nextTag() == START_ELEMENT) {
            List<String> moduleNames = new ArrayList<>();
            do {
                if (! reader.getLocalName().equals("to-module")) throw unknownContent(reader);
                moduleNames.add(parseNameType(reader));
            } while (reader.nextTag() == START_ELEMENT);
            modules = moduleNames.toArray(NO_STRINGS);
        } else {
            modules = null;
        }
        if (open) {
            moduleVisitor.visitOpen(packageName, flags, modules);
        } else {
            moduleVisitor.visitExport(packageName, flags, modules);
        }
        if (reader.nextTag() != END_ELEMENT) throw unknownContent(reader);
    }

    private void parseProvideType(final XMLStreamReader reader, final ModuleVisitor moduleVisitor) throws XMLStreamException {
        String serviceName = null;
        final int attrs = reader.getAttributeCount();
        for (int i = 0; i < attrs; i ++) {
            String localName = reader.getAttributeLocalName(i);
            if ("service-name".equals(localName)) {
                serviceName = reader.getAttributeValue(i).replace('.', '/');
            } else {
                throw unknownAttribute(reader, i);
            }
        }
        if (serviceName == null) throw missingRequiredAttribute(reader, "service-name");
        final String[] providers;
        if (reader.nextTag() == START_ELEMENT) {
            List<String> providerNames = new ArrayList<>();
            do {
                if (! reader.getLocalName().equals("with-class")) throw unknownContent(reader);
                providerNames.add(parseNameType(reader).replace('.', '/'));
            } while (reader.nextTag() == START_ELEMENT);
            providers = providerNames.toArray(NO_STRINGS);
        } else {
            throw missingRequiredElement(reader, "with-class");
        }
        moduleVisitor.visitProvide(serviceName, providers);
    }

    private void parseAnnotationType(final XMLStreamReader reader, final ClassVisitor classVisitor) throws XMLStreamException {
        String name = null;
        boolean runtime = false;
        final int attrs = reader.getAttributeCount();
        for (int i = 0; i < attrs; i ++) {
            String localName = reader.getAttributeLocalName(i);
            if ("name".equals(localName)) {
                name = reader.getAttributeValue(i).replace('.', '/');
                if (name.indexOf('/') == - 1) {
                    name = "java/lang/" + name;
                }
            } else if ("retention".equals(localName)) {
                String value = reader.getAttributeValue(i);
                if ("runtime".equals(value)) {
                    runtime = true;
                } else if ("class".equals(value)) {
                    runtime = false;
                } else {
                    throw unknownAttributeValue(reader, i);
                }
            } else {
                throw unknownAttribute(reader, i);
            }
        }
        if (name == null) throw missingRequiredAttribute(reader, "name");
        final AnnotationVisitor annotationVisitor = classVisitor.visitAnnotation(Type.getObjectType(name).getDescriptor(), runtime);
        while (reader.nextTag() == START_ELEMENT) {
            if (! "parameter".equals(reader.getLocalName())) throw unknownContent(reader);
            parseAnnotationParameterType(reader, annotationVisitor);
        }
    }

    private void parseAnnotationParameterType(final XMLStreamReader reader, final AnnotationVisitor annotationVisitor) throws XMLStreamException {
        String name = "value";
        final int attrs = reader.getAttributeCount();
        for (int i = 0; i < attrs; i ++) {
            String localName = reader.getAttributeLocalName(i);
            if ("name".equals(localName)) {
                name = reader.getAttributeValue(i);
            } else {
                throw unknownAttribute(reader, i);
            }
        }
        parseAnnotationContent(reader, name, annotationVisitor);
    }

    private void parseAnnotationContent(final XMLStreamReader reader, final String name, final AnnotationVisitor annotationVisitor) throws XMLStreamException {
        while (reader.nextTag() == START_ELEMENT) {
            final String localName = reader.getLocalName();
            if (localName.equals("byte")) {
                annotationVisitor.visit(name, Byte.valueOf((byte) parseIntegerAnnotationParameterValueType(reader)));
            } else if (localName.equals("short")) {
                annotationVisitor.visit(name, Short.valueOf((short) parseIntegerAnnotationParameterValueType(reader)));
            } else if (localName.equals("int")) {
                annotationVisitor.visit(name, Integer.valueOf((int) parseIntegerAnnotationParameterValueType(reader)));
            } else if (localName.equals("long")) {
                annotationVisitor.visit(name, Long.valueOf(parseIntegerAnnotationParameterValueType(reader)));
            } else if (localName.equals("char")) {
                annotationVisitor.visit(name, Character.valueOf((char) parseIntegerAnnotationParameterValueType(reader)));
            } else if (localName.equals("float")) {
                annotationVisitor.visit(name, Float.valueOf((float) parseFloatAnnotationParameterValueType(reader)));
            } else if (localName.equals("double")) {
                annotationVisitor.visit(name, Double.valueOf(parseFloatAnnotationParameterValueType(reader)));
            } else if (localName.equals("string")) {
                annotationVisitor.visit(name, parseStringAnnotationParameterValueType(reader));
            } else if (localName.equals("enum")) {
                parseEnumAnnotationParameterValueType(reader, annotationVisitor, name);
            } else if (localName.equals("array")) {
                parseArrayAnnotationParameterValueType(reader, annotationVisitor, name);
            } else if (localName.equals("class")) {
                parseClassAnnotationParameterValueType(reader, annotationVisitor, name);
            } else if (localName.equals("annotation")) {
                parseAnnotationAnnotationParameterValueType(reader, annotationVisitor, name);
            } else {
                throw unknownContent(reader);
            }
        }
    }

    private long parseIntegerAnnotationParameterValueType(final XMLStreamReader reader) throws XMLStreamException {
        boolean gotValue = false;
        long value = 0;
        final int attrs = reader.getAttributeCount();
        for (int i = 0; i < attrs; i ++) {
            String localName = reader.getAttributeLocalName(i);
            if ("value".equals(localName)) {
                gotValue = true;
                String str = reader.getAttributeValue(i).trim();
                boolean neg = false;
                if (str.startsWith("-")) {
                    neg = true;
                    str = str.substring(1);
                }
                try {
                    if (str.startsWith("0b")) {
                        value = Long.parseLong(str.substring(2).replaceAll("_", ""), 2);
                    } else if (str.startsWith("0x")) {
                        value = Long.parseLong(str.substring(2).replaceAll("_", ""), 16);
                    } else if (str.startsWith("0")) {
                        value = Long.parseLong(str.substring(1).replaceAll("_", ""), 8);
                    } else {
                        value = Long.parseLong(str.replaceAll("_", ""));
                    }
                } catch (NumberFormatException e) {
                    throw unknownAttributeValue(reader, i);
                }
                if (neg) value = -value;
            } else {
                throw unknownAttribute(reader, i);
            }
        }
        if (! gotValue) throw missingRequiredAttribute(reader, "value");
        return value;
    }

    private double parseFloatAnnotationParameterValueType(final XMLStreamReader reader) throws XMLStreamException {
        boolean gotValue = false;
        double value = 0;
        final int attrs = reader.getAttributeCount();
        for (int i = 0; i < attrs; i ++) {
            String localName = reader.getAttributeLocalName(i);
            if ("value".equals(localName)) {
                gotValue = true;
                String str = reader.getAttributeValue(i).trim();
                try {
                    value = Double.parseDouble(str);
                } catch (NumberFormatException e) {
                    throw unknownAttributeValue(reader, i);
                }
            } else {
                throw unknownAttribute(reader, i);
            }
        }
        if (! gotValue) throw missingRequiredAttribute(reader, "value");
        return value;
    }

    private String parseStringAnnotationParameterValueType(final XMLStreamReader reader) throws XMLStreamException {
        String value = null;
        final int attrs = reader.getAttributeCount();
        for (int i = 0; i < attrs; i ++) {
            String localName = reader.getAttributeLocalName(i);
            if ("value".equals(localName)) {
                value = reader.getAttributeValue(i);
            } else {
                throw unknownAttribute(reader, i);
            }
        }
        if (value == null) throw missingRequiredAttribute(reader, "value");
        if (reader.nextTag() != END_ELEMENT) throw unknownContent(reader);
        return value;
    }

    private void parseEnumAnnotationParameterValueType(final XMLStreamReader reader, final AnnotationVisitor annotationVisitor, final String paramName) throws XMLStreamException {
        String name = null;
        String value = null;
        final int attrs = reader.getAttributeCount();
        for (int i = 0; i < attrs; i ++) {
            String localName = reader.getAttributeLocalName(i);
            if ("name".equals(localName)) {
                name = Type.getObjectType(reader.getAttributeValue(i).replace('.', '/')).getDescriptor();
            } else if ("value".equals(localName)) {
                value = reader.getAttributeValue(i);
            } else {
                throw unknownAttribute(reader, i);
            }
        }
        if (name == null) throw missingRequiredAttribute(reader, "name");
        if (value == null) throw missingRequiredAttribute(reader, "value");
        annotationVisitor.visitEnum(paramName, name, value);
        if (reader.nextTag() != END_ELEMENT) throw unknownContent(reader);
    }

    private void parseArrayAnnotationParameterValueType(final XMLStreamReader reader, final AnnotationVisitor outer, final String name) throws XMLStreamException {
        final AnnotationVisitor annotationVisitor = outer.visitArray(name);
        if (reader.getAttributeCount() > 0) throw unknownAttributeValue(reader, 0);
        if (reader.nextTag() == START_ELEMENT) {
            final String localName = reader.getLocalName();
            do {
                if (! localName.equals(reader.getLocalName())) {
                    throw unknownContent(reader);
                }
                if (localName.equals("byte")) {
                    annotationVisitor.visit(name, Byte.valueOf((byte) parseIntegerAnnotationParameterValueType(reader)));
                } else if (localName.equals("short")) {
                    annotationVisitor.visit(name, Short.valueOf((short) parseIntegerAnnotationParameterValueType(reader)));
                } else if (localName.equals("int")) {
                    annotationVisitor.visit(name, Integer.valueOf((int) parseIntegerAnnotationParameterValueType(reader)));
                } else if (localName.equals("long")) {
                    annotationVisitor.visit(name, Long.valueOf(parseIntegerAnnotationParameterValueType(reader)));
                } else if (localName.equals("char")) {
                    annotationVisitor.visit(name, Character.valueOf((char) parseIntegerAnnotationParameterValueType(reader)));
                } else if (localName.equals("float")) {
                    annotationVisitor.visit(name, Float.valueOf((float) parseFloatAnnotationParameterValueType(reader)));
                } else if (localName.equals("double")) {
                    annotationVisitor.visit(name, Double.valueOf(parseFloatAnnotationParameterValueType(reader)));
                } else if (localName.equals("String")) {
                    annotationVisitor.visit(name, parseStringAnnotationParameterValueType(reader));
                } else if (localName.equals("enum")) {
                    parseEnumAnnotationParameterValueType(reader, annotationVisitor, name);
                } else if (localName.equals("array")) {
                    parseArrayAnnotationParameterValueType(reader, annotationVisitor, name);
                } else if (localName.equals("class")) {
                    parseClassAnnotationParameterValueType(reader, annotationVisitor, name);
                } else if (localName.equals("annotation")) {
                    parseAnnotationAnnotationParameterValueType(reader, annotationVisitor, name);
                } else {
                    throw unknownContent(reader);
                }
            } while (reader.nextTag() == START_ELEMENT);
        }
    }

    private void parseClassAnnotationParameterValueType(final XMLStreamReader reader, final AnnotationVisitor annotationVisitor, final String paramName) throws XMLStreamException {
        String name = null;
        int dim = 0;
        final int attrs = reader.getAttributeCount();
        for (int i = 0; i < attrs; i ++) {
            String localName = reader.getAttributeLocalName(i);
            if ("name".equals(localName)) {
                name = reader.getAttributeValue(i).replace('.', '/');
                if (name.indexOf('/') == - 1) {
                    name = "java/lang/" + name;
                }
            } else if ("array-dimensions".equals(localName)) {
                dim = Integer.parseInt(reader.getAttributeValue(i));
            } else {
                throw unknownAttribute(reader, i);
            }
        }
        if (name == null) throw missingRequiredAttribute(reader, "name");
        for (int i = 0; i < dim; i ++) {
            name = "[" + name;
        }
        annotationVisitor.visit(paramName, Type.getObjectType(name));
    }

    private void parseAnnotationAnnotationParameterValueType(final XMLStreamReader reader, final AnnotationVisitor annotationVisitor, final String paramName) throws XMLStreamException {
        String name = null;
        final int attrs = reader.getAttributeCount();
        for (int i = 0; i < attrs; i ++) {
            String localName = reader.getAttributeLocalName(i);
            if ("name".equals(localName)) {
                name = reader.getAttributeValue(i).replace('.', '/');
                if (name.indexOf('/') == -1) {
                    name = "java/lang/" + name;
                }
            } else {
                throw unknownAttribute(reader, i);
            }
        }
        if (name == null) throw missingRequiredAttribute(reader, "name");
        final AnnotationVisitor nested = annotationVisitor.visitAnnotation(paramName, Type.getObjectType(name).getDescriptor());
        while (reader.nextTag() == START_ELEMENT) {
            if (! "parameter".equals(reader.getLocalName())) throw unknownContent(reader);
            parseAnnotationParameterType(reader, nested);
        }
    }

    private XMLStreamException unknownContent(final XMLStreamReader reader) {
        return new XMLStreamException(String.format("%s:%d: Unknown content", moduleInfoXml, Integer.valueOf(reader.getLocation().getLineNumber())));
    }

    private XMLStreamException unknownAttribute(final XMLStreamReader reader, final int idx) {
        return new XMLStreamException(String.format("%s:%d: Unknown attribute \"%s\"", moduleInfoXml, Integer.valueOf(reader.getLocation().getLineNumber()), reader.getAttributeName(idx)));
    }

    private XMLStreamException unknownAttributeValue(final XMLStreamReader reader, final int idx) {
        return new XMLStreamException(String.format("%s:%d: Unknown value \"%s\" given for attribute \"%s\"", moduleInfoXml, Integer.valueOf(reader.getLocation().getLineNumber()), reader.getAttributeValue(idx), reader.getAttributeName(idx)));
    }

    private XMLStreamException missingRequiredAttribute(final XMLStreamReader reader, final String name) {
        return new XMLStreamException(String.format("%s:%d: Missing required attribute \"%s\"", moduleInfoXml, Integer.valueOf(reader.getLocation().getLineNumber()), name));
    }

    private XMLStreamException missingRequiredElement(final XMLStreamReader reader, final String name) {
        return new XMLStreamException(String.format("%s:%d: Missing required element \"%s\"", moduleInfoXml, Integer.valueOf(reader.getLocation().getLineNumber()), name));
    }

}
