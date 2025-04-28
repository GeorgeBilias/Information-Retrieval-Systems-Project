package src.main;

import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.store.FSDirectory;
import org.deeplearning4j.models.embeddings.loader.WordVectorSerializer;
import org.deeplearning4j.models.word2vec.Word2Vec;
import org.deeplearning4j.text.tokenization.tokenizerfactory.DefaultTokenizerFactory;
import org.nd4j.common.io.ClassPathResource;
import org.nd4j.linalg.api.ndarray.INDArray;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.*;

public class Phase3 {
    public static void main(String[] args) throws IOException {

        //read word2vec
        IndexReader reader = DirectoryReader.open(FSDirectory.open(Paths.get("Phase 1\\src\\main\\index")));
        FieldValuesSentenceIterator iter = new FieldValuesSentenceIterator(reader, "content");
        Word2Vec vec = new Word2Vec.Builder()
                .layerSize(100)
                .windowSize(5)
                .tokenizerFactory(new DefaultTokenizerFactory())
                .iterate(iter)
                .build();
        vec.fit();

        // Path to the file
        String filePath = "Phase 1\\src\\main\\queries.txt";

        // Initialize the map to store queries and vectors
        Map<String, List<String>> queriesMap = new HashMap<>();
        Map<String, INDArray> vectorsMap = new HashMap<>();

        // Call the method to read the queries and populate the map
        readQueriesFromFile(queriesMap);

        // Process each query to compute and store its average vector
        for (Map.Entry<String, List<String>> entry : queriesMap.entrySet()) {
            String queryId = entry.getKey();
            List<String> terms = entry.getValue();
            // Compute the average vector
            INDArray averageVector = toDenseAverageVector(vec, terms.toArray(new String[0]));
            vectorsMap.put(queryId, averageVector);
        }

        // Print the vectors map to verify the results
        for (Map.Entry<String, INDArray> entry : vectorsMap.entrySet()) {
            System.out.println("Query ID: " + entry.getKey());
            System.out.println("Average Vector: " + entry.getValue());
        }

        List<MyDoc> docs = new ArrayList<>();
        try {
            docs = TXTParsing.parse("Phase 1\\src\\main\\documents.txt"); //parse the documents
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        // Initialize the map to store document vectors
        Map<String, INDArray> docVectorsMap = new HashMap<>();

        // Process each document to compute and store its average vector
        for (MyDoc doc : docs) {
            String docId = doc.getID();
            String docContent = doc.getContent();

            // Compute the average vector for the document content
            INDArray averageVector = toDenseAverageVector(vec, docContent.split("\\s+"));
            docVectorsMap.put(docId, averageVector);
        }


        Map<String, Double> docSimilarities = new HashMap<>();
        List<Map.Entry<String, Double>> sortedDocs = new ArrayList<>(docSimilarities.entrySet());

        FileWriter writer = new FileWriter("Phase 1\\src\\main\\trec_eval\\results2.txt");



        for (Map.Entry<String, INDArray> queryEntry : vectorsMap.entrySet()) {
            String queryId = queryEntry.getKey();
            INDArray queryVector = queryEntry.getValue();
            docSimilarities = new HashMap<>();

            for (Map.Entry<String, INDArray> docEntry : docVectorsMap.entrySet()) {
                String docId = docEntry.getKey();
                INDArray docVector = docEntry.getValue();

                // Compute cosine similarity
                double similarity = cosineSimilarity(queryVector, docVector);
                docSimilarities.put(docId, similarity);
            }

            // Sort documents by similarity in descending order and collect the top k
            sortedDocs = new ArrayList<>(docSimilarities.entrySet());
            sortedDocs.sort(Map.Entry.<String, Double>comparingByValue().reversed());



            // Print the top k documents for this query
            int k = 50;
            int rank = 1;
            System.out.println("Query ID: " + queryId);
            for (int i = 0; i < Math.min(k, sortedDocs.size()); i++) {
                Map.Entry<String, Double> docEntry = sortedDocs.get(i);
                System.out.println("Document ID: " + docEntry.getKey() + ", Similarity: " + docEntry.getValue());
                String docId = docEntry.getKey();
                double score = docEntry.getValue();
                writer.write(String.format("%s\tQ0\t%s\t%d\t%f\tMySystem\n", queryId, docId, rank++, score));
            }





        }

        // continue from here!

    }

    // Compute cosine similarity between two vectors
    private static double cosineSimilarity(INDArray vec1, INDArray vec2) {
        //return Nd4j.getExecutioner().execAndReturn(new CosineSimilarity(vec1, vec2)).getDouble(0);
        return VectorizeUtils.cosineSimilarity(vec1.toDoubleVector(), vec2.toDoubleVector());
    }

    public static INDArray toDenseAverageVector(Word2Vec word2Vec, String... terms) {
        return word2Vec.getWordVectorsMean(Arrays.asList(terms));
    }

    private static void readQueriesFromFile(Map<String, List<String>> queriesMap) {
        try (BufferedReader reader = new BufferedReader(new FileReader("Phase 1\\src\\main\\queries.txt"))) {
            String line;
            String currentQueryId = null;
            List<String> currentQueryTerms = null;

            while ((line = reader.readLine()) != null) {
                String[] parts = line.split("///");
                if (parts.length > 0) {
                    String queryText = parts[0].trim();

                    if (queryText.startsWith("Q") && queryText.length() <= 3) {
                        // Save the previous query if it exists
                        if (currentQueryId != null && currentQueryTerms != null) {
                            queriesMap.put(currentQueryId, currentQueryTerms);
                        }

                        // Start a new query
                        currentQueryId = queryText;
                        currentQueryTerms = new ArrayList<>();
                    } else if (!queryText.isEmpty() && currentQueryId != null) {
                        // Add terms to the current query
                        currentQueryTerms.addAll(Arrays.asList(queryText.split("\\s+")));
                    }
                }
            }

            // Save the last query if it exists
            if (currentQueryId != null && currentQueryTerms != null) {
                queriesMap.put(currentQueryId, currentQueryTerms);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

