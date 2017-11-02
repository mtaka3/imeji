package de.mpg.imeji.logic.hierarchy;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import de.mpg.imeji.logic.hierarchy.Hierarchy.Node;
import de.mpg.imeji.logic.model.CollectionImeji;
import de.mpg.imeji.logic.model.Item;

/**
 * Service to manage the hierarchy between imeji objects
 * 
 * @author saquet
 *
 */
public class HierarchyService implements Serializable {
  private static final long serialVersionUID = -3479895793901732353L;

  private static Hierarchy hierarchy = new Hierarchy();

  public HierarchyService() {
    if (hierarchy == null) {
      hierarchy = new Hierarchy();
      hierarchy.init();
    }
  }

  /**
   * Reload the hierarchy
   */
  public static void reloadHierarchy() {
    hierarchy.init();
  }

  /**
   * Find all Subcollection of the collection/subcollection
   * 
   * @param collectionUri
   * @return
   */
  public List<String> findAllSubcollections(String collectionUri) {
    return hierarchy.getTree().get(collectionUri) != null ? hierarchy.getTree().get(collectionUri)
        .stream().flatMap(s -> Stream.concat(Stream.of(s), findAllSubcollections(s).stream()))
        .collect(Collectors.toList()) : new ArrayList<>();
  }

  /**
   * Find the list of all parents of the objects
   * 
   * @param o
   * @return
   */
  public List<String> findAllParents(Object o) {
    List<String> l = new ArrayList<>();
    String uri = getParentUri(o);
    if (uri != null) {
      l.addAll(findAllParents(uri));
      l.add(uri);
      return l;
    }
    return l;
  }

  /**
   * Return the list of all parents of the object with this uri
   * 
   * @param parentUri
   * @return
   */
  public List<String> findAllParents(String uri) {
    List<String> l = new ArrayList<>();
    String parent = getParent(uri);
    if (parent != null) {
      l.addAll(findAllParents(parent));
      l.add(parent);
    }
    return l;
  }

  /**
   * Return the Parent uri of the passed uri
   * 
   * @param uri
   * @return
   */
  public String getParent(String uri) {
    Node n = hierarchy.getNodes().get(uri);
    return n != null ? n.getParent() : null;
  }


  /**
   * Return the last parent of the object
   * 
   * @param o
   * @return
   */
  public String getLastParent(Object o) {
    String uri = getParentUri(o);
    if (uri != null) {
      return getLastParent(uri);
    }
    return null;
  }

  /**
   * Get the last parent of the object according to its first parent
   * 
   * @param firstParent
   * @return
   */
  public String getLastParent(String firstParent) {
    Node parentNode = hierarchy.getNodes().get(firstParent);
    if (parentNode != null) {
      return getLastParent(parentNode.getParent());
    }
    return firstParent;
  }



  /**
   * If the object is an Item or a collection and has a parent, return the uri of its parent
   * 
   * @param o
   * @return
   */
  private String getParentUri(Object o) {
    if (o instanceof Item) {
      return ((Item) o).getCollection().toString();
    }
    if (o instanceof CollectionImeji) {
      return ((CollectionImeji) o).getCollection() != null
          ? ((CollectionImeji) o).getCollection().toString() : null;
    }
    return null;
  }
}