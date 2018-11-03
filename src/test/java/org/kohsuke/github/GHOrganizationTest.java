package org.kohsuke.github;

import org.junit.After;
import org.junit.Assert;
import org.junit.Test;

import java.nio.file.*;

import java.io.IOException;

public class GHOrganizationTest extends AbstractGitHubApiTestBase {

    public static final String GITHUB_API_TEST = "github-api-test";
    private GHOrganization org;

    @Override
    public void setUp() throws Exception {
        super.setUp();
        org = gitHub.getOrganization("github-api-test-org");
    }
    
    @Test
    public void testCreateRepository() throws IOException {
        GHRepository repository = org.createRepository(GITHUB_API_TEST,
            "a test repository used to test kohsuke's github-api", "http://github-api.kohsuke.org/", "Core Developers", true);
        Assert.assertNotNull(repository);
    }

    @Test
    public void testCreateRepositoryWithAutoInitialization() throws IOException {
        GHRepository repository = org.createRepository(GITHUB_API_TEST)
                .description("a test repository used to test kohsuke's github-api")
                .homepage("http://github-api.kohsuke.org/")
                .team(org.getTeamByName("Core Developers"))
                .autoInit(true).create();
        Assert.assertNotNull(repository);
        Assert.assertNotNull(repository.getReadme());
    }

    @After
    public void cleanUp() throws Exception {
        GHRepository repository = org.getRepository(GITHUB_API_TEST);
        repository.delete();
    }

    @Test
    public void Membership()throws Exception{
	
	//load expected membership from file
	Path PathexpMembership = FileSystems.getDefault().getPath("src", "test", "resources", "org", "kohsuke", "github", "expMembership.json");	
	GHMembership expMembebership = gitHub.MAPPER.readValue(PathexpMembership.toFile(), GHMembership.class);
	
	org = gitHub.getOrganization(expMembership.getOrganization().getLogin());
	GHMembership actMembership = org.getMemberShipDetails(expMembership.getUser().getLogin());
	Assert.assertEquals("membership not correct", expMemberShip, actMembership);

    }
}
