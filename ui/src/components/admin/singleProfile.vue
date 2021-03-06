<template>
    <div>
        <b-row>
            <b-col cols="1.5">
                <!-- Nav bar for admin to display the selected page they wish to work on. -->
                <b-navbar toggleable="lg" class="stickyMinorNav">
                    <b-collapse id="nav-collapse-admin" is-nav>
                        <b-nav class="p-1 bg-white" vertical>
                            <b-nav-item @click="goBack">Go Back</b-nav-item>
                            <b-navbar-brand @click="currentDisplay = 0" class="nav-bar-brand">
                                <b-img :src="getProfilePictureThumbnail()"
                                       onerror="this.onerror= null; this.src = '../../../static/default_profile_picture.png'"
                                       fluid rounded="circle" width="50%">
                                </b-img>
                                {{editProfile.firstName}}
                            </b-navbar-brand>
                            <b-nav-item @click="currentDisplay = 1" :class="{boldText: currentDisplay === 1}">Edit
                                Profile
                            </b-nav-item>
                            <b-nav-item @click="currentDisplay = 2" :class="{boldText: currentDisplay === 2}">View
                                Trips
                            </b-nav-item>
                            <b-nav-item @click="currentDisplay = 3" :class="{boldText: currentDisplay === 3}">Add
                                Trips
                            </b-nav-item>
                            <b-nav-item @click="currentDisplay = 4" :class="{boldText: currentDisplay === 4}">
                                Destinations
                            </b-nav-item>
                            <b-nav-item @click="currentDisplay = 5" :class="{boldText: currentDisplay === 5}">
                                Objectives
                            </b-nav-item>
                            <b-nav-item @click="currentDisplay = 6" :class="{boldText: currentDisplay === 6}">Quests
                            </b-nav-item>
                        </b-nav>
                    </b-collapse>
                    <b-navbar-toggle target="nav-collapse-admin"></b-navbar-toggle>
                </b-navbar>

            </b-col>

            <b-col>
                <view-profile
                        :adminView="adminView"
                        :destinations="destinations"
                        :nationalityOptions="nationalityOptions"
                        :profile="editProfile"
                        :userProfile="profile"
                        :showSaved="showSaved"
                        :travTypeOptions="travTypeOptions"
                        v-if="currentDisplay === 0">
                </view-profile>
                <edit-profile
                        :adminView="adminView"
                        :nationalityOptions="nationalityOptions"
                        :profile="editProfile"
                        :travTypeOptions="travTypeOptions"
                        @profile-saved="redirectToViewProfile"
                        v-if="currentDisplay === 1">
                </edit-profile>
                <your-trips
                        :admin-view="adminView"
                        :destinations="destinations"
                        :profile="editProfile"
                        :userProfile="editProfile"
                        v-if="currentDisplay === 2">
                </your-trips>
                <plan-a-trip
                        :adminView="adminView"
                        :destinations="destinations"
                        :heading="'Plan a Trip'"
                        :profile="editProfile"
                        :subHeading="'Book your next trip!'"
                        v-if="currentDisplay === 3">
                </plan-a-trip>
                <destinations-page
                        :destinationTypes="destinationTypes"
                        :adminView="adminView"
                        :destinations="destinations"
                        :travTypeOptions="travTypeOptions"
                        :profile="editProfile"
                        v-if="currentDisplay === 4">
                </destinations-page>
                <objective-page
                        :adminView="adminView"
                        :profile="editProfile"
                        v-if="currentDisplay === 5">
                </objective-page>
                <quest-page
                        :adminView="adminView"
                        :profile="editProfile"
                        v-if="currentDisplay === 6">
                </quest-page>
            </b-col>
        </b-row>
    </div>
</template>

<script>
    import NavBarMain from '../helperComponents/navbarMain.vue'
    import ViewProfile from "./../dash/viewProfile.vue"
    import PlanATrip from './../trips/planATrip.vue'
    import YourTrips from './../trips/yourTrips.vue'
    import EditProfile from "./../dash/editProfile.vue"
    import DestinationsPage from "./../destinations/destinationsPage.vue"
    import ObjectivePage from "../objectives/objectivePage";
    import QuestPage from "../quests/questPage";

    export default {
        name: "singleProfile",

        props: {
            adminView: Boolean,
            profile: Object,
            editProfile: Object,
            nationalityOptions: Array,
            travTypeOptions: Array,
            destinations: Array,
            destinationTypes: Array
        },

        data() {
            return {
                profileImage: {blank: true, width: 75, height: 75, class: 'm1'},
                currentDisplay: 0,
                showSaved: false,
                refreshDestinations: 0
            }
        },

        methods: {
            /**
             * Emits an event to the admin panel page, this will redirect the admin back to the admin dashboard when the
             * go back button is clicked.
             */
            goBack() {
                this.$emit('go-back', null);
            },


            /**
             * If the profile is successfully saved, then redirect to the view profile page.
             */
            redirectToViewProfile(editProfile) {
                this.editProfile = editProfile;
                this.currentDisplay = 0;
                this.showSaved = true;
            },


            /**
             * Retrieves the user's primary photo thumbnail.
             */
            getProfilePictureThumbnail() {
                if (this.editProfile.profilePicture !== null) {
                    return `/v1/photos/thumb/` + this.editProfile.profilePicture.id;
                }
                return "../../../static/default_profile_picture.png";
            }
        },

        components: {
            QuestPage,
            ObjectivePage,
            ViewProfile,
            PlanATrip,
            YourTrips,
            EditProfile,
            NavBarMain,
            DestinationsPage
        }
    }
</script>

<style scoped>
    @import "../../css/admin.css";
</style>