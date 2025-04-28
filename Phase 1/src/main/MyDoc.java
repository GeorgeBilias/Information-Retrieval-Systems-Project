package src.main;

/**
 *
 * @author Georgios Bilias - Ioannis Patouchas
 */
public class MyDoc {

    private String id;
    private String title;
    private String content;

    //---- Constructors definition ----
    public MyDoc(String id, String title, String content) {
        this.id = id;
        this.title = title;
        this.content = content;
    }
    // To String method
    @Override
    public String toString() {
        String ret = "MyDoc{"
                + "\n\tID: " + id
                + "\n\tTitle: " + title
                + "\n\tContent: " + content;                
        return ret + "\n}";
    }

    //---- Getters & Setters definition ----
    public String getID() {
        return this.id;
    }

    public void setID(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
