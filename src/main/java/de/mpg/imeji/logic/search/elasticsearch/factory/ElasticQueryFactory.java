package de.mpg.imeji.logic.search.elasticsearch.factory;


import java.util.Collection;
import java.util.List;

import org.apache.commons.lang.math.NumberUtils;
import org.apache.log4j.Logger;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.MatchAllQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;

import com.hp.hpl.jena.util.iterator.Filter;

import de.mpg.imeji.logic.authorization.util.SecurityUtil;
import de.mpg.imeji.logic.search.elasticsearch.ElasticService;
import de.mpg.imeji.logic.search.elasticsearch.ElasticService.ElasticTypes;
import de.mpg.imeji.logic.search.elasticsearch.factory.util.ElasticSearchFactoryUtil;
import de.mpg.imeji.logic.search.elasticsearch.model.ElasticFields;
import de.mpg.imeji.logic.search.model.SearchElement;
import de.mpg.imeji.logic.search.model.SearchFields;
import de.mpg.imeji.logic.search.model.SearchGroup;
import de.mpg.imeji.logic.search.model.SearchLogicalRelation;
import de.mpg.imeji.logic.search.model.SearchLogicalRelation.LOGICAL_RELATIONS;
import de.mpg.imeji.logic.search.model.SearchMetadata;
import de.mpg.imeji.logic.search.model.SearchOperators;
import de.mpg.imeji.logic.search.model.SearchPair;
import de.mpg.imeji.logic.search.model.SearchQuery;
import de.mpg.imeji.logic.search.model.SearchTechnicalMetadata;
import de.mpg.imeji.logic.search.util.SearchUtils;
import de.mpg.imeji.logic.util.DateFormatter;
import de.mpg.imeji.logic.vo.Grant;
import de.mpg.imeji.logic.vo.Grant.GrantType;
import de.mpg.imeji.logic.vo.ImejiLicenses;
import de.mpg.imeji.logic.vo.Properties.Status;
import de.mpg.imeji.logic.vo.User;

/**
 * Factory to create an ElasticSearch query from the {@link SearchQuery}
 *
 * @author bastiens
 *
 */
public class ElasticQueryFactory {
  private static final Logger LOGGER = Logger.getLogger(ElasticQueryFactory.class);

  /**
   * Build a {@link QueryBuilder} from a {@link SearchQuery}
   *
   * @param query
   * @return
   * @return
   */
  public static QueryBuilder build(SearchQuery query, String folderUri, User user,
      ElasticTypes type) {
    final BoolQueryBuilder q = QueryBuilders.boolQuery();
    final QueryBuilder searchQuery = buildSearchQuery(query, user);
    final QueryBuilder containerQuery = buildContainerFilter(folderUri);
    final QueryBuilder securityQuery = buildSecurityQuery(user, folderUri);
    final QueryBuilder statusQuery = buildStatusQuery(query, user);
    if (!isMatchAll(searchQuery)) {
      q.must(searchQuery);
    }
    if (!isMatchAll(containerQuery)) {
      q.must(containerQuery);
    }
    if (!isMatchAll(securityQuery)) {
      q.must(securityQuery);
    }
    if (type != ElasticTypes.users && !isMatchAll(statusQuery)) {
      q.must(statusQuery);
    }
    return q;
  }

  /**
   * True if the query is a match all query
   *
   * @param q
   * @return
   */
  private static boolean isMatchAll(QueryBuilder q) {
    return q instanceof MatchAllQueryBuilder;
  }

  /**
   * The {@link QueryBuilder} with the search query
   *
   * @param query
   * @return
   */
  private static QueryBuilder buildSearchQuery(SearchQuery query, User user) {
    if (query == null || query.getElements().isEmpty()) {
      return QueryBuilders.matchAllQuery();
    }
    return buildSearchQuery(query.getElements(), user);
  }

