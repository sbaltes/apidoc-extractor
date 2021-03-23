package dev.baltes.apidoc.extractor;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.QuoteMode;

class ApiDocumentation {
    static final CSVFormat csvFormat = CSVFormat.DEFAULT // uses CRLF as line separator
            .withHeader("repo", "file", "method", "path", "documentation", "notes")
            .withDelimiter(',')
            .withQuote('"')
            .withQuoteMode(QuoteMode.MINIMAL)
            .withTrim(true)
            .withNullString("");

    private String filename;
    private String repo;
    private String file;
    private String classpath;
    private String method;
    private String path;
    private String documentation;
    private String notes;

    public void setFilename(String filename) {
        this.filename = filename;
        this.repo = filename.split("#")[0].replace("$", "/");
        this.file = filename.split("#")[1].replace("$", "/");
    }

    public void setClassPath(String classpath) {
        this.classpath = classpath;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public void setDocumentation(String documentation) {
        if (this.documentation == null) {
            this.documentation = documentation;
        } else if (!this.documentation.equals(documentation)) {
            this.documentation += " + " + documentation;
        }
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public String getFilename() {
        return filename;
    }
    
    public String getRepo() {
        return repo;
    }

    public String getFile() {
        return file;
    }

    public String getClassPath() {
        return classpath == null ? "" : clean(classpath);
    }

    public String getMethod() {
        return method == null ? "" : clean(method);
    }

    public String getPath() {
        return path == null ? "" : clean(path);
    }

    public String getDocumentation() {
        return documentation == null ? "" : clean(documentation);
    }

    public String getNotes() {
        return notes == null ? "" : clean(notes);
    }

    public String getFullPath() {
        if (getPath().isEmpty()) {
            return "";
        }
        if (getClassPath().isEmpty()) {
            return getPath();
        }
        return String.format("%s/%s", getClassPath(), getPath()).replaceAll("/{2,}", "/");
    }

    @Override
    public String toString() {
        return String.format("\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\"",
                getRepo(), getFile(), getMethod(), getFullPath(), getDocumentation(), getNotes());
    }

    private String clean(String str) {
        return str.replaceAll("(?<!\\\\)((?:\\\\{2})*)\"\\s*\\+\\s*\"", "$1")
                .replaceAll("(?<!\\\\)((?:\\\\{2})*)\"\\s*\\+\\s*(?!\")", "$1 ")
                .replaceAll("(?<!\")\\s*\\+\\s*\"", " ")
                .replaceAll("^\"", "")
                .replaceAll("\"$", "");
    }

    public boolean isEmpty() {
        return getMethod().isEmpty()
                && getFullPath().isEmpty()
                && getDocumentation().isEmpty()
                && getNotes().isEmpty();
    }
}
