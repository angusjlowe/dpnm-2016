import pyrebase
import time
from mod_conf import config

#initialise connection with firebase
firebase = pyrebase.initialize_app(config)
db = firebase.database()

#create database object of study spaces
res = db.child('study spaces').get()
error_count = 0

#search database for comments with votes below -5.
def moderate(spaces):
	for space in spaces:
		if (space is not None) and (db.child('study spaces').child(space.key()).child('comments').get() is not None):
			c1 = db.child('study spaces').child(space.key()).child('comments').get()
			try:
				for comment in c1.each():
					votes = db.child('study spaces').child(space.key()).child('comments').child(comment.key()).child('votes').get()
					if int(votes.val()) <= (-5):
						db.child('study spaces').child(space.key()).child('comments').child(comment.key()).remove()

			except TypeError:
				global error_count
				error_count += 1
				print(str(error_count)+'type error') #make logging

#function to serach database for decibel lists and update the lists
def decibels(spaces):
	for sound in spaces:
		try:
			levels = (db.child('study spaces').child(sound.key()).child('decibel_list').get().val())
			level_list = levels.split()
			tot = 0
			
			for i in range(len(level_list)):
				tot += int(level_list[i].rstrip(',')) #There is a comma after each int.

			avg = tot/(len(level_list))
			data = {"decibel": str(avg)}
			db.child('study spaces').child(sound.key()).update(data)
		
		except TypeError:
			global error_count	
			error_count += 1
			print(str(error)+'type error')

#function to search and update ratings in the database
def ratings(spaces):
	for rating in spaces:
		try:
			levels = (db.child('study spaces').child(rating.key()).child('rating_list').get().val())
			level_list = levels.split()
			tot = 0
			
			for i in range(len(level_list)):
				tot += float(level_list[i].rstrip(',')) #There is a comma after each int.

			avg = tot/(len(level_list))
			data = {"rating": str(avg)}
			db.child('study spaces').child(rating.key()).update(data)
		
		except TypeError:
			global error_count
			error_count += 1
			print(str(error_count)+'type error')

#function to call the above function on data changes
def stream_handler(post):
	global res
	db_data = res.each()
	moderate(db_data)
	decibels(db_data)
	ratings(db_data)

#create monitoring service for firebase.
#sleep time is to allow for the database stream to connect
stream = db.child('study spaces').stream(stream_handler)
time.sleep(30)
print('Server is Running')