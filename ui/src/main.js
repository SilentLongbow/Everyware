import Vue from 'vue'
import App from './App.vue'
import BootstrapVue from 'bootstrap-vue'
import router from './router'
import VueRouter from 'vue-router';
import VueSlider from 'vue-slider-component';
import ScrollDown from './assets/scroll-down';

import assets from './assets/assets';

import 'bootstrap/dist/css/bootstrap.css'
import 'bootstrap-vue/dist/bootstrap-vue.css'
import 'vue-slider-component/theme/default.css'

import RewardToast from "./components/helperComponents/rewardToast";
import ErrorToast from "./components/helperComponents/errorToast";

Vue.use(VueRouter);
Vue.use(ScrollDown);

Vue.component('VueSlider', VueSlider);

Vue.config.productionTip = false;
Vue.use(BootstrapVue);

/**
 * Allows use of these methods in every single component.
 */
Vue.mixin({
    /** Handles the events for if the user is viewing on mobile. **/
    created() {
        window.addEventListener("resize", this.handleViewOnMobile);
    },

    /** Handles the events for if the user is no longer viewing on mobile. **/
    destroyed() {
        window.removeEventListener("resize", this.handleViewOnMobile);
    },

    components: {
        RewardToast,
        ErrorToast
    },

    mounted() {
        this.handleViewOnMobile();
    },

    methods: {
        /**
         * Displays a toast for each action that awards points and each badge progress achieved.
         *
         * @param rewardJson        the Json reward resulting from an action.
         */
        showRewardToast(rewardJson) {
            if((rewardJson.hasOwnProperty('badgesAchieved') && rewardJson.badgesAchieved.length) || (rewardJson.hasOwnProperty('pointsRewarded'))) {
                for (let j = 0; j < rewardJson.pointsRewarded.length; j++) {
                    if (rewardJson.pointsRewarded[j]) {
                        const toastElement = this.$createElement;

                        const toastContent = toastElement(
                            'reward-toast',
                            {props: {pointsRewarded: rewardJson.pointsRewarded[j]}}
                        );

                        this.$bvToast.toast([toastContent], {
                            title: this.possibleActions[rewardJson.pointsRewarded[j].name],
                            autoHideDelay: 5000,
                            appendToast: true,
                            solid: true,
                            variant: 'success'
                        });
                    }
                }
                for (let i = 0; i < rewardJson.badgesAchieved.length; i++) {
                    if (rewardJson.badgesAchieved[i]) {
                        const toastElement = this.$createElement;
                        const toastContent = toastElement(
                            'reward-toast',
                            {props: {badgeAchieved: rewardJson.badgesAchieved[i]}}
                        );

                        this.$bvToast.toast([toastContent], {
                            title: "Congratulations!",
                            autoHideDelay: 5000,
                            appendToast: true,
                            solid: true,
                            variant: 'success'
                        });
                    }
                }
            }
        },


        /**
         * Displays a toast on the page if the user increases their login streak.
         *
         * @param streakValue   the value of the user's current login streak to be displayed on the toast.
         */
        showStreakToast(streakValue) {
            const toastElement = this.$createElement;
            const toastContent = toastElement(
                'reward-toast',
                {props: {streakValue: streakValue}}
            );

            this.$bvToast.toast([toastContent], {
                title: "Congratulations!",
                autoHideDelay: 5000,
                appendToast: true,
                solid: true,
                variant: 'success'
            });
        },


        /**
         * Handles any unsatisfactory response from the backend after sending a request. If the response body is a Json
         * form, then display a toast containing the response Json messages. Otherwise displays a generic error toast.
         *
         * @param errorResponse     the Json body of the response error.
         */
        handleErrorResponse(errorResponse) {
            // Checks the response is in a Json form, otherwise show a generic message.
            if (errorResponse.headers.get("content-type").indexOf("application/json") !== -1) {
                errorResponse.json().then(responseBody => {
                        if(responseBody[0].hasOwnProperty('message')) {
                            for (let i = 0; i < responseBody.length; i++) {
                                this.showErrorToast(responseBody[i].message);
                            }
                        } else {
                            this.showErrorToast("An unexpected error occurred.")
                        }
                    }

                );
            } else {
                this.showErrorToast("An unexpected error occurred.");
            }
        },


        /**
         * Takes a message string to display an error toast if any backend response error occurs.
         *
         * @param message   string message to be displayed as an error.
         */
        showErrorToast(message) {
            const toastElement = this.$createElement;

            const toastContent = toastElement(
                'error-toast',
                {props: {errorMessage: message}}
            );

            this.$bvToast.toast([toastContent], {
                title: "Oh dear, an error occurred",
                autoHideDelay: 10000,
                appendToast: true,
                solid: true,
                variant: 'danger'
            });
        },


        /**
         * Iterates through the response body and retrieves the error messages for each error.
         * This can be called from anywhere, and returns a string with a newline character separating each message.
         *
         * @param responseBody  the Json response body containing the list of errors.
         * @returns {string}    the error message string with each error message separated by a new line character.
         */
        getErrorMessage(responseBody) {
            let errorString = "";
            for (let errorMessage = 0; errorMessage < responseBody.length; errorMessage++) {
                errorString += responseBody[errorMessage].message + "\n";
            }
            return errorString;
        },


        /**
         * Runs every five minutes to check if a user is active.
         */
        updateActivity() {
            let self = this;
            let time = this.MINUTE * 5;      // Runs every 5 minutes
            this.setLastSeenDate();
            setTimeout(function() {
                self.updateActivity();
            }, time)
        },


        /**
         * Sets the date the user was last seen as active in the backend.
         * Displays a toast if the user has increased their login streak or has achieved progress on their badges.
         */
        setLastSeenDate() {
            let date = new Date();
            let offset = new Date().getTimezoneOffset();
            let self = this;
            fetch('/v1/achievementTracker/updateLastSeen', {
                method: 'POST',
                headers: {'content-type': 'application/json'},
                accept: 'application/json',
                body: JSON.stringify({clientDate: date, dateOffset: offset})
            }).then(function (response) {
                if (!response.ok) {
                    throw response;
                } else {
                    return response.json();
                }
            }).then(function (responseBody) {
                if (responseBody.hasOwnProperty("currentStreak")) {
                    self.showStreakToast(responseBody.currentStreak);
                }
                self.showRewardToast(responseBody.reward);
            }).catch(function () {

            });
        },


        /**
         * Handles if the user if viewing the page on a mobile device.
         */
        handleViewOnMobile() {
            this.onMobile = window.innerWidth <= 991;
        }
    },

    computed: {
        assets() {
            return assets
        }
    },

    data() {
        return {
            possibleActions: {
                CHECKED_IN: 'Checked In',
                DESTINATION_CREATED: 'Destination Created',
                QUEST_CREATED: 'Quest Created',
                QUEST_COMPLETED: 'Quest Completed',
                TRIP_CREATED: 'Trip Created',
                HINT_CREATED: 'Hint Created',
                RIDDLE_SOLVED_NO_HINT: 'Riddle Solved with No Hints',
                RIDDLE_SOLVED_ONE_HINT: 'Riddle Solved with Only One Hint',
                RIDDLE_SOLVED_TWO_HINT: 'Riddle Solved with Two Hints'
            },
            MINUTE: 60000,
            onMobile: false,
            streakUpdated: false
        }
    }
});

new Vue({
    el: '#app',
    router: router,
    template: '<App/>',
    components: { App }
});

/* Pages that are not to be displayed on mobile. */
let restrictedPages = ['/destinations','/admin', '/trips'];

/* If the user is viewing a page on mobile that is restricted, redirect them to the profile page. */
router.beforeEach((to, from, next) => {
    if (restrictedPages.includes(to.path) && viewingOnMobile()) {
        router.push(from.path);
    } else {
        next();
    }
});

/* If the user is viewing a page on mobile that is restricted, redirect them to the profile page. */
if (restrictedPages.includes(window.location.pathname) && viewingOnMobile()) {
    router.push("/profile");
}

/* Detects if the user is viewing on a mobile device (width is 991). */
$(window).on("resize", viewingOnMobile);
function viewingOnMobile( e ) {
    return $(window).width() <= 991;
}