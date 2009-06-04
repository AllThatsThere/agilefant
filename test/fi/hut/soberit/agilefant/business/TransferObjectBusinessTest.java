package fi.hut.soberit.agilefant.business;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

import org.junit.*;
import static org.junit.Assert.*;

import fi.hut.soberit.agilefant.business.impl.TransferObjectBusinessImpl;
import fi.hut.soberit.agilefant.model.Iteration;
import fi.hut.soberit.agilefant.model.Project;
import fi.hut.soberit.agilefant.model.Story;
import fi.hut.soberit.agilefant.model.Task;
import fi.hut.soberit.agilefant.model.User;
import fi.hut.soberit.agilefant.util.ResponsibleContainer;
import fi.hut.soberit.agilefant.util.StoryTO;
import fi.hut.soberit.agilefant.util.TaskTO;

public class TransferObjectBusinessTest {

    private TransferObjectBusinessImpl transferObjectBusiness = new TransferObjectBusinessImpl();
    private ProjectBusiness projectBusiness;
    Iteration iteration;
    Story story1;
    Story story2;
    Task task;
    User assignedUser;
    User notAssignedUser; 
    
    @Before
    public void setUp() {
        projectBusiness = createMock(ProjectBusiness.class);
        transferObjectBusiness.setProjectBusiness(projectBusiness);
        
        iteration = new Iteration();
        Project project = new Project();
        project.setId(8474);
        iteration.setParent(project);
        
        assignedUser = new User();
        assignedUser.setId(666);
        notAssignedUser = new User();
        notAssignedUser.setId(515);
        
        story1 = new Story();
        story2 = new Story();
        task = new Task();
        story1.setId(1265);
        story2.setId(8472);        
        task.setId(1236);
    }
    
    @Test
    public void testContructIterationDataWithUserData() {
        story1.getTasks().add(task);
        iteration.setStories(Arrays.asList(story1, story2));
        
        replay(projectBusiness);
        
        Collection<Story> actualStories = new ArrayList<Story>();
        actualStories.addAll(transferObjectBusiness
                .constructIterationDataWithUserData(iteration, Arrays.asList(assignedUser)));

        verify(projectBusiness);
        
        assertTrue("List does not contain correct story transfer object",
                containsStoryWithId(story1.getId(), actualStories));
        assertTrue("List does not contain correct story transfer object",
                containsStoryWithId(story2.getId(), actualStories));
        assertTrue("Story 1 transfer object does not contain correct task transfer object",
                containsTaskWithId(task.getId(), story1.getId(), actualStories));
    }
    
    @Test
    public void testContructIterationDataWithUserData_emptyIteration() {
        iteration.getStories().clear();
        Collection<StoryTO> stories =
            transferObjectBusiness.constructIterationDataWithUserData(iteration, null);
        assertEquals(0, stories.size());
    }
    
    @Test
    public void testConstructTaskTO_delegate() {
        task.setIteration(iteration);
        expect(projectBusiness.getAssignedUsers((Project)task.getIteration().getParent()))
            .andReturn(Arrays.asList(assignedUser));
        replay(projectBusiness);
        
        assertEquals(task.getId(),
                transferObjectBusiness.constructTaskTO(task).getId());
        
        verify(projectBusiness);
    }
    
    @Test
    public void testConstructTaskTO() {
        task.setIteration(iteration);
        task.setResponsibles(Arrays.asList(assignedUser, notAssignedUser));       
        
        replay(projectBusiness);
        TaskTO actualTO = transferObjectBusiness
            .constructTaskTO(task, Arrays.asList(assignedUser));
        verify(projectBusiness);
        
        assertEquals("Task and transfer object id's not equal",
                task.getId(), actualTO.getId());
       
        boolean assignedUserFound = false;
        boolean notAssignedUserFound = false;
        for (ResponsibleContainer rc : actualTO.getUserData()) {
            if (rc.getUser() == assignedUser) {
                assignedUserFound = true;
                assertTrue("The assigned user seems not to be assigned to project", rc.isInProject());
            }
            if (rc.getUser() == notAssignedUser) {
                notAssignedUserFound = true;
                assertFalse("The not assigned user seems to be assigned to project", rc.isInProject());
            }
        }
        assertTrue("User not found in responsible containers", assignedUserFound && notAssignedUserFound);
    }
    
    /**
     * Helper method to check that the stories list contains a story with a specific id.
     */
    private boolean containsStoryWithId(int expectedId, Collection<Story> storiesList) {
        boolean idFound = false;
        for (Story actualStory : storiesList) {
            if (actualStory.getId() == expectedId) {
                idFound = true;
                break;
            }
        }
        return idFound;
    }

    /**
     * Helper method to check that the task list contains a task with a specific id.
     * @param storyId TODO
     */
    private boolean containsTaskWithId(int expectedId, int storyId, Collection<Story> storiesList) {
        boolean idFound = false;
        for (Story actualStory : storiesList) {
            if (actualStory.getId() == storyId) {
                for (Task task : actualStory.getTasks()) {
                    if (task.getId() == expectedId) {
                        idFound = true;
                        break;
                    }
                }
                break;
            }
        }
        return idFound;
    }
    
}
