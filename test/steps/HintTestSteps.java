package steps;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import models.objectives.Objective;
import models.profiles.Profile;
import org.junit.Assert;
import play.mvc.Http;
import play.mvc.Result;
import play.test.Helpers;
import repositories.objectives.ObjectiveRepository;
import repositories.profiles.ProfileRepository;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import static play.test.Helpers.*;

public class HintTestSteps {

    /**
     * Singleton class which stores generally used variables.
     */
    private TestContext testContext = TestContext.getInstance();


    /**
     * Authorisation token for sessions
     */
    private static final String AUTHORIZED = "authorized";


    /**
     * The objective uri.
     */
    private static final String OBJECTIVE_URI = "/v1/objectives/";


    /**
     * The hints uri.
     */
    private static final String HINTS_URI = "/hints";
    private static final String NEW_HINT_URI = "/new";
    private static final String SEEN_HINTS_URI = "/seen";


    /**
     * String constants for reading data from data tables and json.
     */
    private static final String MESSAGE_STRING = "Message";
    private static final String MESSAGE = "message";
    private static final String PAGE_SIZE = "pageSize";
    private static final String PAGE_NUMBER = "pageNumber";

    private static final String USER_ID_ONE = "1";
    private static final String ID = "id";

    /**
     * String constants for creating a query string.
     */
    private static final String QUESTION_MARK = "?";
    private static final String AND = "&";
    private static final String EQUALS = "=";

    /**
     * Repository to access the objectives in the running application.
     */
    private ObjectiveRepository objectiveRepository =
            testContext.getApplication().injector().instanceOf(ObjectiveRepository.class);

    /**
     * Repository to access the profiles in the running application.
     */
    private ProfileRepository profileRepository =
            testContext.getApplication().injector().instanceOf(ProfileRepository.class);


    /**
     * Object mapper for creating and reading Json objects.
     */
    private ObjectMapper objectMapper =
            testContext.getApplication().injector().instanceOf(ObjectMapper.class);


    /**
     * Converts a given data table containing a hint into a json node object.
     *
     * @param dataTable the data table containing the hint message.
     * @param index     the position in the data table the json components are extracted from.
     * @return a JsonNode of a hint containing information extracted from the data table.
     */
    private JsonNode convertDataTableToHintJson(io.cucumber.datatable.DataTable dataTable, int index) {
        List<Map<String, String>> list = dataTable.asMaps(String.class, String.class);
        String hintMessage = list.get(index).get(MESSAGE_STRING);

        ObjectMapper mapper = new ObjectMapper();
        ObjectNode json = mapper.createObjectNode();
        json.put(MESSAGE, hintMessage);

        return json;
    }


    /**
     * Sends a request to create a hint from the given json node.
     *
     * @param json        a JsonNode containing the body of the request.
     * @param objectiveId the Id of the objective the hint is created for.
     */
    private void createHintRequest(JsonNode json, int objectiveId) {
        Http.RequestBuilder request = fakeRequest()
                .method(POST)
                .bodyJson(json)
                .uri(OBJECTIVE_URI + objectiveId + HINTS_URI + "/" + testContext.getTargetId())
                .session(AUTHORIZED, testContext.getLoggedInId());
        Result result = route(testContext.getApplication(), request);
        testContext.setStatusCode(result.status());
        testContext.setResponseBody(Helpers.contentAsString(result));
    }


    /**
     * Sends a request to retrieve all hints for an objective with the id specified.
     *
     * @param objectiveId       the id of of the objective that is having its hints retrieved.
     */
    private void fetchAllHintsRequest(int objectiveId) {
        // If user is not logged in, id is required for url
        String targetId = testContext.getLoggedInId() != null ? testContext.getLoggedInId() : USER_ID_ONE;

        Http.RequestBuilder request = fakeRequest()
                .method(GET)
                .session(AUTHORIZED, testContext.getLoggedInId())
                .uri(OBJECTIVE_URI + objectiveId + HINTS_URI + "/" + targetId);
        Result result = route(testContext.getApplication(), request);
        testContext.setStatusCode(result.status());
        testContext.setResponseBody(Helpers.contentAsString(result));
    }


