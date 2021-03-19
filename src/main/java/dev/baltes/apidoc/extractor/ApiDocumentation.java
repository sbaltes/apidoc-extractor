package dev.baltes.apidoc.extractor;

class ApiDocumentation {
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

    public void setClasspath(String classpath) {
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

    public String getClasspath() {
        return (classpath == null || clean(classpath).isBlank()) ? "" : clean(classpath);
    }

    public String getMethod() {
        return (method == null || clean(method).isBlank()) ? "n/a" : clean(method);
    }

    public String getPath() {
        return (path == null || clean(path).isBlank()) ? "" : clean(path);
    }

    public String getDocumentation() {
        return (documentation == null || clean(documentation).isBlank()) ? "n/a" : clean(documentation);
    }

    public String getNotes() {
        return (notes == null || clean(notes).isBlank()) ? "n/a" : clean(notes);
    }

    private String getFullpath() {
        if (getPath().isBlank()) return "n/a";
        if (getClasspath().isBlank()) return getPath();
        return String.format("%s/%s", getClasspath(), getPath()).replaceAll("/{2,}", "/");
    }

    @Override
    public String toString() {
        return String.format("\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\"",
                repo, file, getMethod(), getFullpath(), getDocumentation(), getNotes());
    }

    private String clean(String str) {
        return str.replaceAll("(?<!\\\\)((?:\\\\{2})*)\"\\s*\\+\\s*\"", "$1")
                .replaceAll("(?<!\\\\)((?:\\\\{2})*)\"\\s*\\+\\s*(?!\")", "$1 ")
                .replaceAll("(?<!\")\\s*\\+\\s*\"", " ")
                .replaceAll("^\"", "")
                .replaceAll("\"$", "")
                .replace("\"", "\"\"");
    }

    public boolean isEmpty() {
        return getMethod().equals("n/a")
                && getFullpath().equals("n/a")
                && getDocumentation().equals("n/a")
                && getNotes().equals("n/a");
    }
}
