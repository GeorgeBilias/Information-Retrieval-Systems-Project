package src.main;

import java.io.BufferedReader;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collection;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.en.EnglishAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.similarities.BM25Similarity;
import org.apache.lucene.search.similarities.ClassicSimilarity;
import org.apache.lucene.search.similarities.LMJelinekMercerSimilarity;
import org.apache.lucene.search.similarities.MultiSimilarity;
import org.apache.lucene.search.similarities.Similarity;
import org.apache.lucene.store.FSDirectory;

import java.io.FileReader;
import java.io.FileWriter;

public class Searcher4 {

    public Searcher4() {
        try {
            String indexLocation = ("Phase 1\\src\\main\\index");
            String field = "contents";


            MultiSimilarity similarity = new MultiSimilarity(new Similarity[]{
                    new BM25Similarity(3, 1),
                    new LMJelinekMercerSimilarity(0.9F),
            });

            // Create a FileWriter to write results to a file
            FileWriter writer = new FileWriter("Phase 1\\src\\main\\trec_eval\\results4.txt");

            // Read queries from the file and write results to the file for each similarity

                IndexReader indexReader = DirectoryReader.open(FSDirectory.open(Paths.get(indexLocation)));
                IndexSearcher indexSearcher = new IndexSearcher(indexReader);
                indexSearcher.setSimilarity(similarity);

                // Process queries and write results
                readQueriesFromFile(indexSearcher, field, writer);

                indexReader.close();

            // Close the FileWriter
            writer.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void readQueriesFromFile(IndexSearcher indexSearcher, String field, FileWriter writer) {
        try {
            BufferedReader reader = new BufferedReader(new FileReader("Phase 1\\src\\main\\queries.txt"));
            String line;
            int queryNumber = 1;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split("///");
                if (parts.length > 0) {
                    String queryText = parts[0].trim();
                    if (queryText.startsWith("Q") && queryText.length() <= 3) {
                        System.out.println("Found Q: " + queryText);
                        continue; // Skip lines containing only query identifier
                    }
                    if (!queryText.isEmpty()) {
                        // Process the query
                        System.out.println("Processing query " + queryNumber + ": " + queryText);

                        Analyzer analyzer = new EnglishAnalyzer();
                        QueryParser parser = new QueryParser(field, analyzer);
                        System.out.println("Parsing query: " + queryText);
                        Query query = parser.parse(queryText);
                        TopDocs results = indexSearcher.search(query, 50);
                        ScoreDoc[] hits = results.scoreDocs;
                        long numTotalHits = results.totalHits;

                        if (numTotalHits == 0) {
                            System.out.println("No results found for query " + queryNumber + ": " + queryText);
                        } else {
                            for (int i = 0; i < hits.length; i++) {
                                Document hitDoc = indexSearcher.doc(hits[i].doc);
                                writer.write("Q" + String.format("%02d", queryNumber) + "\t" + "0\t" + hitDoc.get("id") + "\t0\t" + hits[i].score + "\t" + "myIRmethod" + "\n");
                            }
                        }

                        queryNumber++;
                    } else {
                        System.out.println("Skipping empty line or line with only delimiter at query " + queryNumber);
                    }
                }
            }
            reader.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        Searcher4 searcher = new Searcher4();
    }
}
