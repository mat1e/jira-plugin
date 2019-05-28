package hudson.plugins.jira;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import com.cloudbees.hudson.plugins.folder.Folder;
import hudson.model.FreeStyleProject;
import hudson.util.FormValidation;
import io.jenkins.plugins.casc.misc.ConfiguredWithCode;
import io.jenkins.plugins.casc.misc.JenkinsConfiguredWithCodeRule;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;
import org.mockito.Mockito;
import org.mockito.Spy;

public class JiraProjectPropertyTest {

    @Rule
    public final JenkinsRule r = new JenkinsConfiguredWithCodeRule();

    private FreeStyleProject freeStyleProject;
    private Folder folder;
    private List<JiraSite> firstList;

    @Before
    public void initialize() throws Exception {
        folder = r.jenkins.createProject(Folder.class, "first");
        JiraFolderProperty jiraFolderProperty = new JiraFolderProperty();
        firstList = new ArrayList<>();
        firstList.add(new JiraSite("https://first.com/"));
        jiraFolderProperty.setSites(firstList);
        folder.getProperties().add(jiraFolderProperty);
    }

    @Test
    public void getSitesNullWithoutFolder() throws Exception {
        FreeStyleProject freeStyleProject = r.createFreeStyleProject();
        JiraProjectProperty prop = new JiraProjectProperty(null, false, null);
        freeStyleProject.addProperty(prop);
        JiraProjectProperty actual = freeStyleProject.getProperty(JiraProjectProperty.class);
        assertNotNull(actual);
        assertNull(actual.getSite());
    }

    @Test
    public void getSitesNullWithFolder() throws Exception {
        freeStyleProject = folder.createProject(FreeStyleProject.class, "something");
        JiraProjectProperty prop = new JiraProjectProperty(null, false, null);
        freeStyleProject.addProperty(prop);
        JiraProjectProperty property = freeStyleProject.getProperty(JiraProjectProperty.class);
        assertNotNull(property);
        assertNull(property.getSite());
    }

    @Test
    @ConfiguredWithCode("single-site.yml")
    public void getSiteFromProjectProperty() {
        JiraProjectProperty prop = new JiraProjectProperty(null, false, null);
        JiraSite site = prop.getSite();
        @SuppressWarnings("ConstantConditions")
        String actual = site.getUrl().toExternalForm();
        assertEquals("https://jira.com/", actual);
    }

    @Test
    @ConfiguredWithCode("single-site.yml")
    public void getSiteFromSingleEntry() throws Exception {
        freeStyleProject = r.createFreeStyleProject();
        JiraSite expected = JiraGlobalConfiguration.get().getSites().get(0);
        JiraProjectProperty prop = new JiraProjectProperty(null, false, null);
        freeStyleProject.addProperty(prop);
        JiraProjectProperty property = freeStyleProject.getProperty(JiraProjectProperty.class);
        assertNotNull(property);
        assertNotNull(property.getSite());
        assertEquals(expected.getName(), property.siteName);
        r.assertEqualDataBoundBeans(expected, property.getSite());
    }

    @Test
    @ConfiguredWithCode("multiple-sites.yml")
    public void getSiteFromFirstGlobalMultipleEntryMultipleSites() throws Exception {
        freeStyleProject = r.createFreeStyleProject();
        JiraSite expected = JiraGlobalConfiguration.get().getSites().get(0);
        JiraProjectProperty prop = new JiraProjectProperty(null, false, null);
        freeStyleProject.addProperty(prop);
        JiraProjectProperty property = freeStyleProject.getProperty(JiraProjectProperty.class);
        assertNotNull(property);
        assertNotNull(property.getSite());
        assertEquals(expected.getName(), property.siteName);
        r.assertEqualDataBoundBeans(expected, property.getSite());
    }

