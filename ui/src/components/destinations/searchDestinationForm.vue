<template>
    <div>
        <h4 class="page-title" v-if="searchPublic">Search Public Destinations
            <b-img id="public_search_info" height="25%" :src="assets['information']"></b-img></h4>
        <h4 class="page-title" v-else>Search Your Destinations
            <b-img id="your_search_info" height="25%" :src="assets['information']"></b-img></h4>

        <!-- Info tooltip for public search -->
        <b-tooltip target="public_search_info" title="" placement="bottom">
            <strong><i>How to Search Destinations</i></strong> <br><br>

            Enter as many search parameters as you like and click the Search button. <br>
            Note: if you would like to view all destinations, click Search without any parameters.<br><br>

            To load more destinations, click the Load More button.
        </b-tooltip>

        <!-- Info tooltip for your search -->
        <b-tooltip target="your_search_info" title="" placement="bottom">
            <strong><i>How to Search Destinations</i></strong> <br><br>

            Enter as many search parameters as you like and click the Search button. <br>
            Note: if you would like to view all destinations, click Search without any parameters.<br><br>

            To load more destinations, click the Load More button.
        </b-tooltip>

        <b-form @submit.prevent="searchDestinations">
            <!--Input fields for searching for destinations-->
            <b-form-group
                    id="name-field"
                    label="Destination Name:"
                    label-for="name">
                <b-form-input id="name"
                              v-model="searchName"
                              :state="destinationNameValidation"
                              maxlength="200">
                </b-form-input>
            </b-form-group>

            <b-form-group
                    id="type-field"
                    label="Destination Type:"
                    label-for="type">
                <!--Dropdown field for destination types-->
                <b-form-select id="type" trim v-model="searchType">
                    <template slot="first">
                        <option value="">-- Any --</option>
                    </template>
                    <option :value="destination.id" v-for="destination in destinationTypes"
                            :state="destinationTypeValidation">
                        {{destination.destinationType}}
                    </option>
                </b-form-select>
            </b-form-group>

            <b-form-group
                    id="district-field"
                    label="District:"
                    label-for="district">
                <b-form-input id="district"
                              trim
                              v-model="searchDistrict"
                              :state="destinationDistrictValidation">
                </b-form-input>
            </b-form-group>

            <b-form-group
                    id="latitude-field"
                    label="Latitude:"
                    label-for="latitude">
                <b-form-input id="latitude"
                              trim
                              v-model="searchLatitude"
                              :state="destinationLatitudeValidation">
                </b-form-input>
                <b-form-invalid-feedback :state="destinationLatitudeValidation">
                    {{latitudeErrorMessage}}
                </b-form-invalid-feedback>

            </b-form-group>

            <b-form-group
                    id="longitude-field"
                    label="Longitude:"
                    label-for="longitude">
                <b-form-input id="longitude"
                              trim
                              v-model="searchLongitude"
                              :state="destinationLongitudeValidation">
                </b-form-input>
                <b-form-invalid-feedback :state="destinationLongitudeValidation">
                    {{longitudeErrorMessage}}
                </b-form-invalid-feedback>

            </b-form-group>

            <b-form-group
                    id="country-field"
                    label="Country:"
                    label-for="country">
                <!--Dropdown field for country types-->
                <b-form-select id="country" trim v-model="searchCountry">
                    <template slot="first">
                        <option value="">-- Any --</option>
                    </template>
                    <option :value="country.name" v-for="country in countryList"
                            :state="destinationCountryValidation">
                        {{country.name}}
                    </option>
                </b-form-select>
            </b-form-group>

            <b-button @click="searchDestinations" block variant="primary" type="submit">Search</b-button>
        </b-form>
    </div>
</template>

