package src.main;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.apache.lucene.document.*;
import org.apache.lucene.index.IndexOptions;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.analysis.en.EnglishAnalyzer;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.search.similarities.BM25Similarity;
import org.apache.lucene.search.similarities.ClassicSimilarity;
import org.apache.lucene.search.similarities.LMJelinekMercerSimilarity;
import org.apache.lucene.search.similarities.MultiSimilarity;
import org.apache.lucene.search.similarities.Similarity;
import org.deeplearning4j.models.word2vec.Word2Vec;
import org.nd4j.linalg.api.ndarray.INDArray;

public class Phase4 {
    public static void main(String[] args) {
        try {
            List<MyDoc> docs = TXTParsing.parse("Phase 1\\src\\main\\documents.txt"); // parse the documents

            String indexLocation = ("Phase 1\\src\\main\\index"); // define where to store the index

            Directory dir = FSDirectory.open(Paths.get(indexLocation)); // open the directory
            Analyzer analyzer = new EnglishAnalyzer(); // define Analyzer


            MultiSimilarity similarity = new MultiSimilarity(new Similarity[]{
                    new BM25Similarity(3, 1),
                    new LMJelinekMercerSimilarity(0.9F),
            });


                // Configure IndexWriter
                IndexWriterConfig iwc = new IndexWriterConfig(analyzer);
                iwc.setSimilarity(similarity); // set the similarity
                iwc.setOpenMode(OpenMode.CREATE); // Create a new index in the directory, removing any previously indexed documents

                // Create the IndexWriter with the configuration as above
                IndexWriter indexWriter = new IndexWriter(dir, iwc);
                FieldType ft = new FieldType(TextField.TYPE_STORED);
                ft.setIndexOptions(IndexOptions.DOCS_AND_FREQS_AND_POSITIONS_AND_OFFSETS);
                ft.setTokenized(true);
                ft.setStored(true);
                ft.setStoreTermVectors(true);
                ft.setStoreTermVectorOffsets(true);
                ft.setStoreTermVectorPositions(true);

                for (MyDoc doc : docs) {
                    indexDoc(indexWriter, doc); // index the documents
                }

                indexWriter.close(); // close the index

        } catch (IOException e) {
            e.printStackTrace(); // catch the error
        } catch (Exception e) {
            e.printStackTrace(); // catch the error
        }
    }

    private static void indexDoc(IndexWriter indexWriter, MyDoc mydoc) {
        try {
            // make a new, empty document
            Document doc = new Document();

            // create the fields of the document and add them to the document
            StoredField id = new StoredField("id", mydoc.getID());
            doc.add(id);
            StoredField title = new StoredField("title", mydoc.getTitle());
            doc.add(title);
            StoredField content = new StoredField("content", mydoc.getContent());
            doc.add(content);
            String fullSearchableText = mydoc.getID() + " " + mydoc.getTitle() + " " + mydoc.getContent();
            TextField contents = new TextField("contents", fullSearchableText, Field.Store.NO);
            doc.add(contents);

            if (indexWriter.getConfig().getOpenMode() == OpenMode.CREATE) {
                // New index, so we just add the document (no old document can be there):
                indexWriter.addDocument(doc);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static INDArray toDenseAverageVector(Word2Vec word2Vec, String... terms) {
        return word2Vec.getWordVectorsMean(Arrays.asList(terms));
    }
}