    @Test
    @ConfiguredWithCode("multiple-sites.yml")
    public void getSiteFromSecondGlobalEntryMultipleSites() throws Exception {
        freeStyleProject = r.createFreeStyleProject();
        JiraSite expected = new JiraSite("https://jira.com/");
        JiraProjectProperty prop = new JiraProjectProperty(expected.getName(), false, null);
        freeStyleProject.addProperty(prop);
        JiraProjectProperty property = freeStyleProject.getProperty(JiraProjectProperty.class);
        assertNotNull(property);
        assertNotNull(property.getSite());
        assertEquals(expected.getName(), property.siteName);
        r.assertEqualDataBoundBeans(expected, property.getSite());
    }

    @Test
    @ConfiguredWithCode("single-site.yml")
    public void getSiteFromFirstFolderLayer() throws Exception {
        freeStyleProject = folder.createProject(FreeStyleProject.class, "something");
        JiraSite expected = firstList.get(0);
        JiraProjectProperty prop = new JiraProjectProperty(expected.getName(), false, null);
        freeStyleProject.addProperty(prop);
        JiraProjectProperty property = freeStyleProject.getProperty(JiraProjectProperty.class);
        assertNotNull(property);
        assertNotNull(property.getSite());
        assertEquals(expected.getName(), property.siteName);
        r.assertEqualDataBoundBeans(expected, property.getSite());
    }

    @Test
    @ConfiguredWithCode("single-site.yml")
    public void getSiteFromNestedFolderLayer() throws Exception {
        Folder secondFolder = folder.createProject(Folder.class, "second");
        freeStyleProject = secondFolder.createProject(FreeStyleProject.class, "something");
        // testing we can get value from folder above.
        JiraSite expected = firstList.get(0);
        JiraProjectProperty prop = new JiraProjectProperty(expected.getName(), false, null);
        freeStyleProject.addProperty(prop);
        JiraProjectProperty property = freeStyleProject.getProperty(JiraProjectProperty.class);
        assertNotNull(property);
        assertNotNull(property.getSite());
        assertEquals(expected.getName(), property.siteName);
        r.assertEqualDataBoundBeans(expected, property.getSite());
    }

    @Test
    @ConfiguredWithCode("single-site.yml")
    public void getJiraProjectSession() throws Exception {
        freeStyleProject = r.createFreeStyleProject();
        JiraSite site = new JiraSite("https://jira.com/");
        JiraSite spySite = Mockito.spy(site);
        JiraSession sessionMock = Mockito.mock(JiraSession.class);
        Mockito.when(spySite.getSession()).thenReturn(sessionMock);
        JiraProjectProperty prop = new JiraProjectProperty(spySite.getName(), false, null);
        freeStyleProject.addProperty(prop);
        JiraProjectProperty property = freeStyleProject.getProperty(JiraProjectProperty.class);
        JiraProjectProperty propertySpy = Mockito.spy(property);
        assertNotNull(propertySpy);
        Mockito.when(propertySpy.getSite()).thenReturn(spySite);
        assertNotNull(propertySpy.getSite());
        assertNotNull(propertySpy.getJiraProjectSession());
    }
    
    @Test(expected = Test.None.class)
    public void doTestConnectionTest() throws IOException {
        freeStyleProject = r.createFreeStyleProject();
        JiraSite site = new JiraSite("https://jira.com/");
        String jobCredentialId = "jobCredentialId";
        String siteName = "siteName";
        JiraSite spySite = Mockito.spy(site);
        JiraSession sessionMock = Mockito.mock(JiraSession.class);
        Mockito.when(spySite.getSession(jobCredentialId)).thenReturn(sessionMock);
        JiraProjectProperty prop = new JiraProjectProperty(spySite.getName(), false, null);
        freeStyleProject.addProperty(prop);
        FormValidation result = JiraProjectProperty.DescriptorImpl.doTestConnection(jobCredentialId, siteName);
        Assert.assertEquals("Failed to login to JIRA", result.getMessage());
        result = JiraProjectProperty.DescriptorImpl.doTestConnection(null, siteName);
        Assert.assertEquals("Credential must be specified", result.getMessage());
    }
    
    
}
