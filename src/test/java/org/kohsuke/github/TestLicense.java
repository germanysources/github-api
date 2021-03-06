/*
 * The MIT License
 *
 * Copyright (c) 2018, Johannes Gerbershagen <johannes.gerbershagen@kabelmail.de>
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
import org.junit.Test;
import org.junit.Assert;
import org.junit.BeforeClass;

import java.io.*;
import java.nio.file.*;
import java.util.*;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Test pur license information (key, name)
 */
public class TestLicense extends GHRepository{

    private static GitHub account;
    private static Properties object;//the repository object
    private static ObjectMapper mapper;

    //directory expected test results
    private static final Path dir = FileSystems.getDefault().getPath("src", "test", "resources", "org", "kohsuke", "github", "RepositoryTest");

    @BeforeClass
    public static void Setup()throws Exception{
	account = GitHub.connect();
	object = new Properties();
	object.load(new BufferedInputStream(new FileInputStream(dir.resolve("repo.license.properties").toFile())));
	mapper = new ObjectMapper();
    }

    @Test
    public void get()throws Exception{

	GHLicense expLicense = mapper.readValue(dir.resolve("expLicense.json").toFile(), GHLicense.class);
	GHRepository repo = account.getRepository(object.getProperty("get.fullName"));
	Assert.assertEquals("license key", expLicense.getKey(), repo.getLicenseKey().getKey());
	Assert.assertEquals("license name", expLicense.getName(), repo.getLicenseKey().getName());
	
    }
}
