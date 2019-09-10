package steps;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import cucumber.api.java.bs.A;
import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import models.points.Badge;
import org.junit.Assert;
import play.libs.Json;
import play.mvc.Http;
import play.mvc.Result;
import play.test.Helpers;
import repositories.quests.QuestRepository;
import scala.Int;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static play.test.Helpers.*;

public class AchievementTrackerTestSteps {

    /**
     * Singleton class which stores generally used variables
     */
    private TestContext testContext = TestContext.getInstance();


    /**
     * New instance of the general test steps.
     */
    private GeneralTestSteps generalTestSteps = new GeneralTestSteps();


    /**
     * The achievement tracker URI endpoint.
     */
    private static final String ACHIEVEMENT_TRACKER_URI = "/v1/achievementTracker/";


    /**
     * The profiles uri.
     */
    private static final String PROFILES_URI = "/v1/profiles";


    /**
     * The points URI endpoint
     */
    private static final String POINTS_URI = "/points";


    /**
     * The quest URI endpoint.
     */
    private static final String QUEST_URI = "/v1/quests";


    /**
     * The destination endpoint uri.
     */
    private static final String DESTINATION_URI = "/v1/destinations";


    /**
     * The trip endpoint uri.
     */
    private static final String TRIP_URI = "/v1/trips";


    /**
     * Authorisation token for sessions
     */
    private static final String AUTHORIZED = "authorized";


    /**
     * The quest attempt URI endpoint.
     */
    private static final String QUEST_ATTEMPT_URI = "/attempt/";


    /**
     * The quest attempt guess URI endpoint.
     */
    private static final String GUESS_URI = "/guess/";


    /**
     * The quest attempt check in URI endpoint.
     */
    private static final String CHECK_IN_URI = "/checkIn";


    /**
     * Boolean to evaluate against the response body of a riddle guess.
     */
    private static final boolean SUCCESSFUL_GUESS = true;
    private static final boolean UNSUCCESSFUL_GUESS = false;


    private static final long TO_CHECK_IN_RIDDLE_ID = 3L;
    private static final long TO_GUESS_RIDDLE_ID = 4L;
    private static final long DESTINATION_TO_GUESS = 1834L;
    private static final long INCORRECT_DESTINATION_GUESS = 6024L;


    // -------------------------- IDs of users used for tests ---------------------------

    private static final long REG_USER_ID = 2L;
    private static final long OTHER_USER_ID = 3L;
    private static final long GLOBAL_ADMIN_ID = 1L;


    private static final int EMPTY_LIST_RESPONSE_SIZE = 2;
    private static final String DESTINATIONS = "destinations";
    private static final String TRIPS = "trips";
    private static final String QUESTS = "quests";


    /**
     * The static Json variable keys for a trip.
     */
    private static final String NAME = "trip_name";
    private static final String ID = "id";
    private static final String TRIP_DESTINATIONS = "trip_destinations";
    private static final String DESTINATION = "destination_id";
    private static final String START_DATE = "start_date";
    private static final String END_DATE = "end_date";


    private static final String NAME_STRING = "Name";
    private static final String DESTINATION_STRING = "Destination";
    private static final String START_DATE_STRING = "Start Date";
    private static final String END_DATE_STRING = "End Date";

    private static final String ACHIEVEMENT_TRACKER = "achievementTracker";
    private static final String BADGE = "badge";

    private ObjectNode tripJson;
    private List<ObjectNode> tripDestinations = new ArrayList<>();

    /**
     * And object mapper used during tests.
     */
    private ObjectMapper mapper = new ObjectMapper();

    /**
     * Points the profile started with.
     */
    private int startingPoints;


    /**
     * Points the profile has after an action.
     */
    private int currentPoints;


    private void getPointsRequest(String userId) {
        Http.RequestBuilder request = fakeRequest()
                .method(GET)
                .uri(ACHIEVEMENT_TRACKER_URI + userId + POINTS_URI)
                .session(AUTHORIZED, testContext.getLoggedInId());
        Result result = route(testContext.getApplication(), request);
        testContext.setStatusCode(result.status());
        testContext.setResponseBody(Helpers.contentAsString(result));
    }


    private void sendRiddleGuessRequest(long attemptId, long destinationId) {
        Http.RequestBuilder request = fakeRequest()
                .method(POST)
                .uri(QUEST_URI + QUEST_ATTEMPT_URI + attemptId + GUESS_URI + destinationId)
                .session(AUTHORIZED, testContext.getLoggedInId());
        Result result = route(testContext.getApplication(), request);
        testContext.setStatusCode(result.status());
        testContext.setResponseBody(Helpers.contentAsString(result));
    }


    private void sendCheckInRequest(long attemptId) {
        Http.RequestBuilder request = fakeRequest()
                .method(POST)
                .uri(QUEST_URI + QUEST_ATTEMPT_URI + attemptId + CHECK_IN_URI)
                .session(AUTHORIZED, testContext.getLoggedInId());
        Result result = route(testContext.getApplication(), request);
        testContext.setStatusCode(result.status());
        testContext.setResponseBody(Helpers.contentAsString(result));
    }