  /**
   * Build a query for the status
   *
   * @param query
   * @param user
   * @return
   */
  private static QueryBuilder buildStatusQuery(SearchQuery query, User user) {
    if (user == null) {
      // Not Logged in: can only view release objects
      return fieldQuery(ElasticFields.STATUS, Status.RELEASED.name(), SearchOperators.EQUALS,
          false);
    } else if (query != null && hasStatusQuery(query.getElements())) {
      // Don't filter, since it is done later via the searchquery
      return QueryBuilders.matchAllQuery();
    } else {
      // Default = don't view discarded objects
      return fieldQuery(ElasticFields.STATUS, Status.WITHDRAWN.name(), SearchOperators.EQUALS,
          true);
    }

  }

  /**
   * Check if at least on {@link SearchPair} is related to the status. If yes, return true
   *
   * @param elements
   * @return
   */
  private static boolean hasStatusQuery(List<SearchElement> elements) {
    for (final SearchElement e : elements) {
      if (e instanceof SearchPair && ((SearchPair) e).getField() == SearchFields.status) {
        return true;
      } else if (e instanceof SearchGroup && hasStatusQuery(e.getElements())) {
        return true;
      }
    }
    return false;
  }

  /**
   * Build a {@link QueryBuilder} from a list of {@link SearchElement}
   *
   * @param elements
   * @return
   */
  private static QueryBuilder buildSearchQuery(List<SearchElement> elements, User user) {
    boolean OR = true;
    final BoolQueryBuilder q = QueryBuilders.boolQuery();
    for (final SearchElement el : elements) {
      if (el instanceof SearchPair) {
        if (OR) {
          q.should(termQuery((SearchPair) el, user));
        } else {
          q.must(termQuery((SearchPair) el, user));
        }
      } else if (el instanceof SearchLogicalRelation) {
        OR = ((SearchLogicalRelation) el).getLogicalRelation() == LOGICAL_RELATIONS.OR ? true
            : false;
      } else if (el instanceof SearchGroup) {
        if (OR) {
          q.should(buildSearchQuery(((SearchGroup) el).getElements(), user));
        } else {
          q.must(buildSearchQuery(((SearchGroup) el).getElements(), user));
        }
      }
    }
    return q;
  }

  /**
   * Build the security Query according to the user.
   *
   * @param user
   * @return
   */
  private static QueryBuilder buildSecurityQuery(User user, String folderUri) {
    if (user != null) {
      if (SecurityUtil.authorization().isSysAdmin(user)) {
        // Admin: can view everything
        return QueryBuilders.matchAllQuery();
      } else {
        // normal user
        return buildGrantQuery(user, null);
      }
    }
    return QueryBuilders.matchAllQuery();
  }

  /**
   * Build a Filter for a container (album or folder): if the containerUri is not null, search
   * result will be filter to this only container
   *
   * @param containerUri
   * @return
   */
  private static QueryBuilder buildContainerFilter(String containerUri) {
    if (containerUri != null) {
      return fieldQuery(ElasticFields.FOLDER, containerUri, SearchOperators.EQUALS, false);
    }
    return QueryBuilders.matchAllQuery();
  }


  /**
   * Build the query with all Read grants
   *
   * @param grants
   * @return
   */
  private static QueryBuilder buildGrantQuery(User user, GrantType grantType) {
    final Collection<Grant> grants =
        SecurityUtil.authorization().toGrantList(SecurityUtil.authorization().getAllGrants(user));
    final BoolQueryBuilder q = QueryBuilders.boolQuery();
    // Add query for all release objects
    if (grantType == null) {
      q.should(
          fieldQuery(ElasticFields.STATUS, Status.RELEASED.name(), SearchOperators.EQUALS, false));
    }
    // if grantType is null, set it to READ
    grantType = grantType == null ? GrantType.READ : grantType;
    // Add query for each read grant
    for (final Grant g : grants) {
      if (g.asGrantType() == grantType) {
        q.should(fieldQuery(ElasticFields.FOLDER, g.getGrantFor().toString(),
            SearchOperators.EQUALS, false));
        q.should(fieldQuery(ElasticFields.ID, g.getGrantFor().toString(), SearchOperators.EQUALS,
            false));
      }
    }
    return q;
  }