    /**
     * Sends a request to retrieve all hints for an objective with the id specified.
     * Includes the addition of a search query.
     *
     * @param objectiveId       the id of of the objective that is having its hints retrieved.
     */
    private void fetchAllHintsRequest(int objectiveId, String searchQuery) {
        Http.RequestBuilder request = fakeRequest()
                .method(GET)
                .session(AUTHORIZED, testContext.getLoggedInId())
                .uri(OBJECTIVE_URI + objectiveId + HINTS_URI + "/" + testContext.getLoggedInId() + searchQuery);
        Result result = route(testContext.getApplication(), request);
        testContext.setStatusCode(result.status());
        testContext.setResponseBody(Helpers.contentAsString(result));
    }


    /**
     * Sends a request to retrieve all hints seen by the target user for an objective with the given id.
     *
     * @param objectiveId       the id of of the objective that is having its hints retrieved.
     */
    private void fetchSeenHintRequest(int objectiveId) {
        Http.RequestBuilder request = fakeRequest()
                .method(GET)
                .session(AUTHORIZED, testContext.getLoggedInId())
                .uri(OBJECTIVE_URI + objectiveId + HINTS_URI + "/" + testContext.getTargetId() + SEEN_HINTS_URI);
        Result result = route(testContext.getApplication(), request);
        testContext.setStatusCode(result.status());
        testContext.setResponseBody(Helpers.contentAsString(result));
    }


    /**
     * Sends a request to retrieve one new hint for target user for an objective with the given id.
     * This hint has not been previously seen by the user.
     *
     * @param objectiveId       the id of of the objective that the retrieved hint is for.
     */
    private void fetchNewHintRequest(int objectiveId) {
        Http.RequestBuilder request = fakeRequest()
                .method(GET)
                .session(AUTHORIZED, testContext.getLoggedInId())
                .uri(OBJECTIVE_URI + objectiveId + HINTS_URI + "/" + testContext.getTargetId() + NEW_HINT_URI);
        Result result = route(testContext.getApplication(), request);
        testContext.setStatusCode(result.status());
        testContext.setResponseBody(Helpers.contentAsString(result));
    }

    /**
     * Creates a search query to contain pagination information.
     *
     * @param pageSize      the page size requested.
     * @param pageNumber    the page number requested.
     * @return              a string containing the page size and number for a query string.
     */
    private String createSearchQuery(String pageSize, String pageNumber) {
        return QUESTION_MARK
                + PAGE_SIZE + EQUALS + pageSize
                + AND
                + PAGE_NUMBER + EQUALS + pageNumber;
    }


    @Given("^I own the objective with id (\\d+)$")
    public void iOwnTheObjectiveWithId(Integer objectiveId) {
        Objective objective = objectiveRepository.findById(Long.valueOf(objectiveId));
        Assert.assertEquals(testContext.getLoggedInId(), objective.getOwner().getId().toString());
    }


    @Given("^I do not own the objective with id (\\d+)$")
    public void iDoNotOwnTheObjectiveWithId(Integer objectiveId) {
        Objective objective = objectiveRepository.findById(Long.valueOf(objectiveId));
        Assert.assertNotEquals(testContext.getLoggedInId(), objective.getOwner().getId().toString());
    }


    @Given("^I have solved the objective with id (\\d+)$")
    public void iHaveSolvedTheObjectiveWithId(Integer objectiveId) {
        Profile loggedInUser = profileRepository.findById(Long.parseLong(testContext.getLoggedInId()));
        Objective objective = objectiveRepository.findById(Long.valueOf(objectiveId));
        Assert.assertTrue(objectiveRepository.hasSolved(loggedInUser, objective));
    }


    @Given("^I have not solved the objective with id (\\d+)$")
    public void iHaveNotSolvedTheObjectiveWithId(Integer objectiveId) {
        Profile loggedInUser = profileRepository.findById(Long.parseLong(testContext.getLoggedInId()));
        Objective objective = objectiveRepository.findById(Long.valueOf(objectiveId));
        Assert.assertFalse(objectiveRepository.hasSolved(loggedInUser, objective));
    }


