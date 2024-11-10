package uni.roma3.homework2.model;

public class SearchResult {
    private String filename;
    private float score;
    private String title;
    private String authors;
    private String abstractText;
    private String contentSnippet;

    // Constructor
    public SearchResult(String filename, float score, String title, String authors, String abstractText, String contentSnippet) {
        this.filename = filename;
        this.score = score;
        this.title = title;
        this.authors = authors;
        this.abstractText = abstractText;
        this.contentSnippet = contentSnippet;
    }

    // Getters and Setters
    public String getFilename() { return filename; }
    public void setFilename(String filename) { this.filename = filename; }
    public float getScore() { return score; }
    public void setScore(float score) { this.score = score; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getAuthors() { return authors; }
    public void setAuthors(String authors) { this.authors = authors; }
    public String getAbstractText() { return abstractText; }
    public void setAbstractText(String abstractText) { this.abstractText = abstractText; }
    public String getContentSnippet() { return contentSnippet; }
    public void setContentSnippet(String contentSnippet) { this.contentSnippet = contentSnippet; }

    @Override
    public String toString() {
        return "SearchResult{" +
                "title='" + title + '\'' +
                ", abstract='" + abstractText + '\'' +
                ", author='" + authors + '\'' +
                '}';
    }
}