/*
 * The MIT License
 *
 * Copyright (c) 2019, Johannes Gerbershagen
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package org.kohsuke.github;

import java.io.IOException;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.ArrayNode;

public abstract class GraphQLSearch{

    /** @see https://developer.github.com/v4/guides/resource-limitations/ */
    private final static int ResultLimit = 100;
    private final static String tailApiUrl = "graphql";
    //the query send to github api with variables for the first fetch
    protected final static String InitialApiQuery = "query($search_term:String!,$results:Int!) { search(query:$search_term, type:REPOSITORY, first:$results){ repositoryCount edges {  cursor } nodes { ... on Repository{ $fields }}}}";
    //the query send to github in pagination mode
    protected final static String PaginationApiQuery = "query($search_term:String!,$results:Int!, $cursor:String!) { search(query:$search_term, type:REPOSITORY, after:$cursor, first:$results){ edges {  cursor } nodes { ... on Repository{ $fields }}}}";
    private final static String VarFields = "$fields";
    private GitHub root;
    private String fields;
    private Requester request;
    private int count;
    private String lastCursor;

    public GraphQLSearch(GitHub root, String fields){
	this.root = root;
	this.fields = fields;
	request = new Requester(root).method("POST");
    }

    protected String getApiQuery(String ApiQuery){	
	return ApiQuery.replace(VarFields.subSequence(0,VarFields.length()),fields.subSequence(0,fields.length()));
    }

    /**
     * Execute search with graph ql api.
     * @param results max results to fetch. If bigger then 100 (limit for graphql api), the api call is splitted (pagination).
     */
    public void execute(String SearchTerm, int results)throws IOException, MalFormedQueryException{
	
	int resultPerCall = results;
	if(resultPerCall > ResultLimit)
	    resultPerCall = ResultLimit;

	int fetched = executeFirst(SearchTerm, resultPerCall);
	if(count < results)
	    results = count;
	while(fetched < results){
	    if((count - fetched) < resultPerCall)
		resultPerCall = count - fetched;
	    fetched += executePagination(SearchTerm, resultPerCall);
	}

    }

    /** Fetches all found results
     *
     */
    public void execute(String SearchTerm)throws IOException, MalFormedQueryException{
	
	int resultPerCall = ResultLimit;

	int fetched = executeFirst(SearchTerm, resultPerCall);
	while(fetched < count){
	    if((count - fetched) < resultPerCall)
		resultPerCall = count - fetched;
	    fetched += executePagination(SearchTerm, resultPerCall);
	}

    }

    /** executes pagination api call with cursor */
    protected int executePagination(String SearchTerm, int results)throws IOException, MalFormedQueryException{
	
	request.with("query", getApiQuery(PaginationApiQuery));
	request._with("variables", new SearchCursorVariables(SearchTerm, results, lastCursor));

	ObjectNode DataAndError = (ObjectNode)root.MAPPER.readTree(request.asStream(root.getApiUrl() + '/'+ tailApiUrl));
	if(DataAndError.get("errors") != null)
	    throw new MalFormedQueryException((ArrayNode)DataAndError.get("errors"));
	
	//parse content looks like {"data":{"edges":[{"cursor":"Y3Vyc29yOjE="}]"search":{"repositoryCount":8,"nodes":[{result}]}}}
	return parse(DataAndError);

    }

    /** executes first api call. Cursor is not need here
     */
    protected int executeFirst(String SearchTerm, int results)throws IOException, MalFormedQueryException{
	
	/*Put the query and the variables object into request body.
	  $fields must be replaced at the client otherwise github api throws a parsing error 	  
	 */
	request.with("query", getApiQuery(InitialApiQuery));
	request._with("variables", new SearchVariables(SearchTerm, results));

	ObjectNode DataAndError = (ObjectNode)root.MAPPER.readTree(request.asStream(root.getApiUrl() + '/'+ tailApiUrl));
	/*only malformed json in the post payload get the status code 400
	  DataAndError can contain in the body errors!
	 */

	if(DataAndError.get("errors") != null)
	    throw new MalFormedQueryException((ArrayNode)DataAndError.get("errors"));
	
	//parse content looks like {"data":{"edges":[{"cursor":"Y3Vyc29yOjE="}]"search":{"repositoryCount":8,"nodes":[{result}]}}}
	return parse(DataAndError);
	
    }

    /**
     * @returns found repositories
     */
    private int parse(ObjectNode data)throws IOException{
	ObjectNode results = (ObjectNode)(data.get("data").get("search"));
	
	try{
	    putRepositoryCount(results.get("repositoryCount").asInt());
	}catch(NullPointerException e){
	    //in pagination mode repositoryCount is omitted
	}
	
	//repositories
	ArrayNode repos = (ArrayNode)results.get("nodes");
	for(int i=0;i<repos.size();i++){
	    putRepository((ObjectNode)repos.get(i));
	}
	//last cursor
	ArrayNode cursors = (ArrayNode)results.get("edges");
	if(cursors.get(cursors.size()-1) == null)
	    System.out.println("cursors.get(cursors.size()) == null");
	lastCursor = cursors.get(cursors.size()-1).get("cursor").asText();
	return repos.size();

    }

    protected void putRepositoryCount(int count){
	this.count = count;
    }

    protected abstract void putRepository(ObjectNode repo)throws IOException;

}
