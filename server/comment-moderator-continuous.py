import pyrebase
from mod_conf import config

#initialise connection with firebase
firebase = pyrebase.initialize_app(config)
db = firebase.database()

#create database object of study spaces
res = db.child('study spaces').get()
spaces = res.each()

#search database for comments with votes below -5.
for space in spaces:
	if (space is not None) and (db.child('study spaces').child(space.key()).child('comments').get() is not None):
		c1 = db.child('study spaces').child(space.key()).child('comments').get()
		try:
			for comment in c1.each():
				votes = db.child('study spaces').child(space.key()).child('comments').child(comment.key()).child('votes').get()
				if int(votes.val()) <= (-5):
					db.child('study spaces').child(space.key()).child('comments').child(comment.key()).remove()

		except TypeError:
			print('type error') #make logging

