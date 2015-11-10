package de.mpg.imeji.logic.search.elasticsearch;

import java.io.IOException;
import java.net.URISyntaxException;

import org.apache.log4j.Logger;
import org.elasticsearch.action.admin.indices.create.CreateIndexRequest;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.Requests;
import org.elasticsearch.cluster.metadata.AliasMetaData;
import org.elasticsearch.common.collect.ImmutableOpenMap;
import org.elasticsearch.node.Node;
import org.elasticsearch.node.NodeBuilder;

import de.mpg.imeji.presentation.util.PropertyReader;

/**
 * elasticsearch service for spot
 * 
 * @author bastiens
 * 
 */
public class ElasticService {
  private static Node node;
  public static Client client;
  private static String CLUSTER_NAME = "name of my cluster";
  private static final Logger logger = Logger.getLogger(ElasticService.class);

  /**
   * The Index in Elasticsearch
   * 
   * @author bastiens
   * 
   */
  public enum ElasticIndex {
    data;
  }

  public static String INDEX;

  /**
   * The Index where all data are indexed
   */
  public static String DATA_ALIAS = "data";

  /**
   * The Types in Elasticsearch
   * 
   * @author bastiens
   * 
   */
  public enum ElasticTypes {
    items, folders, albums, spaces;
  }

  public static void start() throws IOException, URISyntaxException {
    CLUSTER_NAME = PropertyReader.getProperty("elastic.cluster.name");
    node = NodeBuilder.nodeBuilder().clusterName(CLUSTER_NAME).node();
    client = node.client();
    INDEX = initializeIndex();
    new ElasticIndexer(INDEX, ElasticTypes.items).addMapping();
    new ElasticIndexer(INDEX, ElasticTypes.folders).addMapping();
    new ElasticIndexer(INDEX, ElasticTypes.albums).addMapping();
    new ElasticIndexer(INDEX, ElasticTypes.spaces).addMapping();
  }

  /**
   * Initialize the index. If index exists, don't create one, if not create one. Index is created
   * with DATA_ALIAS as alias
   * 
   * @return
   */
  private static String initializeIndex() {
    String indexName = getIndexNameFromAliasName(DATA_ALIAS);
    if (indexName != null) {
      return indexName;
    } else {
      return createIndexWithAlias();
    }
  }

  /**
   * Get the First Index pointed by the Alias. This method should be used, when there is only one
   * Index pointed by the alias (on startup for instance)
   * 
   * @param aliasName
   * @return
   */
  public static String getIndexNameFromAliasName(final String aliasName) {
    ImmutableOpenMap<String, AliasMetaData> indexToAliasesMap =
        client.admin().cluster().state(Requests.clusterStateRequest()).actionGet().getState()
            .getMetaData().aliases().get(aliasName);
    if (indexToAliasesMap != null && !indexToAliasesMap.isEmpty()) {
      if (indexToAliasesMap.size() > 1) {
        logger.error("Alias " + aliasName
            + " has more than one index. This is forbidden: All indexes will be removed, please reindex!!!");
        reset();
        return null;
      }
      return indexToAliasesMap.keys().iterator().next().value;
    }
    return null;
  }

  /**
   * Create an Index point an alias to it
   * 
   * @return
   */
  public static String createIndexWithAlias() {
    try {
      String indexName = createIndex();
      ElasticService.client.admin().indices().prepareAliases().addAlias(indexName, DATA_ALIAS)
          .execute().actionGet();
      return indexName;
    } catch (Exception e) {
      logger.info("Index +" + "+ already existing");
    }
    return null;
  }

  /**
   * Create a new Index (without alias)
   * 
   * @return
   */
  public static String createIndex() {
    try {
      String indexName = DATA_ALIAS + "-" + System.currentTimeMillis();
      ElasticService.client.admin().indices().create(new CreateIndexRequest(indexName)).actionGet();
      return indexName;
    } catch (Exception e) {
      logger.info("Index +" + "+ already existing");
    }
    return null;
  }

  /**
   * Atomically move the alias from the old to the new index
   * 
   * @param oldIndex
   * @param newIndex
   */
  public static void setNewIndexAndRemoveOldIndex(String newIndex) {
    String oldIndex = getIndexNameFromAliasName(DATA_ALIAS);
    if (oldIndex != null) {
      ElasticService.client.admin().indices().prepareAliases().addAlias(newIndex, DATA_ALIAS)
          .removeAlias(oldIndex, DATA_ALIAS).execute().actionGet();
      ElasticService.client.admin().indices().prepareDelete(oldIndex).execute().actionGet();
    } else {
      ElasticService.client.admin().indices().prepareAliases().addAlias(newIndex, DATA_ALIAS)
          .execute().actionGet();
    }
    INDEX = newIndex;
  }

  /**
   * DANGER: delete all data from elasticsearch. A new reindex will be necessary
   */
  public static void reset() {
    ElasticService.client.admin().indices().delete(new DeleteIndexRequest("_all")).actionGet();
    INDEX = initializeIndex();
  }

  public static void shutdown() {
    node.close();
  }
}
