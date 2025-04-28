package src.main;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.StoredField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.document.Field;
//import org.apache.lucene.analysis.core.SimpleAnalyzer;
//import org.apache.lucene.analysis.core.WhitespaceAnalyzer;
import org.apache.lucene.analysis.en.EnglishAnalyzer;
//import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.search.similarities.BM25Similarity;
import org.apache.lucene.search.similarities.ClassicSimilarity;
import org.apache.lucene.search.similarities.LMJelinekMercerSimilarity;
import org.apache.lucene.search.similarities.Similarity;
import org.apache.lucene.index.IndexWriterConfig;
import org.deeplearning4j.models.embeddings.learning.impl.elements.CBOW;
import org.deeplearning4j.models.word2vec.Word2Vec;
import org.deeplearning4j.text.sentenceiterator.BasicLineIterator;
import org.deeplearning4j.text.sentenceiterator.SentenceIterator;
import org.nd4j.common.io.ClassPathResource;
import org.nd4j.linalg.api.ndarray.INDArray;


public class FirstPhase {
    public static void main(String[] args) {
        try {
            List<MyDoc> docs = TXTParsing.parse("Phase 1\\src\\main\\documents.txt"); //parse the documents



            String indexLocation = ("Phase 1\\src\\main\\index"); //define were to store the index

            Directory dir = FSDirectory.open(Paths.get(indexLocation)); //open the directory
            Analyzer analyzer = new EnglishAnalyzer(); //define Analyzer

            // define which similarity to use
            BM25Similarity similarity = new BM25Similarity(3, 1);
            //LMJelinekMercerSimilarity similarity = new LMJelinekMercerSimilarity(0.9f);



            // configure IndexWriter
            IndexWriterConfig iwc = new IndexWriterConfig(analyzer);
            iwc.setSimilarity(similarity); // set the similarity

            // Create a new index in the directory, removing any
            // previously indexed documents:
            iwc.setOpenMode(OpenMode.CREATE);

            // create the IndexWriter with the configuration as above
            IndexWriter indexWriter = new IndexWriter(dir, iwc);


            for (MyDoc doc : docs){
                indexDoc(indexWriter, doc); //index the documents
            }

            indexWriter.close(); //close the index

        } catch (IOException e) {
            e.printStackTrace(); //catch the error
        } catch (Exception e) {
            e.printStackTrace(); //catch the error
        }
    }

    private static void indexDoc(IndexWriter indexWriter, MyDoc mydoc){

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
                // System.out.println("adding " + mydoc);
                indexWriter.addDocument(doc);
            }
        } catch(Exception e){
            e.printStackTrace();
        }

    }

    public static INDArray toDenseAverageVector(Word2Vec word2Vec, String...
            terms) {
        return word2Vec.getWordVectorsMean(Arrays.asList(terms));
    }
}