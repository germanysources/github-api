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

import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.Assert;
import org.junit.Ignore;

@Ignore
public class TestGraphQLSearch extends GraphQLSearch{

    private static GitHub root;

    @BeforeClass
    public static void setup()throws IOException{
	
	root = GitHubBuilder.fromCredentials().build();

    }

    public TestGraphQLSearch(){
	super(root, "name url");
    }

    @Test
    public void tgetApiQuery(){
	String exp = "query($search_term:String!,$results:Int!) { search(query:$search_term, type:REPOSITORY, first:$results){ nodes { ... on Repository{ name url }}}}";
	Assert.assertEquals("$fields not replaced correct", exp, getApiQuery());
    }

    @Test
    public void texecute()throws IOException{

	System.out.println("check manually results first 2 results for search term curl org:curl:");
	execute("curl org:curl", 2);

    }
    
    @Override
    protected void putRepositoryCount(int count){
    }

    @Override
    protected void putRepository(ObjectNode repo){	
	System.out.println(repo.toString());
    }


}
