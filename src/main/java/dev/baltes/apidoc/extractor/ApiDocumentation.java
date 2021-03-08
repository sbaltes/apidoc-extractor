package dev.baltes.apidoc.extractor;

class ApiDocumentation {
    String method;
    String path;
    String documentation;
    String notes;

    public void setMethod(String method) {
        this.method = method;
    }

    public void setPath(String path) {
        this.path = path;
    }

    void setDocumentation(String documentation) {
        if (this.documentation == null) {
            this.documentation = documentation;
        } else {
            this.documentation += " + " + documentation;
        }
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public String getMethod() {
        return method == null ? "n/a" : method;
    }

    public String getPath() {
        return path == null ? "n/a" : clean(path);
    }

    public String getDocumentation() {
        return documentation == null ? "n/a" : clean(documentation);

    }

    public String getNotes() {
        return notes == null ? "n/a" : clean(notes);
    }

    @Override
    public String toString() {
        return String.format("\"%s\",\"%s\",\"%s\",\"%s\"", getMethod(), getPath(), getDocumentation(), getNotes());
    }

    private String clean(String str) {
        return str.replaceAll("\"\\s*\\+\\s*\"", " ")
                .replaceAll("^\"", "")
                .replaceAll("\"$", "");
    }

    public boolean isEmpty() {
        return method == null && path == null && (documentation == null || notes == null);
    }
}
