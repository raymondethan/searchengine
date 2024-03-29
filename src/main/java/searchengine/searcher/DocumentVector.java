package searchengine.searcher;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

/**
 *
 */
public class DocumentVector {
    private List<Double> tfIdfs;

    public DocumentVector(int length) {
        List<Double> tfIdfs = new ArrayList<>();
        for (int i = 0; i < length; ++i)
            tfIdfs.add(0.0);

        this.tfIdfs = tfIdfs;
    }

    public DocumentVector(List<Double> tfIdfs) {
        this.tfIdfs = tfIdfs;
    }

    public DocumentVector() {
        this.tfIdfs = new ArrayList<>();
    }

    public static boolean sameLength(DocumentVector a, DocumentVector b) {
        return a.tfIdfs.size() == b.tfIdfs.size();
    }

    public static DocumentVector div(DocumentVector a, DocumentVector b) {
        DocumentVector result = new DocumentVector();
        for (int i = 0; i < a.tfIdfs.size(); ++i)
            result.tfIdfs.add(a.tfIdfs.get(i) / b.tfIdfs.get(i));
        return result;
    }

    public static DocumentVector div(DocumentVector a, double b) {
        DocumentVector result = new DocumentVector();
        for (int i = 0; i < a.tfIdfs.size(); ++i)
            result.tfIdfs.add(a.tfIdfs.get(i) / b);
        return result;
    }

    public static DocumentVector mul(DocumentVector a, DocumentVector b) {
        DocumentVector result = new DocumentVector();
        for (int i = 0; i < a.tfIdfs.size(); ++i)
            result.tfIdfs.add(a.tfIdfs.get(i) * b.tfIdfs.get(i));
        return result;
    }

    public static DocumentVector add(DocumentVector a, DocumentVector b) {
        DocumentVector result = new DocumentVector();
        for (int i = 0; i < a.tfIdfs.size(); ++i)
            result.tfIdfs.add(a.tfIdfs.get(i) + b.tfIdfs.get(i));
        return result;
    }

    public static DocumentVector sub(DocumentVector a, DocumentVector b) {
        DocumentVector result = new DocumentVector();
        for (int i = 0; i < a.tfIdfs.size(); ++i)
            result.tfIdfs.add(a.tfIdfs.get(i) - b.tfIdfs.get(i));
        return result;
    }

    public static double dot(DocumentVector a, DocumentVector b) {
        double sum = 0;
        for (int i = 0; i < a.tfIdfs.size(); ++i)
            sum += a.tfIdfs.get(i) * b.tfIdfs.get(i);
        return sum;
    }

    public static double distance(DocumentVector a, DocumentVector b) {
        return a.sub(b).length();
    }

    public static double cosineDistance(DocumentVector a, DocumentVector b) {
        return dot(a, b)/(a.length()*b.length());
    }

    public double cosineDistance(DocumentVector to) {
        return cosineDistance(this, to);
    }

    public double dot(DocumentVector with) {
        return dot(this, with);
    }

    public double get(int dimension) {
        return tfIdfs.get(dimension);
    }

    public Stream<Double> vectorStream() {
        return tfIdfs.stream();
    }

    public int dimensions() {
        return tfIdfs.size();
    }

    public List<Double> getTfIdfs() {
        return tfIdfs;
    }

    public double length() {
        return Math.sqrt(lengthSquared());
    }

    public double lengthSquared() {
        return dot(this, this);
    }

    public DocumentVector div(DocumentVector a) {
        return div(this, a);
    }
    public DocumentVector div(double b) {return div(this, b);}

    public DocumentVector mul(DocumentVector a) {
        return mul(this, a);
    }

    public DocumentVector sub(DocumentVector a) {
        return sub(this, a);
    }
    public DocumentVector normalize(DocumentVector a) {
        return a.div(a.length());
    }

    public DocumentVector add(DocumentVector a) {
        return add(this, a);
    }
}
