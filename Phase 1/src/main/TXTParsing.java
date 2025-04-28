package src.main;

import src.utils.IO;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author  Tonia Kyriakopoulou
 */
public class TXTParsing {

    public static List<MyDoc> parse(String file) throws Exception {
        try {
            // Parse txt file
            String txt_file = IO.ReadEntireFileIntoAString(file); // Read the entire file into a string
            String[] docs = txt_file.split("///\\s*\\n+"); // Split the string into documents

            System.out.println("Read: " + docs.length + " docs"); // Print the number of documents
    
            // Parse each document from the txt file

            List<MyDoc> parsed_docs = new ArrayList<MyDoc>(); // Create a list to store the parsed documents

            for (String doc : docs) { // For each document
                String[] parts = doc.split("\n", 2); // Split document into ID, title, and content

                    // Get the ID, title, and content

                    String id = parts[0].trim(); // Get the ID
                    String parts2[] = parts[1].split(":", 2); // Split the rest of the document into title and content
                    String title = parts2[0].trim(); // Get the title
                    String content = parts2[1].trim(); // Get the content

                    parsed_docs.add(new MyDoc(id, title, content)); // Add the parsed document to the list

                    // Print the parsed document
                    //System.out.println("ID:"+id+"\n");
                    //System.out.println("Title:"+title+"\n");
                    //System.out.println("Content:"+content+"\n");
            }
    
            System.out.println(parsed_docs.size() + " docs parsed"); // Print the number of parsed documents

            return parsed_docs; // Return the parsed documents

        } catch (Throwable err) {
            err.printStackTrace(); // Print the error
            return null; // Return null
        }
    }

}
