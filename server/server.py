import pyrebase_joey
import time
import shutil
from oauth_client import credentials
from oauth_client import refresh_credentials as refresh
from requests.exceptions import HTTPError
from conf import config


# initialise connection with firebase
firebase = pyrebase_joey.initialize_app(config)
# firebase.set_creds(credentials)
# firebase.set_access_token(credentials.get_access_token())
db = firebase.database()

# create database object of study spaces
res = db.child('study_spaces').get()

# running error count
error_count = 0

# search database for comments with votes below -5.
def moderate(spaces, post=None):
	global error_count
	if post is None:
		for space in spaces:
			space1 = db.child('study_spaces').child(space.key()).child('comments').get()
			if (space is not None) and (space1.each() is not None):
				c1 = db.child('study_spaces').child(space.key()).child('comments').get()
				try:
					for comment in c1.each():
						votes = db.child('study_spaces').child(space.key()).child('comments').child(comment.key()).child('votes').get()
						if int(votes.val()) <= (-5):
							db.child('study_spaces').child(space.key()).child('comments').child(comment.key()).remove()

				except TypeError:
					error_count += 1
					print(str(error_count)+'type errors. Error occured in moderate 1') #make logging

			elif(space1.each() is None):
				break

	else:
		try:
			votes = db.child('study_spaces').child(post[1]).child('comments').child(post[3]).child('votes').get()
			if int(votes.val()) <= (-5):
				db.child('study_spaces').child(post[1]).child('comments').child(post[3]).remove()

		except TypeError:
			error_count += 1
			print(str(error_count)+' type errors. Error occured in Moderate 2')

#function to serach database for decibel lists and update the lists
def decibels(spaces, post=None):
	if post is None:
		for sound in spaces:
			try:
				levels = db.child('study_spaces').child(sound.key()).child('decibel_list').get().val()
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
				print(str(error)+'type errors. Error occured in decibels 1')

			except ZeroDivisionError:
				data = {"decibel": "0"}
				db.child('study_spaces').child(sound.key()).update(data)
	else:
		levels = db.child('study_spaces').child(post[1]).child('decibel_list').get().val()
		level_list = levels.split()
		tot = 0
				
		for i in range(len(level_list)):
			tot += int(level_list[i].rstrip(',')) #There is a comma after each int.

		avg = tot/(len(level_list))
		data = {"decibel": str(avg)}
		db.child('study_spaces').child(post[1]).update(data)

#function to search and update ratings in the database
def ratings(spaces, post=None):
	if post is None:
		for rating in spaces:
			try:
				levels = db.child('study_spaces').child(rating.key()).child('rating_list').get().val()
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
				print(str(error_count)+'type errors. Error occured in ratings 1')
	else:
		levels = db.child('study_spaces').child(post[1]).child('rating_list').get().val()
		level_list = levels.split()
		tot = 0
		
		for i in range(len(level_list)):
			tot += float(level_list[i].rstrip(',')) #There is a comma after each int.

		avg = round(tot/(len(level_list)), 1)
		data = {"rating": str(avg)}
		db.child('study_spaces').child(post[1]).update(data)

# function to give a rough estimate of how many students are at a study space based off checkins.
def occupancy(spaces, post=None):
	global error_count
	if post is None:
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
					error_count += 1
					print(str(error_count)+'type errors. Error occured in occupancy 1')
			elif (peoples.each() is None):
				data = {'num_occupants': '0'}
				db.child('study_spaces').child(people.key()).update(data)
	else:
		c1 = db.child('study_spaces').child(post[1]).child('occupants').get()
		try:
			if c1.each() is None:
				data = {'num_occupants': '0'}
				db.child('study_spaces').child(post[1]).update(data)
			else:
				count = 0
				for ocupant in c1.each():
					count += 1

				data = {'num_occupants': str(count)}
				db.child('study_spaces').child(post[1]).update(data)

		except TypeError:
			error_count += 1
			print(str(error_count)+'type errors. Error occured in occupancy 2')

#function to call the above function on data changes
def stream_handler(post):
	global res
	db_data = res.each()

	# get path to database location of the change, then sort to correct database function(s)
	location = str(post['path']).split('/')

	if 'comments' in location:
		moderate(db_data, location)
	elif 'rating_list' in location:
		ratings(db_data, location)
	elif 'occupants' in location:
		occupancy(db_data, location)
	elif 'decibel_list' in location:
		decibels(db_data, location)
	elif 'decibel' in location:
		decibel(db_data)
	elif 'num_occupants' in location:
		occupancy(db_data)
	elif 'rating' in location:
		ratings(db_data)
	else:
		print('unmoderated data change, checking database')
		ratings(db_data)
		decibels(db_data)
		occupancy(db_data)

# inital run to ensure data exists
moderate(res.each())
decibels(res.each())
ratings(res.each())
occupancy(res.each())

#variable to control loop mesage
firstrun = True
# monitor database for changes. Sleep time allows for connection to be established.
while True:
	try:
		#stream is a live stream of the database
		stream = db.child('study_spaces').stream(stream_handler)
		time.sleep(15)
		if firstrun:
			print('server is running')
		else:
			print('server reconnected')

	except HTTPError:
		#destroy old credentials
		print('removing old directory')
		try:
			shutil.rmtree('__pycache__')
		except FileNotFoundError:
			continue

		#regenerate credentials and reinitialize firebase database
		credentials = refresh()
		firebase = pyrebase_joey.initialize_app(config)
		firebase.set_creds(credentials)
		firebase.set_access_token(credentials.get_access_token())
		
		db = firebase.database()
		res = db.child('study_spaces').get()
		firstrun = False
		continue

	break