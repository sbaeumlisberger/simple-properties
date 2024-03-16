package io.github.sbaeumlisberger.simpleproperties;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.*;
import java.util.function.Function;
import java.util.stream.IntStream;

public class SimpleProperties {
    private final List<PropertiesEntry> entries = new ArrayList<>();
    private final Map<String, String> valuesByKey = new HashMap<>();

    private final List<PropertiesPlugin> plugins;

    public SimpleProperties() {
        this.plugins = Collections.emptyList();
    }

    public SimpleProperties(PropertiesPlugin... plugins) {
        this.plugins = Arrays.asList(plugins);
    }

    public SimpleProperties(List<PropertiesPlugin> plugins) {
        this.plugins = List.copyOf(plugins);
    }

    public void load(BufferedReader reader) {
        Iterator<String> lines = reader.lines().iterator();
        int lineNumber = 1;
        while (lines.hasNext()) {
            String line = lines.next();
            entries.add(parseLine(line, lineNumber));
            lineNumber++;
        }
        entries.stream().filter(entry -> entry.getKey() != null)
                .forEach(entry -> valuesByKey.put(entry.getKey(), entry.getValue()));
    }

    public void load(InputStream stream, Charset charset) {
        load(new BufferedReader(new InputStreamReader(stream, charset)));
    }

    public void load(InputStream stream) {
        load(stream, StandardCharsets.UTF_8);
    }

    public void load(Path filePath, Charset charset) throws IOException {
        try (BufferedReader reader = Files.newBufferedReader(filePath, charset)) {
            load(reader);
        }
    }

    public void load(Path filePath) throws IOException {
        load(filePath, StandardCharsets.UTF_8);
    }

    public List<PropertiesEntry> getEntries() {
        return Collections.unmodifiableList(entries);
    }

    /**
     * Gets the value of the specified property or null if not present.
     */
    public String getProperty(String key) {
        checkNotNull(key, "key");
        return valuesByKey.get(key);
    }

    /**
     * Gets the value of the specified property or null if not present.
     */
    public <T> T getProperty(String key, Function<String, T> mapper) {
        checkNotNull(key, "key");
        String rawValue = valuesByKey.get(key);
        return rawValue != null ? mapper.apply(rawValue) : null;
    }

    /**
     * Gets the index of the specified property or -1 if not present.
     */
    public int getIndexOfProperty(String key) {
        checkNotNull(key, "key");
        return IntStream.range(0, entries.size())
                .filter(i -> key.equals(entries.get(i).getKey()))
                .findFirst().orElse(-1);
    }

    /**
     * Gets the index of the specified comment or -1 if not present.
     */
    public int getIndexOfComment(String comment) {
        checkNotNull(comment, "comment");
        return IntStream.range(0, entries.size())
                .filter(i -> comment.equals(entries.get(i).getComment()))
                .findFirst().orElse(-1);
    }

    public void appendProperty(String key, String value) {
        checkNotNull(key, "key");
        checkNotNull(value, "value");

        if (valuesByKey.containsKey(key)) {
            // TODO throw (flag remove if exists?)
        }

        valuesByKey.put(key, value);
        entries.add(new PropertiesEntry(key, value));
    }

    public void insertProperty(int index, String key, String value) {
        checkNotNull(key, "key");
        checkNotNull(value, "value");

        if (valuesByKey.containsKey(key)) {
            // TODO throw
        }

        valuesByKey.put(key, value);
        entries.add(index, new PropertiesEntry(key, value));
    }

    public void setProperty(String key, String value) {
        checkNotNull(key, "key");

        if (value == null) {
            removeProperty(key);
        }

        valuesByKey.put(key, value);

        var existingEntry = entries.stream().filter(entry -> key.equals(entry.getKey())).findFirst();

        if (existingEntry.isPresent()) {
            existingEntry.get().setValue(value);
        } else {
            entries.add(new PropertiesEntry(key, value));
        }
    }

    public void appendComment(String comment) {
        checkNotNull(comment, "comment");
        entries.add(new PropertiesEntry(comment));
    }

    public void insertComment(int index, String comment) {
        checkNotNull(comment, "comment");
        entries.add(index, new PropertiesEntry(comment));
    }

    public void removeEntry(int index) {
        PropertiesEntry entry = entries.remove(index);

        if (entry.getKey() != null) {
            valuesByKey.remove(entry.getKey());
        }
    }

    public boolean removeProperty(String key) {
        checkNotNull(key, "key");
        valuesByKey.remove(key);
        return entries.removeIf(entry -> key.equals(entry.getKey()));
    }

    public boolean removeComment(String comment) {
        checkNotNull(comment, "comment");
        return entries.removeIf(entry -> comment.equals(entry.getComment()));
    }

    public void removeAllComments() {
        entries.removeIf(entry -> entry.getKey() == null);
    }

    public void save(BufferedWriter writer) throws IOException {
        for (var entry : entries) {
            if (entry.isProperty()) {
                String key = entry.getKey();
                String value = entry.getValue();
                for (var plugin : plugins) {
                    value = plugin.onPropertyWrite(key, value);
                }
                writer.append(key).append(" = ").append(entry.getValue());
            } else if (entry.isComment()) {
                writer.append("# ").append(entry.getComment());
            }
            writer.newLine();
        }
    }

    public void save(OutputStream outputStream, Charset charset) throws IOException {
        save(new BufferedWriter(new OutputStreamWriter(outputStream, charset)));
    }

    public void save(OutputStream outputStream) throws IOException {
        save(outputStream, StandardCharsets.UTF_8);
    }

    public void save(Path file, Charset charset) throws IOException {
        try (BufferedWriter writer = Files.newBufferedWriter(file, charset, StandardOpenOption.WRITE,
                StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)) {
            save(writer);
        }
    }

    public void save(Path file) throws IOException {
        save(file, StandardCharsets.UTF_8);
    }

    private PropertiesEntry parseLine(String line, int lineNumber) {
        String[] keyAndValue = line.split("=", 2);
        if (keyAndValue.length == 2) {
            String key = keyAndValue[0].trim();
            String value = keyAndValue[1].trim();
            for (var plugin : plugins) {
                value = plugin.onPropertyRead(key, value);
            }
            return new PropertiesEntry(key, value);
        } else if (line.trim().startsWith("#")) {
            return new PropertiesEntry(line.trim().substring(1));
        } else if (line.isBlank()) {
            return new PropertiesEntry();
        } else {
            // TODO add exception type
            throw new RuntimeException("Failed to parse line " + lineNumber + ":" + line);
        }
    }

    private void checkNotNull(Object argument, String argumentName) {
        if (argument == null) {
            throw new IllegalArgumentException(argumentName + " must be not null");
        }
    }

}
