<template xmlns:v-slot="http://www.w3.org/1999/XSL/Transform">
    <div>
        <b-row>
            <b-col cols="12" md="8">
                <b-list-group>
                    <!--Successful quest alert -->
                    <b-alert
                            :show="dismissCountDown"
                            @dismiss-count-down="countDownChanged"
                            @dismissed="dismissCountDown=0"
                            dismissible
                            variant="success">
                        <p>{{alertText}}</p>
                        <b-progress
                                :max="dismissSeconds"
                                :value="dismissCountDown - 1"
                                height="4px"
                                variant="success"
                        ></b-progress>
                    </b-alert>
                    <b-list-group-item class="flex-column justify-content-center"
                                       v-if="creatingQuest">
                        <quest-item
                                :selected-objective="selectedObjective"
                                :heading="'Create'"
                                :profile="profile"
                                @successCreate="showSuccess"
                                @cancelCreate="cancelCreate"
                                :selectedDestination="destinationSelected"
                                @OBJ-side-bar="showHideBar => this.showDestinations = showHideBar"
                                @Your-OBJ-side-bar="showHideBar => this.showYourObjectives = showHideBar"
                        ></quest-item>
                    </b-list-group-item>
                    <b-list-group-item class="flex-column justify-content-center"
                                       v-if="editingQuest">
                        <quest-item
                                :selected-objective="selectedObjective"
                                :inputQuest="copiedQuest"
                                :heading="'Edit'"
                                :profile="profile"
                                @successEdit="successEdit"
                                @cancelCreate="cancelEdit"
                                :selectedDestination="destinationSelected"
                                @OBJ-side-bar="showHideBar => this.showDestinations = showHideBar"
                                @Your-OBJ-side-bar="showHideBar => this.showYourObjectives = showHideBar"
                                @add-hint-side-bar="showHintSidebar"
                                @hide-hint-side-bar="showHideBar => this.showHintSideBar = showHideBar"
                        ></quest-item>
                    </b-list-group-item>
                    <div v-if="yourQuests">
                        <b-list-group-item class="flex-column justify-content-center"
                                           v-if="!creatingQuest && !editingQuest">
                            <div class="d-flex justify-content-center">
                                <b-button variant="success" @click="addQuest" block>Add a New Quest</b-button>
                            </div>
                        </b-list-group-item>
                    </div>
                    <b-list-group-item v-for="quest in foundQuests" href="#"
                                       class="flex-column align-items-start"
                                       :key="quest.id"
                                       draggable="false"
                                       v-if="!activeQuests"
                                       @click="selectedQuest = quest">
                        <template v-if="!editingQuest && !(activeId === quest.id) && !creatingQuest">
                            <b-row>
                                <b-col :cols="availableQuests ? 5 : ''">
                                    <div>
                                        <h4>Title</h4>
                                        <p>{{quest.title}}</p>
                                    </div>
                                    <div>
                                        <h4>Start Date</h4>
                                        <p class="mobile-text">{{new Date(quest.startDate)}}</p>
                                    </div>
                                </b-col>
                                <b-col :cols="availableQuests ? 5 : ''">
                                    <div>
                                        <h4>Countries</h4>
                                        <p>{{getQuestCountries(quest)}}</p>
                                    </div>
                                    <div>
                                        <h4>End Date</h4>
                                        <p class="mobile-text">{{new Date(quest.endDate)}}</p>
                                    </div>
                                </b-col>
                                <b-col v-if="availableQuests" md="2" class="align-self-center align-content-center">
                                    <b-button variant="primary" @click="createAttempt(quest, true)" block>
                                        Start Now
                                    </b-button>
                                    <b-button variant="secondary" @click="createAttempt(quest, false)" block>
                                        Start Later
                                    </b-button>
                                </b-col>
                            </b-row>
                            <div v-if="yourQuests" class="buttonMarginsTop">
                                <b-button
                                        @click="showHideLocations(quest)"
                                        variant="primary"
                                        class="buttonMarginsBottom">
                                            Show/Hide Locations
                                </b-button>
                                <b-container fluid style="margin-top: 20px; display: none" :id="'display-' + quest.id">
                                    <!-- Table displaying all quest objectives -->
                                    <b-table :current-page="currentPage" :fields="fields" :items="quest.objectives"
                                             :per-page="perPage"
                                             hover
                                             id="myTrips"
                                             outlined
                                             ref="questObjective"
                                             striped>

                                        <template v-slot:cell(radius)="row">
                                            {{getRadiusValue(row.item.radius)}}
                                        </template>
                                    </b-table>
                                    <!-- Determines pagination and number of results per row of the table -->
                                    <b-row>
                                        <b-col cols="2">
                                            <b-form-group
                                                    id="numItems-field"
                                                    label-for="perPage">
                                                <b-form-select :options="optionViews"
                                                               id="perPage"
                                                               size="sm"
                                                               trim v-model="perPage">
                                                </b-form-select>
                                            </b-form-group>
                                        </b-col>
                                        <b-col>
                                            <b-pagination
                                                    :per-page="perPage"
                                                    :total-rows="rows(quest)"
                                                    align="center"
                                                    aria-controls="my-table"
                                                    first-text="First"
                                                    last-text="Last"
                                                    size="sm"
                                                    v-model="currentPage">
                                            </b-pagination>
                                        </b-col>
                                    </b-row>
                                </b-container>
                            </div>

                            <b-row v-if="yourQuests">
                                <b-col>
                                    <b-button variant="warning" @click="setActiveId(quest)" block>Edit</b-button>
                                </b-col>
                                <b-col>
                                    <b-button variant="danger" @click="setQuest(quest)" block>Delete
                                    </b-button>
                                </b-col>
                            </b-row>
                        </template>
                        <!--Quest component-->
                    </b-list-group-item>
                    <!---Load More--->
                    <b-list-group-item
                            class="flex-column justify-content-center"
                            v-if="!yourQuests && !completedQuests && foundQuests.length">
                        <div class="d-flex justify-content-center" v-if="loadingResults">
                            <b-img alt="Loading" class="align-middle loading" :src="assets['loadingLogo']"></b-img>
                        </div>
                        <div>
                            <div v-if="moreResults && !loadingResults">
                                <b-button variant="success" class="buttonMarginsTop" @click="getMore" block>
                                    Load More
                                </b-button>
                            </div>
                            <div class="d-flex justify-content-center" v-else-if="!moreResults && !loadingResults">
                                <h5 class="mb-1">No More Results</h5>
                            </div>
                        </div>

                    </b-list-group-item>
                </b-list-group>
                <!-- Confirmation modal for deleting a quest. -->
                <b-modal hide-footer id="deleteQuestModal" ref="deleteQuestModal" title="Delete Quest">
                    <div v-if="activeUsers > 0"
                         class="d-block">
                        This quest is used by {{activeUsers}} users.
                    </div>
                    <div class="d-block">
                        Are you sure that you want to delete this Quest?
                    </div>
                    <b-button
                            class="mr-2 float-right"
                            variant="danger"
                            @click="deleteQuest">Delete
                    </b-button>
                    <b-button
                            @click="dismissModal('deleteQuestModal')"
                            class="mr-2 float-right">Cancel
                    </b-button>
                </b-modal>
                <b-list-group-item class="flex-column justify-content-center" v-if="loadingResults">
                    <div class="d-flex justify-content-center">
                        <b-img alt="Loading" class="align-middle loading" :src="assets['loadingLogo']"></b-img>
                    </div>
                </b-list-group-item>
                <b-list-group-item class="flex-column justify-content-center"
                                   v-if="!loadingResults && foundQuests.length === 0">
                    <div class="d-flex justify-content-center">
                        <strong>No Quests Found</strong>
                    </div>
                </b-list-group-item>
            </b-col>
            <b-col cols="12" md="4">
                <b-card class="d-none d-lg-block" v-if="!hideSideBar">
                    <found-destinations
                            v-if="showDestinations"
                            :search-public="true"
                            :profile="profile"
                            @destination-click="destination => this.destinationSelected = destination">
                    </found-destinations>
                    <objective-list
                            v-if="showYourObjectives"
                            :yourObjectives="true"
                            :profile="profile"
                            :sideBarView="true"
                            @select-objective="setSelectedObjective">
                    </objective-list>
                    <quest-search-form
                            v-if="availableQuests"
                            :profile="profile"
                            @searched-quests="quests => this.foundQuests = quests">
                    </quest-search-form>
                    <completed-quest-details
                            v-if="completedQuests"
                            :profile="profile"
                            :quest="selectedQuest"
                            @successCreate="successCreateHint">
                    </completed-quest-details>
                    <div v-if="showHintSideBar === 'Hints'">
                        <p class="mb-1 mobile-text font-weight-bold">Riddle: {{currentObjective.riddle}}</p>
                        <p class="mb-1 mobile-text">
                            Destination: {{currentObjective.destination.name}}
                        </p>
                        <list-hints
                                :objective="currentObjective"
                                :profile="profile"
                                :solved="true"
                                @add-hint="showHintSideBar = 'Create Hint'">
                        </list-hints>
                    </div>

                    <create-hint v-else-if="showHintSideBar === 'Create Hint'"
                                 :profile="profile"
                                 :objective="currentObjective"
                                 @successCreate="successCreateHint"
                                 @cancelCreate="cancelCreateHint">
                    </create-hint>
                </b-card>
            </b-col>
        </b-row>
    </div>