<script>
    import SingleDestination from "../destinations/singleDestination";

    export default {
        name: "searchDestinationForm.vue",

        props: {
            searchPublic: Boolean,
            profile: Object,
            userProfile: {
                default: function () {
                    return this.profile
                }
            },
        },

        mounted() {
            this.getCountries();
            this.getDestinationTypes();
        },

        data() {
            return {
                sortBy: 'name',
                sortDesc: false,
                searchName: "",
                destinations: [],
                searchType: "",
                searchDistrict: "",
                searchLatitude: "",
                searchLongitude: "",
                searchCountry: "",
                optionViews: [
                    {value: 1, text: "1"},
                    {value: 5, text: "5"},
                    {value: 10, text: "10"},
                    {value: 15, text: "15"}
                ],
                perPage: 10,
                currentPage: 1,
                fields: [
                    {key: 'name', value: 'name', sortable: true},
                    {key: 'type.destinationType', label: 'Type', sortable: true},
                    {key: 'district', value: 'district', sortable: true},
                    'latitude',
                    'longitude',
                    {key: 'country', value: 'country', sortable: true}
                ],
                searchDestination: "",
                retrievingDestinations: false,
                longitudeErrorMessage: "",
                latitudeErrorMessage: "",
                countryList: [],
                destinationTypes: []
            }
        },

        computed: {
            /**
             * @returns {number} number of rows to be displayed based on number of destinations present.
             */
            rows() {
                return this.destinations.length
            },
            /**
             * Validates the input fields based on regex.
             *
             * @returns {*} true if input is valid.
             */
            destinationNameValidation() {
                if (this.searchName.length === 0) {
                    return null;
                }
                return this.searchName.length > 0;
            },
            destinationTypeValidation() {
                if (this.searchType === null) {
                    return null;
                }
                return this.searchType.length > 0 || this.searchType !== null;
            },
            destinationDistrictValidation() {
                if (this.searchDistrict.length === 0) {
                    return null;
                }
                return this.searchDistrict.length > 0;
            },
            destinationLatitudeValidation() {
                if (this.searchLatitude.length === 0) {
                    return null;
                }
                if (isNaN(this.searchLatitude)) {
                    this.latitudeErrorMessage = "Latitude: '" + this.searchLatitude + "' is not a number!";
                    return false;
                } else if (this.searchLatitude > 90 || this.searchLatitude < -90) {
                    this.latitudeErrorMessage = "Latitude: '" + this.searchLatitude + "' must be between " +
                        "-90 and 90";
                    return false;
                }
                return true;
            },
            destinationLongitudeValidation() {
                if (this.searchLongitude.length === 0) {
                    return null;
                }
                if (isNaN(this.searchLongitude)) {
                    this.longitudeErrorMessage = "Longitude: '" + this.searchLongitude + "' is not a number!";
                    return false;
                } else if (this.searchLongitude > 180 || this.searchLongitude < -180) {
                    this.longitudeErrorMessage = "Longitude: '" + this.searchLongitude + "' must be between " +
                        "-180 and 180";
                    return false;
                }
                return true;
            },
            destinationCountryValidation() {
                if (this.searchCountry === null) {
                    return null;
                }
                return this.searchCountry.length > 0 || this.searchCountry !== null;
            }
        },

        methods: {
            /**
             * Retrieves the different destination types from the backend.
             */
            getDestinationTypes() {
                let self = this;
                fetch(`/v1/destinationTypes`, {
                    accept: "application/json"
                }).then(function (response) {
                    if (!response.ok) {
                        throw response;
                    } else {
                        return response.json();
                    }
                }).then(function (responseBody) {
                    self.destinationTypes = responseBody;
                }).catch(function (response) {
                    self.handleErrorResponse(response);
                });
            },


            /**
             * Sets values for search.
             */
            searchDestinations() {
                if (this.validateFields(this.destinationNameValidation)
                    && this.validateFields(this.destinationTypeValidation)
                    && this.validateFields(this.destinationDistrictValidation)
                    && this.validateFields(this.destinationLatitudeValidation)
                    && this.validateFields(this.destinationLongitudeValidation)
                    && this.validateFields(this.destinationCountryValidation)) {
                    this.$emit('searched-destination', {
                        name: this.searchName,
                        type: this.searchType,
                        district: this.searchDistrict,
                        latitude: this.searchLatitude,
                        longitude: this.searchLongitude,
                        country: this.searchCountry
                    });
                }

            },


            /**
             * Sets the countries list to the list of countries from the country api.
             */
            getCountries() {
                let self = this;
                fetch("https://restcountries.eu/rest/v2/all", {})
                .then(function (response) {
                    if (!response.ok) {
                        throw response;
                    } else {
                        return response.json();
                    }
                }).then(function (responseBody) {
                    self.countryList = responseBody;
                }).catch(function (response) {
                    self.handleErrorResponse(response);
                });
            },


            /**
             * Checks each of the validation fields to ensure they are return either null (no value is given), or the
             * field is valid.
             *
             * @returns {boolean} true if the fields are valid.
             */
            validateFields(validationField) {
                if (validationField === null || validationField === true) {
                    return true;
                }
            }
        },

        components: {
            SingleDestination
        }
    }
</script>