    @Given("^no hints exist for the objective with id (\\d+)$")
    public void noHintsExistForTheObjectiveWithId(Integer objectiveId) {
        Objective objective = objectiveRepository.findById(Long.valueOf(objectiveId));
        Assert.assertNotNull(objective);
        Assert.assertTrue(objective.getHints().isEmpty());
    }


    @When("^I attempt to create a hint with the following values for the objective with id (\\d+)$")
    public void iAttemptToCreateAHintWithTheFollowingValuesForTheObjectiveWithId(Integer objectiveId, io.cucumber.datatable.DataTable dataTable) {
        testContext.setTargetId(testContext.getLoggedInId());
        for (int i = 0; i < dataTable.height() - 1; i++) {
            JsonNode json = convertDataTableToHintJson(dataTable, i);
            createHintRequest(json, objectiveId);
        }
    }


    @When("^I attempt to create a hint for user (\\d+) with the following values for the objective with id (\\d+)$")
    public void iAttemptToCreateAHintWithTheFollowingValuesForTheObjectiveWithId(Integer userId, Integer objectiveId, io.cucumber.datatable.DataTable dataTable) {
        testContext.setTargetId(userId.toString());
        for (int i = 0; i < dataTable.height() - 1; i++) {
            JsonNode json = convertDataTableToHintJson(dataTable, i);
            createHintRequest(json, objectiveId);
        }
    }


    @When("^I attempt to retrieve all hints for the objective with id (\\d+)$")
    public void iAttemptToRetrieveAllHintsForTheObjectiveWithId(Integer objectiveId) {
        fetchAllHintsRequest(objectiveId);
    }


    @When("^I attempt to retrieve all hints for the objective with id (\\d+) with page size \"(.*)\" and page number \"(.*)\"$")
    public void iAttemptToRetrieveAllHintsForTheObjectiveWithIdWithPageSizeAndPageNumber(Integer objectiveId, String pageSize, String pageNumber) {
        fetchAllHintsRequest(objectiveId, createSearchQuery(pageSize, pageNumber));
    }


    @When("^I request a new hint for objective with id (\\d+)$")
    public void iRequestANewHintForObjectiveWithId(Integer objectiveId) {
        testContext.setTargetId(testContext.getLoggedInId());
        fetchNewHintRequest(objectiveId);
    }


    @When("^I request a new hint for user (\\d+) for objective with id (\\d+)$")
    public void iRequestANewHintForUserForObjectiveWithId(Integer userId, Integer objectiveId) {
        testContext.setTargetId(userId.toString());
        fetchNewHintRequest(objectiveId);
    }


    @When("^I request all the hints for user (\\d+) that I have seen for objective with id (\\d+)$")
    public void iRequestAllTheHintsForUserThatIHaveSeenForObjectiveWithId(Integer userId, Integer objectiveId) {
        testContext.setTargetId(userId.toString());
        fetchSeenHintRequest(objectiveId);
    }


    @When("^I request all the hints that I have seen for objective with id (\\d+)$")
    public void iRequestAllTheHintsThatIHaveSeenForObjectiveWithId(Integer objectiveId) {
        testContext.setTargetId(testContext.getLoggedInId());
        fetchSeenHintRequest(objectiveId);
    }


    @Then("^the response contains (\\d+) hints$")
    public void theResponseContainsHints(Integer expectedNumberOfHints) throws IOException {
        Integer responseLength = objectMapper.readTree(testContext.getResponseBody()).size();
        Assert.assertEquals(expectedNumberOfHints, responseLength);
    }


    @Then("^I receive a hint with id (\\d+)$")
    public void iReceiveAHintWithId(Integer expectedHintId) throws IOException {
        Integer actualHintId = objectMapper.readTree(testContext.getResponseBody()).get(ID).asInt();
        Assert.assertEquals(expectedHintId, actualHintId);
    }


    @Then("the response contains the following message")
    public void theResponseContainsTheFollowingMessage(io.cucumber.datatable.DataTable dataTable) throws IOException {
        String expectedMessage = dataTable.asList().get(0);
        String message = objectMapper.readTree(testContext.getResponseBody()).get(MESSAGE).asText();
        Assert.assertTrue(expectedMessage.contains(message));
    }
}
