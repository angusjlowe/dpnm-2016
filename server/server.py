import pyrebase_joey
import time
from oauth_client import credentials
from conf import config

# initialise connection with firebase
firebase = pyrebase_joey.initialize_app(config)
firebase.set_creds(credentials)
firebase.set_access_token(credentials.get_access_token())
db = firebase.database()

# create database object of study spaces
res = db.child('study_spaces').get()

# running error count
error_count = 0

# search database for comments with votes below -5.
def moderate(spaces):
	for space in spaces:
		if (space is not None) and (db.child('study_spaces').child(space.key()).child('comments').get() is not None):
			c1 = db.child('study_spaces').child(space.key()).child('comments').get()
			try:
				for comment in c1.each():
					votes = db.child('study_spaces').child(space.key()).child('comments').child(comment.key()).child('votes').get()
					if int(votes.val()) <= (-5):
						db.child('study_spaces').child(space.key()).child('comments').child(comment.key()).remove()

			except TypeError:
				global error_count
				error_count += 1
				print(str(error_count)+'type error') #make logging

#function to serach database for decibel lists and update the lists
def decibels(spaces):
	for sound in spaces:
		try:
			levels = (db.child('study_spaces').child(sound.key()).child('decibel_list').get().val())
			level_list = levels.split()
			tot = 0
			
			for i in range(len(level_list)):
				tot += int(level_list[i].rstrip(',')) #There is a comma after each int.

			avg = tot/(len(level_list))
			data = {"decibel": str(avg)}
			db.child('study_spaces').child(sound.key()).update(data)
		
		except TypeError:
			global error_count	
			error_count += 1
			print(str(error)+'type error')

#function to search and update ratings in the database
def ratings(spaces):
	for rating in spaces:
		try:
			levels = (db.child('study_spaces').child(rating.key()).child('rating_list').get().val())
			level_list = levels.split()
			tot = 0
			
			for i in range(len(level_list)):
				tot += float(level_list[i].rstrip(',')) #There is a comma after each int.

			avg = round(tot/(len(level_list)), 1)
			data = {"rating": str(avg)}
			db.child('study_spaces').child(rating.key()).update(data)
		
		except TypeError:
			global error_count
			error_count += 1
			print(str(error_count)+'type error')

# function to give a rough estimate of how many students are at a study space based off checkins.
def occupancy(spaces):
	for people in spaces:
		peoples = db.child('study_spaces').child(people.key()).child('occupants').get()
		if (people is not None) and (peoples.each() is not None):
			c1 = db.child('study_spaces').child(people.key()).child('occupants').get()
			try:
				count = 0
				for ocupant in c1.each():
					count += 1

				data = {'num_occupants': str(count)}
				db.child('study_spaces').child(people.key()).update(data)

			except TypeError:
				global error_count
				error_count += 1
				print(str(error_count)+'type error')
		elif (peoples.each() is None):
			data = {'num_occupants': '0'}
			db.child('study_spaces').child(people.key()).update(data)

#function to call the above function on data changes
def stream_handler(post):
	global res
	db_data = res.each()
	moderate(db_data)
	decibels(db_data)
	ratings(db_data)
	occupancy(db_data)

# inital run to ensure data exists
moderate(res.each())
decibels(res.each())
ratings(res.each())

# monitor database for changes. Sleep time allows for connection to be established.
stream = db.child('study_spaces').stream(stream_handler)
time.sleep(30)
print('Server is Running')