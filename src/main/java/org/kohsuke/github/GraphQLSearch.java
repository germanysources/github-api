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

    private final static String tailApiUrl = "graphql";
    //the query send to github api with variables
    private final static String ApiQuery = "query($search_term:String!,$results:Int!) { search(query:$search_term, type:REPOSITORY, first:$results){ nodes { ... on Repository{ $fields }}}}";
    private final static String VarFields = "$fields";
    private GitHub root;
    private String fields;
    private Requester request;

    public GraphQLSearch(GitHub root, String fields){
	this.root = root;
	this.fields = fields;
	request = new Requester(root).method("POST");
    }

    protected String getApiQuery(){	
	return ApiQuery.replace(VarFields.subSequence(0,VarFields.length()),fields.subSequence(0,fields.length()));
    }

    public void execute(String SearchTerm, int results)throws IOException, MalFormedQueryException{
	
	/*Put the query and the variables object into request body.
	  $fields must be replaced at the client otherwise github api throws a parsing error 
	  
	 */
	request.with("query", getApiQuery());
	request._with("variables", new SearchVariables(SearchTerm, results));
		
	ObjectNode DataAndError = (ObjectNode)root.MAPPER.readTree(request.asStream(root.getApiUrl() + '/'+ tailApiUrl));
	/*only malformed json in the post payload get the status code 400
	  DataAndError can contain in the body errors!
	 */

	if(DataAndError.get("errors") != null)
	    throw new MalFormedQueryException((ArrayNode)DataAndError.get("errors"));
	
	//parse content looks like {"data":{"search":{"repositoryCount":8,"nodes":[{result}]}}}
	parse(DataAndError);
	
    }

    private void parse(ObjectNode data){
	ObjectNode results = (ObjectNode)(data.get("data").get("search"));
	
	try{
	    putRepositoryCount(results.get("repositoryCount").asInt());
	}catch(NullPointerException e){
	    //repository count is absent
	}
	ArrayNode repos = (ArrayNode)results.get("nodes");
	for(int i=0;i<repos.size();i++){
	    putRepository((ObjectNode)repos.get(i));
	}

    }

    protected abstract void putRepositoryCount(int count);

    protected abstract void putRepository(ObjectNode repo);

}
