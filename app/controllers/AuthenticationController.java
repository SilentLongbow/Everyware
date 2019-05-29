package controllers;

import com.fasterxml.jackson.databind.JsonNode;
import models.Profile;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Result;
import javax.xml.bind.DatatypeConverter;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Controller to handle the authorisation of clients.
 * Sets session cookies when user is logging in, handles hashing a user's password and handles logging out.
 */
public class AuthenticationController extends Controller {

    private static final Logger LOGGER = Logger.getLogger( AuthenticationController.class.getName() );
    private static final String USERNAME = "username";
    private static final String PASS_FIELD = "password";
    private static final String AUTHORIZED = "authorized";

    /**
     * Checks if client is authorized, if authorized, return ok() (Http 200) .If not, checks if user exists in the
     * database. If exists, set session and return ok() (Http 200).
     *
     * @param request   Http request from the client.
     * @return          ok() (Http 200) if the user is logged in/successfully logged in, badRequest() (Http 400) if the
     *                  user credentials are invalid, unauthorized() (Http 401) if no profile matching the user's
     *                  credentials can be found.
     */
    public Result login(Http.Request request) {
        return request.session()
                .getOptional(AUTHORIZED)
                .map(userId -> ok("OK, Already logged In")) // User is already logged in, return OK
                .orElseGet(() -> { // orElseGet tries to get the `getOptional` value, otherwise executes the following function

                    // User is not logged in, attempt to search database
                    JsonNode json = request.body().asJson();

                    // Check if a body was given and has required fields
                    if (json == null || (!(json.has(USERNAME) && json.has(PASS_FIELD)))) {
                        // If JSON Object contains no user or pass key, return bad request
                        // Prevents null pointer exceptions when trying to get the values below.
                        return badRequest("Bad User Credentials");
                    }

                    String username = json.get(USERNAME).asText();

                    // Uses the hashProfilePassword() method to hash the given password.
                    String password = null;
                    try {
                        password = hashProfilePassword(json.get(PASS_FIELD).asText());
                    } catch (NoSuchAlgorithmException e) {
                        LOGGER.log(Level.SEVERE, "Invalid JSON: JSON Object contains no user or password key", e);
                    }

                    Profile profile = Profile.find.query().where()
                            .like(USERNAME, username).findOne();

                    if ((profile != null) && (profile.getPassword().equals(password))) {
                        // Profile was successfully fetched and password matches,
                        // Set session token as id and return ok (200 response)
                        return ok("Logged In").addingToSession(request, AUTHORIZED, profile.id.toString());
                    }

                    return unauthorized("Invalid user credentials provided");

                });
    }

    /**
     * Hashes a password string using the SHA 256 method from the MessageDigest library.
     * @param password                  the string that is requested to be hashed.
     * @return                          a string of the hashed binary array as a hexadecimal string.
     * @throws NoSuchAlgorithmException if the algorithm specified does not exist for the MessageDigest library.
     */
    private String hashProfilePassword(String password) throws NoSuchAlgorithmException {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        return DatatypeConverter.printHexBinary(digest.digest(password.getBytes(StandardCharsets.UTF_8)));
    }

    /**
     * Clears session resulting in authorized tag being cleared, therefore de-authorizing client.
     *
     * @return ok() (Http 200) result, as logout should always succeed.
     */
    public Result logout() {
        return ok("OK, Logged out").withNewSession(); // Sets a new session, clearing the old one
    }
}