    /**
     * Converts a given data table of trip values to a Json node object of this trip.
     *
     * @param dataTable     the data table containing values of a trip.
     */
    private void convertDataTableTripJson(io.cucumber.datatable.DataTable dataTable, int index) {
        //Get all input from the data table
        List<Map<String, String>> list = dataTable.asMaps(String.class, String.class);
        String name       = list.get(index).get(NAME_STRING);

        //Add values to a JsonNode
        ObjectMapper mapper = new ObjectMapper();
        tripJson = mapper.createObjectNode();
        tripJson.put(NAME, name);
    }


    /**
     * Converts a given data table of trip destination values to a Json node object of this trip.
     *
     * @param dataTable     the data table containing values of a trip destination.
     */
    private void convertDataTableToObjectiveJson(io.cucumber.datatable.DataTable dataTable, int index) {
        //Get all input from the data table
        List<Map<String, String>> list = dataTable.asMaps(String.class, String.class);
        String destination         = list.get(index).get(DESTINATION_STRING);
        String startDate           = list.get(index).get(START_DATE_STRING);
        String endDate             = list.get(index).get(END_DATE_STRING);

        // If there is already destinations in the trip, then we need the dates to be spaced out.
        int dateBuffer = 0;
        if (!tripDestinations.isEmpty()) {
            dateBuffer += 10;
        }

        if (startDate.isEmpty()) {
            startDate = generalTestSteps.getDateBuffer(true, dateBuffer);
        }

        if (endDate.isEmpty()) {
            endDate = generalTestSteps.getDateBuffer(false, dateBuffer);
        }


        //Add values to a JsonNode
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode json = mapper.createObjectNode();

        json.put(DESTINATION, destination);
        json.put(START_DATE, startDate);
        json.put(END_DATE, endDate);
        tripDestinations.add(json);
    }


    /**
     * Sends the backend request to create a trip.
     *
     * @param json  the given Json body containing a trip.
     */
    private void createTripRequest(JsonNode json) {
        Http.RequestBuilder request = fakeRequest()
                .method(POST)
                .session(AUTHORIZED, testContext.getLoggedInId())
                .bodyJson(json)
                .uri(TRIP_URI + "/" + testContext.getLoggedInId());
        Result result = route(testContext.getApplication(), request);
        testContext.setStatusCode(result.status());
        tripDestinations.clear();

        testContext.setResponseBody(Helpers.contentAsString(result));
    }

    private Badge getBadgeForLoggedInUser() {
        System.out.println(testContext.getLoggedInId());
        System.out.println(testContext.getTargetId());
        Http.RequestBuilder request = fakeRequest()
                .method(GET)
                .session(AUTHORIZED, testContext.getTargetId())
                .uri(PROFILES_URI);
        Result result = route(testContext.getApplication(), request);

        System.out.println(Helpers.contentAsString(result));

        Iterator<JsonNode> iterator = generalTestSteps.getTheResponseIterator(Helpers.contentAsString(result));

        // Finds badge from the iterator
        Badge badge = new Badge();
        while (iterator.hasNext()) {
            JsonNode jsonProfile = iterator.next();
            badge = Json.fromJson(jsonProfile.get(ACHIEVEMENT_TRACKER).get(BADGE), Badge.class);
        }
        return badge;
    }


    @Given("I have some starting points")
    public void iHaveSomeStartingPoints() throws IOException {
        String userToView = testContext.getLoggedInId();
        getPointsRequest(userToView);
        JsonNode responseBody = mapper.readTree(testContext.getResponseBody());
        startingPoints = responseBody.get("userPoints").asInt();
    }


    @Given("I currently have no \"(.*)\" created$")
    public void iCurrentlyHaveNoTripsCreated(String endPoint) {
        String uri = "";
        switch(endPoint) {
            case DESTINATIONS:
                uri = DESTINATION_URI;
                break;
            case TRIPS:
                uri = TRIP_URI;
                break;
            case QUESTS:
                uri = QUEST_URI;
                break;
        }

        Http.RequestBuilder request = fakeRequest()
                .method(GET)
                .uri(uri + "/" + testContext.getLoggedInId())
                .session(AUTHORIZED, testContext.getLoggedInId());
        Result result = route(testContext.getApplication(), request);
        testContext.setStatusCode(result.status());
        testContext.setResponseBody(Helpers.contentAsString(result));
        Assert.assertEquals(OK, testContext.getStatusCode());
        Assert.assertEquals(EMPTY_LIST_RESPONSE_SIZE, testContext.getResponseBody().length());
    }


    @Given("^the user has (\\d+) points$")
    public void theUserHasPoints(int userPoints) {
        // Write code here that turns the phrase above into concrete actions
        throw new cucumber.api.PendingException();
    }


    @Given("^my current progress towards the \"(.*)\" badge is (\\d+)$")
    public void myCurrentProgressTowardsTheBadgeIs(String badgeName, Integer progress) {
        Badge badge = getBadgeForLoggedInUser(badgeName);
        Assert.assertEquals(badge.getProgress(), progress);
        System.out.println(Json.toJson(badge));
    }


