<template>
    <div>
        <table ref="gallery" class="mt-3">
            <!--Table containing the rows of photos to be displayed-->
            <tr v-for="rowNumber in (numberOfRows)">
                <td v-for="photo in getRowPhotos(rowNumber)">
                    <b-container class="p-1" :class="{colorBlue: selected(photo)}">
                        <b-img :src="getThumbImage(photo.id)" @click="$emit('photo-click', photo)"
                               onerror="this.onerror = null; this.src='../../../static/default_image.png'"
                               alt="Image not Found" thumbnail>
                        </b-img>
                    </b-container>
                    <b-select @change="$emit('privacy-update', photo)" class="w-100"
                              :disabled="userProfile.profilePicture !== null
                               && userProfile.profilePicture.id === photo.id"
                              v-if="showDropdown"
                              v-model="photo.public"
                              :class="{colorBlue: userProfile.profilePicture === null
                              || (userProfile.profilePicture.id !== photo.id),
                              colorDisabled: (userProfile.profilePicture !== null
                              && userProfile.profilePicture.id === photo.id)}">
                        <option value="true">
                            Public
                        </option>
                        <option value="false">
                            Private
                        </option>
                    </b-select>
                </td>
            </tr>
        </table>
        <div class="d-flex justify-content-center mb-3">
            <b-img alt="Loading" class="mt-3 align-middle loading" v-if="retrievingPhotos" :src="assets['loadingLogo']">
            </b-img>
            <p v-if="!photos.length && !retrievingPhotos"><b>No photos found.</b></p>
        </div>
        <div class="d-flex justify-content-center w-100 mt-1">
            <b-pagination
                    :per-page="perPage"
                    :total-rows="rows"
                    ref="navigationGallery"
                    size="sm"
                    first-text="First"
                    last-text="Last"
                    v-model="currentPage"
            ></b-pagination>
        </div>
    </div>
</template>

<script>
    export default {
        name: "photoTable",

        props: {
            photos: Array,
            profile: Object,
            userProfile: Object,
            numberOfRows: {
                default: function () {
                    return 3
                }
            },
            numberOfColumns: {
                default: function () {
                    return 6
                }
            },
            selectedImages: {
                default: function () {
                    return []
                }
            },
            adminView: {
                default: function() {
                    return false;
                }
            },
            retrievingPhotos: Boolean
        },

        data: function () {
            return {
                currentPage: 1,
                auth: false,
                publicDestinationPhotos: []
            }
        },

        watch: {
            profile() {
                this.checkAuth();
            }
        },

        computed: {
            /**
             * The total number of photos. Used for the table pagination.
             */
            rows() {
                return this.photos.length;
            },


            /**
             * How many photos to display on the table. Used for pagination.
             */
            perPage() {
                return this.numberOfRows * this.numberOfColumns;
            },


            /**
             * Determines if the dropdown for photo privacy should be displayed.
             */
            showDropdown() {
                return this.auth;
            }
        },

        mounted() {
            this.checkAuth()
        },

        methods: {
            /**
             * Calculates the positions of photos within a gallery grid row.
             *
             * @param rowNumber     the row currently having photos positioned within it.
             */
            getRowPhotos(rowNumber) {
                let numberOfPhotos = (this.photos.length);
                let endRowIndex = ((rowNumber * this.numberOfColumns) + ((this.currentPage - 1) * this.perPage));
                let startRowIndex = (rowNumber - 1) * this.numberOfColumns + ((this.currentPage - 1) * this.perPage);

                // Check preventing an IndexOutOfRangeError, before filling the row with photos indexed from the list.
                if (endRowIndex > numberOfPhotos) {
                    return this.photos.slice(startRowIndex);
                } else {
                    return this.photos.slice(startRowIndex, endRowIndex);
                }
            },


            /**
             * Sends a GET request to get a thumbnail image from the backend.
             */
            getThumbImage(id) {
                return 'v1/photos/thumb/' + id;
            },


            /**
             * Checks the authorization of the user profile that is logged in to see if they can.
             * view the users private photos and can add or delete images from the media.
             */
            checkAuth() {
                this.auth = (this.userProfile.id === this.profile.id || this.adminView);
            },


            /**
             * Determines if a photo is selected or not.
             *
             * @param photo         the photo to be checked if selected.
             * @returns {boolean}   true if the photo is selected, false otherwise.
             */
            selected(photo) {
                for(var i = 0; i < this.selectedImages.length; i += 1) {
                    if(this.selectedImages[i].id === photo.id) {
                        return true;
                    }
                }
                return false;
            }
        }
    }
</script>

<style scoped>
    .colorBlue {
        color: white;
        font-weight: bold;
        background-color: #85BCE5;
    }

    .colorDisabled {
        background-color: #dddddd;
    }
</style>