  /**
   * Create a QueryBuilder with a term filter (see
   * https://www.elastic.co/guide/en/elasticsearch/reference/current/query-dsl-term-filter.html)
   *
   * @param pair
   * @return
   */
  private static QueryBuilder termQuery(SearchPair pair, User user) {
    if (pair instanceof SearchMetadata) {
      return metadataFilter((SearchMetadata) pair);
    } else if (pair instanceof SearchTechnicalMetadata) {
      return technicalMetadataQuery((SearchTechnicalMetadata) pair);
    }
    final SearchFields index = pair.getField();
    switch (index) {
      case all:
        return allQuery(pair);
      case fulltext:
        return contentQuery(
            fieldQuery(ElasticFields.FULLTEXT, pair.getValue(), pair.getOperator(), pair.isNot()));
      case checksum:
        return contentQuery(
            fieldQuery(ElasticFields.CHECKSUM, pair.getValue(), pair.getOperator(), pair.isNot()));
      case col:
        return fieldQuery(ElasticFields.FOLDER, pair.getValue(), pair.getOperator(), pair.isNot());
      case description:
        return fieldQuery(ElasticFields.DESCRIPTION, pair.getValue(), pair.getOperator(),
            pair.isNot());
      case author_familyname:
        return fieldQuery(ElasticFields.AUTHOR_FAMILYNAME, pair.getValue(), pair.getOperator(),
            pair.isNot());
      case author_givenname:
        return fieldQuery(ElasticFields.AUTHOR_GIVENNAME, pair.getValue(), pair.getOperator(),
            pair.isNot());
      case author:
        return fieldQuery(ElasticFields.AUTHOR_COMPLETENAME, pair.getValue(), pair.getOperator(),
            pair.isNot());
      case author_org:
        return fieldQuery(ElasticFields.AUTHOR_ORGANIZATION, pair.getValue(), pair.getOperator(),
            pair.isNot());
      case title:
        return fieldQuery(ElasticFields.NAME, pair.getValue(), pair.getOperator(), pair.isNot());
      case created:
        return timeQuery(ElasticFields.CREATED.name(), pair.getValue(), pair.getOperator(),
            pair.isNot());
      case creator:
        return fieldQuery(ElasticFields.CREATOR,
            ElasticSearchFactoryUtil.getUserId(pair.getValue()), pair.getOperator(), pair.isNot());
      case collaborator:
        return roleQueryWithoutCreator(ElasticFields.READ, pair.getValue(), pair.isNot());
      case uploader:
        return roleQuery(ElasticFields.UPLOAD, pair.getValue(), pair.isNot());
      case read:
        return fieldQuery(ElasticFields.READ, pair.getValue(), SearchOperators.EQUALS,
            pair.isNot());
      case date:
        return timeQuery(ElasticFields.METADATA_NUMBER.name(), pair.getValue(), pair.getOperator(),
            pair.isNot());
      case filename:
        return fieldQuery(ElasticFields.NAME, pair.getValue(), pair.getOperator(), pair.isNot());
      case filetype:
        return fileTypeQuery(pair);
      case grant:
        // same as grant_type
        final GrantType grant = pair.getValue().equals("upload") ? GrantType.EDIT
            : GrantType.valueOf(pair.getValue().toUpperCase());
        return buildGrantQuery(user, grant);
      case grant_type:
        // same as grant
        final GrantType grantType = pair.getValue().equals("upload") ? GrantType.EDIT
            : GrantType.valueOf(pair.getValue().toUpperCase());
        return buildGrantQuery(user, grantType);
      case member:
        return fieldQuery(ElasticFields.MEMBER, pair.getValue(), pair.getOperator(), pair.isNot());
      case license:
        return licenseQuery(pair);
      case modified:
        return timeQuery(ElasticFields.MODIFIED.name(), pair.getValue(), pair.getOperator(),
            pair.isNot());
      case number:
        return fieldQuery(ElasticFields.METADATA_NUMBER, pair.getValue(), pair.getOperator(),
            pair.isNot());
      case person:
        return fieldQuery(ElasticFields.METADATA_TEXT, pair.getValue(), pair.getOperator(),
            pair.isNot());
      case person_family:
        return fieldQuery(ElasticFields.METADATA_FAMILYNAME, pair.getValue(), pair.getOperator(),
            pair.isNot());
      case person_given:
        return fieldQuery(ElasticFields.METADATA_GIVENNAME, pair.getValue(), pair.getOperator(),
            pair.isNot());
      case person_org:
        return fieldQuery(ElasticFields.METADATA_FAMILYNAME, pair.getValue(), pair.getOperator(),
            pair.isNot());
      case status:
        return fieldQuery(ElasticFields.STATUS, formatStatusSearchValue(pair), pair.getOperator(),
            pair.isNot());
      case text:
        return fieldQuery(ElasticFields.METADATA_TEXT, pair.getValue(), pair.getOperator(),
            pair.isNot());
      case time:
        return timeQuery(ElasticFields.METADATA_NUMBER.name(), pair.getValue(), pair.getOperator(),
            pair.isNot());
      case location:
        return fieldQuery(ElasticFields.METADATA_TEXT, pair.getValue(), pair.getOperator(),
            pair.isNot());
      case metadatatype:
        return fieldQuery(ElasticFields.METADATA_TYPE, pair.getValue(), pair.getOperator(),
            pair.isNot());
      case url:
        return fieldQuery(ElasticFields.METADATA_URI, pair.getValue(), pair.getOperator(),
            pair.isNot());
      case hasgrant:
        return fieldQuery(ElasticFields.CREATOR, pair.getValue(), pair.getOperator(), pair.isNot());
      case coordinates:
        return fieldQuery(ElasticFields.METADATA_LOCATION, pair.getValue(), pair.getOperator(),
            pair.isNot());
      case pid:
        return fieldQuery(ElasticFields.PID, pair.getValue(), pair.getOperator(), pair.isNot());
      case info_label:
        return fieldQuery(ElasticFields.INFO_LABEL, pair.getValue(), pair.getOperator(),
            pair.isNot());
      case info_text:
        return fieldQuery(ElasticFields.INFO_TEXT, pair.getValue(), pair.getOperator(),
            pair.isNot());
      case info_url:
        return fieldQuery(ElasticFields.INFO_URL, pair.getValue(), pair.getOperator(),
            pair.isNot());
      case email:
        return fieldQuery(ElasticFields.EMAIL, pair.getValue(), SearchOperators.EQUALS,
            pair.isNot());
      case itemId:
        return QueryBuilders.hasParentQuery(ElasticTypes.items.name(),
            fieldQuery(ElasticFields.ID, pair.getValue(), pair.getOperator(), pair.isNot()));
    }
    return matchNothing();
  }

