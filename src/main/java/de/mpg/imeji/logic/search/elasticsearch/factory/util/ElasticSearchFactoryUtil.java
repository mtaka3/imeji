package de.mpg.imeji.logic.search.elasticsearch.factory.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.lucene.queryparser.classic.QueryParserBase;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.ExistsQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.sort.SortBuilder;

import de.mpg.imeji.logic.model.Grant;
import de.mpg.imeji.logic.model.User;
import de.mpg.imeji.logic.model.UserGroup;
import de.mpg.imeji.logic.search.elasticsearch.ElasticService;
import de.mpg.imeji.logic.search.elasticsearch.factory.ElasticSortFactory;
import de.mpg.imeji.logic.search.elasticsearch.model.ElasticFields;
import de.mpg.imeji.logic.search.model.SortCriterion;
import de.mpg.imeji.logic.security.usergroup.UserGroupService;

/**
 * Utility Class for ElasticSearch
 *
 * @author bastiens
 *
 */
public class ElasticSearchFactoryUtil {

  /**
   * Read the field of an object in Elasticsearch. The value is returned as String
   *
   * @param id
   * @param field
   * @param dataType
   * @param index
   * @return
   */
  public static String readFieldAsString(String id, ElasticFields field, String dataType,
      String index) {
    final Map<String, Object> sourceMap = ElasticService.getClient().prepareGet(index, dataType, id)
        .setFetchSource(true).execute().actionGet().getSource();
    if (sourceMap != null) {
      final Object obj = sourceMap.get(field.field());
      return obj != null ? obj.toString() : "";
    }
    return "";
  }

  
  /**
   * Function searches ElasticSearch database
   * Retrieves all documents (of a given type) that have no or an empty field  
   * 
   * @param dataType  type of documents that will be searched
   * @param nameOfMissingField  name of the field
   * @return list of UIDs of documents that miss the field 
   */
  public static List<String> getDocumentsThatMissAField(String dataType, String nameOfMissingField){
	  
	  List<String> resultUIDs = new ArrayList<String>();
	  
	  int numberOfBatchResultsInScrolling = 1000;
	  int timeoutValueInScrolling = 60000;
	  
	  Client elasticSearchClient = ElasticService.getClient();

	  
	  // (1) construction of Query to ElasticSearch

	  SearchRequestBuilder elasticSearchRequestBuilder = elasticSearchClient.prepareSearch(ElasticService.DATA_ALIAS);
	  
	  // set the scan&scroll search type parameters: size and timeout
	  elasticSearchRequestBuilder = elasticSearchRequestBuilder.setSize(numberOfBatchResultsInScrolling);
	  elasticSearchRequestBuilder.setScroll(new TimeValue(timeoutValueInScrolling));
	  
	  // set the document type	  
	  elasticSearchRequestBuilder = elasticSearchRequestBuilder.setTypes(dataType);
	  
	  // set the query
	  final ExistsQueryBuilder eqb = new ExistsQueryBuilder(nameOfMissingField);
	  BoolQueryBuilder bqb = new BoolQueryBuilder(); 
	  bqb = bqb.mustNot(eqb);
	  elasticSearchRequestBuilder =  elasticSearchRequestBuilder.setQuery(bqb);
	  
	  // (2) Send query to ElasticSearch:
	  SearchResponse scrollResponse = elasticSearchRequestBuilder.get();
	  
	  //Scroll until no hits are returned
	  do {
	      for (SearchHit hit : scrollResponse.getHits().getHits()) {
	    	// get UIDs from retrieved documents
	    	resultUIDs.add(hit.getId());
	      }

	     scrollResponse = elasticSearchClient.prepareSearchScroll(scrollResponse.getScrollId()).setScroll(new TimeValue(timeoutValueInScrolling)).execute().actionGet();
	  } 
	  while(scrollResponse.getHits().getHits().length != 0); // Zero hits mark the end of the scroll and the while loop.
    
      return resultUIDs;
	    	  
  }
  

  
  /**
   * Add single or  multilevel sorting to an ElasticSearch {@link SearchRequestBuilder}
   * 
   * Add a list of Imeji {@link SortCriterion}.
   * These are used to sort results as follows:
   * 
   *  Results are first sorted by the first sort criterion in the list
   *  Elements that fall into the same category are then sorted by the second criterion of the list
   *  and so on
   *  
   * @param searchRequestBuilder
   * @param sortCriteria
   * @return SearchRequestBuilder with single or multilevel sorting
   */
  public static SearchRequestBuilder addSorting(SearchRequestBuilder searchRequestBuilder, List<SortCriterion> sortCriteria) {
	  
	  List<SortBuilder> sortBuilders = ElasticSortFactory.build(sortCriteria);
	  	  
	  for(SortBuilder sortBuilder : sortBuilders) {
		  searchRequestBuilder.addSort(sortBuilder);
	  }
	  
	  return searchRequestBuilder;	  
  }
  
  
  
