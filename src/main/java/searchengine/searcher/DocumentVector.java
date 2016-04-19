package searchengine.searcher;

import java.util.ArrayList;
import java.util.List;

/**
 *
 */
public class DocumentVector {
    public List<Double> tfIdfs = new ArrayList<>();

    public static DocumentVector div(DocumentVector a, DocumentVector b) {
        DocumentVector result = new DocumentVector();
        for (int i = 0; i < a.tfIdfs.size(); ++i)
            result.tfIdfs.add(a.tfIdfs.get(i) * b.tfIdfs.get(i));
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

    public double length() {
        return Math.sqrt(lengthSquared());
    }

    public double lengthSquared() {
        return dot(this, this);
    }

    public DocumentVector div(DocumentVector a) {
        return div(this, a);
    }

    public DocumentVector mul(DocumentVector a) {
        return mul(this, a);
    }

    public DocumentVector sub(DocumentVector a) {
        return sub(this, a);
    }

    public DocumentVector add(DocumentVector a) {
        return add(this, a);
    }
}