  /**
   * Create a {@link QueryBuilder} for a {@link SearchMetadata}
   *
   * @param md
   * @return
   */
  private static QueryBuilder metadataFilter(SearchMetadata md) {
    if (md.getField() == null) {
      return metadataQuery(
          fieldQuery(ElasticFields.METADATA_TEXT, md.getValue(), md.getOperator(), md.isNot()),
          md.getIndex());
    }
    switch (md.getField()) {
      case text:
        return metadataQuery(
            fieldQuery(ElasticFields.METADATA_TEXT, md.getValue(), md.getOperator(), md.isNot()),
            md.getIndex());
      case number:
        return metadataQuery(
            fieldQuery(ElasticFields.METADATA_NUMBER, md.getValue(), md.getOperator(), md.isNot()),
            md.getIndex());
      case date:
        return metadataQuery(
            fieldQuery(ElasticFields.METADATA_TEXT, md.getValue(), md.getOperator(), md.isNot()),
            md.getIndex());
      case url:
        return metadataQuery(
            fieldQuery(ElasticFields.METADATA_URI, md.getValue(), md.getOperator(), md.isNot()),
            md.getIndex());
      case person_family:
        return metadataQuery(fieldQuery(ElasticFields.METADATA_FAMILYNAME, md.getValue(),
            md.getOperator(), md.isNot()), md.getIndex());
      case person_given:
        return metadataQuery(fieldQuery(ElasticFields.METADATA_GIVENNAME, md.getValue(),
            md.getOperator(), md.isNot()), md.getIndex());
      case coordinates:
        // return metadataQuery(fieldQuery(ElasticFields.METADATA_LOCATION, md.getValue(),
        // md.getOperator(), md.isNot()), md.getIndex());
        return metadataQuery(geoQuery(md.getValue()), md.getIndex());
      case time:
        return metadataQuery(timeQuery(ElasticFields.METADATA_NUMBER.field(), md.getValue(),
            md.getOperator(), md.isNot()), md.getIndex());
      default:
        return metadataQuery(
            fieldQuery(ElasticFields.METADATA_TEXT, md.getValue(), md.getOperator(), md.isNot()),
            md.getIndex());
    }
  }

