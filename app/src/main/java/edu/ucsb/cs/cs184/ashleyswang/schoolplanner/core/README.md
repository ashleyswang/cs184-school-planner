
# Core API Code Documentation
The Core API provides the core design outline of the components needed to make the application and retains a copy of the information stored in the Firebase database and pushes changes to the database. The Core API is primarily used in connecting the UI to the Firebase database so information is accurately reflected both in the phone application as well as the database. 

## Documentation for Development
The following section contains the class structure of the Core API and the public variables and methods contained in each class. More detailed documentation can be found in the source file for each class/interface. 

---
### Controller Class
The controller class holds all the information needed for the app for a certain user. It contains information of all the terms a single user has made.

**Public Variables**
* `user: String` | user ID stored in the database
* `db: DatabaseReference` | database reference for Controller 
* `terms: MutableMap<String, Term>` | map of Term objects created by user

**Public Methods**
* `Controller(user: String)` <br>Constructor for Controller
	* _Parameters:_  `user` | user ID stored in the database 
* `addTerm(): Term` <br> Creates and adds new Term object to Controller's associated terms and database
	* _Parameters:_  None
	* _Returns:_ created Term object associated with user
* `removeTerm(term: Term): Term?` <br> Removes Term from Controller's associated terms and database
	* _Parameters:_  `term` | Term object to be removed
	* _Returns:_ removed Term object if successfully removed or `null`
---
### Term Class
A term object represents a single school term (ie. a quarter or semester) and contains information about the courses and events for that term. 

**Public Variables**
* `id: String` | key ID used in terms map in Controller
* `db: DatabaseReference` | database reference for Term 
* `control: Controller` | Controller object for Term
* `name: String` | name given to Term (displayed on UI)
* `start: LocalDateTime?` | _optional_ : start date of Term
* `end: LocalDateTime?` | _optional_ : end date of Term
* `courses: MutableMap<String, Course>` | map of Course objects for Term
* `events: MutableMap<String, Event>` | map of Event objects for Term (not including Course events)

**Public Methods**
* `Term(control: Controller)` <br> Constructor for new Term to add to database
	* _Parameters:_  `control` | Controller associated with Term object
* `Term(control: Controller, key: String, value: Map<String, Any>)` <br> Constructor for new Term already existing in database (eg. from database updates)
	* _Parameters:_
	    * `control` | Controller associated with Term object
	    * `key` | string to use for Term ID
	    * `value` | map of values of Term variables stored in database
* `addCourse(): Course` <br> Creates and adds new Course object to Term's associated courses and database
	* _Parameters:_  None
	* _Returns:_ created Course object associated with Term
* `removeCourse(course: Course): Course?` <br> Removes Course from Term's associated courses and database
	* _Parameters:_  `course` | Course object to be removed
	* _Returns:_ removed Course object if successfully removed or `null`
* `addEvent(): Event` <br> Creates and adds new Event object to Term's associated events and database
	* _Parameters:_  None
	* _Returns:_ created Event object associated with Term
* `removeEvent(event: Event): Event?` <br> Removes Event from Term's associated courses and database
	* Parameters:  `event` | Event object to be removed
	* Returns: removed Event object if successfully removed or `null`

---
### Course Class
A course object represents a single course taken in a particular term and contains information about the assignments, meetings, and events for that course. Optionally, it can contain other course information such as instructor and office hours information. 

**Public Variables**
* `id: String` | key ID used in courses map in Term
* `db: DatabaseReference` | database reference for Course 
* `term: Term` | Term object associated with Course
* `name: String` | name given to Course (displayed on UI)
* `assign: MutableMap<String, Assignment>` | map of Assignment objects for Course
* `meet: MutableMap<String, Meeting>` | map of Meeting objects for Course
* `events: MutableMap<String, Event>` | map of Event objects for Course

**Public Methods**
* `Course(term: Term)` <br> Constructor for new Course to add to database
	* _Parameters:_  `term` | Term associated with Course object
* `Course(term: Term, key: String, value: Map<String, Any>)` <br> Constructor for new Course already existing in database (eg. from database updates)
	* _Parameters:_
	    * `term` | Term associated with Course object
	    * `key` | string to use for Course ID
	    * `value` | map of values of Course variables stored in database
* `addAssign(): Assignment` <br> Creates and adds new Assignment object to Course and database
	* _Parameters:_  None
	* _Returns:_ created Assignment object associated with Course
* `removeAssign(assign: Assignment): Assignment?` <br> Removes Assignment from Course and database
	* _Parameters:_  `assign` | Assignment object to be removed
	* _Returns:_ removed Assignment object if successfully removed or `null`
* `addMeet(): Meeting` <br> Creates and adds new Meeting object to Course and database
	* _Parameters:_  None
	* _Returns:_ created Meeting object associated with Course
* `removeMeet(meet: Meeting): Meeting?` <br> Removes Meeting from Course and database
	* _Parameters:_  `meet` | Meeting object to be removed
	* _Returns:_ removed Meeting object if successfully removed or `null`