</template>

<script>
    import QuestItem from "./questItem";
    import FoundDestinations from "../destinations/destinationSearchList";
    import ObjectiveList from "../objectives/objectiveList";
    import QuestSearchForm from "./questSearchForm";
    import QuestAttemptSolve from "./activeQuestSolve";
    import ActiveQuestList from "./activeQuestPage";
    import CompletedQuestDetails from "./completedQuestDetails";
    import CreateHint from "../hints/createHint";
    import ListHints from "../hints/listHints";

    export default {
        name: "questList",

        props: {
            profile: Object,
            adminView: {
                default: function () {
                    return false;
                }
            },
            yourQuests: {
                default: function () {
                    return false;
                }
            },
            completedQuests: {
                default: function () {
                    return false;
                }
            },
            availableQuests: {
                default: function () {
                    return false;
                }
            },
            activeQuests: {
                default: function () {
                    return false;
                }
            },
            selectedDestination: {},
            refreshQuests: Boolean,
            hideSideBar: {
                default: function () {
                    return false;
                }
            }
        },

        data() {
            return {
                foundQuests: [],
                loadingResults: true,
                moreResults: true,
                creatingQuest: false,
                editingQuest: false,
                activeId: 0,
                questId: null,
                dismissSeconds: 3,
                dismissCountDown: 0,
                alertText: "",
                copiedQuest: null,
                showDestinations: false,
                showYourObjectives: false,
                showQuestAttemptSolve: false,
                showHintSideBar: 'hide',
                currentObjective: "",
                selectedObjectiveTemplate: {
                    id: null,
                    destination: null,
                    riddle: "",
                    radius: null
                },
                selectedObjective: {
                    id: null,
                    destination: null,
                    riddle: "",
                    radius: null
                },
                destinationSelected: {},
                perPage: 5,
                currentPage: 1,
                showLocations: false,
                fields: [
                    {key: 'riddle', label: 'Riddle'},
                    {key: 'destination.name', label: 'Destination'},
                    {key: 'radius', label: 'Radius'}
                ],
                optionViews: [
                    {value: 1, text: "1"},
                    {value: 5, text: "5"},
                    {value: 10, text: "10"},
                    {value: 15, text: "15"},
                    {value:Infinity, text:"All"}],
                questAttempts: [],
                selectedQuestAttempt: {},
                selectedQuest: {},
                activeUsers: 0,
                queryPage: 0,
                hintsDefaultPerPage: 5,
                hintsDefaultCurrentPage: 1,
                refreshHints: false,
                questsAvailable: 0,
            }
        },


        watch: {
            refreshQuests() {
                this.refreshList();
                this.getMore()
            },

            profile() {
                this.getMore();
            }
        },

        methods: {
            /**
             * Resets the foundQuests array and the query page for tab switching and lazy loading.
             */
            refreshList() {
                this.foundQuests = [];
                this.queryPage = 0;
            },


            /**
             * Used to convert the quest object into a Json object.
             */
            copyQuest(quest) {
                this.copiedQuest = JSON.parse(JSON.stringify(quest))
            },


            /**
             * Function to retrieve more quests when a user reaches the bottom of the list.
             */
            getMore() {
                if (this.yourQuests) {
                    this.queryYourQuests();
                } else if(this.completedQuests) {
                    this.queryCompletedQuests();
                } else {
                    this.queryQuests();
                }
            },


            /**
             * Send the Http request to delete the specified Quest.
             */
            deleteQuest() {
                let self = this;
                fetch('/v1/quests/' + this.questId, {
                    method: 'DELETE'
                }).then(function (response) {
                    if (!response.ok) {
                        throw response;
                    } else {
                        return response.json();
                    }
                }).then(function () {
                    self.getMore();
                    self.$refs['deleteQuestModal'].hide();
                    self.alertText = "Quest Successfully Deleted";
                    self.showAlert();
                }).catch(function (response) {
                    self.handleErrorResponse(response);
                });
            },


            /**
             * Runs a query which searches through the quests in the database and returns all.
             */
            queryQuests() {
                this.loadingResults = true;
                let self = this;
                if (this.profile.id !== undefined) {
                    fetch('/v1/quests/available/' + this.profile.id + '?page=' + this.queryPage, {
                        accept: "application/json"
                    }).then(function (response) {
                        if (!response.ok) {
                            throw response;
                        } else {
                            return response.json();
                        }
                    }).then(function (responseBody) {
                        self.loadingResults = false;
                        if (responseBody !== null && responseBody !== undefined) {
                            self.foundQuests = self.foundQuests.concat(responseBody.quests);
                            self.moreResults = self.foundQuests.length < responseBody.totalAvailable;
                            self.queryPage += 1;
                        }
                    }).catch(function (response) {
                        self.loadingResults = false;
                        self.handleErrorResponse(response);
                    });
                }
            },


            /**
             * Runs a query which searches through the quests in the database and returns only
             * quests created by the profile.
             */
            queryYourQuests() {
                let self = this;
                if (this.profile.id !== undefined) {
                    this.loadingResults = true;
                    fetch(`/v1/quests/` + this.profile.id, {})
                        .then(function (response) {
                            if (!response.ok) {
                                throw response;
                            } else {
                                return response.json();
                            }
                        }).then(function (responseBody) {
                            self.loadingResults = false;
                            self.foundQuests = responseBody;
                        }).catch(function (response) {
                            self.loadingResults = false;
                            self.handleErrorResponse(response);
                        });
                }
            },


            /**
             * Runs a query which searches through the quests in the database and returns only
             * quests started by the profile.
             */
            queryYourActiveQuests() {
                let self = this;
                if (this.profile.id !== undefined) {
                    this.loadingResults = true;
                    fetch(`/v1/quests/profiles/` + this.profile.id, {})
                        .then(function (response) {
                            if (!response.ok) {
                                throw response;
                            } else {
                                return response.json();
                            }
                        }).then(function (responseBody) {
                            self.loadingResults = false;
                            self.questAttempts = responseBody;
                        }).catch(function (response) {
                            self.handleErrorResponse(response);
                        });
                }

            },


            /**
             * Creates a new quest attempt for the selected quest and current user.
             *
             * @param questToAttempt                the selected quest which will be contained in the attempt.
             * @param viewActive                    a boolean determining whether the view should change upon starting.
             * @returns {Promise<Response | never>}
             */
            createAttempt(questToAttempt, viewActive) {
                let self = this;
                if (this.profile.id !== undefined) {
                    return fetch(`/v1/quests/` + questToAttempt.id + `/attempt/` + this.profile.id, {
                        method: 'POST'
                    }).then(function (response) {
                        if (!response.ok) {
                            throw response;
                        } else {
                            return response.json();
                        }
                    }).then(function (responseBody) {
                        if (viewActive) {
                            self.$emit('start-quest-now', responseBody);
                        } else {
                            // Remove the quest selected for later use from the list of available quests.
                            let index = self.foundQuests.indexOf(questToAttempt);
                            self.foundQuests.splice(index, 1);
                        }
                    }).catch(function (response) {
                        self.handleErrorResponse(response);
                    });
                }
            },


            /**
             * Runs a query which searches through the quests in the database and returns only
             * quests created by the profile.
             */
            queryCompletedQuests() {
                let self= this;
                if (this.profile.id !== undefined) {
                    this.loadingResults = true;
                    fetch(`/v1/quests/` + this.profile.id + `/complete`, {
                        accept: 'application/json'
                    }).then(function (response) {
                        if (!response.ok) {
                            throw response;
                        } else {
                            return response.json();
                        }
                    }).then(function (responseBody) {
                        self.loadingResults = false;
                        self.foundQuests = responseBody;
                    }).catch(function (response) {
                        self.loadingResults = false;
                        self.handleErrorResponse(response);
                    });
                }

            },


            /**
             * Changes creatingQuest to true to show the create quest window, and calls function to close edit
             * windows.
             */
            addQuest() {
                this.creatingQuest = true;
                this.editingQuest = false;
                this.cancelEdit();
            },


            /**
             * Changes the active quest id to the inputted one, and sets creatingQuest to false to hide creation
             * box.
             *
             * @param quest     the quest to be changed to.
             */
            setActiveId(quest) {
                this.copyQuest(quest);
                this.activeId = quest.id;
                this.creatingQuest = false;
                this.editingQuest = true;
            },


            /**
             * Changes the quest id to the currently selected quest id.
             * Dismisses the delete quest modal.
             *
             * @param quest         the quest to be checked for active users.
             */
            setQuest(quest) {
                this.questId = quest.id;

                this.getActiveUsers();
                this.$refs['deleteQuestModal'].show();
            },


            /**
             * Gets all users that are currently using the given quest.
             */
            getActiveUsers() {
                let self = this;
                return fetch('/v1/quests/' + this.questId + '/profiles', {
                    accept: "application/json"
                }).then(function (response) {
                    if (!response.ok) {
                        throw response;
                    } else {
                        return response.json();
                    }
                }).then(function (responseBody) {
                    self.activeUsers = responseBody.length;
                }).catch(function (response) {
                    self.handleErrorResponse(response);
                });
            },


            /**
             * Sets editingQuest to false and the active quest ID to 0 to close any open quest editing box.
             */
            cancelEdit() {
                this.getMore();
                this.editingQuest = false;
                this.activeId = 0;
                this.showDestinations = false;
                this.showHintSideBar = false;
            },


            /**
             * Displays success message and hides all edit fields.
             */
            successEdit() {
                this.getMore();
                this.editingQuest = false;
                this.activeId = 0;
                this.showDestinations = false;
                this.alertText = "Quest successfully edited";
                this.showAlert();
            },


            /**
             * Success create hint show.
             */
            successCreateHint(responseBody) {
                this.alertText = "Hint successfully created!";
                this.showAlert();
                this.showRewardToast(responseBody.reward);
                this.showHintSideBar = 'Hints';
                this.currentObjective.numberOfHints += 1;
                this.getPageHints(this.hintsDefaultCurrentPage, this.hintsDefaultPerPage);
            },


            /**
             * When the user cancels the creation of a hint.
             */
            cancelCreateHint() {
                this.showHintSideBar = 'Hints';
                this.getPageHints(this.hintsDefaultCurrentPage, this.hintsDefaultPerPage);
            },


            /**
             * Sets creatingQuest to false and emits signal to hide destination search box. clears selected destination.
             */
            cancelCreate() {
                this.getMore();
                this.creatingQuest = false;
                this.showDestinations = false;
            },


            /**
             * Sets the message for the success alert to the inputted message and runs showAlert to show the success
             * message.
             *
             * @param messageObject     the object to display.
             */
            showSuccess(messageObject) {
                this.getMore();
                this.showRewardToast(messageObject.reward);
                this.alertText = messageObject.message;
                this.showAlert();
            },


            /**
             * Show the hint sidebar for adding a hint to an objective.
             *
             * @param objective         the current objective the user is looking at hints for.
             */
            showHintSidebar(objective) {
                this.showHintSideBar = "Hints";
                this.currentObjective = objective;
                this.getPageHints(this.hintsDefaultCurrentPage, this.hintsDefaultPerPage);
            },


            /**
             * Used to dismiss the delete a quest confirmation modal.
             *
             * @param modal, the modal that is wanting to be dismissed.
             */
            dismissModal(modal) {
                this.$refs[modal].hide();
            },


            /**
             * Displays the countdown alert on the successful deletion of a quest.
             */
            showAlert() {
                this.dismissCountDown = this.dismissSeconds;
            },


            /**
             * Used to countdown the progress bar on an alert to countdown.
             *
             * @param dismissCountDown      the name of the alert.
             */
            countDownChanged(dismissCountDown) {
                this.dismissCountDown = dismissCountDown;
            },


            /**
             * Sets the objective emitted from the select objective side bar.
             *
             * @param objective         the selected objective.
             */
            setSelectedObjective(objective) {
                let newObjective = JSON.parse(JSON.stringify(objective));
                let radius = newObjective.radius;
                let radiusValue;
                let radiusList = [
                    {value: 0.005, text: "5 m"},
                    {value: 0.01, text: "10 m"},
                    {value: 0.02, text: "20 m"},
                    {value: 0.03, text: "30 m"},
                    {value: 0.04, text: "40 m"},
                    {value: 0.05, text: "50 m"},
                    {value: 0.1, text: "100 m"},
                    {value: 0.25, text: "250 m"},
                    {value: 0.5, text: "500 m"},
                    {value: 1, text: "1 Km"},
                    {value: 2.5, text: "2.5 Km"},
                    {value: 5, text: "5 Km"},
                    {value: 7.5, text: "7.5 Km"},
                    {value: 10, text: "10 Km"},
                ];
                for (let i = 0; i < radiusList.length; i++) {
                    if (radius === radiusList[i].value) {
                        radiusValue = radiusList[i];
                    }
                }
                newObjective.radius = radiusValue;
                this.selectedObjective = newObjective;
            },


            /**
             * Returns a string radius value determined by the size.
             *
             * @param radius        the radius to be changed.
             * @returns {String}     the radius as a string, represented either in meters or kilometers.
             */
            getRadiusValue(radius) {
                if (radius < 1) {
                    return radius * 1000 + " Meters"
                }
                return radius + " Km";
            },


            /**
             * Returns a string of the countries contained in the quest objectives, which indicates the quest scope.
             *
             * @param quest         the quest containing one or more countries.
             * @returns {String}     a string of the countries contained within the quest.
             */
            getQuestCountries(quest) {
                let countries = "";
                let numberOfCountries = quest.objectiveCountries.length;
                for (let i = 0; i < numberOfCountries - 1; i++) {
                    countries += quest.objectiveCountries[i] + ", ";
                }
                countries += quest.objectiveCountries[numberOfCountries - 1];

                return countries;
            },


            /**
             * Computed function used for the pagination of the table.
             *
             * @param quest         the quest containing the objectives.
             * @returns {number}    the number of rows required in the table based on number of objectives to be
             *                      displayed.
             */
            rows(quest) {
                return quest.objectives.length
            },


            /**
             * Hides or shows the quest locations given by the quest location id parameter.
             *
             * @param quest      the quest locations to hide.
             */
            showHideLocations(quest) {
                let questLocationsId = "display-" + quest.id;
                let locationsSection = document.getElementById(questLocationsId);

                if (locationsSection.style.display === "none") {
                    locationsSection.style.display = "block";
                    this.checkShowHide(quest);
                } else {
                    locationsSection.style.display = "none";
                    this.checkShowHide(quest);
                }
            }
        },

        components: {
            ListHints,
            CreateHint,
            CompletedQuestDetails,
            ActiveQuestList,
            QuestAttemptSolve,
            ObjectiveList,
            QuestItem,
            FoundDestinations,
            QuestSearchForm
        }
    }
</script>