  /**
   * Create a {@link QueryBuilder}
   *
   * @param index
   * @param value
   * @param operator
   * @return
   */
  private static QueryBuilder fieldQuery(ElasticFields field, String value,
      SearchOperators operator, boolean not) {
    return fieldQuery(field.field(), value, operator, not);
  }

  /**
   * Create a {@link QueryBuilder}
   *
   * @param index
   * @param value
   * @param operator
   * @return
   */
  private static QueryBuilder fieldQuery(String fieldName, String value, SearchOperators operator,
      boolean not) {
    QueryBuilder q = null;

    if (operator == null) {
      operator = SearchOperators.EQUALS;
    }
    switch (operator) {
      case EQUALS:
        q = matchFieldQuery(fieldName, ElasticSearchFactoryUtil.escape(value));
        break;
      case GREATER:
        q = greaterThanQuery(fieldName, value);
        break;
      case LESSER:
        q = lessThanQuery(fieldName, value);
        break;
      default:
        // default is REGEX
        q = matchFieldQuery(fieldName, value);
        break;
    }
    return negate(q, not);
  }

  /**
   * Search for a date saved as a time (i.e) in ElasticSearch
   *
   * @param field
   * @param dateString
   * @param operator
   * @param not
   * @return
   */
  private static QueryBuilder timeQuery(String field, String dateString, SearchOperators operator,
      boolean not) {
    QueryBuilder q = null;
    if (operator == null) {
      operator = SearchOperators.EQUALS;
    }
    switch (operator) {
      case GREATER:
        q = greaterThanQuery(field, Long.toString(DateFormatter.getTime(dateString)));
        break;
      case LESSER:
        q = lessThanQuery(field, Long.toString(DateFormatter.getTime(dateString)));
        break;
      default:
        q = QueryBuilders.rangeQuery(field)
            .gte(Long.toString(DateFormatter.parseDate(dateString).getTime()))
            .lte(Long.toString(DateFormatter.parseDate2(dateString).getTime()));
        break;
    }
    return negate(q, not);
  }



  /**
   * Create a {@link QueryBuilder} - used to sarch for metadata which are defined with a statement
   *
   * @param index
   * @param value
   * @param operator
   * @param statement
   * @return
   */
  private static QueryBuilder metadataQuery(QueryBuilder valueQuery, String statement) {
    return QueryBuilders.nestedQuery(ElasticFields.METADATA.field(),
        QueryBuilders.boolQuery().must(valueQuery).must(
            fieldQuery(ElasticFields.METADATA_INDEX, statement, SearchOperators.EQUALS, false)));

  }

  /**
   * Query for technical metadata
   *
   * @param label
   * @param value
   * @param not
   * @return
   */
  private static QueryBuilder technicalMetadataQuery(SearchTechnicalMetadata tmd) {
    return contentQuery(QueryBuilders.nestedQuery(ElasticFields.TECHNICAL.field(),
        QueryBuilders.boolQuery()
            .must(fieldQuery(ElasticFields.TECHNICAL_NAME, tmd.getLabel(), SearchOperators.EQUALS,
                false))
            .must(fieldQuery(ElasticFields.TECHNICAL_VALUE, tmd.getValue(), tmd.getOperator(),
                tmd.isNot()))));
  }

