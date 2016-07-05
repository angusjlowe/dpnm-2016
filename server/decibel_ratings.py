import pyrebase
from mod_conf import config

#initialise connection with firebase
firebase = pyrebase.initialize_app(config)
db = firebase.database()

#create database object of study spaces
res = db.child('study spaces').get()
spaces = res.each()

#get decibel readings from database and compute average
for sound in spaces:
		try:
			levels = (db.child('study spaces').child(sound.key()).child('decibel_list').get().val())
			level_list = levels.split()
			tot = 0
			for i in range(len(level_list)):
				tot += int(level_list[i].rstrip(',')) #There is a comma after each int.

			avg = tot/(len(level_list))
			db.child('study spaces').child(sound.key()).child('decibel').push(avg)
		except TypeError:
			print('type error')