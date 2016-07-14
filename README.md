
#Student Study Space
##Main Features

The following describes a high-level conceptual design of the features which will be implemented in the application, from the
front-end across to the back-end.

###Google Maps API
- Manual addition of study spots available by clicking on spots on the map
- Manual check-in option enabled by clicking on existing study spot (bottom of pop-up dialog box)
- Map API will automatically zoom based on current location

###Details, Adding New Locations, and Checking In
- Check-in initiates environment reading (sound detection) and an actual check-in so the database
stores the number of people who are in a location and makes an estimate of how full it is based on the
number of users who check-in and the capacity of the study spaces
- When adding a new location, mandatory categories of information will include whether or not talking is
permitted and (roughly) the size of the study space (small medium large ranking)
- Users will be able to submit comments on existing study spaces through a pop-up that appears when a location is
clicked
- Comments will be moderated by a voting system on the comments (if they receive five or more downvotes then they will be removed)
- Each study spot will have a ranking (using a five-star system in the pop-up box once again)
- Study spaces can be added to a user's favourites or be looked up in a user's history (using time stamps)
- User's will use google or Facebook as authentication, through Firebase

###UI
- Add study spaces by clicking on check-in button that will appear normally on the map
- When the user's location is within another study space, change icon to reflect a check-in
- Also, options to manually override the check-in or adding a location will be implemented
- Both noise-level and capacity will be indicated by a meter
- All information will be highly readable in the pop-up dialog boxes
- User's will have the option to sign out or view their favourite/recent study spots in a slide in menu