  /**
   * Retrieve the Id of the user according to its email
   *
   * @param email
   * @return
   */
  public static String getUserId(String email) {
    final List<String> r = searchStringAndRetrieveFieldValue("email:\"" + email.toString() + "\"",
        ElasticFields.ID.field().toLowerCase(), null, 0, 1);
    if (r.size() > 0) {
      return r.get(0);
    }
    return null;
  }

  /**
   * Retrieve all user groups of one user
   *
   * @param userId
   * @return
   */
  public static List<String> getGroupsOfUser(String userId) {
    return searchStringAndRetrieveFieldValue("users:\"" + userId + "\"",
        ElasticFields.ID.field().toLowerCase(), null, 0, -1);
  }

  /**
   * Get all Grants of the users, including the grants of its groups
   * 
   * @param user
   * @return
   */
  public static List<Grant> getAllGrants(User user) {
    List<Grant> grants =
        user.getGrants().stream().map(s -> new Grant(s)).collect(Collectors.toList());
    grants.addAll(getGroupGrants(user));
    return grants;
  }

  /**
   * Get the all the grants of the groups of the users
   * 
   * @param user
   * @return
   */
  public static List<Grant> getGroupGrants(User user) {
    UserGroupService ugs = new UserGroupService();
    List<UserGroup> groups = ugs.retrieveBatch(getGroupsOfUser(user.getId().toString()), user);
    return groups.stream().flatMap(group -> group.getGrants().stream()).map(s -> new Grant(s))
        .collect(Collectors.toList());
  }

  /**
   * Search for a String query and retrieve only the value of a specific field
   *
   * @param query
   * @param field
   * @param sort
   * @param user
   * @param from
   * @param size
   * @return
   */
  public static List<String> searchStringAndRetrieveFieldValue(String query, String field,
      SortCriterion sort, int from, int size) {
    final QueryBuilder q = QueryBuilders.queryStringQuery(query);
    final SearchResponse resp = ElasticService.getClient().prepareSearch(ElasticService.DATA_ALIAS)
        .addField(field).setQuery(q).addSort(ElasticSortFactory.build(sort)).setSize(size)
        .setFrom(from).execute().actionGet();
    final List<String> fieldValues = new ArrayList<>();
    for (final SearchHit hit : resp.getHits()) {
      if (field.equals(ElasticFields.ID.field())) {
        fieldValues.add(hit.getId());
      } else {
        fieldValues.add(hit.field(field).getValue());
      }
    }
    return fieldValues;
  }

  /**
   * Escape input to avoid error in Elasticsearch. * and ? are unescaped, to allow wildcard search
   *
   * @param s
   * @return
   */
  public static String escape(String s) {
    return QueryParserBase.escape(s).replace("\\*", "*").replace("\\?", "?").replace("\\\"", "\"");
  }
}