    @When("^I search for profiles with (\\d+) points$")
    public void iSearchForProfilesWithPoints(int searchPoints) {
        // Write code here that turns the phrase above into concrete actions
        throw new cucumber.api.PendingException();
    }


    @When("I solve the current riddle for a Quest")
    public void iSolveTheFirstRiddleOfTheQuestWithID() throws IOException {
        sendRiddleGuessRequest(TO_GUESS_RIDDLE_ID, DESTINATION_TO_GUESS);
        JsonNode responseBody = mapper.readTree(testContext.getResponseBody());
        Assert.assertEquals(SUCCESSFUL_GUESS, responseBody.get("guessResult").asBoolean());
    }


    @When("I create a new trip with the following values")
    public void iCreateANewTripWithTheFollowingValues(io.cucumber.datatable.DataTable dataTable) {
        testContext.setTargetId(testContext.getLoggedInId());
        for (int i = 0 ; i < dataTable.height() -1 ; i++) {
            convertDataTableTripJson(dataTable, i);
        }
    }


    @When("the trip has a destination with the following values")
    public void theTripHasADestinationWithTheFollowingValues(io.cucumber.datatable.DataTable dataTable) {
        for (int i = 0 ; i < dataTable.height() -1 ; i++) {
            convertDataTableToObjectiveJson(dataTable, i);
            tripJson.putArray(TRIP_DESTINATIONS).addAll(tripDestinations);
        }
    }


    @When("I create the trip")
    public void ICreateTheTrip() {
        createTripRequest(tripJson);
    }


    @Then("I have gained points")
    public void iHaveGainedPoints() throws IOException {
        String userToView = testContext.getLoggedInId();
        getPointsRequest(userToView);

        JsonNode responseBody = mapper.readTree(testContext.getResponseBody());
        currentPoints = responseBody.get("userPoints").asInt();
        Assert.assertTrue("Current points is not greater than starting points",currentPoints > startingPoints);
    }


    @When("I try to view my points")
    public void iTryToViewMyPoints() {
        String userToView = testContext.getLoggedInId();
        getPointsRequest(userToView);
    }


    @Then("I am given my point total")
    public void iAmGivenMyPointTotal() throws IOException {
        // Get the userPoints value from the JSON, convert it to an int and store it under current points if not null.

        JsonNode responseBody = mapper.readTree(testContext.getResponseBody());
        Assert.assertNotNull("No userPoints JSON value", responseBody.get("userPoints"));

        currentPoints =  responseBody.get("userPoints").asInt();

        // Points should never be negative, so something has gone wrong.
        Assert.assertTrue("Points value is negative", currentPoints >= 0);

    }


    @When("I try to view another user's points value")
    public void iTryToViewAnotherUserSPointsValue() {
        String userToView = Long.toString(OTHER_USER_ID);
        getPointsRequest(userToView);
    }


    @Then("I am given their total number of points")
    public void iAmGivenTheirTotalNumberOfPoints() throws IOException {
        JsonNode responseBody = mapper.readTree(testContext.getResponseBody());
        Assert.assertNotNull("No userPoints JSON value", responseBody.get("userPoints"));

        currentPoints =  responseBody.get("userPoints").asInt();

        // Points should never be negative, so something has gone wrong.
        Assert.assertTrue("Points value is negative", currentPoints >= 0);
    }


    @When("I incorrectly guess the answer to a quest riddle")
    public void iIncorrectlyGuessTheAnswerToAQuestRiddle() throws IOException {
        sendRiddleGuessRequest(TO_GUESS_RIDDLE_ID, INCORRECT_DESTINATION_GUESS);
        JsonNode responseBody = mapper.readTree(testContext.getResponseBody());
        Assert.assertEquals(UNSUCCESSFUL_GUESS, responseBody.get("guessResult").asBoolean());
    }


    @When("I check into a destination")
    public void iCheckIntoADestination() {
        sendCheckInRequest(TO_CHECK_IN_RIDDLE_ID);

        Assert.assertEquals(200, testContext.getStatusCode());

    }


    @Then("I have not gained points")
    public void iHaveNotGainedPoints() throws IOException {
        String userToView = testContext.getLoggedInId();
        getPointsRequest(userToView);

        JsonNode responseBody = mapper.readTree(testContext.getResponseBody());
        currentPoints = responseBody.get("userPoints").asInt();
        Assert.assertEquals("Starting and end point values are not equal", currentPoints, startingPoints);
    }


    @Then("^I gain the \"(.*)\" badge with level (\\d+)$")
    public void iGainTheBadgeWithLevel(String obtainedBadge, int badgeLevel) {
        // Write code here that turns the phrase above into concrete actions
        throw new cucumber.api.PendingException();
    }


    @Then("I have completed the quest")
    public void iHaveCompletedTheQuest() {
        // Write code here that turns the phrase above into concrete actions
        throw new cucumber.api.PendingException();
    }

}