* `addEvent(): Event` <br> Creates and adds new Event object to Course and database
	* _Parameters:_  None
	* _Returns:_ created Event object associated with Course
* `removeEvent(event: Event): Event?` <br> Removes Event from Course and database
	* _Parameters:_  `event` | Event object to be removed
	* _Returns:_ removed Event object if successfully removed or `null`

---
### Event Class
An event object represents an event that must be displayed on the Calendar. The event may have some duration or be a deadline. For an event, we use an associated `Scope` Interface that is used to filter events as specific calendars. A scope can either be a Term or Course object. 

**Public Variables**
* `id: String` | key ID used in events map in Scope (Term or Course)
* `db: DatabaseReference` | database reference for Event
* `scope: Scope` | Scope of event (used to filter events as different calendars)
* `name: String` | name given to Event (displayed on UI)
* `start: LocalDateTime` | start datetime of event
* `end: LocalDateTime?` | _optional_ : end datetime of event, if `null` event is a deadline
* `recur: RecurringEvent?` | _optional_ : RecurringEvent object to generate next recurrence

**Public Methods**
* `Event(scope: Scope)` <br> Constructor for new Event to add to database
	* _Parameters:_  `scope` | Scope associated with Event object
* `Event(scope: Scope, key: String, value: Map<String, Any>)` <br> Constructor for new Event already existing in database (eg. from database updates)
	* _Parameters:_
	    * `scope` | Scope associated with Event object
	    * `key` | string to use for Event ID
	    * `value` | map of values of Event variables stored in database
 * `getDuration(): Duration` <br> Returns a Duration object of the duration of the event

---
### RecurringEvent Interface
Classes implementing the RecurringEvent interface are used to generate the dates of events in the recurrence. There are two classes that extend the interface: WeeklyEvent and DailyEvent.

**Public Variables**
* `event: Event` | Event object of which the recurrence is based off
* `start: LocalDateTime` | start datetime of recurrence
* `end: LocalDateTime?` | _optional_ : end datetime of recurrence
* `type: String` | type of recurrence: `"weekly"` or `"daily"`
* `canceled: MutableSet<LocalDateTime>` | set of dates that have been removed from recurrence

**Public Methods**
* `Constructor(event: Event)` <br> Constructor for new RecurringEvent to add to database
	* _Parameters:_  `event` | Event object to base recurrence from
* `Contructor(event: Event, info: Map<String, Any>)` <br> Constructor for new RecurringEvent already existing in database (eg. from database updates)
	* _Parameters:_
	    * `event` | Event object to base recurrence from
	    * `info` | map of values of object variables stored in database
 * `getNextDate(): LocalDateTime?` <br> Returns datetime of next occurrence from the current datetime or `null` if none exists
 * `getDates(from: LocalDateTime, to: LocalDateTime): ArrayList<LocalDateTime>`<br> Returns ArrayList of occurrences' datetimes in between given dates
 * `removeDate(date: LocalDateTime)` <br> Adds date to canceled so it will not be generated by `getNextDate()` or `getDates()`

**WeeklyEvent Class Additional Methods**
* `days: MutableSet<DayOfWeek>` | 	set of days to repeat recurrence on (ie. repeat every Monday)
* `addDays(vararg days: DayOfWeek)` | Adds DayOfWeek to recurrence 
* `removeDays(vararg days: DayOfWeek)` | Removes DayOfWeek from recurrence

---
### Assignment and Meeting Class
An assignment object represents an assignment for the course and keeps track of the associated event object for the assignment and other options such as the completion status and received grade of the assignment. A meeting object represents a meeting for the course (ie. lecture or section) and keeps track of the event and other information associated with the meeting. 

**Public Variables**
* `id: String` | key ID used in assignment/meeting map in Course
* `db: DatabaseReference` | database reference for object
* `course: Course` | Course object associated with Assignment/Meeting
* `name: String` | name given to associated Event (displayed on UI)
* `eventId: String` | key ID of associated event used in events map in Course
* `event: Event` | associated Event object
* `options: [Inner].Options` | inner class Options object for Assignment/Meeting

**Public Methods**
* `Constructor(course: Course, eventId: String)` <br> Constructor for new object to add to database
	* _Parameters:_  
		* `course` | Course associated with object
		* `eventId` | key ID of associated event used in events map in Course
* `Constructor(course: Course, key: String, value: Map<String, Any>)` <br> Constructor for new object already existing in database (eg. from database updates)
	* _Parameters:_
	    * `course` | Course associated with Course object
	    * `key` | string to use for object ID
	    * `value` | map of values of object variables stored in database

**Assignment.Options Inner Class**
* `weight: Float?` | _optional_ : grade weight of assignment
* `grade: Float?` | _optional_ : received grade of assignment
* `complete: Boolean?` | _optional_ : completion status of assignment

**Meeting.Options Inner Class**
* `mandatory: Boolean?` | _optional_ : mandatory status of meeting
* `link: String?` | _optional_ : meeting link