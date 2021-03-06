package repositories.destinations;

import io.ebean.BeanRepository;
import io.ebean.Ebean;
import io.ebean.ExpressionList;
import models.destinations.Destination;
import models.photos.PersonalPhoto;
import repositories.profiles.ProfileRepository;

import com.google.inject.Inject;
import java.util.List;


/**
 * Handles database interaction for destinations.
 * Extends the BeanRepository containing all CRUD methods.
 */
public class DestinationRepository extends BeanRepository<Long, Destination> {

    private static final Long DEFAULT_ADMIN_ID = 1L;
    private static final String PHOTO_FIELD = "photoGallery.photo";
    private static final String PROPOSED_TRAVELLER_TYPES_ADD = "proposedTravellerTypesAdd";
    private static final String PROPOSED_TRAVELLER_TYPES_REMOVE = "proposedTravellerTypesRemove";
    private static final String NAME = "name";
    private static final String TYPE = "type";
    private static final String DISTRICT = "district";
    private static final String LATITUDE = "latitude";
    private static final String LONGITUDE = "longitude";
    private static final String COUNTRY = "country";
    private static final String ID = "id";
    private static final String IS_PUBLIC = "is_public";
    private static final String OWNER = "owner";


    private ProfileRepository profileRepository;

    @Inject
    public DestinationRepository(ProfileRepository profileRepository) {
        super(Destination.class, Ebean.getDefaultServer());
        this.profileRepository = profileRepository;
    }


    /**
     * Finds all the destinations that contain a given photo.
     *
     * @param photo       the photo.
     * @return            list of destinations containing the photo.
     */
    public List<Destination> fetch(PersonalPhoto photo) {
        return query().where().eq(PHOTO_FIELD, photo).findList();
    }


    /**
     * Finds all the destinations that have proposed traveller types.
     *
     * @return      list of destinations that have proposed traveller types.
     */
    public List<Destination> fetchProposed() {
        return query()
            .where()
            .disjunction()
            .isNotEmpty(PROPOSED_TRAVELLER_TYPES_ADD)
            .isNotEmpty(PROPOSED_TRAVELLER_TYPES_REMOVE)
            .endJunction()
            .findList();
    }


    /**
     * Deletes the destination specified.
     *
     * @param destination       the destination to delete from the database.
     */
    @Override
    public boolean delete(Destination destination) {
        // Clear the destination photos
        destination.clearAllTravellerTypeSets();
        destination.clearPhotoGallery();
        super.update(destination);
        // Delete destination
        return super.delete(destination);
    }


    /**
     * Transfers the ownership of a destination to the default admin. Will be used when a public destination is used by
     * another user.
     *
     * @param destination   the destination to be changed ownership of.
     */
    public void transferToAdmin(Destination destination) {
        destination.changeOwner(profileRepository.findById(DEFAULT_ADMIN_ID));
        super.update(destination);
    }


    /**
     * Returns a list of Destinations that are equal, excluding the given Destination.
     *
     * @param destination   destination to search with.
     * @return              list of destinations that are equal.
     */
    public List<Destination> findEqual(Destination destination) {
        return query()
                .where()
                .eq(NAME, destination.getName())
                .eq(TYPE, destination.getType())
                .eq(DISTRICT, destination.getDistrict())
                .eq(LATITUDE, destination.getLatitude())
                .eq(LONGITUDE, destination.getLongitude())
                .eq(COUNTRY, destination.getCountry())
                .ne(ID, destination.getId())
                .findList();
    }


    /**
     * Determines if there are any destinations that match the specified destination. However, unlike the findEqual()
     * method above, any destination found must either be private to the user or a public destination.
     *
     * @param destination   the destination to be checked for equal destinations.
     * @return              a list of destinations that are equal.
     */
    public List<Destination> findEqualFromAvailable(Destination destination) {
        return query()
                .where()
                .eq(NAME, destination.getName())
                .eq(TYPE, destination.getType())
                .eq(DISTRICT, destination.getDistrict())
                .eq(LATITUDE, destination.getLatitude())
                .eq(LONGITUDE, destination.getLongitude())
                .eq(COUNTRY, destination.getCountry())
                .disjunction()
                    .eq(IS_PUBLIC, true)
                    .conjunction()
                        .eq(IS_PUBLIC, false)
                        .eq(OWNER, destination.getOwner())
                    .endJunction()
                .endJunction()
                .ne(ID, destination.getId())
                .findList();
    }

    public ExpressionList<Destination> getExpressionList() {
        return query().where();
    }
}
