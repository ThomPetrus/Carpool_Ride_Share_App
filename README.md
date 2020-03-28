# Carpool_Ride_Share_App
*********************************************************************************************************************************
**Car Pool and Ride Share Android Application**
Basic Driver and Passenger Oriented Application that allows individuals to meet up arrange rides based on their locations.
*********************************************************************************************************************************

**March 24**

Started Step 4 prototype for the project. Early credit goes to coding with Mitch's 2018 tutorial which has thus far provided the
basis for the project and the knowledge needed to get started. The group members thus far had to update and refactor the existing code base to work with JetPack and such. Additionally the Firebase backend and Google APIs access were set up.

**March 26**

Finished off the Map component of the base application. We have added several features to start distinguishing the application from the open source code provided. Including the ability to delete chat rooms from database by entering their name, a splash screen, prompts to indicate whether the user is a driver or a passenger and a message that indicates their intent in the chat room.

**March 28**

The prototype is taking form, we took what we learned from open source and have further developed our app to its current state. We now have a splash screen followed by role selection (Driver / Passenger). From there the user is taken to the main map view where the chatrooms / destinations are displayed upon clicking the show chatrooms button. The user can now create chat rooms by clicking anywhere on the map and naming the chatroom. There is the option to expand the map to full screen (+ buttons) as well. The user can also find themselves on the map with a click of a button. Once in a chatroom the user can send messages to the other members of the chatroom, and view the map where all the users' location is displayed.

Things to consider moving forward are:
* OPENING the chatrooms upon clicking the corresponding marker
* Limiting the naming of the chat rooms to dates / destinations
* Ensuring no identically named chatrooms 
* directions to destination markers / selected users in a chatroom
* DRIVER ability to indicate price 
* set chatroom category and allow user to filter by category.
* once a route is completely calculated it would be neat to be able to either send them to google maps, or in some other form export them to a more suitable gps based application