  /**
   * Search for a match (not the exact value)
   *
   * @param field
   * @param value
   * @return
   */
  private static QueryBuilder matchFieldQuery(String fieldName, String value) {
    if (ElasticFields.ALL.name().equals(fieldName)) {
      return QueryBuilders
          .queryStringQuery(value + " " + ElasticFields.NAME.field() + ".suggest:" + value);
    }
    return QueryBuilders.queryStringQuery(fieldName + ":" + value);
  }

  /**
   * Search for value greater than the searched value
   *
   * @param field
   * @param value
   * @return
   */
  private static QueryBuilder greaterThanQuery(String fieldName, String value) {
    if (NumberUtils.isNumber(value)) {
      return QueryBuilders.rangeQuery(fieldName).gte(Double.parseDouble(value));
    }
    return matchNothing();
  }

  /**
   * Search for value smaller than searched value
   *
   * @param field
   * @param value
   * @return
   */
  private static QueryBuilder lessThanQuery(String fieldName, String value) {
    if (NumberUtils.isNumber(value)) {
      return QueryBuilders.rangeQuery(fieldName).lte(Double.parseDouble(value));
    }
    return matchNothing();
  }

  private static QueryBuilder geoQuery(String value) {
    final String[] values = value.split(",");
    String distance = "1km";
    final double lat = Double.parseDouble(values[0]);
    final double lon = Double.parseDouble(values[1]);
    if (values.length == 3) {
      distance = values[2];
    }
    return QueryBuilders.geoDistanceQuery(ElasticFields.METADATA_LOCATION.field())
        .distance(distance).point(lat, lon);
  }

  /**
   * Add NOT filter to the {@link Filter} if not is true
   *
   * @param f
   * @param not
   * @return
   */
  private static QueryBuilder negate(QueryBuilder f, boolean not) {
    return not ? QueryBuilders.boolQuery().mustNot(f) : f;
  }

  /**
   * Return a query which find nothing
   *
   * @return
   */
  private static QueryBuilder matchNothing() {
    return QueryBuilders.boolQuery().mustNot(QueryBuilders.matchAllQuery());
  }

  /**
   * True if the uri is an uri folder
   *
   * @param uri
   * @return
   */
  private static boolean isFolderUri(String uri) {
    return uri.contains("/collection/") ? true : false;
  }

  /**
   * Create the query for role="email". Role can be uploader, collaborator. Objects where the user
   * is creator are not excluded
   *
   * @param email
   * @param not
   * @return
   */
  private static QueryBuilder roleQuery(ElasticFields role, String email, boolean not) {
    final String userId = ElasticSearchFactoryUtil.getUserId(email);
    final QueryBuilder q1 = QueryBuilders.termsLookupQuery(ElasticFields.ID.field())
        .lookupIndex(ElasticService.DATA_ALIAS).lookupId(userId)
        .lookupType(ElasticTypes.users.name()).lookupPath(role.field());
    final QueryBuilder q2 = QueryBuilders.termsLookupQuery(ElasticFields.FOLDER.field())
        .lookupIndex(ElasticService.DATA_ALIAS).lookupId(userId)
        .lookupType(ElasticTypes.users.name()).lookupPath(role.field());
    final BoolQueryBuilder q = QueryBuilders.boolQuery().should(q1).should(q2);
    final List<String> groups = ElasticSearchFactoryUtil.getGroupsOfUser(userId);
    for (final String group : groups) {
      q.should(QueryBuilders.termsLookupQuery(ElasticFields.ID.field())
          .lookupIndex(ElasticService.DATA_ALIAS).lookupId(group)
          .lookupType(ElasticTypes.usergroups.name()).lookupPath(role.field()));
    }
    return q;
  }



