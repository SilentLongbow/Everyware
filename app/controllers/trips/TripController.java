package controllers.trips;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import controllers.points.AchievementTrackerController;
import models.profiles.Profile;
import models.destinations.Destination;
import models.trips.Trip;
import models.trips.TripDestination;
import models.util.ApiError;
import models.util.Errors;
import repositories.destinations.DestinationRepository;
import repositories.profiles.ProfileRepository;
import repositories.trips.TripRepository;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Result;
import util.AuthenticationUtil;

import com.google.inject.Inject;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class TripController extends Controller {

    private static final String NAME = "trip_name";
    private static final String TRIP_DESTINATIONS = "trip_destinations";
    private static final String START_DATE = "start_date";
    private static final String END_DATE = "end_date";
    private static final String DESTINATION_ID = "destination_id";
    public static final String REWARD = "reward";
    private static final String NEW_TRIP_ID = "newTripId";
    private static final String PAGE_FUTURE = "pageFuture";
    private static final String PAGE_PAST = "pagePast";
    private static final String PAGE_SIZE_FUTURE = "pageSizeFuture";
    private static final String PAGE_SIZE_PAST = "pageSizePast";
    private static final String FUTURE_TRIPS = "futureTrips";
    private static final String PAST_TRIPS = "pastTrips";
    private static final String EMPTY_STRING = "";
    private static final String NULL = "null";
    private static final String DESTINATION_OWNERSHIP_CHANGED = "Destination ownership changed";
    private static final String DESTINATION_OWNERSHIP_NO_CHANGE = "Destination ownership doesn't need to be changed";
    private static final int MAX_PAGE_SIZE = 100;
    private static final int MINIMUM_TRIP_DESTINATIONS = 2;
    private static final int DEFAULT_ADMIN_ID = 1;

    private TripRepository tripRepository;
    private ProfileRepository profileRepository;
    private DestinationRepository destinationRepository;
    private AchievementTrackerController achievementTrackerController;
    private ObjectMapper objectMapper;


    @Inject
    public TripController(TripRepository tripRepository,
                          ProfileRepository profileRepository,
                          DestinationRepository destinationRepository,
                          AchievementTrackerController achievementTrackerController,
                          ObjectMapper objectMapper) {
        this.tripRepository = tripRepository;
        this.profileRepository = profileRepository;
        this.destinationRepository = destinationRepository;
        this.achievementTrackerController = achievementTrackerController;
        this.objectMapper = objectMapper;
    }


    /**
     * Creates a trips for a user based on information sent in the Http Request body. A trip will have a trip name,
     * and at least two destinations.
     *
     * @param request           Http Request containing Json Body.
     * @param affectedUserId    The user id of the user who will own the new trip.
     * @return                  created() (Http 201) response for successful trip creation.
     *                          badRequest() (Http 400) if the given request body is invalid.
     *                          forbidden() (Http 403) if the user doesn't have the permissions to achieve this action.
     *                          unauthorized() (Http 401) if the user is not logged in.
     */
    public Result create(Http.Request request, Long affectedUserId) {
        Profile loggedInUser = AuthenticationUtil.validateAuthentication(profileRepository, request);
        if (loggedInUser == null) {
            return unauthorized(ApiError.unauthorized());
        }

        Profile affectedProfile = profileRepository.findById(affectedUserId);

        if (affectedProfile == null) {
            return badRequest(ApiError.badRequest(Errors.PROFILE_NOT_FOUND));
        }

        // If user is admin, or if they are editing their own profile then allow them to edit.
        if (!AuthenticationUtil.validUser(loggedInUser, affectedProfile)) {
            return forbidden(ApiError.forbidden());
        }

        JsonNode json = request.body().asJson();

        if (!isValidTrip(json)) {
            return badRequest(ApiError.invalidJson());
        }

        // Create a trip object and give it the name extracted from the request.
        Trip trip = new Trip();
        trip.setName(json.get(NAME).asText());

        // Create a json node for the destinations contained in the trip to use for iteration.
        ArrayNode tripDestinations = (ArrayNode) json.get(TRIP_DESTINATIONS);

        // Create an empty List for TripDestination objects to be populated from the request.
        List<TripDestination> destinationList = parseTripDestinations(tripDestinations);

        // Set the trip destinations to be the array of TripDestination parsed, save the trip, and return 201.
        if (!destinationList.isEmpty() && isValidDateOrder(destinationList)) {
            trip.setDestinations(destinationList);
            affectedProfile.addTrip(trip);
            profileRepository.save(affectedProfile);
            for (TripDestination tripDestination: destinationList) {
                determineDestinationOwnershipTransfer(affectedProfile, tripDestination);
            }
            tripRepository.save(trip);

            ObjectNode returnJson = objectMapper.createObjectNode();
            returnJson.set(REWARD, achievementTrackerController.rewardTripCreate(affectedProfile));
            returnJson.put(NEW_TRIP_ID, trip.getId());

            return created(returnJson);
        } else {
            return badRequest(ApiError.invalidJson());
        }
    }


    /**
     * Method for looking at the contents of the main Json body for a trip in a request.
     * NOTE: Does not examine array contents.
     *
     * @param json      the Json body of a request received.
     * @return          false if Json doesn't contain a name or an array of destinations with at least two nodes, else
     *                  returns true.
     */
    private boolean isValidTrip(JsonNode json) {

        // Check if the request contains a trip name and an array of destinations.
        if (!(json.has(NAME) && json.has(TRIP_DESTINATIONS))) {
            return false;
        }

        // Check that the trip name is not empty
        if (json.get(NAME).asText().length() <= 0) {
            return false;
        }

        // Check if the array of destinations in the request contains at least two destinations.
        return (json.get(TRIP_DESTINATIONS).size() >= MINIMUM_TRIP_DESTINATIONS);
    }


    /**
     * Updates a single trip for selected user's profile.
     *
     * @param request   Http Request containing Json Body of the selected trip to modify.
     * @param tripId    the id of the trip being modified.
     * @return          ok() (Http 200) if the trip has been successfully modified.
     *                  badRequest() (Http 400) If the trip is not valid.
     *                  unauthorized() (Http 401) If the user is not logged in.
     */
    public Result edit(Http.Request request, Long tripId) {
        Profile loggedInUser = AuthenticationUtil.validateAuthentication(profileRepository, request);
        if (loggedInUser == null) {
            return unauthorized(ApiError.unauthorized());
        }

        // Retrieve the individual trip being deleted by its id.
        Trip trip = tripRepository.findById(tripId);

        if (trip == null) {
            return notFound(ApiError.notFound(Errors.TRIP_NOT_FOUND));
        }

        // Retrieve the profile having its trip removed from the trip id.
        Long ownerId = tripRepository.fetchTripOwner(tripId);
        if (ownerId == null) {
            return badRequest(ApiError.badRequest(Errors.PROFILE_NOT_FOUND));
        }

        Profile tripOwner = profileRepository.findById(ownerId);

        if (!AuthenticationUtil.validUser(loggedInUser, tripOwner)) {
            return forbidden(ApiError.forbidden());
        }

        JsonNode json = request.body().asJson();

        if (!isValidTrip(json)) {
            return badRequest(ApiError.invalidJson());
        }


        String name = json.get(NAME).asText();
        trip.setName(name);

        tripRepository.removeTripDestinations(trip);

        // Create a json node for the destinations contained in the trip to use for iteration.
        ArrayNode tripDestinations = (ArrayNode) json.get(TRIP_DESTINATIONS);

        // Create an empty List for TripDestination objects to be populated from the request.
        List<TripDestination> destinationList = parseTripDestinations(tripDestinations);

        if (!destinationList.isEmpty() && isValidDateOrder(destinationList)) {
            tripRepository.updateTrip(tripOwner, trip, destinationList);
            return ok(Json.toJson(trip));
        } else {
            return badRequest(ApiError.invalidJson());
        }
    }


    /**
     * Parse the ArrayNode from a valid request's Json body to create a list of TripDestination objects
     * This method is used when creating a trip and when editing a trip.
     *
     * @param tripDestinations      the array of trip destinations.
     * @return                      an array of destinations.
     */
    private List<TripDestination> parseTripDestinations(ArrayNode tripDestinations) {

        List<TripDestination> result = new ArrayList<>();
        List<TripDestination> badResult = new ArrayList<>();

        // Simple integer for incrementing the list_order attribute for trip destinations.
        int order = 0;
        long previousDestination = -1;

        // Parse JSON to create and append trip destinations using an iterator.
        Iterator<JsonNode> iterator = tripDestinations.elements();
        while (iterator.hasNext()) {
            // Set the current node having its contents extracted.
            JsonNode destinationJson = iterator.next();
            Long id = destinationJson.get(DESTINATION_ID).asLong();

            // Check if current node has a destination ID, and it corresponds with a destination in our database.
            if (destinationJson.get(DESTINATION_ID) != null
                    && destinationJson.get(DESTINATION_ID).asLong() != previousDestination
                    && destinationRepository.findById(id) != null
            ) {
                // Checks the dates are done correctly
                if (!isValidDates(destinationJson.get(START_DATE).asText(), destinationJson.get(END_DATE).asText())) {
                    return badResult;
                }
                // Parse the values contained in the current node of the array
                Long parsedDestinationId = destinationJson.get(DESTINATION_ID).asLong();

                LocalDate parsedStartDate = parseDestinationDates(destinationJson, START_DATE);
                LocalDate parsedEndDate = parseDestinationDates(destinationJson, END_DATE);

                Destination parsedDestination = destinationRepository.findById(parsedDestinationId);

                // Create a new TripDestination object and set the values to be those parsed.
                TripDestination newTripDestination = new TripDestination();
                newTripDestination.setDestination(parsedDestination);
                newTripDestination.setStartDate(parsedStartDate);
                newTripDestination.setEndDate(parsedEndDate);
                newTripDestination.setListOrder(order++);

                // Add created destination to the list of trip destinations.
                result.add(newTripDestination);
                previousDestination = id;
            } else {
                return badResult;
            }
        }
        return result;
    }


    /**
     * Checks if each date for a destination in a quest is valid. If valid parses it into a LocalDate object.
     *
     * @param destinationJson   the Json object containing the destination dates.
     * @param field             the specified field, either start date or end date, that will be retrieved from the
     *                          destinationJson.
     * @return                  a LocalDate variable containing the requested date field, if none found returns null.
     */
    private LocalDate parseDestinationDates(JsonNode destinationJson, String field) {
        if (!(destinationJson.get(field).asText().equals(NULL)
                || destinationJson.get(field).asText().equals(EMPTY_STRING))) {
           return LocalDate.parse(destinationJson.get(field).asText());
        }
        return null;
    }


    /**
     * Checks the start and end dates to make sure that the start date does not happen after the end date but if either
     * is null this does not apply.
     *
     * @param startDate     starting date as string.
     * @param endDate       ending date as string.
     * @return              true if the dates are valid (blank, null, or start date occurs before or at the same time as
     *                      end date), otherwise returns false.
     */
    private boolean isValidDates(String startDate, String endDate) {
        if (startDate.equals(EMPTY_STRING) || startDate.equals("null")) {
            return true;
        } else if (endDate.equals(EMPTY_STRING) || endDate.equals(NULL)) {
            return true;
        } else {
            return (LocalDate.parse(startDate).isBefore(LocalDate.parse(endDate))
                    || LocalDate.parse(startDate).equals(LocalDate.parse(endDate)));
        }
    }


    /**
     * Adds the given date to the list of dates if it is not null.
     *
     * @param dateToAdd     the date to be added to the list.
     * @param allDates      the list containing all dates.
     */
    private void addDate(LocalDate dateToAdd, List<LocalDate> allDates) {
        if (dateToAdd != null) {
            allDates.add(dateToAdd);
        }
    }


    /**
     * Checks if all of the start/end dates within a trip are in valid order, to be called after saving a reorder.
     *
     * @param tripDestinations  array of all the destinations in the trip in the new order.
     * @return                  true if all the dates of destinations within a trip are in chronological order,
     *                          false otherwise.
     */
    private boolean isValidDateOrder(List<TripDestination> tripDestinations) {
        // Adds all dates within the list of trip destinations to an array if they aren't null
        List<LocalDate> allDates = new ArrayList<LocalDate>() {};
        for (TripDestination tripDestination : tripDestinations) {
            addDate(tripDestination.getStartDate(), allDates);
            addDate(tripDestination.getEndDate(), allDates);
        }

        // Iterate through from item 1 and 2 to n-1 and n. return false if any pair is not in order
        for (int j = 0; j < (allDates.size() - 1); j++) {
            boolean nullDates = allDates.get(j) == null || allDates.get(j + 1) == null;
            boolean invalidOrder = allDates.get(j).isAfter(allDates.get(j + 1));
            boolean areEqual = allDates.get(j).equals(allDates.get(j + 1));
            if (!nullDates && invalidOrder && !areEqual) {
                return false;
            }
        }
        return true;
    }


    /**
     * Determines if any of the destinations in a trip need to have their ownership changed. This is if the destination
     * is public, not owned by the global admin and the affected profile is not the owner of the public destination.
     *
     * @param affectedProfile   the profile that is having the trip added to.
     * @param tripDestination   the destination that is stored in the trip.
     * @return ok               with the message of if the destination ownerships needs to change or not.
     */
    private Result determineDestinationOwnershipTransfer(Profile affectedProfile, TripDestination tripDestination) {
        Destination destination = tripDestination.getDestination();
        Profile owner = destination.getOwner();

        // Destination is not owned by global admin, it is public, and the user is not the owner of the destination.
        if (owner == null || owner.getId() != DEFAULT_ADMIN_ID && destination.getPublic()
                && !affectedProfile.getId().equals(owner.getId())) {
            destinationRepository.transferToAdmin(destination);
            return ok(DESTINATION_OWNERSHIP_CHANGED);
        }

        return ok(DESTINATION_OWNERSHIP_NO_CHANGE);
    }


    /**
     * Fetches all the trips for a specified user, using pagination for retrieving trips based on the requested page.
     *
     * @param request   the Http request containing the relevant authentication values.
     * @param id        the id of the user requested.
     * @return          unauthorized() (Http 401) if the user is not logged in.
     *                  ok() (Http 200) containing the list of trips as a Json.
     */
    public Result fetchAllTrips(Http.Request request, Long id) {
        Profile loggedInUser = AuthenticationUtil.validateAuthentication(profileRepository, request);
        if (loggedInUser == null) {
            return unauthorized(ApiError.unauthorized());
        }

        Profile owner = profileRepository.findById(id);
        if (owner == null) {
            return badRequest(ApiError.badRequest(Errors.PROFILE_NOT_FOUND));
        }

        int pageNumberFuture = 0;
        int pageNumberPast = 0;

        if (request.getQueryString(PAGE_FUTURE) != null && !request.getQueryString(PAGE_FUTURE).isEmpty()) {
            pageNumberFuture = Integer.parseInt(request.getQueryString(PAGE_FUTURE));
        }

        if (request.getQueryString(PAGE_PAST) != null && !request.getQueryString(PAGE_PAST).isEmpty()) {
            pageNumberPast = Integer.parseInt(request.getQueryString(PAGE_PAST));
        }

        Integer pageSizeFuture = determinePageSize(request, PAGE_SIZE_FUTURE);
        Integer pageSizePast = determinePageSize(request, PAGE_SIZE_PAST);

        if (pageSizeFuture == null || pageSizePast == null) {
            return badRequest(ApiError.badRequest(Errors.INVALID_PAGE_SIZE_REQUESTED));
        }

        List<Trip> futureTrips = tripRepository.fetchFuture(owner, pageSizeFuture, pageNumberFuture);
        List<Trip> pastTrips = tripRepository.fetchPast(owner, pageSizePast, pageNumberPast);

        ObjectNode returnJson = objectMapper.createObjectNode();

        returnJson.set(FUTURE_TRIPS, Json.toJson(futureTrips));
        returnJson.set(PAST_TRIPS, Json.toJson(pastTrips));

        return ok(returnJson);
    }


    /**
     * Determines the page size from the given query string inside the request. Is used to calculate the future and past
     * trip pages.
     *
     * @param request           the Http request containing the query string.
     * @param pageSizeRequested the page being requested, whether it be future or past page size.
     * @return                  null if the requested page cannot be passed as an integer.
     *                          otherwise returns the requested page size.
     */
    private Integer determinePageSize(Http.Request request, String pageSizeRequested) {
        int pageSize = MAX_PAGE_SIZE;
        if (request.getQueryString(pageSizeRequested) != null && !request.getQueryString(pageSizeRequested).isEmpty()) {
            try {
                pageSize = Integer.parseInt(request.getQueryString(pageSizeRequested));
                // Restrict the page size to be no larger than the maximum page size.
                pageSize = Math.min(pageSize, MAX_PAGE_SIZE);
            } catch (NumberFormatException e) {
                return null;
            }
        }
        return pageSize;
    }


    /**
     * Retrieves the total number of trips the user has. This is so the frontend can determine the appropriate
     * pagination for the trip tables.
     *
     * @param request   the Http request containing the relevant authentication values.
     * @param id        the id of the user requested.
     * @return          unauthorized() (Http 401) if the user is not logged in.
     *                  ok() (Http 200) total number of trip the specified user has.
     */
    public Result getTotalNumberOfTrips(Http.Request request, Long id) {
        Profile loggedInUser = AuthenticationUtil.validateAuthentication(profileRepository, request);
        if (loggedInUser == null) {
            return unauthorized(ApiError.unauthorized());
        }

        int totalNumberOfFutureTrips = tripRepository.fetchAllFutureTripsCount(id);
        int totalNumberOfPastTrips = tripRepository.fetchAllPastTripsCount(id);

        ObjectNode returnJson = objectMapper.createObjectNode();

        returnJson.set(FUTURE_TRIPS, Json.toJson(totalNumberOfFutureTrips));
        returnJson.set(PAST_TRIPS, Json.toJson(totalNumberOfPastTrips));

        return ok(returnJson);
    }


    /**
     * Deletes a trip from the user currently logged in.
     *
     * @param request   Http request from the client, from which the current user profile can be obtained.
     * @param tripId    the id of the trip being deleted from a profile.
     * @return          If no profile or no trip is found, returns notFound() (Http 404).
     *                  If the trip id is not associated with any profile, returns badRequest() (Http 400).
     *                  If the user is not logged in, returns unauthorized() (Http 401).
     *                  If the user is not the trip owner or an admin, returns unauthorized() (Http 401).
     *                  Otherwise, if trip is successfully deleted, returns ok() (Http 200).
     */
    public Result destroy(Http.Request request, Long tripId) {
        Profile loggedInUser = AuthenticationUtil.validateAuthentication(profileRepository, request);
        if (loggedInUser == null) {
            return unauthorized(ApiError.unauthorized());
        }

        // Retrieve the individual trip being deleted by its id.
        Trip trip = tripRepository.findById(tripId);

        if (trip == null) {
            return notFound(ApiError.notFound(Errors.TRIP_NOT_FOUND));
        }

        // Retrieve the profile having its trip removed from the trip id.
        Long ownerId = tripRepository.fetchTripOwner(tripId);
        if (ownerId == null) {
            return badRequest(ApiError.badRequest(Errors.PROFILE_NOT_FOUND));
        }
        Profile tripOwner = profileRepository.findById(ownerId);

        if (tripOwner == null) {
            return badRequest(ApiError.badRequest(Errors.PROFILE_NOT_FOUND));
        }

        if (!AuthenticationUtil.validUser(loggedInUser, tripOwner)) {
            return forbidden(ApiError.forbidden());
        }

        // Repository method handling the database and object manipulation.
        tripRepository.deleteTripFromProfile(tripOwner, trip);
        // Deletion successful.
        return ok(Json.toJson(trip));
    }
}
