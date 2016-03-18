package searchengine.indexer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import jdbm.RecordManager;
import jdbm.htree.HTree;

/**
 *
 */
public class LinkIndex extends BasicIndex<String> {
    public static final String LINK_INDEX_NAME = "linkindex";

    public static final String PARENT_LINK_INDEX_NAME = "parentlinks";
    public static final String CHILD_LINK_INDEX_NAME = "childlinks";

    private HTree childHashTree;
    private HTree parentHashTree;

    public LinkIndex(RecordManager recman) throws IOException {
        super(LINK_INDEX_NAME, recman);

        long recid = recman.getNamedObject(CHILD_LINK_INDEX_NAME);
        if (recid != 0)
            childHashTree = HTree.load(recman, recid);
        else
        {
            childHashTree = HTree.createInstance(recman);
            recman.setNamedObject(CHILD_LINK_INDEX_NAME, childHashTree.getRecid() );
        }

        recid = recman.getNamedObject(PARENT_LINK_INDEX_NAME);
        if (recid != 0)
            parentHashTree = HTree.load(recman, recid);
        else
        {
            parentHashTree = HTree.createInstance(recman);
            recman.setNamedObject(PARENT_LINK_INDEX_NAME, parentHashTree.getRecid() );
        }
    }

    /**
     * Adds a parent for a link
     * @param link The link to add the parent for
     * @param parent The parent link
     */
    public void addParent(String link, String parent) throws IOException {
        int linkId = getId(link);

        List<String> parents = (List<String>) parentHashTree.get(linkId);
        if (parents == null)
            parents = new ArrayList<>();

        //We don't store the parent links as a string because we don't gain
        //any advantage searching by doing so and it would add overhead to getting a list
        //of parent links. Maybe in future if we don't want duplicates or something
        parents.add(parent);
        parentHashTree.put(linkId, parents);
    }

    /**
     * Gets a list of parents for a link
     * @param link The link to get parents for
     * @return The parents of the link
     */
    public List<String> getParents(String link) throws IOException {
        int linkId = getId(link);
        List<String> result = (List<String>) parentHashTree.get(linkId);
        return result == null ? new ArrayList<>() : result;
    }

    /**
     * Adds a collection of children to a link
     * @param link The link to add the children to
     * @param children The children
     */
    public void addChildren(String link, List<String> children) throws IOException {
        int linkId = getId(link);

        List<String> currentChildren = (List<String>) childHashTree.get(linkId);
        if (currentChildren == null)
            currentChildren = new ArrayList<>();

        children.addAll(children);
        childHashTree.put(linkId, children);
    }

    /**
     * Adds a child link to a link
     * @param link The link to add the child to
     * @param child The child link
     */
    public void addChild(String link, String child) throws IOException {
        List<String> children = new ArrayList<>();
        children.add(child);

        addChildren(link, children);
    }

    /**
     * Gets a list of children for a link
     * @param link The link to get children for
     * @return The children of the link
     */
    public List<String> getChildren(String link) throws IOException {
        int linkId = getId(link);
        List<String> result = (List<String>) childHashTree.get(linkId);
        return result == null ? new ArrayList<>() : result;
    }
}