  /**
   * Create the query for role="email". Role can be uploader, collaborator. Objects where the user
   * is creator will be excluded
   *
   * @param email
   * @param not
   * @return
   */
  private static QueryBuilder roleQueryWithoutCreator(ElasticFields role, String email,
      boolean not) {
    final String userId = ElasticSearchFactoryUtil.getUserId(email);
    final BoolQueryBuilder q1 = QueryBuilders.boolQuery();
    q1.must(QueryBuilders.termsLookupQuery(ElasticFields.ID.field())
        .lookupIndex(ElasticService.DATA_ALIAS).lookupId(userId)
        .lookupType(ElasticTypes.users.name()).lookupPath(role.field()));
    q1.mustNot(fieldQuery(ElasticFields.CREATOR, ElasticSearchFactoryUtil.getUserId(email),
        SearchOperators.EQUALS, not));
    final BoolQueryBuilder q2 = QueryBuilders.boolQuery();
    q2.must(QueryBuilders.termsLookupQuery(ElasticFields.FOLDER.field())
        .lookupIndex(ElasticService.DATA_ALIAS).lookupId(userId)
        .lookupType(ElasticTypes.users.name()).lookupPath(role.field()));
    q2.mustNot(fieldQuery(ElasticFields.CREATOR, ElasticSearchFactoryUtil.getUserId(email),
        SearchOperators.EQUALS, not));
    final BoolQueryBuilder q = QueryBuilders.boolQuery().should(q1).should(q2);
    final List<String> groups = ElasticSearchFactoryUtil.getGroupsOfUser(userId);
    for (final String group : groups) {
      q.should(QueryBuilders.termsLookupQuery(ElasticFields.ID.field())
          .lookupIndex(ElasticService.DATA_ALIAS).lookupId(group)
          .lookupType(ElasticTypes.usergroups.name()).lookupPath(role.field()));
    }
    return q;
  }

  /**
   * Build Query for all terms
   * 
   * @param pair
   * @return
   */
  private static QueryBuilder allQuery(SearchPair pair) {
    final BoolQueryBuilder f = QueryBuilders.boolQuery()
        .should(fieldQuery(ElasticFields.ALL, pair.getValue(), SearchOperators.EQUALS, false));
    if (NumberUtils.isNumber(pair.getValue())) {
      f.should(fieldQuery(ElasticFields.METADATA_NUMBER, pair.getValue(), SearchOperators.EQUALS,
          false));
    }
    return negate(f, pair.isNot());
  }

  /**
   * Search for content
   * 
   * @param q
   * @return
   */
  private static QueryBuilder contentQuery(QueryBuilder q) {
    return QueryBuilders.hasChildQuery(ElasticTypes.content.name(), q);
  }

  /**
   * Make a query for license
   * 
   * @param pair
   * @return
   */
  private static QueryBuilder licenseQuery(SearchPair pair) {
    final BoolQueryBuilder licenseQuery = QueryBuilders.boolQuery();
    for (final String licenseName : pair.getValue().split(" OR ")) {
      if (ImejiLicenses.NO_LICENSE.equals(licenseName)) {
        licenseQuery.should(QueryBuilders.boolQuery()
            .mustNot(QueryBuilders.existsQuery(ElasticFields.LICENSE.field())));
      } else if ("*".equals(licenseName)) {
        licenseQuery.should(QueryBuilders.existsQuery(ElasticFields.LICENSE.field()));
      } else {
        licenseQuery
            .should(fieldQuery(ElasticFields.LICENSE, licenseName, SearchOperators.EQUALS, false));
      }
    }
    return licenseQuery;
  }

  /**
   * Build a query to search for filetypes
   * 
   * @param pair
   * @return
   */
  private static QueryBuilder fileTypeQuery(SearchPair pair) {
    final BoolQueryBuilder filetypeQuery = QueryBuilders.boolQuery();
    for (final String ext : SearchUtils.parseFileTypesAsExtensionList(pair.getValue())) {
      filetypeQuery.should(
          fieldQuery(ElasticFields.NAME, "\"." + ext + "\"", SearchOperators.EQUALS, false));
    }
    return filetypeQuery;
  }

  /**
   * Format the search value for the status, as indexed
   * 
   * @param pair
   * @return
   */
  private static String formatStatusSearchValue(SearchPair pair) {
    String status = pair.getValue();
    if (status.contains("#")) {
      status = status.split("#")[1];
    }
    if ("private".equals(status)) {
      status = Status.PENDING.name();
    }
    if ("public".equals(status)) {
      status = Status.RELEASED.name();
    }
    if ("discarded".equals(status)) {
      status = Status.WITHDRAWN.name();
    }
    return status.toUpperCase();
  }
}
